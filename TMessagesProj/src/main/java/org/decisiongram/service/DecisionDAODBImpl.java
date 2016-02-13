package org.decisiongram.service;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import org.decisiongram.data.Decision;
import org.decisiongram.data.Option;
import org.decisiongram.data.ParsedMessage;
import org.decisiongram.data.TextOption;
import org.decisiongram.data.TimeRangeOption;
import org.decisiongram.data.Vote;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by davide on 18/10/15.
 */
class DecisionDAODBImpl implements DecisionDAO {

    private static final String LOG_TAG = "PGDBDAO";
    private final SqlLiteHelper helper;

    public DecisionDAODBImpl() {
        helper = new SqlLiteHelper();
    }

    @Override
    public void purgeData() {
        helper.getWritableDatabase().execSQL("DELETE FROM " + SqlLiteHelper.T_Decision.TABLE_NAME);
        helper.getWritableDatabase().execSQL("DELETE FROM " + SqlLiteHelper.T_TextOption.TABLE_NAME);
        helper.getWritableDatabase().execSQL("DELETE FROM " + SqlLiteHelper.T_Vote.TABLE_NAME);
    }

    @Override
    public void putStubData(int chatId, int creatorId) {
        // TODO remove it one day
        Log.i(LOG_TAG, "Put Stub test data");

        {
            Date creationDate = new Date();
            Decision decision1 = new Decision(chatId, creatorId, "what present do we buy ?", "huge bla bla bla", creationDate, true);

            List<Option> options = new ArrayList<>();
            options.add(new TextOption("Ski", "They cost 385EUR i saw them at the corner shop", decision1.getId()));
            options.add(new TextOption("Phone", "The new StonexOne is AWESOME !!!", decision1.getId()));
            options.add(new TextOption("Trip", "Yeah a trip trought Europe can be a nice idea", decision1.getId()));
            options.add(new TextOption("A stupid idea", "it is late and i have no more ideas ;-/", decision1.getId()));

            DecisiongramFactory.getService().notifyNewDecision(decision1, options);
        }

        {
            Date creationDate = new Date();
            Decision decision1 = new Decision(chatId, creatorId, "Where do we'd like to go skiing ?",
                    "Lorem ipsum dolor sit amet, vix te deserunt ullamcorper. Ut probatus dignissim sea, vocent discere vivendum ad mea. Eam ut blandit scribentur, ius an salutatus reprimique. Ut eros rationibus nec, ex deserunt invenire quo.\n" +
                            "Lorem ipsum dolor sit amet, vix te deserunt ullamcorper. Ut probatus dignissim sea, vocent discere vivendum ad mea. Eam ut blandit scribentur, ius an salutatus reprimique. Ut eros rationibus nec, ex deserunt invenire quo.\n" +
                            "Lorem ipsum dolor sit amet, vix te deserunt ullamcorper. Ut probatus dignissim sea, vocent discere vivendum ad mea. Eam ut blandit scribentur, ius an salutatus reprimique. Ut eros rationibus nec, ex deserunt invenire quo.\n" +
                            "\n" +
                            "Id nulla tacimates mandamus est, duo agam luptatum philosophia ex, wisi vidit reprehendunt quo ea. Ei sed omnis nostrum probatus, quis liber expetendis id sea. Pro id nibh recusabo, has suas volutpat cu. Copiosae detraxit petentium has ne. ", creationDate, true);

            List<Option> options = new ArrayList<>();
            options.add(new TextOption("Val di fiemme obereggen, ovvero pameago", "L'è sempre bel e ghe el park davert", decision1.getId()));
            options.add(new TextOption("Cortina d'ampezzo", "Lorem ipsum dolor sit amet, vix te deserunt ullamcorper. Ut probatus dignissim sea, vocent discere vivendum ad mea. Eam ut blandit scribentur, ius an salutatus reprimique. Ut eros rationibus nec, ex deserunt invenire quo.\n" +
                    "\n" +
                    "Id nulla tacimates mandamus est, duo agam luptatum philosophia ex, wisi vidit reprehendunt quo ea. Ei sed omnis nostrum probatus, quis liber expetendis id sea. Pro id nibh recusabo, has suas volutpat cu. Copiosae detraxit petentium has ne.", decision1.getId()));
            options.add(new TextOption("Le funivie del'ghiacciaio della valle di stubai che si trova in austria vicino ad innsbruck", "è un po lungo il viaggio ma potrebbe essere assai fico", decision1.getId()));
            options.add(new TextOption("Sul piste del passo del Broccon", null, decision1.getId()));

            DecisiongramFactory.getService().notifyNewDecision(decision1, options);
        }
    }

