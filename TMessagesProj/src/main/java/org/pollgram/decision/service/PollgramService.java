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
import java.util.Date;
import java.util.List;
import java.util.Map;

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
     * @param message
     * @return whether the messsage is a pollgram transaction message
     */
    boolean isPollgramMessage(MessageObject message);

    /**
     * @param messageObject
     * @return the reference date of the passed message
     */
    Date getMessageDate(MessageObject messageObject);

    /**
     * Process a message and return the new message, performing some transformation if needed.
     * @param message the input message
     * @param showToastOnError show a Toast if an error will occur
     * @return the input message transformed if needed
     */
    void processMessage(MessageObject message, boolean showToastOnError);

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
     * @throws PollgramDAOException in case of not found decision. The message can be showed to
     * the user by using for example a toast
     */
    Bundle getBundleForVotesManagerFragment(TLRPC.ChatFull info, MessageObject messageObject,
                                            final ClickableSpan url) throws PollgramDAOException;

    /**
     * @param user
     * @return a string representation of the passed user.
     */
    String asString(TLRPC.User user);

    /**
     * runs the initial import of messages in the group..only inf not did before
     * @param objects
     */
    void processMessages(final long dialog_id, List<MessageObject> objects);

    /**
     *
     * @param dialog_id
     * @param dialogMessagesByIds
     * @param excludeMessages
     * @return the messages that has not been parsed successfully, since now, that are included in
     * dialogMessagesByIds but not in excludeMessages
     */
    List<MessageObject> getUnParsedMessages(final long dialog_id, Map<Integer, MessageObject> dialogMessagesByIds,
                                            List<MessageObject> excludeMessages);
}
