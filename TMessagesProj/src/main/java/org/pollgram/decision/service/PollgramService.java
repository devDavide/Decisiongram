package org.pollgram.decision.service;

import android.os.Bundle;
import android.support.annotation.Nullable;
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

    /**
     * @param decisionId
     * @param members
     * @return a data structure representing a decision and the vote of each member
     */
    UsersDecisionVotes getUsersDecisionVotes(long decisionId, int[] members);

    /**
     * @param decisionId
     * @param members
     * @return a data structure representing a decision and the vote of each member
     */
    UsersDecisionVotes getUsersDecisionVotes(long decisionId, List<TLRPC.User> members);

    /**
     * remind the user to vote for the passed decision
     * @param decision
     * @param user
     */
    void remindUserToVote(Decision decision, TLRPC.User user);

    /**
     * notify to other groups members that a new decision has been cretaed
     * @param decision
     * @param options
     */
    void notifyNewDecision(Decision decision, List<Option> options);

    /**
     * Notify to other groups members the votes performed by the current user for the decision
     * @param decision
     * @param votes2Save
     */
    void notifyVote(Decision decision, Collection<Vote> votes2Save);

    /**
     * Notify to other groups members that a decision has been closed
     * @param decision
     */
    void notifyClose(Decision decision);

    /**
     * Notify to other groups members that a decision has been reopened
     * @param decision
     */
    void notifyReopen(Decision decision);

    /**
     * Notify to other groups members that a decision, and all his data has been deleted.
     * @param decision
     */
    void notifyDelete(Decision decision);

    /**
     * Notify to other groups members the new options for the decision
     * @param decision
     * @param newOptions
     */
    void notifyNewOptions(Decision decision, List<Option> newOptions);

    /**
     * Notify to other groups members that some options has been deleted from decision
     * @param decision
     * @param deleteOptions
     */
    void notifyDeleteOptions(Decision decision, List<Option> deleteOptions);

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

    /**
     * @param userid
     * @return a user with the givne user id, or null if the user does not exist or is a bot
     */
    @Nullable TLRPC.User getUser(int userid);

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
     *
     * @param currentChat
     * @param selectedObject
     * @return a Bundle for creating a new decision starting from a message, see NewDecisionFragment
     */
    Bundle getBundleForNewDecision(TLRPC.Chat currentChat, MessageObject selectedObject);

    /**
     * @param currentChat
     * @param selectedObject
     * @return a Bundle for adding a new option to a decision starting from a message, see SelectDecisionFragment
     */
    Bundle getBundleForNewOption(TLRPC.Chat currentChat, MessageObject selectedObject);

    /**
     * @param chatInfo
     * @return a Bundle ready to pass to DecisionsListFragment
     */
    Bundle getBundleForDecisionList(TLRPC.ChatFull chatInfo);

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
     * @param dialog_id
     * @param dialogMessagesByIds
     * @param excludeMessages
     * @return the messages that has not been parsed successfully, since now, that are included in
     * dialogMessagesByIds but not in excludeMessages
     */
    List<MessageObject> getUnParsedMessages(final long dialog_id, Map<Integer, MessageObject> dialogMessagesByIds,
                                            List<MessageObject> excludeMessages);


}
