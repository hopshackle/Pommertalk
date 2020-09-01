package players.heuristics;

import core.GameState;
import negotiations.Agreement;
import objects.Avatar;
import utils.Types;

import java.util.Set;
import java.util.stream.Collectors;

public class CustomHeuristic extends StateHeuristic {
    private BoardStats rootBoardStats;
    private double FACTOR_ENEMY;
    private double FACTOR_TEAM;
    private double FACTOR_WOODS;
    private double FACTOR_CANKICK;
    private double FACTOR_BLAST;
    private double FACTOR_ALLY_DISTANCE;
    private double FACTOR_AMMO;

    public CustomHeuristic(GameState root) {
        this(root, 0.5, 0.0, 0.1, 0.15, 0.15, 0.0, 0.0);
    }

    public CustomHeuristic(GameState root, double... weights) {
        rootBoardStats = new BoardStats(root);
        FACTOR_ENEMY = weights.length > 0 ? weights[0] : 0.5;
        FACTOR_TEAM = weights.length > 1 ? weights[1] : 0.0;
        FACTOR_WOODS = weights.length > 2 ? weights[2] : 0.1;
        FACTOR_CANKICK = weights.length > 3 ? weights[3] : 0.15;
        FACTOR_BLAST = weights.length > 4 ? weights[4] : 0.15;
        FACTOR_ALLY_DISTANCE = weights.length > 5 ? weights[5] : 0.0;
        FACTOR_AMMO = weights.length > 6 ? weights[6] : 0.0;
    }

    @Override
    public double evaluateState(GameState gs) {
        boolean gameOver = gs.isTerminal();
        Types.RESULT win = gs.winner();

        // Compute a score relative to the root's state.
        BoardStats lastBoardState = new BoardStats(gs);

        double rawScore = rootBoardStats.score(lastBoardState);

        if (gameOver && win == Types.RESULT.LOSS)
            rawScore = -1;

        if (gameOver && win == Types.RESULT.WIN)
            rawScore = 1;

        return rawScore;
    }

    class BoardStats {
        int tick, nTeammates, nEnemies, blastStrength;
        boolean canKick;
        int distanceToAllies;
        int nWoods;
        double maxBlastStrength = 10;
        int ammo = 0;

        BoardStats(GameState gs) {
            nEnemies = gs.getAliveEnemyIDs().size();

            // Init weights based on game mode
            if (gs.getGameMode() == Types.GAME_MODE.FFA) {
                Set<Types.TILETYPE> allies = gs.getAgreements().stream()
                        .filter(a -> a.getType() == Agreement.TYPE.ALLIANCE &&
                                (a.getParticipant1Id() == gs.getPlayerId() || a.getParticipant2Id() == gs.getPlayerId()))
                        .mapToInt(a -> a.getParticipant1Id() == gs.getPlayerId() ? a.getParticipant2Id() : a.getParticipant1Id())
                        .mapToObj(id -> Types.TILETYPE.values()[id])
                        .collect(Collectors.toSet());

                nTeammates = 0;
                for (Types.TILETYPE ally : allies) {
                    if (gs.getAliveEnemyIDs().contains(ally)) {
                        nTeammates++;
                        int allyIndex = ally.getKey() - Types.TILETYPE.AGENT0.getKey();
                        Avatar allyDetails = gs.getAgent(allyIndex);
                        distanceToAllies += allyDetails.getPosition().manhattanDistance(gs.getPosition());
                    }
                }
            } else {
                nTeammates = gs.getAliveTeammateIDs().size();  // We only need to know the alive teammates in team modes
                nEnemies -= 1;  // In team modes there's an extra Dummy agent added that we don't need to care about
            }

            // Save game state information
            this.tick = gs.getTick();
            this.blastStrength = gs.getBlastStrength();
            this.canKick = gs.canKick();
            this.ammo = gs.getAmmo();

            // Count the number of wood walls
            this.nWoods = 1;
            for (Types.TILETYPE[] gameObjectsTypes : gs.getBoard()) {
                for (Types.TILETYPE gameObjectType : gameObjectsTypes) {
                    if (gameObjectType == Types.TILETYPE.WOOD)
                        nWoods++;
                }
            }
        }

        /**
         * Computes score for a game, in relation to the initial state at the root.
         * Minimizes number of opponents in the game and number of wood walls. Maximizes blast strength and
         * number of teammates, wants to kick.
         *
         * @param futureState the stats of the board at the end of the rollout.
         * @return a score [0, 1]
         */
        double score(BoardStats futureState) {
            int diffAllyDistance = futureState.distanceToAllies - this.distanceToAllies;
            int diffTeammates = futureState.nTeammates - this.nTeammates;
            int diffEnemies = -(futureState.nEnemies - this.nEnemies);
            int diffWoods = -(futureState.nWoods - this.nWoods);
            double maxWoods = Math.max(this.nWoods, futureState.nWoods);
            int diffCanKick = futureState.canKick ? 1 : 0;
            int diffBlastStrength = futureState.blastStrength - this.blastStrength;
            int diffAmmo = futureState.ammo - this.ammo;

            return (diffEnemies / 3.0) * FACTOR_ENEMY + diffTeammates * FACTOR_TEAM + (diffWoods / maxWoods) * FACTOR_WOODS
                    + diffCanKick * FACTOR_CANKICK + (diffBlastStrength / maxBlastStrength) * FACTOR_BLAST
                    + diffAllyDistance * FACTOR_ALLY_DISTANCE + diffAmmo * FACTOR_AMMO;
        }
    }
}
