package org.pollgram.decision.data;

import android.support.annotation.Nullable;
import android.util.Log;

import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by davide on 13/10/15.
 */
public class UsersDecisionVotes {

    private static final String LOG_TAG = "UserDecVotes";


    private final Comparator<Option> optionsComparator = new Comparator<Option>() {
        @Override
        public int compare(Option o1, Option o2) {
            int o1VoteCount = getPositiveVoteCount(o1);
            int o2VoteCount = getPositiveVoteCount(o2);
            if (o1VoteCount == o2VoteCount)
                return o1.getTitle().compareTo(o2.getTitle());
            else
                return o1VoteCount > o2VoteCount ? -1 : 1;
        }
    };

    private final Decision decision;
    private final List<TLRPC.User> users;
    private final List<Option> options;
    private final Map<UserIdOptionKey, Vote> voteMap = new HashMap<>();
    private int maxVote;

    public UsersDecisionVotes(Decision decision, List<TLRPC.User> users, List<Option> optionsPar, List<Vote> votes) {
        this.decision = decision;
        this.options = optionsPar;
        this.users = users;
        Map<Long,Option> idOptionMap = new HashMap<>();
        for (Vote v : votes) {
            Option option = idOptionMap.get(v.getOptionId());
            if (option == null){
                for (Option o : options) {
                    if (o.getId() == v.getOptionId()){
                        option = o;
                        idOptionMap.put(option.getId(),option);
                        break;
                    }
                }
            }
            if (option == null) {
                Log.e(LOG_TAG, "voteId ["+v.getId()+"] refers to unknown option ["+v.getOptionId()+"] it will be skipped");
                continue;
            }
            voteMap.put(new UserIdOptionKey(v.getUserId(), option.getId()), v);
        }
        Collections.sort(options,optionsComparator);

        // calculate max votes
        updateMaxVoteCount();

    }

    private void updateMaxVoteCount() {
        maxVote = 0;
        for (Option o : getOptions()){
            maxVote = Math.max(maxVote, getPositiveVoteCount(o));
        }
    }

    public Decision getDecision(){
        return decision;
    }

    public List<TLRPC.User> getUsers(){
        return users;
    }

    public List<Option> getOptions(){
        return options;
    }

    public boolean atLeastOneIsNull(int userId){
        for (Option o : options){
            if (getVotes(userId,o).isVote() == null)
                return true;
        }
        return false;
    }
    public int getUserThatVoteCount(){
        int count = 0 ;
        for (TLRPC.User user : users){
            boolean allNulls = true;
            for (Option o : options){
                if (getVotes(user.id,o).isVote() != null) {
                    allNulls = false;
                    break;
                }
            }
            if (!allNulls)
                count++;
        }
        return count;
    }

    public List<Vote> getVotes(int userID){
        List<Vote> votes = new ArrayList<>();
        for (Option o : getOptions()){
            votes.add(getVotes(userID, o));
        }
        return votes;
    }

    public Vote getVotes(int userId, Option option){
        Vote v = voteMap.get(new UserIdOptionKey(userId,option.getId()));
        if (v == null) {
            // userId never vote for option v
            v = new Vote(userId, option.getId());
            //Log.d(LOG_TAG,"vote not found for userId["+userId+"] and optionId["+option.getId()+"]");
        }
        return v;
    }

    public void setVote(int userID, Option option, Vote vote){
        voteMap.put(new UserIdOptionKey(userID,option.getId()), vote);
        // update cache
        Integer positiveVoteCount = calculateVoteCount(option, true);
        cachedPositiveVoteCount.put(option, positiveVoteCount);
        cachedNegativeVoteCount.put(option, calculateVoteCount(option,false));

        Collections.sort(options, optionsComparator);
        updateMaxVoteCount();
    }

    private final Map<Option, Integer> cachedPositiveVoteCount = new HashMap<>();
    private final Map<Option, Integer> cachedNegativeVoteCount = new HashMap<>();


    /**
     * @param option
     * @return the number of positive count for option in index optionIndex
     */
    public int getPositiveVoteCount(Option option) {
        Integer count = cachedPositiveVoteCount.get(option);
        if (count != null)
            return count;

        count = calculateVoteCount(option,true);
        cachedPositiveVoteCount.put(option,count);
        return count;
    }

    /**
     * @param option
     * @return the number of positive count for option in index optionIndex
     */
    public int getNegativeVoteCount(Option option) {
        Integer count = cachedNegativeVoteCount.get(option);
        if (count != null)
            return count;

        count = calculateVoteCount(option,false);
        cachedNegativeVoteCount.put(option,count);
        return count;
    }

    private int calculateVoteCount(Option option, boolean positive) {
        int count = 0;
        for (UserIdOptionKey key : voteMap.keySet()){
            if (option.getId() == key.getOptionId()){
                Vote v = voteMap.get(key);
                if (v.isVote() != null && v.isVote() == positive)
                    count++;
            }
        }
        return count;
    }

    public @Nullable Option getOption(long optionId) {
        for(Option o : options){
            if (o.getId() == optionId)
                return o;
        }
        Log.d(LOG_TAG, "Option ["+optionId+"] not found.");
        return null;
    }

    public boolean isWinningOption(Option o){
        int count = getPositiveVoteCount(o);
        return  count != 0 && count == maxVote;
    }
}
