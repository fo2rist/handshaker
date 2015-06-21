package com.weezlabs.handshakerphone.datastorage;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.weezlabs.handshakerphone.models.Attempt;
import com.weezlabs.handshakerphone.ui.AttemptsAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AttemptsManager {
    public static final long DAY_MILLISECONDS = 86400000L;
    public static final long JERKOFF_MIN_SECONDS = 3;
    private static AttemptsManager instance_ = null;

    private ArrayList<Attempt> attemptsList_ = new ArrayList<Attempt>();
    private AttemptsAdapter attemptsAdapter_ = null; //lazy init

    public static synchronized AttemptsManager getInstance() {
        if (instance_ == null) {
            instance_ = new AttemptsManager();
        }

        return instance_;
    }

    private AttemptsManager() {
        Date now = new Date();
        Date yesterday = new Date();
        yesterday.setTime(now.getTime() - DAY_MILLISECONDS);
        Date weekAgo = new Date();
        weekAgo.setTime(now.getTime() - DAY_MILLISECONDS * 4);
        Date someDayAgo = new Date();
        someDayAgo.setTime(now.getTime() - DAY_MILLISECONDS * 10);
        Date monthAgo = new Date();
        monthAgo.setTime(now.getTime() - DAY_MILLISECONDS * 200);

        attemptsList_.add(new Attempt(true, 10, now));
        attemptsList_.add(new Attempt(true, 110, yesterday));
        attemptsList_.add(new Attempt(false, 120, weekAgo));
        attemptsList_.add(new Attempt(false, 130, someDayAgo));
        attemptsList_.add(new Attempt(false, 140, monthAgo));
        attemptsList_.add(new Attempt(false, 150, monthAgo));
    }

    public List<Attempt> getAttempts() {
        return attemptsList_;
    }

    public void storeAttempt(Attempt attempt) {
        Attempt lastAttempt = null;
        if (!attemptsList_.isEmpty()) {
            lastAttempt = attemptsList_.get(0);
        }

        //If not to much time passed since the last attempt
        if (lastAttempt != null
            && (attempt.date.getTime() - (lastAttempt.date.getTime() + lastAttempt.duration*1000) < 10000)) {

            attempt = new Attempt(attempt.successfull, attempt.duration + lastAttempt.duration, lastAttempt.date);
            attemptsList_.set(0, attempt); //Update first item
        } else { //a new attempt
            if (attempt.duration >= JERKOFF_MIN_SECONDS) {
                attemptsList_.add(0, attempt); //add new item
            } else {
                return; //false alarm stop working
            }
        }

        if (attemptsAdapter_ != null) {
            attemptsAdapter_.notifyDataSetChanged();
        }
        storeToDisk();
    }

    public RecyclerView.Adapter<AttemptsAdapter.AttemptViewHolder> getAttemptsAdapter(Context context) {
        if (attemptsAdapter_ == null) {
         attemptsAdapter_ = new AttemptsAdapter(context, getAttempts());
        }
        return attemptsAdapter_;
    }

    public int getTodayAttempts() {
        Date now = new Date();
        int result = 0;
        for (Attempt attempt : attemptsList_) {
            result += ((now.getTime() - attempt.date.getTime()) < DAY_MILLISECONDS) ? 1 : 0;
        }
        return result;
    }

    public int getTodayGoal() {
        return 2;
    }

    public int getWeekAttempts() {
        Date now = new Date();
        int result = 0;
        for (Attempt attempt : attemptsList_) {
            result += ((now.getTime() - attempt.date.getTime()) < DAY_MILLISECONDS * 7) ? 1 : 0;
        }
        return result;
    }

    public int getWeekGoal() {
        return 8;
    }

    public int getMonthAttempts() {
        Date now = new Date();
        int result = 0;
        for (Attempt attempt : attemptsList_) {
            result += ((now.getTime() - attempt.date.getTime()) < DAY_MILLISECONDS * 30) ? 1 : 0;
        }
        return result;
    }

    public int getMonthGoal() {
        return 22;
    }

    private void restoreFromDisk() {

    }

    private void storeToDisk() {

    }
}
