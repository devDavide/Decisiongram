package org.pollgram.decision.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.pollgram.decision.dao.PollgramDAO;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.TextOption;
import org.pollgram.decision.data.TimeRangeOption;
import org.pollgram.decision.data.Vote;
import org.telegram.messenger.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by davide on 04/10/15.
 */
public class OptionAdapter extends ArrayAdapter<Vote> {

    private static final String LOG_TAG = "ChoiceAdapter";

    private static final int LAYOUT_RES_ID = R.layout.option_choice;
    private final List<Vote> votes;
    private final List<Boolean> originalVotes;
    private final Set<Vote> newVoteSet;
    private OnVoteChangeListener onVoteChageListener;

    public interface OnVoteChangeListener {
        void voteChanges(boolean areThereChangesToSave);
    }

    public OptionAdapter(Context context, List<Vote> votes) {
        super(context, LAYOUT_RES_ID, votes.toArray(new Vote[votes.size()]));
        this.votes = votes;
        this.newVoteSet = new HashSet<>();
        this.originalVotes = new ArrayList<>();
        for (Vote v : votes){
            originalVotes.add(v.isVote() == null ? null :new Boolean(v.isVote().booleanValue()));
        }
        this.onVoteChageListener = new OnVoteChangeListener() {
            @Override
            public void voteChanges(boolean areThereChangesToSave) {
            }
        };
    }

    public void setOnVoteChageListener(OnVoteChangeListener onVoteChageListener){
        this.onVoteChageListener = onVoteChageListener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Vote vote = getItem(position);
        final Option c = PollgramDAO.getInstance().getOption(vote.getOptionId());
        if (c instanceof TimeRangeOption){
            Log.e("ChoiceAdapter", "TimeRangeOption not supported yet");
            return null;
        }
        final TextOption o = (TextOption)c;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(LAYOUT_RES_ID, parent, false);
        ImageView optionImage = (ImageView)rowView.findViewById(R.id.item_option_iv_image);
        TextView optionTitle = (TextView)rowView.findViewById(R.id.item_option_tv_title);
        TextView optionSubtitle = (TextView)rowView.findViewById(R.id.item_option_tv_subtitle);
//        SurfaceView optionView = (SurfaceView)rowView.findViewById(R.id.item_option_sw_bar);
        final CheckBox optionCheckBox = (CheckBox)rowView.findViewById(R.id.item_option_cb);
        optionCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vote.setVote(optionCheckBox.isChecked());
                if (vote.isVote() != null && vote.isVote().equals(originalVotes.get(position))){
                    newVoteSet.remove(vote);;
                } else {
                    newVoteSet.add(vote);
                }
                Log.d(LOG_TAG, "item [" + position + "] selected[" + optionCheckBox.isChecked() + "] ");
                onVoteChageListener.voteChanges(!newVoteSet.isEmpty());
            }
        });

        // TODO optionImage
        optionTitle.setText(o.getTitle());
        optionSubtitle.setText(o.getLongDescription());
        // TODO optionView
        optionCheckBox.setChecked(vote.isVote());

        return rowView;
    }

    public Collection<Vote> getNewVoteSet() {
        return newVoteSet;
    }
}
