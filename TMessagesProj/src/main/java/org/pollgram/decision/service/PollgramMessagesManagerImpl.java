package org.pollgram.decision.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.util.Log;

import org.pollgram.R;
import org.pollgram.decision.data.DBBean;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.TextOption;
import org.pollgram.decision.data.Vote;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.MessageObject;
import org.telegram.ui.Components.URLSpanNoUnderline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Created by davide on 18/11/15.
 */
class PollgramMessagesManagerImpl implements PollgramMessagesManager {
    private static final String LOG_TAG = "MessageTransactions";

    private static final String POLLGRAM_MESSAGE_PREFIX = "#Pollgram ";
    protected static final char QUOTE_CHAR = '\'';
    protected static final char ESCAPE_QUOTE_CHAR = '´';
    private static final char NEW_LINE = '\n';

    private static final String WINKING_FACE_EMOJI = EmojiUtils.getEmojiAsString((byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte)0x89);// winking face
    private static final String TRUE_EMOJI = EmojiUtils.getEmojiAsString((byte) 0xE2, (byte) 0x9C, (byte) 0x85);// WHITE HEAVY CHECK MARK
    private static final String FALSE_EMOJI = EmojiUtils.getEmojiAsString((byte) 0xE2, (byte) 0x9D, (byte) 0x8C);// CROSS MARK
    private static final String BULLET_LIST_EMOJI = EmojiUtils.getEmojiAsString((byte) 0xE2, (byte) 0x96, (byte) 0xAB);// white small square

    private final PollgramDAO pollgramDAO;
    private final Context context;

    PollgramMessagesManagerImpl(){
        this(PollgramFactory.getDAO());
    }

    String getTailingString(){
        return new StringBuilder().append(NEW_LINE).
                append(NEW_LINE).
                append(getResString(R.string.downloadPollgramFromMarket)).
                append(' ').
                append(context.getString(R.string.appDownloadURL)).
                toString();
    }

    PollgramMessagesManagerImpl(PollgramDAO pollgramDAO){
        this.pollgramDAO = pollgramDAO;
        context = ApplicationLoader.applicationContext;
    }

    /**
     * Local version of StringTokenizer that manage default sep char and escape
     */
    private class EscapeStringTokenizer extends  StringTokenizer{

        public EscapeStringTokenizer(String string) {
            super(string, Character.toString(QUOTE_CHAR));
        }

        public EscapeStringTokenizer(String string, boolean considerNewLine) {
            super(string, considerNewLine ? Character.toString(QUOTE_CHAR)+ Character.toString(NEW_LINE) : Character.toString(QUOTE_CHAR) );
        }

        @Override
        public String nextToken() {
            String tok = super.nextToken();
            return parseMessageField(tok);
        }
    }

    @Override
    public String parseMessageField(String str) {
        str = str.replace(
                Character.toString(PollgramMessagesManagerImpl.QUOTE_CHAR), "");
        return str.replace(Character.toString(ESCAPE_QUOTE_CHAR), Character.toString(QUOTE_CHAR));
    }

    private String format(Object obj){
        if (obj instanceof Boolean)
            return getBooleanValue((Boolean) obj);

        String strValue;
        if (obj instanceof Decision) {
            strValue = ((Decision) obj).getTitle();
        } else if (obj instanceof  Option){
            strValue = ((Option)obj).getTitle();
        } else
            strValue = "" + obj;

        strValue = strValue.replace(Character.toString(QUOTE_CHAR), Character.toString(ESCAPE_QUOTE_CHAR));

        if (strValue.length() == 0)
            strValue = " ";

        strValue = QUOTE_CHAR + strValue + QUOTE_CHAR;
        return strValue;
    }

    private String getResString(int resId){
        String s = context.getString(resId);
        if (s == null)
            return  null;
        return s.replace(Character.toString(QUOTE_CHAR), Character.toString(ESCAPE_QUOTE_CHAR));
    }

    private String getBooleanValue(Boolean b) {
        StringBuilder sb = new StringBuilder();
        sb.append(b ? TRUE_EMOJI : FALSE_EMOJI);
        sb.append(" ");
        sb.append(getResString(b ? R.string.yes : R.string.no));
        return sb.toString();
    }

    private Boolean getBooleanValue(String str) {
        return str != null && str.startsWith(TRUE_EMOJI);
    }

