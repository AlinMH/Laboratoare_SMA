package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import platform.Log;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The BraceletAgent.
 */
public class BraceletAgent extends Agent {
    /**
     * The serial UID.
     */
    private static final long serialVersionUID = 5088484951993491457L;
    private static final Map<String, Integer> wakeUpModes = Stream.of(new Object[][]{
            {WakeUpPreference.SUPER_SOFT, 0},   // Default value
            {WakeUpPreference.SOFT, 1},         // We prefer bracelet instead of phone alarm, because is "softer"
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (Integer) data[1]));

    private static final MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
            MessageTemplate.MatchPerformative(ACLMessage.CFP));

    private String wakeUpMode;

    @Override
    public void setup() {
        Log.log(this, "Hello");

        // Register the ambient-agent service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType(ServiceType.AMBIENT_AGENT);
        sd.setName("ambient-wake-up-call");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // TODO add behaviors
        addBehaviour(new ContractNetResponder(this, template) {
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
                String agentPreference = cfp.getContent();
                System.out.println("Agent " + getLocalName() + ": CFP received from " + cfp.getSender().getName() + ". Action is " + agentPreference);
                boolean canPerformAction = checkIfAbleToPerformAction(agentPreference);
                if (canPerformAction) {
                    // We provide a proposal
                    Integer wakeUpModeValue = wakeUpModes.get(agentPreference);
                    String proposal = agentPreference + " with value " + wakeUpModeValue;
                    System.out.println("Agent " + getLocalName() + ": Proposing " + proposal);

                    ((BraceletAgent) myAgent).setWakeUpMode(agentPreference);
                    ACLMessage propose = cfp.createReply();
                    propose.setPerformative(ACLMessage.PROPOSE);
                    propose.setContent(String.valueOf(wakeUpModeValue));
                    return propose;
                } else {
                    // We refuse to provide a proposal
                    System.out.println("Agent " + getLocalName() + ": Refuse");
                    throw new RefuseException("evaluation-failed");
                }
            }

            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                System.out.println("Agent " + getLocalName() + ": Proposal accepted");
                ACLMessage inform = accept.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                String wakeUpMode = ((BraceletAgent) myAgent).getWakeUpMode();
                inform.setContent("Wake-Up-Mode:" + wakeUpMode + " with value " + wakeUpModes.get(wakeUpMode));
                return inform;
            }

            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                System.out.println("Agent " + getLocalName() + ": Proposal rejected");
            }
        });

    }

    private boolean checkIfAbleToPerformAction(String preference) {
        // Simulate an evaluation by generating a random number
        return wakeUpModes.containsKey(preference);
    }

    private void setWakeUpMode(String wakeUpMode) {
        this.wakeUpMode = wakeUpMode;
    }

    private String getWakeUpMode() {
        return wakeUpMode;
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
