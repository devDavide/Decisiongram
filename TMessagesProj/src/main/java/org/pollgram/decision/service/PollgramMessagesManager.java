package org.pollgram.decision.service;

import android.support.annotation.NonNull;
import android.text.Spannable;

import org.pollgram.R;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.Vote;
import org.pollgram.decision.utils.PollgramUtils;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessageObject;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by davide on 19/11/15.
 */
public interface PollgramMessagesManager {

    /**
     * The different messages type managed by Pollgram
     * Emoji codes are taken from http://apps.timwhitlock.info/emoji/tables/unicode#block-6a-additional-emoticons
     */
    enum MessageType {
        REMIND_TO_VOTE(R.string.MessageType_REMIND_TO_VOTE, (byte) 0xF0, (byte) 0x9F, (byte) 0x94, (byte) 0x94), // bell
        NEW_DECISION(R.string.MessageType_NEW_DECISION, (byte) 0xF0, (byte) 0x9F, (byte) 0x93, (byte) 0x8A), // bar chart
        VOTE(R.string.MessageType_VOTE, (byte) 0xF0, (byte) 0x9F, (byte) 0x93, (byte) 0x9D), // memo
        CLOSE_DECISION(R.string.MessageType_CLOSE_DECISION, (byte) 0xF0, (byte) 0x9F, (byte) 0x9A, (byte) 0xAB), // no entry sign
        REOPEN_DECISION(R.string.MessageType_REOPEN_DECISION, (byte) 0xF0, (byte) 0x9F, (byte) 0x94, (byte) 0x84),    //ANTICLOCKWISE DOWNWARDS AND UPWARDS OPEN CIRCLE ARROWS
        DELETE_DECISION(R.string.MessageType_DELETE_DECISION, (byte) 0xE2, (byte) 0x9D, (byte) 0x8C);    //CROSS MARK

        private final String emoji;
        private final int descrStringRes;
        private String descriptionString;

        private MessageType(int descrStringRes, byte... emojiBytes) {
            this.emoji = PollgramUtils.getEmojiAsString(emojiBytes);
            this.descrStringRes = descrStringRes;
        }

        public String getEmoji() {
            return emoji;
        }

        public static MessageType byEmoji(String emoji){
            String trimmed = emoji.trim();
            for (MessageType mt : MessageType.values()){
                if (trimmed.equals(mt.getEmoji()))
                    return mt;
            }
            return null;
        }

        public String getDescription() {
            if (descriptionString == null)
                descriptionString = ApplicationLoader.applicationContext.getString(descrStringRes);
            return descriptionString;
        }

    }

    /**
     * Add URL link for decision title on charSequence message
     * @param type
     * @param charSequence
     */
    void addDecisionURLSpan(MessageType type, Spannable charSequence);


    /**
     * @param messageObject
     * @return the group chat id of this message or -1 if this message is not a group chat message
     */
    int getMessageGroupId(MessageObject messageObject);

    /**
     * @param msg message to parse
     * @return whether the message is a pollgram transaction message it returns the message type,
     * otherwise it returns null
     */
    MessageType getMessageType(String msg);

    /**
     * remove unnecessary part of the message, like the link for downloading Pollgram from the market
     * that is needed just for Telegram client different than Pollgram
     * @param message
     * @return
     */
    String reformatMessage(String message);

    /**
     * Build message for notify a vote transaction
     * @param decision
     * @param votes2Save
     * @return the message ready to be sent
     */
    String buildNotifyVoteMessage(Decision decision, Collection<Vote> votes2Save);

    /**
     * build a message for notifying a creation of a new decision in a group
     * @param decision
     * @param options
     * @return the message ready to be sent
     */
    String buildNotifyNewDecision(Decision decision, List<Option> options);

    /**
     * build a message in order to remind to userAsString, that he must vote for the decision
     * @param userAsString
     * @param decision
     * @return the message ready to be sent
     */
    String buildRemindMessage(String userAsString, Decision decision);

    /**
     * build a message for notifying that the passed decision is now closed and is no more possible to vote
     * @param decision
     * @param  winningOption the option that received more votes so far
     * @param voteCount
     * @return the message ready to be sent
     */
    String buildCloseDecision(Decision decision, Option winningOption, int voteCount);

    /**
     * build a message for notifying that the passed decision is has been reopened and is no more possible to vote
     * @param decision
     * @return
     */
    String buildReopenDecision(Decision decision);

    /**
     * build a message for notifying that the passed decision is has been deleted. All decision data will be lost.
     * @param  decision
     * @return
     */
    String buildDeleteDecision(Decision decision);

    /**
     * Only if getMessageType(text) == MessageType_VOTE
     * if the message is {@link org.pollgram.decision.service.PollgramMessagesManager.MessageType#VOTE}
     * @param msg the text message to parse
     * @param currentChat current group chat
     * @param userId message owner
     * @return a collection of the vote contained in the message
     * @throws ParseException is the message is not well formed
     */
    Collection<Vote> getVotes(String msg, int currentChat, Date messageDate, int userId) throws PollgramParseException;

    /**
     * Retrun vale for getNewDecision method
     */
    class NewDecisionData{
        final @NonNull Decision decision;
        final @NonNull List<Option> optionList;

        public NewDecisionData(@NonNull Decision decision, @NonNull List<Option> optionList) {
            this.decision = decision;
            this.optionList = optionList;
        }
    }

    /**
     * Only if getMessageType(text) == MessageType_NEW_DECISION
     * Return the new decision data contained in the passed message
     * @param msg the text message to parse
     * @param currentChat current group chat
     * @param userId message owner
     * @param messageDate the message date
     * @return the decision and its options
     */
    NewDecisionData getNewDecision(String msg, int currentChat, int userId, Date messageDate) throws PollgramParseException;

    /**
     * Return value for getCloseDecision
     */
    class ClosedDecisionDate {
        final Decision decision;
        final Option winningOption;

        public ClosedDecisionDate(Decision decision, Option winningOption) {
            this.decision = decision;
            this.winningOption = winningOption;
        }
    }

    /**
     * Only if getMessageType(text) == MessageType_CLOSE_DECISION
     * @param text
     * @param groupChatId
     * @return the data for close decision message
     */
    ClosedDecisionDate getCloseDecision(String text, int groupChatId) throws PollgramParseException;

    /**
     * * Only if getMessageType(text) == MessageType_DELETE_DECISION
     * @param text
     * @param groupChatId
     * @return the decision to delete
     */
    Decision getDeleteDecision(String text, int groupChatId) throws PollgramParseException;

    /**
     * Only if getMessageType(text) == MessageType_REOPEN_DECISION
     * @param text
     * @param groupChatId
     * @return the decision to reopen
     */
    Decision getReopenDecision(String text, int groupChatId) throws PollgramParseException;

}
