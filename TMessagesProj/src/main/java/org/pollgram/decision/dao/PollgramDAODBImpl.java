package org.pollgram.decision.dao;

import android.support.annotation.Nullable;

import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.UsersDecisionVotes;
import org.pollgram.decision.data.Vote;
import org.telegram.tgnet.TLRPC;

import java.util.Collection;
import java.util.List;

/**
 * Created by davide on 18/10/15.
 */
public class PollgramDAODBImpl extends PollgramDAO {
    @Override
    public Decision getDecision(long decisionId) {
        return null;
    }

    @Override
    public List<Decision> getDecisions(@Nullable Boolean open) {
        return null;
    }

    @Override
    public List<Option> getOptions(Decision decision) {
        return null;
    }

    @Override
    public List<Option> getOptions(long decisionId) {
        return null;
    }

    @Override
    public List<TLRPC.User> getUsers(int[] usersIds) {
        return null;
    }

    @Override
    public List<Vote> getUserVoteForDecision(long decisionId, int userId) {
        return null;
    }

    @Override
    public void save(Collection<Vote> votest2save) {

    }

    @Override
    public UsersDecisionVotes getUsersDecisionVotes(long decisionId, int[] participantIds) {
        return null;
    }
}
