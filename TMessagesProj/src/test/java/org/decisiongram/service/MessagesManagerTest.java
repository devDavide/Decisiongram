package org.decisiongram.service;

import android.content.Context;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.decisiongram.data.Decision;
import org.decisiongram.data.Option;
import org.decisiongram.data.TextOption;
import org.decisiongram.data.Vote;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.tgnet.TLRPC;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by davide on 21/11/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class MessagesManagerTest {

    @Mock
    Context mMockContext;

    private DecisionDAOTestImpl dao;
    private MessagesManagerImpl messageManager;
    private DecisionServiceImpl service;
    private Map<Long, String> receivedId2LastMessages = new HashMap<>();
    private TLRPC.Chat chat;
    private TLRPC.User user;
    private Decision decision;

    @Before
    public void setUp() {
        ApplicationLoader.applicationContext = mMockContext;

        chat = new TLRPC.Chat();
        chat.id = 39379118;

        user = new TLRPC.User();
        user.id = 93880097;

        this.dao = new DecisionDAOTestImpl(chat.id, user.id);
        messageManager = new MessagesManagerImpl(dao);
        this.service = new DecisionServiceImpl(dao, messageManager);

        decision = dao.getDecisions(chat.id, null).get(0);

    }

    @Test
    public void testMessageTypeDifferentEmoji() {
        for (MessagesManager.MessageType mt1 : MessagesManager.MessageType.values()) {
            for (MessagesManager.MessageType mt2 : MessagesManager.MessageType.values()) {
                if (mt1.equals(mt2))
                    Assert.assertTrue(mt1.getEmoji().equals(mt2.getEmoji()));
                else
                    Assert.assertFalse(mt1.getEmoji().equals(mt2.getEmoji()));
            }
        }
    }

    @Test
    public void testMessageTypeGetEmoji() {
        for (MessagesManager.MessageType mt1 : MessagesManager.MessageType.values()) {
            Assert.assertEquals(mt1, MessagesManager.MessageType.byEmoji(mt1.getEmoji()));
        }
    }

    @Test
    public void testBuildMessage() {
        for (MessagesManager.MessageType mt1 : MessagesManager.MessageType.values()) {
            String msg = messageManager.buildMessage(mt1, "Message Body");
            MessagesManager.MessageType  type = messageManager.getMessageType(msg);
            Assert.assertEquals(mt1, type);
        }
    }

    @Test
    public void testSendVotes() throws ParseException, MessageParseException {
        List<Option> options = dao.getOptions(decision);
        Collection<Vote> votes = new ArrayList<>();
        Date voteDate = new Date();
        for (int i = 0; i < options.size(); i++) {
            Vote v = new Vote(i % 2 == 0, voteDate, user.id, options.get(i).getId());
            votes.add(v);
        }
        String message = messageManager.buildNotifyVoteMessage(decision, votes);
        MessagesManager.MessageType type = messageManager.getMessageType(message);
        Assert.assertEquals(type, MessagesManager.MessageType.VOTE);

        assertVotes(votes, voteDate, message);
        assertVotes(votes, voteDate, messageManager.reformatMessage(message));
    }

    private void assertVotes(Collection<Vote> votes, Date voteDate, String message) throws MessageParseException {
        Collection<Vote> parsedVotes = messageManager.getVotes(message, chat.id, voteDate, user.id);
        Assert.assertEquals(parsedVotes, votes);
    }

    @Test
    public void testSendRemind() throws ParseException {
        String message = messageManager.buildRemindMessage("UserName String", decision);
        MessagesManager.MessageType type = messageManager.getMessageType(message);
        Assert.assertEquals(type, MessagesManager.MessageType.REMIND_TO_VOTE);
    }

    @Test
    public void testDecision1() throws MessageParseException {
        testNewDecision(dao.getDecision(DecisionDAOTestImpl.DECISION_ID_1));
    }

    @Test
    public void testDecisionEmptyLongDescription() throws MessageParseException {
        testNewDecision(dao.getDecision(DecisionDAOTestImpl.DECISION_ID_EMPTY_LONG_DESC));
    }

    @Test
    public void testDecisionNullLongDescription() throws MessageParseException {
        testNewDecision(dao.getDecision(DecisionDAOTestImpl.DECISION_ID_NULL_LONG_DESC));
    }

    @Test
    public void testDecisionMultilineLongDescription() throws MessageParseException {
        testNewDecision(dao.getDecision(DecisionDAOTestImpl.DECISION_ID_MULTILINE_LONG_DESC));
    }

    @Test
    public void testDecisionQuotedLongDescription() throws MessageParseException {
        testNewDecision(dao.getDecision(DecisionDAOTestImpl.DECISION_ID_QUOTED_LONG_DESC));
    }

    private void testNewDecision(Decision decision) throws MessageParseException {
        if (decision == null)
            throw new InvalidParameterException("decision can not be null");

        if (!decision.isOpen())
            throw new InvalidParameterException("decision can not be closed");

        List<Option> options = dao.getOptions(decision);
        String message = messageManager.buildNotifyNewDecision(decision, options);

        MessagesManager.MessageType type = messageManager.getMessageType(message);
        Assert.assertEquals(type, MessagesManager.MessageType.NEW_DECISION);

        assertNewDecision(decision, options, message);
        assertNewDecision(decision, options, messageManager.reformatMessage(message));
    }

    private void assertNewDecision(Decision decision, List<Option> options, String message) throws MessageParseException {
        MessagesManager.DecisionOptionData result = messageManager.getNewDecision(message, chat.id, user.id, new Date());
        Assert.assertEquals(decision, result.decision);
        Assert.assertEquals(options, result.optionList);
    }

    @Test
    public void testCloseDecisionMultipleWinningOptions() throws MessageParseException {
        List<Option> winningOptions = dao.getOptions(decision);
        int voteCount = 5;
        String message = messageManager.buildCloseDecision(decision, winningOptions, voteCount);
        Assert.assertEquals(messageManager.getMessageType(message), MessagesManager.MessageType.CLOSE_DECISION);

        assertCloseDecision(decision, winningOptions, message);
        assertCloseDecision(decision, winningOptions, messageManager.reformatMessage(message));
    }

    @Test
    public void testCloseDecisionSingleWinningOptions() throws MessageParseException {
        List<Option> winningOptions = Arrays.asList(dao.getOptions(decision).get(0));
        int voteCount = 5;
        String message = messageManager.buildCloseDecision(decision, winningOptions, voteCount);
        Assert.assertEquals(messageManager.getMessageType(message), MessagesManager.MessageType.CLOSE_DECISION);

        assertCloseDecision(decision, winningOptions, message);
        assertCloseDecision(decision, winningOptions, messageManager.reformatMessage(message));
    }

    private void assertCloseDecision(Decision decision, List<Option> winningOption, String message) throws MessageParseException {
        MessagesManager.ClosedDecisionDate result = messageManager.getCloseDecision(message, chat.id, user.id);
        Assert.assertEquals(decision, result.decision);
        Assert.assertEquals(winningOption, result.winningOptions);
    }

    @Test
    public void testReopenDecision() throws MessageParseException {
        String message = messageManager.buildReopenDecision(decision);
        MessagesManager.MessageType type = messageManager.getMessageType(message);
        Assert.assertEquals(MessagesManager.MessageType.REOPEN_DECISION, type);
        assertReopenDecision(decision, message);
        assertReopenDecision(decision, messageManager.reformatMessage(message));
    }

    private void assertReopenDecision(Decision decision, String message) throws MessageParseException {
        Decision foundDecision = messageManager.getReopenDecision(message, chat.id, user.id);
        Assert.assertEquals(foundDecision, decision);
    }

    @Test
    public void testDeleteDecision() throws MessageParseException {
        String message = messageManager.buildDeleteDecision(decision);
        MessagesManager.MessageType type = messageManager.getMessageType(message);
        Assert.assertEquals(MessagesManager.MessageType.DELETE_DECISION, type);
        assertDeleteDecision(decision, message);
        assertDeleteDecision(decision, messageManager.reformatMessage(message));
    }

    private void assertDeleteDecision(Decision decision, String message) throws MessageParseException {
        Decision foundDecision = messageManager.getDeleteDecision(message, chat.id, user.id);
        Assert.assertEquals(foundDecision, decision);
    }

    @Test
    public void testAddOptions() throws MessageParseException {
        List<Option> newOptions = new ArrayList<>();
        newOptions.add(new TextOption("Option1 Title", "Option1 long desc", decision.getId()));
        newOptions.add(new TextOption("Option2 Title", "", decision.getId()));
        newOptions.add(new TextOption("Option3 Title", null, decision.getId()));
        newOptions.add(new TextOption("It's options 4", "option 4 desc it's the best", decision.getId()));
        newOptions.add(new TextOption("options 5", "option 5 desc it's even better", decision.getId()));
        String message = messageManager.buildAddOptions(decision, newOptions);
        MessagesManager.MessageType type = messageManager.getMessageType(message);
        Assert.assertEquals(MessagesManager.MessageType.ADD_OPTIONS, type);
        assertAddOptions(message, decision, newOptions);
        assertAddOptions(messageManager.reformatMessage(message), decision, newOptions);
    }

    private void assertAddOptions(String message, Decision decision, List<Option> newOptions) throws MessageParseException {
        MessagesManager.DecisionOptionData result = messageManager.getAddedOption(message, chat.id, user.id);
        Assert.assertEquals(decision, result.decision);
        Assert.assertEquals(newOptions, result.optionList);
    }

    @Test
    public void testDeleteOptions() throws MessageParseException {
        List<Option> optionToDelte = new ArrayList<>();
        List<Option> decisionOptions = dao.getOptions(decision);
        for (int i = 0; i< decisionOptions.size() ; i++){
            if (i % 2 == 0)
                optionToDelte.add(decisionOptions.get(i));
        }
        String message = messageManager.buildDeleteOptions(decision, optionToDelte);
        MessagesManager.MessageType type = messageManager.getMessageType(message);
        Assert.assertEquals(MessagesManager.MessageType.DELETE_OPTIONS, type);
        assertDeleteOptions(message, decision, optionToDelte);
        assertDeleteOptions(messageManager.reformatMessage(message), decision, optionToDelte);
    }

    private void assertDeleteOptions(String message, Decision decision, List<Option> optionToDelte) throws MessageParseException {
        MessagesManager.DecisionOptionData result = messageManager.getDeletedOption(message, chat.id, user.id);
        Assert.assertEquals(decision, result.decision);
        Assert.assertEquals(optionToDelte, result.optionList);
    }

    @Test
    public void testUpdateOptionDescription() throws MessageParseException {
        TextOption textOption = (TextOption) dao.getOptions(decision).get(0);
        textOption.setNotes("A new Note it's a great idea");
        String message = messageManager.buildUpdateOptionNotes(decision,textOption);
        MessagesManager.MessageType type = messageManager.getMessageType(message);
        Assert.assertEquals(MessagesManager.MessageType.UPDATE_OPTION_NOTES, type);
        assertUpdateOption(message, textOption);
        assertUpdateOption(messageManager.reformatMessage(message), textOption);
    }

    private void assertUpdateOption(String message, TextOption textOption) throws MessageParseException {
        TextOption fromMsg = messageManager.getNewOptionData(message, chat.id, user.id);
        Assert.assertEquals(textOption, fromMsg);

    }

}