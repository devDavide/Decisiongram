package org.pollgram.decision.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import org.pollgram.decision.dao.PGSqlLiteHelper.T_Decision;
import org.pollgram.decision.dao.PGSqlLiteHelper.T_TextOption;
import org.pollgram.decision.dao.PGSqlLiteHelper.T_Vote;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.TextOption;
import org.pollgram.decision.data.TimeRangeOption;
import org.pollgram.decision.data.UsersDecisionVotes;
import org.pollgram.decision.data.Vote;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by davide on 18/10/15.
 */
class PollgramDAODBImpl extends PollgramDAO {

    private static final String LOG_TAG = "PGDBDAO";
    private final PGSqlLiteHelper helper;

    public PollgramDAODBImpl() {
        helper = new PGSqlLiteHelper();
        putStubData();
    }

    private void putStubData() {


        /// just for test
        int chatId = 39379118;
        int creatorId = 23483618;
        List<Decision> decisions = new ArrayList<>();
        decisions.add(new Decision(chatId, creatorId, "what present do we buy ?", "huge bla bla bla", true, 0));
        decisions.add(new Decision(chatId, creatorId, "Where do we go ?", "huge bla bla bla", true, 0));
        decisions.add(new Decision(chatId, creatorId, "When will the party be ?", "huge bla bla bla", true, 0));
        decisions.add(new Decision(chatId, creatorId, "Do we add Slomp to the group ?", "huge bla bla bla", false, 0));
        helper.getWritableDatabase().execSQL("DELETE FROM " + T_Decision.TABLE_NAME);
        for (Decision d : decisions) {
            Decision newD = insert(d);
            Log.i(LOG_TAG, "inserted decision id:" + newD.getId());
            Decision found = getDecision(newD.getId());
        }
        Log.i(LOG_TAG, "query getDecisions(null)");
        for (Decision d : getDecisions(null)) {
            Log.d(LOG_TAG, "found-1: " + d);
        }

        Log.i(LOG_TAG, "query getDecisions(true)");
        for (Decision d : getDecisions(true)) {
            Log.d(LOG_TAG, "found-2: " + d);
        }
        Log.i(LOG_TAG, "query getDecisions(false)");
        for (Decision d : getDecisions(false)) {
            Log.d(LOG_TAG, "found-3 " + d);
        }

        Decision decision1 = getDecisions(null).get(0);
        List<TextOption> options = new ArrayList<>();
        options.add(new TextOption("Ski", "They cost 385EUR i saw them at the corner shop", decision1.getId()));
        options.add(new TextOption("Phone", "The new StonexOne is AWESOME !!!", decision1.getId()));
        options.add(new TextOption("Trip", "Yeah a trip trought Europe can be a nice idea", decision1.getId()));
        options.add(new TextOption("A stupid idea", "it is late and i have no more ideas ;-/", decision1.getId()));
        helper.getWritableDatabase().execSQL("DELETE FROM " + T_TextOption.TABLE_NAME);
        for(TextOption te : options){
            Option newOpt = insert(te);
            Log.i(LOG_TAG, "inserted TextOption id:" + newOpt.getId());
            Option found = getOption(newOpt.getId());
        }

        Log.i(LOG_TAG, "query getOptions(decision1.getId())");
        List<Option> optionsQuery = getOptions(decision1.getId());
        for (int i=0; i< optionsQuery.size() ; i++ ) {
            Option o = optionsQuery.get(i);
            Log.d(LOG_TAG, "optionFound " + o);
            Vote v = new Vote(null, new Date(), 93880097, o.getId());
            Vote newV = save(v);
            Log.d(LOG_TAG, "insert vote: " + v);
        }
    }

    @Override
    public Decision insert(Decision d) {
        return helper.insert(d, helper.DECISION_MAPPER);
    }

    @Override
    public void update(Decision d) {
        helper.update(d, helper.DECISION_MAPPER);
    }

    @Override
    public Decision getDecision(long decisionId) {
        return helper.findById(decisionId, helper.DECISION_MAPPER);
    }

    @Override
    public List<Decision> getDecisions(@Nullable Boolean open) {
        if (open == null)
            return helper.query(helper.DECISION_MAPPER, null, null);
        else
            return helper.query(helper.DECISION_MAPPER, T_Decision.OPEN + "= ?",
                    new String[]{PGSqlLiteHelper.toString(open)});
    }

    @Override
    public Option insert(Option o) {
        if (o instanceof TimeRangeOption)
            throw new RuntimeException("Not yet supported :-(");
        else {
            TextOption tOpt = (TextOption) o;
            return helper.insert(tOpt, helper.TEXT_OPTION_MAPPER);
        }
    }

    @Override
    public void update(Option o) {
        if (o instanceof TimeRangeOption)
            throw new RuntimeException("Not yet supported :-(");
        else {
            TextOption tOpt = (TextOption) o;
            helper.update(tOpt, helper.TEXT_OPTION_MAPPER);
        }
    }

    @Override
    public Option getOption(long optionId) {
        return helper.findById(optionId, helper.TEXT_OPTION_MAPPER);
    }

    @Override
    public List<Option> getOptions(Decision decision) {
        return getOptions(decision.getId());
    }

    @Override
    public List<Option> getOptions(long decisionId) {
        List<TextOption> textOptions = helper.query(helper.TEXT_OPTION_MAPPER,
                T_TextOption.FK_DECISION + "= ?",
                new String[]{Long.toString(decisionId)});
        // TODO eventually query time range options

        List<Option> outList = new ArrayList<>();
        outList.addAll(textOptions);
        return outList;
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
        return getVote(decisionId, userId);
    }

    /**
     * Votes for given decisionId and userId. UserId can be null, in this case will reurn the
     * votes for any user
     * @param decisionId decision
     * @param userId userId, if null it means all user
     * @return
     */
    private List<Vote> getVote(long decisionId, @Nullable Integer userId) {
        SQLiteDatabase db = helper.getReadableDatabase();

        List<String> params = new ArrayList<>(2);
        params.add(Long.toString(decisionId));

        StringBuilder strQuery =  new StringBuilder();
        strQuery.append(" select ").append(T_Vote.cloumns("v"));
        strQuery.append(" from text_option o inner join vote v ");
        strQuery.append("  on o.id = v.fk_option ");
        strQuery.append(" where o.fk_decision = ? ");
        if (userId != null) {
            strQuery.append(" and v.user_id = ? ");
            params.add(Integer.toString(userId));
        }
        try {
            List<Vote> result = new ArrayList<>();
            Cursor cursor = db.rawQuery(strQuery.toString(),params.toArray(new String[params.size()]));
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                result.add(helper.VOTE_MAPPER.from(cursor));
                cursor.moveToNext();
            }
            return result;
        } finally {
            if (db != null && db.isOpen())
                db.close();
        }
    }

    @Override
    public Vote save(Vote vote) {
        if (vote.getId() == Vote.ID_NOT_SET) {
            return helper.insert(vote, helper.VOTE_MAPPER);
        } else {
            helper.update(vote, helper.VOTE_MAPPER);
            return vote;
        }
    }


    @Override
    public UsersDecisionVotes getUsersDecisionVotes(long decisionId, int[] participantIds) {
        Decision decision = getDecision(decisionId);
        List<TLRPC.User> users = getUsers(participantIds);
        List<Option> options = getOptions(decisionId);
        List<Vote> votes = getVote(decisionId, null);
        UsersDecisionVotes udv = new UsersDecisionVotes(decision, users, options, votes);
        return udv;
    }
}

