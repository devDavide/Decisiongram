package org.pollgram.decision.service;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.widget.Toast;

import org.pollgram.R;
import org.pollgram.decision.data.DBBean;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.ParsedMessage;
import org.pollgram.decision.data.UsersDecisionVotes;
import org.pollgram.decision.data.Vote;
import org.pollgram.decision.ui.VotesManagerFragment;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.URLSpanNoUnderline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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
        String userAsString = ContactsController.formatName(user.first_name, user.last_name);
        // TODO Remove START
//        Toast.makeText(ApplicationLoader.applicationContext, "formatName="+userAsString,Toast.LENGTH_SHORT).show();
//        Toast.makeText(ApplicationLoader.applicationContext, "user.first_name="+user.first_name,Toast.LENGTH_SHORT).show();
//        Toast.makeText(ApplicationLoader.applicationContext, "user.username="+user.username,Toast.LENGTH_SHORT).show();
        // TODO Remove END
        String msg = messageManager.buildRemindMessage(userAsString, decision);
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
        String message = messageManager.buildCloseDecision(decision, winningOption.options, winningOption.voteCount);
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
        pollgramDAO.delete(decision);
        String message = messageManager.buildDeleteDecision(decision);
        sendMessage(decision.getChatId(), message);
    }
    @Override
    public void notifyNewDecision(Decision decision, List<Option> options) {
        Log.d(LOG_TAG, "notifyNewDecision decision[" + decision + "]  options[" + options + "]");
        decision = pollgramDAO.save(decision);
        saveNewOptions(decision, options);
        String message = messageManager.buildNotifyNewDecision(decision, options);
        sendMessage(decision.getChatId(), message);
    }

    @Override
    public void notifyNewOptions(Decision decision, List<Option> newOptions) {
        Log.d(LOG_TAG, "notifyNewOptions decision[" + decision + "] newoptions[" + newOptions + "]");
        saveNewOptions(decision, newOptions);
        String message = messageManager.buildAddOptions(decision, newOptions);
        sendMessage(decision.getChatId(), message);
    }

    @Override
    public void notifyDeleteOptions(Decision decision, List<Option> deleteOptions) {
        Log.d(LOG_TAG, "notifyDeleteOptions decision[" + decision + "] deleteOptions[" + deleteOptions + "]");
        deleteOptions(deleteOptions);
        String message = messageManager.buildDeleteOptions(decision, deleteOptions);
        sendMessage(decision.getChatId(), message);
    }

    private void deleteOptions(List<Option> deleteOptions) {
        for(Option o : deleteOptions)
            pollgramDAO.delete(o);
    }

    private void saveNewOptions(Decision decision, List<Option> newOptions) {
        for(Option o : newOptions) {
            if (o.getDecisionId() == DBBean.ID_NOT_SET) {
                o.setDecisionId(decision.getId());
            } else if (o.getDecisionId() != decision.getId()) {
                Log.e(LOG_TAG, "Option decisionid[" + o.getDecisionId() + "] != decision.getId()[" + decision.getId() + "]");
                continue;
            }
            pollgramDAO.save(o);
        }
    }

    @Override
    public boolean isPollgramMessage(MessageObject message) {
        long groupChatId = messageManager.getMessageGroupId(message);
        if (groupChatId == -1)
            return false;

        String text = message.messageText.toString();
        return messageManager.getMessageType(text) != null;
    }

    @Override
    public Date getMessageDate(MessageObject messageObject) {
        if (messageObject == null)
            return null;
        if (messageObject.messageOwner == null)
            return null;
        return new Date((long) (messageObject.messageOwner.date) * 1000);
    }

    @Override
    public void processMessage(MessageObject message) {
        processMessage(message, true);
    }

    @Override
    public void processMessage(MessageObject message, boolean showToastOnError) {
        Log.d(LOG_TAG, "parsing message [" + message.messageText + "]");
        if (message.messageOwner == null) {
            Log.d(NOT_PARSED_TAG,"message.messageOwner not set");
            return;
        }

        long groupChatId = messageManager.getMessageGroupId(message);
        if (groupChatId == -1){
            Log.d(NOT_PARSED_TAG,"group chat id not found");
            return;
        }

        String text = message.messageText.toString();
        PollgramMessagesManagerImpl.MessageType msgType = messageManager.getMessageType(text);
        if (msgType == null) {
            Log.d(NOT_PARSED_TAG,"unknown MessageType");
            return;
        }

        int messageId = message.messageOwner.id;
        if (pollgramDAO.hasBeenParsed(groupChatId, messageId)) {
            Log.d(NOT_PARSED_TAG,"it has already been parsed message["+messageId+"] group["+groupChatId+"]");
            return;
        }

        int userId = message.messageOwner.from_id;
        Date messageDate = getMessageDate(message);
        boolean parsedSuccessfully = true;

        try {
            switch (msgType) {
                case NEW_DECISION: {
                    PollgramMessagesManager.DecisionOptionData resut = messageManager.getNewDecision(text,
                            groupChatId, userId, messageDate);
                    if (resut == null){
                        throw new PollgramParseException("Decision not found for NEW_DECISION messsage");
                    }
                    if (pollgramDAO.getDecision(resut.decision.getTitle(),resut.decision.getChatId()) != null){
                        Log.d(LOG_TAG,"New decision already found will not insert twice");
                        break;
                    }
                    Decision d = pollgramDAO.save(resut.decision);
                    for (Option o : resut.optionList) {
                        o.setDecisionId(d.getId());
                        pollgramDAO.save(o);
                    }
                    break;
                }
                case ADD_OPTIONS:{
                    PollgramMessagesManager.DecisionOptionData resut = messageManager.getAddedOption(text,
                            groupChatId, userId);
                    if (resut == null){
                        throw new PollgramParseException("Decision not found for "+msgType+" messsage");
                    }
                    for (Option o : resut.optionList) {
                        o.setDecisionId(resut.decision.getId());
                        pollgramDAO.save(o);
                    }
                    break;
                }
                case DELETE_OPTIONS:{
                    PollgramMessagesManager.DecisionOptionData result = messageManager.getDeletedOption(text,
                            groupChatId, userId);
                    if (result == null){
                        throw new PollgramParseException("Decision not found for "+msgType+" messsage");
                    }
                    for (Option o : result.optionList) {
                        Option found = pollgramDAO.getOption(o.getTitle(),result.decision);
                        if (found == null){
                            throw new PollgramParseException("Option ["+o+"] in decision " +
                                    "["+result.decision+"] it could no be deleted");
                        }
                        pollgramDAO.delete(found);
                    }
                    break;
                }

                case REOPEN_DECISION: {
                    Decision decision = messageManager.getReopenDecision(text, groupChatId);
                    decision.setOpen(true);
                    pollgramDAO.save(decision);
                    break;
                }
                case CLOSE_DECISION: {
                    PollgramMessagesManager.ClosedDecisionDate result = messageManager.getCloseDecision(text, groupChatId);
                    result.decision.setOpen(false);
                    pollgramDAO.save(result.decision);
                    break;
                }
                case DELETE_DECISION: {
                    Decision decision = messageManager.getDeleteDecision(text, groupChatId);
                    if (decision != null)
                        pollgramDAO.delete(decision);
                    break;
                }
                case REMIND_TO_VOTE: {
                    break;
                }
                case VOTE: {
                    Collection<Vote> votes = messageManager.getVotes(text, groupChatId, messageDate, userId);
                    for (Vote v : votes)
                        pollgramDAO.save(v);
                    break;
                }
                default: {
                    Log.e(NOT_PARSED_TAG,"unknown message type["+msgType+"]");
                }
            }

        } catch (PollgramParseException e){
            parsedSuccessfully = false;
            if (showToastOnError) {
                Toast.makeText(ApplicationLoader.applicationContext,
                        "Error process message: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            Log.e(LOG_TAG,"Error parsing message ["+text+"]",e);
        }

        pollgramDAO.setMessageAsParsed(groupChatId,messageId, parsedSuccessfully);
    }

    protected void sendMessage(long groupChatId, String message) {
        long peer = -groupChatId;
        MessageObject replyToMsg = null;
        TLRPC.WebPage webPAge = null;
        boolean searchLinks = false;
        boolean asAdmin = false;
        SendMessagesHelper.getInstance().sendMessage(message, peer, replyToMsg, webPAge, searchLinks, asAdmin, null, null);
        Log.i(LOG_TAG, "sent message [" + message + "] in group [" + groupChatId + "]");
    }

    @Override
    public @Nullable TLRPC.User getUser(int userId) {
        TLRPC.User user = MessagesController.getInstance().getUser(userId);
        if (user == null){
            Log.i(LOG_TAG, "Userid [" + userId + "] not found");
            return null;
        }
        if (user.status == null) {// suppose this is a bot
            Log.i(LOG_TAG, "User [" + user + "] is a BOT, it will be skipped");
            return null;
        }
        return user;
    }

    @Override
    public List<TLRPC.User> getUsers(int[] usersIds) {
        List<TLRPC.User> users = new ArrayList<>();
        for (int i = 0; i < usersIds.length; i++) {
            TLRPC.User user = getUser(usersIds[i]);
            if (user != null)
                users.add(user);
        }
        return users;
    }

    @Override
    public String asString(TLRPC.User user){
        if (user.id == UserConfig.getCurrentUser().id)
            return ApplicationLoader.applicationContext.getString(R.string.you);

        if (user.id / 1000 != 777 && user.id / 1000 != 333 &&
                ContactsController.getInstance().contactsDict.get(user.id) == null &&
                (ContactsController.getInstance().contactsDict.size() != 0 || !ContactsController.getInstance().isLoadingContacts())) {
            if (user.phone != null && user.phone.length() != 0) {
                return PhoneFormat.getInstance().format("+" + user.phone);
            } else {
                return UserObject.getUserName(user);
            }
        } else {
            return UserObject.getUserName(user);
        }
    }

    @Override
    public Bundle getBundleForVotesManagerFragment(TLRPC.ChatFull info, MessageObject messageObject, ClickableSpan url) {
        PollgramMessagesManager.MessageType type = messageManager.getMessageType(messageObject.messageText.toString());
        if (type == null) {
            throw new PollgramDAOException("Not a pollgram message");
        }
        long groupChatId = messageManager.getMessageGroupId(messageObject);
        if (groupChatId == -1) {
            throw new PollgramDAOException("Not a group chat message");
        }
        String urlString = ((URLSpanNoUnderline) url).getURL();
        String decisionTitle = messageManager.parseMessageField(urlString);

        Decision d = pollgramDAO.getDecision(decisionTitle, groupChatId);
        if (d == null) {
            throw  new PollgramDAOException(ApplicationLoader.applicationContext.
                    getString(R.string.decisionNotFound, urlString));
        }
        int[] participantsUserIds = new int[info.participants.participants.size()];
        for (int i = 0; i < info.participants.participants.size(); i++) {
            participantsUserIds[i] = info.participants.participants.get(i).user_id;
        }
        Bundle bundle = new Bundle();
        bundle.putLong(VotesManagerFragment.PAR_GROUP_CHAT_ID, groupChatId);
        bundle.putLong(VotesManagerFragment.PAR_DECISION_ID, d.getId());
        bundle.putIntArray(VotesManagerFragment.PAR_PARTICIPANT_IDS, participantsUserIds);
        return  bundle;
    }


    @Override
    public void processMessages(final long dialog_id, List<MessageObject> objects) {
        long groupChatId = messageManager.getMessageGroupId(dialog_id);
        Log.i(LOG_TAG, "Messages not imported yet for group [" + groupChatId + "] importing " + objects.size() + " messages");
        SortedMap<Date, MessageObject> timeOrderedPollgramMessages = new TreeMap<Date, MessageObject>();
        for (MessageObject messageObject : objects) {
            if (isPollgramMessage(messageObject)) {
                Date d = getMessageDate(messageObject);
                timeOrderedPollgramMessages.put(d, messageObject);
            }
        }
        for (Date d : timeOrderedPollgramMessages.keySet()) {
            MessageObject messageObject = timeOrderedPollgramMessages.get(d);
            Log.d("Pollgram", "Parsing message date[" + d + "] message["+messageObject.messageText+"]");
            processMessage(messageObject, false);
        }
    }

    @Override
    public List<MessageObject> getUnParsedMessages(final long dialog_id, Map<Integer, MessageObject> dialogMessagesByIds,
                                                   List<MessageObject> excludeMessages) {
        Set<Integer> excludeMessagesSet = new HashSet<>();
        for (MessageObject mo : excludeMessages) {
            if (mo.messageOwner != null)
                excludeMessagesSet.add(mo.messageOwner.id);
        }

        long groupChatId = messageManager.getMessageGroupId(dialog_id);
        List<ParsedMessage> unparsed = pollgramDAO.getUnparsedMessages(groupChatId);
        List<MessageObject> outList = new ArrayList<>();
        for (ParsedMessage pm : unparsed){
            if (excludeMessagesSet.contains(pm.getMessageId()))
                continue;
            MessageObject msgObj = dialogMessagesByIds.get(pm.getMessageId());
            if (msgObj == null)
                Log.w(LOG_TAG, "Message not found in dialogMessagesByIds map for id ["+pm.getMessageId()+"]");
            else
                outList.add(msgObj);
        }
        return outList;
    }
}
