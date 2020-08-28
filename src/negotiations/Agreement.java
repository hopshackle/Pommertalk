package negotiations;

import utils.*;
import java.util.*;

public class Agreement {

    public enum TYPE {
        ALLIANCE,
        SHARE_VISION,
        NO_BOMB_PLACING,
        NO_BOMB_KICKING,
        STAY_APART
    }

    private int participant1, participant2;
    private TYPE agreement;

    public Agreement(Types.TILETYPE agent1, Types.TILETYPE agent2, TYPE agreementType) {
        this.agreement = agreementType;
        this.participant1 = agent1.getKey() - Types.TILETYPE.AGENT0.getKey();
        this.participant2 = agent2.getKey() - Types.TILETYPE.AGENT0.getKey();
    }
    public Agreement(int agent1, int agent2, TYPE agreementType) {
        this.agreement = agreementType;
        this.participant1 = agent1;
        this.participant2 = agent2;
    }

    private Types.TILETYPE tileTypeFromId(int id) {
        switch (id) {
            case 0:
                return Types.TILETYPE.AGENT0;
            case 1:
                return Types.TILETYPE.AGENT1;
            case 2:
                return Types.TILETYPE.AGENT2;
            case 3:
                return Types.TILETYPE.AGENT3;
            default:
                throw new AssertionError("Unknown playerID: " + id);
        }
    }

    public Agreement.TYPE getType() { return agreement;}
    public Types.TILETYPE getParticipant1() {return tileTypeFromId(participant1);}
    public Types.TILETYPE getParticipant2() {return tileTypeFromId(participant2);}
    public int getParticipant1Id() {return participant1;}
    public int getParticipant2Id() {return participant2;}

    @Override
    public String toString() {
        return String.format("Player %d -> Player %d: %s", participant1, participant2, agreement);
    }
}
