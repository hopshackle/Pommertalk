package negotiations;

import Message.MessageManager;
import core.GameState;

public interface Negotiator {

    /**
     * Function requests an action from the agent, given current game state observation.
     * @param gs - current game state
     * @return - an array of Agreements that the player would like to propose
     */
    void makeProposals(GameState gs, MessageManager manager);

    /**
     * Function requests an action from the agent, given current game state observation.
     * @param gs - current game state
     * @return - true|false depending on whether the agent agrees or not
     */
    void reviewProposals(GameState gs, MessageManager manager);
}
