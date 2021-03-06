package org.decisiongram.service;

import android.support.annotation.Nullable;

import org.decisiongram.data.DBBean;
import org.decisiongram.data.Decision;
import org.decisiongram.data.Option;
import org.decisiongram.data.ParsedMessage;
import org.decisiongram.data.TextOption;
import org.decisiongram.data.Vote;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by davide on 21/11/15.
 */
public class DecisionDAOTestImpl implements DecisionDAO {

    private static final String LOG_TAG = "DecisionDAOImpl";
    private static int myId = 0;

    static final long DECISION_ID_1 = myId++;
    static final int DECISION_ID_EMPTY_LONG_DESC = myId++;
    static final int DECISION_ID_NULL_LONG_DESC = myId++;
    static final int DECISION_ID_MULTILINE_LONG_DESC = myId++;
    static final int DECISION_ID_QUOTED_LONG_DESC = myId++;


    // TODO remove stub field
    private final List<Decision> decisions = new ArrayList<>();
    private final List<Option> textOptions = new ArrayList<>();
    private final List<Vote> votes = new ArrayList<>();

    DecisionDAOTestImpl(int chatId, int creatorId) {
        /// just for test

        Date date = new Date();
        int id = 1;

        decisions.add(new Decision(DECISION_ID_1, chatId, creatorId, "what present do we buy ?", " i am huge bla bla bla", date, true));
        textOptions.add(new TextOption(id++, "Ski", "They cost 385EUR i saw them at the corner shop", DECISION_ID_1));
        textOptions.add(new TextOption(id++, "Phone", "The new StonexOne is AWESOME !!!", DECISION_ID_1));
        textOptions.add(new TextOption(id++, "Trip", "Yeah a trip trought Europe can be a nice idea", DECISION_ID_1));
        textOptions.add(new TextOption(id++, "A stupid idea", "it is late and i have no more ideas ;-/", DECISION_ID_1));

        decisions.add(new Decision(DECISION_ID_EMPTY_LONG_DESC, chatId, creatorId, "Where do we go ?", "", date, true));
        textOptions.add(new TextOption(id++, "it's a pair of Ski", "They cost 385EUR i saw them at the corner shop", DECISION_ID_EMPTY_LONG_DESC));
        textOptions.add(new TextOption(id++, "Phone", "The new StonexOne's AWESOME !!!", DECISION_ID_EMPTY_LONG_DESC));
        textOptions.add(new TextOption(id++, "that's a trip. It's", "Yeah a trip trought Europe can be a nice idea", DECISION_ID_EMPTY_LONG_DESC));
        textOptions.add(new TextOption(id++, "A stupid idea", "it is late and i have no more ideas ;-/", DECISION_ID_EMPTY_LONG_DESC));

        decisions.add(new Decision(DECISION_ID_NULL_LONG_DESC, chatId, creatorId, "When will the party be ?", null, date, true));

        decisions.add(new Decision(DECISION_ID_MULTILINE_LONG_DESC, chatId, creatorId, "When will the party be ?", "this is\na multine\nfucking dscription", date, true));
        textOptions.add(new TextOption(id++, "Ski", "They cost 385EUR i saw them at the corner shop", DECISION_ID_MULTILINE_LONG_DESC));
        textOptions.add(new TextOption(id++, "Phone", "The new StonexOne is AWESOME !!!", DECISION_ID_MULTILINE_LONG_DESC));
        textOptions.add(new TextOption(id++, "Trip", "Yeah a trip trought Europe can be a nice idea", DECISION_ID_MULTILINE_LONG_DESC));
        textOptions.add(new TextOption(id++, "A stupid idea", "it is late and i have no more ideas ;-/", DECISION_ID_MULTILINE_LONG_DESC));

        decisions.add(new Decision(DECISION_ID_QUOTED_LONG_DESC, chatId, creatorId, "Do we add Slomp to the group ?", "i'm a huge bla bla", date, true));
        textOptions.add(new TextOption(id++, "Ski", "They cost 385EUR i saw them at the corner shop", DECISION_ID_QUOTED_LONG_DESC));
        textOptions.add(new TextOption(id++, "Phone", "The new StonexOne is AWESOME !!!", DECISION_ID_QUOTED_LONG_DESC));
        textOptions.add(new TextOption(id++, "Trip", "Yeah a trip trought Europe can be a nice idea", DECISION_ID_QUOTED_LONG_DESC));
        textOptions.add(new TextOption(id++, "A stupid idea", "it is late and i have no more ideas ;-/", DECISION_ID_QUOTED_LONG_DESC));

    }

    @Override
    public Decision save(Decision d) {
        Decision foundDecision = getDecision(d.getTitle(), d.getChatId());
        return save(d, foundDecision, decisions);
    }

