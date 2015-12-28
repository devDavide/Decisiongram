package org.pollgram.decision.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.pollgram.R;
import org.pollgram.decision.adapter.NewOptionsAdapter;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.service.PollgramDAO;
import org.pollgram.decision.service.PollgramFactory;
import org.pollgram.decision.service.PollgramService;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

import java.util.Date;
import java.util.List;

/**
 * Created by davide on 13/12/15.
 */
public class NewDecisionFragment extends BaseFragment {

    static final String PAR_GROUP_CHAT_ID = "PAR_GROUP_CHAT_ID";
    private static final short PAGE_1 = 1;
    private static final short PAGE_2 = 2;

    private static final int NEXT_MENU_ITEM_ID = 1;

    private static final String LOG_TAG = "NEW_DEC_FRAG";
    private EditText edTitle;
    private EditText edLongDescription;

    private short currentPage;

    private Bundle wizardBundleArgs;
    private PollgramDAO pollgramDAO;
    private int groupChatId;
    private PollgramService pollgramService;

    // page 2
    private NewOptionsAdapter newOptionAdapter;
    private String decisionLongDescription;
    private String decisionTitle;
    private ActionBarMenuItem nextItemMenu;


    public NewDecisionFragment(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        pollgramDAO = PollgramFactory.getPollgramDAO();
        pollgramService = PollgramFactory.getPollgramService();
        wizardBundleArgs = new Bundle();
        groupChatId = getArguments().getInt(PAR_GROUP_CHAT_ID);

        decisionTitle = "";
        decisionLongDescription = "";
        return true;
    }


    @Override
    public View createView(Context context) {
        // init
        fragmentView = new SizeNotifierFrameLayout(context);
        ActionBarMenu menu = actionBar.createMenu();
        nextItemMenu = menu.addItem(NEXT_MENU_ITEM_ID, 0);
        newOptionAdapter = new NewOptionsAdapter(getParentActivity());
        getParentActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        showPage1();

        return fragmentView;
    }

    private void reset(){
        ((ViewGroup)fragmentView).removeAllViews();
    }

    private void showPage1() {
        currentPage = PAGE_1;
        reset();
        actionBar.setTitle(getParentActivity().getString(R.string.newDecision));
        actionBar.setBackButtonImage(R.drawable.ic_close_white);
        nextItemMenu.setBackgroundResource(R.drawable.ic_arrow_forward_white_24dp);
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
        LayoutInflater li = LayoutInflater.from(getParentActivity());
        View myView = li.inflate(R.layout.new_decision_step1_layout, (ViewGroup) fragmentView);

        edTitle = (EditText) myView.findViewById(R.id.new_decision_ed_title);
        edTitle.setText(decisionTitle);
        edLongDescription = (EditText) myView.findViewById(R.id.new_decision_ed_long_description);
        edLongDescription.setText(decisionLongDescription);
    }


    private void showPage2() {
        currentPage = PAGE_2;
        reset();
        actionBar.setTitle(getParentActivity().getString(R.string.selectOptions));
        actionBar.setBackButtonImage(R.drawable.ic_arrow_back_white_24dp);
        nextItemMenu.setBackgroundResource(R.drawable.ic_done_white_36dp);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                switch (id){
                    case UIUtils.ACTION_BAR_BACK_ITEM_ID :
                        showPage1();
                        break;
                    case NEXT_MENU_ITEM_ID:
                        final List<Option> options = newOptionAdapter.getOptions();
                        // check if the last option is not empty !!!
                        for (int i=0;i<options.size();i++){
                            if (options.get(i).getTitle() == null) {
                                Toast.makeText(getParentActivity(), getParentActivity().getString(R.string.emptyTitleOnOption, i+1),
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setMessage(getParentActivity().getString(R.string.saveDecisionQuestion,
                                decisionTitle ,options.size()));
                        builder.setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        saveDecision(options);
                                    }
                                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // nothing to do11
                            }
                        }).show();

                        break;
                    default:
                        Log.w(LOG_TAG, "unknown action bar menu id [" + id + "]");
                }
            }

        });


        LayoutInflater li = LayoutInflater.from(getParentActivity());
        ViewGroup myView = (ViewGroup) li.inflate(R.layout.new_decision_step2_layout, (ViewGroup) fragmentView);
        ListView newOptionListView = (ListView) myView.findViewById(R.id.new_decisions_option_list_view);
        newOptionListView.setAdapter(newOptionAdapter);
    }

    private void saveDecision(List<Option> options) {
        if (decisionTitle == null || decisionTitle.isEmpty()){
            Toast.makeText(getParentActivity(), R.string.pleaseSelectATitle, Toast.LENGTH_SHORT).show();
            return;
        }

        Decision decision = new Decision(groupChatId,
                UserConfig.getCurrentUser().id, decisionTitle,
                decisionLongDescription, new Date(), true);


        pollgramService.notifyNewDecision(decision, options);
        Toast.makeText(getParentActivity(), R.string.decisionSaved, Toast.LENGTH_LONG).show();

        super.finishFragment();
        return;
    }

    private void nextStep() {
        String title = edTitle.getText().toString();
        if (title == null || title.trim().isEmpty()){
            Toast.makeText(getParentActivity(), R.string.pleaseSelectATitle, Toast.LENGTH_SHORT).show();
            return;
        }
        Decision decision = pollgramDAO.getDecision(title, groupChatId);
        if (decision != null){
            Toast.makeText(getParentActivity(),R.string.decisionAlreadyExist,Toast.LENGTH_LONG).show();
            return;
        }

        decisionTitle = title;
        decisionLongDescription = edLongDescription.getText().toString();
        showPage2();
    }

    @Override
    public void onResume() {

    }

    @Override
    public boolean onBackPressed() {
        if (currentPage == PAGE_1)
            return true;
        else {
            showPage1();
            return false;
        }
    }
}