package org.decisiongram.service;

import android.support.annotation.Nullable;

import org.decisiongram.data.Decision;
import org.decisiongram.data.Option;
import org.decisiongram.data.ParsedMessage;
import org.decisiongram.data.Vote;

import java.util.List;

/**
 * Created by davide on 03/10/15.
 */
public interface DecisionDAO {

    /**
     * Insert or update the passed decision if not found
     * @param d
     * @return the inserted object
     */
    Decision save(Decision d);

    /**
     * return decision by id
     * @param decisionId
     * @return null if not found
     */
    Decision getDecision(long decisionId);

    /**
     * @param chatId
     * @param open if null it will return either open or close decisions
     * @return decision for given chat id
     */
    List<Decision> getDecisions(long chatId, @Nullable Boolean open);

    /**
     * @param chatId
     * @param  decisionOwnerId decision owner
     * @return decision for given chat id
     */
    List<Decision> getDecisions(long chatId, int decisionOwnerId);

    /**
     * Insert or update the passed Option if not found
     * @param o
     * @return the inserted object
     */
    Option save(Option o);

    Option getOption(long optionId);

    List<Option> getOptions(Decision decision);

    List<Option> getOptions(long decisionId);

    /**
     * Votes for given decisionId and userId. UserId can be null, in this case will reurn the
     * votes for any user
     *
     * @param decisionId decision
     * @param userId     userId, if null it means all user
     * @return
     */
    List<Vote> getVotes(long decisionId, @Nullable Integer userId);

    /**
     * Insert or update the passed Vote if not found
     * @param vote
     * @return the inserted object
     */
    Vote save(Vote vote);

    /**
     * Get decision with givne decisionTitle and chatId
     * @param decisionTitle
     * @param chatId
     * @return null if no decision was found
     */
    Decision getDecision(String decisionTitle, long chatId);

    Option getOption(String optionTitle, Decision decision);

    /**
     * @param optionId
     * @param userId
     * @return
     */
    Vote getVote(long optionId, int userId);

    /**
     * @param decision
     * @return how many users has voted, at least one option, for the target decision
     */
    int getUserVoteCount(Decision decision);

    /**
     * Delete permanently a decision and all its options and vote
     * @param decision
     */
    void delete(Decision decision);

    /**
     * Delete permanently an option and its votes
     * @param option
     */
    void delete(Option option);

    /**
     *
     * @param groupChatId
     * @param id
     * @return whether the message has been parsed succesfully
     */
    boolean hasBeenParsed(final long groupChatId, int id);

    /**
     * set a message as parsed
     * @param groupChatId
     * @param messageId
     * @param parsedSuccessfully the message has been parsed successfully
     */
    ParsedMessage setMessageAsParsed(final long groupChatId, int messageId, boolean parsedSuccessfully);

    /**
     * @param groupChatId
     * @return the list of the messages that has not been parsed yet
     */
    List<ParsedMessage> getUnparsedMessages(final long groupChatId);


    /**
     * Result for method getWinningOption
     */
    class WinningOption{
        public final int voteCount;
        public final List<Option> options;

        public WinningOption(int voteCount, List<Option> options) {
            this.voteCount = voteCount;
            this.options = options;
        }
    }

    /**
     * @param decision
     * @return the option that recive more votes for the decision
     */
    WinningOption getWinningOption(Decision decision);

    /**
     * just for test
     */
    void purgeData();

    /**
     * just for test
     * TODO Remove
     */
    void putStubData(int chatId, int creatorId);
}