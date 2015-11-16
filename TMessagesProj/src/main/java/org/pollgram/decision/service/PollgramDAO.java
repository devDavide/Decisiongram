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

    Decision insert(Decision d);

    void update(Decision d);

    Decision getDecision(long decisionId);

    List<Decision> getDecisions(@Nullable Boolean open);

    Option insert(Option o);

    void update(Option o);

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

    Vote save(Vote vote);
}