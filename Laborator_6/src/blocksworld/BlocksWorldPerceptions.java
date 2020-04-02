package blocksworld;

import base.Perceptions;
import blocksworld.BlocksWorldEnvironment.Station;

/**
 * What an agent is able to perceive.
 * 
 * @author andreiolaru
 */
public class BlocksWorldPerceptions implements Perceptions
{
	/**
	 * The state of the current station.
	 */
	Stack	visibleStack;
	/**
	 * The name of the current station.
	 */
	Station	current;
	/**
	 * <code>true</code> if the previous action of the agent succeeded; <code>false</code> otherwise.
	 */
	boolean	previousActionSucceeded;
	
	/**
	 * Constructor.
	 * 
	 * @param stack
	 *            - the state of the current station.
	 * @param currentStation
	 *            - the current station.
	 * @param previousActionSucceeded
	 *            - whether the previous action was carried out correctly.
	 */
	public BlocksWorldPerceptions(Stack stack, Station currentStation, boolean previousActionSucceeded)
	{
		this.visibleStack = stack;
		this.current = currentStation;
		this.previousActionSucceeded = previousActionSucceeded;
	}
	
	/**
	 * @return the perceived state of the current station.
	 */
	public Stack getVisibleStack()
	{
		return visibleStack;
	}
	
	/**
	 * @return the current station.
	 */
	public Station getCurrentStation()
	{
		return current;
	}
	
	/**
	 * @return <code>true</code> if the previous action was carried out correctly.
	 */
	public boolean hasPreviousActionSucceeded()
	{
		return previousActionSucceeded;
	}
}
