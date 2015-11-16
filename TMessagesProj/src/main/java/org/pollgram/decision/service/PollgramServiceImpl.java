package org.pollgram.decision.service;

import android.util.Log;

import org.pollgram.R;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.UsersDecisionVotes;
import org.pollgram.decision.data.Vote;
import org.pollgram.decision.utils.PollgramUtils;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davide on 10/11/15.
 */
public class PollgramServiceImpl implements PollgramService {

    private static final String LOG_TAG = "POLLGSRV";
    private final PollgramDAO pollgramDAO;

    public PollgramServiceImpl() {
        this.pollgramDAO = PollgramServiceFactory.getPollgramDAO();
    }

    @Override
    public List<TLRPC.User> getUsers(int[] usersIds) {
        List<TLRPC.User> users = new ArrayList<>();
        for (int i = 0; i < usersIds.length; i++) {
            TLRPC.User user = MessagesController.getInstance().getUser(usersIds[i]);
            if (user.status == null) {// suppose this is abot
                Log.i(LOG_TAG, "User [" + user + "] is a BOT, it will be skipped");
                continue;
            }
            users.add(user);
        }
        return users;
    }

    @Override
    public UsersDecisionVotes getUsersDecisionVotes(long decisionId, int[] participantIds) {
        List<TLRPC.User> users = getUsers(participantIds);
        return getUsersDecisionVotes(decisionId, users);

    }

    @Override
    public UsersDecisionVotes getUsersDecisionVotes(long decisionId, List<TLRPC.User> users) {
        Decision decision = pollgramDAO.getDecision(decisionId);
        List<Option> options = pollgramDAO.getOptions(decisionId);
        List<Vote> votes = pollgramDAO.getVotes(decisionId, null);
        UsersDecisionVotes udv = new UsersDecisionVotes(decision, users, options, votes);
        return udv;
    }

    @Override
    public Vote save(Vote vote) {
        // TODO send message for vote
        return pollgramDAO.save(vote);
    }

    @Override
    public void remindUserToVote(TLRPC.User user, long groupChatId, Decision decision) {
        String msg = PollgramUtils.POLLGRAM_MESSAGE_PREFIX + ApplicationLoader.applicationContext.
                getString(R.string.remindMessage, PollgramUtils.asString(user), decision.getTitle());
        long peer = -groupChatId;
        MessageObject replyToMsg = null;
        TLRPC.WebPage webPAge = null;
        boolean searchLinks = false;
        boolean asAdmin = false;
        SendMessagesHelper.getInstance().sendMessage(msg, peer, replyToMsg, webPAge, searchLinks, asAdmin);
    }

}
