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
import org.telegram.messenger.ApplicationLoader;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

import java.text.DateFormat;
import java.util.List;

/**
 * Created by davide on 04/10/15.
 */
public class VotesManagerFragment extends BaseFragment {

    static final String LOG_TAG = "SlidingTabs";

    // menu ids
    private static int nextId = 1;
    private static final int ID_CLOSE_DECISION = nextId++;
    private static final int ID_REOPEN_DECISION = nextId++;
    private static final int ID_DELETE_DECISION = nextId++;
    private static final int ID_EDIT_OPTIONS = nextId++;


    public static final String PAR_DECISION_ID = "PAR_DECISION_ID";
    public static final String PAR_PARTICIPANT_IDS  = "PAR_PARTICIPANT_IDS" ;
    public static final String PAR_GROUP_CHAT_ID = "PAR_GROUP_CHAT_ID" ;

    private List<TLRPC.User> members;
    private PollgramDAO pollgramDAO;
    private PollgramService pollgramService;
    private Decision decision;

    private TextView tvCreationInfo;
    private TextView tvUserVoteCount;
    private TextView tvDecisionStatus;

    private TextView menuDeleteDecisionItem;
    private TextView menuReopenDecisionItem;
    private TextView menuCloseDecisionItem;
    private TextView menuEditOptions;

    private ActionBarMenu menu;
    private VotesManagerTabsFragment votesManagerTabsFragment;

    public VotesManagerFragment(Bundle bundle) {
        super(bundle);
    }

    @Override
    public boolean onFragmentCreate() {
        // init field
        super.swipeBackEnabled = false;
        pollgramDAO = PollgramFactory.getDAO();
        pollgramService = PollgramFactory.getService();
        long decisionId = getArguments().getLong(PAR_DECISION_ID);
        members = pollgramService.getUsers(getArguments().getIntArray(PAR_PARTICIPANT_IDS));
        decision = pollgramDAO.getDecision(decisionId);
        if (decision == null){
            Log.e(LOG_TAG,"Decision not found for id ["+decisionId+"]");
            Toast.makeText(ApplicationLoader.applicationContext,
                    ApplicationLoader.applicationContext.getString(R.string.decisionNotFound,"  "),
                    Toast.LENGTH_SHORT).show();
            finishFragment();
            return false;
        }
        return super.onFragmentCreate();
    }


    @Override
    public View createView(final Context context) {
        tvUserVoteCount = UIUtils.init(actionBar, decision.getTitle(), R.drawable.decision_icon_small);
        actionBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putLong(DecisionDetailFragment.PAR_DECISION_ID, decision.getId());
                presentFragment(new DecisionDetailFragment(bundle));
            }
        });
        menu = actionBar.createMenu();
        ActionBarMenuItem headerItem = menu.addItem(0, R.drawable.ic_ab_other);
        menuCloseDecisionItem = headerItem.addSubItem(ID_CLOSE_DECISION, context.getString(R.string.closeDecision), 0);
        menuReopenDecisionItem =  headerItem.addSubItem(ID_REOPEN_DECISION, context.getString(R.string.reopenDecision), 0);
        menuDeleteDecisionItem =headerItem.addSubItem(ID_DELETE_DECISION, context.getString(R.string.deleteDecision), 0);
        menuEditOptions = headerItem.addSubItem(ID_EDIT_OPTIONS,context.getString(R.string.editOptions),0);
        
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == UIUtils.ACTION_BAR_BACK_ITEM_ID) {
                    finishFragment();
                    return;
                }else if (id == ID_DELETE_DECISION) {
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
                    return;
                } else if (id == ID_CLOSE_DECISION) {
                    int voteCount = pollgramDAO.getUserVoteCount(decision);
                    int membersCount = members.size();
                    if (voteCount == membersCount) {
                        // all users voted al least one option for the current decision
                        closeDecision();
                    } else {
                        StringBuilder message =  new StringBuilder();
                        if (voteCount == 1)
                            message.append(context.getString(R.string.closeDecisionQuestionPrefixSingle, voteCount, membersCount));
                        else
                            message.append(context.getString(R.string.closeDecisionQuestionPrefixMulti, voteCount, membersCount));
                        message.append(context.getString(R.string.closeDecisionQuestionSuffix));
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage(message.toString());
                        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                closeDecision();
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // nothing to do11
                            }
                        }).show();
                    }

                } else if (id == ID_REOPEN_DECISION) {
                    pollgramService.notifyReopen(decision);
                    Toast.makeText(context, context.getString(R.string.decisionReopened), Toast.LENGTH_SHORT).show();
                    votesManagerTabsFragment.updateView();
                    updateView();
                } else if (id == ID_EDIT_OPTIONS){
                    Bundle bundle = new Bundle();
                    bundle.putLong(EditOptionsFragment.PAR_DECISION_ID, decision.getId());
                    presentFragment(new EditOptionsFragment(bundle));
                } else {

                    Log.e(LOG_TAG, "Unknown action id[" + id + "]");
                    return;
                }

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
        votesManagerTabsFragment = new VotesManagerTabsFragment(this){
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

    private void closeDecision() {
        pollgramService.notifyClose(decision);
        Toast.makeText(getParentActivity(), getParentActivity().getString(R.string.decisionClosed), Toast.LENGTH_SHORT).show();
        votesManagerTabsFragment.updateView();
        updateView();
    }


    private void updateView(){
        Context ctx = getParentActivity();
        decision = pollgramDAO.getDecision(decision.getId());
        int userThatVoteSoFar = pollgramDAO.getUserVoteCount(decision);

        menuReopenDecisionItem.setVisibility(View.GONE);
        menuDeleteDecisionItem.setVisibility(View.GONE);
        menuCloseDecisionItem.setVisibility(View.GONE);
        menuEditOptions.setVisibility(View.GONE);
        if (decision.isEditable()) {
            menu.setVisibility(View.VISIBLE);
            if (decision.isOpen()) {
                menuCloseDecisionItem.setVisibility(View.VISIBLE);
                menuEditOptions.setVisibility(View.VISIBLE);
            } else {
                menuReopenDecisionItem.setVisibility(View.VISIBLE);
                menuDeleteDecisionItem.setVisibility(View.VISIBLE);
            }
        } else {
            menu.setVisibility(View.GONE);
        }

        String userStr = pollgramService.asString(pollgramService.getUser(decision.getUserCreatorId()));
        String creationDateStr = DateFormat.getDateInstance(DateFormat.LONG).
                format(decision.getCreationDate());
        tvCreationInfo.setText(ctx.getString(R.string.createdByUserOnDayNewLine, userStr, creationDateStr));

        String statusDesc = ctx.getString(decision.isOpen() ? R.string.statusOpen : R.string.statusClose);
        tvDecisionStatus.setText(ctx.getString(R.string.decisionStatus, statusDesc));
        tvDecisionStatus.setBackgroundColor(decision.isOpen() ? Color.GREEN : Color.RED);

        tvUserVoteCount.setText(ctx.getString(R.string.howManyMemberVote,
                userThatVoteSoFar, members.size()));
        if (votesManagerTabsFragment != null)
            votesManagerTabsFragment.updateView();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }
}
