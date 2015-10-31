package org.pollgram.decision.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pollgram.decision.dao.DecisionDAO;
import org.pollgram.decision.dao.DecisionDAOImpl;
import org.pollgram.decision.data.Choice;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Vote;
import org.pollgram.decision.utils.PolgramUtils;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

import java.util.Map;

/**
 * Created by davide on 04/10/15.
 */
public class ChoicesManagerFragment extends BaseFragment {

    static final String LOG_TAG = "SlidingTabs";


    public static final String PAR_DECISION_ID = "PAR_DECISION_ID";
    public static final String PAR_PARTICIPANT_IDS  = "PAR_PARTICIPANT_IDS" ;

    private int[] participantsUserIds;
    private DecisionDAO decisionDAO;
    private Decision decision;

    public ChoicesManagerFragment(Bundle bundle) {
        super(bundle);
    }

    @Override
    public boolean onFragmentCreate() {
        // init field
        decisionDAO = new DecisionDAOImpl();
        long decisionId = getArguments().getLong(PAR_DECISION_ID);
        participantsUserIds = getArguments().getIntArray(PAR_PARTICIPANT_IDS);
        decision = decisionDAO.findById(decisionId);
        return super.onFragmentCreate();
    }


    @Override
    public View createView(Context context) {
        // set up action bar
        // TODO add real icon
        PolgramUtils.init(actionBar, decision.getDescription(), 18, R.drawable.attach_camera);
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
        ViewGroup rootView = (ViewGroup)li.inflate(R.layout.decision_detail_main, (ViewGroup) fragmentView);
        // Create view
        TextView title = (TextView) rootView.findViewById(R.id.decision_detail_top_title);
        // TODO this description is wrong !!!!!
        title.setText(context.getString(R.string.decisionDetailSummarizeTitle, decision.getVoteCount(), participantsUserIds.length));

        android.support.v4.app.FragmentTransaction transaction = getParentActivity().getSupportFragmentManager().beginTransaction();
        DecisionTabsFragment fragment = new DecisionTabsFragment();
        fragment.setArguments(getArguments());
        transaction.replace(R.id.sample_content_fragment, fragment);
        transaction.commit();

        return rootView;
    }

}