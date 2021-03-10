package my;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import base.Agent;
import blocksworld.BlocksWorld;
import blocksworld.DynamicEnvironment;
import tester.Tester;

/**
 * Main class for testing.
 * 
 * @author Andrei Olaru
 */
public class MyTester extends Tester
{
	/**
	 * Delay between two agent steps. In milliseconds.
	 */
	protected static final int		STEP_DELAY	= 500;
	
	/**
	 * The place to get the tests from.
	 */
	protected static final String	TEST_SUITE	= "tests/0d2/";
	
	/**
	 * The probability for the environment to change dynamically at one step. Between 0 and 1.
	 */
	public static final float		DYNAMICITY	= 0;
	
	/**
	 * The name of the agent.
	 */
	protected static final String	AGENT_NAME	= "*A";
	
	/**
	 * Creates a new tester instance and begins testing.
	 * 
	 * @throws IOException
	 *             - see {@link Tester}.
	 */
	public MyTester() throws IOException
	{
		initializeEnvironment(TEST_SUITE);
		initializeAgents(TEST_SUITE);
		makeSteps();
	}
	
	/**
	 * Main loop.
	 * 
	 * @param testSuite
	 *            - the path for test files.
	 *
	 * @throws FileNotFoundException
	 *             - if world state file not found.
	 * @throws IOException
	 *             - if world state file is corrupted.
	 */
	protected void initializeEnvironment(String testSuite) throws IOException
	{
		try (InputStream input = new FileInputStream(testSuite + SI + EXT))
		{
			environment = new DynamicEnvironment(new BlocksWorld(input));
		}
	}
	
	/**
	 * @param testSuite
	 *            - the path for test files.
	 * @throws FileNotFoundException
	 *             - if desired state file not found.
	 * @throws IOException
	 *             - if desired state file is corrupted.
	 */
	protected void initializeAgents(String testSuite) throws IOException
	{
		agents = new LinkedList<>();
		Map<Agent, BlocksWorld> agentsStates = new HashMap<>();
		try (InputStream input = new FileInputStream(testSuite + SF + EXT))
		{
			BlocksWorld desires = new BlocksWorld(input);
			Agent ag = new MyAgent(desires, AGENT_NAME);
			agentsStates.put(ag, desires);
			agents.add(ag);
		}
		for(Agent agent : agentsStates.keySet())
			environment.addAgent(agent, agentsStates.get(agent), null);
		for(Agent agent : agents)
		{
			if(agentsStates.get(agent) != null)
			{
				System.out.println(agent.toString() + " desires:");
				System.out.println(agentsStates.get(agent).toString());
			}
		}
	}
	
	@Override
	protected int getDelay()
	{
		return STEP_DELAY;
	}
	
	/**
	 * Main.
	 * 
	 * @param args
	 *            - not used
	 * @throws IOException
	 *             - see {@link Tester}.
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException
	{
		new MyTester();
	}
}
