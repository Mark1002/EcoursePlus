package edu.ccu.cs.Parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Parser {

    private final Document doc;
    private final int parsingMode;
    private ArrayList<HashMap<String, String>> parsingResult;
    
    public Parser(String html, int mode) {
        doc = Jsoup.parse(html);
        parsingMode = mode;
        parsingResult = new ArrayList<HashMap<String, String>>();
    }
    
    // If parsing fail, return null
    // parsingResult may contain zero item
    public ArrayList<HashMap<String, String>> parse() {
        return (parsingNavigator() ? parsingResult : null);
    }
    
    private boolean parsingNavigator() {
        switch (parsingMode) {
        case ParserConstant.COURSE_LIST_MODE:
            parseCourseList();
            break;
        case ParserConstant.NEWS_LIST_MODE:
            parseNewsList();
            break;
        case ParserConstant.NEWS_MODE:
            parseNews();
            break;
        case ParserConstant.GRADE_LIST_MODE:
            parseGradeList();
            break;
        case ParserConstant.FINAL_GRADE_MODE:
            parseFinalGrade();
            break;
        case ParserConstant.MATERIAL_MENU_MODE:
            parseMaterialMenu();
            break;
        case ParserConstant.MATERIAL_LIST_MODE:
            parseMaterialList();
            break;
        default:
            return false;
        }
        
        return true;
    }
    
    private void parseCourseList() {
        Elements list = doc.select("table tr[bgcolor~=(#E6FFFC|#F0FFEE)]");
        
        for(Element listItem : list) {
            Element courseElement = listItem.child(3).child(0).child(0);
            Element nameElement = listItem.child(4).child(0).child(0);
            
            HashMap<String, String> item = new HashMap<String, String>();
            item.put(ParserConstant.COURSE_NAME, courseElement.text());
            
            String url = ParserConstant.ECOURSE_URL + "php/" + courseElement.attr("href").substring(3);
            item.put(ParserConstant.URL, url);
            
            item.put(ParserConstant.NAME, nameElement.text());
            
            parsingResult.add(item);
        }
    }
    
    private void parseNewsList() {
        Elements list = doc.select("table tr[bgcolor~=(#F0FFEE|#E6FFFC)]");
        
        for(Element listItem : list) {
            Element dateElement = listItem.child(0).child(0).child(0);
            Element titleElement = listItem.child(2).child(0).child(0).child(0);
            
            HashMap<String, String> item = new HashMap<String, String>();
            item.put(ParserConstant.DATE, dateElement.text());
            item.put(ParserConstant.TITLE, titleElement.text());
            
            String[] token = titleElement.attr("onClick").split("(\'|\")");
            String url = ParserConstant.ECOURSE_URL + "php/news/" + token[1].substring(2);
            item.put(ParserConstant.URL, url);
            
            String isLatest = (titleElement.children().size() == 1) ? "NEW!" : "";
            item.put(ParserConstant.IS_LATEST, isLatest);
            
            parsingResult.add(item);
        }
    }
    
    private void parseNews() {
        Elements news = doc.select("table[bordercolor=#4d6eb2] tbody");
        
        for(Element newsRow : news) {
            Element titleElement = newsRow.child(0).child(1).child(0).child(0);
            Element dateElement = newsRow.child(1).child(1).child(0).child(0);
            Element contentElement = newsRow.child(2).child(1).child(0).child(0);
            
            HashMap<String, String> item = new HashMap<String, String>();
            item.put(ParserConstant.TITLE, titleElement.text());
            item.put(ParserConstant.DATE, dateElement.text());
            item.put(ParserConstant.CONTENT, contentElement.text());
            
            parsingResult.add(item);
        }
    }

    private void parseGradeList() {
        Elements list = doc.select("table tr[bgcolor~=(#F0FFEE|#E6FFFC)]");
        
        for(Element listItem : list) {
            Element nameElement = listItem.child(0);
            Element percentageElement = listItem.child(1);
            Element scoreElement = listItem.child(2);
            Element ratingElement = listItem.child(3);
            
            HashMap<String, String> item = new HashMap<String, String>();
            item.put(ParserConstant.NAME, nameElement.text());
            item.put(ParserConstant.PERCENTAGE, percentageElement.text());
            item.put(ParserConstant.SCORE, scoreElement.text());
            item.put(ParserConstant.RATING, ratingElement.text());
            
            parsingResult.add(item);
        }
    }

    private void parseFinalGrade() {
        Elements grade = doc.select("table tr[bgcolor=#B0BFC3]");
        
        Element ratingElement = grade.get(0).child(1);
        Element scoreElement = grade.get(1).child(1);
        
        HashMap<String, String> item = new HashMap<String, String>();
        item.put(ParserConstant.RATING, ratingElement.text());
        item.put(ParserConstant.SCORE, scoreElement.text());
        
        parsingResult.add(item);
    }
    
    private void parseMaterialMenu() {
        Elements menu = doc.select("div a[href]");
        
        for(Element menuItem : menu) {
            HashMap<String, String> item = new HashMap<String, String>();
            item.put(ParserConstant.NAME, menuItem.text());
            
            String url = ParserConstant.ECOURSE_URL + "php/textbook/" + menuItem.attr("href");
            item.put(ParserConstant.URL, url);
            
            parsingResult.add(item);
        }
    }
    
    private void parseMaterialList() {
        Elements list = doc.select("table tr[bgcolor~=(#edf3fa|#ffffff)]");
        
        for(Element listItem : list) {
            Element urlElement = listItem.child(0).child(0);
            Element modifyElement = listItem.child(2);
            
            HashMap<String, String> item = new HashMap<String, String>();
            item.put(ParserConstant.NAME, urlElement.text());
            
            String url = ParserConstant.ECOURSE_URL + urlElement.attr("href").substring(6);
            item.put(ParserConstant.URL, url);
            
            item.put(ParserConstant.MODIFY, modifyElement.text());
            
            parsingResult.add(item);
        }
    }
}

