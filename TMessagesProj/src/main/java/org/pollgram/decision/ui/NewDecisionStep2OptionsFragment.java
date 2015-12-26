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
import android.widget.ListView;
import android.widget.Toast;

import org.pollgram.R;
import org.pollgram.decision.adapter.NewOptionsAdapter;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.service.PollgramFactory;
import org.pollgram.decision.service.PollgramService;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

import java.util.Date;
import java.util.List;

/**
 * Created by davide on 13/12/15.
 */
public class NewDecisionStep2OptionsFragment extends BaseFragment {

    private static final int SAVE_DECISION_MENU_ITEM_ID = 1;
    private static final String LOG_TAG = "NEW_DEC_FRAG";
    private final BaseFragment previousFragemnt;

    // Decision fields
    private int fullChatId;
    private String decisionLongDescription;
    private String decisionTitle;

    private NewOptionsAdapter newOptionAdapter;
    private PollgramService pollgramService;

    public NewDecisionStep2OptionsFragment(BaseFragment previousFragemnt, Bundle args) {
        super(args);
        this.previousFragemnt = previousFragemnt;
    }

    @Override
    public boolean onFragmentCreate() {
        pollgramService = PollgramFactory.getPollgramService();
        fullChatId = getArguments().getInt(NewDecisionStep1TitleFragment.PAR_GROUP_CHAT_ID);
        decisionTitle = getArguments().getString(NewDecisionStep1TitleFragment.PAR_DECISION_TITLE);
        decisionLongDescription = getArguments().getString(NewDecisionStep1TitleFragment.PAR_DECISION_LONG_DESCRIPTION);
        return true;
    }

    @Override
    public View createView(Context context) {

        getParentActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        UIUtils.init(actionBar,R.string.selectOptions, R.drawable.decision);
        ActionBarMenu menu = actionBar.createMenu();
        actionBar.setBackButtonImage(R.drawable.ic_arrow_back_white_24dp);
        menu.addItem(SAVE_DECISION_MENU_ITEM_ID, R.drawable.ic_done_white_36dp);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                switch (id){
                    case UIUtils.ACTION_BAR_BACK_ITEM_ID :
                        finishFragment();
                        break;
                    case SAVE_DECISION_MENU_ITEM_ID:
                        final List<Option> options = newOptionAdapter.getOptions();
                        // check if the last option is not empty !!!
                        for (int i=0;i<options.size();i++){
                            if (options.get(i).getTitle() == null) {
                                Toast.makeText(getParentActivity(), getParentActivity().getString(R.string.emptyTitleOnOption,i),
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
                        Log.w(LOG_TAG,"unknown action bar menu id ["+id +"]");
                }
            }

        });

        fragmentView = new SizeNotifierFrameLayout(context);
        LayoutInflater li = LayoutInflater.from(context);
        ViewGroup myView = (ViewGroup) li.inflate(R.layout.new_decision_step2_layout, (ViewGroup) fragmentView);
        ListView newOptionListView = (ListView) myView.findViewById(R.id.new_decisions_option_list_view);
        newOptionListView.setAdapter(newOptionAdapter = new NewOptionsAdapter(getParentActivity()));
        return  fragmentView;
    }

    private void saveDecision(List<Option> options) {
        if (decisionTitle == null || decisionTitle.isEmpty()){
            Toast.makeText(getParentActivity(), R.string.pleaseSelectATitle, Toast.LENGTH_SHORT).show();
            return;
        }

        Decision decision = new Decision(fullChatId,
                UserConfig.getCurrentUser().id, decisionTitle,
                decisionLongDescription, new Date(), true);


        pollgramService.notifyNewDecision(decision, options);
        Toast.makeText(getParentActivity(), R.string.decisionSaved, Toast.LENGTH_LONG).show();

        // TODO not so goood :-(
        presentFragment(previousFragemnt);
        return;
    }

}
