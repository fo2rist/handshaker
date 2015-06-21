package com.weezlabs.research.handshaker;

import android.app.Fragment;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.weezlabs.research.handshaker.SensorFragmentPagerAdapter.DisplayMode;

public class SensorFragment extends Fragment implements AccelerometerHelper.OnAccelerometerListener {

    public static SensorFragment newInstance(int mode) {
        SensorFragment fragment = new SensorFragment();

        Bundle args = new Bundle();
        args.putInt("mode", mode);
        fragment.setArguments(args);

        return fragment;
    }

    private DisplayMode mMode;
    @Nullable
    private TextView mTextValues;
    @Nullable
    private ImageView mBlotImage;
    @Nullable
    private TextView mShakingText;
    @Nullable
    private Animation mFadeInAnimation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mMode = DisplayMode.get(args.getInt("mode"));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        if (mMode == DisplayMode.Developer) {
            view = inflater.inflate(R.layout.sensor_developer, container, false);
            mTextValues = (TextView) view.findViewById(R.id.text_values);
        } else if (mMode == DisplayMode.User) {
            view = inflater.inflate(R.layout.sensor_user, container, false);
            mBlotImage = (ImageView) view.findViewById(R.id.blot_image);
            mShakingText = (TextView) view.findViewById(R.id.start_shaking_text);

            if (!((MainActivity)getActivity()).noSplash) {
                DisplayMetrics metrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int width = metrics.widthPixels;

                final TranslateAnimation shakingAnimation;
                shakingAnimation = new TranslateAnimation(0f, 0F, 0F, width / 3.8F);
                shakingAnimation.setDuration(2000);
                shakingAnimation.setRepeatCount(-1);
                shakingAnimation.setRepeatMode(Animation.REVERSE);
                shakingAnimation.setInterpolator(new BounceInterpolator());

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mShakingText.getVisibility() == View.VISIBLE) {
                            mShakingText.startAnimation(shakingAnimation);
                        }
                    }
                }, 2000);
            } else {
                mShakingText.setVisibility(View.GONE);
                mBlotImage.setVisibility(View.VISIBLE);
            }

            mFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        } else {
            throw new IllegalArgumentException("Unexpected mode was passed");
        }

        AccelerometerHelper.getInstance().init(getActivity());
        AccelerometerHelper.getInstance().register(this);
        return view;
    }

    @Override
    public void onStop() {
        AccelerometerHelper.getInstance().unregister(this);
        super.onStop();
    }

    @Override
    public void onAccelerometerValueChanged(SensorEvent event) {
        if (mMode == DisplayMode.User) {
            return;
        }

        if (mTextValues == null) {
            throw new IllegalStateException("mTextValues is null");
        }

        mTextValues.setText(
                "x = " + Float.toString(event.values[0]) + "\n" +
                        "y = " + Float.toString(event.values[1]) + "\n" +
                        "z = " + Float.toString(event.values[2]) + "\n"
        );
    }

    @Override
    public void onShakeDetected() {
        if (mMode == DisplayMode.Developer) {
            return;
        }

        if (mBlotImage == null || mShakingText == null) {
            throw new IllegalStateException("mBlotImage or mShakingText is null");
        }

        if (mShakingText.getVisibility() == View.VISIBLE) {
            mShakingText.clearAnimation();
            mShakingText.setVisibility(View.GONE);
            mBlotImage.setVisibility(View.VISIBLE);
        }

        mBlotImage.clearAnimation();
        mBlotImage.startAnimation(mFadeInAnimation);

//        OkWear ok = new OkWear(getActivity());
//        ok.connect();
//        Payload payload =
//                new Payload.Builder("/path")
//                        .addPayload("key1", 0)
//                        .addPayload("key2", "hello")
//                        .build();
//
//        // use callback listener
//        ok.syncData(payload, new SendResultListener<DataApi.DataItemResult>() {
//            @Override
//            public void onResult(DataApi.DataItemResult result) {
//                Log.v("LOL", "LOL Status: " + result.getStatus());
//            }
//        });
    }

}
