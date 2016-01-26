package org.pollgram.decision.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.pollgram.R;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.Components.FrameLayoutFixed;
import org.telegram.ui.Components.LayoutHelper;

/**
 * Created by davide on 08/10/15.
 */
class UIUtils {

    public static final int ACTION_BAR_BACK_ITEM_ID = -1;

    private static final float TITLE_FONT_SIZE = 19;
    private static final String LOG_TAG = "uiUtils";


    public static DialogInterface.OnClickListener emptyOnClickListener(){
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // nothing to do
            }
        };
    }

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
            pollIconContainer.addView(pollIcon, LayoutHelper.createFrame(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM | Gravity.LEFT, 0, 0, 0, 8));
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

    /**
     * @param ed
     * @return a string from and edit text trimming it and removing non necessary newline suffix
     */
    public static String getText(EditText ed){
        if (ed == null || ed.getText() == null)
            return null;
        String text = ed.getText().toString().trim();

        if (text.length() > 2 && text.charAt(text.length()-1)== '\n')
            text = text.substring(0,text.length()-2);
        return text;
    }

    /**
     * Constant for setDynamicTextSize() method
     */
    private static final float MAX_FONT_SIZE = 19;
    private static final float MIN_FONT_SIZE = 11;
    private static final int MIN_TEXT_THRESHOLD_LEN = 15;
    private static final int MAX_TEXT_THRESHOLD_LEN = 40;
    private static final float SCALE = (MAX_FONT_SIZE- MIN_FONT_SIZE) / (MAX_TEXT_THRESHOLD_LEN - MIN_TEXT_THRESHOLD_LEN);

    public static void setDynamicTextSize(TextView tv){
        int len = tv.getText() == null ? 0 : tv.getText().toString().length();
        float fontSize;
        if (len < MIN_TEXT_THRESHOLD_LEN)
            fontSize = MAX_FONT_SIZE;
        else if (len > MAX_TEXT_THRESHOLD_LEN)
            fontSize = MIN_FONT_SIZE;
        else
            fontSize = MIN_FONT_SIZE + SCALE * (MAX_TEXT_THRESHOLD_LEN - len);

        int lines;
        if (len < 9)
            lines = 1;
        if (fontSize == MIN_FONT_SIZE)
            lines = 3;
        else
            lines = 2;

        Log.d(LOG_TAG, "font size returned :" + fontSize + " lines:"+lines);

        tv.setTextSize(fontSize);
        tv.setLines(lines);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        tv.setWidth(AndroidUtilities.dp(100));
        tv.setGravity(Gravity.TOP|Gravity.LEFT);
    }


}
