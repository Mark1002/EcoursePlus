/**
 *	MainActivity
 *	簡介：此Activity用來控制讀取不同課程間的資訊
 * 
 * */

package edu.ccu.cs;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;

import com.actionbarsherlock.app.*;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ActionBar.OnNavigationListener;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import edu.ccu.cs.Parser.Parser;
import edu.ccu.cs.Parser.ParserConstant;
import edu.ccu.cs.HTTPHandler.HtmlGetter;

/***
 * 
 * Extends: FragmentActivity
 * 	- 由於要利用viewpager控制fragment
 * Implements:
 * 	OnNavigationListener
 * 		- 監聽navigation
 *
 */

public class MainActivity extends FragmentActivity /*implements OnNavigationListener*/ 
{

	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	public static ArrayList<HashMap<String, String>> mMaterialMenu;
	private static int listViewCount;
	private static String acc;
	private static String pwd;
	private static ArrayList<HashMap<String, String>> mAnnouList;
	private static ArrayList<HashMap<String, String>> mScoreList;
	private static ArrayList<HashMap<String, String>> mTotalScoreList;
	private static ArrayList<HashMap<String, String>> mCourseListName;
	private static ArrayList<HashMap<String, String>> mCourseList;
	private static ArrayAdapter mArrayAdapter;
	private static AnnounceAdapter mAnnounceAdapter;
	private static MeterialAdapter mMeterialAdapter;
	private static ScoreAdapter mScoreAdapter;
	private static boolean load_done = false;
	private static final int LOADING_ANNO = 0x0001;
	private static final int LOADING_MATE = 0x0002;
	private static final int LOADING_SCOR = 0x0003;
	private static final int LOADING_DONE = 0x0004;
	private TextView mtextviewLoading; 
	private Spinner mSpinner;
	private boolean readOrNot = false;
	private static DownloadManager manager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		load_done = false; // 讀取尚未結束
		manager =(DownloadManager)getSystemService(DOWNLOAD_SERVICE); 
		// 如果螢幕轉向了，會重新呼叫oncreate，這邊可以幫助把資料讀回來
		final MyDataObject data = (MyDataObject) getLastCustomNonConfigurationInstance();
		if (data != null && data.tload_done) {
			setContentView(R.layout.layout_main);
			mAnnouList = data.tAnnouList;
			mScoreList = data.tScoreList;
			mCourseListName = data.tCourseListName;
			mAnnounceAdapter = data.tAnnounceAdapter;
			mMeterialAdapter = data.tMeterialAdapter;
			mSpinner = data.tSpinner;
			mArrayAdapter = data.tArrayAdapter;
			mScoreAdapter = data.tScoreAdapter;
			acc = data.tacc;
			pwd = data.tpwd;
			listViewCount = data.tlistViewCount;
			load_done = data.tload_done;

			mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
			mViewPager = (ViewPager) findViewById(R.id.MainPager); 
			mViewPager.setAdapter(mSectionsPagerAdapter);
			
			PagerTitleStrip mPagerTitleStrip = (PagerTitleStrip)findViewById(R.id.pagerMainTitleStrip);
			mPagerTitleStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
			mPagerTitleStrip.setTextSpacing(100);
			mPagerTitleStrip.setTextColor(Color.WHITE);
			
/*
			final ActionBar actionBar = getActionBar();
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setListNavigationCallbacks(mArrayAdapter, this);
			actionBar.setSelectedNavigationItem(listViewCount);
			actionBar.setDisplayShowTitleEnabled(false);*/
		}
		else {	// 如果沒有轉向
			setContentView(R.layout.layout_loading);
			checkNetwork();
			final MyHandler handler = new MyHandler();
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
	/**
	 * onOptionsItemSelected()
	 * 		- 回上一層或是登出
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(this, CourseListActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				Bundle bundle =  new Bundle();
				bundle.putString("acc", acc);
				bundle.putString("pwd", pwd);
				intent.putExtras(bundle);
				startActivity(intent);
	            return true;
	        case R.id.logout:
	        	Intent logout = new Intent(this, LoginActivity.class);
	            logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(logout);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	/**
	 * onCreateOptinsMenu()
	 * 		- 將登出option載入
	 */
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}



