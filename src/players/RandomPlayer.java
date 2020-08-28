package players;

import Message.MessageManager;
import core.GameState;
import negotiations.Negotiator;
import utils.Types;

import java.util.Random;

public class RandomPlayer extends Player implements Negotiator {
    private Random random;

    public RandomPlayer(long seed, int id) {
        super(seed, id);
        reset(seed, id);
    }

    @Override
    public void reset(long seed, int playerID) {
        super.reset(seed, playerID);
        random = new Random(seed);
    }

    @Override
    public Types.ACTIONS act(GameState gs) {
        int actionIdx = random.nextInt(gs.nActions());
        return Types.ACTIONS.all().get(actionIdx);
    }

    @Override
    public int[] getMessage() {
        // default message
        return new int[Types.MESSAGE_LENGTH];
    }

    @Override
    public Player copy() {
        return new RandomPlayer(seed, playerID);
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

    }
}
