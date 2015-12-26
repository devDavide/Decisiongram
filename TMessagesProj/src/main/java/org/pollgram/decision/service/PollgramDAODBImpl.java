package org.pollgram.decision.service;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.TextOption;
import org.pollgram.decision.data.TimeRangeOption;
import org.pollgram.decision.data.Vote;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by davide on 18/10/15.
 */
class PollgramDAODBImpl implements PollgramDAO {

    private static final String LOG_TAG = "PGDBDAO";
    private final PGSqlLiteHelper helper;

    public PollgramDAODBImpl() {
        helper = new PGSqlLiteHelper();
    }

    @Override
    public void purgeData() {
        helper.getWritableDatabase().execSQL("DELETE FROM " + PGSqlLiteHelper.T_Decision.TABLE_NAME);
        helper.getWritableDatabase().execSQL("DELETE FROM " + PGSqlLiteHelper.T_TextOption.TABLE_NAME);
        helper.getWritableDatabase().execSQL("DELETE FROM " + PGSqlLiteHelper.T_Vote.TABLE_NAME);
    }

    @Override
    public void putStubData(int chatId, int creatorId) {
        // TODO remove it one day
        Log.i(LOG_TAG, "Put Stub test data");

        Date creationDate = new Date();
        Decision d =  new Decision(chatId, creatorId, "what present do we buy ?", "huge bla bla bla", creationDate, true);

        Decision decision1 = getDecisions(chatId, null).get(0);
        List<Option> options = new ArrayList<>();
        options.add(new TextOption("Ski", "They cost 385EUR i saw them at the corner shop", decision1.getId()));
        options.add(new TextOption("Phone", "The new StonexOne is AWESOME !!!", decision1.getId()));
        options.add(new TextOption("Trip", "Yeah a trip trought Europe can be a nice idea", decision1.getId()));
        options.add(new TextOption("A stupid idea", "it is late and i have no more ideas ;-/", decision1.getId()));

        PollgramFactory.getPollgramService().notifyNewDecision(d, options);
    }

