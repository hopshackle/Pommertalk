package negotiations;


import core.*;
import objects.Avatar;
import org.junit.jupiter.api.Test;
import utils.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class NegotiationTest {

    protected static final int seed = 12345;

    private static final int[][] DEFAULT_BOARD = new int[][]{
            new int[]{0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 11, 0, 0, 3, 0, 0, 0, 0, 12, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 1, 2, 2, 0, 0},
            new int[]{0, 0, 0, 0, 10, 0, 4, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    };

    private static final Types.ACTIONS[] MOVE_CLOSER_ACTIONS = new Types.ACTIONS[]{
            Types.ACTIONS.ACTION_UP,
            Types.ACTIONS.ACTION_UP,
            Types.ACTIONS.ACTION_UP,
            Types.ACTIONS.ACTION_LEFT
    };

    private static final Types.ACTIONS[] PLACE_BOMB_ACTIONS = new Types.ACTIONS[]{
            Types.ACTIONS.ACTION_UP,
            Types.ACTIONS.ACTION_UP,
            Types.ACTIONS.ACTION_UP,
            Types.ACTIONS.ACTION_LEFT,
            Types.ACTIONS.ACTION_BOMB,
            Types.ACTIONS.ACTION_DOWN
    };

    private static final Types.ACTIONS[] DO_NOTHING = new Types.ACTIONS[]{
            Types.ACTIONS.ACTION_STOP,
            Types.ACTIONS.ACTION_STOP
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
        Negotiation negotiation = Negotiation.createFromAgreements(Arrays.asList(
                new Agreement(Types.TILETYPE.AGENT0, Types.TILETYPE.AGENT1, Agreement.TYPE.STAY_APART),
                new Agreement(Types.TILETYPE.AGENT1, Types.TILETYPE.AGENT2, Agreement.TYPE.STAY_APART)
        ));

        Game game = ForwardModelTest.testNFrames(4, DEFAULT_BOARD, MOVE_CLOSER_ACTIONS, new Types.ACTIONS[0], Types.GAME_MODE.FFA, true, negotiation);
        System.out.println(game.getGameState().toString());

        assertEquals(10, game.getGameState().getBoard()[2][4].getKey());
    }

    @Test
    void canPlaceBombWithoutAgreement() {
        Game game = ForwardModelTest.testNFrames(6, DEFAULT_BOARD, PLACE_BOMB_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(3, game.getGameState().getBoard()[2][3].getKey());
        assertEquals(10, game.getGameState().getBoard()[3][3].getKey());
        Avatar agent0 = (Avatar) game.getAvatars(0)[0];
        assertEquals(0, agent0.getAmmo());
    }

    @Test
    void cannotPlaceBombToContraveneAgreement() {
        Negotiation negotiation = Negotiation.createFromAgreements(Arrays.asList(
                new Agreement(Types.TILETYPE.AGENT0, Types.TILETYPE.AGENT1, Agreement.TYPE.NO_BOMB_PLACING),
                new Agreement(Types.TILETYPE.AGENT1, Types.TILETYPE.AGENT2, Agreement.TYPE.NO_BOMB_PLACING)
        ));
        Game game = ForwardModelTest.testNFrames(6, DEFAULT_BOARD, PLACE_BOMB_ACTIONS, new Types.ACTIONS[0], Types.GAME_MODE.FFA, true, negotiation);
        System.out.println(game.getGameState().toString());

        assertEquals(0, game.getGameState().getBoard()[2][3].getKey());
        assertEquals(10, game.getGameState().getBoard()[3][3].getKey());
        Avatar agent0 = (Avatar) game.getAvatars(0)[0];
        assertEquals(1, agent0.getAmmo());
    }


    private static final Types.ACTIONS[] KICK_BOMB_ACTIONS = new Types.ACTIONS[]{
            Types.ACTIONS.ACTION_UP,
            Types.ACTIONS.ACTION_RIGHT,
            Types.ACTIONS.ACTION_UP
    };
    private static final int[][] KICK_BOARD = new int[][]{
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 8, 3, 0, 0, 0, 12, 0},
            new int[]{0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    };

    @Test
    void canKickBombTowardsNonAlly() {
        // Types.TILETYPE.KICK = 8
        // Types.TILETYPE.BOMB = 3
        Game game = ForwardModelTest.testNFrames(6, KICK_BOARD, KICK_BOMB_ACTIONS, new Types.ACTIONS[0],
                Types.GAME_MODE.FFA, false, Negotiation.createFromAgreements(new ArrayList<>()));
        System.out.println(game.getGameState().toString());

        Avatar agent0 = (Avatar) game.getAvatars(0)[0];
        assertTrue(agent0.canKick());
        assertEquals(0, game.getGameState().getBoard()[4][5].getKey());
        assertEquals(10, game.getGameState().getBoard()[3][5].getKey());
        assertEquals(3, game.getGameState().getBoard()[4][10].getKey());
    }

    @Test
    void cannotKickBombTowardsAlly() {
        Negotiation negotiation = Negotiation.createFromAgreements(Arrays.asList(
                new Agreement(Types.TILETYPE.AGENT0, Types.TILETYPE.AGENT1, Agreement.TYPE.NO_BOMB_PLACING),
                new Agreement(Types.TILETYPE.AGENT1, Types.TILETYPE.AGENT2, Agreement.TYPE.NO_BOMB_PLACING),
                new Agreement(Types.TILETYPE.AGENT2, Types.TILETYPE.AGENT0, Agreement.TYPE.NO_BOMB_KICKING)
        ));
        Game game = ForwardModelTest.testNFrames(6, KICK_BOARD, KICK_BOMB_ACTIONS, new Types.ACTIONS[0],
                Types.GAME_MODE.FFA, false, negotiation);
        System.out.println(game.getGameState().toString());

        Avatar agent0 = (Avatar) game.getAvatars(0)[0];
        assertTrue(agent0.canKick());
        assertEquals(3, game.getGameState().getBoard()[4][5].getKey());
        assertEquals(0, game.getGameState().getBoard()[3][5].getKey());
        assertEquals(10, game.getGameState().getBoard()[3][4].getKey());
    }

    @Test
    void canKickBombAwayFromAlly() {
        assertEquals(KICK_BOARD[4][9], 12);
        KICK_BOARD[4][9] = 0;
        for (int i = 0; i < 11; i++) {
            KICK_BOARD[i][9] = 12;
            Negotiation negotiation = Negotiation.createFromAgreements(Arrays.asList(
                    new Agreement(Types.TILETYPE.AGENT0, Types.TILETYPE.AGENT1, Agreement.TYPE.NO_BOMB_PLACING),
                    new Agreement(Types.TILETYPE.AGENT1, Types.TILETYPE.AGENT2, Agreement.TYPE.NO_BOMB_PLACING),
                    new Agreement(Types.TILETYPE.AGENT2, Types.TILETYPE.AGENT0, Agreement.TYPE.NO_BOMB_KICKING)
            ));

            Game game = ForwardModelTest.testNFrames(6, KICK_BOARD, KICK_BOMB_ACTIONS, new Types.ACTIONS[0],
                    Types.GAME_MODE.FFA, false, negotiation);
            System.out.println(game.getGameState().toString());

            System.out.println(i);
            Avatar agent0 = (Avatar) game.getAvatars(0)[0];
            assertTrue(agent0.canKick());
            if (i >= 2 && i <= 6) {
                assertEquals(3, game.getGameState().getBoard()[4][5].getKey());
                assertEquals(0, game.getGameState().getBoard()[3][5].getKey());
                assertEquals(10, game.getGameState().getBoard()[3][4].getKey());
            } else {
                assertEquals(0, game.getGameState().getBoard()[4][5].getKey());
                assertEquals(10, game.getGameState().getBoard()[3][5].getKey());
                assertEquals(3, game.getGameState().getBoard()[4][10].getKey());
            }
            KICK_BOARD[i][9] = 0;
        }
    }


}
