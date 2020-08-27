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
    }
    /*
    Then this method will be called by the core game engine to conduct the negotiations
     */
    public void runNegotiationProcess() {
        // do stuff
        // crucially, make sure this populates the finalAgreement List

        //Call method in each agent to start negotiating
        //Wait X time, call FirstPhaseEnd
        //Call method in each agent to strat responses
        //Wait X time, call SencondPhaseEnd
        //Populate final agreements, with outcome
    }

    public List<Agreement> getFinalAgreements() {
        return List.copyOf(finalAgreements);
    }
}
