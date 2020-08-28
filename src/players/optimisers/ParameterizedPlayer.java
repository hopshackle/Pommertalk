package players.optimisers;

import Message.MessageManager;
import core.GameState;
import negotiations.Negotiator;
import negotiations.RandomNegotiator;
import players.Player;
import players.optimisers.ParameterSet;

public abstract class ParameterizedPlayer extends Player implements Negotiator {

    private ParameterSet params;
    private Negotiator negotiator = null;

    /**
     * Default constructor, to be called in subclasses (initializes player ID and random seed for this agent.
     *
     * @param seed - random seed for this player.
     * @param pId  - this player's ID.
     */
    protected ParameterizedPlayer(long seed, int pId) {
        super(seed, pId);
    }

    protected ParameterizedPlayer(long seed, int pId, ParameterSet params, Negotiator negotiator) {
        super(seed, pId);
        this.params = params;
        this.negotiator = negotiator;
    }

    public final void setParameters(ParameterSet params) {
        this.params = params;
    }

    public final ParameterSet getParameters() {
        return params;
    }

    public void translateParameters(int[] a, boolean topLevel) {
        params.translate(a, topLevel);
    }

    public final void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public void makeProposals(int playerIndex, GameState gs, MessageManager manager) {
        if (negotiator != null)
            negotiator.makeProposals(playerIndex, gs, manager);
    }

    @Override
    public void reviewProposals(int playerIndex, GameState gs, MessageManager manager) {
        if (negotiator != null)
            negotiator.reviewProposals(playerIndex, gs, manager);
    }
}
