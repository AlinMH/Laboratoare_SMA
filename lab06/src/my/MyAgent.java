package my;

import base.Action;
import base.Agent;
import base.Perceptions;
import blocksworld.*;
import blocksworld.Stack;
import blocksworld.BlocksWorldAction.Type;
import javafx.util.Pair;

import java.util.*;

/**
 * Agent to implement.
 */
public class MyAgent implements Agent {
	private static final int BASE_BLOCK_PLAN = 1;
	private static final int NO_PLAN = 2;
	private static final int BLOCK_PLAN = 3;

	/**
	 * Name of the agent.
	 */
	String agentName;

	BlocksWorld desiredState;

	List<BlocksWorldAction> planActions = new ArrayList<>();

	HashMap<BlocksWorldEnvironment.Station, Stack> stationStackHashMap = new HashMap<>();

	BlocksWorldPerceptions perceptions;

	List<BlocksWorldAction> allPlanActions;

	BlocksWorldAction prevAction;

	Block toCreateBaseBlock;

	HashMap<BlocksWorldEnvironment.Station, ArrayList<Block>> stationBlocksDone = new HashMap<>();

	boolean blockBaseDone = false;

	int times = 0;

	int planType = 0;

	/**
	 * Constructor for the agent.
	 *
	 * @param desiredState - the desired state of the world.
	 * @param name         - the name of the agent.
	 */
	public MyAgent(BlocksWorld desiredState, String name) {
		agentName = name;
		// TODO
		this.desiredState = desiredState;
	}

	@Override
	public Action response(Perceptions input) {
		this.perceptions = (BlocksWorldPerceptions) input;
		for (Block visibleBlock : perceptions.getVisibleStack().getBlocks()) {
			for (Map.Entry entry : stationStackHashMap.entrySet()) {
				if (((Stack) entry.getValue()).getBlocks().contains(visibleBlock)) {
					stationStackHashMap.remove((BlocksWorldEnvironment.Station) entry.getKey());
					break;
				}
			}
		}

		stationStackHashMap.put(perceptions.getCurrentStation(), perceptions.getVisibleStack());

		if (isCompleted()) {
			return new BlocksWorldAction(Type.AGENT_COMPLETED);
		}

		if (prevAction != null && prevAction.getType().equals(Type.GO_TO_STATION) && !perceptions.getCurrentStation().equals(prevAction.getFirstArgument())) {
			stationStackHashMap.remove((BlocksWorldEnvironment.Station) prevAction.getFirstArgument());
			this.planActions.clear();
		}

		if (planActions.isEmpty()) {
			this.planActions = plan();
		}

		if (! perceptions.hasPreviousActionSucceeded() && prevAction != null) {
			this.planActions.clear();
			this.planActions = plan();
		}
		Stack visibleStack = perceptions.getVisibleStack();
		BlocksWorldAction currentAction = planActions.get(0);
		switch (currentAction.getType()) {
			case STACK:
				if (visibleStack.getTopBlock().equals(currentAction.getSecondArgument())) {
					break;
				}
				this.planActions.clear();
				clearStackAboveBlock(currentAction.getSecondArgument(), visibleStack);

				this.planActions.add(0, new BlocksWorldAction(Type.GO_TO_STATION, perceptions.getCurrentStation()));
				this.planActions.add(0, new BlocksWorldAction(Type.PUTDOWN, currentAction.getFirstArgument()));

				break;
			case UNSTACK:
				if (visibleStack.getTopBlock().equals(currentAction.getFirstArgument())) {
					break;
				}

				if (!visibleStack.getBlocks().contains(currentAction.getFirstArgument())) {
					if (this.planActions.contains(new BlocksWorldAction(Type.LOCK, currentAction.getFirstArgument()))) {
						this.planActions.clear();
						this.planActions.add(new BlocksWorldAction(Type.NEXT_STATION));
						break;
					}
					deletePlanActionsForOneBlock(currentAction.getFirstArgument());
					if (!visibleStack.getBlocks().contains(currentAction.getSecondArgument())) {
						if (this.planActions.contains(new BlocksWorldAction(Type.LOCK, currentAction.getFirstArgument()))) {
							this.planActions.clear();
							this.planActions.add(new BlocksWorldAction(Type.NEXT_STATION));
							break;
						}
						deletePlanActionsForOneBlock(currentAction.getSecondArgument());
					} else {
						clearStackAboveBlock(currentAction.getSecondArgument(), visibleStack);
					}
				} else {
					clearStackAboveBlock(currentAction.getFirstArgument(), visibleStack);
				}

				if (this.planActions.isEmpty()) {
					this.planActions.add(new BlocksWorldAction(Type.NEXT_STATION));
				}
				break;

			case LOCK:
				if (visibleStack.getTopBlock().equals(currentAction.getFirstArgument())) {
					break;
				}
			case PICKUP:
				if (visibleStack.getTopBlock().equals(currentAction.getFirstArgument())) {
					if (visibleStack.getBlocks().size() > 1) {
						this.planActions.add(0, new BlocksWorldAction(Type.UNSTACK, currentAction.getFirstArgument(), visibleStack.getBelow(currentAction.getFirstArgument())));
						this.planActions.remove(1);
					}
					break;
				}
				if (visibleStack.getBlocks().contains(currentAction.getFirstArgument())) {
					if (prevAction.getType().equals(Type.GO_TO_STATION)) {
						clearStackAboveBlock(currentAction.getFirstArgument(), visibleStack);
					} else {
						this.planActions.clear();
						this.planActions.add(0, new BlocksWorldAction(Type.PUTDOWN, currentAction.getFirstArgument()));
					}
					break;
				}
				this.planActions.clear();
				this.planActions.add(new BlocksWorldAction(Type.NEXT_STATION));
				break;
		}

		BlocksWorldAction action = planActions.get(0);
		if (!canFinishTask(action)) {
			return new BlocksWorldAction(Type.AGENT_COMPLETED);
		}

		planActions.remove(action);
		prevAction = action;

		if (action.getType().equals(Type.LOCK)) {
			if (stationBlocksDone.containsKey(perceptions.getCurrentStation())) {
				stationBlocksDone.get(perceptions.getCurrentStation()).add(action.getArgument());
			} else {
				ArrayList<Block> blocks = new ArrayList<>();
				blocks.add(action.getArgument());
				stationBlocksDone.put(perceptions.getCurrentStation(), blocks);
			}
		}
		return action;

	}

