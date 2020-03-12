package my;

import base.Action;
import base.Perceptions;
import communication.AgentID;
import communication.AgentMessage;
import communication.SocialAction;
import gridworld.GridPosition;
import gridworld.GridRelativeOrientation;
import gridworld.ProbabilityMap;
import hunting.AbstractWildlifeAgent;
import hunting.WildlifeAgentType;

import java.util.ArrayList;
import java.lang.Math;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation for predator agents.
 *
 * @author Alexandru Sorici
 */
public class MyPredator extends AbstractWildlifeAgent {
    /**
     * Default constructor.
     */

    GridPosition preyPosition = null;
    int distanceToPrey = 0;
    MyEnvironment.MyAction lastAction = null;
    private HashSet<AgentID> knownPredators;

    public MyPredator() {
        super(WildlifeAgentType.PREDATOR);
        knownPredators = new HashSet<>();
    }

    public double getDistance(GridPosition pos1, GridPosition pos2) {
        int deltaX = pos1.getX() - pos2.getX();
        int deltaY = pos1.getY() - pos2.getY();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    @Override
    public Action response(Perceptions perceptions) {
        MyEnvironment.MyPerceptions wildlifePerceptions = (MyEnvironment.MyPerceptions) perceptions;
        Set<AgentMessage> receivedMessages = wildlifePerceptions.getMessages();

        for (AgentMessage message : receivedMessages) {
            if (preyPosition == null)
                preyPosition = (GridPosition) message.getContent();
        }

        GridPosition agentPos = wildlifePerceptions.getAgentPos();

        ArrayList<MyEnvironment.MyAction> allActions = new ArrayList<>();
        allActions.add(MyEnvironment.MyAction.NORTH);
        allActions.add(MyEnvironment.MyAction.SOUTH);
        allActions.add(MyEnvironment.MyAction.WEST);
        allActions.add(MyEnvironment.MyAction.EAST);

        // remove actions which are unavailable because of obstacles
        for (GridPosition obs : wildlifePerceptions.getObstacles()) {
            if (agentPos.getDistanceTo(obs) > 1)
                continue;

            GridRelativeOrientation relativeOrientation = agentPos.getRelativeOrientation(obs);

            switch (relativeOrientation) {
                case FRONT:
                    // don't go up
                    allActions.remove(MyEnvironment.MyAction.NORTH);
                    break;
                case BACK:
                    // don't go down
                    allActions.remove(MyEnvironment.MyAction.SOUTH);
                    break;
                case LEFT:
                    // don't go left
                    allActions.remove(MyEnvironment.MyAction.WEST);
                    break;
                case RIGHT:
                    // don't go right
                    allActions.remove(MyEnvironment.MyAction.EAST);
                    break;
                default:
                    break;
            }
        }

        MyEnvironment.MyAction chosenAction = MyEnvironment.MyAction.NORTH;
        double currentBestDistance = Double.POSITIVE_INFINITY;

		Set<AgentID> nearbyPredators = wildlifePerceptions.getNearbyPredators().keySet();
		nearbyPredators.remove(AgentID.getAgentID(this));
		knownPredators.addAll(nearbyPredators);

        for (GridPosition preyPositon : wildlifePerceptions.getNearbyPrey()) {
            this.preyPosition = preyPositon;
            distanceToPrey = 4;
            GridRelativeOrientation relativePos = agentPos.getRelativeOrientation(preyPositon);
            switch (relativePos) {
                case FRONT:
                    if (getDistance(agentPos, preyPositon) < currentBestDistance) {
                        chosenAction = MyEnvironment.MyAction.NORTH;
                        currentBestDistance = getDistance(agentPos, preyPositon);
                    }
                case FRONT_LEFT:
                    if (getDistance(agentPos, preyPositon) < currentBestDistance) {
                        chosenAction = MyEnvironment.MyAction.NORTH;
                    }
                case FRONT_RIGHT:
                    if (getDistance(agentPos, preyPositon) < currentBestDistance) {
                        chosenAction = MyEnvironment.MyAction.NORTH;
                        currentBestDistance = getDistance(agentPos, preyPositon);
                    }

                case BACK:
                    if (getDistance(agentPos, preyPositon) < currentBestDistance) {
                        chosenAction = MyEnvironment.MyAction.SOUTH;
                        currentBestDistance = getDistance(agentPos, preyPositon);
                    }
                case BACK_LEFT:
                    if (getDistance(agentPos, preyPositon) < currentBestDistance) {
                        chosenAction = MyEnvironment.MyAction.SOUTH;
                        currentBestDistance = getDistance(agentPos, preyPositon);
                    }

                case BACK_RIGHT:
                    if (getDistance(agentPos, preyPositon) < currentBestDistance) {
                        chosenAction = MyEnvironment.MyAction.SOUTH;
                        currentBestDistance = getDistance(agentPos, preyPositon);
                    }
                case LEFT:
                    if (getDistance(agentPos, preyPositon) < currentBestDistance) {
                        chosenAction = MyEnvironment.MyAction.WEST;
                        currentBestDistance = getDistance(agentPos, preyPositon);
                    }
                case RIGHT:
                    if (getDistance(agentPos, preyPositon) < currentBestDistance) {
                        chosenAction = MyEnvironment.MyAction.EAST;
                        currentBestDistance = getDistance(agentPos, preyPositon);
                    }
                default:
                    break;
            }
        }

        if (currentBestDistance != Double.POSITIVE_INFINITY) {
            lastAction = chosenAction;
            if (preyPosition != null) {
                SocialAction socialAction = new SocialAction(lastAction);
                for (AgentID knowPredator : knownPredators) {
                    AgentMessage message = new AgentMessage(AgentID.getAgentID(this), knowPredator, preyPosition);
                    socialAction.addOutgoingMessage(message);
                }
                return socialAction;
            }
            return new SocialAction(chosenAction);

        } else if (distanceToPrey > 0 || preyPosition != null) {
            distanceToPrey -= 1;
            GridRelativeOrientation relativePos = agentPos.getRelativeOrientation(this.preyPosition);
            switch (relativePos) {
                case FRONT:
                    if (getDistance(agentPos, this.preyPosition) < currentBestDistance) {
                        chosenAction = MyEnvironment.MyAction.NORTH;
                    }
                case FRONT_LEFT:
                    if (getDistance(agentPos, this.preyPosition) < currentBestDistance) {
                        chosenAction = MyEnvironment.MyAction.NORTH;
                    }
                case FRONT_RIGHT:
                    if (getDistance(agentPos, this.preyPosition) < currentBestDistance) {
                        chosenAction = MyEnvironment.MyAction.NORTH;
                    }
                case BACK:
                    if (getDistance(agentPos, this.preyPosition) < currentBestDistance) {
                        chosenAction = MyEnvironment.MyAction.SOUTH;
                    }
                case BACK_LEFT:
                    if (getDistance(agentPos, this.preyPosition) < currentBestDistance) {
                        chosenAction = MyEnvironment.MyAction.SOUTH;
                    }

                case BACK_RIGHT:
                    if (getDistance(agentPos, this.preyPosition) < currentBestDistance) {
                        chosenAction = MyEnvironment.MyAction.SOUTH;
                    }
                case LEFT:
                    if (getDistance(agentPos, this.preyPosition) < currentBestDistance) {
                        chosenAction = MyEnvironment.MyAction.WEST;
                    }
                case RIGHT:
                    if (getDistance(agentPos, this.preyPosition) < currentBestDistance) {
                        chosenAction = MyEnvironment.MyAction.EAST;
                    }
                default:
                    break;
            }
            lastAction = chosenAction;
            SocialAction socialAction = new SocialAction(lastAction);
            for (AgentID knowPredator : knownPredators) {
                AgentMessage message = new AgentMessage(AgentID.getAgentID(this), knowPredator, preyPosition);
                socialAction.addOutgoingMessage(message);
            }
            return socialAction;
        }

//        if (lastAction != null) {
//            if (allActions.contains(lastAction)) {
//				return new SocialAction(lastAction);
//            }
//        }

        int randomIndex = (int) (Math.random() * allActions.size());
        lastAction = allActions.get(randomIndex);

        return new SocialAction(lastAction);
    }
}
