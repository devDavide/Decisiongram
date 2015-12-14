package org.pollgram.decision.ui;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by davide on 14/12/15.
 */
public class StackedBarPercTest {


    @Test(expected = IllegalArgumentException.class)
    public void testPercWrongPerc(){
        StackedBar.getPercs(5, 6, 6);
    }

    @Test
    public void testPerc1(){
        //int totalUserCount, int positiveVoteCount, int userThatVoteCount
        StackedBar.Percs p = StackedBar.getPercs(10, 5, 6);
        Assert.assertEquals(p.emptyPerc, 0.4, 0.001);
        Assert.assertEquals(p.positivePerc, 0.5, 0.001);
        Assert.assertEquals(p.negativePerc, 0.1, 0.001);
    }

    @Test
    public void testPerc2() {
        //int totalUserCount, int positiveVoteCount, int userThatVoteCount
        StackedBar.Percs p = StackedBar.getPercs(3, 1, 1);
        Assert.assertEquals(p.emptyPerc, 0.666, 0.001);
        Assert.assertEquals(p.positivePerc, 0.333, 0.001);
        Assert.assertEquals(p.negativePerc, 0, 0.001);
    }

    @Test
    public void testNoVote() {
        //int totalUserCount, int positiveVoteCount, int userThatVoteCount
        StackedBar.Percs p = StackedBar.getPercs(0, 0, 0);
        Assert.assertEquals(p.emptyPerc, 1, 0.001);
        Assert.assertEquals(p.positivePerc, 0, 0.001);
        Assert.assertEquals(p.negativePerc, 0, 0.001);
    }

    @Test
    public void testAllPositive() {
        //int totalUserCount, int positiveVoteCount, int userThatVoteCount
        StackedBar.Percs p = StackedBar.getPercs(66, 66, 66);
        Assert.assertEquals(p.emptyPerc, 0, 0.001);
        Assert.assertEquals(p.positivePerc, 1, 0.001);
        Assert.assertEquals(p.negativePerc, 0, 0.001);
    }

    @Test
    public void testAllNegative() {
        //int totalUserCount, int positiveVoteCount, int userThatVoteCount
        StackedBar.Percs p = StackedBar.getPercs(569, 0, 569);
        Assert.assertEquals(p.emptyPerc, 0, 0.001);
        Assert.assertEquals(p.positivePerc, 0, 0.001);
        Assert.assertEquals(p.negativePerc, 1, 0.001);
    }
}

