package Message;

import players.optimisers.ParameterizedPlayer;
import java.util.ArrayList;
import java.util.HashMap;

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

}
