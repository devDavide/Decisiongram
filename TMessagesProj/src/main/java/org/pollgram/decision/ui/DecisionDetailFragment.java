package org.pollgram.decision.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.pollgram.R;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.service.PollgramDAO;
import org.pollgram.decision.service.PollgramFactory;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

/**
 * Created by davide on 31/12/15.
 */
public class DecisionDetailFragment extends BaseFragment {
    private static final String LOG_TAG = "DecisionDetail";

    static final String PAR_DECISION_ID = "PAR_DECISION_ID";
    private PollgramDAO pollgramDAO;
    private Decision decision;

    public DecisionDetailFragment(Bundle bundle) {
        super(bundle);
    }

    @Override
    public boolean onFragmentCreate() {
        pollgramDAO = PollgramFactory.getPollgramDAO();

        long decisionId = getArguments().getLong(PAR_DECISION_ID);
        decision = pollgramDAO.getDecision(decisionId);
        return true;
    }


    @Override
    public View createView(Context context) {

        actionBar.setTitle(getParentActivity().getString(R.string.decisionDetailTitle));
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                switch (id) {
                    case UIUtils.ACTION_BAR_BACK_ITEM_ID:
                        finishFragment();
                        break;
                    default:
                        Log.w(LOG_TAG, "unknown action bar menu id [" + id + "]");
                }
            }

        });

        fragmentView = new SizeNotifierFrameLayout(context);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View myView = layoutInflater.inflate(R.layout.decision_detail_layout, (ViewGroup) fragmentView);

        EditText edTitle = (EditText) myView.findViewById(R.id.new_decision_ed_title);
        edTitle.setText(decision.getTitle());
        EditText edLongDescription = (EditText) myView.findViewById(R.id.new_decision_ed_long_description);
        edLongDescription.setText(decision.getLongDescription());
        return fragmentView;
    }
}