    @Override
    public String buildRemindMessage(String userAsString, Decision decision) {
        StringBuilder body = new StringBuilder();
        body.append(getResString(R.string.tmsg_RemindToVoteP1));
        body.append(' ');
        body.append(userAsString);
        body.append(' ');
        body.append(getResString(R.string.tmsg_RemindToVoteP2));
        body.append(NEW_LINE);
        body.append(format(decision));
        body.append(NEW_LINE);
        body.append(getResString(R.string.tmsg_RemindToVoteP3));
        body.append(' ');
        body.append(WINKING_FACE_EMOJI);
        return  buildMessage(MessageType.REMIND_TO_VOTE, body.toString());
    }

    @Override
    public String buildCloseDecision(Decision decision, List<Option> winningOptions, int voteCount) {
        StringBuilder body = new StringBuilder();
        body.append(getResString(R.string.tmsg_CloseDecisionP1));
        body.append(' ');
        body.append(format(decision));
        body.append(getResString(R.string.tmsg_CloseDecisionP2));
        body.append(' ');
        body.append(getResString(winningOptions.size() == 1 ? R.string.tmsg_CloseDecisionP2Single : R.string.tmsg_CloseDecisionP2Multi));
        body.append(' ');
        if (voteCount == 0)
            body.append(format(getResString(R.string.tmsg_CloseDecisionNoOptionDesc)));
        else {
            for (Option o : winningOptions) {
                body.append(NEW_LINE);
                body.append(BULLET_LIST_EMOJI);
                body.append(format(o));
            }
        }
        body.append(NEW_LINE);
        body.append(getResString(R.string.tmsg_CloseDecisionP3));
        body.append(' ');
        body.append(voteCount);
        body.append(' ');
        body.append(getResString(R.string.tmsg_CloseDecisionP4));

        return  buildMessage(MessageType.CLOSE_DECISION, body.toString());
    }

    @Override
    public String buildReopenDecision(Decision decision) {
        return buildDeleteOrReopenMessage(MessageType.REOPEN_DECISION, decision,
                R.string.tmsg_ReopenDecisionPrefix, R.string.tmsg_ReopenDecisionSuffix);
    }

    @Override
    public String buildDeleteDecision(Decision decision) {
        return buildDeleteOrReopenMessage(MessageType.DELETE_DECISION, decision,
                R.string.tmsg_DeleteDecisionPrefix, R.string.tmsg_DeleteDecisionSuffix);
    }

    private String buildDeleteOrReopenMessage(MessageType type, Decision decision, int prefixStringRes, int suffixStringRes){
        StringBuilder body = new StringBuilder();
        body.append(getResString(prefixStringRes));
        body.append(' ');
        body.append(format(decision));
        body.append(NEW_LINE);
        body.append(getResString(suffixStringRes));
        return buildMessage(type, body.toString());
    }

    @Override
    public String buildNotifyVoteMessage(Decision decision, Collection<Vote> votes2Save) {
        StringBuilder body = new StringBuilder();
        body.append(getResString(R.string.tmsg_Vote));
        body.append(NEW_LINE);
        body.append(format(decision));
        body.append(NEW_LINE);
        Iterator<Vote> it = votes2Save.iterator();
        while (it.hasNext()) {
            Vote v = it.next();
            body.append(format(v.isVote()));
            body.append(' ');
            Option option = pollgramDAO.getOption(v.getOptionId());
            if (option == null)
                throw new PollgramDAOException("option not found for id [" + v.getOptionId() + "]");
            body.append(format(option));
            if (it.hasNext())
                body.append(NEW_LINE);
        }
        return buildMessage(MessageType.VOTE, body.toString());
    }

    @Override
    public String buildNotifyNewDecision(Decision decision, List<Option> options) {
        StringBuilder body = new StringBuilder();
        body.append(getResString(R.string.tmsg_NewDecisionMsgPrefix));
        body.append(NEW_LINE);
        body.append(format(decision));
        body.append(',');
        body.append(NEW_LINE);
        body.append(format(decision.getLongDescription()));
        body.append('.');
        body.append(NEW_LINE);
        body.append(getResString(R.string.tmsg_NewDecisionOptionsPrefix));
        addOptionsToMsg(options, body);
        return buildMessage(MessageType.NEW_DECISION, body.toString());
    }