	class MyHandler extends Handler /*implements  ActionBar.OnNavigationListener*/{ 
		@Override 
		public void handleMessage(Message msg) { 
			switch (msg.what) {
				case LOADING_ANNO:
					checkNetwork();
					mtextviewLoading.setText("Loading Announcements..."); 
					break;
				case LOADING_MATE:
					checkNetwork();
					mtextviewLoading.setText("Loading Materials..."); 
					break;
				case LOADING_SCOR:
					checkNetwork();
					mtextviewLoading.setText("Loading Scores..."); 
					break;
				case LOADING_DONE: 
					checkNetwork();
					setContentView(R.layout.layout_main);
					
					mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
					mViewPager = (ViewPager) findViewById(R.id.MainPager);
					mViewPager.setAdapter(mSectionsPagerAdapter);
					
					PagerTitleStrip mPagerTitleStrip = (PagerTitleStrip)findViewById(R.id.pagerMainTitleStrip);
					mPagerTitleStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
					mPagerTitleStrip.setTextSpacing(100);
					mPagerTitleStrip.setTextColor(Color.WHITE);
					/*
					final ActionBar actionBar = getActionBar();
					actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
					
					actionBar.setDisplayHomeAsUpEnabled(true);
					*/
					ArrayList<String> mCourseListName = new ArrayList<String>();
					Iterator<HashMap<String, String>> tempiterator = mCourseList.iterator();
					while(tempiterator.hasNext()){
						HashMap<String, String> item = tempiterator.next();
						mCourseListName.add(item.get(ParserConstant.COURSE_NAME));
					}
					mArrayAdapter = new ArrayAdapter(getApplicationContext(), R.layout.layout_drop_down_list_item, mCourseListName);

					mSpinner = new Spinner(getApplicationContext());
					mSpinner.setAdapter(mArrayAdapter);
					/*
					actionBar.setListNavigationCallbacks(mArrayAdapter, this);
					actionBar.setSelectedNavigationItem(listViewCount);
					actionBar.setDisplayShowTitleEnabled(false);*/
					
					break;
			} 
			super.handleMessage(msg); 
		} 
		
		/**
		 * onNavigationItemSelected()
		 * 		- 依照選取的課程，將參數重新帶入此activity
		 */
		/*
		public boolean onNavigationItemSelected(int arg0, long arg1) {
			// TODO Auto-generated method stub
			// 解決
			if(!readOrNot){
				readOrNot = true;
			}
			else{
				Intent intent = new Intent().setClass(getApplicationContext(), MainActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("listViewCount", arg0);
				bundle.putString("acc", acc);
				bundle.putString("pwd", pwd);
				intent.putExtras(bundle);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			return false;
		}*/
		 

	}
	/**
	 * 
	 * @param handler
	 * @return 是否載入成功
	 */
	private boolean load(MyHandler handler) {
		Message m1 = new Message();
		m1.what = LOADING_ANNO; 
		handler.sendMessage(m1);
		Bundle bundle = this.getIntent().getExtras();
		listViewCount = bundle.getInt("listViewCount");
		acc = bundle.getString("acc");
		pwd = bundle.getString("pwd");
		HtmlGetter mHtmlGetter = new HtmlGetter();
		String courseHtml = mHtmlGetter.login(acc, pwd);;
		if(courseHtml.equals("Login Failed")){
			Log.d("loading", "fail");
			return false;
		}
		mCourseList = new Parser(courseHtml, ParserConstant.COURSE_LIST_MODE).parse();
		mHtmlGetter.course(mCourseList.get(listViewCount).get(ParserConstant.URL));
		
		mAnnouList = new Parser(mHtmlGetter.newsList(), ParserConstant.NEWS_LIST_MODE ).parse();
		mAnnounceAdapter = new AnnounceAdapter(this.getLayoutInflater());
		Iterator<HashMap<String, String>>   iterator = mAnnouList.iterator();
		iterator = mAnnouList.iterator();
		while(iterator.hasNext()){
			HashMap<String, String> item = iterator.next();
			mAnnounceAdapter.addItem(item.get(ParserConstant.TITLE), item.get(ParserConstant.DATE), item.get(ParserConstant.IS_LATEST));
		}
		Message m2 = new Message();
		m2.what = LOADING_MATE; 
		handler.sendMessage(m2);
		mMaterialMenu = new Parser(mHtmlGetter.materialMenu(), ParserConstant.MATERIAL_MENU_MODE).parse();
		mMeterialAdapter = new MeterialAdapter(this.getLayoutInflater());
		iterator = mMaterialMenu.iterator();
        while (iterator.hasNext()) {
        	HashMap<String, String> item = iterator.next();
        	mMeterialAdapter.addMenu(item.get(ParserConstant.NAME));
        	ArrayList<HashMap<String, String>> mMaterialList = new Parser(mHtmlGetter.materialList(item.get(ParserConstant.URL)), ParserConstant.MATERIAL_LIST_MODE).parse();
        	Iterator<HashMap<String, String>> listIterator = mMaterialList.iterator();
        	while (listIterator.hasNext()) {
				HashMap<String, String> listitem = (HashMap<String, String>) listIterator.next();
				mMeterialAdapter.addItem(listitem.get(ParserConstant.NAME), listitem.get(ParserConstant.MODIFY),listitem.get(ParserConstant.URL));
			}
        }
		
		Message m3 = new Message();
		m3.what = LOADING_SCOR; 
		handler.sendMessage(m3);
		
		mScoreList = new Parser(mHtmlGetter.grade(), ParserConstant.GRADE_LIST_MODE).parse();
		mTotalScoreList = new Parser(mHtmlGetter.grade(), ParserConstant.FINAL_GRADE_MODE).parse();
		mScoreAdapter = new ScoreAdapter(this.getLayoutInflater());
		iterator = mScoreList.iterator();
        while (iterator.hasNext()) {
        	HashMap<String, String> item = iterator.next();
        	mScoreAdapter.addEach(item.get(ParserConstant.NAME), item.get(ParserConstant.PERCENTAGE), item.get(ParserConstant.SCORE), item.get(ParserConstant.RATING));
        }
		iterator = mTotalScoreList.iterator();
		while(iterator.hasNext()){
			HashMap<String, String> item = (HashMap<String, String>) iterator.next();
			mScoreAdapter.addTotal(item.get(ParserConstant.SCORE), item.get(ParserConstant.RATING));
		}
		
		load_done = true;
		Log.d("loading", "done");
		return true;
	}