	/**
	 * @return a new plan, as a sequence of {@link BlocksWorldAction} instances, based on the agent's knowledge.
	 */
	@SuppressWarnings("static-method")
	protected List<BlocksWorldAction> plan() {
		planActions = null;
		List<BlocksWorldAction> blocksWorldActions;

		blocksWorldActions = searchBaseBlockPlan();
		if (blocksWorldActions == null) {
			blocksWorldActions = searchBlockPlan();
			if (blocksWorldActions != null) {
				planType = BLOCK_PLAN;
			}
		} else {
			planType = BASE_BLOCK_PLAN;
		}

		if (blocksWorldActions == null) {
			blocksWorldActions = new ArrayList<>();
			blocksWorldActions.add(new BlocksWorldAction(Type.NEXT_STATION));
			planType = NO_PLAN;
		}
		allPlanActions = blocksWorldActions;
		return blocksWorldActions;
	}

	private List<BlocksWorldAction> searchBaseBlockPlan() {
		if (blocksBaseDone()) {
			return null;
		}

		Stack visibleStack = perceptions.getVisibleStack();
		Pair<Block, Stack> pair = getOneBaseBlock(visibleStack);
		if (pair != null) {
			toCreateBaseBlock = pair.getKey();
			return planBaseBlock(pair.getKey());
		}

		for (Stack stack : stationStackHashMap.values()) {
			pair = getOneBaseBlock(stack);
			if (pair != null) {
				toCreateBaseBlock = pair.getKey();
				List<BlocksWorldAction> actions = planBaseBlock(pair.getKey());
				actions.add(0, new BlocksWorldAction(Type.GO_TO_STATION, getStationByBlock(pair.getKey())));
			}
		}

		return null;
	}

	private List<BlocksWorldAction> searchBlockPlan() {
		Stack visibleStack = perceptions.getVisibleStack();

		for (Block visibleBlock : visibleStack.getBlocks()) {
			BlocksWorldEnvironment.Station stationToMove = getStationToMove(visibleBlock);
			if (stationToMove != null) {
				ArrayList<Block> blocks = stationBlocksDone.get(stationToMove);
				return planBlock(visibleBlock, stationToMove, blocks.get(blocks.size() - 1));
			}
		}

		for (Map.Entry entry : stationStackHashMap.entrySet()) {
			Stack stack = (Stack) entry.getValue();
			BlocksWorldEnvironment.Station station = (BlocksWorldEnvironment.Station) entry.getKey();
			for (Block block : stack.getBlocks()) {
				BlocksWorldEnvironment.Station stationToMove = getStationToMove(block);
				if (stationToMove != null) {
					ArrayList<Block> blocks = stationBlocksDone.get(stationToMove);
					ArrayList<BlocksWorldAction> actions = new ArrayList<>();
					actions.add(new BlocksWorldAction(Type.GO_TO_STATION, station));
					actions.addAll(planBlock(block, stationToMove, blocks.get(blocks.size() - 1)));
					return actions;
				}
			}
		}
		return null;
	}