    private <T extends DBBean> T save(T d, T found, List<T> list) {
        if (found == null)
            list.add(d);
        else {
            d.setId(found.getId());
            list.set(list.indexOf(found), d);
        }
        return d;
    }


    @Override
    public Decision getDecision(long decisionId) {
        for (Decision d : decisions) {
            if (d.getId() == decisionId)
                return d;
        }
        return null;
    }

    @Override
    public List<Decision> getDecisions(long chatId, @Nullable Boolean open) {
        List<Decision> outList = new ArrayList<>();
        for (Decision d : decisions)
            if (open == null || d.isOpen() == open.booleanValue())
                outList.add(d);
        return outList;
    }

    @Override
    public List<Decision> getDecisions(long chatId, int decisionOwnerId) {
        return null;
    }

    @Override
    public Option getOption(long optionId) {
        for (Option o : textOptions)
            if (o.getId() == optionId)
                return o;
        return null;
    }

    @Override
    public List<Option> getOptions(Decision decision) {
        return getOptions(decision.getId());

    }

    @Override
    public List<Option> getOptions(long decisionId) {
        List<Option> outList = new ArrayList<>();
        for (Option o : textOptions) {
            if (o.getDecisionId() == decisionId)
                outList.add(o);
        }
        return outList;
    }

    private Vote getVote(int userId, Option option) {
        // TODO so stub
        long stubId = option.getId() * 17;
        Boolean voteValue = option.getId() % 2 == 0 ? true : (option.getId() % 3 == 0 ? null : false);
        return new Vote(stubId, voteValue, new Date(), userId, option.getId());
    }


    public List<TLRPC.User> getUsers(int[] usersIds) {
        List<TLRPC.User> users = new ArrayList<>();
        for (int i = 0; i < usersIds.length; i++) {
            users.add(MessagesController.getInstance().getUser(usersIds[i]));
        }
        return users;
    }

    @Override
    public List<Vote> getVotes(long decisionId, @Nullable Integer userId) {
        List<Vote> votes = new ArrayList<>();
        for (Option c : getOptions(decisionId)) {
            votes.add(getVote(userId, c));
        }
        return votes;
    }

    @Override
    public Option save(Option o) {
        Option found = getOption(o.getTitle(), getDecision(o.getDecisionId()));
        return save(o, found, textOptions);
    }

    @Override
    public Vote save(Vote vote) {
        {
            Vote newVote = getVote(vote.getOptionId(), vote.getUserId());
            if (newVote == null) {
                votes.add(vote);
                vote.setId(votes.size());
                return vote;
            }
        }
        for (int i = 0; i < votes.size(); i++) {
            if (vote.getOptionId() == votes.get(i).getOptionId() &&
                    vote.getUserId() == votes.get(i).getUserId()) ;
            votes.get(i).setVoteTime(vote.getVoteTime());
            votes.get(i).setVote(vote.isVote());
            return votes.get(i);
        }
        return vote;
    }

    @Override
    public Decision getDecision(String decisionTitle, long chatId) {
        for (Decision d : decisions) {
            if (d.getTitle().equals(decisionTitle) && d.getChatId() == chatId)
                return d;
        }
        return null;
    }

    @Override
    public Option getOption(String optionTitle, Decision decision) {
        for (Option o : textOptions) {
            if (o.getTitle().equals(optionTitle) && o.getDecisionId() == decision.getId())
                return o;
        }
        return null;
    }

    @Override
    public Vote getVote(long optionId, int userId) {
        for (Vote v : votes) {
            if (v.getOptionId() == optionId && v.getUserId() == userId)
                return v;
        }
        return null;
    }

    @Override
    public void purgeData() {

    }

    @Override
    public void putStubData(int chatId, int creatorId) {

    }

    @Override
    public int getUserVoteCount(Decision decision) {
        return 0;
    }

    @Override
    public void delete(Decision decision) {

    }

    @Override
    public void delete(Option option) {
        for (int i = 0; i< textOptions.size() ; i++)
            if (textOptions.get(i).getId() == option.getId()){
                textOptions.remove(i);
                return;
        }
    }

    @Override
    public boolean hasBeenParsed(long groupChatId, int id) {
        return true;
    }

    @Override
    public ParsedMessage setMessageAsParsed(long groupChatId, int messageId, boolean parsedSuccessfully) {
        return new ParsedMessage(groupChatId,messageId,parsedSuccessfully);
    }

    @Override
    public List<ParsedMessage> getUnparsedMessages(long groupChatId) {
        return Arrays.asList();
    }

    @Override
    public WinningOption getWinningOption(Decision decision) {
        List<Option> options = getOptions(decision);
        int max = 0;
        return new WinningOption(max, options);
    }
}
