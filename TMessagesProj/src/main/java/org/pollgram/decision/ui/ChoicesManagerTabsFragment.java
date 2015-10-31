/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pollgram.decision.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import org.pollgram.decision.adapter.ChoiceAdapter;
import org.pollgram.decision.dao.DecisionDAO;
import org.pollgram.decision.dao.DecisionDAOImpl;
import org.pollgram.decision.data.Choice;
import org.pollgram.decision.data.UsersDecisionVotes;
import org.pollgram.decision.data.Vote;
import org.pollgram.decision.utils.PolgramUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import java.util.Collection;
import java.util.List;

public class ChoicesManagerTabsFragment extends Fragment {

    static final String LOG_TAG = "SlidingTabs";

    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;

    private DecisionDAO decisionDAO;
    private List<Vote> votes;
    private UsersDecisionVotes usersDecisionVotes;

    public ChoicesManagerTabsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        decisionDAO = new DecisionDAOImpl();
        long decisionId = getArguments().getLong(ChoicesManagerFragment.PAR_DECISION_ID);
        votes = decisionDAO.getUserVoteForDecision(decisionId, UserConfig.getCurrentUser().id);
        int[] participantsUserIds = getArguments().getIntArray(ChoicesManagerFragment.PAR_PARTICIPANT_IDS);
        usersDecisionVotes = new DecisionDAOImpl().getUsersDecisionVotes(decisionId, participantsUserIds);
    }

    /**
     * Inflates the {@link View} which will be displayed by this {@link Fragment}, from the app's
     * resources.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tabbed_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setAdapter(new DecisionPagerAdapter());

        slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(viewPager);
    }


    /**
     * PagerAdapter for decisions
     */
    class DecisionPagerAdapter extends PagerAdapter {

        private static final int OPTION_ID = 0, TABLE_VIEW_ID = 1;

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 2;
        }

        /**
         * @return true if the value returned from {@link #instantiateItem(ViewGroup, int)} is the
         * same object as the {@link View} added to the {@link ViewPager}.
         */
        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link SlidingTabLayout}.
         * <p/>
         * Here we construct one using the position value, but for real application the title should
         * refer to the item's contents.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case OPTION_ID:
                    return getActivity().getString(R.string.optionTabName);
                case TABLE_VIEW_ID:
                    return getActivity().getString(R.string.tableViewTabName);
                default: {
                    Log.e(LOG_TAG, "Unknow tab position " + position);
                    return "tab " + position;
                }
            }
        }

        /**
         * Instantiate the {@link View} which should be displayed at {@code position}. Here we
         * inflate a layout from the apps resources and then change the text view to signify the position.
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View rootView = null;
            LayoutInflater inflater = getActivity().getLayoutInflater();
            switch (position) {
                case OPTION_ID: {
                    rootView = getOptionsListView(container, inflater);
                    break;
                }
                case TABLE_VIEW_ID: {
                    rootView = getOptionsTable(container, inflater);
                    break;
                }
                default:
                    rootView = null;
                    break;
            }
            container.addView(rootView);
            return rootView;
        }

        /**
         * Destroy the item from the {@link ViewPager}. In our case this is simply removing the
         * {@link View}.
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            Log.i(LOG_TAG, "destroyItem() [position: " + position + "]");
        }
    }

    @NonNull
    private View getOptionsListView(ViewGroup container, LayoutInflater inflater) {
        View rootView;
        rootView = inflater.inflate(R.layout.decision_option_list, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.decision_option_lw_options);
        final ChoiceAdapter adapter = new ChoiceAdapter(getActivity(), votes);
        listView.setAdapter(adapter);
        final Button btnSaveOption = (Button) rootView.findViewById(R.id.decision_option_btn_save_votes);
        btnSaveOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Collection<Vote> votest2save = adapter.getNewVoteSet();
                Log.i(LOG_TAG, "saving votes[" + votest2save + "]");
                decisionDAO.save(votest2save);
                Toast.makeText(getContext(), R.string.voteSaved, Toast.LENGTH_SHORT).show();
                ;
            }
        });

        adapter.setOnVoteChageListener(new ChoiceAdapter.OnVoteChangeListener() {
            @Override
            public void voteChanges(boolean areThereChangesToSave) {
                if (areThereChangesToSave)
                    btnSaveOption.setVisibility(View.VISIBLE);
                else
                    btnSaveOption.setVisibility(View.GONE);
            }
        });
        return rootView;
    }

    @NonNull
    private View getOptionsTable(ViewGroup container, LayoutInflater inflater) {
        View rootView = inflater.inflate(R.layout.decision_option_table, container,false);

        TableLayout tableLayout = (TableLayout) rootView.findViewById(R.id.scrollable_part);
        TableLayout fixedColumn = (TableLayout) rootView.findViewById(R.id.fixed_column);

        // build first row
        {
            int firstRowHeight = AndroidUtilities.dp(40);
            TableRow row = newRow();
            // first cell is empty
            TextView emptyCell = new TextView(getContext());

            //add2Row(row, emptyCell,-1);
            for (int j = 0; j < usersDecisionVotes.getChoices().size(); j++) {
                Choice c = usersDecisionVotes.getChoices().get(j);
                TextView tvChoice = new TextView(getContext());
                tvChoice.setText(c.getTitle());
                tvChoice.setGravity(Gravity.CENTER);
                tvChoice.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
                tvChoice.setTypeface(tvChoice.getTypeface(), Typeface.BOLD);
                tvChoice.setEllipsize(TextUtils.TruncateAt.END);
                tvChoice.setLines(1);
                tvChoice.setWidth(AndroidUtilities.dp(80));
                add2Row(row, tvChoice,firstRowHeight);
            }
            tableLayout.addView(row);
            fixedColumn.addView(emptyCell,ViewGroup.LayoutParams.WRAP_CONTENT, firstRowHeight);
        }
        // build second row
        {
            int secondRowHeight = AndroidUtilities.dp(33);

            TextView emptyCell = new TextView(getContext());
//            add2Row(row, emptyCell, -1);

            TableRow row = newRow();
            for (int j = 0; j < usersDecisionVotes.getChoices().size(); j++) {
                Choice c = usersDecisionVotes.getChoices().get(j);
                TextView tvVoteCount = new TextView(getContext());
                tvVoteCount.setTextSize(18);
                tvVoteCount.setGravity(Gravity.CENTER);
                tvVoteCount.setText(Integer.toString(c.getPositiveVoteCount()));
                tvVoteCount.setHeight(secondRowHeight);
                add2Row(row, tvVoteCount,secondRowHeight);
            }
            tableLayout.addView(row);
            fixedColumn.addView(emptyCell,ViewGroup.LayoutParams.WRAP_CONTENT, secondRowHeight);
        }

        int otherRowHeight = AndroidUtilities.dp(33);

        // build other row row
        for(int i=0; i < usersDecisionVotes.getUsers().size() ;i++){
            TableRow row = newRow();
            TLRPC.User user = usersDecisionVotes.getUsers().get(i);

            LinearLayout usernameLayout = new LinearLayout(getContext());
            usernameLayout.setOrientation(LinearLayout.HORIZONTAL);
            usernameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, AndroidUtilities.dp(20)));
            {
                // Avatar image
                BackupImageView avatarImageView = new BackupImageView(getContext());
                avatarImageView.setRoundRadius(AndroidUtilities.dp(21));
                TLRPC.FileLocation newPhoto = null;
                if (user.photo != null)
                    newPhoto = user.photo.photo_small;
                AvatarDrawable avatarDrawable = new AvatarDrawable(user);
                avatarImageView.setImage(newPhoto, "20_20", avatarDrawable);
                int imageSize = otherRowHeight - 3;
                usernameLayout.addView(avatarImageView, LayoutHelper.createFrame(30, 30,Gravity.CENTER,10,0,10,0));
            }

            {
                // username
                TextView userNameTv = new TextView(getContext());
                userNameTv.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
                userNameTv.setPadding(15, 0, 0, 0);
                userNameTv.setEllipsize(TextUtils.TruncateAt.END);
                userNameTv.setText(PolgramUtils.asString(user));
                int maxWith = AndroidUtilities.dp(105);
                userNameTv.setMaxWidth(maxWith);
                userNameTv.setMaxLines(1);
                userNameTv.setGravity(Gravity.CENTER_VERTICAL);

                usernameLayout.addView(userNameTv, maxWith, ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            // remind button
            Button remindButton = new Button(getContext());
            remindButton.setBackgroundResource(R.drawable.ic_smiles_bell_active);
            remindButton.setGravity(Gravity.LEFT);
            usernameLayout.addView(remindButton, LayoutHelper.createFrame(35, 35, Gravity.LEFT, 0, 3, 0, 0));
            usernameLayout.setBackgroundResource(R.drawable.cell_normal);
            fixedColumn.addView(usernameLayout, ViewGroup.LayoutParams.WRAP_CONTENT, otherRowHeight);
            //   add2Row(row, linearLayout);

            boolean atLeastOneIsNull = false;
            for (int j = 0; j < usersDecisionVotes.getChoices().size(); j++) {
                Vote v = usersDecisionVotes.getVote(i, j);
                View item;
                if (v.isVote() != null) {
                    ImageView vote = new ImageView(getContext());
                    if (v.isVote()) {
                        vote.setImageResource(R.drawable.checkbig);
                        vote.setBackgroundResource(R.drawable.cell_vote_positive);
                        add2Row(row, vote, otherRowHeight,R.drawable.cell_vote_positive);
                    } else
                        add2Row(row, vote, otherRowHeight, R.drawable.cell_vote_negative);
                } else {
                    atLeastOneIsNull = true;
                    TextView noVoteTv = new TextView(getContext());
                    noVoteTv.setText(R.string.no_vote_desc);
                    noVoteTv.setTypeface(noVoteTv.getTypeface(), Typeface.BOLD);
                    noVoteTv.setGravity(Gravity.CENTER);
                    noVoteTv.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
                    add2Row(row, noVoteTv, otherRowHeight,R.drawable.cell_vote_notpresent);
                }
            }
            if (!atLeastOneIsNull)
                remindButton.setVisibility(View.INVISIBLE);

            tableLayout.addView(row);
        }
        return rootView;
    }


    private TableRow newRow(){
        TableRow row = new TableRow(getContext());
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return row;
    }


    private void add2Row(TableRow row, View view, int height, int drawableBackGround){
        view.setPadding(11,11,11,11);
        view.setBackgroundResource(drawableBackGround);
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                height, Gravity.RIGHT));
        row.addView(view, ViewGroup.LayoutParams.WRAP_CONTENT, height);
    }

    private void add2Row(TableRow row, View view, int heigth) {
        add2Row(row, view, heigth, R.drawable.cell_normal);
    }

}
