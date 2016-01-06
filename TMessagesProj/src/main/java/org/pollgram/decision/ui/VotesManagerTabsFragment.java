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

import org.pollgram.R;
import org.pollgram.decision.adapter.VoteListAdapter;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.UsersDecisionVotes;
import org.pollgram.decision.data.Vote;
import org.pollgram.decision.service.PollgramDAO;
import org.pollgram.decision.service.PollgramFactory;
import org.pollgram.decision.service.PollgramService;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import java.util.Collection;
import java.util.Date;

public abstract class VotesManagerTabsFragment extends Fragment {

    static final String LOG_TAG = "SlidingTabs";
    private final BaseFragment parentFragment;

    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;

    private PollgramDAO pollgramDAO;
    private PollgramService pollgramService;
    private UsersDecisionVotes usersDecisionVotes;

    private int currentUserId;
    private ViewGroup optionTableViewContainer;
    private long groupChatId;
    private VoteListAdapter voteListAdapter;
    private int[] participantsUserIds;


    public VotesManagerTabsFragment(BaseFragment parentFragment) {
        this.parentFragment = parentFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pollgramDAO = PollgramFactory.getPollgramDAO();
        pollgramService = PollgramFactory.getPollgramService();
        groupChatId = getArguments().getLong(VotesManagerFragment.PAR_GROUP_CHAT_ID);
        long decisionId = getArguments().getLong(VotesManagerFragment.PAR_DECISION_ID);
        participantsUserIds = getArguments().getIntArray(VotesManagerFragment.PAR_PARTICIPANT_IDS);
        usersDecisionVotes = pollgramService.getUsersDecisionVotes(decisionId, participantsUserIds);
        currentUserId = UserConfig.getCurrentUser().id;
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

    private boolean areThereNoOptions(){
        return usersDecisionVotes.getOptions().size() == 0;
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
            if (areThereNoOptions()) {
                rootView = inflater.inflate(R.layout.votes_manager_no_option_present, container, false);
            } else {
                switch (position) {
                    case OPTION_ID: {
                        rootView = getOptionsListView(container, inflater);
                        break;
                    }
                    case TABLE_VIEW_ID: {
                        optionTableViewContainer = new LinearLayout(getContext());
                        updateOptionsTableView(optionTableViewContainer, inflater);
                        rootView = optionTableViewContainer;
                        break;
                    }
                    default:
                        rootView = null;
                        break;
                }
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
        rootView = inflater.inflate(R.layout.votes_manager_list_tab, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.decision_option_lw_options);
        voteListAdapter = new VoteListAdapter(parentFragment, participantsUserIds,
                usersDecisionVotes.getDecision().isOpen());
        voteListAdapter.setData(usersDecisionVotes, currentUserId);
        listView.setAdapter(voteListAdapter);
        final Button btnSaveOption = (Button) rootView.findViewById(R.id.decision_option_btn_save_votes);
        btnSaveOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collection<Vote> votes2Save;
                if (usersDecisionVotes.atLeastOneIsNull(currentUserId)) {
                    votes2Save = voteListAdapter.getVotes();
                    // set to false null votes
                    for (Vote v : votes2Save) {
                        if (v.isVote() == null) {
                            v.setVote(false);
                            v.setVoteTime(new Date());
                        }
                    }
                } else
                    votes2Save = voteListAdapter.getNewVotes();

                Log.i(LOG_TAG, "saving votes[" + votes2Save + "]");
                pollgramService.notifyVote(usersDecisionVotes.getDecision(), votes2Save);
                btnSaveOption.setVisibility(View.GONE);

                // update view
                updateView();

                // Call method in order ti
                onVoteSaved();

                Toast.makeText(getContext(), R.string.voteSaved, Toast.LENGTH_SHORT).show();
            }
        });

        voteListAdapter.setOnVoteChangeListener(new VoteListAdapter.OnVoteChangeListener() {
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

    protected void updateView() {
        usersDecisionVotes = PollgramFactory.getPollgramService().
                getUsersDecisionVotes(usersDecisionVotes.getDecision().getId(),
                        usersDecisionVotes.getUsers());

        if (!areThereNoOptions()) {

            // set new sorted  votes in the voteListAdapter
            voteListAdapter.setData(usersDecisionVotes, currentUserId);
            voteListAdapter.setEditable(usersDecisionVotes.getDecision().isOpen());
            voteListAdapter.notifyDataSetChanged();

            // Update table user interface
            optionTableViewContainer.removeAllViews();
            updateOptionsTableView(optionTableViewContainer, getActivity().getLayoutInflater());
        }
    }

    @NonNull
    private View updateOptionsTableView(ViewGroup container, LayoutInflater inflater) {
        View rootView = inflater.inflate(R.layout.votes_manager_table_tab, container, false);

        TableLayout tableLayout = (TableLayout) rootView.findViewById(R.id.scrollable_part);
        TableLayout fixedColumn = (TableLayout) rootView.findViewById(R.id.fixed_column);

        // build first row
        {
            int firstRowHeight = AndroidUtilities.dp(50);
            TableRow row = newRow();
            // first cell is empty
            TextView emptyCell = new TextView(getContext());
            for (Option option : usersDecisionVotes.getOptions()) {
                TextView tvOption = new TextView(getContext());
                tvOption.setText(option.getTitle());
                tvOption.setTypeface(tvOption.getTypeface(), Typeface.BOLD);
                UIUtils.setDynamicTextSize(tvOption);

                LinearLayout rowLayout = new LinearLayout(getContext());
                rowLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, firstRowHeight));
                rowLayout.setBackgroundResource(R.drawable.cell_normal);
                rowLayout.addView(tvOption);

                add2Row(row, rowLayout, firstRowHeight);
            }
            tableLayout.addView(row);
            fixedColumn.addView(emptyCell, ViewGroup.LayoutParams.WRAP_CONTENT, firstRowHeight);
        }
        // build second row
        {
            int secondRowHeight = AndroidUtilities.dp(33);
            TextView emptyCell = new TextView(getContext());

            TableRow row = newRow();
            for (Option option : usersDecisionVotes.getOptions()) {
                LinearLayout rowLayout = new LinearLayout(getContext());
                rowLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, secondRowHeight));
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setGravity(Gravity.CENTER);

                ImageView starImageView = new ImageView(getContext());
                starImageView.setImageResource(R.drawable.assign_manager);
                starImageView.setVisibility(usersDecisionVotes.isWinningOption(option) ? View.VISIBLE : View.GONE);
                starImageView.setPadding(AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4), 0);
                rowLayout.addView(starImageView);