	/**
	 * 
	 * 用來控制讀取不同fragment的adapter
	 *
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		@Override
		public Fragment getItem(int position) {
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {return 3;}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.fragment_title_1).toUpperCase(l);
			case 1:
				return getString(R.string.fragment_title_2).toUpperCase(l);
			case 2:
				return getString(R.string.fragment_title_3).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";		
		ListView listView;
		SimpleAdapter adapter;
		SimpleAdapter adapter_total;
		
		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.layout_announcement_list, container, false);	
			switch(getArguments().getInt(ARG_SECTION_NUMBER)){
			case 1:
				// 將listview的layout讀出來
				rootView = inflater.inflate(R.layout.layout_announcement_list, container, false);
				((ListView)rootView.findViewById(R.id.listViewAnnouncementList)).setAdapter(mAnnounceAdapter);
				((ListView)rootView.findViewById(R.id.listViewAnnouncementList)).setOnItemClickListener(new listViewListener());
				
				break;
			case 2:
				rootView = inflater.inflate(R.layout.layout_material_list, container, false);

				((ListView)rootView.findViewById(R.id.listViewMaterialList)).setAdapter(mMeterialAdapter);
				((ListView)rootView.findViewById(R.id.listViewMaterialList)).setOnItemClickListener(new materialListViewListener());
				((ListView)rootView.findViewById(R.id.listViewMaterialList)).setOnItemLongClickListener(new materialListViewListener());
				break;
			case 3:
				rootView = inflater.inflate(R.layout.layout_score_list, container, false);
				((ListView)rootView.findViewById(R.id.listViewScoreList)).setAdapter(mScoreAdapter);
				break;
			}
			return rootView;
		}
		
		public class listViewListener implements OnItemClickListener{
			private ProgressDialog mDialog;

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
				mDialog = ProgressDialog.show(getActivity(), "Please wait", "Loading");
				new Thread(){
					public void run(){
						try{
							Bundle bundle = new Bundle();
							final Intent intent = new Intent().setClass(getActivity(), AnnouncementDetailActivity.class);
							bundle.putString("acc", acc);
							bundle.putString("pwd", pwd);
							bundle.putInt("listViewCount", listViewCount);
							bundle.putInt("announcementCount", arg2);
							intent.putExtras(bundle);
							startActivity(intent);
						}
						catch(Exception e) { 
							e.printStackTrace(); 
						}
						mDialog.dismiss();
						}
				}.start();
			}
		}
		
		public class materialListViewListener implements OnItemLongClickListener,OnItemClickListener {

			

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
								
				//Toast.makeText(getActivity(), ((HashMap<String, String>)arg0.getItemAtPosition(arg2)).get("url").toString(), Toast.LENGTH_LONG).show();

				if(((HashMap<String, String>)arg0.getItemAtPosition(arg2)).containsKey("url"))
				{
					String url=((HashMap<String, String>)arg0.getItemAtPosition(arg2)).get("url").toString();				
					Uri uri= Uri.parse("https://docs.google.com/viewer?embedded=true&url="+Uri.encode(url));
					Intent intent=new Intent(Intent.ACTION_VIEW,uri);
					startActivity(intent);
				}
			}

			@Override
			public boolean onItemLongClick(final AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {
				// TODO Auto-generated method stub
				if(((HashMap<String, String>)arg0.getItemAtPosition(arg2)).containsKey("url"))
				{	
					AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(getActivity());
					final String url=((HashMap<String, String>)arg0.getItemAtPosition(arg2)).get("url").toString();
					dialogBuilder.setTitle("Download");
					dialogBuilder.setMessage(((HashMap<String, String>)arg0.getItemAtPosition(arg2)).get("title").toString());
					
					DialogInterface.OnClickListener clickListener=new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Uri uri=Uri.parse(url);
							Request request=new Request(uri);
							request.setDestinationInExternalPublicDir("/download/", ((HashMap<String, String>)arg0.getItemAtPosition(arg2)).get("title").toString());
							//request.setNotificationVisibility(request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
							((DownloadManager) manager).enqueue(request);  
						}
					};
					DialogInterface.OnClickListener notclickListener=new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					};
					
					dialogBuilder.setNegativeButton("cancel", notclickListener);
					dialogBuilder.setPositiveButton("download", clickListener);
					dialogBuilder.show();

				}
				return true;
			}
			
		}
	}
	
	/**
	 * 
	 * ListView要客製化顯示的內容，利用設計不同的adapter
	 *
	 */
	private class AnnounceAdapter extends BaseAdapter {
		private static final int TYPE_ANNO_LIST = 0;
		private static final int TYPE_MAX_COUNT = 1;
		private ArrayList<HashMap<String, String>> mData = new ArrayList<HashMap<String,String>>();
		private LayoutInflater mInflater;
		//private TreeSet mSeparatorsSet = new TreeSet();

