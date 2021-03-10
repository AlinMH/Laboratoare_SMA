package agents;

import java.util.*;

import agents.behaviors.AmbientServiceDiscoveryBehavior;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.ContractNetInitiator;
import platform.Log;

/**
 * The PersonalAgent.
 */
public class PersonalAgent extends Agent {
    /**
     * The serial UID.
     */
    private static final long serialVersionUID = 2081456560111009192L;

    /**
     * Known ambient agents.
     */
    List<AID> ambientAgents = new LinkedList<>();

    /**
     * Known preference agent
     */
    AID preferenceAgent;

    private String preference;
    private int nResponders;


    @Override
    protected void setup() {
        Log.log(this, "Hello from PersonalAgent");
        Log.log(this, "Adding DF subscribe behaviors");

        // Create a parallel behavior to handle the two DF subscriptions: one for the two ambient-agent and one for the
        // preference-agent services
        addBehaviour(new AmbientServiceDiscoveryBehavior(this, ParallelBehaviour.WHEN_ALL));
    }

    /**
     * This method will be called when all the needed agents have been discovered.
     */
    protected void onDiscoveryCompleted() {
        // TODO: add the RequestInitiator behavior for asking the PreferenceAgent about preferred wake up mode
        retrieveWakeUpPreference();
    }

    private void requestWakeUpNotification() {
        nResponders = ambientAgents.size();
        System.out.println("Trying to delegate dummy-action to one out of " + nResponders + " responders.");

        // Fill the CFP message
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        for (AID aid : ambientAgents) {
            msg.addReceiver(aid);
        }

        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // We want to receive a reply in 10 secs
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
        msg.setContent(preference);

        addBehaviour(new ContractNetInitiator(this, msg) {
            protected void handlePropose(ACLMessage propose, Vector v) {
                System.out.println("Agent " + propose.getSender().getName() + " proposed " + propose.getContent());
            }

            protected void handleRefuse(ACLMessage refuse) {
                System.out.println("Agent " + refuse.getSender().getName() + " refused");
            }

            protected void handleFailure(ACLMessage failure) {
                if (failure.getSender().equals(myAgent.getAMS())) {
                    // FAILURE notification from the JADE runtime: the receiver
                    // does not exist
                    System.out.println("Responder does not exist");
                } else {
                    System.out.println("Agent " + failure.getSender().getName() + " failed");
                }
                // Immediate failure --> we will not receive a response from this agent
                nResponders--;
            }

            protected void handleAllResponses(Vector responses, Vector acceptances) {
                if (responses.size() < nResponders) {
                    // Some responder didn't reply within the specified timeout
                    System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
                }
                // Evaluate proposals.
                int bestProposal = -1;
                AID bestProposer = null;
                ACLMessage accept = null;
                Enumeration e = responses.elements();
                while (e.hasMoreElements()) {
                    ACLMessage msg = (ACLMessage) e.nextElement();
                    if (msg.getPerformative() == ACLMessage.PROPOSE) {
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        acceptances.addElement(reply);
                        int proposal = Integer.parseInt(msg.getContent());

                        if (proposal > bestProposal) {
                            bestProposal = proposal;
                            bestProposer = msg.getSender();
                            accept = reply;
                        }
                    }
                }
                // Accept the proposal of the best proposer
                if (accept != null) {
                    System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
                    accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                }
            }

            protected void handleInform(ACLMessage inform) {
                System.out.println("Agent " + inform.getSender().getName() + " successfully performed action: "+ inform.getContent());
            }
        });
    }

    private void retrieveWakeUpPreference() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(preferenceAgent);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
        msg.setContent("preference-request");

        addBehaviour(new AchieveREInitiator(this, msg) {
            protected void handleInform(ACLMessage inform) {
                System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
                String preference = inform.getContent();
                ((PersonalAgent) myAgent).setPreference(preference);

                System.out.println("Received preference " + preference);
                ((PersonalAgent) myAgent).requestWakeUpNotification();
            }

            protected void handleRefuse(ACLMessage refuse) {
                System.out.println("Agent " + refuse.getSender().getName() + " refused to perform the requested action");
            }
        });
    }

    /**
     * Retains an agent provided a service.
     *
     * @param serviceType - the service type.
     * @param agent       - the agent providing a service.
     */
    public void addServiceAgent(String serviceType, AID agent) {
        if (serviceType.equals(ServiceType.AMBIENT_AGENT))
            ambientAgents.add(agent);
        if (serviceType.equals(ServiceType.PREFERENCE_AGENT)) {
            if (preferenceAgent != null)
                Log.log(this, "Warning: a second preference agent found.");
            preferenceAgent = agent;
        }
        if (preferenceAgent != null && ambientAgents.size() >= 2)
            onDiscoveryCompleted();
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }

    @Override
    protected void takeDown() {
        // Printout a dismissal message
        Log.log(this, "terminating.");
    }
}
