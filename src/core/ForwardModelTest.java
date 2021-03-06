package core;

import negotiations.Agreement;
import negotiations.Negotiation;
import objects.Avatar;
import org.junit.jupiter.api.Test;
import players.DoNothingPlayer;
import players.Player;
import players.SimonSaysPlayer;
import utils.Types;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;
import static utils.Types.VERBOSE;

public class ForwardModelTest {

    protected static final int seed = 12345;

    private static final Types.ACTIONS[] KICK_EXPERIMENT_ACTIONS = new Types.ACTIONS[]{
            Types.ACTIONS.ACTION_BOMB,
            Types.ACTIONS.ACTION_UP,
            Types.ACTIONS.ACTION_DOWN,
    };

    private static final Types.ACTIONS[] EXPLOSION_EXPERIMENT_ACTIONS = new Types.ACTIONS[]{
            Types.ACTIONS.ACTION_BOMB,
            Types.ACTIONS.ACTION_UP,
            Types.ACTIONS.ACTION_UP,
            Types.ACTIONS.ACTION_UP,
    };

    private static final Types.ACTIONS[] DOUBLE_BOMB_ACTIONS = new Types.ACTIONS[]{
            Types.ACTIONS.ACTION_BOMB,
            Types.ACTIONS.ACTION_DOWN,
            Types.ACTIONS.ACTION_BOMB,
            Types.ACTIONS.ACTION_DOWN,
            Types.ACTIONS.ACTION_DOWN,
            Types.ACTIONS.ACTION_DOWN,
    };

    private static final Types.ACTIONS[] PICKUP_EXPERIMENT_ACTIONS = new Types.ACTIONS[]{
            Types.ACTIONS.ACTION_DOWN,
            Types.ACTIONS.ACTION_BOMB,
            Types.ACTIONS.ACTION_DOWN,
            Types.ACTIONS.ACTION_LEFT,
    };

    private static final Types.ACTIONS[] RIGHT_ACTIONS = new Types.ACTIONS[]{
            Types.ACTIONS.ACTION_RIGHT,
            Types.ACTIONS.ACTION_RIGHT,
            Types.ACTIONS.ACTION_RIGHT,
    };

    private static final Types.ACTIONS[] LEFT_ACTIONS = new Types.ACTIONS[]{
            Types.ACTIONS.ACTION_LEFT,
            Types.ACTIONS.ACTION_LEFT,
            Types.ACTIONS.ACTION_LEFT,
    };

    private static final Types.ACTIONS[] DOWN_ACTIONS = new Types.ACTIONS[]{
            Types.ACTIONS.ACTION_DOWN,
            Types.ACTIONS.ACTION_DOWN,
            Types.ACTIONS.ACTION_DOWN,
    };

    private static final Types.ACTIONS[] UP_ACTIONS = new Types.ACTIONS[]{
            Types.ACTIONS.ACTION_UP,
            Types.ACTIONS.ACTION_UP,
            Types.ACTIONS.ACTION_UP,
    };

    private static final int[][] DEFAULT_BOARD = new int[][]{
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 12, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    };

    public static Game testNFrames(int n, int[][] intBoard, Types.ACTIONS[] actions, Types.GAME_MODE gameMode) {
        return testNFrames(n, intBoard, actions, gameMode, true);
    }

    public static Game testNFrames(int n, int[][] intBoard, Types.ACTIONS[] actions, Types.GAME_MODE gameMode, boolean canKick) {
        return testNFrames(n, intBoard, actions, new Types.ACTIONS[0], gameMode, canKick);
    }

    public static Game testNFrames(int n, int[][] intBoard, Types.ACTIONS[] actions1, Types.ACTIONS[] actions2, Types.GAME_MODE gameMode, boolean canKick) {
        return testNFrames(n, intBoard, actions1, actions2, gameMode, canKick, null);
    }

