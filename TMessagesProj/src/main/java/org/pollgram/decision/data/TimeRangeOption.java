package org.pollgram.decision.data;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by davide on 03/10/15.
 */
public class TimeRangeOption extends Choice {

    private final DateFormat DAY_DF = new SimpleDateFormat("yyyy/MM/dd");
    private final DateFormat DAY_TIME_DF = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

    private final Date from;
    private final Date to;
    private final Accuracy accuracy;

    public static enum Accuracy {
        DAY, HOUR;
    }

    public TimeRangeOption(long id, int positiveVoteCount, int negativeVoteCount, long id1, Date from, Date to, Accuracy accuracy) {
        super(id, positiveVoteCount, negativeVoteCount);
        id = id1;
        this.from = from;
        this.to = to;
        this.accuracy = accuracy;
    }

    @Override
    public long getId() {
        return id;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeRangeOption that = (TimeRangeOption) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
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
