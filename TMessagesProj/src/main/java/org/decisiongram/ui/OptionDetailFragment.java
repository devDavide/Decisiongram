package org.decisiongram.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.decisiongram.R;
import org.decisiongram.data.Decision;
import org.decisiongram.data.TextOption;
import org.decisiongram.data.UsersDecisionVotes;
import org.decisiongram.service.DecisionService;
import org.decisiongram.service.DecisiongramFactory;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

/**
 * Created by davide on 30/12/15.
 */
public class OptionDetailFragment extends BaseFragment {

    public static final String PAR_DECISION_ID = "PAR_DECISION_ID";
    public static final String PAR_PARTICIPANT_IDS  = "PAR_PARTICIPANT_IDS" ;
    public static final String PAR_OPTION_ID = "PAR_OPTION_ID";
    public static final String PAR_POSITIVE_VOTE_COUNT = "PAR_POSITIVE_VOTE_COUNT";
    public static final String PAR_NEGATIVE_VOTE_COUNT = "PAR_NEGATIVE_VOTE_COUNT";

    private static final String LOG_TAG = "OptionDetailFrame";
    private static final int SAVE_MENU_ITEM_ID = 1;

    private Decision decision;
    private TextOption option;
    private int missingVoteCount;
    private int positiveVoteCount;
    private int negativeVoteCount;
    private int voteCount;
    private int membersCount;
    EditText edLongDesc;
    private DecisionService service;

    public OptionDetailFragment(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public View createView(final Context context) {
        // ini fields
        long optionId = getArguments().getLong(PAR_OPTION_ID);
        long decisionId = getArguments().getLong(PAR_DECISION_ID);
        int[] members = getArguments().getIntArray(PAR_PARTICIPANT_IDS);

        service = DecisiongramFactory.getService();
        UsersDecisionVotes usersDecisionVotes = service.getUsersDecisionVotes(decisionId, members);

        option = (TextOption) usersDecisionVotes.getOption(optionId);
        decision = usersDecisionVotes.getDecision();
        membersCount = usersDecisionVotes.getUsers().size();
        negativeVoteCount = getArguments().getInt(PAR_NEGATIVE_VOTE_COUNT);
        positiveVoteCount = getArguments().getInt(PAR_POSITIVE_VOTE_COUNT);
        voteCount = negativeVoteCount + positiveVoteCount;
        missingVoteCount = membersCount - voteCount;


        actionBar.setTitle(context.getString(R.string.optionDetailTitle));
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        final ActionBarMenuItem saveItemMenu = actionBar.createMenu().addItem(SAVE_MENU_ITEM_ID, 0);
        saveItemMenu.setBackgroundResource(R.drawable.ic_done_white_36dp);
        saveItemMenu.setVisibility(View.INVISIBLE);

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        fragmentView = new SizeNotifierFrameLayout(context);
        View myView = layoutInflater.inflate(R.layout.option_detail_fragment, (ViewGroup) fragmentView);

        EditText edTitle = (EditText) myView.findViewById(R.id.option_detail_ed_title);
        edLongDesc = (EditText) myView.findViewById(R.id.option_detail_ed_notes);
        TextView tvMissing = (TextView) myView.findViewById(R.id.option_detail_tv_missing_votes_count);
        TextView tvPositive = (TextView) myView.findViewById(R.id.option_detail_tv_positive_votes_count);
        TextView tvNegative = (TextView) myView.findViewById(R.id.option_detail_tv_negative_votes_count);
        LinearLayout stackedBarContainer = (LinearLayout)myView.findViewById(R.id.option_detail_stacked_bar_layout_container);

        edTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,R.string.titleNotEditable,Toast.LENGTH_SHORT).show();
            }
        });

        if (!decision.isEditable()){
            edLongDesc.setFocusable(false);
            edLongDesc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context,R.string.editableOnlyByDecisionOwner, Toast.LENGTH_SHORT).show();
                }
            });
        }

        edLongDesc.addTextChangedListener(new DefaultTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                Linkify.addLinks(edLongDesc, Linkify.ALL);
                boolean saveEnabled = decision.isEditable() && !s.toString().equals(option.getNotes());
                saveItemMenu.setVisibility(saveEnabled ? View.VISIBLE : View.INVISIBLE);
            }
        });

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == UIUtils.ACTION_BAR_BACK_ITEM_ID) {
                    myFinishFragment();
                    return;
                } else if (id == SAVE_MENU_ITEM_ID) {
                    option.setNotes(edLongDesc.getText().toString());
                    service.notifyOptionUpdateLongDescription(option);
                    Toast.makeText(context,R.string.optionHasBeenSaved,Toast.LENGTH_SHORT).show();
                    saveItemMenu.setVisibility(View.INVISIBLE);
                } else
                    Log.w(LOG_TAG, "unkown on item click id [" + id + "]");
            }
        });

        edTitle.setText(option.getTitle());
        edLongDesc.setText(option.getNotes());

        StackedBar stackedBar = new StackedBar(context, membersCount, positiveVoteCount ,negativeVoteCount, true);
        StackedBar.Percentages percentages = stackedBar.getPercentages();
        tvMissing.setText(context.getString(R.string.missingVoteDesc, missingVoteCount, percentages.emptyPerc * 100 ));
        tvPositive.setText(context.getString(R.string.positiveVoteDesc, positiveVoteCount, percentages.positivePerc * 100));
        tvNegative.setText(context.getString(R.string.negativeVoteDesc, negativeVoteCount, percentages.negativePerc * 100));

        stackedBarContainer.addView(stackedBar, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return fragmentView;
    }


    private void myFinishFragment() {
        if (edLongDesc.getText().toString().equals(option.getNotes())){
            finishFragment();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setMessage(getParentActivity().getString(R.string.exitWithoutSaving));
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishFragment();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });
        builder.show();
    }

    @Override
    public boolean onBackPressed() {
        myFinishFragment();
        return false;
    }
}
