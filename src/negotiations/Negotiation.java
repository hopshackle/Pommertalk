package negotiations;

import Message.MessageManager;
import core.*;
import objects.*;
import players.Player;
import utils.*;

import java.util.*;
import java.util.stream.*;

public class Negotiation {

    private List<Agreement> finalAgreements = Collections.emptyList();
    private MessageManager messageManager = new MessageManager(true);
    private List<Negotiator> negotiatingAgents = new ArrayList<>();

    public static Negotiation createForPlayers(List<Player> players) {
        return new Negotiation(players, true);
    }

    public static Negotiation createFromAgreements(List<Agreement> agreements) {
        return new Negotiation(agreements);
    }

    private Negotiation(List<Player> agents, boolean sillyFlag) {
        for (Player agent : agents) {
            if (agent instanceof Negotiator) {
                negotiatingAgents.add((Negotiator) agent);
            }
        }
    }

    /*
    An alternative constructor primarily intended for testing and copying
     */
    private Negotiation(List<Agreement> agreements) {
        this.finalAgreements = agreements;
    }

    public void startPhaseOne(GameState gs) {
        //Call method in each agent to initiate proposals
        for (Negotiator negotiator : negotiatingAgents) {
            negotiator.makeProposals(gs, messageManager);
        }
    }

    public void startPhaseTwo(GameState gs) {
        messageManager.FirstPhaseEnd();
        //Call method in each agent to start responses
        // TODO: for each proposal from the MessageManager,
    }

    public void endPhaseTwo(GameState gs) {
        messageManager.SecondPhaseEnd();
        //Populate final agreements, with outcome
    }

    public List<Agreement> getFinalAgreements() {
        return List.copyOf(finalAgreements);
    }

    public Set<Integer> getAgreements(int playerIndex, Agreement.TYPE type) {
        return finalAgreements.stream()
                .filter(a -> a.getType() == type
                        && (a.getParticipant1Id() == playerIndex
                        || a.getParticipant2Id() == playerIndex))
                .map(a -> (a.getParticipant1Id() == playerIndex) ? a.getParticipant2Id() : a.getParticipant1Id())
                .collect(Collectors.toSet());
    }

    public boolean isPermitted(Types.ACTIONS action, Avatar agent, GameObject[] allAgents) {
        // First we check for STAY_APART agreements
        // easiest thing to do is move them on board, and then check to see if the agreement is met
        // agent.
        List<Agreement> agentAgreements = finalAgreements.stream()
                .filter(a -> a.getParticipant1() == agent.getType() || a.getParticipant2() == agent.getType())
                .collect(Collectors.toList());

        Vector2d targetSpace = agent.getDesiredCoordinate();
        for (Agreement a : agentAgreements) {
            Types.TILETYPE other = a.getParticipant1();
            if (other == agent.getType())
                other = a.getParticipant2();
            GameObject otherAgent = allAgents[other.getKey() - Types.TILETYPE.AGENT0.getKey()];
            int manhattanDistance = targetSpace.manhattanDistance(otherAgent.getPosition());
            switch (a.getType()) {
                case STAY_APART:
                    if (action.getDirection() != Types.DIRECTIONS.NONE && manhattanDistance <= Types.STAY_APART_DISTANCE)
                        return false;
                    break;
                case NO_BOMB_PLACING:
                    if (action == Types.ACTIONS.ACTION_BOMB && manhattanDistance <= Types.NO_BOMB_DISTANCE)
                        return false;
                    break;
                case SHARE_VISION:
                case ALLIANCE:
                    return true;
                default:
                    throw new AssertionError("Agreement type not yet implemented: " + a.getType());
            }
        }


        return true;
    }

    public Negotiation reduce(int playerIdx) {
        List<Agreement> visibleAgreements = finalAgreements.stream()
                .filter(a -> a.getParticipant1Id() == playerIdx || a.getParticipant2Id() == playerIdx)
                .collect(Collectors.toList());
        return new Negotiation(visibleAgreements);
    }

    public Negotiation copy(List<Player> players) {
        Negotiation copy = new Negotiation(players, true);
        copy.finalAgreements = List.copyOf(finalAgreements);
        // TODO: MessageManager needs to have a copy() function added if we want to copy Game in the middle of a negotiation
        copy.messageManager = messageManager;
        return copy;
    }
}
