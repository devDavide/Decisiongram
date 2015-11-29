package org.pollgram.decision.service;

import android.content.Context;
import android.util.Log;

import org.pollgram.R;
import org.pollgram.decision.data.DBBean;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.TextOption;
import org.pollgram.decision.data.Vote;
import org.pollgram.decision.utils.PollgramUtils;
import org.telegram.messenger.ApplicationLoader;

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

    private static final String GOOGLE_PLAY_POOLGRAM_URL = "https://play.google.com/store/apps/details?id=org.pollgram";

    private static final String POLLGRAM_MESSAGE_PREFIX = "#Pollgram ";
    private static final char QUOTE_CHAR = '\'';
    private static final char NEW_LINE = '\n';

    private static final String WINKING_FACE_EMOJI = PollgramUtils.getEmojiAsString((byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte)0x89);// winking face
    private static final String TRUE_EMOJI = PollgramUtils.getEmojiAsString((byte) 0xE2, (byte) 0x9C, (byte) 0x85);// WHITE HEAVY CHECK MARK
    private static final String FALSE_EMOJI = PollgramUtils.getEmojiAsString((byte) 0xE2, (byte) 0x9D, (byte) 0x8C);// CROSS MARK
    private static final String BULLET_LIST_EMOJI = PollgramUtils.getEmojiAsString((byte) 0xE2, (byte) 0x96, (byte) 0xAB);// white small square

    private final PollgramDAO pollgramDAO;
    private final Context context;

    PollgramMessagesManagerImpl(){
        this(PollgramFactory.getPollgramDAO());
    }

    String getTailingString(){
        Context c = ApplicationLoader.applicationContext;
        return new StringBuilder().append(NEW_LINE).
                append(NEW_LINE).
                append(c.getString(R.string.downloadPollgramFromMarket)).
                append(' ').
                append(GOOGLE_PLAY_POOLGRAM_URL).
                toString();
    }

    PollgramMessagesManagerImpl(PollgramDAO pollgramDAO){
        this.pollgramDAO = pollgramDAO;
        context = ApplicationLoader.applicationContext;
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

        strValue = QUOTE_CHAR + strValue + QUOTE_CHAR;
        return strValue;
    }

    private String getBooleanValue(Boolean b) {
        StringBuilder sb = new StringBuilder();
        sb.append(b ? TRUE_EMOJI : FALSE_EMOJI);
        sb.append(" ");
        sb.append(ApplicationLoader.applicationContext.getString(b ? R.string.yes : R.string.no));
        return sb.toString();
    }

    private Boolean getBooleanValue(String str) {
        return str != null && str.startsWith(TRUE_EMOJI);
    }

    @Override
    public String buildRemindMessage(String userAsString, Decision decision) {
        StringBuilder body = new StringBuilder();
        body.append(context.getString(R.string.tmsg_RemindToVoteP1));
        body.append(' ');
        body.append(format(userAsString));
        body.append(' ');
        body.append(context.getString(R.string.tmsg_RemindToVoteP2));
        body.append(NEW_LINE);
        body.append(format(decision));
        body.append(NEW_LINE);
        body.append(context.getString(R.string.tmsg_RemindToVoteP3));
        body.append(' ');
        body.append(WINKING_FACE_EMOJI);
        return  buildMessage(MessageType.REMIND_TO_VOTE, body.toString());
    }

    @Override
    public String buildCloseDecision(Decision decision, Option winningOption, int voteCount) {
        StringBuilder body = new StringBuilder();
        body.append(context.getString(R.string.tmsg_CloseDecisionP1));
        body.append(' ');
        body.append(format(decision));
        body.append(context.getString(R.string.tmsg_CloseDecisionP2));
        body.append(format(winningOption));
        body.append(context.getString(R.string.tmsg_CloseDecisionP3));
        body.append(' ');
        body.append(voteCount);
        body.append(' ');
        body.append(context.getString(R.string.tmsg_CloseDecisionP4));

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
        body.append(context.getString(prefixStringRes));
        body.append(' ');
        body.append(format(decision));
        body.append(NEW_LINE);
        body.append(context.getString(suffixStringRes));
        return buildMessage(type, body.toString());
    }

    @Override
    public String buildNotifyVoteMessage(Decision decision, Collection<Vote> votes2Save) {
        StringBuilder body = new StringBuilder();
        body.append(context.getString(R.string.tmsg_Vote));
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
        body.append(context.getString(R.string.tmsg_NewDecisionMsgPrefix));
        body.append(NEW_LINE);
        body.append(format(decision));
        body.append(',');
        body.append(NEW_LINE);
        body.append(format(decision.getLongDescription()));
        body.append('.');
        body.append(NEW_LINE);
        body.append(context.getString(R.string.tmsg_NewDecisionOptionsPrefix));
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
        return buildMessage(MessageType.NEW_DECISION, body.toString());
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
        return message.replace(getTailingString(), "");
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
                Log.d(LOG_TAG, "MessageType for [" + Arrays.toString(msgEmoji.getBytes()) + "] is [" + t + "]");
                return t;
            } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
                Log.e(LOG_TAG, "Error parsing message type for message [" + msg + "] il will not be parset", e);
                return null;
            }
        }
        Log.d(LOG_TAG,"["+msg+"] is a normal message");
        return null;
    }


    @Override
    public Collection<Vote> getVotes(String msg, int currentChat, Date messageDate ,int userId) throws PollgramParseException {

        try {
            StringTokenizer strTok = new StringTokenizer(msg, Character.toString(QUOTE_CHAR) + Character.toString(NEW_LINE));
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
    public NewDecisionData getNewDecision(String msg, int currentChat, int userId) throws PollgramParseException {
        Decision decision;
        List<Option> optionList = new ArrayList<>();
        try {
            StringTokenizer strTok = new StringTokenizer(msg, Character.toString(QUOTE_CHAR));
            { //Create decsion
                strTok.nextToken();//skip this token
                String title = strTok.nextToken();
                strTok.nextToken();//skip this token
                String longDescription = strTok.nextToken();
                decision = new Decision(currentChat, userId, title, longDescription, true);
            }
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
        } catch (NoSuchElementException e) {
            Log.e(LOG_TAG, "Error parsing message [" + msg + "]", e);
            throw new PollgramParseException("Token not found", e);
        }
        Log.d(LOG_TAG, "getNewDecision decision[" + decision + "] optionList[" + optionList + "]");
        return new NewDecisionData(decision, optionList);
    }

    @Override
    public ClosedDecisionDate getCloseDecision(String msg, int currentChat) throws PollgramParseException {
        Decision decision;
        Option winningOption;
        try {
            StringTokenizer strTok = new StringTokenizer(msg, Character.toString(QUOTE_CHAR));
            strTok.nextToken();//skip this token
            String title = strTok.nextToken();
            strTok.nextToken();//skip this token
            String optionTitle = strTok.nextToken();//skip this token

            String longDescription = strTok.nextToken();
            decision = pollgramDAO.getDecision(title, currentChat);
            winningOption = pollgramDAO.getOption(optionTitle, decision);


        } catch (NoSuchElementException e) {
            Log.e(LOG_TAG, "Error parsing message [" + msg + "]", e);
            throw new PollgramParseException("Token not found", e);
        }
        Log.d(LOG_TAG, "getNewDecision decision[" + decision + "] winningOption[" + winningOption + "]");
        return new ClosedDecisionDate(decision, winningOption);
    }

    @Override
    public Decision getDeleteDecision(String text, int groupChatId) throws PollgramParseException {
        Decision d = getDecisionInDeleteOrReopenMessage(text, groupChatId);
        Log.d(LOG_TAG, "getDeleteDecision Decision["+d+"]");
        return d;
    }

    @Override
    public Decision getReopenDecision(String text, int groupChatId) throws PollgramParseException {
        Decision d = getDecisionInDeleteOrReopenMessage(text,groupChatId);
        Log.d(LOG_TAG, "getReopenDecision Decision["+d+"]");
        return d;
    }

    /**
     * Actually Delete and Reopen messages have the same structure
     * @param msg
     * @param groupChatId
     * @return
     */
    private Decision getDecisionInDeleteOrReopenMessage(String msg, int groupChatId) throws PollgramParseException {
        Decision decision;
        try {
            StringTokenizer strTok = new StringTokenizer(msg, Character.toString(QUOTE_CHAR));
            strTok.nextToken(); // skipt this token
            String decisionTitle = strTok.nextToken();
            Decision d = pollgramDAO.getDecision(decisionTitle, groupChatId);
            if (d ==null)
                throw new PollgramParseException("Decision not found for title["+decisionTitle+"] and groupChatId["+groupChatId+"]");
            return d;
        } catch (NoSuchElementException e){
            Log.e(LOG_TAG, "Error parsing message [" + msg + "]", e);
            throw new PollgramParseException("Token not found", e);
        }
    }


}
