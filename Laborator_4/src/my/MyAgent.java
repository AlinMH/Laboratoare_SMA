package my;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import base.Action;
import base.Agent;
import base.Perceptions;
import blocksworld.BlocksWorldAction.Type;
import blocksworld.BlocksWorldEnvironment.Station;
import blocksworld.*;

/**
 * Agent to implement.
 */
public class MyAgent implements Agent {
	/**
	 * Name of the agent.
	 */
	String agentName;

	List<BlocksWorldAction> currentPlan;
	Stack currentStack;
	Station currentStation;

	List<Predicate> goals;

	// this only makes sense for 1 block stacks
	Map<Block, Station> blockPositions;
	Map<Station, Stack> stackPositions;

	Set<Block> requiredBlocks;

	boolean allDown;
	boolean needsRevision;
	boolean canContinue;

	BlocksWorldAction lastAction;
	BlocksWorldAction prevLastAction;

	/**
	 * Constructor for the agent.
	 *
	 * @param desiredState - the desired state of the world.
	 * @param name         - the name of the agent.
	 */
	public MyAgent(final BlocksWorld desiredState, final String name) {
		agentName = name;
		currentPlan = new ArrayList<>();
		blockPositions = new HashMap<>();
		goals = new ArrayList<>();
		requiredBlocks = new HashSet<>();
		canContinue = true;
		stackPositions = new HashMap<>();

		// transform desired word in a list of predicates
		// keep track of required blocks
		for (Stack stack : desiredState.getStacks()) {
			List<Predicate> predicates = stack.getPredicates();
			requiredBlocks.addAll(stack.getBlocks());
			Collections.reverse(predicates);
			goals.addAll(predicates);
		}

		List<Predicate> predicatesToDelete = new ArrayList<>();
		for (Predicate predicate : goals) {
			if (predicate.getType() != Predicate.Type.ON) {
				predicatesToDelete.add(predicate);
			}
		}
		goals.removeAll(predicatesToDelete);
	}


	@Override
	public Action response(Perceptions input) {
		@SuppressWarnings("unused")
		BlocksWorldPerceptions perceptions = (BlocksWorldPerceptions) input;

		// update the current position
		currentStation = perceptions.getCurrentStation();
		currentStack = perceptions.getVisibleStack();

		if (needsRevision) {
			reviseBeliefs();
		}

		if (currentPlan.isEmpty()) {
			plan();
		}


		prevLastAction = lastAction;
		lastAction = currentPlan.remove(0);
		return lastAction;
	}


	protected void reviseBeliefs()
	{
		// if we happen to get to a previous visited station while "exploring" we found all the stacks
		if (stackPositions.containsKey(currentStation) && lastAction.getType() == Type.NEXT_STATION
				&& prevLastAction.getType() == Type.NEXT_STATION)
		{
			// we put down all the individual blocks
			allDown = true;

			canContinue = requiredBlocks.isEmpty();
			if (!canContinue) {
				System.out.println("Impossible desire!");
			}
		}

		stackPositions.put(currentStation, currentStack);
	}


	@SuppressWarnings("static-method")
	protected void plan()
	{
		if (!canContinue) {
			currentPlan.add(new BlocksWorldAction(Type.AGENT_COMPLETED));
		} else if (!allDown) {
			if (currentStack.isSingleBlock()) {
				currentPlan.add(new BlocksWorldAction(Type.NEXT_STATION));
				blockPositions.put(currentStack.getTopBlock(), currentStation);
				requiredBlocks.remove(currentStack.getTopBlock());
				needsRevision = true;
			} else {
				Block topBlock = currentStack.getTopBlock();
				Block bottomBlock = currentStack.getBelow(topBlock);
				requiredBlocks.remove(topBlock);
				currentPlan.add(new BlocksWorldAction(Type.UNSTACK, topBlock, bottomBlock));
				currentPlan.add(new BlocksWorldAction(Type.PUTDOWN, topBlock));
				currentPlan.add(new BlocksWorldAction(Type.NEXT_STATION));
			}
		} else {
			if (currentStack.isSingleBlock()) {
				blockPositions.put(currentStack.getTopBlock(), currentStation);
			}

			if (goals.isEmpty()) {
				currentPlan.add(new BlocksWorldAction(Type.AGENT_COMPLETED));
			}
			else {
				Predicate nextGoal = goals.get(0);

				if (nextGoal.getType() == Predicate.Type.ON){
					Block top = nextGoal.getFirstArgument();
					Block bottom = nextGoal.getSecondArgument();
					Station topStation = blockPositions.get(top);
					Station bottomStation = blockPositions.get(bottom);
					if (topStation == null | bottomStation == null){
						currentPlan.add(new BlocksWorldAction(Type.NEXT_STATION));
					} else {
						currentPlan.add(new BlocksWorldAction(Type.GO_TO_STATION, topStation));
						currentPlan.add(new BlocksWorldAction(Type.PICKUP, top));
						currentPlan.add(new BlocksWorldAction(Type.GO_TO_STATION, bottomStation));
						currentPlan.add(new BlocksWorldAction(Type.STACK, top, bottom));
						blockPositions.put(top, bottomStation);
						goals.remove(0);
					}
				}
			}
		}
	}

	@Override
	public String statusString()
	{
		return currentPlan.toString();
	}

	@Override
	public String toString()
	{
		return "" + agentName;
	}
}
