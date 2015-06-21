package com.weezlabs.handshakerphone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.weezlabs.handshakerphone.R;
import com.weezlabs.handshakerphone.datastorage.AttemptsManager;
import com.weezlabs.handshakerphone.models.Attempt;


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

	static final int KEY_TYPE = 0;
	static final int KEY_ACCEL_X = 1;
	static final int KEY_ACCEL_Y = 2;
	static final int KEY_ACCEL_Z = 3;
	static final int KEY_DURATION = 4;

	static final int TYPE_START = 0;
	static final int TYPE_END = 1;
	static final int TYPE_PROGRESS = 2;

	private ArcProgress todayProgress;
	private ArcProgress weekProgress;
	private ArcProgress monthProgress;
	private RecyclerView attemptsList;

	/**
	 * Alternative sideloading method
	 * Source: http://forums.getpebble.com/discussion/comment/103733/#Comment_103733
	 */
	private static void sideloadInstall(Context ctx, String assetFilename) {
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


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Customize ActionBar
		ActionBar actionBar = getActionBar();
		actionBar.setTitle("H-A-N-D-S-H-A-K-E-R");
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionbar_orange)));
		actionBar.setIcon(R.drawable.ic_app);

		//Get controls
		todayProgress = (ArcProgress) findViewById(R.id.today_progress);
		weekProgress = (ArcProgress) findViewById(R.id.week_progress);
		monthProgress = (ArcProgress) findViewById(R.id.month_progress);
		attemptsList = (RecyclerView) findViewById(R.id.attempts_list);

		//Setup list
		LinearLayoutManager llm = new LinearLayoutManager(this);
		llm.setOrientation(LinearLayoutManager.VERTICAL);
		attemptsList.setLayoutManager(llm);
		attemptsList.setAdapter(AttemptsManager.getInstance().getAttemptsAdapter(this));

		//Register for messages
		PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(WATCHAPP_UUID) {

			@Override
			public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
				Long type = data.getInteger(KEY_TYPE);
				switch (type.intValue()) {
					case TYPE_START:
						todayProgress.setFinishedStrokeColor(getResources().getColor(android.R.color.holo_blue_dark));
						break;
					case TYPE_PROGRESS:
						todayProgress.setBottomText("...");
						break;
					case TYPE_END:
						todayProgress.setFinishedStrokeColor(getResources().getColor(android.R.color.holo_blue_light));
						todayProgress.setBottomText("today");
						int duration = data.getInteger(KEY_DURATION).intValue();
						AttemptsManager.getInstance().storeAttempt(
								new Attempt(true, duration, new Date())
						);
						refreshStats();// Hack even more dirty than me
						break;
				}

				PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);
			}
		});

		refreshStats();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_install_app:
				sideloadInstall(getApplicationContext(), WATCHAPP_FILENAME);
				break;
			case R.id.menu_launch_app:
				PebbleKit.startAppOnPebble(getApplicationContext(), WATCHAPP_UUID);
				break;
		}
		return true;
	}

	private void refreshStats() {
		AttemptsManager attemptsManager = AttemptsManager.getInstance();

		todayProgress.setProgress(100 * attemptsManager.getTodayAttempts() / attemptsManager.getTodayGoal());
		weekProgress.setProgress(100 * attemptsManager.getWeekAttempts() / attemptsManager.getWeekGoal());
		monthProgress.setProgress(100 * attemptsManager.getMonthAttempts() / attemptsManager.getMonthGoal());
	}

}
