package com.weezlabs.handshakerphone.ui;

import android.content.pm.LabeledIntent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.weezlabs.handshakerphone.R;
import com.weezlabs.handshakerphone.models.Attempt;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AttemptsAdapter extends RecyclerView.Adapter<AttemptsAdapter.AttemptViewHolder> {

    public static class AttemptViewHolder extends RecyclerView.ViewHolder {
        protected TextView date;
        protected TextView duration;

        public AttemptViewHolder(View itemView) {
            super(itemView);

            date = (TextView) itemView.findViewById(R.id.date);
            duration = (TextView) itemView.findViewById(R.id.duration);
        }
    }

    private final List<Attempt> attemptsList_;

    public AttemptsAdapter(List<Attempt> attemptsList) {
        this.attemptsList_ = attemptsList;
    }

    @Override
    public AttemptViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View result = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.attempt_card, viewGroup, false);
        return new AttemptViewHolder(result);
    }

    @Override
    public void onBindViewHolder(AttemptViewHolder attemptViewHolder, int i) {
        Attempt attemptInfo = attemptsList_.get(i);
        Date now = new Date();
        Calendar today = Calendar.getInstance();
        today.setTime(now);

        Calendar eventDay = Calendar.getInstance();
        today.setTime(attemptInfo.date);

        String dateText = null;
        if (eventDay.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && eventDay.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            dateText = "today";
        } else {
            //default
            dateText = new SimpleDateFormat().format(attemptInfo.date);
        }

        attemptViewHolder.date.setText( dateText );
        attemptViewHolder.duration.setText("" + attemptInfo.duration + " s");
    }

    @Override
    public int getItemCount() {
        return attemptsList_.size();
    }
}
