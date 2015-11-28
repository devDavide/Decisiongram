package org.pollgram.decision.service;

import android.util.Log;
import android.widget.Toast;

import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.UsersDecisionVotes;
import org.pollgram.decision.data.Vote;
import org.pollgram.decision.utils.PollgramUtils;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by davide on 10/11/15.
 */
public class PollgramServiceImpl implements PollgramService {

    private static final String LOG_TAG = "POLLGSRV";
    private static final String NOT_PARSED_TAG = "NOT_PARSED";

    private final PollgramDAO pollgramDAO;
    private final PollgramMessagesManager messageManager;

    public PollgramServiceImpl() {
        this.pollgramDAO = PollgramFactory.getPollgramDAO();
        this.messageManager = PollgramFactory.getPollgramMessagesManager();
    }

    PollgramServiceImpl(PollgramDAO pollgramDAO, PollgramMessagesManager messageManager) {
        this.pollgramDAO = pollgramDAO;
        this.messageManager = messageManager;
    }

    @Override
    public List<TLRPC.User> getUsers(int[] usersIds) {
        List<TLRPC.User> users = new ArrayList<>();
        for (int i = 0; i < usersIds.length; i++) {
            TLRPC.User user = MessagesController.getInstance().getUser(usersIds[i]);
            if (user.status == null) {// suppose this is a bot
                Log.i(LOG_TAG, "User [" + user + "] is a BOT, it will be skipped");
                continue;
            }
            users.add(user);
        }
        return users;
    }

    @Override
    public UsersDecisionVotes getUsersDecisionVotes(long decisionId, int[] participantIds) {
        List<TLRPC.User> users = getUsers(participantIds);
        return getUsersDecisionVotes(decisionId, users);

    }

    @Override
    public UsersDecisionVotes getUsersDecisionVotes(long decisionId, List<TLRPC.User> users) {
        Decision decision = pollgramDAO.getDecision(decisionId);
        List<Option> options = pollgramDAO.getOptions(decisionId);
        List<Vote> votes = pollgramDAO.getVotes(decisionId, null);
        UsersDecisionVotes udv = new UsersDecisionVotes(decision, users, options, votes);
        return udv;
    }

    private List<Object> asList(Object... objs){
        List<Object> l = new ArrayList<>(objs.length);
        for(int i=0;i<objs.length ; i++){
            l.add(objs[i]);
        }
        return l;
    }

    @Override
    public void remindUserToVote(Decision decision, TLRPC.User user) {
        Log.d(LOG_TAG, "remindUserToVote groupChatId[" + decision.getChatId() + "] decision[" + decision + "] user[" + user + "]");
        String msg = messageManager.buildRemindMessage(PollgramUtils.asString(user), decision);
        sendMessage(decision.getChatId(), msg);
    }

    @Override
    public void notifyVote(Decision decision, Collection<Vote> votes2Save) {
        Log.d(LOG_TAG, "notifyVote groupChatId[" + decision.getChatId() + "] decision[" + decision + "] votes2Save[" + votes2Save + "]");

        // Save vote on the db first
        for(Vote v : votes2Save){
            pollgramDAO.save(v);
        }
        String message = messageManager.buildNotifyVoteMessage(decision, votes2Save);
        sendMessage(decision.getChatId(), message);
    }

    @Override
    public void notifyClose(Decision decision) {
        decision.setOpen(false);
        decision = pollgramDAO.save(decision);
        PollgramDAO.WinningOption winningOption = pollgramDAO.getWinningOption(decision);
        String message = messageManager.buildCloseDecision(decision, winningOption.option, winningOption.voteCount);
        sendMessage(decision.getChatId(), message);
    }

    @Override
    public void notifyReopen(Decision decision) {
        decision.setOpen(true);
        decision = pollgramDAO.save(decision);
        String message = messageManager.buildReopenDecision(decision);
        sendMessage(decision.getChatId(), message);
    }

    @Override
    public void notifyDelete(Decision decision) {
        // TODO
    }

    @Override
    public void notifyNewDecision(Decision decision, List<Option> options) {
        Log.d(LOG_TAG, "notifyNewDecision decision[" + decision + "] decision[" + decision + "] options[" + options + "]");
        pollgramDAO.save(decision);
        for(Option o : options){
            if (o.getDecisionId() != decision.getId()) {
                Log.e(LOG_TAG, "Option decisionid["+o.getDecisionId()+"] != decision.getId()["+decision.getId()+"]");
                continue;
            }
            pollgramDAO.save(o);
        }

        String message = messageManager.buildNotifyNewDecision(decision,options);
        sendMessage(decision.getChatId(), message);
    }

    @Override
    public void processMessage(MessageObject message, TLRPC.Chat currentChat) {
        Log.d(LOG_TAG,"parsing message ["+message.messageText+"] for chat ["+currentChat+"]");
        if (currentChat == null) {
            Log.d(NOT_PARSED_TAG,"not a group message");
            return;
        }

        if (ChatObject.isChannel(currentChat)){
            Log.d(NOT_PARSED_TAG,"is a channel");
            return;
        }

        String text = message.messageText.toString();
        PollgramMessagesManagerImpl.MessageType msgType = messageManager.getMessageType(text);
        if (msgType == null) {
            Log.d(NOT_PARSED_TAG,"unknown MessageType");
            return;
        }

        int userId = message.messageOwner.from_id;
        Date messageDate = new Date((long)(message.messageOwner.date)*1000);

        try {
            switch (msgType) {
                case NEW_DECISION: {
                    PollgramMessagesManager.NewDecisionData resut = messageManager.getNewDecision(text, currentChat, userId);
                    if (resut == null){
                        throw new PollgramParseException("Decision not found for NEW_DECISION messsage");
                    }
                    Decision d = pollgramDAO.save(resut.decision);
                    for (Option o : resut.optionList) {
                        o.setDecisionId(d.getId());
                        pollgramDAO.save(o);
                    }
                    break;
                }
                case REOPEN_DECISION: {
                    break;
                }
                case CLOSE_DECISION: {
                    PollgramMessagesManager.ClosedDecisionDate result = messageManager.getCloseDecision(text, currentChat);
                    result.decision.setOpen(false);
                    pollgramDAO.save(result.decision);
                    break;
                }
                case DELETE_DECISION: {
                    break;
                }
                case REMIND_TO_VOTE: {
                    break;
                }
                case VOTE: {
                    Collection<Vote> votes = messageManager.getVotes(text, currentChat, messageDate, userId);
                    for (Vote v : votes)
                        pollgramDAO.save(v);
                    break;
                }
            }
        } catch (PollgramParseException e){
            Toast.makeText(ApplicationLoader.applicationContext,
                    "Error process message: "+ e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG,"Error parsing message ["+text+"]",e);
        }
    }

    protected void sendMessage(long groupChatId, String message) {
        long peer = -groupChatId;
        MessageObject replyToMsg = null;
        TLRPC.WebPage webPAge = null;
        boolean searchLinks = false;
        boolean asAdmin = false;
        SendMessagesHelper.getInstance().sendMessage(message, peer, replyToMsg, webPAge, searchLinks, asAdmin);
        Log.i(LOG_TAG, "sended message [" + message + "] in group [" + groupChatId + "]");
    }
}
