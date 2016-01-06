package org.pollgram.decision.service;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.ParsedMessage;
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

        {
            Date creationDate = new Date();
            Decision decision1 = new Decision(chatId, creatorId, "what present do we buy ?", "huge bla bla bla", creationDate, true);

            List<Option> options = new ArrayList<>();
            options.add(new TextOption("Ski", "They cost 385EUR i saw them at the corner shop", decision1.getId()));
            options.add(new TextOption("Phone", "The new StonexOne is AWESOME !!!", decision1.getId()));
            options.add(new TextOption("Trip", "Yeah a trip trought Europe can be a nice idea", decision1.getId()));
            options.add(new TextOption("A stupid idea", "it is late and i have no more ideas ;-/", decision1.getId()));

            PollgramFactory.getPollgramService().notifyNewDecision(decision1, options);
        }

        {
            Date creationDate = new Date();
            Decision decision1 = new Decision(chatId, creatorId, "Where do we'd like to go skiing ?",
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

            PollgramFactory.getPollgramService().notifyNewDecision(decision1, options);
        }
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
        String voteCountFieldName= "vote_count";
        Cursor c = null;
        try {
            c =db.rawQuery("SELECT "+PGSqlLiteHelper.T_TextOption.cloumns("o")+",  count (*) as "+voteCountFieldName+
                            " FROM decision d inner join text_option o " +
                            " on d." + PGSqlLiteHelper.T_Decision.ID + " = o." + PGSqlLiteHelper.T_TextOption.FK_DECISION + " " +
                            " inner join vote v " +
                            " on o." + PGSqlLiteHelper.T_TextOption.ID + " = v." + PGSqlLiteHelper.T_Vote.FK_OPTION + " " +
                            " where d." + PGSqlLiteHelper.T_Decision.ID + " = ? " +
                            " and "+ PGSqlLiteHelper.T_Vote.VOTE + " = ? " +
                            " group by " + PGSqlLiteHelper.T_Vote.FK_OPTION +
                            " having vote_count = (" +
                              " Select max(votes) FROM (\n" +
                                 " SELECT count (*) as  votes" +
                                " FROM decision d inner join text_option o " +
                                " on d." + PGSqlLiteHelper.T_Decision.ID + " = o." + PGSqlLiteHelper.T_TextOption.FK_DECISION + " " +
                                " inner join vote v " +
                                " on o." + PGSqlLiteHelper.T_TextOption.ID + " = v." + PGSqlLiteHelper.T_Vote.FK_OPTION + " " +
                                " where d." + PGSqlLiteHelper.T_Decision.ID + " = ? " +
                                " and "+ PGSqlLiteHelper.T_Vote.VOTE + " = ? " +
                                " group by " + PGSqlLiteHelper.T_Vote.FK_OPTION +
                                ") " +
                            ")",
                    new String[]{Long.toString(decision.getId()), PGSqlLiteHelper.toString(true),
                            Long.toString(decision.getId()), PGSqlLiteHelper.toString(true)});
            if (!c.moveToFirst())
                return null;
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
    public List<Decision> getDecisions(long chatId, @Nullable Boolean open) {
        String selection = PGSqlLiteHelper.T_Decision.GROUP_ID + " = ? ";
        String[] selectionArgs;
        if (open != null) {
            selection = PGSqlLiteHelper.T_Decision.OPEN + "= ? and " + selection;
            selectionArgs = new String[]{PGSqlLiteHelper.toString(open), Long.toString(chatId)};
        } else {
            selectionArgs = new String[]{Long.toString(chatId)};
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
    public Decision getDecision(String decisionTitle, long chatId) {
        return helper.findFirst(helper.DECISION_MAPPER,
                PGSqlLiteHelper.T_Decision.TITLE + " = ? AND " + PGSqlLiteHelper.T_Decision.GROUP_ID + " = ?",
                new String[]{decisionTitle, Long.toString(chatId)});
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

    @Override
    public boolean hasBeenParsed(final long groupChatId, int messageId) {
        ParsedMessage pm = getParseMessage(groupChatId,messageId);
        return pm == null ? false : pm.isParsedSuccessfully();
    }

    private  ParsedMessage getParseMessage(final long groupChatId, int messageId){
        return helper.findFirst(PGSqlLiteHelper.PARSED_MESSAGES_MAPPER,
                PGSqlLiteHelper.T_ParsedMessages.GROUP_ID + " = ? and " +
                        PGSqlLiteHelper.T_ParsedMessages.MESSAGE_ID + " = ? ",
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
        return helper.query(PGSqlLiteHelper.PARSED_MESSAGES_MAPPER,
                PGSqlLiteHelper.T_ParsedMessages.GROUP_ID + " = ? and " +
                        PGSqlLiteHelper.T_ParsedMessages.PARSED_SUCCESSFULLY + " = ? ",
                new String[]{Long.toString(groupChatId), PGSqlLiteHelper.toString(false)});

    }
}

