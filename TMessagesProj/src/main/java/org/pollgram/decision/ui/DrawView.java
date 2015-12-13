package org.pollgram.decision.ui;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
public class DrawView extends TextView {

    private static final String LOG_TAG ="DRAW_VIEW";

    private final Paint paint;
    private final float positivePerc;
    private final float emptyPerc;
    private final float negativePerc;
    private float height;
    private float width;
    private float padding;
    private float externalStroke;
    private float left;
    private float top;


    public DrawView(Context context, int totalUserCount, int positiveVoteCount, int userThatVoteCount) {
        super(context);

        Log.d(LOG_TAG, "totalUserCount["+totalUserCount+"] positiveVoteCount["+ positiveVoteCount +"] " +
                "userThatVoteCount["+userThatVoteCount+"]");

        this.emptyPerc = (float)(totalUserCount - userThatVoteCount) / totalUserCount;
        this.positivePerc = (float) positiveVoteCount/ totalUserCount;
        this.negativePerc = (float) 1 - emptyPerc - positivePerc;
        checkPerc();

        padding = AndroidUtilities.dp(10);
        externalStroke = AndroidUtilities.dp(1);
        setText("aaaa\naaaa\naaa\naa");
        paint = new Paint();
    }

    private void checkPerc() {
        float[] percs = new float[]{emptyPerc, positivePerc, negativePerc};
        for (float p : percs) {
            if (p > 1 || p < 0)
                throw new IllegalArgumentException("positivePerc [" + p + "]" +
                        " must be between 1 and 0");
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.left = left + padding;
        this.top = top + padding ;
        right -= padding;
        this.width = right - left;
        bottom -= padding;
        this.height = bottom - top;
        Log.d(LOG_TAG, "DrawView onLayout: left[" + left + "] top[" + top + "] height[" + height + "] width[" + width + "]");
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void onDraw(Canvas canvas) {
        drawRect(canvas, left, top, width, height, externalStroke);
    }

    private void drawRect(Canvas canvas, float left, float top, float width, float height, float externalStroke) {
        float myRight = left + width;
        float myBottom = top + height;

        paint.setColor(Color.BLACK);
        canvas.drawRect(left, top, myRight, myBottom, paint);

        left += externalStroke;
        top += externalStroke;
        myRight -= externalStroke;
        myBottom -= externalStroke;
        width -= (externalStroke *2);
        height -= (externalStroke*2);
        paint.setColor(Color.WHITE);
        canvas.drawRect(left, top, myRight, myBottom, paint);

        paint.setColor(Color.GREEN);
        //float left, float top, float right, float bottom,
        float myLeft = left;
        float myTop = top + (height * emptyPerc);
        myRight = left + width;
        myBottom = myTop + (height * positivePerc) ;
        canvas.drawRect(myLeft, myTop, myRight, myBottom, paint);

        paint.setColor(Color.RED);
        myTop = myBottom;
        myBottom = top + height;
        canvas.drawRect(myLeft, myTop, myRight, myBottom, paint);

    }

}
