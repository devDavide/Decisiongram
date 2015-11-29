package org.pollgram.decision.service;

import android.content.Context;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.Vote;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.tgnet.TLRPC;

import java.text.ParseException;
import java.util.ArrayList;
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

    private PollgramDAOTestImpl dao;
    private PollgramMessagesManagerImpl messageManager;
    private PollgramServiceImpl service;
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

        this.dao = new PollgramDAOTestImpl(chat.id, user.id);
        messageManager = new PollgramMessagesManagerImpl(dao);
        this.service = new PollgramServiceImpl(dao, messageManager);

        decision = dao.getDecisions(chat.id, null).get(0);

    }

    @Test
    public void testMessageTypeDifferentEmoji() {
        for (PollgramMessagesManager.MessageType mt1 : PollgramMessagesManager.MessageType.values()) {
            for (PollgramMessagesManager.MessageType mt2 : PollgramMessagesManager.MessageType.values()) {
                if (mt1.equals(mt2))
                    Assert.assertTrue(mt1.getEmoji().equals(mt2.getEmoji()));
                else
                    Assert.assertFalse(mt1.getEmoji().equals(mt2.getEmoji()));
            }
        }
    }

    @Test
    public void testMessageTypeGetEmoji() {
        for (PollgramMessagesManager.MessageType mt1 : PollgramMessagesManager.MessageType.values()) {
            Assert.assertEquals(mt1, PollgramMessagesManager.MessageType.byEmoji(mt1.getEmoji()));
        }
    }

    @Test
    public void testBuildMessage() {
        for (PollgramMessagesManager.MessageType mt1 : PollgramMessagesManager.MessageType.values()) {
            String msg = messageManager.buildMessage(mt1, "Message Body");
            PollgramMessagesManager.MessageType  type = messageManager.getMessageType(msg);
            Assert.assertEquals(mt1, type);
        }
    }

    @Test
    public void testSendVotes() throws ParseException, PollgramParseException {
        List<Option> options = dao.getOptions(decision);
        Collection<Vote> votes = new ArrayList<>();
        Date voteDate = new Date();
        for (int i = 0; i < options.size(); i++) {
            Vote v = new Vote(i % 2 == 0, voteDate, user.id, options.get(i).getId());
            votes.add(v);
        }
        String message = messageManager.buildNotifyVoteMessage(decision, votes);
        PollgramMessagesManager.MessageType type = messageManager.getMessageType(message);
        Assert.assertEquals(type, PollgramMessagesManager.MessageType.VOTE);

        assertVotes(votes, voteDate, message);
        assertVotes(votes, voteDate, messageManager.reformatMessage(message));
    }

    private void assertVotes(Collection<Vote> votes, Date voteDate, String message) throws PollgramParseException {
        Collection<Vote> parsedVotes = messageManager.getVotes(message, chat.id, voteDate, user.id);
        Assert.assertEquals(parsedVotes, votes);
    }

    @Test
    public void testSendRemind() throws ParseException {
        String message = messageManager.buildRemindMessage("UserName String", decision);
        PollgramMessagesManager.MessageType type = messageManager.getMessageType(message);
        Assert.assertEquals(type, PollgramMessagesManager.MessageType.REMIND_TO_VOTE);
    }

    @Test
    public void testNewDecision() throws ParseException, PollgramParseException {
        List<Option> options = dao.getOptions(decision);
        String message = messageManager.buildNotifyNewDecision(decision, options);

        PollgramMessagesManager.MessageType type = messageManager.getMessageType(message);
        Assert.assertEquals(type, PollgramMessagesManager.MessageType.NEW_DECISION);

        assertNewDecision(decision, options, message);
        assertNewDecision(decision, options, messageManager.reformatMessage(message));

    }

    private void assertNewDecision(Decision decision, List<Option> options, String message) throws PollgramParseException {
        PollgramMessagesManager.NewDecisionData result = messageManager.getNewDecision(message, chat.id, user.id);
        Assert.assertEquals(decision, result.decision);
        Assert.assertEquals(options, result.optionList);
    }

    @Test
    public void testCloseDecision() throws PollgramParseException {
        Option winningOption = dao.getOptions(decision).get(0);
        int voteCount = 5;
        String message = messageManager.buildCloseDecision(decision, winningOption, voteCount);
        Assert.assertEquals(messageManager.getMessageType(message), PollgramMessagesManager.MessageType.CLOSE_DECISION);

        assertCloseDecision(decision, winningOption, message);
        assertCloseDecision(decision, winningOption, messageManager.reformatMessage(message));
    }

    private void assertCloseDecision(Decision decision, Option winningOption, String message) throws PollgramParseException {
        PollgramMessagesManager.ClosedDecisionDate result = messageManager.getCloseDecision(message, chat.id);
        Assert.assertEquals(decision, result.decision);
        Assert.assertEquals(winningOption, result.winningOption);
    }

    @Test
    public void testReopenDecision() throws PollgramParseException {
        String message = messageManager.buildReopenDecision(decision);
        PollgramMessagesManager.MessageType type = messageManager.getMessageType(message);
        Assert.assertEquals(PollgramMessagesManager.MessageType.REOPEN_DECISION, type);
        assertReopenDecision(decision, message);
        assertReopenDecision(decision, messageManager.reformatMessage(message));
    }

    private void assertReopenDecision(Decision decision, String message) throws PollgramParseException {
        Decision foundDecision = messageManager.getReopenDecision(message, chat.id);
        Assert.assertEquals(foundDecision, decision);
    }

    @Test
    public void testDeleteDecision() throws PollgramParseException {
        String message = messageManager.buildDeleteDecision(decision);
        PollgramMessagesManager.MessageType type = messageManager.getMessageType(message);
        Assert.assertEquals(PollgramMessagesManager.MessageType.DELETE_DECISION, type);
        assertDeleteDecision(decision, message);
        assertDeleteDecision(decision, messageManager.reformatMessage(message));
    }

    private void assertDeleteDecision(Decision decision, String message) throws PollgramParseException {
        Decision foundDecision = messageManager.getDeleteDecision(message, chat.id);
        Assert.assertEquals(foundDecision, decision);
    }

}