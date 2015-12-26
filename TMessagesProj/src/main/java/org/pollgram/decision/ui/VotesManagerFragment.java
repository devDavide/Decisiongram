package org.pollgram.decision.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.pollgram.R;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.service.PollgramDAO;
import org.pollgram.decision.service.PollgramFactory;
import org.pollgram.decision.service.PollgramService;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

import java.text.DateFormat;

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

    private TextView tvCreationInfo;
    private TextView tvUserVoteCount;
    private TextView tvDecisionStatus;
    private TextView menuDeleteDecisionItem;
    private TextView menuReopenDecisionItem;
    private TextView menuCloseDecisionItem;
    private ActionBarMenu menu;
    private VotesManagerTabsFragment votesManagerTabsFragment;

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
        tvUserVoteCount = UIUtils.init(actionBar, decision.getTitle(), R.drawable.check_list);
        menu = actionBar.createMenu();
        ActionBarMenuItem headerItem = menu.addItem(0, R.drawable.ic_ab_other);
        menuCloseDecisionItem = headerItem.addSubItem(ID_CLOSE_DECISOIN, context.getString(R.string.closeDecision), 0);
        menuReopenDecisionItem =  headerItem.addSubItem(ID_REOPEN_DECISOIN, context.getString(R.string.reopenDecision), 0);
        menuDeleteDecisionItem =headerItem.addSubItem(ID_DELETE_DECISOIN, context.getString(R.string.deleteDecision), 0);
        
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == UIUtils.ACTION_BAR_BACK_ITEM_ID) {
                    finishFragment();
                    return;
                }
                if (id == ID_DELETE_DECISOIN) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(R.string.deleteDecisionQuestion).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            pollgramService.notifyDelete(decision);
                            Toast.makeText(context, context.getString(R.string.decisionDeleted), Toast.LENGTH_SHORT).show();
                            finishFragment();
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // nothing to do11
                        }
                    }).show();
                }
                int stringId;
                if (id == ID_CLOSE_DECISOIN) {
                    pollgramService.notifyClose(decision);
                    stringId = R.string.decisionClosed;
                } else if (id == ID_REOPEN_DECISOIN) {
                    pollgramService.notifyReopen(decision);
                    stringId = R.string.decisionReopened;
                } else {
                    Log.e(LOG_TAG, "Unknown action id[" + id + "]");
                    return;
                }
                votesManagerTabsFragment.updateView();
                updateView();
                Toast.makeText(context, context.getString(stringId), Toast.LENGTH_SHORT).show();
            }
        });
        fragmentView = new SizeNotifierFrameLayout(context);
        //SizeNotifierFrameLayout contentView = (SizeNotifierFrameLayout) fragmentView;
        LayoutInflater li = LayoutInflater.from(context);
        ViewGroup rootView = (ViewGroup)li.inflate(R.layout.votes_manager_main, (ViewGroup) fragmentView);

        // Create view
        tvCreationInfo = (TextView) rootView.findViewById(R.id.vote_manager_tv_creationInfo);
        tvDecisionStatus = (TextView) rootView.findViewById(R.id.vote_manager_tv_decision_status);
        updateView();

        android.support.v4.app.FragmentTransaction transaction = getParentActivity().getSupportFragmentManager().beginTransaction();
        votesManagerTabsFragment = new VotesManagerTabsFragment(){
            @Override
            protected void onVoteSaved() {
                updateView();
            }
        };
        votesManagerTabsFragment.setArguments(getArguments());
        transaction.replace(R.id.sample_content_fragment, votesManagerTabsFragment);
        transaction.commit();

        return rootView;
    }

    private void updateView(){
        Context ctx = getParentActivity();
        decision = pollgramDAO.getDecision(decision.getId());
        int userThatVoteSoFar = pollgramDAO.getUserVoteCount(decision);

        menuReopenDecisionItem.setVisibility(View.GONE);
        menuDeleteDecisionItem.setVisibility(View.GONE);
        menuCloseDecisionItem.setVisibility(View.GONE);
        if (decision.getUserCreatorId() == UserConfig.getCurrentUser().id) {
            menu.setVisibility(View.VISIBLE);
            if (decision.isOpen())
                menuCloseDecisionItem.setVisibility(View.VISIBLE);
            else {
                menuReopenDecisionItem.setVisibility(View.VISIBLE);
                menuDeleteDecisionItem.setVisibility(View.VISIBLE);
            }
        } else {
            menu.setVisibility(View.GONE);
        }

        String userStr = pollgramService.asString(pollgramService.getUser(decision.getUserCreatorId()));
        String creationDateStr = DateFormat.getDateInstance(DateFormat.LONG).
                format(decision.getCreationDate());
        tvCreationInfo.setText(ctx.getString(R.string.createdByUserOnDayNewLine,userStr, creationDateStr));

        String statusDesc = ctx.getString(decision.isOpen() ? R.string.statusOpen : R.string.statusClose);
        tvDecisionStatus.setText(ctx.getString(R.string.decisionStatus, statusDesc));
        tvDecisionStatus.setBackgroundColor(decision.isOpen() ? Color.GREEN : Color.RED);

        tvUserVoteCount.setText(ctx.getString(R.string.howManyMemberVote,
                userThatVoteSoFar, participantsUserIds.length));

    }
}