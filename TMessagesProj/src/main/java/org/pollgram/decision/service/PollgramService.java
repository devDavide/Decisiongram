package org.pollgram.decision.service;

import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.UsersDecisionVotes;
import org.pollgram.decision.data.Vote;
import org.telegram.tgnet.TLRPC;

import java.util.List;

/**
 * Created by davide on 10/11/15.
 */
public interface PollgramService {

    List<TLRPC.User> getUsers(int[] usersIds);

    UsersDecisionVotes getUsersDecisionVotes(long decisionId, int[] participantIds);

    UsersDecisionVotes getUsersDecisionVotes(long decisionId, List<TLRPC.User> users);

    Vote save(Vote vote);

    void remindUserToVote(TLRPC.User user, long groupChatId, Decision decision);
}
