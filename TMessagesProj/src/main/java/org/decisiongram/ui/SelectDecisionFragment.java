package org.decisiongram.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.decisiongram.R;
import org.decisiongram.adapter.DecisionAdapter;
import org.decisiongram.data.Decision;
import org.decisiongram.service.DecisionDAO;
import org.decisiongram.service.DecisiongramFactory;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

import java.util.List;

/**
 * Created by davide on 24/01/16.
 */
public class SelectDecisionFragment extends BaseFragment {

    public static final String PAR_GROUP_CHAT_ID = "PAR_GROUP_CHAT_ID";
    public static final String PAR_NEW_OPTION_LONG_DESCRIPTION = "PAR_NEW_OPTION_LONG_DESCRIPTION";

    private static final String LOG_TAG = "SelectDecision";

    private DecisionDAO dao;
    private int groupChatId;
    private String newOptionLongDescription;

    public SelectDecisionFragment(Bundle bundle){
        super(bundle);
    }

    @Override
    public boolean onFragmentCreate() {
        dao = DecisiongramFactory.getDAO();
        groupChatId = getArguments().getInt(PAR_GROUP_CHAT_ID);
        newOptionLongDescription = getArguments().getString(PAR_NEW_OPTION_LONG_DESCRIPTION);
        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        actionBar.setTitle(context.getString(R.string.selectDecision));
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == UIUtils.ACTION_BAR_BACK_ITEM_ID) {
                    finishFragment();
                    return;
                } else
                    Log.w(LOG_TAG, "unknown on item click id [" + id + "]");
            }
        });

        // set up layout
        fragmentView = new SizeNotifierFrameLayout(context);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View myView = layoutInflater.inflate(R.layout.select_decision_layout, (ViewGroup) fragmentView);
        TextView tvNoDecisionForCurrentUser = (TextView)myView.findViewById(R.id.select_decision_tv_no_decisions_present);
        final ListView lvDecisions = (ListView)myView.findViewById(R.id.select_decision_list_view);

        // put values
        List<Decision> decisionList = dao.getDecisions(groupChatId, UserConfig.getClientUserId());
        tvNoDecisionForCurrentUser.setVisibility(decisionList.size() == 0 ? View.VISIBLE : View.GONE);

        DecisionAdapter decisionAdapter = new DecisionAdapter(context, decisionList);
        lvDecisions.setAdapter(decisionAdapter);
        lvDecisions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Decision decision = (Decision) parent.getAdapter().getItem(position);
                Bundle bundle = new Bundle();
                bundle.putLong(EditOptionsFragment.PAR_DECISION_ID, decision.getId());
                bundle.putString(EditOptionsFragment.PAR_NEW_OPTION_LONG_DESC, newOptionLongDescription);
                presentFragment(new EditOptionsFragment(bundle));
                removeSelfFromStack();
            }
        });

        return fragmentView;
    }
}
