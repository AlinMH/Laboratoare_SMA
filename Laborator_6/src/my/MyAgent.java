package my;

import java.util.List;

import base.Action;
import base.Agent;
import base.Perceptions;
import blocksworld.BlocksWorld;
import blocksworld.BlocksWorldAction;
import blocksworld.BlocksWorldAction.Type;
import blocksworld.BlocksWorldPerceptions;

/**
 * Agent to implement.
 */
public class MyAgent implements Agent
{
	/**
	 * Name of the agent.
	 */
	String agentName;
	
	/**
	 * Constructor for the agent.
	 * 
	 * @param desiredState
	 *            - the desired state of the world.
	 * @param name
	 *            - the name of the agent.
	 */
	public MyAgent(BlocksWorld desiredState, String name)
	{
		agentName = name;
		// TODO
	}
	
	@Override
	public Action response(Perceptions input)
	{
		@SuppressWarnings("unused")
		BlocksWorldPerceptions perceptions = (BlocksWorldPerceptions) input;
		
		// TODO: revise beliefs; if necessary, make a plan; return an action.
		
		return new BlocksWorldAction(Type.AGENT_COMPLETED);
	}
	
	/**
	 * @param perceivedWorldState
	 *            - the blocks that the agent can see.
	 */
	protected void reviseBeliefs(BlocksWorld perceivedWorldState)
	{
		// TODO: check if what the agent knows corresponds to what the agent sees.
	}
	
	/**
	 * @return a new plan, as a sequence of {@link BlocksWorldAction} instances, based on the agent's knowledge.
	 */
	@SuppressWarnings("static-method")
	protected List<BlocksWorldAction> plan()
	{
		// TODO
		return null;
	}
	
	@Override
	public String statusString()
	{
		// TODO: return information about the agent's current state and current plan.
		return toString() + ": PLAN MISSING.";
	}
	
	@Override
	public String toString()
	{
		return "" + agentName;
	}
}
