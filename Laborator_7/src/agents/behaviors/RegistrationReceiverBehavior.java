package agents.behaviors;

import agents.MyAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Behavior that receives registration messages.
 */
public class RegistrationReceiverBehavior extends TickerBehaviour
{
	
	/**
	 * The serial UID.
	 */
	private static final long	serialVersionUID	= 2088300789458693623L;
	
	/**
	 * Time between checking for messages.
	 */
	public static final int		TICK_PERIOD			= 100;
	/**
	 * Number of ticks to wait for registration messages.
	 */
	public static final int		MAX_TICKS			= 50;
	
	/**
	 * Template for registration messages.
	 * <p>
	 * Alternative: <code>
	 * new MatchExpression() {
	 *  &#64;Override
	 *  public boolean match(ACLMessage msg) {
	 *  	return (msg.getPerformative() == ACLMessage.INFORM && msg.getProtocol().equals("register-child"));
	 *  }}
	 * </code>
	 */
	private MessageTemplate		msgTemplate			= MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.INFORM),
			MessageTemplate.MatchProtocol(RegistrationSenderBehavior.REGISTRATION_PROTOCOL));
	
	/**
	 * @param a
	 *            the agent this behavior belongs to.
	 */
	public RegistrationReceiverBehavior(Agent a)
	{
		super(a, TICK_PERIOD);
	}
	
	@Override
	protected void onTick()
	{
		ACLMessage receivedMsg = myAgent.receive(msgTemplate);
		
		// register the agent if message received
		if(receivedMsg != null)
		{
			AID childAID = receivedMsg.getSender();
			((MyAgent) myAgent).addChildAgent(childAID);
		}
		
		// if number of ticks surpassed, take down the agent
		if(getTickCount() >= MAX_TICKS)
		{
			stop();
			myAgent.addBehaviour(new ProcedureStartSenderBehavior(myAgent, 3000));
		}
	}
	
}
