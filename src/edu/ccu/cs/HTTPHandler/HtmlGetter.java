package edu.ccu.cs.HTTPHandler;

public class HtmlGetter 
{
	private String loginUrl;
	private String sessionID;
	private String htmlContent;
	private final String eCourseUrl = "http://ecourse.elearning.ccu.edu.tw/php/index_login.php";
		
	public HtmlGetter()
	{
		htmlContent = sessionID = loginUrl = "";
	}
	
	public String getSessionID()
	{
		return sessionID;
	}
		
	public String login(String account, String password)
	{
		htmlContent = "";
		httpPost post = new httpPost(account, password, eCourseUrl);
		Thread thread = new Thread(post);
		thread.start();
		    
		// 等待登入完成
		while( !post.isDone() ) {
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		    
		sessionID = post.getSessionID();
		loginUrl = post.getCourseListUrl();
		    
		if(loginUrl == null)
			return "Login Failed";
		else
		{
		    httpGet get = new httpGet(loginUrl, sessionID);
		    Thread thread2 = new Thread(get);
		    thread2.start();
		        
		    // 等待取得修課清單
		    while( !get.isDone() ){
		        try {
		        	Thread.sleep(30);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }  
			htmlContent = get.getResult();
			return htmlContent;
		}
	}
	
	public void course(String url)
	{
		httpGet get = new httpGet(url, sessionID);
        Thread thread = new Thread(get);
        thread.start();
        while( !get.isDone() ) {
            try {
				Thread.sleep(30);
            	//wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } 
		return;
	}
	
	public String grade()
	{
		htmlContent = "";
		httpGet get = new httpGet("http://ecourse.elearning.ccu.edu.tw/php/Trackin/SGQueryFrame1.php", sessionID);
		Thread thread = new Thread(get);
        thread.start();
        while( !get.isDone() ) {
            try {
				Thread.sleep(30);
            	//wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }  
        htmlContent = get.getResult();
        return htmlContent;
	}
	
	public String newsList()
	{
		htmlContent = "";
		httpGet get = new httpGet("http://ecourse.elearning.ccu.edu.tw/php/news/news.php", sessionID);
		Thread thread = new Thread(get);
        thread.start();
        while( !get.isDone() ) {
            try {
				Thread.sleep(30);
            	//wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }  
        htmlContent = get.getResult();
        return htmlContent;
	}
	
	public String news(String url)
	{
		htmlContent = "";
		httpGet get = new httpGet(url+sessionID, sessionID);
		Thread thread = new Thread(get);
        thread.start();
        while( !get.isDone() ) {
            try {
				Thread.sleep(30);
            	//wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }  
        htmlContent = get.getResult();
        return htmlContent;
	}
	
	public String materialMenu()
	{
		htmlContent = "";
		httpGet get = new httpGet("http://ecourse.elearning.ccu.edu.tw/php/textbook/course_menu.php?list=1&PHPSESSID="+sessionID, sessionID);
		Thread thread = new Thread(get);
        thread.start();
        while( !get.isDone() ) {
            try {
				Thread.sleep(30);
            	//wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }  
        htmlContent = get.getResult();
        return htmlContent;
	}
	
	public String materialList(String url)
	{
		htmlContent = "";
		httpGet get = new httpGet(url, sessionID);
		Thread thread = new Thread(get);
        thread.start();
        while( !get.isDone() ) {
            try {
				Thread.sleep(30);
            	//wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }  
        htmlContent = get.getResult();
        return htmlContent;
	}
	
	public void logout()
	{
	
		httpGet get = new httpGet("http://ecourse.elearning.ccu.edu.tw/php/logout.php", sessionID);
		Thread thread = new Thread(get);
        thread.start();
        while( !get.isDone() ) {
            try {
				Thread.sleep(30);
            	//wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }  
		
		return;
	}
	

}
