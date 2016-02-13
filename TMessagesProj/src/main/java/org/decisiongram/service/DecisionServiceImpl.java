package org.decisiongram.service;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.widget.Toast;

import org.decisiongram.R;
import org.decisiongram.data.DBBean;
import org.decisiongram.data.Decision;
import org.decisiongram.data.Option;
import org.decisiongram.data.ParsedMessage;
import org.decisiongram.data.TextOption;
import org.decisiongram.data.UsersDecisionVotes;
import org.decisiongram.data.Vote;
import org.decisiongram.ui.DecisionsListFragment;
import org.decisiongram.ui.NewDecisionFragment;
import org.decisiongram.ui.SelectDecisionFragment;
import org.decisiongram.ui.VotesManagerFragment;
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by davide on 10/11/15.
 */
public class DecisionServiceImpl implements DecisionService {

    private static final String LOG_TAG = "POLLGSRV";
    private static final String NOT_PARSED_TAG = "NOT_PARSED";

    private final DecisionDAO decisiongramDAO;
    private final MessagesManager messageManager;

    public DecisionServiceImpl() {
        this.decisiongramDAO = DecisiongramFactory.getDAO();
        this.messageManager = DecisiongramFactory.getMessagesManager();
    }

    DecisionServiceImpl(DecisionDAO decisiongramDAO, MessagesManager messageManager) {
        this.decisiongramDAO = decisiongramDAO;
        this.messageManager = messageManager;
    }


    @Override
    public UsersDecisionVotes getUsersDecisionVotes(long decisionId, int[] participantIds) {
        List<TLRPC.User> users = getUsers(participantIds);
        return getUsersDecisionVotes(decisionId, users);

    }

