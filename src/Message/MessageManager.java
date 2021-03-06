package Message;

import negotiations.Agreement;
import java.util.*;

public class MessageManager {

    private boolean record;
    private ArrayList<HashMap<String, Integer>> messages = new ArrayList<HashMap<String, Integer>>();
    private ArrayList<HashMap<String, Integer>> currTurnM = new ArrayList<HashMap<String, Integer>>();
    private int ID = 0;
    private int round = 1;


    public enum Response {
        PROPOSAL,
        ACCEPT,
        DENY;

        /*
        Is this correct to translate to int?
        public int asInt(Response r) {
            if (r == Response.DENY) { return 2; }
            else if (r == Response.ACCEPT) { return 1; }
            else { return 0; }
        }
        */
    }


    public MessageManager(boolean recordMessages) { record = recordMessages; }


    private Agreement.TYPE propTranslator(int proposal) {
        switch(proposal) {
            case 0:
                return Agreement.TYPE.ALLIANCE;
            case 1:
                return Agreement.TYPE.SHARE_VISION;
            case 2:
                return Agreement.TYPE.NO_BOMB_PLACING;
            case 3:
                return Agreement.TYPE.NO_BOMB_KICKING;
            case 4:
                return Agreement.TYPE.STAY_APART;
        }
        return Agreement.TYPE.ALLIANCE;
    }


    //Create a basic message
    //All messages have a sender, a receiver and an ID
    //Round is also saved to help with future search
    private HashMap<String, Integer> CreateNewMessage(int sender, int receiver) {

        HashMap<String, Integer> newMessage = new HashMap<String, Integer>();

        newMessage.put("ID", ID);
        ID++;

        newMessage.put("Round", round);
        newMessage.put("Sender", sender);
        newMessage.put("Receiver", receiver);

        return newMessage;

    }


    //Find a proposal message using specific id
    //Will be used primarily when finding the player to give a response to
    private HashMap<String, Integer> GetProposalFromID(int id) {

        HashMap<String, Integer> mess = new HashMap<String, Integer>();

        for (HashMap<String, Integer> m : currTurnM) {
            if (m.get("ID") == id) {
                mess = m;
                break;
            }
        }

        return mess;
    }

    public void SendProposal(int sender, int receiver, Agreement.TYPE proposal) {
        SendProposal(sender, receiver, proposal.ordinal());
    }

    //Using the sender, receiver and proposal content, create a proposal message
    public void SendProposal(int sender, int receiver, int proposition) {

        //System.out.println(String.format("Player %d send a proposal %d to Player %d", sender, proposition, receiver));

        HashMap<String, Integer> newProposal = CreateNewMessage(sender, receiver);

        newProposal.put("Response", Response.PROPOSAL.ordinal());
        newProposal.put("Proposal", proposition);

        currTurnM.add(newProposal);

        if (record) { messages.add(newProposal); }
    }

    //Using the id of a proposal, create a response message
    public void SendResponse(int messageID, int response) {

        HashMap<String, Integer> proposal = GetProposalFromID(messageID);

        HashMap<String, Integer> reply = CreateNewMessage(proposal.get("Receiver"), proposal.get("Sender"));

        reply.put("Response", response);
        reply.put("Proposal", proposal.get("Proposal"));

        currTurnM.add(reply);

        if (record) { messages.add(reply); }
    }



    //Using the sender, receiver, proposal and response value, create a response message
    public void SendResponse(int sender, int receiver, Agreement.TYPE proposal, int response) {
        SendResponse(sender, receiver, proposal.ordinal(), response);
    }

        //Using the sender, receiver, proposal and response value, create a response message
    public void SendResponse(int sender, int receiver, int proposal, int response) {

        HashMap<String, Integer> mess = CreateNewMessage(sender, receiver);
        mess.put("Response", response);
        mess.put("Proposal", proposal);

        currTurnM.add(mess);

        if (record) { messages.add(mess); }

    }


    //Using an existing message, send it
    //Will only send proposals after the first phase of negotiations
    //Will only send responses after the second phase of negotiations
    public void SendMessage(HashMap<String, Integer> mess) { currTurnM.add(mess); }


    //Translates messages into agreement objects
    public ArrayList<Agreement> messToAgreement(ArrayList<HashMap<String, Integer>> mess) {

        ArrayList<Agreement> agrees = new ArrayList<Agreement>();

        for (HashMap<String, Integer> m : mess) {
            agrees.add(new Agreement( m.get("Sender"), m.get("Receiver"), propTranslator(m.get("Proposal")) ));
        }

        return agrees;
    }


