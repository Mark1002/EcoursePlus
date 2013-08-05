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
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import edu.ccu.cs.HTTPHandler.*;

public class LoginActivity extends Activity implements Button.OnClickListener, OnCheckedChangeListener{

	private Intent intent;
	private EditText editTextAcc;
	private EditText editTextPwd;
	private Button buttonLogin;
	private CheckBox checkBoxRePwd;
	SharedPreferences settings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = getPreferences(MODE_PRIVATE);
		setContentView(R.layout.layout_login);
		// 初始化
		intent = new Intent().setClass(this, CourseListActivity.class); // 代入activity
		editTextAcc = (EditText)findViewById(R.id.editText_acc);
		editTextPwd = (EditText)findViewById(R.id.editText_pwd);
		buttonLogin = (Button)findViewById(R.id.button_login);
		
		buttonLogin.setOnClickListener(this);
		
		checkBoxRePwd = (CheckBox)findViewById(R.id.checkBox_repwd);
		checkBoxRePwd.setOnCheckedChangeListener(this);
		restorePrefs();	
		
	}

	/* Button*/
	@Override
	public void onClick(View v) {
		if (!isConnectingToInternet()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Failed to access network")
			       .setTitle("ERROR");
			AlertDialog dialog = builder.create();
			dialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which){}});
			dialog.show();			
		}
		else {
			String acc = editTextAcc.getText().toString();
			String pwd = editTextPwd.getText().toString();
			HtmlGetter mHtmlGetter = new HtmlGetter();
			String courseHtml = mHtmlGetter.login(acc, pwd);
			if(courseHtml.equals("Login Failed")){
				// 警告訊息
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.error_msg_1)
				       .setTitle(R.string.error_title);
				AlertDialog dialog = builder.create();
				dialog.setButton("OK", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which){}});
				dialog.show();						
			}
			else{
				// 利用bundle做activity間的溝通
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
	
	
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
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
	private void restorePrefs() {
	    SharedPreferences settings = getPreferences(MODE_PRIVATE);
	    String rememberMe = settings.getString("REMEMBER_ME", "");
	    if(rememberMe.equals("true")) {
	        editTextAcc.setText(settings.getString("ACC", ""));
	        editTextPwd.setText(settings.getString("PWD", ""));
	        checkBoxRePwd.setChecked(true);
	    }
	    
	}
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
