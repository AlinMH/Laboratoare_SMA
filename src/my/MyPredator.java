package my;

import base.Action;
import base.Perceptions;
import hunting.AbstractWildlifeAgent;
import hunting.WildlifeAgentType;

/**
 * Implementation for predator agents.
 * 
 * @author Alexandru Sorici
 */
public class MyPredator extends AbstractWildlifeAgent
{
	/**
	 * Default constructor.
	 */
	public MyPredator()
	{
		super(WildlifeAgentType.PREDATOR);
	}
	
	@Override
	public Action response(Perceptions perceptions)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
