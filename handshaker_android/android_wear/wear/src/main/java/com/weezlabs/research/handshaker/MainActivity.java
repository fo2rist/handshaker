package com.weezlabs.research.handshaker;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends WearableActivity {

    GridViewPager mPager;
    DotsPageIndicator mIndicator;
    TextView mAmbientText;

    public boolean noSplash = false;

    public static boolean isForeground(Context context) {
        // Get the Activity Manager
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);

        // Get a list of running tasks, we are only interested in the last one,
        // the top most so we give a 1 as parameter so we only get the topmost.
        List< ActivityManager.RunningTaskInfo > task = manager.getRunningTasks(1);

        // Get the info we need for comparison.
        ComponentName componentInfo = task.get(0).topActivity;

        // Check if it matches our package name.
        if(componentInfo.getPackageName().equals(context.getPackageName())) return true;

        // If not then our app is not on the foreground.
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setAmbientEnabled();
        if (getIntent().getExtras() != null) {
            noSplash = getIntent().getExtras().getBoolean("NO_SPLASH", false);
        }

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mPager = (GridViewPager) findViewById(R.id.pager);
                mPager.setAdapter(new SensorFragmentPagerAdapter(getFragmentManager()));

                mIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
                mIndicator.setPager(mPager);

                mAmbientText = (TextView) findViewById(R.id.keep_going_text);
            }
        });

        Intent intent = new Intent(this, SensorBackgroundService.class);
        startService(intent);
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
    }

    private void test_enableAmbient() {
        boolean ambient = !isAmbient();
        int ambientLayoutVisibility = ambient ? View.VISIBLE : View.GONE;
        int interactiveLayoutVisibility = ambient ? View.GONE : View.VISIBLE;

        mAmbientText.getPaint().setAntiAlias(!ambient);
        mAmbientText.setVisibility(ambientLayoutVisibility);
        mPager.setVisibility(interactiveLayoutVisibility);
        mIndicator.setVisibility(interactiveLayoutVisibility);
    }

}
