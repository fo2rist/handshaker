package com.weezlabs.handshakerphone.models;

import java.util.Date;

public class Attempt {
    /** Wast it successfull */
    public boolean successfull;
    /** Duration in seconds */
    public int duration;
    /** Attempt start time */
    public Date date;

    public Attempt(boolean successfull, int duration, Date date) {
        this.successfull = successfull;
        this.duration = duration;
        this.date = date;
    }
}
