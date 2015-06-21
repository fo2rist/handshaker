package com.weezlabs.handshakerphone.datastorage;

import android.support.v7.widget.RecyclerView;

import com.weezlabs.handshakerphone.models.Attempt;
import com.weezlabs.handshakerphone.ui.AttemptsAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AttemptsManager {
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
        yesterday.setTime(now.getTime() - 86500000);
        Date someDayAgo = new Date();
        someDayAgo.setTime(now.getTime() - 86400000*10);

        attemptsList_.add(new Attempt(true, 10, now));
        attemptsList_.add(new Attempt(true, 110, yesterday));
        attemptsList_.add(new Attempt(false, 20, someDayAgo));
        attemptsList_.add(new Attempt(false, 21, someDayAgo));
        attemptsList_.add(new Attempt(false, 22, someDayAgo));
        attemptsList_.add(new Attempt(false, 23, someDayAgo));
    }

    public List<Attempt> getAttempts() {
        return attemptsList_;
    }

    public void storeAttempt(Attempt attempt) {
        attemptsList_.add(0, attempt);
        if (attemptsAdapter_ != null) {
            attemptsAdapter_.notifyDataSetChanged();
        }
        storeToDisk();
    }

    public RecyclerView.Adapter<AttemptsAdapter.AttemptViewHolder> getAttemptsAdapter() {
        if (attemptsAdapter_ == null) {
         attemptsAdapter_ = new AttemptsAdapter(getAttempts());
        }
        return attemptsAdapter_;
    }

    private void restoreFromDisk() {

    }

    private void storeToDisk() {

    }
}
