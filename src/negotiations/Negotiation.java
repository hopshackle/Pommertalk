package negotiations;

import core.*;
import java.util.*;

public class Negotiation {

    private ForwardModel forwardModel;
    private List<Agreement> finalAgreements = Collections.emptyList();

    public Negotiation(ForwardModel fm) {
        this.forwardModel = fm;
        // TODO: We could change this to taking in just a list of the currently alive agents?
        // tODO: (Which would be safer and arguably better design)
        // TODO: But by passing in a link to the Forward MOdel you may have more flexibility
        // TODO: Just please don't modify it!!!

        // go and do stuff
    }

    /*
    Once the negotiation has completed, we can call this to get the result of the negotiations.
     */
    public List<Agreement> getFinalAgreements() {
        return finalAgreements;
    }
}
