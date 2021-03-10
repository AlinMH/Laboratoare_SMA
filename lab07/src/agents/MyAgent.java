package agents;

import java.util.LinkedList;
import java.util.List;

import agents.behaviors.*;
import jade.core.AID;
import jade.core.Agent;

/**
 * The Agent.
 */
public class MyAgent extends Agent {
    /**
     * The serial UID.
     */
    private static final long serialVersionUID = 2081456560111009192L;

    /**
     * Known child agents.
     */
    List<AID> childAgents = new LinkedList<>();

    /**
     * The ID of the parent.
     */
    AID parentAID;
    /**
     * The value associated to the agent.
     */
    int agentValue;

    /**
     * @param childAID the ID of the child to add.
     */
    public void addChildAgent(AID childAID) {
        childAgents.add(childAID);
    }

    /**
     * @return the list of IDs of child agents.
     */
    public List<AID> getChildAgents() {
        return childAgents;
    }

    @Override
    protected void setup() {
        parentAID = (AID) getArguments()[0];
        agentValue = ((Integer) getArguments()[1]).intValue();

        System.out.println("[HELLO] Hello from agent: " + getAID().getName() + " with parentAID: " + parentAID);

        // add the RegistrationSendBehavior
        if (parentAID != null) {
            System.out.println("[REGISTRATION] Registration sender behavior for this agent starts in 1 second");
            addBehaviour(new RegistrationSenderBehavior(this, 1000, parentAID));
        } else {
            System.out.println("[REGISTRATION] Registration sender behavior need not start for agent " + getAID().getName());
        }

        // add the RegistrationReceiveBehavior
        addBehaviour(new RegistrationReceiverBehavior(this));
        addBehaviour(new ProcedureStartReceiverBehavior(this));
        addBehaviour(new MaxBehavior(this));
        addBehaviour(new ReceiveMaxValueBehavior(this));
    }

	public AID getParentAID() {
		return parentAID;
	}

	public int getAgentValue() {
		return agentValue;
	}

	public void setAgentValue(int agentValue) {
		this.agentValue = agentValue;
	}

	@Override
    protected void takeDown() {
        // Printout a dismissal message

        String out = "[END PROCEDURE] Agent " + getAID().getLocalName() + " max value is " + agentValue;
        System.out.println(out);
    }
}
