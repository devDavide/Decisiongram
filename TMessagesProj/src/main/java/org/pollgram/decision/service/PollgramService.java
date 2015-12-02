package org.pollgram.decision.service;

import android.os.Bundle;
import android.text.style.ClickableSpan;

import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.UsersDecisionVotes;
import org.pollgram.decision.data.Vote;
import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.TLRPC;

import java.util.Collection;
import java.util.List;

/**
 * Created by davide on 10/11/15.
 */
public interface PollgramService {


    UsersDecisionVotes getUsersDecisionVotes(long decisionId, int[] participantIds);

    UsersDecisionVotes getUsersDecisionVotes(long decisionId, List<TLRPC.User> users);

    void remindUserToVote(Decision decision, TLRPC.User user);

    void notifyNewDecision(Decision decision, List<Option> options);

    void notifyVote(Decision decision, Collection<Vote> votes2Save);

    void notifyClose(Decision decision);

    void notifyReopen(Decision decision);

    void notifyDelete(Decision decision);

    /**
     * Process a message and return the new message, performing some transformation if needed.
     * @param message the input message
     * @return the input message transformed if needed
     */
    void processMessage(MessageObject message);

    List<TLRPC.User> getUsers(int[] usersIds);

    TLRPC.User getUser(int userid);

    /**
     *
     * @param messageObject
     * @param url
     * @return a Bundle for invoking VoteMangerFragment, based on pollgram message
     */
    Bundle getBundleForVotesManagerFragment(TLRPC.ChatFull info, MessageObject messageObject,final ClickableSpan url);

    /**
     * @param user
     * @return a string representation of the passed user.
     */
    String asString(TLRPC.User user);

}