    @Override
    public String buildAddOptions(Decision decision, List<Option> options) {
        String body = buildAddDeleteOptionsMessage(decision,options,
                R.string.tmsg_AddOptionPrefix, R.string.tmsg_AddOptionSuffix);
        return buildMessage(MessageType.ADD_OPTIONS, body);
    }

    @Override
    public String buildDeleteOptions(Decision decision, List<Option> deleteOptions) {
        String body = buildAddDeleteOptionsMessage(decision,deleteOptions,
                R.string.tmsg_DeleteOptionPrefix, R.string.tmsg_DeleteOptionSuffix);
        return buildMessage(MessageType.DELETE_OPTIONS, body);
    }

    private String buildAddDeleteOptionsMessage(Decision decision, List<Option> deleteOptions, int prefixStr, int suffixStr){
        StringBuilder body = new StringBuilder();
        body.append(getResString(prefixStr));
        body.append(NEW_LINE);
        body.append(format(decision));
        body.append(' ');
        body.append(getResString(suffixStr));
        addOptionsToMsg(deleteOptions, body);
        return body.toString();
    }

    private void addOptionsToMsg(List<Option> options, StringBuilder body) {
        body.append(NEW_LINE);
        for(Option o : options){
            body.append(BULLET_LIST_EMOJI);
            body.append(' ');
            body.append(format(o));
            if (o instanceof TextOption) {
                body.append(',');
                body.append(' ');
                body.append(format(((TextOption)o).getLongDescription()));
            }
            body.append(NEW_LINE);
        }
    }

    /**
     * Build a generic message adding specific heading and tailing to the messageBody according
     * to the passed type
     * @param type
     * @param messageBody
     * @return a message ready to be sent
     */
    protected String buildMessage(MessageType type, String messageBody) {
        StringBuilder sb = new StringBuilder();
        sb.append(POLLGRAM_MESSAGE_PREFIX);
        sb.append(' ');
        sb.append(type.getEmoji());
        sb.append(' ');
        sb.append(type.getDescription());
        sb.append(NEW_LINE);

        sb.append(messageBody);

        sb.append(getTailingString());
        return sb.toString();
    }

    @Override
    public String reformatMessage(String message) {
        // remove talning string
        MessageType type = getMessageType(message);
        if (type == null) {
            return message;
        }
        // remove GOOGLE_PLAY_POOLGRAM_URL
        int end = message.lastIndexOf('\n')-1;
        if (end > 0) {
            if (message.substring(end).contains("http"))
                message = message.substring(0, end);
        }
        // Test will fail if uncomment the follwing row
        // message = parseMessageField(message);
        return message;
    }

