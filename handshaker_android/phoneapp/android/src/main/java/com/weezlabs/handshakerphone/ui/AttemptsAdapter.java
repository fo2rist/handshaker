package com.weezlabs.handshakerphone.ui;

import android.content.Context;
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

    public static final long DAY_MILLISECONDS = 86400000L;

    public static class AttemptViewHolder extends RecyclerView.ViewHolder {
        protected TextView date;
        protected TextView duration;

        public AttemptViewHolder(View itemView) {
            super(itemView);

            date = (TextView) itemView.findViewById(R.id.date);
            duration = (TextView) itemView.findViewById(R.id.duration);
        }

    }

    private Context context_;
    private final List<Attempt> attemptsList_;

    public AttemptsAdapter(Context context, List<Attempt> attemptsList) {
        context_ = context;
        attemptsList_ = attemptsList;
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

        Calendar today = Calendar.getInstance();
        Calendar eventDay = Calendar.getInstance();
        eventDay.setTime(attemptInfo.date);

        String dateText = null;
        if (eventDay.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && eventDay.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            dateText = "Today";
        } else if (eventDay.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && (eventDay.get(Calendar.DAY_OF_YEAR)+1) == today.get(Calendar.DAY_OF_YEAR)){
            dateText = "Yesterday";
        } else {
            //default
            dateText = new SimpleDateFormat("MMM d yyyy").format(attemptInfo.date);
        }

        long msAgo = today.getTime().getTime() - attemptInfo.date.getTime();
        if (msAgo < DAY_MILLISECONDS) {
            attemptViewHolder.date.setBackgroundColor(context_.getResources().getColor(android.R.color.holo_blue_dark));
        } else if (msAgo < DAY_MILLISECONDS * 7) {
            attemptViewHolder.date.setBackgroundColor(context_.getResources().getColor(android.R.color.holo_green_dark));
        } else if (msAgo < DAY_MILLISECONDS * 30 ){
            attemptViewHolder.date.setBackgroundColor(context_.getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            attemptViewHolder.date.setBackgroundColor(context_.getResources().getColor(android.R.color.darker_gray));
        }

        attemptViewHolder.date.setText( dateText );
        attemptViewHolder.duration.setText("" + attemptInfo.duration + " s");
    }

    @Override
    public int getItemCount() {
        return attemptsList_.size();
    }
}
