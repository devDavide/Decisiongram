package org.pollgram.decision.dao;

import android.support.annotation.Nullable;

import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.UsersDecisionVotes;
import org.pollgram.decision.data.Vote;
import org.telegram.tgnet.TLRPC;

import java.util.List;

/**
 * Created by davide on 03/10/15.
 */
public abstract class PollgramDAO {

    public abstract Decision insert(Decision d);

    public abstract void update(Decision d);

    public abstract Decision getDecision(long decisionId);

    public abstract List<Decision> getDecisions(@Nullable Boolean open);

    public abstract Option insert(Option o);

    public abstract void update(Option o);

    public abstract Option getOption(long optionId);

    public abstract List<Option> getOptions(Decision decision);

    public abstract List<Option> getOptions(long decisionId);

    public abstract List<TLRPC.User> getUsers(int[] usersIds);

    public abstract List<Vote> getUserVoteForDecision(long decisionId, int userId);

    public abstract Vote save(Vote vote);

    public abstract UsersDecisionVotes getUsersDecisionVotes(long decisionId, int[] participantIds);

    public abstract UsersDecisionVotes getUsersDecisionVotes(long decisionId, List<TLRPC.User> users);

    private static volatile PollgramDAO Instance = null;

    public static PollgramDAO getInstance() {
        PollgramDAO localInstance = Instance;
        if (localInstance == null) {
            synchronized (PollgramDAO.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new PollgramDAODBImpl();
                }
            }
        }
        return localInstance;
    }

}