    /**
     *
     * @param msg
     * @return the MessageType of the given message or null, it is a normal message
     */
    @Override
    public MessageType getMessageType(String msg){
        if (msg != null && msg.startsWith(POLLGRAM_MESSAGE_PREFIX)) {
            try {
                int start = POLLGRAM_MESSAGE_PREFIX.length() + 1;
                String msgEmoji = msg.substring(start, start + 2);
                MessageType t = MessageType.byEmoji(msgEmoji);
                Log.d(LOG_TAG, "MessageType for emoji[" + Arrays.toString(msgEmoji.getBytes()) + "] is [" + t + "]");
                return t;
            } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
                Log.e(LOG_TAG, "Error parsing message type for message [" + msg + "] il will not be parset", e);
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean isPollgram(MessageObject msg) {
        if (msg == null || msg.messageText == null)
            return false;
        return getMessageType(msg.messageText.toString()) != null;
    }

    @Override
    public long getMessageGroupId(MessageObject messageObject) {
        if (messageObject.messageOwner == null)
            return -1;


        if (messageObject.getDialogId() > 0){
            return -1;
        }

        return getMessageGroupId(messageObject.getDialogId());
    }

    @Override
    public long getMessageGroupId(long dialog_id) {
        long groupChatId = dialog_id * -1;
        if (ChatObject.isChannel((int) groupChatId)){
            return -1;
        }
        return groupChatId;
    }

    @Override
    public Collection<Vote> getVotes(String msg, long currentChat, Date messageDate ,int userId) throws PollgramParseException {

        try {
            StringTokenizer strTok = new EscapeStringTokenizer(msg, true);
            strTok.nextToken(); // skip token
            strTok.nextToken(); // skip token
            String decisionTitle = strTok.nextToken();
            Decision decision = pollgramDAO.getDecision(decisionTitle, currentChat);
            if (decision == null)
                throw new PollgramParseException("Decision not found for title["+decisionTitle+"]  currentChat["+currentChat+"]");
            List<Vote> voteList = new ArrayList<>();
            while (strTok.hasMoreTokens()) {
                String voteValue = strTok.nextToken();
                Boolean vote = getBooleanValue(voteValue);

                if (!strTok.hasMoreTokens()){
                    Log.d(LOG_TAG, "No more token after something that look like a vote value ["+voteValue+"]");
                    break;
                }

                String optionTitle = strTok.nextToken();
                Option o = pollgramDAO.getOption(optionTitle, decision);
                if (o == null)
                    throw new PollgramParseException("Option not found for title["+optionTitle+"]  decision["+decision+"]");
                Vote v = new Vote(vote, messageDate, userId, o.getId());
                Log.d(LOG_TAG, "added vote [" + v + "]");
                voteList.add(v);
            }
            Log.d(LOG_TAG, "getVotes votes["+voteList+"]");
            return voteList;
        }catch (NoSuchElementException e ){
            Log.e(LOG_TAG, "Error parsing message ["+msg+"]",e);
            throw new PollgramParseException("Token not found",e);
        }
    }

    @Override
    public DecisionOptionData getNewDecision(String msg, long currentChat, int userId, Date messageDate) throws PollgramParseException {
        Decision decision;
        List<Option> optionList = new ArrayList<>();
        try {
            StringTokenizer strTok = new EscapeStringTokenizer(msg);
            { //Create decsion
                strTok.nextToken();//skip this token
                String title = strTok.nextToken();
                strTok.nextToken();//skip this token
                String longDescription = strTok.nextToken();
                decision = new Decision(currentChat, userId, title, longDescription, messageDate, true);
            }
            addOption2List(optionList, strTok);
        } catch (NoSuchElementException e) {
            Log.e(LOG_TAG, "Error parsing message [" + msg + "]", e);
            throw new PollgramParseException("Token not found", e);
        }
        Log.d(LOG_TAG, "getNewDecision decision[" + decision + "] optionList[" + optionList + "]");
        return new DecisionOptionData(decision, optionList);
    }


    @Override
    public DecisionOptionData getAddedOption(String msg, long currentChat, int userId) throws PollgramParseException {
        return getAddDeleteOptionFromDecision(msg, currentChat, userId);
    }

    @Override
    public DecisionOptionData getDeletedOption(String text, long groupChatId, int userId) throws PollgramParseException {
        return getAddDeleteOptionFromDecision(text, groupChatId, userId);
    }

    @NonNull
    private DecisionOptionData getAddDeleteOptionFromDecision(String msg, long currentChat, int userId) throws PollgramParseException {
        Decision decision;
        List<Option> optionList = new ArrayList<>();
        try {
            StringTokenizer strTok = new EscapeStringTokenizer(msg);
            { //Create decision
                strTok.nextToken();//skip this token
                String title = strTok.nextToken();
                decision = pollgramDAO.getDecision(title,currentChat);
                if (decision == null)
                    throw new PollgramParseException("Decision not found for title ["+title+"]");
                if (!decision.isEditable(userId))
                    throw new PollgramParseException("Decision is not editable by userid ["+userId+"]");
            }
            addOption2List(optionList, strTok);
        } catch (NoSuchElementException e) {
            Log.e(LOG_TAG, "Error parsing message [" + msg + "]", e);
            throw new PollgramParseException("Token not found", e);
        }
        Log.d(LOG_TAG, "getNewDecision decision[" + decision + "] optionList[" + optionList + "]");
        return new DecisionOptionData(decision, optionList);
    }


    private void addOption2List(List<Option> optionList, StringTokenizer strTok) {
        while (strTok.hasMoreTokens()){
            strTok.nextToken();//skip this token
            if (!strTok.hasMoreTokens())
                break;
            String title = strTok.nextToken();
            strTok.nextToken();//skip this token
            String longDesc = strTok.nextToken();
            Option to = new TextOption(title, longDesc, DBBean.ID_NOT_SET);
            optionList.add(to);
        }
    }

    @Override
    public ClosedDecisionDate getCloseDecision(String msg, long currentChat, int userId) throws PollgramParseException {
        Decision decision;
        List<Option> optionList;
        try {
            StringTokenizer strTok = new EscapeStringTokenizer(msg);
            strTok.nextToken();//skip this token
            String title = strTok.nextToken();
            decision = pollgramDAO.getDecision(title, currentChat);
            if (decision == null)
                throw new PollgramParseException("Decision not found for title  [" + title + "]");
            if (!decision.isEditable(userId))
                throw new PollgramParseException("Decision is not editable by userId ["+userId+"]");

            optionList = new ArrayList<>();
            while (strTok.hasMoreTokens()){
                strTok.nextToken();//skip this token
                if (!strTok.hasMoreTokens())
                    break;
                String optTitle = strTok.nextToken();
                Option to = pollgramDAO.getOption(optTitle,decision);
                if (to != null)
                    optionList.add(to);
            }

        } catch (NoSuchElementException e) {
            Log.e(LOG_TAG, "Error parsing message [" + msg + "]", e);
            throw new PollgramParseException("Token not found", e);
        }
        Log.d(LOG_TAG, "getNewDecision decision[" + decision + "] winningOption[" + optionList + "]");
        return new ClosedDecisionDate(decision, optionList);
    }

    @Override
    public Decision getDeleteDecision(String text, long groupChatId, int userId) throws PollgramParseException {
        Decision d = getDecisionInDeleteOrReopenMessage(text, groupChatId, userId);
        Log.d(LOG_TAG, "getDeleteDecision Decision[" + d + "]");
        return d;
    }

    @Override
    public Decision getReopenDecision(String text, long groupChatId, int userId) throws PollgramParseException {
        Decision d = getDecisionInDeleteOrReopenMessage(text,groupChatId, userId);
        if (d ==null)
            throw new PollgramParseException( "Decision not found for message[" + text + "]");
        Log.d(LOG_TAG, "getReopenDecision Decision["+d+"]");
        return d;
    }

    /**
     * Actually Delete and Reopen messages have the same structure
     * @param msg
     * @param groupChatId
     * @param userId
     * @return
     */
    private @Nullable Decision getDecisionInDeleteOrReopenMessage(String msg, long groupChatId, int userId) throws PollgramParseException {
        try {
            StringTokenizer strTok = new EscapeStringTokenizer(msg);
            strTok.nextToken(); // skip this token
            String title = strTok.nextToken();
            Decision d = pollgramDAO.getDecision(title, groupChatId);
            if (d == null)
                throw new PollgramParseException("Decision not found for title ["+title+"]");
            if (!d.isEditable(userId))
                throw new PollgramParseException("Decision is not editable by userid ["+userId+"]");
            return d;
        } catch (NoSuchElementException e){
            Log.e(LOG_TAG, "Error parsing message [" + msg + "]", e);
            throw new PollgramParseException("Token not found", e);
        }
    }

    private int getDecisionTitleStartIdx(MessageType type, String message){
        switch (type){
            // Actually in all messageType decision title is always the first QUTE_CHAR token
            case VOTE:
            case REMIND_TO_VOTE:
            case CLOSE_DECISION:
            case DELETE_DECISION:
            case NEW_DECISION:
            case REOPEN_DECISION:
            case ADD_OPTIONS:
            case DELETE_OPTIONS:
                return message.indexOf(QUOTE_CHAR);
        }
        return  -1;
    }

    private int getDecisionTitleEndIdx(String msgStr, int start) {
        return start + msgStr.substring(start + 1).indexOf(QUOTE_CHAR) + 2;
    }

    @Override
    public void addDecisionURLSpan(MessageType type, Spannable spannable) {
        if (type == null){
            Log.d(LOG_TAG, "Null message type no link added for["+spannable+"]");
            return;
        }
        String msgStr = spannable.toString();
        int start = getDecisionTitleStartIdx(type, msgStr);
        if (start == -1){
            Log.d(LOG_TAG, "Decision title start idx not found for[" + spannable + "]");
            return;
        }
        int end = getDecisionTitleEndIdx(msgStr, start);

        String linkStr = msgStr.subSequence(start, end).toString();
        Log.d(LOG_TAG, "Link string is ["+linkStr+"]");
        URLSpanNoUnderline url = new URLSpanNoUnderline(linkStr);
        spannable.setSpan(url, start, end, 0);
    }


}
