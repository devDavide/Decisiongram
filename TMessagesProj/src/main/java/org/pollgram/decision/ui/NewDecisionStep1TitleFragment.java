package org.pollgram.decision.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.pollgram.R;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.service.PollgramDAO;
import org.pollgram.decision.service.PollgramFactory;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

/**
 * Created by davide on 13/12/15.
 */
public class NewDecisionStep1TitleFragment extends BaseFragment {

    static final String PAR_DECISION_TITLE = "PAR_DECISION_TITLE";
    static final String PAR_DECISION_LONG_DESCRIPTION = "PAR_DECISION_LONG_DESCRIPTION";
    static final String PAR_GROUP_CHAT_ID = "PAR_GROUP_CHAT_ID";

    private static final int NEXT_MENU_ITEM_ID = 1;
    private static final String LOG_TAG = "NEW_DEC_FRAG";
    private final BaseFragment previousFragemnt;
    private EditText edTitle;
    private EditText edLongDescription;

    private NewDecisionStep2OptionsFragment newDecisionStep2OptionsFragment;
    private Bundle wizardBundleArgs;
    private PollgramDAO pollgramDAO;
    private int groupChatId;

    public NewDecisionStep1TitleFragment(BaseFragment previousFragemnt, Bundle args) {
        super(args);
        this.previousFragemnt =previousFragemnt;
    }

    @Override
    public boolean onFragmentCreate() {
        pollgramDAO = PollgramFactory.getPollgramDAO();
        wizardBundleArgs = new Bundle();
        newDecisionStep2OptionsFragment = new NewDecisionStep2OptionsFragment(previousFragemnt,wizardBundleArgs);
        groupChatId = getArguments().getInt(PAR_GROUP_CHAT_ID);

        return true;
    }


    @Override
    public View createView(Context context) {
        UIUtils.init(actionBar, R.string.new_decision, R.drawable.decision);
        ActionBarMenu menu = actionBar.createMenu();
        actionBar.setBackButtonImage(R.drawable.ic_close_white);
        menu.addItem(NEXT_MENU_ITEM_ID, R.drawable.ic_arrow_forward_white_24dp);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                switch (id) {
                    case UIUtils.ACTION_BAR_BACK_ITEM_ID:
                        finishFragment();
                        break;
                    case NEXT_MENU_ITEM_ID:
                        nextStep();
                        break;
                    default:
                        Log.w(LOG_TAG, "unknown action bar menu id [" + id + "]");
                }
            }

        });
        fragmentView = new SizeNotifierFrameLayout(context);
        LayoutInflater li = LayoutInflater.from(context);
        View myView = li.inflate(R.layout.new_decision_step1_layout, (ViewGroup) fragmentView);

        edTitle = (EditText) myView.findViewById(R.id.new_decision_ed_title);
        edLongDescription = (EditText) myView.findViewById(R.id.new_decision_ed_long_description);

        return fragmentView;
    }

    private void nextStep() {
        String title = edTitle.getText().toString();
        if (title == null || title.trim().isEmpty()){
            Toast.makeText(getParentActivity(), R.string.please_select_a_title, Toast.LENGTH_SHORT).show();
            return;
        }
        Decision decision = pollgramDAO.getDecision(title, groupChatId);
        if (decision != null){
            Toast.makeText(getParentActivity(),R.string.decisionAlreadyExist,Toast.LENGTH_LONG).show();
            return;
        }

        wizardBundleArgs.putInt(PAR_GROUP_CHAT_ID,groupChatId);
        wizardBundleArgs.putString(PAR_DECISION_TITLE, title);
        wizardBundleArgs.putString(PAR_DECISION_LONG_DESCRIPTION, edLongDescription.getText().toString());
        presentFragment(newDecisionStep2OptionsFragment);
    }

    @Override
    public void onResume() {

    }
}
