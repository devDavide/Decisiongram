package org.pollgram.decision.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import org.pollgram.R;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.Components.FrameLayoutFixed;
import org.telegram.ui.Components.LayoutHelper;

import java.nio.charset.Charset;

/**
 * Created by davide on 08/10/15.
 */
public class PollgramUtils {

    public static ActionBar init(ActionBar actionBar, int title,int titleFontSize, int drawableIcon){
        return init(actionBar, ApplicationLoader.applicationContext.getString(title), titleFontSize,drawableIcon);
    }

    public static ActionBar init(ActionBar actionBar, String title,int titleFontSize, int drawableIcon){
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
        txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, titleFontSize);
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

        actionBar.addView(pollIconContainer,  LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT,
                Gravity.TOP | Gravity.LEFT, 36, 0, 40, 0));

        return actionBar;
    }

    public static String asString(TLRPC.User user){
        if (user.id / 1000 != 777 && user.id / 1000 != 333 &&
                ContactsController.getInstance().contactsDict.get(user.id) == null &&
                (ContactsController.getInstance().contactsDict.size() != 0 || !ContactsController.getInstance().isLoadingContacts())) {
            if (user.phone != null && user.phone.length() != 0) {
                return PhoneFormat.getInstance().format("+" + user.phone);
            } else {
                return UserObject.getUserName(user);
            }
        } else {
            return UserObject.getUserName(user);
        }
    }

    public static String getEmojiAsString(byte... emojiBytes){
        return new String(emojiBytes, Charset.forName("UTF-8"));
    }

}
