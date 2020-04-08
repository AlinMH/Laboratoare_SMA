package blocksworld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import base.Agent;
import base.Environment;
import blocksworld.BlocksWorldAction.Type;

/**
 * Blocks world environment.
 * 
 * @author Andrei Olaru
 */
public abstract class BlocksWorldEnvironment implements Environment
{
	/**
	 * A station.
	 * 
	 * @author Andrei Olaru
	 */
	public static class Station extends Block
	{
		
		/**
		 * @param label
		 *            the label of the station.
		 */
		public Station(char label)
		{
			super(label);
		}
		
		@Override
		public String toString()
		{
			return "#" + super.toString();
		}
	}
	
	/**
	 * Contains data for each agent in the environment.
	 * 
	 * @author Andrei Olaru
	 */
	public static class AgentData
	{
		/**
		 * The agent reference.
		 */
		protected Agent			agent;
		
		/**
		 * Block that the agent is currently holding. Use <code>null</code> for none.
		 */
		protected Block			holding					= null;
		
		/**
		 * The current station of the agent.
		 */
		private Station			station					= null;
		
		/**
		 * The agent's desire state.
		 */
		protected BlocksWorld	targetState				= null;
		
		/**
		 * Indicates whether the previous action has succeeded.
		 */
		boolean					previousActionSucceeded	= true;
		
		/**
		 * Default constructor.
		 * 
		 * @param linkedAgent
		 *            - the agent.
		 * @param target
		 *            - the desired state of the agent.
		 * @param initialStation
		 *            - the initial position of the agent.
		 */
		public AgentData(Agent linkedAgent, BlocksWorld target, Station initialStation)
		{
			agent = linkedAgent;
			targetState = target;
			station = initialStation;
		}
		
		/**
		 * @return the managed agent.
		 */
		public Agent getAgent()
		{
			return agent;
		}
		
		/**
		 * @return the block held by the agent; <code>null</code> if the agent holds no blocks.
		 */
		public Block getHolding()
		{
			return holding;
		}
		
		/**
		 * Indicates that the previous action was successful.
		 */
		public void setPreviousActionSuccessful()
		{
			previousActionSucceeded = true;
		}
		
		/**
		 * Indicates that the previous action has failed.
		 */
		public void setPreviousActionFailed()
		{
			previousActionSucceeded = false;
		}
		
		/**
		 * @return <code>true</code> if the previous action was successful.
		 */
		public boolean hasPreviousActionSucceeded()
		{
			return previousActionSucceeded;
		}
		
		/**
		 * @param held
		 *            - the block held by the agent; <code>null</code> if the agent holds no blocks.
		 */
		public void setHolding(Block held)
		{
			holding = held;
		}
		
		/**
		 * @return the current station of the agent.
		 */
		public Station getStation()
		{
			return station;
		}
		
		/**
		 * @param station
		 *            - the station the agent arrived at.
		 */
		public void setStation(Station station)
		{
			this.station = station;
		}
		
		/**
		 * @return the targetState
		 */
		public BlocksWorld getTargetState()
		{
			return targetState;
		}
		
		/**
		 * @param targetState
		 *            the targetState to set
		 */
		public void setTargetState(BlocksWorld targetState)
		{
			this.targetState = targetState;
		}
		
		@Override
		public String toString()
		{
			return "Agent " + agent + " at " + station + " holding: " + (holding == null ? "none" : holding)
					+ "; previous action: " + (previousActionSucceeded ? "successful" : "failed");
		}
	}
	
	/**
	 * Current state of the world.
	 */
	protected BlocksWorld		worldstate	= null;
	/**
	 * The list of stations. The indexes correspond exactly to indexes of stacks in the {@link #worldstate}.
	 */
	protected List<Station>		stations	= null;
	/**
	 * List of agents in the system.
	 */
	protected List<AgentData>	agents		= new ArrayList<>();
	
	/**
	 * Constructor of the environment.
	 * 
	 * @param world
	 *            - initial state.
	 */
	public BlocksWorldEnvironment(BlocksWorld world)
	{
		worldstate = world.clone();
		stations = new LinkedList<>();
		char idx = '0';
		for(@SuppressWarnings("unused")
		Stack s : worldstate.stacks)
			stations.add(new Station(idx++));
	}
	
	@Override
	public void addAgent(Agent agent, Object targetState, Object station)
	{
		agents.add(new AgentData(agent, (BlocksWorld) targetState, stations.get(0)));
	}
	
	/**
	 * Method to retrieve the agent data structure for a particular {@link Agent} instance.
	 * 
	 * @param agent
	 *            - the agent for which the data is requested.
	 * @return the requested {@link AgentData} structure.
	 * @throws IllegalArgumentException
	 *             if the agent has not been added to the environment.
	 */
	protected AgentData getAgentData(Agent agent)
	{
		for(AgentData aData : agents)
			if(aData.getAgent() == agent)
				return aData;
		throw new IllegalArgumentException("Agent [" + agent + "] has not been added to the environment");
	}
	
	/**
	 * @return the list of {@link AgentData} instances for all agents.
	 */
	protected List<AgentData> getAgents()
	{
		return agents;
	}
	
	/**
	 * When multiple agents are in the same station, this tells whether an agent has the token.
	 * 
	 * @param agent
	 *            - an agent in the team.
	 * @return <code>true</code> if the team that the agent is part of has the token and the agent is its leader.
	 */
	@SuppressWarnings("static-method")
	protected boolean hasToken(AgentData agent)
	{
		return false;
	}
	
