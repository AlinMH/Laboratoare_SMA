package my;

import java.util.*;

import base.Agent;
import blocksworld.*;
import blocksworld.Stack;

/**
 * Class implementing specific functionality for the environment.
 */
public class MyBlocksWorldEnvironment extends BlocksWorldEnvironment
{
	String $token = null;

	int conflicts = 0;

	final int MAX_CONFLICTS = 20;

	/**
	 * @param world - the initial world.
	 */
	public MyBlocksWorldEnvironment(BlocksWorld world)
	{
		super(world);
	}

	@Override
	public boolean step()
	{
		System.out.println(getAgents() + "\n");

		Map<AgentData, BlocksWorldAction> actionMap = new HashMap<>();
		for(AgentData ag : agents)
		{
			Station agentStation = ag.getStation();
			Stack currentStack = worldstate.getStacks().get(stations.indexOf(agentStation));
			int position = stations.indexOf(agentStation);
			if(position != worldstate.getStacks().indexOf(currentStack))
				throw new RuntimeException("stations and worldstate disaligned.");
			BlocksWorldAction act = (BlocksWorldAction) ag.getAgent()
					.response(new BlocksWorldPerceptions(currentStack, agentStation, ag.hasPreviousActionSucceeded()));
			System.out.println(ag.getAgent() + " opts for " + act);
			ag.setPreviousActionSuccessful();

			// check if action is legal with respect to the state of the agent.
			if((act.getType() == BlocksWorldAction.Type.PUTDOWN || act.getType() == BlocksWorldAction.Type.STACK) && (ag.getHolding() == null
					|| !ag.getHolding().equals(act.hasOneArg() ? act.getArgument() : act.getFirstArgument())))
				throw new IllegalStateException(
						"Can't work with that block: " + (act.hasOneArg() ? act.getArgument() : act.getFirstArgument())
								+ "; agent is holding: " + ag.getHolding());

			if((act.getType() == BlocksWorldAction.Type.PICKUP || act.getType() == BlocksWorldAction.Type.UNSTACK) && ag.getHolding() != null)
				throw new IllegalStateException("Agent already busy with block: " + ag.getHolding());

			actionMap.put(ag, act);
		}

		int nCompleted = this.performActions(actionMap);

		if(nCompleted == agents.size())
			return true;
		return false; // return true when the simulation should stop.
	}

	@Override
	protected int performActions(Map<AgentData, BlocksWorldAction> actionMap)
	{
		// TODO solve conflicts if there are multiple agents.
		if ($token == null) {
			int index = new Random().nextInt(actionMap.size());
			int i = 0;
			for (AgentData agentData : actionMap.keySet()) {
				if (i == index) {
					$token = agentData.getAgent().toString();
					break;
				}
				i++;
			}
		}

		List<BlocksWorldAction> agentActions = new ArrayList<>();
		List<AgentData> agents = new ArrayList<>();

		for (AgentData agentData : actionMap.keySet()) {
			agentActions.add(actionMap.get(agentData));
			agents.add(agentData);
		}

		if (areConflicting(agentActions.get(0), agentActions.get(1))) {
			if (hasToken(agents.get(0))) {
				actionMap.put(agents.get(1), new BlocksWorldAction(BlocksWorldAction.Type.NONE));
				$token = agents.get(1).getAgent().toString();
			} else {
				actionMap.put(agents.get(0), new BlocksWorldAction(BlocksWorldAction.Type.NONE));
				$token = agents.get(0).getAgent().toString();
			}
			conflicts += 1;
		} else {
			conflicts = 0;
		}

		if (conflicts == MAX_CONFLICTS) {
			System.out.println("Can't sovle map. To many conflicting in a row " + MAX_CONFLICTS);
			System.exit(0);
		}

		return super.performActions(actionMap);
	}
	
	@Override
	protected boolean hasToken(AgentData agent)
	{
		// TODO return if an agent has the token or not.
		return agent.getAgent().toString().equals($token);
	}

	private boolean areConflicting(BlocksWorldAction action1, BlocksWorldAction action2)
	{
		switch (action1.getType()) {
			case PICKUP:
				if (action1.equals(action2)) {
					return true;
				}
				if (action2.getType().equals(BlocksWorldAction.Type.STACK)) {
					if (action2.getSecondArgument().equals(action1.getFirstArgument())) {
						return true;
					}
				}
				break;

			case UNSTACK:
				if (action1.equals(action2)) {
					return true;
				}
				if (action2.getType().equals(BlocksWorldAction.Type.STACK)) {
					if (action1.getFirstArgument().equals(action2.getSecondArgument())) {
						return true;
					}
				}
				break;

			case STACK:
				if (action2.getType().equals(BlocksWorldAction.Type.STACK)) {
					if (action1.getSecondArgument().equals(action2.getSecondArgument())) {
						return true;
					}
				}

				if (action2.getType().equals(BlocksWorldAction.Type.UNSTACK) ||
					action2.getType().equals(BlocksWorldAction.Type.PICKUP)) {
					if (action1.getSecondArgument().equals(action2.getFirstArgument())) {
						return true;
					}
				}
				break;
		}
		return false;
	}
}