    //Debugging tool used to find messages with specific parameters
    //Will not look for a specific ID
    //Only available if record is set to true
    public ArrayList<HashMap<String, Integer>> FindMessages(int sender, int receiver, int rounds, int response, int proposal) {

        ArrayList<HashMap<String, Integer>> foundMessages = new ArrayList<HashMap<String, Integer>>();

        HashMap<String, Integer> rules = new HashMap<String, Integer>();
        rules.put("Sender", sender);
        rules.put("Receiver", receiver);
        rules.put("Round", rounds);
        rules.put("Response", response);
        rules.put("Proposal", proposal);

        for (HashMap<String, Integer> m : messages) {
            boolean match = true;

            for (String i : rules.keySet()) {
                if (rules.get(i) == -1) { continue; }
                if (rules.get(i) != m.get(i)) { match = false; break; }
            }

            if (match) { foundMessages.add(m); }
        }

        return foundMessages;
    }


    //Return true if a positive response matches a given proposal
    private boolean propRespMatch(HashMap<String, Integer> prop, HashMap<String, Integer> resp) {

        if (prop.get("Response") != Response.PROPOSAL.ordinal()) { return false; }
        if (resp.get("Response") != Response.ACCEPT.ordinal()) { return false; }
        if (prop.get("Sender") != resp.get("Receiver")) { return false; }
        if (prop.get("Receiver") != resp.get("Sender")) { return false; }
        if (prop.get("Round") != resp.get("Round") || prop.get("Round") != round) { return false; }
        if (prop.get("Proposal") != prop.get("Proposal")) { return false; }

        return true;
    }


    //Converts a list of messages into a boolean array
    //The array indicates an agreement between 2 players was encountered
    //Boolean indicates: [player 1][agreement][player 2]
    public boolean[][][] messageToBool(ArrayList<HashMap<String, Integer>> mess) {

        boolean[][][] messBools = {{{false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}},
                {{false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}},
                {{false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}},
                {{false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}}};

        for (HashMap<String, Integer> m : mess) {
            if (m.get("Receiver") == m.get("Sender")) { continue; }
            if (m.get("Receiver") >= m.get("Sender")) {
                messBools[m.get("Sender")][m.get("Proposal")][m.get("Receiver") -1] = true;
            }
            else {
                messBools[m.get("Sender")][m.get("Proposal")][m.get("Receiver")] = true;
            }

        }

        return messBools;
    }


    //Retrieve all proposals and convert them into boolean array
    //Boolean set to true if a proposal was sent
    public boolean[][][] proposalAsBool() {

        ArrayList<HashMap<String, Integer>> proposals = FindMessages(-1, -1, round, Response.PROPOSAL.ordinal(), -1);

        return messageToBool(proposals);
    }


    //Retrieve all positive responses and convert them into a boolean array
    //Boolean set to true if a response was positive
    public boolean[][][] posResponsesToBool() {

        ArrayList<HashMap<String, Integer>> posResponses = FindMessages(-1, -1, round, Response.ACCEPT.ordinal(), -1);

        return messageToBool(posResponses);
    }


    //Retrieve all negative responses and convert them into a boolean array
    //Boolean set to true if a response was negative
    //(included for completion and debug purposes)
    public boolean[][][] negResponsesToBool() {

        ArrayList<HashMap<String, Integer>> negResponses = FindMessages(-1, -1, round, Response.DENY.ordinal(), -1);

        return messageToBool(negResponses);
    }


    //Method which translates proposals to boolean array,
    // to be displayed to users during the second phase of negotiations
    public boolean[][][] receivedPropToBool() {

        ArrayList<HashMap<String, Integer>> proposals = FindMessages(-1, -1, round, Response.PROPOSAL.ordinal(), -1);
        ArrayList<HashMap<String, Integer>> received = new ArrayList<HashMap<String, Integer>>();

        for (HashMap<String, Integer> m : proposals) {
            HashMap<String, Integer> newMess = new HashMap<String, Integer>();
            newMess.put("Receiver", m.get("Sender"));
            newMess.put("Sender", m.get("Receiver"));
            newMess.put("Proposal", m.get("Proposal"));
            received.add(newMess);
        }

        return messageToBool(received);
    }


    //Retrieve all proposals with a positive response and convert them into a boolean array
    //Boolean set to true if proposal was made and a positive response was sent back
    public boolean[][][] agreedPropToBool() {

        ArrayList<HashMap<String, Integer>> proposals = FindMessages(-1, -1, round, Response.PROPOSAL.ordinal(), -1);
        ArrayList<HashMap<String, Integer>> posResponses = FindMessages(-1, -1, round, Response.ACCEPT.ordinal(), -1);

        ArrayList<HashMap<String, Integer>> agreed = new ArrayList<HashMap<String, Integer>>();

        for (HashMap<String, Integer> p : proposals) {
            for (HashMap<String, Integer> r : posResponses) {
                if (propRespMatch(p, r)) { agreed.add(p); agreed.add(r); break; }
            }
        }

        return messageToBool(agreed);
    }


    //Translates a boolean array into proposal messages
    public void boolPropToMessage(boolean[][][] props) {

        //System.out.println("Received proposals from GUI");

        for (int player = 0; player < props.length; player++) {
            for (int proposal = 0; proposal < props[player].length; proposal++) {
                for (int receiver = 0; receiver < props[player][proposal].length; receiver++) {
                    if (props[player][proposal][receiver]){
                        if (receiver >= player) { SendProposal(player, receiver +1, proposal); }
                        else { SendProposal(player, receiver, proposal); }
                    }
                }
            }
        }
    }


