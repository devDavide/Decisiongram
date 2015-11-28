package org.pollgram.decision.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.pollgram.decision.data.Decision;
import org.pollgram.R;
import org.pollgram.decision.service.PollgramFactory;

import java.util.List;

/**
 * Created by davide on 03/10/15.
 */
public class DecisionAdapter extends ArrayAdapter<Decision> {

    private static final int LAYOUT_RES_ID = R.layout.item_decision_list;
    private final int groupMemberCount;

    public DecisionAdapter(Context context,  List<Decision> items, int groupMemberCount) {
        super(context, LAYOUT_RES_ID, items);
        this.groupMemberCount = groupMemberCount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // init layout
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(LAYOUT_RES_ID, parent, false);
        ImageView decisionImage = (ImageView)rowView.findViewById(R.id.item_decision_iv_image);
        TextView decisionTitle = (TextView)rowView.findViewById(R.id.item_decision_tv_title);
        TextView decisionSubtitle = (TextView)rowView.findViewById(R.id.item_decision_tv_subtitle);


        // put data
        Decision decision = getItem(position);
        decisionTitle.setText(decision.getTitle());
        int userThatVoteSoFar = PollgramFactory.getPollgramDAO().getUserVoteCount(decision);
        String subTitle = getContext().getString(R.string.howManyMemberVote, userThatVoteSoFar,groupMemberCount);
        decisionSubtitle.setText(subTitle);
        if (!decision.isOpen())
            rowView.setBackgroundColor(Color.LTGRAY);

        return rowView;
    }
}
