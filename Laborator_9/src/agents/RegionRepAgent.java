package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.Profile;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.Iterator;
import jade.wrapper.ControllerException;
import platform.Log;
import platform.VoteReader;
import platform.VoteResult;

import java.io.IOException;
import java.util.Random;

/**
 * The Region Representative Agent.
 */
public class RegionRepAgent extends Agent {
    /**
     * The serial UID.
     */
    private static final long serialVersionUID = 2081456560111009192L;


    /**
     * Known election manager agent
     */
    AID electionManagerAgent;

    /**
     * key name for the region vote results from json input file
     */
    String regionVoteKey;


    VoteResult voteResult;

    private static final MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

    @Override
    protected void setup() {
        Log.log(this, "Hello from RegionRepresantative: " + this.getLocalName());
        Log.log(this, "Adding DF subscribe behaviors");

        regionVoteKey = (String) getArguments()[0];

        AID dfAgent = getDefaultDF();
        Log.log(this, "Default DF Agent: " + dfAgent);

        // Create election service discovery behavior
        // Build the DFAgentDescription which holds the service descriptions for the the ambient-agent service

        VoteReader voteReader = new VoteReader();
        voteResult = voteReader.getVoteResult(regionVoteKey);


        DFAgentDescription DFDesc = new DFAgentDescription();
        ServiceDescription serviceDesc = new ServiceDescription();
        serviceDesc.setType(ServiceType.ELECTION_MANAGEMENT);
        DFDesc.addServices(serviceDesc);

        SearchConstraints cons = new SearchConstraints();
        cons.setMaxResults(new Long(1));

        // add sub behavior for ambient-agent service discovery
        this.addBehaviour(new SubscriptionInitiator(this,
                DFService.createSubscriptionMessage(this, dfAgent, DFDesc, cons)) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void handleInform(ACLMessage inform) {
                Log.log(myAgent, "Notification received from DF");

                try {
                    DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
                    if (results.length > 0)
                        for (DFAgentDescription dfd : results) {
                            AID provider = dfd.getName();
                            // The same agent may provide several services; we are interested
                            // in the election-management one
                            for (Iterator it = dfd.getAllServices(); it.hasNext(); ) {
                                ServiceDescription sd = (ServiceDescription) it.next();
                                if (sd.getType().equals(ServiceType.ELECTION_MANAGEMENT)) {
                                    Log.log(myAgent, ServiceType.ELECTION_MANAGEMENT, "service found: Service \"", sd.getName(),
                                            "\" provided by agent ", provider.getName());
                                    addServiceAgent(ServiceType.ELECTION_MANAGEMENT, provider);

                                    // if we found the ElectionManager we can cancel the subscription
                                    cancel(inform.getSender(), true);


                                }
                            }
                        }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });

        // Only for VC
        addBehaviour(new AchieveREResponder(this, template) {
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
                ACLMessage inform = request.createReply();
                try {
                    inform.setContentObject(voteResult);
                    inform.setLanguage(regionVoteKey);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("[RR] Agent " + getLocalName() + ": Action successfully performed");
                inform.setPerformative(ACLMessage.INFORM);
                return inform;
            }
        });
    }

    /**
     * This method will be called when all the needed agents have been discovered.
     */
    protected void onDiscoveryCompleted() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(electionManagerAgent);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        try {
            msg.setContent(getContainerController().getContainerName() + "_" + getLocalName());
        } catch (ControllerException e) {
            e.printStackTrace();
        }

        addBehaviour(new AchieveREInitiator(this, msg) {
            protected void handleInform(ACLMessage inform) {
                System.out.println("[" + regionVoteKey + "] Agent " + inform.getSender().getName() + " successfully performed the requested action");
            }

            protected void handleRefuse(ACLMessage refuse) {
                System.out.println("[" + regionVoteKey + "] Agent " + refuse.getSender().getName() + " refused to perform the requested action");
                retry();
            }

            private void retry() {
                Random random = new Random();
                int waitTime = random.nextInt(1);
                // Initiate the request once more
                myAgent.addBehaviour(new WakerBehaviour(myAgent, (waitTime + 1) * 1000) {
                    @Override
                    protected void onWake() {
                        super.onWake();
                        myAgent.addBehaviour(new AchieveREInitiator(myAgent, msg) {
                            protected void handleInform(ACLMessage inform) {
                                System.out.println("[" + regionVoteKey + "] Agent " + inform.getSender().getName() + " successfully performed the requested action");
                            }

                            protected void handleRefuse(ACLMessage refuse) {
                                System.out.println("[" + regionVoteKey + "] Agent " + refuse.getSender().getName() + " refused to perform the requested action");
                                retry();
                            }
                        });
                    }
                });
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

        if (serviceType.equals(ServiceType.ELECTION_MANAGEMENT)) {
            if (electionManagerAgent != null)
                Log.log(this, "Warning: a second preference agent found.");
            electionManagerAgent = agent;
        }

        if (electionManagerAgent != null)
            onDiscoveryCompleted();
    }

    @Override
    protected void takeDown() {
        // Printout a dismissal message
        Log.log(this, "terminating.");
    }
}
