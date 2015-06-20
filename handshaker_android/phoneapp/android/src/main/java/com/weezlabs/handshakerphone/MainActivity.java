package com.weezlabs.handshakerphone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;


public class MainActivity extends Activity {
	
	private static final UUID WATCHAPP_UUID = UUID.fromString("10fd2b6b-a7b4-44fc-932b-7ddd03d40634");
	private static final String WATCHAPP_FILENAME = "handshaker_pebble.pbw";
	
	private static final int
		KEY_BUTTON = 0,
		KEY_VIBRATE = 1,
		BUTTON_UP = 0,
		BUTTON_SELECT = 1,
		BUTTON_DOWN = 2;
	private static final int DATA_LOG_TAG_ACCEL_DATA = 0x1234;

	private Handler handler = new Handler();
	private PebbleDataReceiver appMessageReciever;
	private TextView whichButtonView;
	private TextView textView;

	private PebbleKit.PebbleDataLogReceiver dataloggingReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Customize ActionBar
		ActionBar actionBar = getActionBar();
		actionBar.setTitle("PebbleKit Example");
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionbar_orange)));

		// Setup TextView
		textView = (TextView)findViewById(R.id.text_view);
		textView.setText("Waiting for logging data...");

		//Register for messages
		PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(WATCHAPP_UUID) {

			@Override
			public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
				Log.i(getLocalClassName(), "Received value=" + data.getInteger(0) + " " + data.getInteger(1) + " "+ data.getInteger(2));
				textView.setText("Got: " + data.getInteger(0) + " " + data.getInteger(1) + " "+ data.getInteger(2));

				PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);
			}

		});

	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// Define data reception behavior
		dataloggingReceiver = new PebbleKit.PebbleDataLogReceiver(WATCHAPP_UUID) {

			@Override
			public void receiveData(Context context, UUID logUuid, Long timestamp, Long tag, int data) {
				// Check this is the compass headings log
				if(tag.intValue() == DATA_LOG_TAG_ACCEL_DATA) {
					textView.setText("Get: " + tag + " dat: " + data + " int\n");
				}
			}

			@Override
			public void receiveData(Context context, UUID logUuid, Long timestamp, Long tag, Long data) {
				if(tag.intValue() == DATA_LOG_TAG_ACCEL_DATA) {
					// Get the compass value and append to result StringBuilder
					textView.setText("Get: " + tag + " dat: " + data + " long\n");
				}
			}

			@Override
			public void receiveData(Context context, UUID logUuid, Long timestamp, Long tag, byte[] data) {
				if(tag.intValue() == DATA_LOG_TAG_ACCEL_DATA) {
					// Get the compass value and append to result StringBuilder
					textView.setText("Get: " + tag + " dat: " + data + " array\n");
				}
			}

			@Override
			public void onFinishSession(Context context, UUID logUuid, Long timestamp, Long tag) {
				// Display all compass headings received
				textView.setText("Session finished!\n");
			}
		};

		// Register DataLogging Receiver
//		PebbleKit.registerDataLogReceiver(this, dataloggingReceiver);
//		PebbleKit.requestDataLogsForApp(this, WATCHAPP_UUID);

		//Register connected disconnecting events
		PebbleKit.registerPebbleConnectedReceiver(getApplicationContext(), new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i(getLocalClassName(), "Pebble connected!");
			}

		});

		PebbleKit.registerPebbleDisconnectedReceiver(getApplicationContext(), new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i(getLocalClassName(), "Pebble disconnected!");
			}

		});
	}
	
	@Override
	protected void onPause() {
		super.onPause();

		// Always unregister callbacks
		if(dataloggingReceiver != null) {
			unregisterReceiver(dataloggingReceiver);
		}
	}

	public void onInstallAppClicked(View sender){
		//Install
		Toast.makeText(getApplicationContext(), "Installing watchapp...", Toast.LENGTH_SHORT).show();
		sideloadInstall(getApplicationContext(), WATCHAPP_FILENAME);
	}

	public void onLaunchAppClicked(View sender) {
		// Launching my app
		PebbleKit.startAppOnPebble(getApplicationContext(), WATCHAPP_UUID);
	}

	public void onCheckAliveClicked(View sender) {
		boolean watchConnected = PebbleKit.isWatchConnected(getApplicationContext());
		Toast.makeText(this, watchConnected ? "Connected" : "Not connected", Toast.LENGTH_SHORT).show();
	}

	/**
     * Alternative sideloading method
     * Source: http://forums.getpebble.com/discussion/comment/103733/#Comment_103733 
     */
    public static void sideloadInstall(Context ctx, String assetFilename) {
        try {
            // Read .pbw from assets/
        	Intent intent = new Intent(Intent.ACTION_VIEW);    
            File file = new File(ctx.getExternalFilesDir(null), assetFilename);
            InputStream is = ctx.getResources().getAssets().open(assetFilename);
            OutputStream os = new FileOutputStream(file);
            byte[] pbw = new byte[is.available()];
            is.read(pbw);
            os.write(pbw);
            is.close();
            os.close();
             
            // Install via Pebble Android app
            intent.setDataAndType(Uri.fromFile(file), "application/pbw");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        } catch (IOException e) {
            Toast.makeText(ctx, "App install failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
