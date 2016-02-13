package org.decisiongram.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.decisiongram.R;
import org.decisiongram.data.Decision;
import org.decisiongram.service.DecisiongramFactory;
import org.decisiongram.service.DecisionService;

import java.text.DateFormat;
import java.util.List;

/**
 * Created by davide on 03/10/15.
 */
public class DecisionAdapter extends ArrayAdapter<Decision> {

    private static final int LAYOUT_RES_ID = R.layout.item_decision_list;
    private final int groupMemberCount;
    private final DecisionService decisiongramService;
    private final LayoutInflater inflater;

    /**
     *
     * @param context
     * @param items
     * @param groupMemberCount or -1 for do not show this info
     */
    public DecisionAdapter(Context context,  List<Decision> items, int groupMemberCount) {
        super(context, LAYOUT_RES_ID, items);
        this.groupMemberCount = groupMemberCount;
        decisiongramService = DecisiongramFactory.getService();
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * do not show how many users votes out of the total
     * @param context
     * @param items
     */
    public DecisionAdapter(Context context, List<Decision> items){
        this(context, items, -1);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // init layout
        View rowView = inflater.inflate(LAYOUT_RES_ID, parent, false);
        TextView tvDecisionTitle = (TextView)rowView.findViewById(R.id.item_decision_tv_title);
        TextView tvDecisionSubtitle1 = (TextView)rowView.findViewById(R.id.item_decision_tv_subtitle_1);
        TextView tvDecisionSubtitle2 = (TextView)rowView.findViewById(R.id.item_decision_tv_subtitle_2);
        TextView tvAdmin = (TextView)rowView.findViewById(R.id.item_decision_tv_admin);


        // put data
        Decision decision = getItem(position);
        tvDecisionTitle.setText(decision.getTitle());
        int userThatVoteSoFar = DecisiongramFactory.getDAO().getUserVoteCount(decision);
        String userAsString = decisiongramService.asString(decisiongramService.getUser(decision.getUserCreatorId()));
        String creationDateStr = DateFormat.getDateInstance(DateFormat.SHORT).
                format(decision.getCreationDate());
        tvDecisionSubtitle1.setText(getContext().getString(R.string.createdByUserOnDay, userAsString, creationDateStr));
        if (groupMemberCount == -1)
            tvDecisionSubtitle2.setVisibility(View.GONE);
        else
            tvDecisionSubtitle2.setText(getContext().getString(R.string.howManyMemberVote, userThatVoteSoFar, groupMemberCount));

        tvAdmin.setVisibility(decision.isEditable() ? View.VISIBLE : View.GONE);
        if (!decision.isOpen())
            rowView.setBackgroundColor(Color.LTGRAY);

        return rowView;
    }
}
