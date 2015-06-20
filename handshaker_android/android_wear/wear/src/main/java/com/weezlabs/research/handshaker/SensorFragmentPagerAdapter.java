package com.weezlabs.research.handshaker;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.wearable.view.FragmentGridPagerAdapter;

public class SensorFragmentPagerAdapter extends FragmentGridPagerAdapter {

    protected enum DisplayMode {
        User(0),
        Developer(1);

        static DisplayMode get(final int modeId) {
            for (DisplayMode mode: DisplayMode.values()) {
                if (mode.mModeId == modeId)
                    return mode;
            }

            throw new IllegalArgumentException("Invalid id");
        }

        private int mModeId;

        DisplayMode(int modeId) {
            mModeId = modeId;
        }
    }

    public SensorFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getFragment(int row, int column) {
        return SensorFragment.newInstance(column);
    }

    @Override
    public int getRowCount() {
        return 1; // fixed to 1 row
    }

    @Override
    public int getColumnCount(int row) {
        return DisplayMode.values().length;
    }

}
