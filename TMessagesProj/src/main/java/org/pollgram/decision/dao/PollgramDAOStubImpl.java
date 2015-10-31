package org.pollgram.decision.dao;

import android.support.annotation.Nullable;
import android.util.Log;

import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.TextOption;
import org.pollgram.decision.data.UsersDecisionVotes;
import org.pollgram.decision.data.Vote;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by davide on 03/10/15.
 */
public class PollgramDAOStubImpl extends PollgramDAO {

    private static final String LOG_TAG = "DecisionDAOImpl";

    // TODO remove stub field
    private final List<Decision> decisions = new ArrayList<>();
    private final List<Option> choiches = new ArrayList<>();

    public PollgramDAOStubImpl() {

        long id = 1;
        /// just for test
        int chatId = 39379118;
        int creatorId = 23483618;
        decisions.add(new Decision(id++,chatId, creatorId,"what present do we buy ?",true, 1));
        decisions.add(new Decision(id++,chatId, creatorId,"Where do we go ?",true, 0));
        decisions.add(new Decision(id++,chatId, creatorId,"When will the party be ?",true, 0));
        decisions.add(new Decision(id++,chatId, creatorId,"Do we add Slomp to the group ?",false, 0));

        choiches.add(new TextOption(id++, 5,2, decisions.get(0),"Ski","They cost 385EUR i saw them at the corner shop"));
        choiches.add(new TextOption(id++, 3,4, decisions.get(0),"Phone","The new StonexOne is AWESOME !!!"));
        choiches.add(new TextOption(id++,2,5, decisions.get(0), "Trip", "Yeah a trip trought Europe can be a nice idea"));
        choiches.add(new TextOption(id++,2,5, decisions.get(0), "A stupid idea", "it is late and i have no more ideas ;-/"));

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
        UsersDecisionVotes udv = new UsersDecisionVotes(decision, usersForDecision, choichesForDecision);
        for (int i = 0; i < usersForDecision.size(); i++) {
            for (int j = 0; j < choichesForDecision.size(); j++) {
                Vote vote = getVote(usersForDecision.get(i).id, choichesForDecision.get(j));
                udv.setVote(i,j,vote);
                if (i == 0)
                    vote.setVote(null);
            }
        }
        return udv;
    }

    private Vote getVote(int userId, Option option) {
        // TODO so stub
        long stubId = option.getId()*17;
        Boolean voteValue = option.getId()%2 ==0 ? true : (option.getId()%3 ==0 ? null : false);
        return new Vote(stubId, option,userId, voteValue, new Date());
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
    public void save(Collection<Vote> votest2save) {
        Log.e(LOG_TAG, "save not yet implemnted :-(");
    }
}

