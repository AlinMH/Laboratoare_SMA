package agents.behaviors;

import agents.MyAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.List;

public class ProcedureStartSenderBehavior extends WakerBehaviour {
    private static final long serialVersionUID = 87341135805457781L;

    /**
     * The name of the registration protocol.
     */
    static final String START_PROTOCOL = "request_value";

    /**
     * The ID of the parent.
     */

    List<AID> childAgents;

    public ProcedureStartSenderBehavior(Agent a, long timeout) {
        super(a, timeout);
        childAgents = ((MyAgent) a).getChildAgents();
    }

    @Override
    public void onWake() {
        for (AID childAgent : childAgents) {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setProtocol(START_PROTOCOL);
            msg.setConversationId("request_value-" + myAgent.getName());
            msg.addReceiver(childAgent);
            System.out.println("[INIT PROCEDURE] Start request sent from " + myAgent.getName() + " to " + childAgent.getLocalName());
            myAgent.send(msg);
        }
    }
}
