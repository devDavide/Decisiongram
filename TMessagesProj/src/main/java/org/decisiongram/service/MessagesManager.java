package org.decisiongram.service;

import android.support.annotation.NonNull;
import android.text.Spannable;

import org.decisiongram.R;
import org.decisiongram.data.Decision;
import org.decisiongram.data.Option;
import org.decisiongram.data.TextOption;
import org.decisiongram.data.Vote;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessageObject;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by davide on 19/11/15.
 */
public interface MessagesManager {


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
        DELETE_DECISION(R.string.MessageType_DELETE_DECISION, (byte) 0xE2, (byte) 0x9D, (byte) 0x8C),    //CROSS MARK
        ADD_OPTIONS(R.string.MessageType_ADD_OPTIONS,(byte)0xE2, (byte)0x9E, (byte)0x95),// heavy plus sign
        DELETE_OPTIONS(R.string.MessageType_DELTE_OPTIONS, (byte)0xE2, (byte)0x9E, (byte)0x96),
        UPDATE_OPTION_NOTES(R.string.MessageType_UPDATE_OPTION_NOTES , BULLET_LIST_EMOJI.getBytes() ); //	heavy minus sign

        private final String emoji;
        private final int descrStringRes;
        private String descriptionString;

        MessageType(int descrStringRes, byte... emojiBytes) {
            this.emoji = EmojiUtils.getEmojiAsString(emojiBytes);
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

    String BULLET_LIST_EMOJI = EmojiUtils.getEmojiAsString((byte) 0xE2, (byte) 0x96, (byte) 0xAB);// white small square

    /**
     * Add URL link for decision title on charSequence message
     * @param type
     * @param charSequence
     */
    void addDecisionURLSpan(MessageType type, Spannable charSequence);

    /**
     * remove unnecessary part of the message, like the link for downloading Pollgram from the market
     * that is needed just for Telegram client different than Pollgram
     * @param message
     * @return
     */
    String reformatMessage(String message);

    String parseMessageField(String decisionTitle);


    /**
     * @param messageObject
     * @return the group chat id of this message or -1 if this message is not a group chat message
     */
    long getMessageGroupId(MessageObject messageObject);

    long getMessageGroupId(long dialog_id);

    /**
     * @param msg message to parse
     * @return whether the message is a decisiongram transaction message it returns the message type,
     * otherwise it returns null
     */
    MessageType getMessageType(String msg);

    /**
     * @param msg
     * @return whether this is a decisiongram message
     */
    boolean isPollgram(MessageObject msg);

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
     * build a message for notifying a that some option has been added to a decision
     * @param decision
     * @param newOptions the new options added
     * @return the message ready to be sent
     */
    String buildAddOptions(Decision decision, List<Option> newOptions);

    /**
     * build a message for notifying that the long description of an option has been updated
     * @return
     */
    String buildUpdateOptionNotes(Decision decision, TextOption option);

    /**
     * build a message for notifying a that some option has been deleted from a decision
     * @param decision
     * @param deleteOptions
     * @return
     */
    String buildDeleteOptions(Decision decision, List<Option> deleteOptions);

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
     * @param  winningOptions the options that received more votes so far
     * @param voteCount
     * @return the message ready to be sent
     */
    String buildCloseDecision(Decision decision, List<Option> winningOptions, int voteCount);

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
     * if the message is {@link MessagesManager.MessageType#VOTE}
     * @param msg the text message to parse
     * @param currentChat current group chat
     * @param userId message owner
     * @return a collection of the vote contained in the message
     * @throws ParseException is the message is not well formed
     */
    Collection<Vote> getVotes(String msg, long currentChat, Date messageDate, int userId) throws MessageParseException;

    /**
     * Retrun vale for getNewDecision method
     */
    class DecisionOptionData {
        final @NonNull Decision decision;
        final @NonNull List<Option> optionList;

        public DecisionOptionData(@NonNull Decision decision, @NonNull List<Option> optionList) {
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
    DecisionOptionData getNewDecision(String msg, long currentChat, int userId, Date messageDate) throws MessageParseException;

    /**
     * Only if getMessageType(text) == MessageType_ADD_OPTION
     * Return the new option added for a decision
     * @param msg the text message to parse
     * @param currentChat current group chat
     * @param userId message owner
     * @return the decision and its options
     */
    DecisionOptionData getAddedOption(String msg, long currentChat, int userId) throws MessageParseException;

    /**
     * Only if getMessageType(text) == MessageType_DELTE_OPTIONS
     * Return the new option that has been deleted for the decision
     * @param text
     * @param groupChatId
     * @param userId
     * @return
     */
    DecisionOptionData getDeletedOption(String text, long groupChatId, int userId) throws MessageParseException;

    /**
     * Only if getMessageType(text) == MessageType_UPDATE_OPTION_NOTES
     * return the new option data
     * @param text
     * @param groupChatId
     * @param userId
     * @return
     */
    TextOption getNewOptionData(String text, long groupChatId, int userId) throws MessageParseException;

    /**
     * Return value for getCloseDecision
     */
    class ClosedDecisionDate {
        final Decision decision;
        final List<Option> winningOptions;

        public ClosedDecisionDate(Decision decision, List<Option> winningOptions) {
            this.decision = decision;
            this.winningOptions = winningOptions;
        }
    }

    /**
     * Only if getMessageType(text) == MessageType_CLOSE_DECISION
     * @param text
     * @param groupChatId
     * @param userId
     * @return the data for close decision message
     */
    ClosedDecisionDate getCloseDecision(String text, long groupChatId, int userId) throws MessageParseException;

    /**
     * * Only if getMessageType(text) == MessageType_DELETE_DECISION
     * @param text
     * @param groupChatId
     * @param userId
     * @return the decision to delete
     */
    Decision getDeleteDecision(String text, long groupChatId, int userId) throws MessageParseException;

    /**
     * Only if getMessageType(text) == MessageType_REOPEN_DECISION
     * @param text
     * @param groupChatId
     * @param userId
     * @return the decision to reopen
     */
    Decision getReopenDecision(String text, long groupChatId, int userId) throws MessageParseException;

}
