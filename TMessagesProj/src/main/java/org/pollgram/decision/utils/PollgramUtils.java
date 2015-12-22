package org.pollgram.decision.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import org.pollgram.R;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.Components.FrameLayoutFixed;
import org.telegram.ui.Components.LayoutHelper;

import java.nio.charset.Charset;

/**
 * Created by davide on 08/10/15.
 */
public class PollgramUtils {

    private static final float TITLE_FONT_SIZE = 19;

    public static TextView init(ActionBar actionBar, int title, int drawableIcon){
        return init(actionBar, ApplicationLoader.applicationContext.getString(title), drawableIcon);
    }

    /**
     *
     * @param actionBar
     * @param title
     * @param drawableIcon
     * @return the textView containing the subtitle
     */
    public static TextView init(ActionBar actionBar, String title,int drawableIcon){
        // set up poll icon
        Context context = ApplicationLoader.applicationContext;
        FrameLayoutFixed pollIconContainer = new FrameLayoutFixed(context);
        pollIconContainer.setBackgroundResource(R.drawable.bar_selector);
        if (drawableIcon != -1) {
            pollIconContainer.setPadding(AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8), 0);
            TextView pollIcon = new TextView(context);
            pollIcon.setBackgroundResource(drawableIcon);
            pollIconContainer.addView(pollIcon, LayoutHelper.createFrame(42, 42, Gravity.BOTTOM | Gravity.LEFT, 0, 0, 0, 8));
        }

        TextView txtTitle = new TextView(context);
        txtTitle.setTextColor(0xffffffff);
        txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TITLE_FONT_SIZE);
        txtTitle.setLines(1);
        txtTitle.setMaxLines(1);
        txtTitle.setSingleLine(true);
        txtTitle.setEllipsize(TextUtils.TruncateAt.END);
        txtTitle.setGravity(Gravity.LEFT);
        txtTitle.setCompoundDrawablePadding(AndroidUtilities.dp(4));
        txtTitle.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        txtTitle.setText(title);
        pollIconContainer.addView(txtTitle, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                Gravity.LEFT | Gravity.BOTTOM, 48, 0, 0, 22));

        // subtitle
        TextView subtitleTextView = new TextView(context);
        subtitleTextView.setTextColor(0xffd7e8f7);
        subtitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        subtitleTextView.setLines(1);
        subtitleTextView.setMaxLines(1);
        subtitleTextView.setSingleLine(true);
        subtitleTextView.setEllipsize(TextUtils.TruncateAt.END);
        subtitleTextView.setGravity(Gravity.LEFT);
        pollIconContainer.addView(subtitleTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 54, 0, 0, 4));


        actionBar.addView(pollIconContainer,  LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT,
                Gravity.TOP | Gravity.LEFT, 36, 0, 40, 0));

        return subtitleTextView;
    }

    public static String getEmojiAsString(byte... emojiBytes){
        return new String(emojiBytes, Charset.forName("UTF-8"));
    }

}
