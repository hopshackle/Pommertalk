package Message;

import players.optimisers.ParameterizedPlayer;
import java.util.*;

public class MessageManager {

    private boolean record;
    private ArrayList<HashMap<String, Integer>> messages = new ArrayList<HashMap<String, Integer>>();
    private ArrayList<HashMap<String, Integer>> currTurnM = new ArrayList<HashMap<String, Integer>>();
    private int ID = 0;
    private int round = 1;


    public MessageManager(ArrayList<ParameterizedPlayer> players, boolean recordMessages) {

        record = recordMessages;

    }


    private HashMap<String, Integer> CreateNewMessage(int sender, int receiver) {

        HashMap<String, Integer> newMessage = new HashMap<String, Integer>();

        newMessage.put("ID", ID);
        ID++;

        newMessage.put("Round", round);
        newMessage.put("Sender", sender);
        newMessage.put("Receiver", receiver);

        return newMessage;

    }


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


    public void SendProposal(int origin, int player, int proposition) {

        HashMap<String, Integer> newProposal = CreateNewMessage(origin, player);

        newProposal.put("Response", 1);
        newProposal.put("Proposal", proposition);

        currTurnM.add(newProposal);

        if (record) { messages.add(newProposal); }
    }


    public void SendResponse(int messageID, int response) {

        HashMap<String, Integer> proposal = GetProposalFromID(messageID);

        HashMap<String, Integer> reply = CreateNewMessage(proposal.get("Receiver"), proposal.get("Sender"));

        reply.put("Response", response);
        reply.put("Proposal", proposal.get("Proposal"));

        currTurnM.add(reply);

        if (record) { messages.add(reply); }
    }


    public void SendMessage(HashMap<String, Integer> mess) { currTurnM.add(mess); }


    public ArrayList<HashMap<String, Integer>> FirstPhaseEnd() {

        ArrayList<HashMap<String, Integer>> proposals =  new ArrayList<HashMap<String, Integer>>();

        for (HashMap<String, Integer> m : currTurnM) {
            if (m.get("Response") == 1) { proposals.add(m); }
        }

        return proposals;
    }


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


    public ArrayList<HashMap<String, Integer>> FindMessages(int sender, int receiver, int roundS, int response, int proposal) {

        ArrayList<HashMap<String, Integer>> foundMessages = new ArrayList<HashMap<String, Integer>>();

        HashMap<String, Integer> rules = new HashMap<String, Integer>();
        rules.put("Sender", sender);
        rules.put("Receiver", receiver);
        rules.put("Round", roundS);
        rules.put("Response", response);
        rules.put("Proposal", proposal);

        for (HashMap<String, Integer> m : messages) {
            boolean match = true;

            for (String i : rules.keySet()) {
                if (rules.get(i) == 0) { continue; }
                if (rules.get(i) != m.get(i)) { match = false; break; }
            }

            if (match) { foundMessages.add(m); }
        }

        return foundMessages;
    }
}
