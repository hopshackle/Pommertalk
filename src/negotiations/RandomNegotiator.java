package negotiations;

import Message.MessageManager;
import core.GameState;
import utils.Types;

import java.util.*;

public class RandomNegotiator implements Negotiator {

    public Random rnd;
    private boolean debug = false;
    private static Agreement.TYPE[] types = Agreement.TYPE.values();

    public RandomNegotiator(long seed) {
        rnd = new Random(seed);
    }
    /**
     * Function requests an action from the agent, given current game state observation.
     *
     * @param playerIndex
     * @param gs          - current game state
     * @param manager
     * @return - an array of Agreements that the player would like to propose
     */
    @Override
    public void makeProposals(int playerIndex, GameState gs, MessageManager manager) {
        for (int i = 0; i < Types.NEGOTIATION_PROPOSAL_LIMIT; i++) {
            int player = rnd.nextInt(3);
            if (playerIndex <= player) player++;
            Agreement.TYPE offerType = types[rnd.nextInt(types.length)];
            manager.SendProposal(playerIndex, player, offerType);
            if (debug)
                System.out.println(String.format("Proposal sent: %d -> %d: %s", playerIndex, player, offerType));
        }
    }

    /**
     * Function requests an action from the agent, given current game state observation.
     *
     * @param playerIndex
     * @param gs          - current game state
     * @param manager
     * @return - true|false depending on whether the agent agrees or not
     */
    @Override
    public void reviewProposals(int playerIndex, GameState gs, MessageManager manager) {
        List<Agreement> proposals = manager.getPlayerProposalAgreements(playerIndex);
        //System.out.println();
        //System.out.println(String.format("Player %d received %d proposals", playerIndex, proposals.size()));
        for (Agreement a : proposals) {
            if (a.getParticipant1Id() == playerIndex)
                throw new AssertionError("Player seems to have sent themselves a Proposal?" + a.toString());
            int response = rnd.nextDouble() > 0.5 ? MessageManager.Response.ACCEPT.ordinal() : MessageManager.Response.DENY.ordinal();
            manager.SendResponse(a.getParticipant2Id(), a.getParticipant1Id(), a.getType(), response);
            if (debug)
                System.out.println(String.format("%s: %d -> %d: %s", MessageManager.Response.values()[response], a.getParticipant1Id(), a.getParticipant2Id(), a.getType()));
        }
    }
}
