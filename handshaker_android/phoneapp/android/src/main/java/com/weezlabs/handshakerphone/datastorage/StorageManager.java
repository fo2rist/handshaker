package com.weezlabs.handshakerphone.datastorage;

import com.weezlabs.handshakerphone.models.Attempt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WeezLabs on 6/20/15.
 */
public class StorageManager {
    private static StorageManager instance_ = null;

    private ArrayList<Attempt> attemptsList_ = new ArrayList<Attempt>();

    public static synchronized StorageManager getInstance() {
        if (instance_ == null) {
            instance_ = new StorageManager();
        }

        return instance_;
    }

    private StorageManager() {
    }

    public List<?> getAttempts() {
        return attemptsList_;
    }

    public void storeAttempt(Attempt attempt) {
        attemptsList_.add(attempt);

        storeToDisk();
    }

    private void restoreFromDisk() {

    }

    private void storeToDisk() {

    }
}