	private List<BlocksWorldAction> planBaseBlock(Block block) {
		List<BlocksWorldAction> actions = new ArrayList<>();
		BlocksWorldEnvironment.Station currentStation = getStationByBlock(block);

		Stack visibleStack = stationStackHashMap.get(currentStation);
		Block currentBlock = visibleStack.getTopBlock();

		while (!currentBlock.equals(block)) {
			actions.add(new BlocksWorldAction(Type.UNSTACK, currentBlock, visibleStack.getBelow(currentBlock)));
			actions.add(new BlocksWorldAction(Type.PUTDOWN, currentBlock));
			if (isBaseBlock(currentBlock)) {
				actions.add(new BlocksWorldAction(Type.LOCK, currentBlock));
			}
			actions.add(new BlocksWorldAction(Type.GO_TO_STATION, perceptions.getCurrentStation()));
			currentBlock = visibleStack.getBelow(currentBlock);
		}

		if (!visibleStack.getBottomBlock().equals(block)) {
			actions.add(new BlocksWorldAction(Type.UNSTACK, block, visibleStack.getBelow(block)));
			actions.add(new BlocksWorldAction(Type.PUTDOWN, block));
			actions.add(new BlocksWorldAction(Type.LOCK, block));
		} else {
			actions.add(new BlocksWorldAction(Type.LOCK, block));
		}

		return actions;
	}

	private List<BlocksWorldAction> planBlock(Block block, BlocksWorldEnvironment.Station station, Block belowBlock) {
		List<BlocksWorldAction> actions = new ArrayList<>();
		BlocksWorldEnvironment.Station currentStation = getStationByBlock(block);

		Stack visibleStack = stationStackHashMap.get(currentStation);
		Block currentBlock = visibleStack.getTopBlock();

		while (!currentBlock.equals(block)) {
			actions.add(new BlocksWorldAction(Type.UNSTACK, currentBlock, visibleStack.getBelow(currentBlock)));
			actions.add(new BlocksWorldAction(Type.PUTDOWN, currentBlock));
			actions.add(new BlocksWorldAction(Type.GO_TO_STATION, currentStation));

			currentBlock = visibleStack.getBelow(currentBlock);
		}
		if (visibleStack.getBottomBlock().equals(block)) {
			actions.add(new BlocksWorldAction(Type.PICKUP, block));
		} else {
			if (station.equals(getStationByBlock(block))) {
				if (isSolved(visibleStack.getBelow(block))) {
					actions.add(new BlocksWorldAction(Type.LOCK, block));
					return actions;
				} else {
					actions.add(new BlocksWorldAction(Type.UNSTACK, block, visibleStack.getBelow(block)));
					actions.add(new BlocksWorldAction(Type.PUTDOWN, block));
					actions.add(new BlocksWorldAction(Type.GO_TO_STATION, station));
					return actions;
				}
			}
			actions.add(new BlocksWorldAction(Type.UNSTACK, block, visibleStack.getBelow(block)));
		}

		actions.add(new BlocksWorldAction(Type.GO_TO_STATION, station));
		actions.add(new BlocksWorldAction(Type.STACK, block, belowBlock));
		actions.add(new BlocksWorldAction(Type.LOCK, block));

		return actions;
	}

	private Pair<Block, Stack> getOneBaseBlock(Stack visibleStack) {
		for (Block block : visibleStack.getBlocks()) {
			if (isSolvedAsBase(block)) {
				continue;
			}
			for (Stack desireStack : desiredState.getStacks()) {
				if (desireStack.getBottomBlock().equals(block)) {
					return new Pair<>(block, desireStack);
				}
			}
		}
		return null;
	}

	private boolean isSolvedAsBase(Block block) {
		for (ArrayList<Block> blocks : stationBlocksDone.values()) {
			if (blocks.get(0).equals(block)) {
				return true;
			}
		}
		return false;
	}

	private boolean isSolved(Block block) {
		for (ArrayList<Block> blocks : stationBlocksDone.values()) {
			if (blocks.contains(block)) {
				return true;
			}
		}
		return false;
	}

	private boolean isBaseBlock(Block block) {
		for (Stack stack : desiredState.getStacks()) {
			if (stack.getBottomBlock().equals(block)) {
				return true;
			}
		}
		return false;
	}

	private boolean blocksBaseDone() {
		if (blockBaseDone) {
			return true;
		}
		for (Stack stack : desiredState.getStacks()) {
			if (!isSolvedAsBase(stack.getBottomBlock())) {
				return false;
			}
		}

		blockBaseDone = true;
		return true;
	}

