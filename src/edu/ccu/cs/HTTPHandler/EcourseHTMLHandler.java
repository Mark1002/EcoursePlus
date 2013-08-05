/***
 * 	Name : EcourseHTMLHandler.java
 *  Date : 2013/5/30
 *  Descirption : HTTP and Parser的結合
 * */

package edu.ccu.cs.HTTPHandler;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ccu.cs.Parser.Parser;
import edu.ccu.cs.Parser.ParserConstant;

public class EcourseHTMLHandler {
	private static String mAcc;
	private static String mPwd;
	private static String courseHtml;
	private static boolean isConnected = false;
	private static HtmlGetter htmlGetter = new HtmlGetter();
	public static ArrayList<HashMap<String, String>> mAnnouList;
	public static ArrayList<HashMap<String, String>> mScoreList;
	public static ArrayList<HashMap<String, String>> mCourseList;
	
	public EcourseHTMLHandler(String acc, String pwd){ mAcc = acc; mPwd = pwd;}
	public boolean setConnet(){
		courseHtml = htmlGetter.login(mAcc, mPwd);
		if(courseHtml.equals("Login Failed")){
			isConnected = false;
			return false;
		}
		else{
			isConnected = true;
		}
			
		return true;
	}
	/*** for test
	public static String getLoginHTML(){
		return courseHtml;
	}*/
	public ArrayList<HashMap<String, String>> getCourseName(){
		if(!isConnected){ setConnet();}
		return new Parser(courseHtml, ParserConstant.COURSE_LIST_MODE).parse();
	}
	
	public ArrayList<HashMap<String, String>> getAnnouncementList(int id){
		ArrayList<HashMap<String, String>> result = getCourseName();
		htmlGetter.course(result.get(id).get(ParserConstant.URL));
		return new Parser(htmlGetter.newsList(), ParserConstant.NEWS_LIST_MODE ).parse();	
	}
	
	public ArrayList<HashMap<String, String>> getAnnouncementListDetail(int id, int num){
		ArrayList<HashMap<String, String>> result = getAnnouncementList(id);
		String html = htmlGetter.news(result.get(num).get(ParserConstant.URL));
		return new Parser(html, ParserConstant.NEWS_MODE).parse();
	}
	
	public ArrayList<HashMap<String, String>> getMaterialMenu(int id){
		ArrayList<HashMap<String, String>> result = getCourseName();
		htmlGetter.course(result.get(id).get(ParserConstant.URL));
		String html = htmlGetter.materialMenu();
		return new Parser(html, ParserConstant.MATERIAL_MENU_MODE).parse();
	} 
	public ArrayList<HashMap<String, String>> getMaterialList(int id, int menuId){
		ArrayList<HashMap<String, String>> result = getMaterialMenu(id);
		String html = result.get(menuId).get(ParserConstant.URL);
		html = htmlGetter.materialList(html);
		return new Parser(html, ParserConstant.MATERIAL_LIST_MODE).parse();
	}
	
	public ArrayList<HashMap<String, String>> getGradeList(int id){
		ArrayList<HashMap<String, String>> result = getCourseName();
		htmlGetter.course(result.get(id).get(ParserConstant.URL));
		return new Parser(htmlGetter.grade(), ParserConstant.GRADE_LIST_MODE).parse();
	}
	public ArrayList<HashMap<String, String>> getFinalGrade(int id){
		ArrayList<HashMap<String, String>> result = getCourseName();
		htmlGetter.course(result.get(id).get(ParserConstant.URL));
		return new Parser(htmlGetter.grade(), ParserConstant.FINAL_GRADE_MODE).parse();
	}
	
}
