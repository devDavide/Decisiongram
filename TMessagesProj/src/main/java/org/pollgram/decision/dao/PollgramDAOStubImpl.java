package org.pollgram.decision.dao;

import android.support.annotation.Nullable;

import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.TextOption;
import org.pollgram.decision.data.UsersDecisionVotes;
import org.pollgram.decision.data.Vote;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by davide on 03/10/15.
 */
@Deprecated
class PollgramDAOStubImpl extends PollgramDAO {

    private static final String LOG_TAG = "DecisionDAOImpl";

    // TODO remove stub field
    private final List<Decision> decisions = new ArrayList<>();
    private final List<Option> choiches = new ArrayList<>();

    public PollgramDAOStubImpl() {

        long id = 1;
        /// just for test
        int chatId = 39379118;
        int creatorId = 23483618;
        decisions.add(new Decision(id++,chatId, creatorId,"what present do we buy ?", id+"huge bla bla bla" ,true ,1));
        decisions.add(new Decision(id++, chatId, creatorId, "Where do we go ?", id + "huge bla bla bla", true, 0));
        decisions.add(new Decision(id++, chatId, creatorId, "When will the party be ?", id + "huge bla bla bla", true, 0));
        decisions.add(new Decision(id++, chatId, creatorId, "Do we add Slomp to the group ?", id + "huge bla bla bla", false, 0));

        choiches.add(new TextOption(id++,"Ski","They cost 385EUR i saw them at the corner shop",decisions.get(0).getId()));
        choiches.add(new TextOption(id++,"Phone","The new StonexOne is AWESOME !!!",decisions.get(0).getId()));
        choiches.add(new TextOption(id++,"Trip", "Yeah a trip trought Europe can be a nice idea",decisions.get(0).getId()));
        choiches.add(new TextOption(id++, "A stupid idea", "it is late and i have no more ideas ;-/",decisions.get(0).getId()));

    }

    @Override
    public Decision insert(Decision d) {
        decisions.add(d);
        return d;
    }

    @Override
    public void update(Decision d) {

    }

    @Override
    public Decision getDecision(long decisionId) {
        for (Decision d : decisions){
            if (d.getId() == decisionId)
                return d;
        }
        return null;
    }

    @Override
    public List<Decision> getDecisions(@Nullable Boolean open) {
        List<Decision> outList = new ArrayList<>();
        for(Decision d : decisions)
            if (open == null || d.isOpen() == open.booleanValue())
                outList.add(d);
        return outList;
    }

    @Override
    public Option getOption(long optionId) {
        return null;
    }

    @Override
    public List<Option> getOptions(Decision decision) {
        return  getOptions(decision.getId());

    }

    @Override
    public List<Option> getOptions(long decisionId) {
        if (decisionId == decisions.get(0).getId())
            return  choiches;
        else
            return new ArrayList<>();
    }

    @Override
    public UsersDecisionVotes getUsersDecisionVotes(long decisionId, int[] participantIds) {
        Decision decision = getDecision(decisionId);
        List<TLRPC.User> usersForDecision = getUsers(participantIds);
        List<Option> choichesForDecision= getOptions(decision) ;
        List<Vote> votes = new ArrayList<>();
        for (int i = 0; i < usersForDecision.size(); i++) {
            for (int j = 0; j < choichesForDecision.size(); j++) {
                Vote vote = getVote(usersForDecision.get(i).id, choichesForDecision.get(j));
                votes.add(vote);
                if (i == 0) {
                    vote.setVote(null);
                }
            }
        }
        UsersDecisionVotes udv = new UsersDecisionVotes(decision, usersForDecision, choichesForDecision, votes);
        return udv;
    }

    private Vote getVote(int userId, Option option) {
        // TODO so stub
        long stubId = option.getId()*17;
        Boolean voteValue = option.getId()%2 ==0 ? true : (option.getId()%3 ==0 ? null : false);
        return new Vote(stubId, voteValue, new Date(), userId, option.getId());
    }

    @Override
    public UsersDecisionVotes getUsersDecisionVotes(long decisionId, List<TLRPC.User> users) {
        return null;
    }

    @Override
    public List<TLRPC.User> getUsers(int[] usersIds) {
        List<TLRPC.User> users = new ArrayList<>();
        for (int i = 0 ; i< usersIds.length ; i++){
            users.add(MessagesController.getInstance().getUser(usersIds[i]));
        }
        return users;
    }

    @Override
    public List<Vote> getUserVoteForDecision(long decisionId, int userId) {
        List<Vote> votes = new ArrayList<>();
        for (Option c : getOptions(decisionId)) {
             votes.add(getVote(userId,c));
        }
        return votes;
    }

    @Override
    public Option insert(Option o) {
        return null;
    }

    @Override
    public void update(Option o) {

    }

    @Override
    public Vote save(Vote vote) {
        return null;
    }
}

