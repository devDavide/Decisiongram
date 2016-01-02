package org.pollgram.decision.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.pollgram.R;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.TextOption;
import org.pollgram.decision.data.TimeRangeOption;
import org.pollgram.decision.data.UsersDecisionVotes;
import org.pollgram.decision.data.Vote;
import org.pollgram.decision.service.PollgramFactory;
import org.pollgram.decision.ui.OptionDetailFragment;
import org.pollgram.decision.ui.StackedBar;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by davide on 04/10/15.
 */
public class VoteListAdapter extends ArrayAdapter<Vote> {

    private static final String LOG_TAG = "ChoiceAdapter";

    private static final int LAYOUT_RES_ID = R.layout.item_vote_list;
    private final LayoutInflater inflater;
    private final BaseFragment baseFragment;
    private final int[] participantsUserIds;
    private boolean editable;
    private List<Vote> votes;
    private List<Boolean> originalVotes;
    private Map<Option,Vote> newVotes;
    private OnVoteChangeListener onVoteChangeListener;
    private UsersDecisionVotes usersDecisionVotes;


    public interface OnVoteChangeListener {
        void voteChanges(boolean areThereChangesToSave);
    }

    public VoteListAdapter(BaseFragment baseFragment, int[] participantsUserIds,  boolean editable) {
        super(baseFragment.getParentActivity(), LAYOUT_RES_ID);
        this.baseFragment = baseFragment;
        this.participantsUserIds = participantsUserIds;
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.editable = editable;
        this.onVoteChangeListener = new OnVoteChangeListener() {
            @Override
            public void voteChanges(boolean areThereChangesToSave) {
            }
        };
    }

    public void setData(UsersDecisionVotes usersDecisionVotes, int currentUserId) {
        this.usersDecisionVotes = usersDecisionVotes;
        this.votes = usersDecisionVotes.getVotes(currentUserId);
        this.newVotes = new HashMap<>();
        this.originalVotes = new ArrayList<>();
        for (Vote v : votes){
            originalVotes.add(v.isVote() == null ? null :new Boolean(v.isVote().booleanValue()));
        }
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public List<Vote> getVotes() {
        return votes;
    }

    @Override
    public int getCount() {
        return votes.size();
    }

    @Override
    public Vote getItem(int position) {
        return votes.get(position);
    }

    @Override
    public int getPosition(Vote item) {
        return votes.indexOf(item);
    }

    public void setOnVoteChangeListener(OnVoteChangeListener onVoteChangeListener){
        this.onVoteChangeListener = onVoteChangeListener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Vote vote = getItem(position);
        final Option c = PollgramFactory.getPollgramDAO().getOption(vote.getOptionId());
        if (c instanceof TimeRangeOption){
            Log.e("ChoiceAdapter", "TimeRangeOption not supported yet");
            return null;
        }
        final TextOption o = (TextOption)c;

        View rowView = inflater.inflate(LAYOUT_RES_ID, parent, false);
        ImageView optionImage = (ImageView)rowView.findViewById(R.id.item_option_iv_image);
        TextView optionTitle = (TextView)rowView.findViewById(R.id.item_option_tv_title);
        final TextView optionVoteCount = (TextView)rowView.findViewById(R.id.item_option_tv_vote_count);
        final ImageView starImageView = (ImageView)rowView.findViewById(R.id.item_option_star_imageView);
        TextView optionSubtitle = (TextView)rowView.findViewById(R.id.item_option_tv_subtitle);
        ViewGroup stackedBarContainer = (ViewGroup)rowView.findViewById(R.id.item_option_stacked_bar);
        final CheckBox optionCheckBox = (CheckBox)rowView.findViewById(R.id.item_option_cb);

        optionCheckBox.setEnabled(editable);
        optionCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vote.setVote(optionCheckBox.isChecked());
                vote.setVoteTime(new Date());
                if (vote.isVote() != null && vote.isVote().equals(originalVotes.get(position))) {
                    newVotes.remove(o);
                    ;
                } else {
                    newVotes.put(o, vote);
                }
                Log.d(LOG_TAG, "item [" + position + "] selected[" + optionCheckBox.isChecked() + "] ");
                usersDecisionVotes.setVote(vote.getUserId(),o, vote);
                onVoteChangeListener.voteChanges(!newVotes.isEmpty());
                notifyDataSetChanged();
            }
        });

        View.OnClickListener openOptionDetailOnClickLister = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putLong(OptionDetailFragment.PAR_OPTION_ID, o.getId());
                bundle.putLong(OptionDetailFragment.PAR_DECISION_ID, o.getDecisionId());
                bundle.putIntArray(OptionDetailFragment.PAR_PARTICIPANT_IDS, participantsUserIds);
                baseFragment.presentFragment(new OptionDetailFragment(bundle));
            }
        };
        // TODO optionImage

        // Set values
        optionTitle.setText(o.getTitle());
        optionTitle.setOnClickListener(openOptionDetailOnClickLister);
        optionSubtitle.setText(o.getLongDescription());
        optionSubtitle.setOnClickListener(openOptionDetailOnClickLister);

        int positiveVoteCount = usersDecisionVotes.getPositiveVoteCount(o);
        optionVoteCount.setText(formatVoteCount(positiveVoteCount));
        //noinspection ResourceType
        starImageView.setVisibility(usersDecisionVotes.isWinningOption(o) ? View.VISIBLE : View.INVISIBLE);


        StackedBar stackedBarStackedBar = new StackedBar(getContext(), usersDecisionVotes.getUsers().size(),
                positiveVoteCount, usersDecisionVotes.getUserThatVoteCount());
        stackedBarContainer.addView(stackedBarStackedBar, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        optionCheckBox.setChecked(vote.isVote() != null && vote.isVote());

        return rowView;
    }

    private String formatVoteCount(int positiveVoteCount) {
        return "(" + positiveVoteCount + ")";
    }


    public Collection<Vote> getNewVotes() {
        return newVotes.values();
    }

}