		public AnnounceAdapter(LayoutInflater inflater){
			//mInflater = (LayoutInflater)getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.mInflater = inflater;
		}
		
		public void addItem(final String title, final String date, final String isLatest){
			HashMap<String, String> item = new HashMap<String, String>();
			item.put("title", title);
			item.put("date", date);
			item.put("isLatest", isLatest);
			mData.add(item);
            notifyDataSetChanged();
		}
		
		@Override
        public int getViewTypeCount() {
            return TYPE_MAX_COUNT;
        }
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mData.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return mData.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			AnnounceViewHolder holder = null;
			if (convertView == null) {
				holder = new AnnounceViewHolder();
                convertView = mInflater.inflate(R.layout.layout_announcement_list_item, null);
                holder.title = (TextView)convertView.findViewById(R.id.textViewAnnouncementTitle);
                holder.date = (TextView)convertView.findViewById(R.id.textViewAnnouncementDate);
                holder.isLatest = (TextView)convertView.findViewById(R.id.textViewAnnouncementNEW);
                convertView.setTag(holder);
			}else{
				holder = (AnnounceViewHolder)convertView.getTag();
			}
			holder.title.setText(mData.get(position).get("title"));
			holder.date.setText(mData.get(position).get("date"));
			holder.isLatest.setText(mData.get(position).get("isLatest"));
			convertView.setBackgroundColor(position % 2 == 0 ? Color.WHITE : getResources().getColor(R.color.red2));
            
