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

    protected List<Types.TILETYPE> participants = new ArrayList<>();
    protected TYPE agreement;

    public Agreement(Types.TILETYPE agent1, Types.TILETYPE agent2, TYPE agreementType) {
        this.agreement = agreementType;
        participants.add(agent1);
        participants.add(agent2);
    }

}
