package edu.ccu.cs;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import edu.ccu.cs.HTTPHandler.*;

/**
 * The login activity of EcoursePlus
 * @author Chang Yuan-Yi
 * @author Wu Chen-Yu
 *
 */
public class LoginActivity extends Activity implements Button.OnClickListener, CompoundButton.OnCheckedChangeListener{

	/** Using intent to create a new activity*/
	private Intent intent; 
	private EditText editTextAcc; 
	private EditText editTextPwd;
	private Button buttonLogin;
	private CheckBox checkBoxRePwd;
	/** Stores the account and password information. */
	SharedPreferences settings; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = getPreferences(MODE_PRIVATE);
		setContentView(R.layout.layout_login);
		// initialize
		intent = new Intent().setClass(this, CourseListActivity.class);
		editTextAcc = (EditText)findViewById(R.id.editText_acc);
		editTextPwd = (EditText)findViewById(R.id.editText_pwd);
		buttonLogin = (Button)findViewById(R.id.button_login);
		
		buttonLogin.setOnClickListener(this);
		
		checkBoxRePwd = (CheckBox)findViewById(R.id.checkBox_repwd);
		checkBoxRePwd.setOnCheckedChangeListener(this);
		restorePrefs();	
		
	}
	/**
	 * It is called when login button is pressed.
	 */
	@Override
	public void onClick(View v) {
		if (!isConnectingToInternet()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.error_msg_2)
			       .setTitle(R.string.error_title);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub				
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();			
		}
		else {
			String acc = editTextAcc.getText().toString();
			String pwd = editTextPwd.getText().toString();
			HtmlGetter mHtmlGetter = new HtmlGetter();
			String courseHtml = mHtmlGetter.login(acc, pwd);
			if(courseHtml.equals("Login Failed")){ // the HtmlGetter will return "Login Failed" when wrong acc/pass 
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.error_msg_1)
				       .setTitle(R.string.error_title);
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub						
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();						
			}
			else{
				// using bundle communicating with other activity
				ProgressDialog mDialog = ProgressDialog.show(this, "Please wait", "Loading");
				storeSetting(acc, pwd);
				Bundle bundle =  new Bundle();
				bundle.putString("acc", acc);
				bundle.putString("pwd", pwd);
				intent.putExtras(bundle);
				startActivity(intent);
				mDialog.dismiss();
			}
		}
	}
	/**
	 * To determine whether the account and password store or not.
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch(buttonView.getId()){
		case R.id.checkBox_repwd:
			if(!isChecked){
				editTextAcc.setText("");
				editTextPwd.setText("");
				settings.edit().putString("REMEMBER_ME", "false").commit();
	            settings.edit().putString("ACC", "").commit();
	            settings.edit().putString("PWD", "").commit();
			}
		}
		
	}
	/**
	 * Check the Internet connection.
	 * @return true if is connected, false otherwise.
	 */
	private boolean isConnectingToInternet(){
	     ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	     if (connMgr != null)
	     {
	       NetworkInfo info = connMgr.getActiveNetworkInfo();
	       if (info != null)
	          return (info.isConnected());
	     }
	     return false;
	}
	/**
	 * Restore the account and password.
	 */
	private void restorePrefs() {
	    SharedPreferences settings = getPreferences(MODE_PRIVATE);
	    String rememberMe = settings.getString("REMEMBER_ME", "");
	    if(rememberMe.equals("true")) {
	        editTextAcc.setText(settings.getString("ACC", ""));
	        editTextPwd.setText(settings.getString("PWD", ""));
	        checkBoxRePwd.setChecked(true);
	    }
	    
	}
	/**
	 * It stores the account and password in SharedPrefernce.
	 * @see #settings
	 * @param acc account
	 * @param pwd password
	 */
	private void storeSetting(String acc, String pwd) {
		
	    SharedPreferences settings = getPreferences(MODE_PRIVATE);
	    if(checkBoxRePwd.isChecked()) {
	        settings.edit().putString("REMEMBER_ME", "true").commit();
	        settings.edit().putString("ACC", acc).commit();
	        settings.edit().putString("PWD", pwd).commit();
	    }
	    else {
	        settings.edit().putString("REMEMBER_ME", "false").commit();
            settings.edit().putString("ACC", "").commit();
            settings.edit().putString("PWD", "").commit();
	    }
	}
}
