package edu.ccu.cs;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ccu.cs.HTTPHandler.EcourseHTMLHandler;
import edu.ccu.cs.Parser.ParserConstant;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AnnouncementDetailActivity extends Activity{
	
	private String acc;
	private String pwd;
	private int listViewCount;
	private int announcementCount;
	private EcourseHTMLHandler ecourseHTMLHandler;
	private TextView textViewTitle, textViewDate, textViewContent;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_announcement_detail);
		
		final ActionBar actionBar = getActionBar();
		/* 等實作完整的把資料存起來再使用 */
		// actionBar.setDisplayHomeAsUpEnabled(true);
		
		textViewTitle = (TextView)findViewById(R.id.AnnouncementDetailTitle);
		textViewDate = (TextView)findViewById(R.id.AnnouncementDetailDate);
		textViewContent = (TextView)findViewById(R.id.AnnouncementDetailContent);
		Bundle bundle = this.getIntent().getExtras();
		acc = bundle.getString("acc");
		pwd = bundle.getString("pwd");
		listViewCount = bundle.getInt("listViewCount");
		announcementCount = bundle.getInt("announcementCount");
		ecourseHTMLHandler = new EcourseHTMLHandler(acc, pwd);
		if(ecourseHTMLHandler.setConnet()){
			ArrayList<HashMap<String, String>> result = ecourseHTMLHandler.getAnnouncementListDetail(listViewCount, announcementCount);
			textViewTitle.setText(result.get(0).get(ParserConstant.TITLE));
			textViewDate.setText(result.get(0).get(ParserConstant.DATE));
			textViewContent.setText(result.get(0).get(ParserConstant.CONTENT));
		}
	}
}
