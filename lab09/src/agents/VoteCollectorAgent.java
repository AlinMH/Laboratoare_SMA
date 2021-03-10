package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.wrapper.ControllerException;
import platform.Log;
import platform.VoteResult;

import java.io.IOException;

/**
 * The Vote Collector Agent.
 */
public class VoteCollectorAgent extends Agent {
	/**
	 * The serial UID.
	 */
	private AID electionManager;
	private String homeContainerName;

	private AID regionRep;

	private VoteResult currentVoteResult;
	private String regionVoteKey;

	private static final long serialVersionUID = -4316893632718883072L;

	private static final MessageTemplate template = MessageTemplate.and(
			MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
			MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

	@Override
	public void setup() {
		Log.log(this, "Hello");
		electionManager = (AID)getArguments()[0];
		try {
			homeContainerName = getContainerController().getContainerName();
		} catch (ControllerException e) {
			e.printStackTrace();
		}

		addBehaviour(new AchieveREResponder(this, template) {
			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
				ACLMessage agree = request.createReply();
				String content = request.getContent();

				int idx = content.indexOf('_');
				String containerName = content.substring(0, idx);
				String rrLocalName = content.substring(idx + 1);
				regionRep = new AID(rrLocalName, AID.ISLOCALNAME);

				System.out.println("[VC] Agent " + getLocalName() + ": Action successfully performed");
				myAgent.doMove(new ContainerID(containerName, null));
				agree.setPerformative(ACLMessage.AGREE);
				return agree;
			}
		});
	}

	@Override
	protected void beforeMove() {
		super.beforeMove();
	}

	@Override
	protected void afterMove() {
		super.afterMove();

		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		if (regionRep != null) {
			msg.addReceiver(regionRep);
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

			addBehaviour(new AchieveREInitiator(this, msg) {
				protected void handleInform(ACLMessage inform) {
					System.out.println("[VC] Agent " + inform.getSender().getName() + " successfully performed the requested action");
					try {
						currentVoteResult = (VoteResult) inform.getContentObject();
						regionVoteKey = inform.getLanguage();

					} catch (UnreadableException e) {
						e.printStackTrace();
					}
					regionRep = null;
					myAgent.doMove(new ContainerID(homeContainerName, null));
				}

				protected void handleRefuse(ACLMessage refuse) {
					System.out.println("[VC] Agent " + refuse.getSender().getName() + " refused to perform the requested action");
				}
			});
		} else {
			// Send vote results to EM
			msg.addReceiver(electionManager);
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			try {
				msg.setContentObject(currentVoteResult);
				msg.setLanguage(regionVoteKey);
			} catch (IOException e) {
				e.printStackTrace();
			}
			addBehaviour(new AchieveREInitiator(this, msg) {
				protected void handleInform(ACLMessage inform) {
					System.out.println("[VC] Agent " + inform.getSender().getName() + " successfully performed the requested action");
				}

				protected void handleRefuse(ACLMessage refuse) {
					System.out.println("[VC] Agent " + refuse.getSender().getName() + " refused to perform the requested action");
				}
			});
		}

	}

	@Override
	protected void takeDown() {
		// Printout a dismissal message
		Log.log(this, "terminating.");
	}
}
