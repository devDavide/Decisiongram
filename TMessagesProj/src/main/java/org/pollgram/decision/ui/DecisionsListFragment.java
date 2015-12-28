package org.pollgram.decision.ui;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.pollgram.R;
import org.pollgram.decision.adapter.DecisionAdapter;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.service.PollgramDAO;
import org.pollgram.decision.service.PollgramFactory;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davide on 30/09/15.
 */
public class DecisionsListFragment extends BaseFragment {
    private static int nextId = 1;
    private static final int ID_TOGGLE_OPEN_CLOSE_DECISIONS = nextId++;
    private static final int ID_PURGE_ALL_DATA = nextId++;
    private static final int ID_PUT_STUB_DATA_DATA = nextId++;

    private TLRPC.ChatFull chatInfo;
    private TLRPC.Chat currentChat;
    private ListView decisionsListView;
    private Context context;

    private boolean hideCloseDecision;
    private PollgramDAO pollgramDAO;
    private int[] participantsUserIds;
    private TextView tvSubtitle;

    public DecisionsListFragment(){
    }

    public DecisionsListFragment(Bundle bundle) {
        super(bundle);
    }

    @Override
    public boolean onFragmentCreate() {
        pollgramDAO = PollgramFactory.getPollgramDAO();
        hideCloseDecision = false;
        return true;
    }

    @Override
    public View createView(final Context context) {
        this.context = context;

        // set up action bar

        tvSubtitle = UIUtils.init(actionBar, R.string.groupDecision, R.drawable.decision);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        // Create menu
        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem headerItem = menu.addItem(0, R.drawable.ic_ab_other);
        final TextView viewOpenCloseTextView =  headerItem.addSubItem(ID_TOGGLE_OPEN_CLOSE_DECISIONS,
                context.getString(hideCloseDecision ? R.string.viewCloseDecision : R.string.hideCloseDecision),0 );
        headerItem.addSubItem(ID_PURGE_ALL_DATA, "Remove current chat decisions", 0);
        headerItem.addSubItem(ID_PUT_STUB_DATA_DATA, "Put stub data for current chat", 0);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == UIUtils.ACTION_BAR_BACK_ITEM_ID) {
                    finishFragment();
                    return;
                } else if (id == ID_TOGGLE_OPEN_CLOSE_DECISIONS) {
                    hideCloseDecision = !hideCloseDecision;
                    if (hideCloseDecision)
                        viewOpenCloseTextView.setText(R.string.viewCloseDecision);
                    else
                        viewOpenCloseTextView.setText(R.string.hideCloseDecision);
                } else if (id == ID_PURGE_ALL_DATA){
                    List<Decision> allDecisions = pollgramDAO.getDecisions(chatInfo.id, null);
                    for (Decision d : allDecisions){
                        pollgramDAO.delete(d);
                    }
                } else if (id == ID_PUT_STUB_DATA_DATA){
                    pollgramDAO.putStubData(currentChat.id, UserConfig.getCurrentUser().id);
                }
                updateResult();
            }
        });

        // inflate xml main layout
        fragmentView = new SizeNotifierFrameLayout(context);
        LayoutInflater li = LayoutInflater.from(context);
        View myView = li.inflate(R.layout.decision_list_layout, (ViewGroup) fragmentView);
        TextView tvTitle = (TextView) myView.findViewById(R.id.decision_list_tv_title);
        ViewGroup imageContainer = (ViewGroup)myView.findViewById(R.id.decision_icon_container);
        decisionsListView = (ListView) myView.findViewById(R.id.decision_list_list_view);
        //myView.setBackground(ApplicationLoader.getCachedWallpaper());

        // set up compoent values
        tvTitle.setText(currentChat.title);
        TLRPC.FileLocation newPhoto = currentChat.photo.photo_small;
        BackupImageView avatarImageView= new BackupImageView(context);
        avatarImageView.setRoundRadius(AndroidUtilities.dp(21));
        imageContainer.addView(avatarImageView, LayoutHelper.createFrame(64, 64, Gravity.TOP | Gravity.LEFT, 0, 3, 0, 3));
        avatarImageView.setImage(newPhoto, "50_50", new AvatarDrawable(currentChat));

        decisionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Decision decision = (Decision) parent.getAdapter().getItem(position);
                Bundle bundle = new Bundle();
                bundle.putLong(VotesManagerFragment.PAR_GROUP_CHAT_ID, chatInfo.id);
                bundle.putLong(VotesManagerFragment.PAR_DECISION_ID, decision.getId());
                bundle.putIntArray(VotesManagerFragment.PAR_PARTICIPANT_IDS, participantsUserIds);
                presentFragment(new VotesManagerFragment(bundle));

            }
        });
        updateResult();

        // Addig "plus" floating button
        /// TODO add to layout or createutil methosd ?
        ImageView floatingButton = new ImageView(context);
        floatingButton.setScaleType(ImageView.ScaleType.CENTER);
        floatingButton.setBackgroundResource(R.drawable.floating_states);
        floatingButton.setImageResource(R.drawable.floating_plus);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            floatingButton.setStateListAnimator(animator);
            floatingButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }

        SizeNotifierFrameLayout contentView = (SizeNotifierFrameLayout) fragmentView;
        contentView.addView(floatingButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, 14));
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putInt(NewDecisionStep1TitleFragment.PAR_GROUP_CHAT_ID, chatInfo.id);
                presentFragment(new NewDecisionStep1TitleFragment(DecisionsListFragment.this, args));
            }
        });
        return fragmentView;
    }

    private void updateResult() {
        Boolean queryPar = hideCloseDecision ? true : null;
        List<Decision> allDecisions = pollgramDAO.getDecisions(chatInfo.id, null);
        List<Decision> filterDecision = new ArrayList<>();
        int openCount = 0 ;
        for (Decision d : allDecisions){
            if (d.isOpen()){
                openCount++;
                filterDecision.add(d);
            } else if (!hideCloseDecision)
                filterDecision.add(d);
        }

        tvSubtitle.setText(context.getString(R.string.decisionsCount,openCount, allDecisions.size() - openCount));
        decisionsListView.setAdapter(new DecisionAdapter(context, filterDecision, currentChat.participants_count));
    }


    public void setChatInfo(TLRPC.ChatFull chatInfo) {
        this.chatInfo = chatInfo;
        participantsUserIds = new int[chatInfo.participants.participants.size()];
        for (int i = 0; i < chatInfo.participants.participants.size() ; i++){
            participantsUserIds[i] = chatInfo.participants.participants.get(i).user_id;
        }
        this.currentChat = MessagesController.getInstance().getChat(chatInfo.id);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateResult();
    }
}