                TextView tvVoteCount = new TextView(getContext());
                tvVoteCount.setTextSize(18);
                tvVoteCount.setGravity(Gravity.CENTER);
                tvVoteCount.setText(Integer.toString(usersDecisionVotes.getPositiveVoteCount(option)));
                tvVoteCount.setHeight(secondRowHeight);
                rowLayout.addView(tvVoteCount);

                rowLayout.setBackgroundResource(R.drawable.cell_normal);
                add2Row(row, rowLayout,secondRowHeight);
            }
            tableLayout.addView(row);
            fixedColumn.addView(emptyCell, ViewGroup.LayoutParams.WRAP_CONTENT, secondRowHeight);
        }

        int otherRowHeight = AndroidUtilities.dp(33);

        // build other row row
        for (final TLRPC.User user : usersDecisionVotes.getUsers()) {
            TableRow row = newRow();

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
                usernameLayout.addView(avatarImageView, LayoutHelper.createFrame(30, 30, Gravity.CENTER, 10, 0, 10, 0));
            }

            {
                // username
                TextView userNameTv = new TextView(getContext());
                userNameTv.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
                userNameTv.setPadding(15, 0, 0, 0);
                userNameTv.setEllipsize(TextUtils.TruncateAt.END);
                userNameTv.setText(pollgramService.asString(user));
                int maxWith = AndroidUtilities.dp(80);
                userNameTv.setMaxWidth(maxWith);
                userNameTv.setMaxLines(1);
                userNameTv.setGravity(Gravity.CENTER_VERTICAL);

                usernameLayout.addView(userNameTv, maxWith, ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            // remind button
            Button remindButton = new Button(getContext());
            remindButton.setBackgroundResource(R.drawable.ic_smiles_bell_active);
            remindButton.setGravity(Gravity.LEFT);
            remindButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pollgramService.remindUserToVote(usersDecisionVotes.getDecision(), user);
                    String message = getContext().getString(R.string.remindToUserSent, pollgramService.asString(user));
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            });

            usernameLayout.addView(remindButton, LayoutHelper.createFrame(35, 35, Gravity.LEFT, 0, 3, 0, 0));
            usernameLayout.setBackgroundResource(R.drawable.cell_normal);
            fixedColumn.addView(usernameLayout, ViewGroup.LayoutParams.WRAP_CONTENT, otherRowHeight);

            boolean atLeastOneIsNull = usersDecisionVotes.atLeastOneIsNull(user.id);
            for (Option option : usersDecisionVotes.getOptions()) {
                Vote v = usersDecisionVotes.getVotes(user.id, option);
                add2Row(row, newVoteView(v), otherRowHeight);
            }
            if (!atLeastOneIsNull || user.id == currentUserId || !usersDecisionVotes.getDecision().isOpen())
                remindButton.setVisibility(View.INVISIBLE);

            tableLayout.addView(row);
        }
        container.addView(rootView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        return rootView;
    }

    private ImageView newVoteView(Vote v) {
        ImageView voteImageView = new ImageView(getContext());
        if (v.isVote() != null) {
            if (v.isVote()) {
                voteImageView.setImageResource(R.drawable.checkbig);
                voteImageView.setBackgroundResource(R.drawable.cell_vote_positive);
            } else {
                voteImageView.setImageResource(0);
                voteImageView.setBackgroundResource(R.drawable.cell_vote_negative);
            }
        } else {
            voteImageView.setImageResource(R.drawable.unknown_vote);
            voteImageView.setBackgroundResource(R.drawable.cell_vote_notpresent);
        }
        return voteImageView;
    }


    private TableRow newRow(){
        TableRow row = new TableRow(getContext());
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return row;
    }


    private void add2Row(TableRow row, View view, int height){
        view.setPadding(11,11,11,11);
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                height, Gravity.RIGHT));
        row.addView(view, ViewGroup.LayoutParams.WRAP_CONTENT, height);
    }

    /**
     * Called when votes are saved
     */
    protected abstract void onVoteSaved();

}
