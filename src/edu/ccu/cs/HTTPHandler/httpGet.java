package edu.ccu.cs.HTTPHandler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class httpGet implements Runnable
{
	String url;
	String html;
	String session;
	private boolean done;
	
	public httpGet(String url, String session)
	{
		this.url = url;
		this.session = session;
		html = "";
		done = false;
	}
	
	public void run()
    {
        getHtmlByGet(url);
        //getHtmlByGet("http://ecourse.elearning.ccu.edu.tw/php/login_s.php?courseid=101_2_32229");
        //getHtmlByGet("http://ecourse.elearning.ccu.edu.tw/php/Trackin/SGQueryFrame1.php");
        done = true;
        
    }
	
	public void getHtmlByGet(String url)
	{ 
		HttpClient client = new DefaultHttpClient();
		
		HttpGet httpRequest = new HttpGet(url);
		httpRequest.setHeader("Cookie", "PHPSESSID=" + session);
	
		html="";
        HttpResponse httpResponse;
        
        try 
        {
           
        	httpResponse = client.execute(httpRequest); 
        	
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
            	//html += "\nHTTP 200 OK \n\n";
            	HttpEntity resEntity = httpResponse.getEntity();
                
                if (resEntity != null) 
                {    
                	html += EntityUtils.toString(resEntity, "big5");
                }
                else
                	html += "Response is null\n";
            } 
            
            else 
            {
            	html += "HTTP get FAIL\n";
            }

        } 
        catch (Exception e)
        {
        	html += "HTTP EXCEPTION\n";
            e.printStackTrace();
        } 
        finally
        {
        	client.getConnectionManager().shutdown();
        }
        
        return;
	}
	
	public String getResult()
	{
		return html;
	}
	
	public boolean isDone()
	{
		return done;
	}
}
