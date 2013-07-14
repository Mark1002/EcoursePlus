package edu.ccu.cs;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ccu.cs.HTTPHandler.HtmlGetter;
import edu.ccu.cs.Parser.Parser;
import edu.ccu.cs.Parser.ParserConstant;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class CourseListActivity extends Activity implements OnItemClickListener{
	
	private ListView listView;
	private SimpleAdapter mSimpleadapter;
	private String acc;
	private String pwd;
	private Intent intent;
	private TextView mtextviewLoading;
	private ArrayList<HashMap<String, String>> mCourseList;
	private static final int LOADING_LIST = 0x0001;
	private static final int LOADING_DONE = 0x0002;
	private static boolean load_done = false;
		
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		load_done = false;
		
		final MyDataObject data = (MyDataObject)getLastNonConfigurationInstance();
		final MyHandler handler = new MyHandler(this);
		
		if (data != null && data.tload_done) {
			mCourseList = data.tCourseList;
			acc = data.tacc;
			pwd = data.tpwd;
			mSimpleadapter = data.tSimpleadapter;
			load_done = data.tload_done;
			setContentView(R.layout.layout_course_list);
			listView = (ListView)findViewById(R.id.listViewCourseList);
			listView.setAdapter(mSimpleadapter);
			listView.setOnItemClickListener(CourseListActivity.this);	
		}
		else {

			setContentView(R.layout.layout_loading);
			mtextviewLoading = (TextView)findViewById(R.id.textViewLoading);
			new Thread(){
				public void run(){
					try{
						load(handler);
						Message m = new Message();
						m.what = LOADING_DONE; 
						handler.sendMessage(m);
					}
					catch(Exception e) { 
						e.printStackTrace(); 
					}
				}
			}.start();
		}
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.logout:
	        	Intent logout = new Intent(this, LoginActivity.class);
	        	logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(logout);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	protected boolean load(MyHandler handler) {
		// TODO Auto-generated method stub
		Message m = new Message();
		m.what = LOADING_LIST; 
		handler.sendMessage(m);
		
		Bundle bundle = this.getIntent().getExtras();
		acc = bundle.getString("acc");
		pwd = bundle.getString("pwd");
		HtmlGetter mHtmlGetter = new HtmlGetter();
		String courseHtml = mHtmlGetter.login(acc, pwd);;
		if(courseHtml.equals("Login Failed")){
			Log.d("loading at course list", "fail");
			return false;
		}
		mCourseList = new Parser(courseHtml, ParserConstant.COURSE_LIST_MODE).parse();
		
		load_done = true;
		return true;
	}

	class MyHandler extends Handler {
		public MyHandler(CourseListActivity courseListActivity) {
			// TODO Auto-generated constructor stub
			this.mContext = courseListActivity;
		}
		private Context mContext;
		@Override 
		public void handleMessage(Message msg) { 
			switch (msg.what) {
			case LOADING_LIST:
				checkNetwork();
				mtextviewLoading.setText("Loading Course List...");
				break;
			case LOADING_DONE:
				checkNetwork();
				setContentView(R.layout.layout_course_list);
				listView = (ListView)findViewById(R.id.listViewCourseList);
				mSimpleadapter = new SimpleAdapter(mContext
						, mCourseList
						, R.layout.layout_course_list_item
						, new String[]{ParserConstant.NAME, ParserConstant.COURSE_NAME}
						, new int[]{R.id.listViewCourseListTeacher, R.id.listViewCourseListCourse});
					listView.setAdapter(mSimpleadapter);
					listView.setOnItemClickListener(CourseListActivity.this);
				break;
			}
		}
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
		intent = new Intent().setClass(this, MainActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("listViewCount", arg2);
		bundle.putString("acc", acc);
		bundle.putString("pwd", pwd);
		intent.putExtras(bundle);
		startActivity(intent);
		Log.d("click", Integer.toString(arg2));
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//Toast.makeText(getApplicationContext(), "CourseListActivity:Destory", Toast.LENGTH_SHORT).show();
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//Toast.makeText(getApplicationContext(), "CourseListActivity:Pause", Toast.LENGTH_SHORT).show();
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//Toast.makeText(getApplicationContext(), "CourseListActivity:Resume", Toast.LENGTH_SHORT).show();
	}


	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		//Toast.makeText(getApplicationContext(), "CourseListActivity:Start", Toast.LENGTH_SHORT).show();
	}


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//Toast.makeText(getApplicationContext(), "CourseListActivity:Stop", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		final MyDataObject data = collectMyLoadedData();
		return data;
	}
	
	private MyDataObject collectMyLoadedData() {
		// TODO Auto-generated method stub
		return new MyDataObject(mCourseList, mSimpleadapter, acc, pwd, load_done);
	}
	
	private class MyDataObject {
		public MyDataObject(ArrayList<HashMap<String, String>> inCourseList, SimpleAdapter inSimpleadapter, String inacc, String inpwd, boolean inload_done) {
			// TODO Auto-generated constructor stub
			this.tCourseList = inCourseList;
			this.tSimpleadapter = inSimpleadapter;
			this.tacc = inacc;
			this.tpwd = inpwd;
			this.tload_done = inload_done;
		}

		private ArrayList<HashMap<String, String>> tCourseList;
		private SimpleAdapter tSimpleadapter;
		private String tacc;
		private String tpwd;
		private boolean tload_done;
	}
	private void checkNetwork() {
		if (!isConnectingToInternet()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Failed to access network")
			       .setTitle("ERROR");
			AlertDialog dialog = builder.create();
			dialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which){
            		Intent logout = new Intent(getApplicationContext(), LoginActivity.class);
        	        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	        startActivity(logout);	
                }});
			dialog.show();
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
}
