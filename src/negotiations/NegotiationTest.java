package negotiations;


import core.*;
import objects.Avatar;
import org.junit.jupiter.api.Test;
import players.*;
import utils.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class NegotiationTest {

    protected static final int seed = 12345;

    private static final int[][] DEFAULT_BOARD = new int[][]{
            new int[]{0,0,0,0,0,0,0,0,0,0,0},
            new int[]{0,11,0,0,0,0,0,0,0,12,0},
            new int[]{0,0,0,0,0,0,0,0,0,0,0},
            new int[]{0,0,0,0,0,0,0,0,0,0,0},
            new int[]{0,0,0,0,0,0,0,0,0,0,0},
            new int[]{0,0,0,0,10,0,0,0,0,0,0},
            new int[]{0,0,0,0,0,0,0,0,0,0,0},
            new int[]{0,0,0,0,0,0,0,0,0,0,0},
            new int[]{0,0,0,0,0,0,0,0,0,0,0},
            new int[]{0,13,0,0,0,0,0,0,0,0,0},
            new int[]{0,0,0,0,0,0,0,0,0,0,0},
    };

    private static final Types.ACTIONS[] MOVE_CLOSER_ACTIONS = new Types.ACTIONS[]{
            Types.ACTIONS.ACTION_UP,
            Types.ACTIONS.ACTION_UP,
            Types.ACTIONS.ACTION_UP,
            Types.ACTIONS.ACTION_LEFT
    };

    /**
     * Test if agent0 can move closer to agent1
     */
    @Test
    void canMoveCloseWithNoAgreement() {
        Game game = ForwardModelTest.testNFrames(4, DEFAULT_BOARD, MOVE_CLOSER_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(10, game.getGameState().getBoard()[2][3].getKey());
    }

    /**
     * Test if Agreement prevents agent0 from moving closer to agent1
     */
    @Test
    void cannotMoveCloseWithAgreement() {
        ForwardModel model = new ForwardModel(seed, DEFAULT_BOARD, Types.GAME_MODE.FFA);
        Negotiation negotiation = new Negotiation(Arrays.asList(
                new Agreement(Types.TILETYPE.AGENT0, Types.TILETYPE.AGENT1, Agreement.TYPE.STAY_APART),
                new Agreement(Types.TILETYPE.AGENT1, Types.TILETYPE.AGENT2, Agreement.TYPE.STAY_APART)
        ));
        model.injectNegotiation(negotiation);

        Game game = ForwardModelTest.testNFrames(4, MOVE_CLOSER_ACTIONS, new Types.ACTIONS[0], Types.GAME_MODE.FFA, true, model);
        System.out.println(game.getGameState().toString());

        assertEquals(10, game.getGameState().getBoard()[2][4].getKey());
    }


}