    @Override
    public int getUserVoteCount(Decision decision) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = null;
        try {
            c =db.rawQuery("Select count(*) FROM (" +
                            "SELECT v." + PGSqlLiteHelper.T_Vote.USER_ID + " " +
                            "FROM decision d inner join text_option o " +
                            "on d." + PGSqlLiteHelper.T_Decision.ID + " = o." + PGSqlLiteHelper.T_TextOption.FK_DECISION + " " +
                            "inner join vote v " +
                            "on o." + PGSqlLiteHelper.T_TextOption.ID + " = v." + PGSqlLiteHelper.T_Vote.FK_OPTION + " " +
                            "where d." + PGSqlLiteHelper.T_Decision.ID + " = ? " +
                            "group by " + PGSqlLiteHelper.T_Vote.USER_ID+
                            ")",
                    new String[]{Long.toString(decision.getId())});
            if (!c.moveToFirst())
                return  0;
            return c.getInt(0);
        } finally {
            if (db != null)
                db.close();
            if (c != null)
                c.close();
        }
    }

    @Override
    public void delete(Decision decision) {
        Log.d(LOG_TAG, "Delete all decision data for decision["+decision+"]");
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] decisionIdPar = new String[]{Long.toString(decision.getId())};
        try {
            db.delete(PGSqlLiteHelper.T_Vote.TABLE_NAME,
                    PGSqlLiteHelper.T_Vote.FK_OPTION + " in  ( " +
                            "select " + PGSqlLiteHelper.T_TextOption.ID + " from " + PGSqlLiteHelper.T_TextOption.TABLE_NAME +
                            " where " + PGSqlLiteHelper.T_TextOption.FK_DECISION + "= ?)",
                    decisionIdPar);
            db.delete(PGSqlLiteHelper.T_TextOption.TABLE_NAME,
                    PGSqlLiteHelper.T_TextOption.FK_DECISION + " =  ? ",
                    decisionIdPar);
            db.delete(PGSqlLiteHelper.T_Decision.TABLE_NAME,
                    PGSqlLiteHelper.T_Decision.ID + " =  ? ",
                    decisionIdPar);
        } finally {
            if (db != null)
                db.close();
        }
    }

    @Override
    public WinningOption getWinningOption(Decision decision) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String voteCountFieldName= "max_vote_count";
        Cursor c = null;
        try {
            c =db.rawQuery("SELECT "+PGSqlLiteHelper.T_TextOption.cloumns(null)+" , max(vote_count) as "+voteCountFieldName+" FROM (" +
                            "SELECT "+PGSqlLiteHelper.T_TextOption.cloumns("o")+",  count (*) as vote_count " +
                            "FROM decision d inner join text_option o " +
                            "on d." + PGSqlLiteHelper.T_Decision.ID + " = o." + PGSqlLiteHelper.T_TextOption.FK_DECISION + " " +
                            "inner join vote v " +
                            "on o." + PGSqlLiteHelper.T_TextOption.ID + " = v." + PGSqlLiteHelper.T_Vote.FK_OPTION + " " +
                            "where d." + PGSqlLiteHelper.T_Decision.ID + " = ? " +
                            "and "+ PGSqlLiteHelper.T_Vote.VOTE + " = ? " +
                            "group by " + PGSqlLiteHelper.T_Vote.FK_OPTION+
                            ")",
                    new String[]{Long.toString(decision.getId()), PGSqlLiteHelper.toString(true)});
            if (!c.moveToFirst())
                return null;
            else
              return new WinningOption(c.getInt(c.getColumnIndex(voteCountFieldName)),
                      helper.TEXT_OPTION_MAPPER.from(c));
        } finally {
            if (db != null)
                db.close();
            if (c != null)
                c.close();
        }
    }

    @Override
    public Decision save(Decision d) {
        Decision foundDecision = getDecision(d.getTitle(), d.getChatId());
        if (foundDecision == null)
            return helper.insert(d, helper.DECISION_MAPPER);
        else{
            d.setId(foundDecision.getId());
            helper.update(d, helper.DECISION_MAPPER);
            return d;
        }
    }

    @Override
    public Decision getDecision(long decisionId) {
        return helper.findById(decisionId, helper.DECISION_MAPPER);
    }

    @Override
    public List<Decision> getDecisions(int chatId, @Nullable Boolean open) {
        String selection = PGSqlLiteHelper.T_Decision.FULL_CHAT_ID + " = ? ";
        String[] selectionArgs;
        if (open != null) {
            selection = PGSqlLiteHelper.T_Decision.OPEN + "= ? and " + selection;
            selectionArgs = new String[]{PGSqlLiteHelper.toString(open), Integer.toString(chatId)};
        } else {
            selectionArgs = new String[]{Integer.toString(chatId)};
        }

        return helper.query(helper.DECISION_MAPPER, selection, selectionArgs, null, null,
                PGSqlLiteHelper.T_Decision.OPEN + " DESC, " + PGSqlLiteHelper.T_Decision.CREATION_DATE) ;
    }

    @Override
    public Option save(Option o) {
        if (o instanceof TimeRangeOption)
            throw new RuntimeException("Not yet supported :-(");

        TextOption tOpt = (TextOption) o;
        if (tOpt.getLongDescription() == null)// mask null values
            tOpt.setLongDescription("");
        Option foundOption = getOption(tOpt.getTitle(), o.getDecisionId());
        if (foundOption == null)
            return helper.insert(tOpt, helper.TEXT_OPTION_MAPPER);
        else {
            tOpt.setId(foundOption.getId());
            helper.update(tOpt, helper.TEXT_OPTION_MAPPER);
            return tOpt;
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
                PGSqlLiteHelper.T_TextOption.FK_DECISION + "= ?",
                new String[]{Long.toString(decisionId)});
        // TODO eventually query time range options

        List<Option> outList = new ArrayList<>();
        outList.addAll(textOptions);
        return outList;
    }

    @Override
    public List<Vote> getUserVoteForDecision(long decisionId, int userId) {
        return getVotes(decisionId, userId);
    }

    /**
     * Votes for given decisionId and userId. UserId can be null, in this case will reurn the
     * votes for any user
     * @param decisionId decision
     * @param userId userId, if null it means all user
     * @return
     */
    public List<Vote> getVotes(long decisionId, @Nullable Integer userId) {
        SQLiteDatabase db = helper.getReadableDatabase();

        List<String> params = new ArrayList<>(2);
        params.add(Long.toString(decisionId));

        StringBuilder strQuery =  new StringBuilder();
        strQuery.append(" select ").append(PGSqlLiteHelper.T_Vote.cloumns("v"));
        strQuery.append(" from text_option o inner join vote v ");
        strQuery.append("  on o.id = v.fk_option ");
        strQuery.append(" where o.fk_decision = ? ");
        if (userId != null) {
            strQuery.append(" and v.user_id = ? ");
            params.add(Integer.toString(userId));
        }
        Cursor cursor = null;
        try {
            List<Vote> result = new ArrayList<>();
            cursor = db.rawQuery(strQuery.toString(),params.toArray(new String[params.size()]));
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                result.add(helper.VOTE_MAPPER.from(cursor));
                cursor.moveToNext();
            }
            return result;
        } finally {
            if (db != null && db.isOpen())
                db.close();
            if (cursor != null)
                cursor.close();
        }
    }

    @Override
    public Vote save(Vote vote) {
        Vote foundVote = getVote(vote.getOptionId(), vote.getUserId());
        if (foundVote == null) {
            return helper.insert(vote, helper.VOTE_MAPPER);
        } else if (!vote.equals(foundVote)){
            vote.setId(foundVote.getId());
            helper.update(vote, helper.VOTE_MAPPER);
        } else {
            Log.d(LOG_TAG, "This vote ["+vote+"] is already saved ["+foundVote+"], nothing to do");
        }
        return vote;
    }

    @Override
    public Decision getDecision(String decisionTitle, int chatId) {
        return helper.findFirst(helper.DECISION_MAPPER,
                PGSqlLiteHelper.T_Decision.TITLE + " = ? AND " + PGSqlLiteHelper.T_Decision.FULL_CHAT_ID + " = ?",
                new String[]{decisionTitle, Integer.toString(chatId)});
    }

    @Override
    public Option getOption(String optionTitle, Decision decision) {
        return getOption(optionTitle, decision.getId());
    }

    private Option getOption(String optionTitle, long decisionId) {
        return helper.findFirst(helper.TEXT_OPTION_MAPPER,
                PGSqlLiteHelper.T_TextOption.TITLE + " = ? AND " + PGSqlLiteHelper.T_TextOption.FK_DECISION + " = ?",
                new String[]{optionTitle, Long.toString(decisionId)});
    }

    @Override
    public Vote getVote(long optionId, int userId) {
        return helper.findFirst(helper.VOTE_MAPPER,
                PGSqlLiteHelper.T_Vote.FK_OPTION + " = ? AND " + PGSqlLiteHelper.T_Vote.USER_ID + " = ?",
                new String[]{Long.toString(optionId), Integer.toString(userId)});
    }

}