    @Override
    public int getUserVoteCount(Decision decision) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = null;
        try {
            c =db.rawQuery("Select count(*) FROM (" +
                            "SELECT v." + SqlLiteHelper.T_Vote.USER_ID + " " +
                            "FROM decision d inner join text_option o " +
                            "on d." + SqlLiteHelper.T_Decision.ID + " = o." + SqlLiteHelper.T_TextOption.FK_DECISION + " " +
                            "inner join vote v " +
                            "on o." + SqlLiteHelper.T_TextOption.ID + " = v." + SqlLiteHelper.T_Vote.FK_OPTION + " " +
                            "where d." + SqlLiteHelper.T_Decision.ID + " = ? " +
                            "group by " + SqlLiteHelper.T_Vote.USER_ID+
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
    public void delete(Option option) {
        Log.d(LOG_TAG, "Delete option ["+option+"]");
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] optionIdPar = new String[]{Long.toString(option.getId())};
        try {
            db.delete(SqlLiteHelper.T_Vote.TABLE_NAME,
                    SqlLiteHelper.T_Vote.FK_OPTION + " = ? ",
                    optionIdPar);
            db.delete(SqlLiteHelper.T_TextOption.TABLE_NAME,
                    SqlLiteHelper.T_TextOption.ID + " =  ? ",
                    optionIdPar);
        } finally {
            if (db != null)
                db.close();
        }
    }

    @Override
    public void delete(Decision decision) {
        Log.d(LOG_TAG, "Delete all decision data for decision["+decision+"]");
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] decisionIdPar = new String[]{Long.toString(decision.getId())};
        try {
            db.delete(SqlLiteHelper.T_Vote.TABLE_NAME,
                    SqlLiteHelper.T_Vote.FK_OPTION + " in  ( " +
                            "select " + SqlLiteHelper.T_TextOption.ID + " from " + SqlLiteHelper.T_TextOption.TABLE_NAME +
                            " where " + SqlLiteHelper.T_TextOption.FK_DECISION + "= ?)",
                    decisionIdPar);
            db.delete(SqlLiteHelper.T_TextOption.TABLE_NAME,
                    SqlLiteHelper.T_TextOption.FK_DECISION + " =  ? ",
                    decisionIdPar);
            db.delete(SqlLiteHelper.T_Decision.TABLE_NAME,
                    SqlLiteHelper.T_Decision.ID + " =  ? ",
                    decisionIdPar);
        } finally {
            if (db != null)
                db.close();
        }
    }

