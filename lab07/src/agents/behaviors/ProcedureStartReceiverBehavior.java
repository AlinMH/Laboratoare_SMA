package agents.behaviors;

import agents.MyAgent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ProcedureStartReceiverBehavior extends CyclicBehaviour {

    static final String TASK_PROTOCOL = "task";
    /**
     * Template for registration messages.
     * <p>
     * Alternative: <code>
     * new MatchExpression() {
     * &#64;Override
     * public boolean match(ACLMessage msg) {
     * return (msg.getPerformative() == ACLMessage.INFORM && msg.getProtocol().equals("register-child"));
     * }}
     * </code>
     */
    private MessageTemplate msgTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.MatchProtocol(ProcedureStartSenderBehavior.START_PROTOCOL));


    public ProcedureStartReceiverBehavior(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        ACLMessage receivedMsg = myAgent.receive(msgTemplate);
        if (receivedMsg != null && ((MyAgent) myAgent).getChildAgents().size() == 0) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setProtocol(TASK_PROTOCOL);
            msg.setConversationId("task-" + myAgent.getName());
            msg.setContent(Integer.toString(((MyAgent) myAgent).getAgentValue()));
            msg.addReceiver(((MyAgent) myAgent).getParentAID());
            myAgent.send(msg);
            System.out.println("[UPWARD] Agent " + myAgent.getAID().getLocalName() + " has sent a message to " +
                    ((MyAgent) myAgent).getParentAID().getLocalName() + " with VALUE: " + ((MyAgent) myAgent).getAgentValue());
        } else {
            block();
        }
    }
}
