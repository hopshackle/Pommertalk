package negotiations;

import Message.MessageManager;
import core.GameState;
import objects.Avatar;
import utils.Types;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class HeuristicsNegotiator implements Negotiator {

    private enum PlayerParams {
        KICK,
        AMMO,
        BLAST
    }


    private Random rnd;
    private double kickWeight;
    private double ammoWeight;
    private double blastWeight;


    public HeuristicsNegotiator(long seed) {
        rnd = new Random(seed);
        kickWeight = rnd.nextDouble();
        ammoWeight = rnd.nextDouble();
        blastWeight = rnd.nextDouble();
    }


    public HeuristicsNegotiator(long seed, double kickImportance, double ammoImportance, double blastImportance) {
        rnd = new Random(seed);
        kickWeight = kickImportance;
        ammoWeight = ammoImportance;
        blastWeight = blastImportance;
    }


    @Override
    public void makeProposals(int playerIndex, GameState gs, MessageManager manager) {

        double[] heuristics = new double[9];

        for (int player = 0; player < 3; player++) {
            int[] currPlayerInfo = new int[3];

            if (player >= playerIndex) { currPlayerInfo = getPlayerInformation(player+1, gs); }
            else { currPlayerInfo = getPlayerInformation(player, gs); }

            heuristics[player * 3] = bombHeuristic(currPlayerInfo);
            heuristics[player * 3 +1] = kickHeuristic(currPlayerInfo);
            heuristics[player * 3 +2] = allianceHeuristic(currPlayerInfo);
        }

        int[] orderedIndexes = getSortArrayIndex(heuristics);

        int i = 0;
        int madeProposals = 0;
        while (madeProposals < Types.NEGOTIATION_PROPOSAL_LIMIT && i < heuristics.length) {

            int proposal = orderedIndexes[i] % 3;
            int player = orderedIndexes[i] / 3;
            if (player >= playerIndex) { player++; }

            switch (proposal) {

                case 0:
                    proposal = (rnd.nextDouble() > 0.5) ? Agreement.TYPE.STAY_APART.ordinal() : Agreement.TYPE.NO_BOMB_PLACING.ordinal();
                    break;

                case 1:
                    proposal = Agreement.TYPE.NO_BOMB_KICKING.ordinal();
                    break;

                case 2:
                    proposal = (rnd.nextDouble() > 0.5) ? Agreement.TYPE.ALLIANCE.ordinal() : Agreement.TYPE.SHARE_VISION.ordinal();
                    break;
            }
            //System.out.println(String.format("Player %d made proposal %d to Player %d", playerIndex, proposal, player));
            manager.SendProposal(playerIndex, player, proposal);
            //System.out.println(String.format("Proposal sent: %d -> %d: %s", playerIndex, player, Agreement.TYPE.values()[proposal]));
            madeProposals++;

            i++;
        }
    }


    @Override
    public void reviewProposals(int playerIndex, GameState gs, MessageManager manager) {

        List<Agreement> proposed = manager.getPlayerProposalAgreements(playerIndex);
        //System.out.println();
        //System.out.println(String.format("Player %d received %d proposals", playerIndex, proposed.size()));

        for (Agreement a : proposed) {

            if (a.getParticipant1Id() == playerIndex)
                throw new AssertionError("Player seems to have sent themselves a Proposal?" + a.toString());

            double heuristic = 0;
            int[] playerInfo = getPlayerInformation(a.getParticipant1Id(), gs);

            switch (a.getType().ordinal()) {

                case 0:
                case 1:
                    heuristic = allianceHeuristic(playerInfo);
                    break;

                case 2:
                case 4:
                    heuristic = bombHeuristic(playerInfo);
                    break;

                case 3:
                    heuristic = kickHeuristic(playerInfo);
                    break;
            }

            if (heuristic > 0.5) {
                manager.SendResponse(playerIndex, a.getParticipant1Id(), a.getType(), MessageManager.Response.ACCEPT.ordinal());
                //System.out.println(String.format("%s: %d -> %d: %s", MessageManager.Response.ACCEPT, a.getParticipant1Id(), a.getParticipant2Id(), a.getType()));
            }
            else {
                manager.SendResponse(playerIndex, a.getParticipant1Id(), a.getType(), MessageManager.Response.DENY.ordinal());
                //System.out.println(String.format("%s: %d -> %d: %s", MessageManager.Response.DENY, a.getParticipant1Id(), a.getParticipant2Id(), a.getType()));
            }
        }

    }


    private int[] getSortArrayIndex(double[] unordered) {

        HashMap<Double, Integer> index = new HashMap<Double, Integer>();
        for (int i = 0; i < unordered.length; i++) {
            index.put(unordered[i], i);
        }

        Arrays.sort(unordered);
        int[] orderedIndexes = new int[unordered.length];
        for (int i = 0; i < orderedIndexes.length; i++) {
            orderedIndexes[i] = index.get(unordered[i]);
        }

        return orderedIndexes;
    }


    private int[] getPlayerInformation(int player, GameState gs) {

        int[] playerInfo =  new int[PlayerParams.values().length];
        Avatar p = gs.getAgent(player);

        playerInfo[PlayerParams.AMMO.ordinal()] = p.getAmmo();
        playerInfo[PlayerParams.BLAST.ordinal()] = p.getBlastStrength();
        if (p.canKick()) { playerInfo[PlayerParams.KICK.ordinal()] = 1; }
        else { playerInfo[PlayerParams.KICK.ordinal()] = 0; }

        return playerInfo;
    }


    private double bombHeuristic(int[] playerInfo) {
        return (
                (playerInfo[PlayerParams.BLAST.ordinal()] * blastWeight / 3) +
                (playerInfo[PlayerParams.AMMO.ordinal()] * ammoWeight / 3)
                ) / 2;
    }


    private double kickHeuristic(int[] playerInfo) {
        return playerInfo[PlayerParams.KICK.ordinal()] * kickWeight;
    }


    private  double allianceHeuristic(int[] playerInfo) {
        return (
                (playerInfo[PlayerParams.KICK.ordinal()] * kickWeight) +
                (playerInfo[PlayerParams.BLAST.ordinal()] * blastWeight / 3) +
                (playerInfo[PlayerParams.AMMO.ordinal()] * ammoWeight / 3)
                ) / 3;
    }
}
