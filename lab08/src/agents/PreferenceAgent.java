package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import platform.Log;

/**
 * Preference agent.
 */
public class PreferenceAgent extends Agent {
    /**
     *
     */
    private static final long serialVersionUID = -3397689918969697329L;

    private static final MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

    @Override
    public void setup() {
        Log.log(this, "Hello");

        // Register the preference-agent service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType(ServiceType.PREFERENCE_AGENT);
        sd.setName("ambient-wake-up-call");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // TODO add behaviors
        addBehaviour(new AchieveREResponder(this, template) {
            protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
                System.out.println("Agent " + getLocalName() + ": REQUEST received from " + request.getSender().getName() + ". Action is " + request.getContent());
                System.out.println("Agent " + getLocalName() + ": Agree");
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            }

            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
                System.out.println("Agent " + getLocalName() + ": Action successfully performed");
                ACLMessage inform = request.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                inform.setContent(WakeUpPreference.HARD); // Select one of these : HARD, SOFT, SUPER_SOFT
                return inform;
            }
        });
    }

    @Override
    protected void takeDown() {
        // De-register from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Printout a dismissal message
        Log.log(this, "terminating.");
    }
}
