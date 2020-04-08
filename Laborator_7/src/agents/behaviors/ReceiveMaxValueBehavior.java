package agents.behaviors;

import agents.MyAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveMaxValueBehavior extends CyclicBehaviour {
    private MessageTemplate msgTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(MaxBehavior.MAX_VALUE_RECEIVE_PROTOCOL));

    public ReceiveMaxValueBehavior(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        ACLMessage receivedMsg = myAgent.receive(msgTemplate);
        if (receivedMsg != null) {
            int maxValue = Integer.parseInt(receivedMsg.getContent());
            System.out.println("[DOWNWARD] Agent" + myAgent.getAID().getLocalName()+ " received the max value " + maxValue);
            ((MyAgent) myAgent).setAgentValue(maxValue);
            for (AID agentId : ((MyAgent) myAgent).getChildAgents()) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setProtocol(MaxBehavior.MAX_VALUE_RECEIVE_PROTOCOL);
                msg.setConversationId("send_max_value-" + myAgent.getName());
                msg.setContent(Integer.toString(maxValue));
                msg.addReceiver(agentId);
                myAgent.send(msg);
            }
            myAgent.doDelete();
        } else {
            block();
        }
    }
}
