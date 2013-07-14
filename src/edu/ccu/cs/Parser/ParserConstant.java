package edu.ccu.cs.Parser;


public final class ParserConstant {

    // Parsing mode
    public static final int COURSE_LIST_MODE = 0;
    public static final int NEWS_LIST_MODE = 1;
    public static final int NEWS_MODE = 2;
    public static final int GRADE_LIST_MODE = 3;
    public static final int FINAL_GRADE_MODE = 4;
    public static final int MATERIAL_MENU_MODE = 5;
    public static final int MATERIAL_LIST_MODE = 6;
    
    
    // Constant string
    public static final String COURSE_NAME = "courseName";
    public static final String URL = "url";
    public static final String DATE = "date";
    public static final String TITLE = "title";
    public static final String IS_LATEST = "isLatest";
    public static final String CONTENT = "content";
    public static final String NAME = "name";
    public static final String PERCENTAGE = "percentage";
    public static final String SCORE = "score";
    public static final String RATING = "rating";
    public static final String MODIFY = "modify";
    
    
    public static final String ECOURSE_URL = "http://ecourse.elearning.ccu.edu.tw/";
    
    
    // This class cannot be instantiated
    private ParserConstant() {}

}
