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
import org.pollgram.decision.adapter.OptionsAdapter;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.PollgramException;
import org.pollgram.decision.data.TextOption;
import org.pollgram.decision.service.PollgramDAO;
import org.pollgram.decision.service.PollgramFactory;
import org.pollgram.decision.service.PollgramService;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davide on 13/12/15.
 */
public class EditOptionsFragment extends BaseFragment {

    public static final String PAR_DECISION_ID = "PAR_DECISION_ID";

    private static final int SAVE_MENU_ITEM_ID = 1;

    private static final String LOG_TAG = "EDIT_DEC_FRAG";


    private PollgramDAO pollgramDAO;
    private PollgramService pollgramService;

    private OptionsAdapter newOptionAdapter;
    private Decision decision;
    private List<TextOption> options;

    public EditOptionsFragment(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        pollgramDAO = PollgramFactory.getPollgramDAO();
        pollgramService = PollgramFactory.getPollgramService();
        long decisionId  = getArguments().getLong(PAR_DECISION_ID);
        decision = pollgramDAO.getDecision(decisionId);
        List<Option> resultOptions = pollgramDAO.getOptions(decision);
        options = new ArrayList<TextOption>();
        for (Option o : resultOptions)
            options.add((TextOption)o);

        return true;
    }


    @Override
    public View createView(Context context) {
        // init
        newOptionAdapter = new OptionsAdapter(context, options, decision.isEditable());

        fragmentView = new SizeNotifierFrameLayout(context);
        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem nextItemMenu = menu.addItem(SAVE_MENU_ITEM_ID, 0);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        getParentActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        actionBar.setTitle(getParentActivity().getString(R.string.addNewtOptionsTitle));
        actionBar.setBackButtonImage(R.drawable.ic_arrow_back_white_24dp);
        nextItemMenu.setBackgroundResource(R.drawable.ic_done_white_36dp);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                switch (id) {
                    case UIUtils.ACTION_BAR_BACK_ITEM_ID:
                        finishFragment();
                        break;
                    case SAVE_MENU_ITEM_ID:
                        final List<Option> options;
                        try {
                            options = newOptionAdapter.getOptions();
                        } catch (PollgramException e) {
                            Log.w(LOG_TAG, "Error in getOption", e);
                            Toast.makeText(getParentActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setMessage(getParentActivity().getString(R.string.addOptionToDecisionQuestion,
                                options.size(), decision.getTitle()));
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

        ViewGroup myView = (ViewGroup) layoutInflater.inflate(R.layout.edit_optios_layout, (ViewGroup) fragmentView);
        ListView newOptionListView = (ListView) myView.findViewById(R.id.edit_option_list_view);
        newOptionListView.setAdapter(newOptionAdapter);

        return fragmentView;
    }


    private void saveDecision(List<Option> options) {
        pollgramService.notifyNewOptions(decision, options);
        Toast.makeText(getParentActivity(), R.string.decisionSaved, Toast.LENGTH_LONG).show();
        super.finishFragment();
        return;
    }

}