    @Override
    public WinningOption getWinningOption(Decision decision) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String voteCountFieldName= "vote_count";
        Cursor c = null;
        try {
            c =db.rawQuery("SELECT "+ SqlLiteHelper.T_TextOption.cloumns("o")+",  count (*) as "+voteCountFieldName+
                            " FROM decision d inner join text_option o " +
                            " on d." + SqlLiteHelper.T_Decision.ID + " = o." + SqlLiteHelper.T_TextOption.FK_DECISION + " " +
                            " inner join vote v " +
                            " on o." + SqlLiteHelper.T_TextOption.ID + " = v." + SqlLiteHelper.T_Vote.FK_OPTION + " " +
                            " where d." + SqlLiteHelper.T_Decision.ID + " = ? " +
                            " and "+ SqlLiteHelper.T_Vote.VOTE + " = ? " +
                            " group by " + SqlLiteHelper.T_Vote.FK_OPTION +
                            " having vote_count = (" +
                              " Select max(votes) FROM (\n" +
                                 " SELECT count (*) as  votes" +
                                " FROM decision d inner join text_option o " +
                                " on d." + SqlLiteHelper.T_Decision.ID + " = o." + SqlLiteHelper.T_TextOption.FK_DECISION + " " +
                                " inner join vote v " +
                                " on o." + SqlLiteHelper.T_TextOption.ID + " = v." + SqlLiteHelper.T_Vote.FK_OPTION + " " +
                                " where d." + SqlLiteHelper.T_Decision.ID + " = ? " +
                                " and "+ SqlLiteHelper.T_Vote.VOTE + " = ? " +
                                " group by " + SqlLiteHelper.T_Vote.FK_OPTION +
                                ") " +
                            ")",
                    new String[]{Long.toString(decision.getId()), SqlLiteHelper.toString(true),
                            Long.toString(decision.getId()), SqlLiteHelper.toString(true)});
            if (!c.moveToFirst())
                return new WinningOption(0, new ArrayList<Option>());
            else {
                List<Option> options = new ArrayList<>();
                int voteCount = c.getInt(c.getColumnIndex(voteCountFieldName));
                while (!c.isAfterLast()) {
                    options.add(helper.TEXT_OPTION_MAPPER.from(c));
                    c.moveToNext();
                }
                return new WinningOption(voteCount,options);
            }
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
    public List<Decision> getDecisions(long chatId, int decisionOwnerId) {
        String selection = SqlLiteHelper.T_Decision.GROUP_ID + " = ? " +
                " and " + SqlLiteHelper.T_Decision.DECISION_OWNER + " = ? " +
                " and " + SqlLiteHelper.T_Decision.OPEN + "= ? ";
        String[] selectionArgs = new String[]{Long.toString(chatId),
                Integer.toString(decisionOwnerId), SqlLiteHelper.toString(true)};

        return helper.query(helper.DECISION_MAPPER, selection, selectionArgs, null, null,
                SqlLiteHelper.T_Decision.CREATION_DATE) ;
    }

    @Override
    public List<Decision> getDecisions(long chatId, @Nullable Boolean open) {
        String selection = SqlLiteHelper.T_Decision.GROUP_ID + " = ? ";
        String[] selectionArgs;
        if (open != null) {
            selection = SqlLiteHelper.T_Decision.OPEN + "= ? and " + selection;
            selectionArgs = new String[]{SqlLiteHelper.toString(open), Long.toString(chatId)};
        } else {
            selectionArgs = new String[]{Long.toString(chatId)};
        }

        return helper.query(helper.DECISION_MAPPER, selection, selectionArgs, null, null,
                SqlLiteHelper.T_Decision.OPEN + " DESC, " + SqlLiteHelper.T_Decision.CREATION_DATE) ;
    }

    @Override
    public Option save(Option o) {
        if (o instanceof TimeRangeOption)
            throw new RuntimeException("Not yet supported :-(");

        TextOption tOpt = (TextOption) o;
        if (tOpt.getNotes() == null)// mask null values
            tOpt.setNotes("");
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
                SqlLiteHelper.T_TextOption.FK_DECISION + "= ?",
                new String[]{Long.toString(decisionId)});
        // TODO eventually query time range options

        List<Option> outList = new ArrayList<>();
        outList.addAll(textOptions);
        return outList;
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
        strQuery.append(" select ").append(SqlLiteHelper.T_Vote.cloumns("v"));
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
    public Decision getDecision(String decisionTitle, long chatId) {
        return helper.findFirst(helper.DECISION_MAPPER,
                SqlLiteHelper.T_Decision.TITLE + " = ? AND " + SqlLiteHelper.T_Decision.GROUP_ID + " = ?",
                new String[]{decisionTitle, Long.toString(chatId)});
    }

    @Override
    public Option getOption(String optionTitle, Decision decision) {
        return getOption(optionTitle, decision.getId());
    }

    private Option getOption(String optionTitle, long decisionId) {
        return helper.findFirst(helper.TEXT_OPTION_MAPPER,
                SqlLiteHelper.T_TextOption.TITLE + " = ? AND " + SqlLiteHelper.T_TextOption.FK_DECISION + " = ?",
                new String[]{optionTitle, Long.toString(decisionId)});
    }

    @Override
    public Vote getVote(long optionId, int userId) {
        return helper.findFirst(helper.VOTE_MAPPER,
                SqlLiteHelper.T_Vote.FK_OPTION + " = ? AND " + SqlLiteHelper.T_Vote.USER_ID + " = ?",
                new String[]{Long.toString(optionId), Integer.toString(userId)});
    }

    @Override
    public boolean hasBeenParsed(final long groupChatId, int messageId) {
        ParsedMessage pm = getParseMessage(groupChatId,messageId);
        return pm == null ? false : pm.isParsedSuccessfully();
    }

    private  ParsedMessage getParseMessage(final long groupChatId, int messageId){
        return helper.findFirst(SqlLiteHelper.PARSED_MESSAGES_MAPPER,
                SqlLiteHelper.T_ParsedMessages.GROUP_ID + " = ? and " +
                        SqlLiteHelper.T_ParsedMessages.MESSAGE_ID + " = ? ",
                new String[]{Long.toString(groupChatId), Integer.toString(messageId)});
    }

    @Override
    public ParsedMessage setMessageAsParsed(final long groupChatId, int messageId, boolean parsedSuccessfully) {
        ParsedMessage newValue = new ParsedMessage(groupChatId,messageId, parsedSuccessfully);
        return save(newValue);
    }

    private ParsedMessage save(ParsedMessage newValue){
        ParsedMessage foundParseMessage = getParseMessage(newValue.getGroupId(),newValue.getMessageId());
        if (foundParseMessage == null)
            return helper.insert(newValue, helper.PARSED_MESSAGES_MAPPER);
        else{
            newValue.setId(foundParseMessage.getId());
            helper.update(newValue, helper.PARSED_MESSAGES_MAPPER);
            return newValue;
        }
    }

    @Override
    public List<ParsedMessage> getUnparsedMessages(final long groupChatId) {
        return helper.query(SqlLiteHelper.PARSED_MESSAGES_MAPPER,
                SqlLiteHelper.T_ParsedMessages.GROUP_ID + " = ? and " +
                        SqlLiteHelper.T_ParsedMessages.PARSED_SUCCESSFULLY + " = ? ",
                new String[]{Long.toString(groupChatId), SqlLiteHelper.toString(false)});

    }
}