    //Translates a boolean array into response messages
    //Values set to true are translated into positive responses
    public void boolRespToMessage(boolean[][][] resps) {

        //System.out.println("Received Responses from GUI");

        for (int player = 0; player < resps.length; player++) {
            for (int proposal = 0; proposal < resps[player].length; proposal++) {
                for (int receiver = 0; receiver < resps[player][proposal].length; receiver++) {
                    if (resps[player][proposal][receiver]){
                        if (receiver >= player) { SendResponse(player, receiver +1, proposal, Response.ACCEPT.ordinal()); }
                        else { SendResponse(player, receiver, proposal, Response.ACCEPT.ordinal()); }
                    }
                }
            }
        }
    }


    //Translates a boolean array into response messages
    //Values set to true are translated into negative responses
    public void boolNegRespToMessage(boolean[][][] resps) {

        for (int player = 0; player < resps.length; player++) {
            for (int proposal = 0; proposal < resps[player].length; proposal++) {
                for (int receiver = 0; receiver < resps[player][proposal].length; receiver++) {
                    if (resps[player][proposal][receiver]){
                        if (receiver >= player) { SendResponse(player, receiver -1, proposal, Response.DENY.ordinal()); }
                        else { SendResponse(player, receiver, proposal, Response.DENY.ordinal()); }
                    }
                }
            }
        }
    }


    //Convert all proposals which were accepted into Agreement class
    //Used to put the rules into effect
    //Only creates one way agreements
    // (if an agreement between 1 and 2 is created, an agreement of the same kind between 2 and 1 will not be created)
    public ArrayList<Agreement> getAgreements() {
        ArrayList<HashMap<String, Integer>> proposals = FindMessages(-1, -1, round, Response.PROPOSAL.ordinal(), -1);
        ArrayList<HashMap<String, Integer>> posResponses = FindMessages(-1, -1, round, Response.ACCEPT.ordinal(), -1);

        /*
        System.out.println();
        System.out.println(String.format("Number of proposals: %d", proposals.size()));
        System.out.println(String.format("Number of responses: %d", posResponses.size()));
        System.out.println();

        for (HashMap<String, Integer> r : posResponses)
            System.out.println(String.format("Player %d -> player %d : Proposal %d : Response %d : Round %d : ID %d",
                    r.get("Sender"), r.get("Receiver"), r.get("Proposal"), r.get("Response"), r.get("Round"), r.get("ID")));
         */

        ArrayList<Agreement> agreed = new ArrayList<Agreement>();

        for (HashMap<String, Integer> p : proposals) {
            //System.out.println(String.format("Player %d -> Player %d: %d", p.get("Sender"), p.get("Receiver"), p.get("Proposal")));
            for (HashMap<String, Integer> r : posResponses) {
                if (propRespMatch(p, r)) {
                    Agreement currAgreement = new Agreement(p.get("Sender"), p.get("Receiver"), propTranslator(p.get("Proposal")));
                    agreed.add(currAgreement);
                    break;
                }
            }
        }

        System.out.println();

        return agreed;
    }


    //Retrieve all all agreements which include a specific player
    public ArrayList<Agreement> getPlayerAgreements(int player) {

        ArrayList<Agreement> agreed = getAgreements();
        ArrayList<Agreement> playerAgreed = new ArrayList<Agreement>();

        for (Agreement a : agreed) {
            if (a.getParticipant1Id() == player || a.getParticipant2Id() == player)
            { playerAgreed.add(a); }
        }

        return playerAgreed;
    }


    //Retrieves all proposals made to a player this round
    //Returns as agreement objects
    public ArrayList<Agreement> getPlayerProposalAgreements(int player) {
        ArrayList<HashMap<String, Integer>> proposed = FindMessages(-1, player, round, Response.PROPOSAL.ordinal(), -1);
        //System.out.println(String.format("There are %d proposals for player %d", proposed.size(), player));
        return messToAgreement(proposed);
    }


    //Called when the first phase of negotiations is over
    //Return all proposal stored in this round up to this point
    public ArrayList<Agreement> FirstPhaseEnd() {

        ArrayList<HashMap<String, Integer>> proposals =  new ArrayList<HashMap<String, Integer>>();

        for (HashMap<String, Integer> m : currTurnM) {
            if (m.get("Response") == Response.PROPOSAL.ordinal()) { proposals.add(m); }
        }

        return messToAgreement(proposals);
    }


    //Called when the second phase of negotiations is over
    //Will return all agreed proposals this round
    //Increments the round number by 1
    //Resets the list of round messages
    public ArrayList<Agreement> SecondPhaseEnd() {

        ArrayList<Agreement> agrees = getAgreements();

        round++;
        currTurnM = new ArrayList<HashMap<String, Integer>>();

        return agrees;
    }
}
