package org.pollgram.decision.service;

import android.support.annotation.Nullable;

import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.Vote;

import java.util.List;

/**
 * Created by davide on 03/10/15.
 */
public interface PollgramDAO {

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
    List<Decision> getDecisions(int chatId, @Nullable Boolean open);

    /**
     * Insert or update the passed Option if not found
     * @param o
     * @return the inserted object
     */
    Option save(Option o);

    Option getOption(long optionId);

    List<Option> getOptions(Decision decision);

    List<Option> getOptions(long decisionId);

    List<Vote> getUserVoteForDecision(long decisionId, int userId);

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
    Decision getDecision(String decisionTitle, int chatId);

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
     * Result for method getWinningOption
     */
    class WinningOption{
        public final int voteCount;
        public final Option option;

        public WinningOption(int voteCount, Option option) {
            this.voteCount = voteCount;
            this.option = option;
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