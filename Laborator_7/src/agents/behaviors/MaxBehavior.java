package agents.behaviors;

import agents.MyAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class MaxBehavior extends CyclicBehaviour {

    private MessageTemplate msgTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(ProcedureStartReceiverBehavior.TASK_PROTOCOL));


    static final String MAX_VALUE_RECEIVE_PROTOCOL = "max_value_recv";

    int nrMessages = 0;
    int agentValue;

    public MaxBehavior(Agent a) {
        super(a);
        agentValue = ((MyAgent)a).getAgentValue();

    }

    @Override
    public void action() {
        ACLMessage receivedMsg = myAgent.receive(msgTemplate);

        if (receivedMsg != null) {
            AID childAID = receivedMsg.getSender();
            nrMessages += 1;
            int childValue = Integer.parseInt(receivedMsg.getContent());
            if (childValue > agentValue) {
                agentValue = childValue;
            }
            if (nrMessages == ((MyAgent)myAgent).getChildAgents().size()) {
                if (((MyAgent) myAgent).getParentAID() != null) {
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setProtocol(ProcedureStartReceiverBehavior.TASK_PROTOCOL);
                    msg.setConversationId("task-" + myAgent.getName());
                    msg.setContent(Integer.toString(agentValue));
                    msg.addReceiver(((MyAgent) myAgent).getParentAID());
                    myAgent.send(msg);
                    System.out.println("[UPWARD] Agent " + myAgent.getAID().getLocalName() + " has sent a message to " +
                            ((MyAgent) myAgent).getParentAID().getLocalName() + " with VALUE: " + ((MyAgent) myAgent).getAgentValue());
                } else {
                    System.out.println("[START BROADCAST] Root received the max value " + agentValue);
                    ((MyAgent) myAgent).setAgentValue(agentValue);
                    for (AID agentId : ((MyAgent) myAgent).getChildAgents()) {
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setProtocol(MAX_VALUE_RECEIVE_PROTOCOL);
                        msg.setConversationId("send_max_value-" + myAgent.getName());
                        msg.setContent(Integer.toString(agentValue));
                        msg.addReceiver(agentId);
                        myAgent.send(msg);
                    }
                    myAgent.doDelete();
                }
            }
        } else {
            block();
        }
    }
}