	private BlocksWorldEnvironment.Station getStationByBlock(Block block) {
		for (Map.Entry entry : stationStackHashMap.entrySet()) {
			if (((Stack) entry.getValue()).contains(block)) {
				return (BlocksWorldEnvironment.Station) entry.getKey();
			}
		}
		return null;
	}

	private boolean canFinishTask(BlocksWorldAction action) {
		if (action.getType().equals(Type.NEXT_STATION) && action.equals(prevAction)) {
			times += 1;
		} else {
			times = 0;
		}
		int max = Math.max(desiredState.getStacks().size(), stationStackHashMap.size());
		if (max <= 2) {
			max = 3;
		}
		if (times >= 5 * max) {
			System.out.println("Can't finish task. Blocks are missing or locked by another agent");
			System.out.println(times + " next_station operation");
			System.out.println(".................");
			System.out.println(".................");
			System.out.println(".................");
			return false;
		}
		return true;
	}

	private void deletePlanActionsForOneBlock(Block block) {
		if (!planActions.get(0).getFirstArgument().equals(block)) {
			return;
		}
		planActions.remove(0);
		if (planActions.get(0).getType().equals(Type.GO_TO_STATION)) {
			planActions.remove(0);
		}
		planActions.remove(0);
		if (planActions.get(0).getType().equals(Type.GO_TO_STATION)) {
			planActions.remove(0);
		}
		if (!planActions.isEmpty() && planActions.get(0).getType() == Type.LOCK) {
			planActions.remove(0);
		}
	}

	/**
	 * If block can be stack over a locked block, return station to move
	 */
	private BlocksWorldEnvironment.Station getStationToMove(Block block) {
		Stack stack;
		try {
			stack = desiredState.getStack(block);
		} catch (Exception exception) {
			return null;
		}
		if (stack == null || stack.getBottomBlock().equals(block)) {
			return null;
		}

		Block belowBlock = stack.getBelow(block);
		for (Map.Entry entry : stationBlocksDone.entrySet()) {
			ArrayList<Block> blocks = (ArrayList<Block>) entry.getValue();
			if (!blocks.contains(block) && blocks.get(blocks.size() - 1).equals(belowBlock)) {
				return (BlocksWorldEnvironment.Station) entry.getKey();
			}
		}
		return null;
	}

	private void clearStackAboveBlock(Block block, Stack visibleStack) {
		Block currentBlock = visibleStack.getAbove(block);
		while (currentBlock != null) {
			this.planActions.add(0, new BlocksWorldAction(Type.GO_TO_STATION, perceptions.getCurrentStation()));
			if (isBaseBlock(currentBlock)) {
				this.planActions.add(0, new BlocksWorldAction(Type.LOCK, currentBlock));
				this.planActions.add(0, new BlocksWorldAction(Type.PUTDOWN, currentBlock));
				this.planActions.add(0, new BlocksWorldAction(Type.UNSTACK, currentBlock, visibleStack.getBelow(currentBlock)));
			} else {
				BlocksWorldEnvironment.Station stationToMove = getStationToMove(currentBlock);
				if (stationToMove != null) {
					ArrayList<Block> blocks = stationBlocksDone.get(stationToMove);
					List<BlocksWorldAction> actions = planBlock(currentBlock, stationToMove, blocks.get(blocks.size() - 1));
					this.planActions.addAll(0, actions);

				} else {
					this.planActions.add(0, new BlocksWorldAction(Type.PUTDOWN, currentBlock));
					this.planActions.add(0, new BlocksWorldAction(Type.UNSTACK, currentBlock, visibleStack.getBelow(currentBlock)));
				}
			}

			currentBlock = visibleStack.getAbove(currentBlock);
		}
	}

	private boolean isCompleted() {
		int nr = 0;
		for (ArrayList<Block> blocks : stationBlocksDone.values()) {
			for (Stack stack : desiredState.getStacks()) {
				List<Block> blocks1 = new ArrayList<>(stack.getBlocks());
				Collections.reverse(blocks1);
				if (blocks1.equals(blocks)) {
					nr += 1;
				}
			}
		}
		if (nr == desiredState.getStacks().size()) {
			return true;
		}
		return false;
	}

	@Override
	public String statusString() {
		// TODO: return information about the agent's current state and current plan.
		if (planActions == null) {
			return toString() + ": PLAN MISSING.";
		}

		String toShow = "";
		for (BlocksWorldAction action : planActions) {
			toShow = toShow.concat(action.toString()) + ", ";
		}
		return toString() + ": PLAN: " + toShow;
	}

	@Override
	public String toString() {
		return "" + agentName;
	}

}