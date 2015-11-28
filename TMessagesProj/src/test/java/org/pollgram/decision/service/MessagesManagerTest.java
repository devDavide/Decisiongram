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
    private PollgramMessagesManager messageManager;
    private PollgramServiceImpl service;
    private Map<Long, String> receivedId2LastMessages = new HashMap<>();
    private TLRPC.Chat chat;
    private TLRPC.User user;

    @Before
    public void setUp(){
        ApplicationLoader.applicationContext = mMockContext;

        chat = new TLRPC.Chat();
        chat.id =39379118;

        user = new TLRPC.User();
        user.id = 22;

        this.dao = new PollgramDAOTestImpl(chat.id);
        messageManager = new PollgramMessagesManagerImpl(dao);
        this.service = new PollgramServiceImpl(dao,messageManager);
    }

    @Test
    public void testMessageTypeDifferentEmoji(){
        for (PollgramMessagesManager.MessageType mt1 : PollgramMessagesManager.MessageType.values()){
            for (PollgramMessagesManager.MessageType mt2 : PollgramMessagesManager.MessageType.values()){
                if (mt1.equals(mt2))
                    Assert.assertTrue(mt1.getEmoji().equals(mt2.getEmoji()));
                else
                    Assert.assertFalse(mt1.getEmoji().equals(mt2.getEmoji()));
            }
        }
    }

    @Test
    public void testMessageTypeGetEmoji(){
        for (PollgramMessagesManager.MessageType mt1 : PollgramMessagesManager.MessageType.values()){
            Assert.assertEquals(mt1, PollgramMessagesManager.MessageType.byEmoji(mt1.getEmoji()));
        }
    }

    @Test
    public void testSendVotes() throws ParseException {
        Decision decision = dao.getDecisions(null).get(0);
        List<Option> options = dao.getOptions(decision);
        Collection<Vote> votes = new ArrayList<>();
        Date voteDate = new Date();
        for (int i=0; i< options.size() ; i++ ){
            Vote v = new Vote( i % 2 == 0, voteDate, user.id, options.get(i).getId());
            votes.add(v);
        }
        String message = messageManager.buildNotifyVoteMessage(decision, votes);
        PollgramMessagesManager.MessageType type = messageManager.getMessageType(message);
        Assert.assertEquals(type, PollgramMessagesManager.MessageType.VOTE);

        Collection<Vote> parsedVotes = messageManager.getVotes(message, chat, voteDate, user.id);
        Assert.assertEquals(parsedVotes,votes);

        message = messageManager.reformatMessage(message);
        parsedVotes = messageManager.getVotes(message, chat, voteDate, user.id);
        Assert.assertEquals(parsedVotes,votes);
    }

    @Test
    public void testSendRemind() throws ParseException {
        Decision decision = dao.getDecisions(null).get(0);

        String message = messageManager.buildRemindMessage("UserName String", decision);
        PollgramMessagesManager.MessageType type = messageManager.getMessageType(message);
        Assert.assertEquals(type, PollgramMessagesManager.MessageType.REMIND_TO_VOTE);
    }


}