            return convertView;
		}
		
	}
	
	private class MeterialAdapter extends BaseAdapter {
		private static final int TYPE_MENU = 0;
		private static final int TYPE_LIST = 1;
		private static final int TYPE_MAX_COUNT = 2;
		private ArrayList<HashMap<String, String>> mData = new ArrayList<HashMap<String,String>>();
		private LayoutInflater mInflater;
		private TreeSet mSeparatorsSet = new TreeSet();
		private int itemcount = 0;
		public MeterialAdapter(LayoutInflater inflater){
			//mInflater = (LayoutInflater)getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.mInflater = inflater;
		}
		
		public void addItem(final String itemName, final String date, final String url){
			HashMap<String, String> item = new HashMap<String, String>();
			item.put("title", itemName);
			item.put("date", date);
			item.put("url", url);
			mData.add(item);
            notifyDataSetChanged();
		}
		
		public void addMenu(final String menuName) {
			HashMap<String, String> item = new HashMap<String, String>();
			item.put("title", menuName);
            mData.add(item);
            // save separator position
            mSeparatorsSet.add(mData.size() - 1);
            notifyDataSetChanged();
        }
		
		@Override
		public int getItemViewType(int position) {
            return mSeparatorsSet.contains(position) ? TYPE_MENU : TYPE_LIST;
        }
		
		@Override
        public int getViewTypeCount() {
            return TYPE_MAX_COUNT;
        }
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mData.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return mData.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
            int type = getItemViewType(position);
            
            if (type == TYPE_MENU) {
				MenuViewHolder holder = null;
				if (convertView == null) {	
					holder = new MenuViewHolder();
                    convertView = mInflater.inflate(R.layout.layout_material_menu_item, null);
                    holder.title = (TextView)convertView.findViewById(R.id.textViewMertialMenuTitle);
                    convertView.setTag(holder);
				} else {
					holder = (MenuViewHolder)convertView.getTag();					
				}
				holder.title.setText(mData.get(position).get("title"));
				holder.title.setTextColor(Color.WHITE);
				convertView.setBackgroundColor(getResources().getColor(R.color.blue2));
            } else {
            	itemcount++;
                ListViewHolder holder = null;
				if (convertView == null) {
					holder = new ListViewHolder();
					convertView = mInflater.inflate(R.layout.layout_material_list_item, null);
					holder.title = (TextView)convertView.findViewById(R.id.textViewMeterialTitle);
					holder.date = (TextView)convertView.findViewById(R.id.textViewMeterialDate);
                    convertView.setTag(holder);
					
				} else {
                	holder = (ListViewHolder)convertView.getTag();
				}
				holder.title.setText(mData.get(position).get("title"));
				holder.date.setText(mData.get(position).get("date"));
				convertView.setBackgroundColor(itemcount % 2 == 0 ? Color.WHITE : getResources().getColor(R.color.blue1));
            }
            return convertView;
		}
		
	}
	
	private class ScoreAdapter extends BaseAdapter {
		private static final int TYPE_EACH = 0;
		private static final int TYPE_TOTAL = 1;
		private static final int TYPE_MAX_COUNT = 2;
		private ArrayList<HashMap<String, String>> mData = new ArrayList<HashMap<String,String>>();
		private LayoutInflater mInflater;
		private TreeSet mSeparatorsSet = new TreeSet();

		public ScoreAdapter(LayoutInflater inflater){
			//mInflater = (LayoutInflater)getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.mInflater = inflater;
		}
		
		public void addEach(final String name, final String percentage, final String score, final String rating){
			HashMap<String, String> item = new HashMap<String, String>();
			item.put("name", name);
			item.put("percentage", percentage);
			item.put("score", score);
			item.put("rating", rating);
			mData.add(item);
            notifyDataSetChanged();
		}
		
		public void addTotal(final String score, final String rating) {
			HashMap<String, String> item = new HashMap<String, String>();
			item.put("score", score);
			item.put("rating", rating);
            mData.add(item);
            // save separator position
            mSeparatorsSet.add(mData.size() - 1);
            notifyDataSetChanged();
        }
		
		@Override
		public int getItemViewType(int position) {
            return mSeparatorsSet.contains(position) ? TYPE_TOTAL : TYPE_EACH;
        }
		
		@Override
        public int getViewTypeCount() {
            return TYPE_MAX_COUNT;
        }
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mData.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return mData.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
            int type = getItemViewType(position);

            if (type == TYPE_EACH) {
				ScoreEachViewHolder holder = null;
				if (convertView == null) {	
					holder = new ScoreEachViewHolder();
                    convertView = mInflater.inflate(R.layout.layout_score_list_item, null);
                    holder.name = (TextView)convertView.findViewById(R.id.ScoreListItemTitle);
                    holder.percentage = (TextView)convertView.findViewById(R.id.ScoreListItemPercentage);
                    holder.score = (TextView)convertView.findViewById(R.id.ScoreListItemScore);
                    holder.rating = (TextView)convertView.findViewById(R.id.ScoreListItemPosition);
                    convertView.setTag(holder);
				} else {
					holder = (ScoreEachViewHolder)convertView.getTag();					
				}
				holder.name.setText(mData.get(position).get("name"));
				holder.percentage.setText(mData.get(position).get("percentage"));
				holder.score.setText(mData.get(position).get("score"));
				holder.rating.setText(mData.get(position).get("rating"));
				if(mData.get(position).get("score")!="" && Double.parseDouble(mData.get(position).get("score")) < 60.0){
					holder.score.setTextColor(Color.RED);
				}
				convertView.setBackgroundColor(position % 2 != 0 ? Color.WHITE : getResources().getColor(R.color.yellow1));
            } else {
                ScoreTotalViewHolder holder = null;
				if (convertView == null) {
					holder = new ScoreTotalViewHolder();
					convertView = mInflater.inflate(R.layout.layout_score_list_item_totalgrade, null);
                    holder.score = (TextView)convertView.findViewById(R.id.DisplayTotalGrade);
                    holder.rating = (TextView)convertView.findViewById(R.id.DisplayTotalRating);
                    convertView.setTag(holder);
					
				} else {
                	holder = (ScoreTotalViewHolder)convertView.getTag();
				}
				holder.score.setText(mData.get(position).get("score"));
				holder.rating.setText(mData.get(position).get("rating"));
				if(mData.get(position).get("score")!="" && Double.parseDouble(mData.get(position).get("score")) < 60.0){
					holder.score.setTextColor(Color.RED);
				}
				convertView.setBackgroundColor(getResources().getColor(R.color.yellow1));
            }
            return convertView;
		}
	}
	
	public static class AnnounceViewHolder {
		public TextView title;
		public TextView date;
		public TextView isLatest;
	}
	public static class ScoreEachViewHolder {
		public TextView name;
		public TextView percentage;
		public TextView score;
		public TextView rating;
	}

	public static class ScoreTotalViewHolder {
		public TextView score;
		public TextView rating;
	}
	
	public static class MenuViewHolder {
        public TextView title;
    }
	
	public static class ListViewHolder {
        public TextView title;
        public TextView date;
    }
	
	/**
	 * 旋轉的期間會把資料暫時存起來
	 */
	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		final MyDataObject data = collectMyLoadedData();
		return data;
	}


	private MyDataObject collectMyLoadedData() {
		// TODO Auto-generated method stub
		return new MyDataObject(mCourseListName, mAnnouList, mScoreList, mAnnounceAdapter, mMeterialAdapter, mScoreAdapter, mSpinner, mArrayAdapter, acc, pwd, listViewCount, load_done);
	}
	
	private class MyDataObject {
		public MyDataObject(ArrayList<HashMap<String, String>> inCourseListName, 
				ArrayList<HashMap<String, String>> inAnnouList,
				ArrayList<HashMap<String, String>> inScoreList,
				AnnounceAdapter inAnnounceAdapter, MeterialAdapter inMeterialAdapter, ScoreAdapter inScoreAdapter, Spinner inSpinner, ArrayAdapter inArrayAdapter, String inacc, String inpwd, int inlistViewCount, boolean inload_done) {
			// TODO Auto-generated constructor stub
			this.tCourseListName = inCourseListName;
			this.tAnnouList = inAnnouList;
			this.tScoreList = inScoreList;
			this.tAnnounceAdapter = inAnnounceAdapter;
			this.tMeterialAdapter = inMeterialAdapter;
			this.tScoreAdapter = inScoreAdapter;
			this.tSpinner = inSpinner;
			this.tArrayAdapter = inArrayAdapter;
			this.tacc = inacc;
			this.tpwd = inpwd;
			this.tlistViewCount = inlistViewCount;
			this.tload_done = inload_done;
		}
		private ArrayList<HashMap<String, String>> tAnnouList;
		private ArrayList<HashMap<String, String>> tScoreList;
		private ArrayList<HashMap<String, String>> tCourseListName;
		private AnnounceAdapter tAnnounceAdapter;
		private MeterialAdapter tMeterialAdapter;
		private ScoreAdapter tScoreAdapter;
		private Spinner tSpinner;
		private ArrayAdapter tArrayAdapter;
		private String tacc;
		private String tpwd;
		private int tlistViewCount;
		private boolean tload_done;
	}
	/*@Override
	public boolean onNavigationItemSelected(int arg0, long arg1) {
		// TODO Auto-generated method stub
		if(!readOrNot){
			readOrNot = true;
		}
		else{
			Intent intent = new Intent().setClass(getApplicationContext(), MainActivity.class);
			Bundle bundle = new Bundle();
			bundle.putInt("listViewCount", arg0);
			bundle.putString("acc", acc);
			bundle.putString("pwd", pwd);
			intent.putExtras(bundle);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		return false;
	}*/
	/**
	 *  checkNetWork()
	 *  	- 檢查是否還有網路連線
	 */
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
