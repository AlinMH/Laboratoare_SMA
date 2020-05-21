package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;

import java.util.*;

import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.wrapper.ControllerException;
import platform.Ballot;
import platform.Log;
import platform.VoteResult;

import javax.sound.midi.Soundbank;

/**
 * ElectionManager agent.
 */
public class ElectionManagerAgent extends Agent {
    /**
     *
     */
    private static final long serialVersionUID = -3397689918969697329L;
    public static final int NR_OF_WINNERS = 3;

    /**
     * The AID of the VoteCollector agent used by this ElectionManager
     */
    AID voteCollector;

    /**
     * The names of the regions from which the ElectionManager awaits vote results collected by the VoteCollector.
     */
    List<String> regionVoteNames;

    boolean isVoteCollectorInRange;

    private static final MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

    @Override
    public void setup() {
        Log.log(this, "Hello");

        // get the AID of the vote collector agent
        voteCollector = (AID) getArguments()[0];
        Log.log(this, "My vote collector agent is: " + voteCollector);

        // get the list of region names (keys in the json file) from which the ElectionManager awaits votes
        regionVoteNames = (List<String>) getArguments()[1];
        Log.log(this, "Awaiting votes from the following regions: " + regionVoteNames);

        isVoteCollectorInRange = true;

        // Register the preference-agent service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        // ambient-wake-up-call
        ServiceDescription sd = new ServiceDescription();
        sd.setType(ServiceType.ELECTION_MANAGEMENT);
        sd.setName("election-management");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new AchieveREResponder(this, template) {
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
                ACLMessage agree = request.createReply();

                String requestLanguage = request.getLanguage();
                if (requestLanguage != null && requestLanguage.startsWith("region") && !isVoteCollectorInRange) {
                    try {
                        VoteResult voteResult = (VoteResult) request.getContentObject();
                        isVoteCollectorInRange = true;
                        doSTV(voteResult, requestLanguage);
                        agree.setPerformative(ACLMessage.AGREE);

                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                } else if (isVoteCollectorInRange) {
                    String content = request.getContent();
                    System.out.println("[EM] Agent " + getLocalName() + ": Action successfully performed");
                    agree.setPerformative(ACLMessage.AGREE);
                    isVoteCollectorInRange = false;

                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.addReceiver(voteCollector);
                    msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                    msg.setContent(content);

                    addBehaviour(new AchieveREInitiator(myAgent, msg) {
                        protected void handleInform(ACLMessage inform) {
                            System.out.println("[EM] Agent " + inform.getSender().getName() + " successfully performed the requested action");
                        }

                        protected void handleRefuse(ACLMessage refuse) {
                            System.out.println("[EM] Agent " + refuse.getSender().getName() + " refused to perform the requested action");
                        }
                    });

                } else {
                    System.out.println("[EM] Agent " + getLocalName() + ": Action refused!");
                    agree.setPerformative(ACLMessage.REFUSE);
                }

                return agree;
            }
        });
    }

    private void doSTV(VoteResult voteResult, String region) {
        List<Ballot> ballots = voteResult.getBallots();
        int nrOfVotes = 0;
        for (Ballot ballot : ballots) {
            nrOfVotes += ballot.getCount();
        }

        String[] candidates = new String[]{"c1", "c2", "c3", "c4", "c5"};
        Map<String, Integer> voteCounter = new HashMap<>();
        List<String> candidatesList = new ArrayList<>(Arrays.asList(candidates));

        for (String candidate : candidates) {
            voteCounter.put(candidate, 0);
        }


        System.out.println("[EM] STV init for region :" + region);
        int qDroop = (int) (Math.floor(nrOfVotes / (NR_OF_WINNERS + 1)) + 1);

        for (Ballot ballot : ballots) {
            String candidateKey = ballot.getCandidates().get(0);
            int votes = voteCounter.get(candidateKey);
            votes += ballot.getCount();
            voteCounter.put(candidateKey, votes);
        }

        while (candidatesList.size() != NR_OF_WINNERS) {
            boolean winner = false;

            for (String candidate : candidatesList) {
                int votes = voteCounter.get(candidate);

                if (votes > qDroop) {
                    winner = true;

                    int surplus = votes - qDroop;
                    voteCounter.put(candidate, votes - surplus);

                    for (Ballot ballot : ballots) {
                        if (ballot.getCandidates().get(0).equals(candidate)) {
                            String candidateAux = null;
                            for (int i = 1; i < ballot.getCandidates().size(); i++) {
                                candidateAux = ballot.getCandidates().get(i);
                                if (candidatesList.contains(candidateAux))
                                    break;
                            }
                            voteCounter.put(candidateAux, voteCounter.get(candidateAux) + (ballot.getCount() / votes) * surplus);
                        }
                    }
                    break;
                }
            }

            if (!winner) {
                int leastVotes = Integer.MAX_VALUE;
                String candidateToRemove = null;

                for (String candidate : candidatesList) {
                    int votes = voteCounter.get(candidate);
                    if (votes < leastVotes) {
                        leastVotes = votes;
                        candidateToRemove = candidate;
                    }
                }

                for (Ballot ballot : ballots) {
                    if (ballot.getCandidates().get(0).equals(candidateToRemove)) {
                        String candidateAux = null;
                        for (int i = 1; i < ballot.getCandidates().size(); i++) {
                            candidateAux = ballot.getCandidates().get(i);
                            if (candidatesList.contains(candidateAux))
                                break;
                        }
                        voteCounter.put(candidateAux, voteCounter.get(candidateAux) + (ballot.getCount()));
                    }
                }
                candidatesList.remove(candidateToRemove);
            }
        }
        regionVoteNames.remove(region);
        System.out.println("[EM] Winners for " + region + ": " + candidatesList.toString());
        System.out.println("[EM] Remaining regions " + regionVoteNames.size());
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
