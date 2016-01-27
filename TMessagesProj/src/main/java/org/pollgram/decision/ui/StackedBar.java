package org.pollgram.decision.ui;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
public class StackedBar extends TextView {

    private static final String LOG_TAG ="StackedBar";

    private static final String PROTOTYPE_TEXT_VALUE_THIN = "aaaa\n\n\n" ;
    private static final String PROTOTYPE_TEXT_VALUE_LARGE = "aaaaaaaaaaa\n\n\n\n\n\n\n\n\n\n\n" ;
    static final int MISSING_VOTE_COLOR = Color.WHITE;
    static final int POSITIVE_VOTE_COLOR = Color.GREEN;
    static final int NEGATIVE_VOTE_COLOR = Color.RED;

    private final Percentages percentages;
    private final Paint paint;
    private float height;
    private float width;
    private float padding;
    private float externalStroke;
    private float left;
    private float top;

    static class Percentages {
        protected final float positivePerc;
        protected final float emptyPerc;
        protected final float negativePerc;

        private Percentages(float emptyPerc, float positivePerc, float negativePerc) {
            this.positivePerc = positivePerc;
            this.emptyPerc = emptyPerc;
            this.negativePerc = negativePerc;
            checkPerc();
        }

        private void checkPerc() {
            float[] percs = new float[]{emptyPerc, positivePerc, negativePerc};
            for (int i=0; i <percs.length ; i++){
                if (percs[i] > 1 || percs[i] < 0)
                    throw new IllegalArgumentException("perc["+i+"]  [" + percs[i] + "]" +
                            " must be between 1 and 0");
            }
        }
    }

    static Percentages getPercentages(int totalUserCount, int positiveVoteCount, int negativeVoteCount) {
        if (totalUserCount == 0)
            return new Percentages(1, 0, 0);

        float emptyPercentage = (float) (totalUserCount - (positiveVoteCount + negativeVoteCount)) / totalUserCount;
        float positivePercentage = (float) positiveVoteCount / totalUserCount;
        float negativePercentage = (float) negativeVoteCount / totalUserCount;
        return new Percentages(emptyPercentage, positivePercentage, negativePercentage);
    }

    /**
     *
     * @param context
     * @param totalUserCount
     * @param positiveVoteCount
     * @param negativeVoteCount
     * @param large true if large false if thin
     */
    public StackedBar(Context context, int totalUserCount, int positiveVoteCount, int negativeVoteCount, boolean large) {
        super(context);

        Log.d(LOG_TAG, "totalUserCount["+totalUserCount+"] positiveVoteCount["+ positiveVoteCount +"] " +
                "negativeVoteCount["+negativeVoteCount+"]");

        this.percentages = getPercentages(totalUserCount, positiveVoteCount, negativeVoteCount);

        padding = AndroidUtilities.dp(10);
        externalStroke = AndroidUtilities.dp(1);
        setText(large ? PROTOTYPE_TEXT_VALUE_LARGE : PROTOTYPE_TEXT_VALUE_THIN);
        paint = new Paint();
    }

    public Percentages getPercentages() {
        return percentages;
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
        paint.setColor(MISSING_VOTE_COLOR);
        canvas.drawRect(left, top, myRight, myBottom, paint);

        paint.setColor(POSITIVE_VOTE_COLOR);
        float myLeft = left;
        float myTop = top + (height * percentages.emptyPerc);
        myRight = left + width;
        myBottom = myTop + (height * percentages.positivePerc) ;
        canvas.drawRect(myLeft, myTop, myRight, myBottom, paint);

        paint.setColor(NEGATIVE_VOTE_COLOR);
        myTop = myBottom;
        myBottom = top + height;
        canvas.drawRect(myLeft, myTop, myRight, myBottom, paint);

    }

}
