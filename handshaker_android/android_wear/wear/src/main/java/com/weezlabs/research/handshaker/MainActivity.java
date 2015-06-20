package com.weezlabs.research.handshaker;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends WearableActivity {

    GridViewPager mPager;
    DotsPageIndicator mIndicator;
    TextView mAmbientText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setAmbientEnabled();
        AccelerometerHelper.getInstance().init(this);

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
