package Message;

import players.optimisers.ParameterizedPlayer;
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
        DENY
    }


    public MessageManager(boolean recordMessages) { record = recordMessages; }


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


    //Using the sender, receiver and proposal content, create a proposal message
    public void SendProposal(int origin, int player, int proposition) {

        HashMap<String, Integer> newProposal = CreateNewMessage(origin, player);

        newProposal.put("Response", 1);
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


    //Using an existing message, send it
    //Will only send proposals after the first phase of negotiations
    //Will only send responses after the second phase of negotiations
    public void SendMessage(HashMap<String, Integer> mess) { currTurnM.add(mess); }


    //Called when the first phase of negotiations is over
    //Return all proposal stored in this round up to this point
    public ArrayList<HashMap<String, Integer>> FirstPhaseEnd() {

        ArrayList<HashMap<String, Integer>> proposals =  new ArrayList<HashMap<String, Integer>>();

        for (HashMap<String, Integer> m : currTurnM) {
            if (m.get("Response") == 1) { proposals.add(m); }
        }

        return proposals;
    }


    //Called when the second phase of negotiations is over
    //Will return all responses stored this round
    //Increments the round number by 1
    //Resets the list of round messages
    public ArrayList<HashMap<String, Integer>> SecondPhaseEnd() {

        ArrayList<HashMap<String, Integer>> responses =  new ArrayList<HashMap<String, Integer>>();

        for (HashMap<String, Integer> m : currTurnM) {
            if (m.get("Response") == 2 || m.get("Responses") == 3)
            { responses.add(m); }
        }

        round++;
        currTurnM = new ArrayList<HashMap<String, Integer>>();

        return responses;
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


    //TODO: Add comments to GUI interface
    public boolean[][][] messageToBool(ArrayList<HashMap<String, Integer>> mess) {

        boolean[][][] messBools = {{{false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}},
                {{false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}},
                {{false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}},
                {{false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}}};

        for (HashMap<String, Integer> m : mess) {
            messBools[m.get("Sender")][m.get("Receiver")][m.get("Proposal")] = true;
        }

        return messBools;
    }


    public boolean[][][] proposalAsBool() {

        ArrayList<HashMap<String, Integer>> proposals = FindMessages(-1, -1, round, 1, -1);

        return messageToBool(proposals);
    }


    public boolean[][][] posResponsesToBool() {

        ArrayList<HashMap<String, Integer>> posResponses = FindMessages(-1, -1, round, 2, -1);

        return messageToBool(posResponses);
    }


    public boolean[][][] negResponsesToBool() {

        ArrayList<HashMap<String, Integer>> negResponses = FindMessages(-1, -1, round, 3, -1);

        return messageToBool(negResponses);
    }


    private boolean propRespMatch(HashMap<String, Integer> prop, HashMap<String, Integer> resp) {

        if (prop.get("Response") != 1) { return false; }
        if (resp.get("Response") != 2) { return false; }
        if (prop.get("Sender") != resp.get("Receiver")) { return false; }
        if (prop.get("Receiver") != resp.get("Sender")) { return false; }
        if (prop.get("Round") != resp.get("Round")) { return false; }
        if (prop.get("Proposal") != prop.get("Proposal")) { return false; }

        return true;
    }


    public boolean[][][] agreedPropToBool() {

        ArrayList<HashMap<String, Integer>> proposals = FindMessages(-1, -1, round, 1, -1);
        ArrayList<HashMap<String, Integer>> posResponses = FindMessages(-1, -1, round, 2, -1);

        ArrayList<HashMap<String, Integer>> agreed = new ArrayList<HashMap<String, Integer>>();

        for (HashMap<String, Integer> p : proposals) {
            for (HashMap<String, Integer> r : posResponses) {
                if (propRespMatch(p, r)) { agreed.add(r); break; }
            }
        }

        return messageToBool(agreed);
    }


    //TODO: Agreements interface method
    //TODO: Let players interrogate for proposals and responses
}
