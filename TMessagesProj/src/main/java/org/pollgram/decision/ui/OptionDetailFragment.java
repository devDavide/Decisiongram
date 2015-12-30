package org.pollgram.decision.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.pollgram.R;
import org.pollgram.decision.data.TextOption;
import org.pollgram.decision.data.UsersDecisionVotes;
import org.pollgram.decision.service.PollgramFactory;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

/**
 * Created by davide on 30/12/15.
 */
public class OptionDetailFragment extends BaseFragment {

    public static final String PAR_DECISION_ID = "PAR_DECISION_ID";
    public static final String PAR_PARTICIPANT_IDS  = "PAR_PARTICIPANT_IDS" ;
    public static final String PAR_OPTION_ID = "PAR_OPTION_ID";
    private static final String LOG_TAG = "OptionDetailFrame";

    private TextOption option;
    private int missingVoteCount;
    private int positiveVoteCount;
    private int negativeVoteCount;
    private int voteCount;
    private int membersCount;

    public OptionDetailFragment(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        long optionId = getArguments().getLong(PAR_OPTION_ID);
        long decisionId = getArguments().getLong(PAR_DECISION_ID);
        int[] members = getArguments().getIntArray(PAR_PARTICIPANT_IDS);

        UsersDecisionVotes usersDecisionVotes = PollgramFactory.getPollgramService().getUsersDecisionVotes(decisionId, members);
        option = (TextOption) usersDecisionVotes.getOption(optionId);

        voteCount = usersDecisionVotes.getUserThatVoteCount();
        membersCount = members.length;
        positiveVoteCount = usersDecisionVotes.getPositiveVoteCount(option);
        negativeVoteCount = voteCount- positiveVoteCount;
        missingVoteCount = membersCount - voteCount;


        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        actionBar.setTitle(context.getString(R.string.optionDetailTitle));
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == UIUtils.ACTION_BAR_BACK_ITEM_ID) {
                    finishFragment();
                    return;
                } else
                    Log.w(LOG_TAG, "unkown on item click id [" + id + "]");
            }
        });


        LayoutInflater layoutInflater = LayoutInflater.from(context);
        fragmentView = new SizeNotifierFrameLayout(context);
        View myView = layoutInflater.inflate(R.layout.option_detail_fragment, (ViewGroup) fragmentView);

        EditText edTitle = (EditText) myView.findViewById(R.id.option_detail_ed_title);
        EditText edLongDesc = (EditText) myView.findViewById(R.id.option_detail_ed_notes);
        TextView tvMissing = (TextView) myView.findViewById(R.id.option_detail_tv_missing_votes_count);
        TextView tvPositive = (TextView) myView.findViewById(R.id.option_detail_tv_positive_votes_count);
        TextView tvNegative = (TextView) myView.findViewById(R.id.option_detail_tv_negative_votes_count);
        LinearLayout stackedBarContainer = (LinearLayout)myView.findViewById(R.id.option_detail_stacked_bar_layout_container);

        StackedBar stackedBar = new StackedBar(context, membersCount, positiveVoteCount ,voteCount);
        StackedBar.Percs percs = stackedBar.getPercs();
        edTitle.setText(option.getTitle());
        edLongDesc.setText(option.getLongDescription());
        tvMissing.setText(context.getString(R.string.missingVoteDesc, missingVoteCount, percs.emptyPerc * 100 ));
        tvPositive.setText(context.getString(R.string.positiveVoteDesc, positiveVoteCount, percs.positivePerc * 100));
        tvNegative.setText(context.getString(R.string.negativeVoteDesc, negativeVoteCount, percs.negativePerc * 100));

        stackedBar.setText("123456789");
        stackedBarContainer.addView(stackedBar, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return fragmentView;
    }
}
