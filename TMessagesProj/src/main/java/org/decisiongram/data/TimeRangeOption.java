package org.decisiongram.data;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by davide on 03/10/15.
 */
public class TimeRangeOption extends Option {

    private final DateFormat DAY_DF = new SimpleDateFormat("yyyy/MM/dd");
    private final DateFormat DAY_TIME_DF = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

    private final Date from;
    private final Date to;
    private final Accuracy accuracy;

    public static enum Accuracy {
        DAY, HOUR;
    }

    public TimeRangeOption(Date from, Date to, Accuracy accuracy, long decisionId) {
        super(decisionId);
        this.from = from;
        this.to = to;
        this.accuracy = accuracy;

    }


    public TimeRangeOption(long id, Date from, Date to, Accuracy accuracy, long decisionId) {
        this(from, to,accuracy, decisionId);
        setId(id);

    }

    public Date getFrom() {
        return from;
    }

    public Date getTo() {
        return to;
    }

    public Accuracy getAccuracy() {
        return accuracy;
    }

    @Override
    public String getTitle() {
        // TODO
        return from + " - " + to;
    }

    @Override
    public String toString() {
        if (from.equals(to))
            return format(from);
        else
            return format(from) + " - " + format(to);
    }

    private String format(Date date) {
        switch (accuracy) {
            case HOUR:
                return DAY_TIME_DF.format(date);
            case DAY:
                return DAY_DF.format(date);
            default: {
                Log.e("TIME-RANGE", "unknown accuracy " + accuracy);
                return "null";
            }
        }
    }
}
