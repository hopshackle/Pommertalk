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
    private boolean debug = false;

    public MessageManager getMessageManager() {
        return messageManager;
    }

    private Map<Integer, Negotiator> negotiatingAgents = new HashMap<>();

    public static Negotiation createForPlayers(List<Player> players) {
        return new Negotiation(players, true);
    }

    public static Negotiation createFromAgreements(List<Agreement> agreements) {
        return new Negotiation(agreements);
    }

    private Negotiation(List<Player> agents, boolean sillyFlag) {
        for (int i = 0; i < agents.size(); i++) {
            if (agents.get(i) instanceof Negotiator) {
                negotiatingAgents.put(i, (Negotiator) agents.get(i));
            } else {
                negotiatingAgents.put(i, null);
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
        Types.TILETYPE[] aliveAgents = gs.getAliveAgentIDs();
        for (Types.TILETYPE avatar : aliveAgents) {
            int playerIndex = avatar.getKey() - Types.TILETYPE.AGENT0.getKey();
            if (negotiatingAgents.get(playerIndex) != null)
                negotiatingAgents.get(playerIndex).makeProposals(playerIndex, gs, messageManager);
        }
    }

    public void startPhaseTwo(GameState gs) {
        messageManager.FirstPhaseEnd();
        //Call method in each agent to start responses
        Types.TILETYPE[] aliveAgents = gs.getAliveAgentIDs();
        for (Types.TILETYPE avatar : aliveAgents) {
            int playerIndex = avatar.getKey() - Types.TILETYPE.AGENT0.getKey();
            if (negotiatingAgents.get(playerIndex) != null)
                negotiatingAgents.get(playerIndex).reviewProposals(playerIndex, gs, messageManager);
        }
    }

    public void endPhaseTwo(GameState gs) {
        finalAgreements = messageManager.SecondPhaseEnd();
        //Populate final agreements, with outcome
        System.out.println("Negotiation results:");
        for (Agreement a : finalAgreements)
            System.out.println("\t" + a.toString());
    }

    public List<Agreement> getFinalAgreements() {
        return finalAgreements;
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
                case NO_BOMB_KICKING:
                    // we need to determine fistely if the
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
        // TODO: MessageManager needs to have a copy() function added if we want to copy Game in the middle of a negotiation
        // TODO: This is not currently needed as RHEA/MCTS do not extend into the negotiation phase
        copy.messageManager = messageManager;
        return copy;
    }

    public boolean isKickPermitted(GameObject agent, Vector2d velocity, GameObject[] allAgents) {
        Vector2d normalVelocity = velocity.copy();
        for (Agreement a : finalAgreements) {
            if (a.getType() == Agreement.TYPE.NO_BOMB_KICKING &&
                    (a.getParticipant1() == agent.getType() || a.getParticipant2() == agent.getType())) {
                int otherIndex = a.getParticipant1() == agent.getType() ? a.getParticipant2Id() : a.getParticipant1Id();
                if (allAgents[otherIndex].getLife() > 0) {
                    Vector2d directionToAvoid = allAgents[otherIndex].getPosition().subtract(agent.getPosition());
                    if (debug) System.out.println(String.format("Dot product is %.2f", directionToAvoid.normalDot(normalVelocity)));
                    if (directionToAvoid.normalDot(normalVelocity) > 0.9)
                        return false;
                }
            }
        }
        return true;
    }
}
