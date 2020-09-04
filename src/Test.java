import core.Game;
import negotiations.RandomHeuristicsNegotiator;
import negotiations.RandomNegotiator;
import players.*;
import utils.Types;
import players.rhea.utils.Constants;
import players.mcts.MCTSPlayer;
import players.mcts.MCTSParams;
import players.rhea.RHEAPlayer;
import players.rhea.utils.RHEAParams;


import java.util.ArrayList;

public class Test {

    public static void main(String[] args) {

        //This is a test from me, G!

        // Game parameters
        long seed = System.currentTimeMillis();
        int boardSize = Types.BOARD_SIZE;
        Types.GAME_MODE gameMode = Types.GAME_MODE.FFA;
        boolean useSeparateThreads = false;

        Game game = new Game(seed, boardSize, Types.GAME_MODE.FFA, "");

        // Key controllers for human player s (up to 2 so far).
        KeyController ki1 = new KeyController(true);
        KeyController ki2 = new KeyController(false);

        // Create players
        ArrayList<Player> players = new ArrayList<>();
        int playerID = Types.TILETYPE.AGENT0.getKey();

        // Currently heuristic weights are ENEMY DEATH / ALLY SURVIVAL / WOOD / CANKICK / FORCE_BLAST / ALLY_DISTANCE / AMMO
        double[] speedyPersonality = new double[]{0.4, 0.1, 0.2, 0.3, 0.15, 0.00, 0.0};
        double[] shadowPersonality = new double[]{0.4, 0.2, 0.1, 0.15, 0.15, -0.01, 0.05};
        double[] bashfulPersonality = new double[]{0.3, 0.3, 0.0, 0.0, 0.15, 0.02, 0.1};
        double[] pokeyPersonality = new double[]{0.6, 0.0, 0.1, 0.15, 0.15, 0.01, 0.0};

//        MCTSParams mctsParams = new MCTSParams();
//        mctsParams.stop_type = mctsParams.STOP_ITERATIONS;
//        mctsParams.heuristic_method = mctsParams.CUSTOM_HEURISTIC;
//        mctsParams.heuristicWeights = new double[]{0.4, 0.1, 0.1, 0.15, 0.15, 0.01, 0.0};

        RHEAParams speedy = new RHEAParams();
        speedy.heuristic_type = Constants.CUSTOM_HEURISTIC;
        speedy.heuristic_weights = speedyPersonality;
        RHEAParams shadow = new RHEAParams();
        shadow.heuristic_type = Constants.CUSTOM_HEURISTIC;
        shadow.heuristic_weights = shadowPersonality;
        RHEAParams bashful = new RHEAParams();
        bashful.heuristic_type = Constants.CUSTOM_HEURISTIC;
        bashful.heuristic_weights = bashfulPersonality;
        RHEAParams pokey = new RHEAParams();
        pokey.heuristic_type = Constants.CUSTOM_HEURISTIC;
        pokey.heuristic_weights = pokeyPersonality;

        //players.add(new MCTSPlayer(seed, playerID++, mctsParams));
        players.add(new HumanPlayer(ki1, playerID++));
        //    players.add(new MCTSPlayer(seed, playerID++, mctsParams, new RandomNegotiator(1)));

//        players.add(new SimplePlayer(seed, playerID++));
//        players.add(new RHEAPlayer(seed, playerID++, speedy, new RandomHeuristicsNegotiator(1, 0.2, 0.2, 0.75)));
        players.add(new RHEAPlayer(seed, playerID++, shadow, new RandomHeuristicsNegotiator(2, 0.1, 1.0, 0.5)));
        players.add(new RHEAPlayer(seed, playerID++, bashful, new RandomHeuristicsNegotiator(3, 0.1, 0.6, 0.1)));
        players.add(new RHEAPlayer(seed, playerID++, pokey, new RandomHeuristicsNegotiator(4, 0.2, 0.2, 0.5)));

//        players.add(new SimplePlayer(seed, playerID++));
   //     players.add(new MCTSPlayer(seed, playerID++, new MCTSParams(), new RandomNegotiator(3)));
   //     players.add(new RHEAPlayer(seed, playerID++, rheaParams, new RandomNegotiator(4)));

        // Make sure we have exactly NUM_PLAYERS players
        assert players.size() == Types.NUM_PLAYERS : "There should be " + Types.NUM_PLAYERS +
                " added to the game, but there are " + players.size();

        //Run a single game with the players
        boolean showSidePanels = players.stream().noneMatch(p -> p instanceof HumanPlayer);
        do {
            game.reset(System.currentTimeMillis());
            game.setPlayers(players);
            Run.runGame(game, ki1, ki2, useSeparateThreads, showSidePanels);
        } while (game.runAgain);

        /* Uncomment to run the replay of the previous game: */
//        if (game.isLogged()){
//            Game replay = Game.getLastReplayGame();
//            Run.runGame(replay, ki1, ki2, useSeparateThreads);
//            assert(replay.getGameState().equals(game.getGameState()));
//        }



        /* Run with no visuals, N Times: */
//        int N = 20;
//        Run.runGames(game, new long[]{seed}, N, useSeparateThreads);

    }

}