    public static Game testNFrames(int n, int[][] intBoard, Types.ACTIONS[] actions1, Types.ACTIONS[] actions2, Types.GAME_MODE gameMode, boolean canKick, Negotiation neg) {
        Queue<Types.ACTIONS> actionsQueue1 = new ArrayDeque<>();
        actionsQueue1.addAll(Arrays.asList(actions1));

        Queue<Types.ACTIONS> actionsQueue2 = new ArrayDeque<>();
        actionsQueue2.addAll(Arrays.asList(actions2));

        ForwardModel model = new ForwardModel(seed, intBoard, gameMode);
        Game game = new Game(seed, model, gameMode);

        ArrayList<Player> players = new ArrayList<>();
        int playerID = Types.TILETYPE.AGENT0.getKey();
        SimonSaysPlayer ssp = new SimonSaysPlayer(playerID++, actionsQueue1);
        players.add(ssp);
        SimonSaysPlayer ssp2 = new SimonSaysPlayer(playerID++, actionsQueue2);
        players.add(ssp2);
        players.add(new DoNothingPlayer(playerID++));
        players.add(new DoNothingPlayer(playerID));

        game.setPlayers(players);
        if (neg != null) model.injectNegotiation(neg); // override the one created otherwise

        if (canKick) {
            Avatar avatar1 = (Avatar) game.getAliveAvatars(-1).get(0);
            avatar1.setCanKick();

            Avatar avatar2 = (Avatar) game.getAliveAvatars(-1).get(1);
            avatar2.setCanKick();
        }

        for (int i = 0; i < n; i++) {
            if (!game.isEnded()) {
                // Loop game while it's not ended.
                game.tick(false);
                if (VERBOSE) {
                    game.printBoard();
                }
            }
        }
        return game;
    }

