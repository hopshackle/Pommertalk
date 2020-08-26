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

    private Types.TILETYPE participant1, participant2;
    private TYPE agreement;

    public Agreement(Types.TILETYPE agent1, Types.TILETYPE agent2, TYPE agreementType) {
        this.agreement = agreementType;
        this.participant1 = agent1;
        this.participant2 = agent2;
    }

    public Agreement.TYPE getType() { return agreement;}
    public Types.TILETYPE getParticipant1() {return participant1;}
    public Types.TILETYPE getParticipant2() {return participant2;}
    public int getParticipant1Id() {return participant1.getKey() - Types.TILETYPE.AGENT0.getKey();}
    public int getParticipant2Id() {return participant2.getKey() - Types.TILETYPE.AGENT0.getKey();}


}
