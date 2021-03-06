package org.decisiongram.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.decisiongram.R;
import org.decisiongram.data.Decision;
import org.decisiongram.service.DecisionDAO;
import org.decisiongram.service.DecisiongramFactory;
import org.decisiongram.service.DecisionService;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

import java.text.DateFormat;

/**
 * Created by davide on 31/12/15.
 */
public class DecisionDetailFragment extends BaseFragment {
    private static final String LOG_TAG = "DecisionDetail";

    static final String PAR_DECISION_ID = "PAR_DECISION_ID";
    private DecisionDAO decisiongramDAO;
    private Decision decision;
    private DecisionService decisiongramService;

    public DecisionDetailFragment(Bundle bundle) {
        super(bundle);
    }

    @Override
    public boolean onFragmentCreate() {
        decisiongramDAO = DecisiongramFactory.getDAO();
        decisiongramService = DecisiongramFactory.getService();
        long decisionId = getArguments().getLong(PAR_DECISION_ID);
        decision = decisiongramDAO.getDecision(decisionId);
        return true;
    }


    @Override
    public View createView(final Context context) {

        actionBar.setTitle(getParentActivity().getString(R.string.decisionDetailTitle));
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                switch (id) {
                    case UIUtils.ACTION_BAR_BACK_ITEM_ID:
                        finishFragment();
                        break;
                    default:
                        Log.w(LOG_TAG, "unknown action bar menu id [" + id + "]");
                }
            }

        });
        boolean editEnabled = decision.isEditable() && decision.isOpen();


        fragmentView = new SizeNotifierFrameLayout(context);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View myView = layoutInflater.inflate(R.layout.decision_detail_layout, (ViewGroup) fragmentView);

        EditText edTitle = (EditText) myView.findViewById(R.id.decision_detail_ed_title);
        edTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,R.string.titleNotEditable,Toast.LENGTH_SHORT).show();
            }
        });
        edTitle.setText(decision.getTitle());
        {
            TextView tvCreationInfo = (TextView) myView.findViewById(R.id.decision_detail_tv_creation_info);
            String userAsString = decisiongramService.asString(decisiongramService.getUser(decision.getUserCreatorId()));
            String creationDateStr = DateFormat.getDateInstance(DateFormat.SHORT).
                    format(decision.getCreationDate());
            tvCreationInfo.setText(context.getString(R.string.createdByUserOnDay,userAsString,creationDateStr));
        }
        final EditText edLongDescription = (EditText) myView.findViewById(R.id.decision_detail_ed_long_description);
        edLongDescription.addTextChangedListener(new DefaultTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                Linkify.addLinks(edLongDescription, Linkify.ALL);
            }
        });
        edLongDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, R.string.longDescNotEditable, Toast.LENGTH_SHORT).show();
            }
        });
        edLongDescription.setText(decision.getLongDescription());

        Button ediOptionButton = (Button)myView.findViewById(R.id.decision_detail_edit_option);
        boolean buttonEnabled = decision.isEditable() && decision.isOpen();
        ediOptionButton.setEnabled(buttonEnabled);
        ediOptionButton.setVisibility(buttonEnabled ? View.VISIBLE : View.GONE);
        ediOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putLong(EditOptionsFragment.PAR_DECISION_ID, decision.getId());
                presentFragment(new EditOptionsFragment(bundle));
            }
        });


        return fragmentView;
    }
}