    /**
     * Test if a player can move up
     */
    @Test
    void movingUpWorks() {
        Game game = testNFrames(4, DEFAULT_BOARD, UP_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(10, game.getGameState().getBoard()[2][4].getKey());
    }

    /**
     * Test if a player can move down
     */
    @Test
    void movingDownWorks() {
        Game game = testNFrames(4, DEFAULT_BOARD, DOWN_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(10, game.getGameState().getBoard()[8][4].getKey());
    }

    /**
     * Test if a player can move left
     */
    @Test
    void movingLeftWorks() {
        Game game = testNFrames(4, DEFAULT_BOARD, LEFT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(10, game.getGameState().getBoard()[5][1].getKey());
    }

    /**
     * Test if a player can move right
     */
    @Test
    void movingRightWorks() {
        Game game = testNFrames(4, DEFAULT_BOARD, RIGHT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(10, game.getGameState().getBoard()[5][7].getKey());
    }

    /**
     * By default a player can lay down a bomb. Test if it is placed, and the player manages to move out of the way.
     */
    @Test
    void plantingBombWorks() {
        Game game = testNFrames(2, DEFAULT_BOARD, EXPLOSION_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(3, game.getGameState().getBoard()[5][4].getKey());
        assertEquals(10, game.getGameState().getBoard()[4][4].getKey()); //Test that the avatar is still there as well
    }

    /**
     * Make sure the bomb does not overwrite the avatar on the board when it is placed
     */
    @Test
    void plantingBombDoesNotOverwritePlayer() {
        Types.ACTIONS[] actions = new Types.ACTIONS[]{
                Types.ACTIONS.ACTION_BOMB,
        };
        Game game = testNFrames(2, DEFAULT_BOARD, actions, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());
        assertEquals(10, game.getGameState().getBoard()[5][4].getKey()); //Test that the avatar is still there as well
    }

    /**
     * By default a player can lay down a bomb, test if it explodes
     */
    @Test
    void bombExplodes() {
        Game game = testNFrames(11, DEFAULT_BOARD, EXPLOSION_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(4, game.getGameState().getBoard()[5][3].getKey());
        assertEquals(4, game.getGameState().getBoard()[5][4].getKey());
        assertEquals(4, game.getGameState().getBoard()[5][5].getKey());
        assertEquals(4, game.getGameState().getBoard()[4][4].getKey());
        assertEquals(4, game.getGameState().getBoard()[6][4].getKey());

        assertEquals(10, game.getGameState().getBoard()[2][4].getKey()); //Test that the avatar is still there as well
    }

    /**
     * Test if a player can move down, down, left
     */
    @Test
    void movingDownLeftWorks() {
        Game game = testNFrames(5, DEFAULT_BOARD, PICKUP_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(10, game.getGameState().getBoard()[7][3].getKey());
    }

    /**
     * Test if a player collides with a wall
     */
    @Test
    void playerWall1CollisionWork() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 1, 0, 10, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(5, intBoard, LEFT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(10, game.getGameState().getBoard()[5][3].getKey());
    }

    /**
     * Test if a player can move down, down, left
     */
    @Test
    void playerWall2CollisionWork() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 2, 0, 10, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(5, intBoard, LEFT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(10, game.getGameState().getBoard()[5][3].getKey());
    }

    /**
     * Test if a player bumping into another player makes them stop
     */
    @Test
    void playerPlayerCollisionWorks() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 11, 0, 10, 0, 0, 0, 0, 0, 0}, // a wall to the right of the player
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(5, intBoard, LEFT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(10, game.getGameState().getBoard()[5][3].getKey());
    }

    /**
     * When two players try to move into the same space, they should bounce back to their previous space
     */
    @Test
    void playerPlayerMovingCollisionWorks() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 11, 0, 10, 0, 0, 0, 0, 0, 0}, // a wall to the right of the player
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(5, intBoard, LEFT_ACTIONS, RIGHT_ACTIONS, Types.GAME_MODE.FFA, false);
        System.out.println(game.getGameState().toString());

        assertEquals(10, game.getGameState().getBoard()[5][4].getKey());
        assertEquals(11, game.getGameState().getBoard()[5][2].getKey());
    }

    /**
     * When one player moves right behind the other, they should not collide. Walking down.
     */
    @Test
    void playerFollowWorks() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 11, 10, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(6, intBoard, RIGHT_ACTIONS, RIGHT_ACTIONS, Types.GAME_MODE.FFA, false);
        System.out.println(game.getGameState().toString());

        assertEquals(11, game.getGameState().getBoard()[5][6].getKey());
        assertEquals(10, game.getGameState().getBoard()[5][7].getKey());
    }

    /**
     * When one player moves right behind the other, they should not collide. Walking up.
     */
    @Test
    void playerFollowWorks2() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 11, 10, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(6, intBoard, LEFT_ACTIONS, LEFT_ACTIONS, Types.GAME_MODE.FFA, false);
        System.out.println(game.getGameState().toString());

        assertEquals(11, game.getGameState().getBoard()[5][3].getKey());
        assertEquals(10, game.getGameState().getBoard()[5][4].getKey());
    }

    /**
     * When one player moves right behind the other, they should not collide.
     * Walking in a circle.
     */
    @Test
    void playerFollowWorks3() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 11, 10, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Types.ACTIONS[] ACTIONS_1 = new Types.ACTIONS[]{
                Types.ACTIONS.ACTION_RIGHT,
                Types.ACTIONS.ACTION_UP,
                Types.ACTIONS.ACTION_LEFT,
                Types.ACTIONS.ACTION_DOWN,
        };

        Types.ACTIONS[] ACTIONS_2 = new Types.ACTIONS[]{
                Types.ACTIONS.ACTION_DOWN,
                Types.ACTIONS.ACTION_RIGHT,
                Types.ACTIONS.ACTION_UP,
                Types.ACTIONS.ACTION_LEFT,
        };

        Game game = testNFrames(6, intBoard, ACTIONS_1, ACTIONS_2, Types.GAME_MODE.FFA, false);
        System.out.println(game.getGameState().toString());

        assertEquals(11, game.getGameState().getBoard()[5][6].getKey());
        assertEquals(10, game.getGameState().getBoard()[5][7].getKey());
    }

    /**
     * This should test that when a bomb is kicked in a certain direction,
     * it can be found one step further in that direction at each next tick.
     */
    @Test
    void kickedBombMoves() {
        Game game = testNFrames(6, DEFAULT_BOARD, KICK_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());
        assertEquals(3, game.getGameState().getBoard()[9][4].getKey());
    }

    /**
     * This tests behaviour for when two bombs are placed next to each other and a player attempts to kick
     * one bomb into another. The bombs should not move in this scenario, and the player should bounce back.
     */
    @Test
    void kickedBombsDoNotMove() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0}, // a wall to the right of the player
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Types.ACTIONS[] KICK_EXPERIMENT_ACTIONS = new Types.ACTIONS[]{
                Types.ACTIONS.ACTION_BOMB,
                Types.ACTIONS.ACTION_UP,
                Types.ACTIONS.ACTION_BOMB,
                Types.ACTIONS.ACTION_UP,
                Types.ACTIONS.ACTION_DOWN,
        };

