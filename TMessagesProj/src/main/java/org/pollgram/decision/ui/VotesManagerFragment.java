package org.pollgram.decision.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.pollgram.decision.service.PollgramDAO;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.service.PollgramFactory;
import org.pollgram.decision.service.PollgramService;
import org.pollgram.decision.utils.PollgramUtils;
import org.pollgram.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

/**
 * Created by davide on 04/10/15.
 */
public class VotesManagerFragment extends BaseFragment {

    static final String LOG_TAG = "SlidingTabs";

    // menu ids
    private static int nextId = 1;
    private static final int ID_CLOSE_DECISOIN = nextId++;
    private static final int ID_REOPEN_DECISOIN = nextId++;
    private static final int ID_DELETE_DECISOIN = nextId++;


    public static final String PAR_DECISION_ID = "PAR_DECISION_ID";
    public static final String PAR_PARTICIPANT_IDS  = "PAR_PARTICIPANT_IDS" ;
    public static final String PAR_GROUP_CHAT_ID = "PAR_GROUP_CHAT_ID" ;

    private int[] participantsUserIds;
    private PollgramDAO pollgramDAO;
    private PollgramService pollgramService;
    private Decision decision;
    private TextView title;

    public VotesManagerFragment(Bundle bundle) {
        super(bundle);
    }

    @Override
    public boolean onFragmentCreate() {
        // init field
        super.swipeBackEnabled = false;
        pollgramDAO = PollgramFactory.getPollgramDAO();
        pollgramService = PollgramFactory.getPollgramService();
        long decisionId = getArguments().getLong(PAR_DECISION_ID);
        participantsUserIds = getArguments().getIntArray(PAR_PARTICIPANT_IDS);
        decision = pollgramDAO.getDecision(decisionId);
        return super.onFragmentCreate();
    }


    @Override
    public View createView(final Context context) {
        // TODO add real icon
        // set up action bar
        PollgramUtils.init(actionBar, decision.getTitle(), 18, R.drawable.attach_camera);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem headerItem = menu.addItem(0, R.drawable.ic_ab_other);
        if (decision.isOpen())
            headerItem.addSubItem(ID_CLOSE_DECISOIN, context.getString(R.string.closeDecision),0 );
        else {
            headerItem.addSubItem(ID_REOPEN_DECISOIN, context.getString(R.string.reopenDecision), 0);
            headerItem.addSubItem(ID_DELETE_DECISOIN, context.getString(R.string.deleteDecision), 0);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                    return;
                }
                int stringId;
                if (id == ID_CLOSE_DECISOIN){
                    pollgramService.notifyClose(decision);
                    stringId = R.string.decisionClosed;
                } else if (id == ID_REOPEN_DECISOIN){
                    pollgramService.notifyReopen(decision);
                    stringId = R.string.decisionReopened;
                } else if (id == ID_DELETE_DECISOIN){
                    stringId = R.string.decisionDeleted;
                    pollgramService.notifyDelete(decision);
                } else {
                    Log.e(LOG_TAG, "Unknown action id["+id+"]");
                    return;
                }
                Toast.makeText(context,context.getString(stringId),Toast.LENGTH_SHORT).show();
            }
        });
        fragmentView = new SizeNotifierFrameLayout(context);
        SizeNotifierFrameLayout contentView = (SizeNotifierFrameLayout) fragmentView;
        LayoutInflater li = LayoutInflater.from(context);
        ViewGroup rootView = (ViewGroup)li.inflate(R.layout.votes_manager_main, (ViewGroup) fragmentView);
        // Create view
        title = (TextView) rootView.findViewById(R.id.decision_detail_top_title);
        updateView();

        android.support.v4.app.FragmentTransaction transaction = getParentActivity().getSupportFragmentManager().beginTransaction();
        VotesManagerTabsFragment fragment = new VotesManagerTabsFragment(){
            @Override
            protected void onVoteSaved() {
                updateView();
            }
        };
        fragment.setArguments(getArguments());
        transaction.replace(R.id.sample_content_fragment, fragment);
        transaction.commit();

        return rootView;
    }

    private void updateView(){
        int userThatVoteSoFar = pollgramDAO.getUserVoteCount(decision);
        title.setText(getParentActivity().getString(R.string.decisionDetailSummarizeTitle,
                userThatVoteSoFar, participantsUserIds.length));
    }
}