package org.pollgram.decision.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pollgram.decision.dao.PollgramDAO;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.utils.PolgramUtils;
import org.pollgram.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

/**
 * Created by davide on 04/10/15.
 */
public class VotesManagerFragment extends BaseFragment {

    static final String LOG_TAG = "SlidingTabs";


    public static final String PAR_DECISION_ID = "PAR_DECISION_ID";
    public static final String PAR_PARTICIPANT_IDS  = "PAR_PARTICIPANT_IDS" ;

    private int[] participantsUserIds;
    private PollgramDAO pollgramDAO;
    private Decision decision;

    public VotesManagerFragment(Bundle bundle) {
        super(bundle);
    }

    @Override
    public boolean onFragmentCreate() {
        // init field
        super.swipeBackEnabled = false;
        pollgramDAO = PollgramDAO.getInstance();
        long decisionId = getArguments().getLong(PAR_DECISION_ID);
        participantsUserIds = getArguments().getIntArray(PAR_PARTICIPANT_IDS);
        decision = pollgramDAO.getDecision(decisionId);
        return super.onFragmentCreate();
    }


    @Override
    public View createView(Context context) {
        // set up action bar
        // TODO add real icon
        PolgramUtils.init(actionBar, decision.getTitle(), 18, R.drawable.attach_camera);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        fragmentView = new SizeNotifierFrameLayout(context);
        SizeNotifierFrameLayout contentView = (SizeNotifierFrameLayout) fragmentView;
        LayoutInflater li = LayoutInflater.from(context);
        ViewGroup rootView = (ViewGroup)li.inflate(R.layout.votes_manager_main, (ViewGroup) fragmentView);
        // Create view
        TextView title = (TextView) rootView.findViewById(R.id.decision_detail_top_title);
        title.setText(context.getString(R.string.decisionDetailSummarizeTitle, decision.getUsersThatVoteCount(), participantsUserIds.length));

        android.support.v4.app.FragmentTransaction transaction = getParentActivity().getSupportFragmentManager().beginTransaction();
        VotesManagerTabsFragment fragment = new VotesManagerTabsFragment();
        fragment.setArguments(getArguments());
        transaction.replace(R.id.sample_content_fragment, fragment);
        transaction.commit();

        return rootView;
    }

}