        Game game = testNFrames(6, intBoard, KICK_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());
        assertEquals(10, game.getGameState().getBoard()[3][4].getKey());
        assertEquals(3, game.getGameState().getBoard()[4][4].getKey());
        assertEquals(3, game.getGameState().getBoard()[5][4].getKey());
    }

    /**
     * This tests behaviour for when a bomb is placed next to a wall and a player attempts to kick it.
     * The bomb should not move in this scenario, and the player should bounce back.
     */
    @Test
    void kickedBombIntoWallDoesNotMove() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0}, // a wall to the right of the player
                new int[]{0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(6, intBoard, KICK_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());
        assertEquals(10, game.getGameState().getBoard()[4][4].getKey());
        assertEquals(3, game.getGameState().getBoard()[5][4].getKey());
        assertEquals(2, game.getGameState().getBoard()[6][4].getKey());
    }

    @Test
    void kickedBombIntoWallDoesNotMove2() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 2, 6, 10, 0, 0, 0, 0, 0, 0}, // a wall to the right of the player
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Types.ACTIONS[] KICK_EXPERIMENT_ACTIONS = new Types.ACTIONS[]{
                Types.ACTIONS.ACTION_BOMB,
                Types.ACTIONS.ACTION_LEFT,
                Types.ACTIONS.ACTION_BOMB,
                Types.ACTIONS.ACTION_RIGHT,
                Types.ACTIONS.ACTION_LEFT,
                Types.ACTIONS.ACTION_DOWN,
                Types.ACTIONS.ACTION_UP
        };

        Game game = testNFrames(9, intBoard, KICK_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());
        assertEquals(10, game.getGameState().getBoard()[5][4].getKey());
        assertEquals(3, game.getGameState().getBoard()[5][3].getKey());
        assertEquals(2, game.getGameState().getBoard()[5][2].getKey());
        assertEquals(3, game.getGameState().getBoard()[5][10].getKey());
    }

    /**
     * This tests that when a bomb is kicked against a wall, it stops at that wall
     */
    @Test
    void kickedBombStopsAtWall() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0}, // a wall to the right of the player
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 2, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(6, intBoard, KICK_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());
        assertEquals(3, game.getGameState().getBoard()[8][4].getKey());
    }

    /**
     * This tests that when a bomb is placed against a wooden wall and explodes, the wall is replaced by flame
     */
    @Test
    void explosionDestroysWall1() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 10, 2, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(10, intBoard, EXPLOSION_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());
        assertNotEquals(2, game.getGameState().getBoard()[5][5]);
    }

    /**
     * This tests that when a bomb is kicked against a wall and explodes, the wall is eliminated after flame subsides.
     * The wall is most likely replaced by a power up.
     */
    @Test
    void explosionDestroysWall2() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 10, 2, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(15, intBoard, EXPLOSION_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertNotEquals(2, game.getGameState().getBoard()[5][5]);
        assertNotEquals(4, game.getGameState().getBoard()[5][5]);
    }

    @Test
    void explosionDestroysPowerUp() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 10, 7, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(15, intBoard, EXPLOSION_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(0, game.getGameState().getBoard()[5][5].getKey());
        assertEquals(0, game.getGameState().getBoard()[4][4].getKey());
        assertEquals(0, game.getGameState().getBoard()[5][4].getKey());
    }

    /**
     * This tests that when a bomb is kicked against a wall and explodes, the wall is eliminated after flame subsides.
     * The wall is most likely replaced by a power up.
     */
    @Test
    void killingEnemiesWinsGame() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 11, 10, 12, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 13, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(15, intBoard, EXPLOSION_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertTrue(game.isEnded());
        assertTrue(game.getGameState().isTerminal());
        assertEquals(Types.RESULT.WIN, ((Avatar) game.getGameState().getAliveAgents().get(0)).getWinner());
    }

    /**
     * This tests that when a bomb is placed next to static walls, the flames are stopped
     */
    @Test
    void explosionStoppedByStaticWall() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 10, 1, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(15, intBoard, EXPLOSION_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(1, game.getGameState().getBoard()[5][5].getKey());
        assertEquals(1, game.getGameState().getBoard()[4][4].getKey());
        assertEquals(1, game.getGameState().getBoard()[6][4].getKey());
        assertEquals(0, game.getGameState().getBoard()[5][4].getKey());
    }

    /**
     * This tests that when a bomb is placed against a wooden wall and explodes,
     * the wall behind the exploded wall is still intact.
     */
    @Test
    void explosionStoppedByDoubleWall() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 7, 2, 2, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(12, intBoard, PICKUP_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(2, game.getGameState().getBoard()[5][6].getKey());
    }

    /**
     * This tests that when a bomb is kicked against a player, it stops at that player
     */
    @Test
    void kickedBombStopsAtPlayer() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 12, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(8, intBoard, KICK_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(3, game.getGameState().getBoard()[8][4].getKey());
    }

    /**
     * This tests that when a bomb is kicked against a bomb, it stops at that bomb
     */
    @Test
    void kickedBombStopsAtBomb() {

        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 3, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(6, intBoard, KICK_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(3, game.getGameState().getBoard()[8][4].getKey());
    }

    /**
     * This tests that if the bomb is kicked,
     * it can not be found in the position where it was kicked from.
     * (It is deleted properly from previous locations in addition to being added to the new location)
     */
    @Test
    void kickBombNoTrail() {

        Game game = testNFrames(9, DEFAULT_BOARD, KICK_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertNotEquals(3, game.getGameState().getBoard()[4][9]);
        assertNotEquals(3, game.getGameState().getBoard()[4][8]);
        assertNotEquals(3, game.getGameState().getBoard()[4][7]);
        assertNotEquals(3, game.getGameState().getBoard()[4][6]);
    }

    /**
     * Without the kick power-up, the player can not kick the bomb.
     * The player bounces back from trying to kick the bomb
     */
    @Test
    void noKickNoKick() {
        Game game = testNFrames(5, DEFAULT_BOARD, KICK_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA, false);
        System.out.println(game.getGameState().toString());

        assertEquals(3, game.getGameState().getBoard()[5][4].getKey());
        assertEquals(10, game.getGameState().getBoard()[4][4].getKey());
    }

    /**
     * A player should be able to kick back a kicked bomb directly, on the first touch.
     */
    @Test
    void volleyBall() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };
        Types.ACTIONS[] VOLLEY_1 = new Types.ACTIONS[]{
                Types.ACTIONS.ACTION_BOMB,
                Types.ACTIONS.ACTION_UP,
                Types.ACTIONS.ACTION_DOWN,
        };

        Types.ACTIONS[] VOLLEY_2 = new Types.ACTIONS[]{
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_UP,
                Types.ACTIONS.ACTION_DOWN,
        };

        Game game = testNFrames(6, intBoard, VOLLEY_1, VOLLEY_2, Types.GAME_MODE.FFA, true);
        System.out.println(game.getGameState().toString());

        assertEquals(3, game.getGameState().getBoard()[6][3].getKey());
        assertEquals(11, game.getGameState().getBoard()[8][3].getKey());
        assertEquals(10, game.getGameState().getBoard()[5][3].getKey());
    }

    /**
     * A player should be able to dodge a moving bomb
     * by moving out of the tile the same frame the bomb moves into the tile,
     * and also move onto the tile as the bomb is leaving without causing a kick.
     */
    @Test
    void dodgeBall() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };
        Types.ACTIONS[] VOLLEY_1 = new Types.ACTIONS[]{
                Types.ACTIONS.ACTION_BOMB,
                Types.ACTIONS.ACTION_UP,
                Types.ACTIONS.ACTION_DOWN,
        };

        Types.ACTIONS[] VOLLEY_2 = new Types.ACTIONS[]{
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_LEFT,
                Types.ACTIONS.ACTION_RIGHT,
        };

        Game game = testNFrames(9, intBoard, VOLLEY_1, VOLLEY_2, Types.GAME_MODE.FFA, true);
        System.out.println(game.getGameState().toString());

        assertEquals(3, game.getGameState().getBoard()[10][3].getKey());
        assertEquals(11, game.getGameState().getBoard()[8][3].getKey());
        assertEquals(10, game.getGameState().getBoard()[5][3].getKey());
    }

    /**
     * A player should be able to kick a bomb that is already in motion from being kicked,
     * in a fashion perpendicular to its original velocity.
     * The bombs movement should then as a result completely change direction.
     * If the bomb moves diagonally instead of perpendicularly to the original velocity,
     * something is wrong.
     */
    @Test
    void sidewaysVolley() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };
        Types.ACTIONS[] VOLLEY_1 = new Types.ACTIONS[]{
                Types.ACTIONS.ACTION_BOMB,
                Types.ACTIONS.ACTION_UP,
                Types.ACTIONS.ACTION_DOWN,
        };

        Types.ACTIONS[] VOLLEY_2 = new Types.ACTIONS[]{
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_LEFT,
                Types.ACTIONS.ACTION_RIGHT,
        };

        Game game = testNFrames(6, intBoard, VOLLEY_1, VOLLEY_2, Types.GAME_MODE.FFA, true);
        System.out.println(game.getGameState().toString());

        assertEquals(10, game.getGameState().getBoard()[5][3].getKey());
        assertEquals(11, game.getGameState().getBoard()[8][3].getKey());
        assertEquals(3, game.getGameState().getBoard()[8][5].getKey());
    }

    /**
     * A player should be able to kick a bomb that is already in motion from being kicked,
     * in a fashion perpendicular to its original velocity.
     * The bombs movement should then as a result completely change direction.
     * If the bomb moves diagonally instead of perpendicularly to the original velocity,
     * something is wrong.
     * <p>
     * This test rotates the other sidewaysVolley test 90 degrees, to check that it works on both axes.
     */
    @Test
    void sidewaysVolley2() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 10, 0, 0, 11, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };
        Types.ACTIONS[] VOLLEY_1 = new Types.ACTIONS[]{
                Types.ACTIONS.ACTION_BOMB,
                Types.ACTIONS.ACTION_LEFT,
                Types.ACTIONS.ACTION_RIGHT,
        };

        Types.ACTIONS[] VOLLEY_2 = new Types.ACTIONS[]{
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_DOWN,
                Types.ACTIONS.ACTION_UP,
        };

        Game game = testNFrames(6, intBoard, VOLLEY_1, VOLLEY_2, Types.GAME_MODE.FFA, true);
        System.out.println(game.getGameState().toString());

        assertEquals(11, game.getGameState().getBoard()[5][6].getKey());
        assertEquals(10, game.getGameState().getBoard()[5][3].getKey());
        assertEquals(3, game.getGameState().getBoard()[3][6].getKey());
    }

    /**
     * A player should be able to kick a bomb that is already in motion from being kicked,
     * in a fashion perpendicular to its original velocity.
     * The bombs movement should then as a result completely change direction.
     * If the bomb moves diagonally instead of perpendicularly to the original velocity,
     * something is wrong.
     * <p>
     * This test mirrors sidwaysVolley2
     */
    @Test
    void sidewaysVolley3() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 10, 0, 0, 11, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };
        Types.ACTIONS[] VOLLEY_1 = new Types.ACTIONS[]{
                Types.ACTIONS.ACTION_BOMB,
                Types.ACTIONS.ACTION_LEFT,
                Types.ACTIONS.ACTION_RIGHT,
        };

        Types.ACTIONS[] VOLLEY_2 = new Types.ACTIONS[]{
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_UP,
                Types.ACTIONS.ACTION_DOWN,
        };

        Game game = testNFrames(6, intBoard, VOLLEY_1, VOLLEY_2, Types.GAME_MODE.FFA, true);
        System.out.println(game.getGameState().toString());

        assertEquals(11, game.getGameState().getBoard()[5][6].getKey());
        assertEquals(10, game.getGameState().getBoard()[5][3].getKey());
        assertEquals(3, game.getGameState().getBoard()[7][6].getKey());
    }

    /**
     * A player should be able to kick a bomb that is already in motion from being kicked,
     * in a fashion perpendicular to its original velocity.
     * The bombs movement should then as a result completely change direction.
     * If the bomb moves diagonally instead of perpendicularly to the original velocity,
     * something is wrong.
     */
    @Test
    void sidewaysVolley4() {
        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };
        Types.ACTIONS[] VOLLEY_1 = new Types.ACTIONS[]{
                Types.ACTIONS.ACTION_BOMB,
                Types.ACTIONS.ACTION_UP,
                Types.ACTIONS.ACTION_DOWN,
        };

        Types.ACTIONS[] VOLLEY_2 = new Types.ACTIONS[]{
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_STOP,
                Types.ACTIONS.ACTION_RIGHT,
                Types.ACTIONS.ACTION_LEFT,
        };

        Game game = testNFrames(6, intBoard, VOLLEY_1, VOLLEY_2, Types.GAME_MODE.FFA, true);
        System.out.println(game.getGameState().toString());

        assertEquals(10, game.getGameState().getBoard()[5][3].getKey());
        assertEquals(11, game.getGameState().getBoard()[8][3].getKey());
        assertEquals(3, game.getGameState().getBoard()[8][1].getKey());
    }

    /**
     * When the player has no ammo, they can not lay down the bomb
     */
    @Test
    void noAmmoNoBomb() {
        Game game = testNFrames(5, DEFAULT_BOARD, DOUBLE_BOMB_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(3, game.getGameState().getBoard()[5][4].getKey());
        assertNotEquals(3, game.getGameState().getBoard()[5][5].getKey());
    }


    /**
     * When the player has no ammo, they can not lay down the bomb
     */
    @Test
    void extraAmmoPowerupWorks() {

        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(5, intBoard, DOUBLE_BOMB_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(3, game.getGameState().getBoard()[5][4].getKey());
        assertEquals(3, game.getGameState().getBoard()[6][4].getKey());
    }

    /**
     * When the player has no ammo, they can not lay down the bomb
     */
    @Test
    void chainReactionsWork() {

        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(11, intBoard, DOUBLE_BOMB_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(4, game.getGameState().getBoard()[5][4].getKey());
        assertEquals(4, game.getGameState().getBoard()[5][5].getKey());
        assertEquals(4, game.getGameState().getBoard()[6][5].getKey());
        assertEquals(4, game.getGameState().getBoard()[4][4].getKey());
        assertEquals(4, game.getGameState().getBoard()[7][4].getKey());
    }

    /**
     * When the player picks up a blast range boost, their bombs blast range is larger
     */
    @Test
    void blastRangePowerupWorks() {

        int[][] intBoard = new int[][]{
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 0, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 2, 0, 7, 0, 2, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        Game game = testNFrames(12, intBoard, PICKUP_EXPERIMENT_ACTIONS, Types.GAME_MODE.FFA);
        System.out.println(game.getGameState().toString());

        assertEquals(4, game.getGameState().getBoard()[4][4].getKey());
        assertEquals(4, game.getGameState().getBoard()[8][4].getKey());
        assertEquals(4, game.getGameState().getBoard()[6][2].getKey());
        assertEquals(4, game.getGameState().getBoard()[6][6].getKey());
    }

    @Test
    void copyingForwardModelKeepsOnlyRelevantAgreements() {
        ForwardModel model = new ForwardModel(seed, DEFAULT_BOARD, Types.GAME_MODE.FFA);
        Agreement first = new Agreement(Types.TILETYPE.AGENT0, Types.TILETYPE.AGENT1, Agreement.TYPE.STAY_APART);
        Agreement second = new Agreement(Types.TILETYPE.AGENT1, Types.TILETYPE.AGENT2, Agreement.TYPE.STAY_APART);
        Negotiation negotiation = Negotiation.createFromAgreements(Arrays.asList(first, second));
        model.injectNegotiation(negotiation);

        ForwardModel perspective0 = model.copy(0);
        assertEquals(1, perspective0.negotiation.getFinalAgreements().size());
        assertEquals(first, perspective0.negotiation.getFinalAgreements().get(0));

        ForwardModel perspective1 = model.copy(1);
        assertEquals(2, perspective1.negotiation.getFinalAgreements().size());
        assertTrue(perspective1.negotiation.getFinalAgreements().contains(first));
        assertTrue(perspective1.negotiation.getFinalAgreements().contains(second));

        ForwardModel perspective2 = model.copy(2);
        assertEquals(1, perspective2.negotiation.getFinalAgreements().size());
        assertEquals(second, perspective2.negotiation.getFinalAgreements().get(0));

        ForwardModel perspective3 = model.copy(3);
        assertEquals(0, perspective3.negotiation.getFinalAgreements().size());
    }


    @Test
    void forwardModelCopyRemovesItemsOutOfRange() {
        assertEquals(3, Types.DEFAULT_VISION_RANGE, "Need to set Types.DEFAULT_VISION_RANGE to override default");

        int[][] DEFAULT_BOARD = new int[][]{
                new int[]{13, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 3, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 1, 2, 2, 0, 0},
                new int[]{0, 0, 0, 0, 10, 0, 4, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        ForwardModel model = new ForwardModel(seed, DEFAULT_BOARD, Types.GAME_MODE.FFA);
        assertEquals(2, model.bombs.size());
        assertEquals(3, model.flames.size());

        ForwardModel perspective0 = model.copy(0);
        assertEquals(5, perspective0.getBoard()[1][1].getKey());  // 11 in full map
        assertEquals(5, perspective0.getBoard()[1][9].getKey());  // 12 in full map
        assertEquals(5, perspective0.getBoard()[0][0].getKey());  // 13 in full map
        assertEquals(10, perspective0.getBoard()[5][4].getKey());
        assertEquals(4, perspective0.getBoard()[5][6].getKey());
        assertEquals(5, perspective0.getBoard()[0][2].getKey());  // 4 in full map
        assertEquals(3, perspective0.getBoard()[3][6].getKey());
        assertEquals(5, perspective0.getBoard()[1][4].getKey());  // 3 in full map
        assertEquals(2, perspective0.getBoard()[4][7].getKey());
        assertEquals(5, perspective0.getBoard()[4][8].getKey());  // 2 in full map
        assertEquals(1, perspective0.bombs.size());
        assertEquals(1, perspective0.flames.size());

        ForwardModel perspective1 = model.copy(1);
        assertEquals(11, perspective1.getBoard()[1][1].getKey());  // 11 in full map
        assertEquals(5, perspective1.getBoard()[1][9].getKey());  // 12 in full map
        assertEquals(13, perspective1.getBoard()[0][0].getKey());  // 13 in full map
        assertEquals(5, perspective1.getBoard()[5][4].getKey()); // 10 in full map
        assertEquals(5, perspective1.getBoard()[5][6].getKey()); // 4 in full map
        assertEquals(4, perspective1.getBoard()[0][2].getKey());  // 4 in full map
        assertEquals(5, perspective1.getBoard()[3][6].getKey()); // 3 in full map
        assertEquals(3, perspective1.getBoard()[1][4].getKey());  // 3 in full map
        assertEquals(5, perspective1.getBoard()[4][7].getKey()); // 2 in full map
        assertEquals(5, perspective1.getBoard()[4][8].getKey());  // 2 in full map
        assertEquals(1, perspective1.bombs.size());
        assertEquals(1, perspective1.flames.size());
    }

    @Test
    void forwardModelCopyRemovesItemsOutOfRangeOfFoci() {

        int[][] DEFAULT_BOARD = new int[][]{
                new int[]{13, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 11, 0, 0, 3, 0, 0, 0, 0, 12, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 1, 2, 2, 0, 0},
                new int[]{0, 0, 0, 0, 10, 0, 4, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        assertEquals(3, Types.DEFAULT_VISION_RANGE, "Need to set Types.DEFAULT_VISION_RANGE to override default");
        ForwardModel model = new ForwardModel(seed, DEFAULT_BOARD, Types.GAME_MODE.FFA);
        Agreement first = new Agreement(Types.TILETYPE.AGENT0, Types.TILETYPE.AGENT1, Agreement.TYPE.SHARE_VISION);
        Agreement second = new Agreement(Types.TILETYPE.AGENT1, Types.TILETYPE.AGENT2, Agreement.TYPE.STAY_APART);
        Negotiation negotiation = Negotiation.createFromAgreements(Arrays.asList(first, second));
        model.injectNegotiation(negotiation);
        assertEquals(2, model.bombs.size());
        assertEquals(3, model.flames.size());

        ForwardModel perspective0 = model.copy(0);
        assertEquals(11, perspective0.getBoard()[1][1].getKey());  // 11 in full map
        assertEquals(5, perspective0.getBoard()[1][9].getKey());  // 12 in full map
        assertEquals(13, perspective0.getBoard()[0][0].getKey());  // 13 in full map
        assertEquals(10, perspective0.getBoard()[5][4].getKey());
        assertEquals(4, perspective0.getBoard()[5][6].getKey());
        assertEquals(4, perspective0.getBoard()[0][2].getKey());  // 4 in full map
        assertEquals(3, perspective0.getBoard()[3][6].getKey());
        assertEquals(3, perspective0.getBoard()[1][4].getKey());  // 3 in full map
        assertEquals(2, perspective0.getBoard()[4][7].getKey());
        assertEquals(5, perspective0.getBoard()[4][8].getKey());  // 2 in full map
        assertEquals(2, perspective0.bombs.size());
        assertEquals(2, perspective0.flames.size());

        ForwardModel perspective1 = model.copy(1);
        assertEquals(11, perspective1.getBoard()[1][1].getKey());  // 11 in full map
        assertEquals(5, perspective1.getBoard()[1][9].getKey());  // 12 in full map
        assertEquals(13, perspective1.getBoard()[0][0].getKey());  // 13 in full map
        assertEquals(10, perspective1.getBoard()[5][4].getKey()); // 10 in full map
        assertEquals(4, perspective1.getBoard()[5][6].getKey()); // 4 in full map
        assertEquals(4, perspective1.getBoard()[0][2].getKey());  // 4 in full map
        assertEquals(3, perspective1.getBoard()[3][6].getKey()); // 3 in full map
        assertEquals(3, perspective1.getBoard()[1][4].getKey());  // 3 in full map
        assertEquals(2, perspective1.getBoard()[4][7].getKey()); // 2 in full map
        assertEquals(5, perspective1.getBoard()[4][8].getKey());  // 2 in full map
        assertEquals(2, perspective1.bombs.size());
        assertEquals(2, perspective1.flames.size());
    }

}