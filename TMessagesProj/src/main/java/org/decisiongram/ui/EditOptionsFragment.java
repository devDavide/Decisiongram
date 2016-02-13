package org.decisiongram.ui;

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

import org.decisiongram.R;
import org.decisiongram.adapter.OptionsAdapter;
import org.decisiongram.data.Decision;
import org.decisiongram.data.Option;
import org.decisiongram.data.PollgramException;
import org.decisiongram.data.TextOption;
import org.decisiongram.service.DecisionDAO;
import org.decisiongram.service.DecisiongramFactory;
import org.decisiongram.service.DecisionService;
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
    public static final String PAR_NEW_OPTION_LONG_DESC = "PAR_NEW_OPTION_LONG_DESC";

    private static final int SAVE_MENU_ITEM_ID = 1;

    private static final String LOG_TAG = "EDIT_DEC_FRAG";


    private DecisionDAO decisiongramDAO;
    private DecisionService decisiongramService;

    private OptionsAdapter optionsAdapter;
    private Decision decision;
    private List<TextOption> options;

    public EditOptionsFragment(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        decisiongramDAO = DecisiongramFactory.getDAO();
        decisiongramService = DecisiongramFactory.getService();
        long decisionId  = getArguments().getLong(PAR_DECISION_ID);
        String newOptionLongDesc = getArguments().getString(PAR_NEW_OPTION_LONG_DESC);

        options = new ArrayList<TextOption>();
        if (newOptionLongDesc != null)
            options.add(new TextOption(null, newOptionLongDesc, decisionId));

        decision = decisiongramDAO.getDecision(decisionId);
        List<Option> resultOptions = decisiongramDAO.getOptions(decision);
        for (Option o : resultOptions)
            options.add((TextOption)o);

        return true;
    }


    @Override
    public View createView(final Context context) {
        // init
        optionsAdapter = new OptionsAdapter(context, options, decision.isEditable());

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
                        final List<Option> newOptions;
                        final List<Option> deletedOptions;
                        try {
                            newOptions = optionsAdapter.getNewOptions();
                        } catch (PollgramException e) {
                            Log.w(LOG_TAG, "Error in getOption", e);
                            Toast.makeText(getParentActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        deletedOptions = optionsAdapter.getDeletedOptions();

                        Log.i(LOG_TAG, "option added["+newOptions+"] option deleted["+deletedOptions+"]");
                        String message;

                        if (deletedOptions.size() == 0) {
                            if (newOptions.size() == 0){
                                Toast.makeText(context,R.string.nothingToSave,Toast.LENGTH_SHORT).show();
                                return;
                            }
                            message = context.getString(R.string.addOptionToDecisionQuestion,
                                    newOptions.size(), decision.getTitle());
                        } else
                            message = context.getString(R.string.addRemoveOptionToDecisionQuestion,
                                    newOptions.size(), deletedOptions.size(), decision.getTitle());

                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setMessage(message);
                        builder.setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        saveDecision(newOptions,deletedOptions);
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
        newOptionListView.setAdapter(optionsAdapter);

        return fragmentView;
    }


    private void saveDecision(List<Option> newOptions, List<Option> deleteOptions) {
        if (deleteOptions.size() > 0)
            decisiongramService.notifyDeleteOptions(decision, deleteOptions);


        if (newOptions.size() > 0)
            decisiongramService.notifyNewOptions(decision, newOptions);

        Toast.makeText(getParentActivity(), R.string.decisionSaved, Toast.LENGTH_LONG).show();
        EditOptionsFragment.this.finishFragment();
        return;
    }

}