	@Override
	public String toString()
	{
		Map<Stack, List<String>> prefix = new HashMap<>();
		for(AgentData a : getAgents())
		{
			List<String> data = new LinkedList<>();
			data.add(" " + a.getAgent().toString() + (hasToken(a) ? "$" : " "));
			data.add(" <" + (a.getHolding() != null ? a.getHolding() : "") + ">");
//			data.add("\n");
			if(prefix.containsKey(worldstate.getStacks().get(stations.indexOf(a.getStation()))))
				prefix.get(worldstate.getStacks().get(stations.indexOf(a.getStation()))).addAll(data);
			else
				prefix.put(worldstate.getStacks().get(stations.indexOf(a.getStation())), data);
		}
		Map<Stack, List<String>> suffix = new HashMap<>();
		for(Station station : stations)
		{
			List<String> data = new LinkedList<>();
			data.add("=====");
			data.add(" " + station.toString());
			suffix.put(worldstate.getStacks().get(stations.indexOf(station)), data);
		}
		return worldstate.toString(6, prefix, suffix, false);
	}
	
	@Override
	public boolean step()
	{
		System.out.println(getAgents() + "\n");
		
		Map<AgentData, BlocksWorldAction> actionMap = new HashMap<>();
		for(AgentData ag : agents)
		{
			ag.setPreviousActionSuccessful();
			Station agentStation = ag.getStation();
			Stack currentStack = worldstate.getStacks().get(stations.indexOf(agentStation));
			int position = stations.indexOf(agentStation);
			if(position != worldstate.getStacks().indexOf(currentStack))
				throw new RuntimeException("stations and worldstate disaligned.");
			BlocksWorldAction act = (BlocksWorldAction) ag.getAgent()
					.response(new BlocksWorldPerceptions(currentStack, agentStation, ag.hasPreviousActionSucceeded()));
			System.out.println(ag.getAgent() + " opts for " + act);
			
			// check if action is legal with respect to the state of the agent.
			if((act.getType() == Type.PUTDOWN || act.getType() == Type.STACK) && (ag.getHolding() == null
					|| !ag.getHolding().equals(act.hasOneArg() ? act.getArgument() : act.getFirstArgument())))
				throw new IllegalStateException(
						"Can't work with that block: " + (act.hasOneArg() ? act.getArgument() : act.getFirstArgument())
								+ "; agent is holding: " + ag.getHolding());
			
			if((act.getType() == Type.PICKUP || act.getType() == Type.UNSTACK) && ag.getHolding() != null)
				throw new IllegalStateException("Agent already busy with block: " + ag.getHolding());
			
			actionMap.put(ag, act);
		}
		
		int nCompleted = performActions(actionMap);
		
		if(nCompleted == agents.size())
			return true;
		return false; // return true when the simulation should stop.
	}
	
	/**
	 * Perform the actions on the environment.
	 * 
	 * @param actionMap
	 *            the action opted for by each agent.
	 * @return the number of agents who have complete their goals.
	 */
	protected int performActions(Map<AgentData, BlocksWorldAction> actionMap)
	{
		int nCompleted = 0;
		for(AgentData ag : actionMap.keySet())
		{
			BlocksWorldAction act = actionMap.get(ag);
			Station agentStation = ag.getStation();
			Stack currentStack = worldstate.getStacks().get(stations.indexOf(agentStation));
			int position = stations.indexOf(agentStation);
			switch(act.getType())
			{
			case PICKUP:
				// modify world; remove station; switch agent to other station.
				if(!currentStack.contains(act.getArgument()))
					throw new IllegalArgumentException(
							"The block [" + act.getArgument() + "] is not in the current stack " + currentStack);
				ag.setHolding(worldstate.pickUp(act.getArgument()));
				ag.setStation(stations.get((position + 1) % stations.size()));
				stations.remove(agentStation);
				break;
			case PUTDOWN:
			{
				// modify world; add station; change agent station.
				worldstate.putDown(act.getArgument(), currentStack);
				char stationName = '0';
				while(stations.contains(new Station(stationName)))
					stationName++;
				ag.setStation(new Station(stationName));
				stations.add(position, ag.getStation());
				ag.setHolding(null);
				break;
			}
			case UNSTACK:
				if(!currentStack.contains(act.getFirstArgument()))
					throw new IllegalArgumentException(
							"The block [" + act.getFirstArgument() + "] is not in the current stack " + currentStack);
				ag.setHolding(worldstate.unstack(act.getFirstArgument(), act.getSecondArgument()));
				break;
			case STACK:
				if(!currentStack.contains(act.getSecondArgument()))
					throw new IllegalArgumentException(
							"The block [" + act.getSecondArgument() + "] is not in the current stack " + currentStack);
				worldstate.stack(act.getFirstArgument(), act.getSecondArgument());
				ag.setHolding(null);
				break;
			case GO_TO_STATION:
				if(stations.contains(act.getArgument()))
					ag.setStation((Station) act.getArgument());
				else
				{
					System.out.println("Cannot move to station " + act.getArgument().toString() + ".");
				}
				break;
			case LOCK:
				if(!currentStack.contains(act.getArgument()))
					throw new IllegalArgumentException(
							"The block [" + act.getArgument() + "] is not in the current stack " + currentStack);
				worldstate.lock(act.getFirstArgument());
				break;
			case NEXT_STATION:
				ag.setStation(stations.get((position + 1) % stations.size()));
				break;
			case AGENT_COMPLETED:
				nCompleted++;
				break;
			case NONE:
				System.out.println("Conflicting: " + ag.getAgent().toString() + " do NONE.");
				ag.setPreviousActionFailed();
				break;
			default:
				throw new RuntimeException("Should not be here");
			}
		}
		return nCompleted;
	}
}