    @Override
    public UsersDecisionVotes getUsersDecisionVotes(long decisionId, List<TLRPC.User> users) {
        Decision decision = decisiongramDAO.getDecision(decisionId);
        List<Option> options = decisiongramDAO.getOptions(decisionId);
        List<Vote> votes = decisiongramDAO.getVotes(decisionId, null);
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
            decisiongramDAO.save(v);
        }
        String message = messageManager.buildNotifyVoteMessage(decision, votes2Save);
        sendMessage(decision.getChatId(), message);
    }


    @Override
    public void notifyClose(Decision decision) {
        decision.setOpen(false);
        decision = decisiongramDAO.save(decision);
        DecisionDAO.WinningOption winningOption = decisiongramDAO.getWinningOption(decision);
        String message = messageManager.buildCloseDecision(decision, winningOption.options, winningOption.voteCount);
        sendMessage(decision.getChatId(), message);
    }

    @Override
    public void notifyReopen(Decision decision) {
        decision.setOpen(true);
        decision = decisiongramDAO.save(decision);
        String message = messageManager.buildReopenDecision(decision);
        sendMessage(decision.getChatId(), message);
    }

    @Override
    public void notifyDelete(Decision decision) {
        decisiongramDAO.delete(decision);
        String message = messageManager.buildDeleteDecision(decision);
        sendMessage(decision.getChatId(), message);
    }
    @Override
    public void notifyNewDecision(Decision decision, List<Option> options) {
        Log.d(LOG_TAG, "notifyNewDecision decision[" + decision + "]  options[" + options + "]");
        decision = decisiongramDAO.save(decision);
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
    public void notifyOptionUpdateLongDescription(TextOption option) {
        Log.d(LOG_TAG, "notifyOptionUpdateLongDescription option[" + option + "]");
        Decision d = decisiongramDAO.getDecision(option.getDecisionId());
        if (d == null)
            throw new DecisionDAOException("Decision not found for id ["+option.getDecisionId()+"]");

        decisiongramDAO.save(option);
        String message = messageManager.buildUpdateOptionNotes(d, option);
        sendMessage(d.getChatId(), message);

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
            decisiongramDAO.delete(o);
    }

    private void saveNewOptions(Decision decision, List<Option> newOptions) {
        for(Option o : newOptions) {
            if (o.getDecisionId() == DBBean.ID_NOT_SET) {
                o.setDecisionId(decision.getId());
            } else if (o.getDecisionId() != decision.getId()) {
                Log.e(LOG_TAG, "Option decisionid[" + o.getDecisionId() + "] != decision.getId()[" + decision.getId() + "]");
                continue;
            }
            decisiongramDAO.save(o);
        }
    }

    @Override
    public boolean isDecisiongramMessage(MessageObject message) {
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
        MessagesManagerImpl.MessageType msgType = messageManager.getMessageType(text);
        if (msgType == null) {
            Log.d(NOT_PARSED_TAG,"unknown MessageType");
            return;
        }

        int messageId = message.messageOwner.id;
        if (decisiongramDAO.hasBeenParsed(groupChatId, messageId)) {
            Log.d(NOT_PARSED_TAG,"it has already been parsed message["+messageId+"] group["+groupChatId+"]");
            return;
        }

        int userId = message.messageOwner.from_id;
        Date messageDate = getMessageDate(message);
        boolean parsedSuccessfully = true;

        try {
            switch (msgType) {
                case NEW_DECISION: {
                    MessagesManager.DecisionOptionData result = messageManager.getNewDecision(text,
                            groupChatId, userId, messageDate);
                    if (result == null){
                        throw new MessageParseException("Decision not found for NEW_DECISION messsage");
                    }
                    if (decisiongramDAO.getDecision(result.decision.getTitle(),result.decision.getChatId()) != null){
                        Log.d(LOG_TAG,"New decision already found will not insert twice");
                        break;
                    }
                    Decision d = decisiongramDAO.save(result.decision);
                    for (Option o : result.optionList) {
                        o.setDecisionId(d.getId());
                        decisiongramDAO.save(o);
                    }
                    break;
                }
                case ADD_OPTIONS:{
                    MessagesManager.DecisionOptionData result = messageManager.getAddedOption(text,
                            groupChatId, userId);
                    if (result == null){
                        throw new MessageParseException("Decision not found for "+msgType+" messsage");
                    }
                    for (Option o : result.optionList) {
                        o.setDecisionId(result.decision.getId());
                        decisiongramDAO.save(o);
                    }
                    break;
                }
                case DELETE_OPTIONS:{
                    MessagesManager.DecisionOptionData result = messageManager.getDeletedOption(text,
                            groupChatId, userId);
                    if (result == null){
                        throw new MessageParseException("Decision not found for "+msgType+" messsage");
                    }
                    for (Option o : result.optionList) {
                        Option found = decisiongramDAO.getOption(o.getTitle(),result.decision);
                        if (found == null){
                            throw new MessageParseException("Option ["+o+"] in decision " +
                                    "["+result.decision+"] it could no be deleted");
                        }
                        decisiongramDAO.delete(found);
                    }
                    break;
                }

                case UPDATE_OPTION_NOTES:{
                    TextOption option = messageManager.getNewOptionData(text, groupChatId, userId);
                    decisiongramDAO.save(option);
                    break;
                }

                case CLOSE_DECISION: {
                    MessagesManager.ClosedDecisionDate result = messageManager.getCloseDecision(text,
                            groupChatId, userId);
                    result.decision.setOpen(false);
                    decisiongramDAO.save(result.decision);
                    break;
                }

                case REOPEN_DECISION: {
                    Decision decision = messageManager.getReopenDecision(text, groupChatId, userId);
                    decision.setOpen(true);
                    decisiongramDAO.save(decision);
                    break;
                }

                case DELETE_DECISION: {
                    Decision decision = messageManager.getDeleteDecision(text, groupChatId, userId);
                    if (decision != null)
                        decisiongramDAO.delete(decision);
                    break;
                }
                case REMIND_TO_VOTE: {
                    break;
                }
                case VOTE: {
                    Collection<Vote> votes = messageManager.getVotes(text, groupChatId, messageDate, userId);
                    for (Vote v : votes)
                        decisiongramDAO.save(v);
                    break;
                }
                default: {
                    Log.e(NOT_PARSED_TAG,"unknown message type["+msgType+"]");
                }
            }

        } catch (MessageParseException e){
            parsedSuccessfully = false;
            boolean isCurrentUser = userId != UserConfig.getClientUserId();
            if (showToastOnError && isCurrentUser) {
                Toast.makeText(ApplicationLoader.applicationContext,
                        "Error process message: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            Log.e(LOG_TAG,"Error parsing message ["+text+"] isCurrentUser["+isCurrentUser+"]",e);
        }

        decisiongramDAO.setMessageAsParsed(groupChatId, messageId, parsedSuccessfully);
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
        return asString(user, true);
    }

    private String asString(TLRPC.User user, boolean overrideYou) {
        if (user == null)
            return null;
        if (overrideYou && user.id == UserConfig.getCurrentUser().id)
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
        MessagesManager.MessageType type = messageManager.getMessageType(messageObject.messageText.toString());
        if (type == null) {
            throw new DecisionDAOException("Not a decisiongram message");
        }
        long groupChatId = messageManager.getMessageGroupId(messageObject);
        if (groupChatId == -1) {
            throw new DecisionDAOException("Not a group chat message");
        }
        String urlString = ((URLSpanNoUnderline) url).getURL();
        String decisionTitle = messageManager.parseMessageField(urlString);

        Decision d = decisiongramDAO.getDecision(decisionTitle, groupChatId);
        if (d == null) {
            throw  new DecisionDAOException(ApplicationLoader.applicationContext.
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
    public Bundle getBundleForDecisionList(TLRPC.ChatFull chatInfo) {

        List<Integer> ids = new ArrayList<>(chatInfo.participants.participants.size());
        for (int i = 0; i < chatInfo.participants.participants.size() ; i++){
            TLRPC.User user = DecisiongramFactory.getService().getUser(chatInfo.participants.participants.get(i).user_id);
            if (user != null)
                ids.add(user.id);
        }
        int[] participantsUserIds = new int[ids.size()];
        for (int i=0;i<ids.size();i++)
            participantsUserIds[i] = ids.get(i);
        Bundle args = new Bundle();
        args.putInt(DecisionsListFragment.PAR_GROUP_CHAT_ID, chatInfo.id);
        args.putIntArray(DecisionsListFragment.PAR_PARTICIPANT_IDS,participantsUserIds);
        return args;
    }

    @Override
    public Bundle getBundleForNewDecision(TLRPC.Chat currentChat, MessageObject selectedObject) {
        Bundle args = new Bundle();
        args.putInt(NewDecisionFragment.PAR_GROUP_CHAT_ID, currentChat.id);
        args.putString(NewDecisionFragment.PAR_DECISION_LONG_DESCRIPTION,
                getLongDescription(selectedObject).toString());
        return args;
    }

    @Override
    public Bundle getBundleForNewOption(TLRPC.Chat currentChat, MessageObject selectedObject) {
        Bundle args = new Bundle();
        args.putInt(SelectDecisionFragment.PAR_GROUP_CHAT_ID, currentChat.id);
        args.putString(SelectDecisionFragment.PAR_NEW_OPTION_LONG_DESCRIPTION,
                getLongDescription(selectedObject).toString());
        return args;
    }

    @NonNull
    private StringBuilder getLongDescription(MessageObject selectedObject) {
        Context context = ApplicationLoader.applicationContext;

        TLRPC.User user = getUser(selectedObject.messageOwner.from_id);
        String dateAsString = DateFormat.getDateInstance(DateFormat.SHORT).
                format(getMessageDate(selectedObject));

        StringBuilder longDescription = new StringBuilder();
        if (user != null) {
            longDescription.append(context.getString(R.string.newDecisionFromMessageHeader, dateAsString,
                    asString(user, false)));
            longDescription.append('\n');
        }
        longDescription.append(selectedObject.messageText.toString());
        return longDescription;
    }


    /**
     * Internal class used for sorting messages
     */
    private class TimeMessageKey implements  Comparable<TimeMessageKey> {
        final MessageObject messageObject;
        final Date messageDate;
        public TimeMessageKey(MessageObject messageObject) {
            this.messageObject = messageObject;
            this.messageDate =getMessageDate(messageObject);
        }

        @Override
        public boolean equals(Object o) {
            return messageObject.equals(o);
        }

        @Override
        public int hashCode() {
            return messageObject.hashCode();
        }

        @Override
        public int compareTo(TimeMessageKey another) {
            int cmp = messageDate.compareTo(another.messageDate);
            if (cmp != 0)
                return cmp;
            else{
                int id1 = messageObject.getId();
                int id2 = another.messageObject.getId();
                return id1 < id2 ? -1 : (id1 == id2 ? 0 : 1);
            }
        }
    }

    @Override
    public void processMessages(final long dialog_id, List<MessageObject> objects) {
        long groupChatId = messageManager.getMessageGroupId(dialog_id);
        Log.i(LOG_TAG, "Messages not imported yet for group [" + groupChatId + "] importing " + objects.size() + " messages");
        SortedSet<TimeMessageKey> timeOrderedDecisiongramMessages = new TreeSet<TimeMessageKey>();
        for (MessageObject messageObject : objects) {
            if (isDecisiongramMessage(messageObject)) {
                timeOrderedDecisiongramMessages.add(new TimeMessageKey(messageObject));
            }
        }
        for (TimeMessageKey tmk : timeOrderedDecisiongramMessages) {
            Log.d("Decisiongram", "Parsing message date[" + tmk.messageDate + "] message["+tmk.messageObject.messageText+"]");
            processMessage(tmk.messageObject, false);
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
        List<ParsedMessage> unparsed = decisiongramDAO.getUnparsedMessages(groupChatId);
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
