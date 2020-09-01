package negotiations;

import Message.MessageManager;
import core.GameState;

public interface Negotiator {

    /**
     * Function requests an action from the agent, given current game state observation.
     * @param gs - current game state
     */
    void makeProposals(int playerIndex, GameState gs, MessageManager manager);

    /**
     * Function requests an action from the agent, given current game state observation.
     * @param gs - current game state
     */
    void reviewProposals(int playerIndex, GameState gs, MessageManager manager);
}
