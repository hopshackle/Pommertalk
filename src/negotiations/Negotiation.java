package negotiations;

import core.*;
import objects.*;
import utils.*;

import java.util.*;
import java.util.stream.*;

public class Negotiation {

    private ForwardModel forwardModel;
    private List<Agreement> finalAgreements = Collections.emptyList();

    public Negotiation(ForwardModel fm) {
        this.forwardModel = fm;
        // TODO: We could change this to taking in just a list of the currently alive agents?
        // tODO: (Which would be safer and arguably better design)
        // TODO: But by passing in a link to the Forward Model you may have more flexibility
        // TODO: Just please don't modify it!!!
    }

    /*
    An alternative constructor primarily intended for testing and copying
     */
    public Negotiation(List<Agreement> agreements) {
        this.finalAgreements = agreements;
    }

    /*
    Then this method will be called by the core game engine to conduct the negotiations
     */
    public void runNegotiationProcess() {
        // do stuff
        // crucially, make sure this populates the finalAgreement List
        finalAgreements = new ArrayList<>();
        finalAgreements.add(new Agreement(Types.TILETYPE.AGENT0, Types.TILETYPE.AGENT1, Agreement.TYPE.SHARE_VISION));
    }

    public List<Agreement> getFinalAgreements() {
        return List.copyOf(finalAgreements);
    }

    private int indexFromTileType(Types.TILETYPE tileType) {
        return tileType.getKey() - Types.TILETYPE.AGENT0.getKey();
    }

    public Set<Integer> getAgreements(int playerIndex, Agreement.TYPE type) {
        return finalAgreements.stream()
                .filter(a -> a.agreement == type
                        && (indexFromTileType(a.participants.get(0)) == playerIndex
                        || indexFromTileType(a.participants.get(1)) == playerIndex))
                .map(a -> (indexFromTileType(a.participants.get(0)) == playerIndex) ? indexFromTileType(a.participants.get(1)) : indexFromTileType(a.participants.get(0)))
                .collect(Collectors.toSet());
    }

    public boolean isPermitted(Types.ACTIONS action, Avatar agent, GameObject[] allAgents) {
        // First we check for STAY_APART agreements
        // easiest thing to do is move them on board, and then check to see if the agreement is met
        // agent.
        List<Agreement> agentAgreements = finalAgreements.stream()
                .filter(a -> a.participants.contains(agent.getType()))
                .collect(Collectors.toList());

        Vector2d targetSpace = agent.getDesiredCoordinate();
        for (Agreement a : agentAgreements) {
            Types.TILETYPE other = a.participants.get(0);
            if (other == agent.getType())
                other = a.participants.get(1);
            GameObject otherAgent = allAgents[other.getKey() - Types.TILETYPE.AGENT0.getKey()];
            int manhattanDistance = targetSpace.manhattanDistance(otherAgent.getPosition());
            switch (a.agreement) {
                case STAY_APART:
                    if (action.getDirection() != Types.DIRECTIONS.NONE && manhattanDistance <= Types.STAY_APART_DISTANCE)
                        return false;
                    break;
                case NO_BOMB_PLACING:
                    if (action == Types.ACTIONS.ACTION_BOMB && manhattanDistance <= Types.NO_BOMB_DISTANCE)
                        return false;
                    break;
                case SHARE_VISION:
                    return true;
                default:
                    throw new AssertionError("Agreement type not yet implemented: " + a.agreement);
            }
        }


        return true;
    }

    public Negotiation reduce(int playerIdx) {
        List<Agreement> visibleAgreements = finalAgreements.stream()
                .filter(a -> a.participants.get(0).getKey() == playerIdx + Types.TILETYPE.AGENT0.getKey() ||
                        a.participants.get(1).getKey() == playerIdx + Types.TILETYPE.AGENT0.getKey())
                .collect(Collectors.toList());
        return new Negotiation(visibleAgreements);
    }
}
