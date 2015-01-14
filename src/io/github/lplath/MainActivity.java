package io.github.lplath;

import java.util.HashMap;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends ActionBarActivity implements OnEditorActionListener{
	
	private final static String TAG = "Morse";
	private static long sleepTime = 500;
	
	private Camera mCamera;
	private EditText mEditText;
	private ProgressDialog mProgress;
	private HashMap<String, String> mDictionary = new HashMap<String, String>();

	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mDictionary.put("A", "10111");
		mDictionary.put("B", "111010101");
		mDictionary.put("C", "11101011101");
		mDictionary.put("D", "1110101");
		mDictionary.put("E", "1");
		mDictionary.put("F", "101011101");
		mDictionary.put("G", "111011101");
		mDictionary.put("H", "1010101");
		mDictionary.put("I", "101");
		mDictionary.put("J", "1011101110111");
		mDictionary.put("K", "111010111");
		mDictionary.put("L", "101110101");
		mDictionary.put("M", "1110111");
		mDictionary.put("N", "11101");
		mDictionary.put("O", "11101110111");
		mDictionary.put("P", "10111011101");
		mDictionary.put("Q", "1110111010111");
		mDictionary.put("R", "10111");
		mDictionary.put("S", "10101");
		mDictionary.put("T", "111");
		mDictionary.put("U", "1010111");
		mDictionary.put("V", "101010111");
		mDictionary.put("W", "10101110111");
		mDictionary.put("X", "11101010111");
		mDictionary.put("Y", "1110101110111");
		mDictionary.put("Z", "11101110101");
		
		mDictionary.put("1", "10111011101110111");
		mDictionary.put("2", "101011101110111");
		mDictionary.put("3", "1010101110111");
		mDictionary.put("4", "10101010111");
		mDictionary.put("5", "101010101");
		mDictionary.put("6", "11101010101");
		mDictionary.put("7", "1110111010101");
		mDictionary.put("8", "111011101110101");
		mDictionary.put("9", "11101110111011101");
		mDictionary.put("0", "1110111011101110111");
		
		if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
			new AlertDialog.Builder(this)
			.setMessage("Konnte nicht auf Kamera zugreifen! Die App wird nun beendet.")
			.setTitle(android.R.string.dialog_alert_title)
			.setCancelable(true)
			.setNeutralButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
		        	public void onClick(DialogInterface dialog, int whichButton){
		        		finish();
		        	}
		        })
		     .show();
		}
		
		
		mEditText = (EditText)findViewById(R.id.inputField);
		mEditText.setOnEditorActionListener(this);
		
		mProgress = new ProgressDialog(this);
		mProgress.setMessage("Sende Nachricht..");
		mProgress.setCancelable(true);
		
		
		// Camera
		mCamera = Camera.open();
		mCamera.startPreview();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.speed_0_1:
	        	sleepTime = 100;
	        	return true;
	        case R.id.speed_0_5:
	        	sleepTime = 500;
	        	return true;
	        case R.id.speed_1:
	        	sleepTime = 1000;
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		final String plainText = mEditText.getText().toString();
		final String morseCode = encrypt(plainText);
		final Parameters pFlashOn = mCamera.getParameters();
		final Parameters pFlashOff = mCamera.getParameters();
		
		pFlashOn.setFlashMode(Parameters.FLASH_MODE_TORCH);
		pFlashOff.setFlashMode(Parameters.FLASH_MODE_OFF);
		
		
		if(actionId == EditorInfo.IME_ACTION_SEND) {
			mEditText.setText("");
			mProgress.show();
			
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					boolean flashOn = false;
					
					for(int i = 0; i < morseCode.length(); i++) {
						String chr = String.valueOf(morseCode.charAt(i));
						Log.i(TAG, chr);
						
						if(chr.equals("1")) {
							if(!flashOn) {
								Log.i(TAG, "ON");
								mCamera.setParameters(pFlashOn);
								flashOn = true;
							}
						}
						else if(chr.equals("0")) {
							if(flashOn) {
								Log.i(TAG, "OFF");
								mCamera.setParameters(pFlashOff);
								flashOn = false;
							}
						}
						
						try { Thread.sleep(sleepTime); } catch (InterruptedException e) { e.printStackTrace(); }
					}
					
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mProgress.dismiss();
						}
					});
					
				}
				
			}).start();
			return true;
		}
		return false;
	}
	
	private String encrypt(String in) {
		String out = "";
		in = in.toUpperCase(Locale.US);
		
		for(int i = 0; i < in.length(); i++) {
			String dic = mDictionary.get(String.valueOf(in.charAt(i)));
			
			if(dic != null) {
				out += dic;
				out += "000";
			}
			else if((in.charAt(i) + "") == " ") {
				out += "000000";
			}
		}
		return out;
	}
	
	
	public void onDestroy() {
		mDictionary.clear();
	}

}