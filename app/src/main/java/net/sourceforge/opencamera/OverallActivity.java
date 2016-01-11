package net.sourceforge.opencamera;

import net.sourceforge.opencamera.MainActivity;

// IMPORTS
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.util.Base64;
import android.view.View;
import android.view.MenuItem;
import android.widget.Button;
import android.util.Log;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.*;

public class OverallActivity extends Activity {

	public static final String TAG = "OverallActivity";
	public static final boolean SEND_VIDEO = true;
	// socket - connection oriented stuff.
	private static final int SERVERPORT = 9999; // python socket TCP server
	private static final String SERVER_IP = "192.168.1.67"; // nick.lan
	// file to send
	private Socket socket;
	private File text_file = new File("/storage/sdcard0/DCIM/OpenCamera/kalimera.txt");
	private File results_file = new File("/storage/sdcard0/DCIM/OpenCamera/results.txt");
	private File video_file = new File("/storage/sdcard0/DCIM/OpenCamera/VID_20160110_033932.mp4");
	private Thread connectionThread = new Thread(new ClientThread());
	private boolean connectionEstablished = false; // boolean to check weather the connection is working

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_overall);
		if (MyDebug.LOG)
			Log.d(TAG, "onCreate");


		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


		Button shootVideo_btn = (Button) findViewById(R.id.shootVideo_btn);
		Button analyzeVideo_btn = (Button) findViewById(R.id.analyzeVideo_btn);
		Button previousResults_btn = (Button) findViewById(R.id.previousResults_btn);

		// CLICK LISTENERS
		shootVideo_btn.setOnClickListener(
				new Button.OnClickListener() {
					public void onClick(View v) {
						Intent mainActivityIntent = new Intent(OverallActivity.this, MainActivity.class);
//						mainActivityIntent.putExtra("key", value); //Optional parameters
						OverallActivity.this.startActivity(mainActivityIntent);
						// log message
						if (MyDebug.LOG)
							Log.d(TAG, "Shoot Video clicked.");
					}
				}
		);

		analyzeVideo_btn.setOnClickListener(
				new Button.OnClickListener() {
					public void onClick(View v) {
						if (MyDebug.LOG) {
							Log.d(TAG, "Analyze Video clicked.");
							Log.d(TAG, "video_file = " + video_file.toString());
						}


						// the server better be up, otherwise the app breaks
						// todo - fix this

						connectionThread.start();
						// do the following only if the connection is working, otherwise it breaks the application
						try {
							byte[] message = null;

							// I may be sending a simple textfile
							if (SEND_VIDEO == true) {
								message = getBinRepr0File(video_file);
								if (MyDebug.LOG)
									Log.d(TAG, "Got Bin Representation of file");
							} else {
								message = getBinRepr0File(text_file);
							}
							DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
//								out.writeInt(message.length); // write length of the message
							dOut.write(message);
							if (MyDebug.LOG)
								Log.d(TAG, "Written the message");

							// todo - make client wait for the answer and receive it
							// this shall be a blocking call, have a progress bar as long as the blocking takes place
							// render the txt results in the activity of the user.

//							// block and receive the answer
//							if (MyDebug.LOG)
//								Log.d(TAG, "Gonna wait for the results from the server");
//
//							DataInputStream dIn = new DataInputStream(socket.getInputStream());
//							byte[] result_in = new byte[64000];
//							dIn.readFully(result_in);
//
//							try
//							{
//								FileOutputStream saveResults_stream = new FileOutputStream(results_file);
//								saveResults_stream.write(result_in);
//								saveResults_stream.flush();
//								saveResults_stream.close();
//								if (MyDebug.LOG)
//									Log.d(TAG, "Successfully saved the results");
//							} catch (FileNotFoundException e)
//							{
//								e.printStackTrace();
//								if (MyDebug.LOG)
//									Log.d(TAG, "Writing Results: FileNotFoundException");
//							}

						} catch (UnknownHostException e1) {
							e1.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}

						try {
							socket.close();
							if (MyDebug.LOG)
								Log.d(TAG, "Closed connection with server");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}


				}
		);

		// todo write code here.
		previousResults_btn.setOnClickListener(
				new Button.OnClickListener() {
					public void onClick(View v) {
						if (MyDebug.LOG)
							Log.d(TAG, "Previous Results clicked.");
					}
				}
		);
	}

	@Override
	protected void onResume() {
		if (MyDebug.LOG)
			Log.d(TAG, "onResume");
		super.onResume();

		// if video is taken and stored successfully
		if (getIntent() != null && getIntent().getStringExtra("video_path") != null) {

			String video_path = getIntent().getStringExtra("video_path");
			if (MyDebug.LOG)
				Log.d(TAG, video_path);
			video_file = new File(video_path);


			TextView info_view = (TextView) findViewById(R.id.info_view);
			info_view.setText("Video Captured");

			// show message if it's the first time.
			boolean isFirstTime = getIntent().getBooleanExtra("isFirstTime", false);

			Button analyzeVideo_btn = (Button) findViewById(R.id.analyzeVideo_btn);
			analyzeVideo_btn.setEnabled(true);

			if (isFirstTime) {
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
				alertDialog.setTitle(R.string.app_name);
				alertDialog.setMessage(R.string.firstTimeOverall);
				alertDialog.setPositiveButton(R.string.intro_ok, null);
				alertDialog.show();
			}

		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			socket.close();
		} catch (Exception e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		socket = null;
	}
	// CLICKED BUTTONS CALLBACKS

    public void clickedSettingsOver(View view) {
		if( MyDebug.LOG )
			Log.d(TAG, "clickedSettingsOver");
	    openSettings();
    }

	public void clickedInfo(View view) {
		if( MyDebug.LOG )
			Log.d(TAG, "clickedInfo");

			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(R.string.app_name);
            alertDialog.setMessage(R.string.intro_text);
            alertDialog.setPositiveButton(R.string.about_ok, null);
            alertDialog.show();
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private byte[] getBinRepr0File(File file) {

		// http://stackoverflow.com/a/13416628/2843583
		// encode file in base64 binary
		byte[] byteBinaryData = null; // need to be initialized outside the try statement
		try {
			FileInputStream objFileIS = new FileInputStream(file);
			ByteArrayOutputStream objByteArrayOS = new ByteArrayOutputStream();
			byte[] byteBufferString = new byte[1024];
			for (int readNum; (readNum = objFileIS.read(byteBufferString)) != -1; ) {
				objByteArrayOS.write(byteBufferString, 0, readNum);
				System.out.println("read " + readNum + " bytes,");
			}
			byteBinaryData = Base64.encode((objByteArrayOS.toByteArray()), Base64.DEFAULT);
			return byteBinaryData;

		} catch (IOException e) {
			e.printStackTrace();
			if (MyDebug.LOG)
				Log.d(TAG, "getBinRepr0File: In IOException");

			return null;
		}
	}

	class ClientThread implements Runnable {

		@Override
		public void run() {

			if (MyDebug.LOG)
				Log.d(TAG, "ClientThread: Running.");

			try {
				InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
				socket = new Socket(serverAddr, SERVERPORT);
				if (MyDebug.LOG)
					Log.d(TAG, "ClientThread: Thread made the connection.");
				connectionEstablished = true;
			} catch (UnknownHostException e1) {
				if (MyDebug.LOG)
					Log.d(TAG, "ClientThread: UnknownHostException");
				e1.printStackTrace();
			} catch (IOException e1) {
				if (MyDebug.LOG)
					Log.d(TAG, "ClientThread: IOException");
				e1.printStackTrace();
			}

		}

	}
	private void openSettings() {
		if( MyDebug.LOG )
			Log.d(TAG, "openSettings");

		Bundle bundle = new Bundle();
//		bundle.putInt("cameraId", this.preview.getCameraId());
		bundle.putString("cameFrom", "OverallActivity");

		MyPreferenceFragment fragment = new MyPreferenceFragment();
		fragment.setArguments(bundle);
		getFragmentManager().beginTransaction().add(R.id.prefs_container_over, fragment, "PREFERENCE_FRAGMENT").addToBackStack(null).commit();
	}

	MyPreferenceFragment getPreferenceFragment() {
        MyPreferenceFragment fragment = (MyPreferenceFragment)getFragmentManager().findFragmentByTag("PREFERENCE_FRAGMENT");
        return fragment;
    }
}
