package my;

import base.Action;
import base.Perceptions;
import gridworld.GridPosition;
import gridworld.GridRelativeOrientation;
import gridworld.ProbabilityMap;
import hunting.AbstractWildlifeAgent;
import hunting.WildlifeAgentType;

import java.util.ArrayList;
import java.lang.Math;

/**
 * Implementation for predator agents.
 *
 * @author Alexandru Sorici
 */
public class MyPredator extends AbstractWildlifeAgent {
	/**
	 * Default constructor.
	 */

	GridPosition preyPosition;
	int counter = 0;
	MyEnvironment.MyAction lastAction = null;

	public MyPredator() {
		super(WildlifeAgentType.PREDATOR);
	}

	public double getDistance(GridPosition pos1, GridPosition pos2) {
		int deltaX = pos1.getX() - pos2.getX();
		int deltaY = pos1.getY() - pos2.getY();
		return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
	}

	@Override
	public Action response(Perceptions perceptions) {
		MyEnvironment.MyPerceptions wildlifePerceptions = (MyEnvironment.MyPerceptions) perceptions;
		GridPosition agentPos = wildlifePerceptions.getAgentPos();
		System.out.println(agentPos);

		ProbabilityMap probMap = new ProbabilityMap();

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

		// save available moves

		// examine which prey is the closest and goes after it
		// if no prey is visible goes in one directiona
		MyEnvironment.MyAction chosenAction = MyEnvironment.MyAction.NORTH;
		double currentBestDistance = Double.POSITIVE_INFINITY;

		for (GridPosition preyPositon : wildlifePerceptions.getNearbyPrey()) {
			this.preyPosition = preyPositon;
			counter = 4;
			System.out.println("SEEING PRAY");
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
		//SEEN PREY GO TOWARDS IT
		if (currentBestDistance != Double.POSITIVE_INFINITY) {
			lastAction = chosenAction;
			return chosenAction;
		} else {
			//PREY OUT OF RANGE, GO TOWARDS THAT DIRECTION
			if (counter > 0) {
				counter -= 1;
				GridRelativeOrientation relativePos = agentPos.getRelativeOrientation(this.preyPosition);
				System.out.println("SEEN PREY GOING TO LAST KNOWN LOCATION");
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
				return chosenAction;
			}

		}
		// DO NOT KNOW WHERE PRAY IS, GOING SAME DIRECTION UNTILL WALL HIT
		if (lastAction != null) {
			if (allActions.contains(lastAction)) {
				return lastAction;
			}
		}

		int randomIndex = (int) (Math.random() * allActions.size());
		lastAction = allActions.get(randomIndex);
		return lastAction;

	}
}
