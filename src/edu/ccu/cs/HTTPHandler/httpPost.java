package edu.ccu.cs.HTTPHandler;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class httpPost implements Runnable
{
	String url;
	String html;
	private boolean done;
	private boolean valid;
	String account;
	String password;
	String PHPSESSID;
	String sessionID;
	
	public httpPost(String account, String password, String url)
	{
		this.url = url;
		this.account = account;
		this.password = password;
		html = sessionID = PHPSESSID = "";
		done = false;
		valid = true;
	}
	
	public void run()
    {
        getHtmlByPost(url);
        done = true;
    }
	
	public void getHtmlByPost(String url)
	{    
		List<NameValuePair> mParams = new ArrayList<NameValuePair>();
	    mParams.add( new BasicNameValuePair( "id", account ) );
	    mParams.add( new BasicNameValuePair( "pass", password ) );
	    mParams.add( new BasicNameValuePair( "ver", "C" ) );
	   
		HttpClient client = new DefaultHttpClient();
		HttpPost httpRequest = new HttpPost(url);
        HttpResponse httpResponse;
        try 
        {
            
        	httpRequest.setEntity(new UrlEncodedFormEntity(mParams, HTTP.UTF_8));
        	httpResponse = client.execute(httpRequest); 
     
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
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
            	html += "HTTP POST FAIL\n";
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
        
        int a = html.indexOf("src");
        int b = html.indexOf("name");
        if(b == -1)
        {
        	valid = false;
        	return;
        }
        PHPSESSID = html.substring(a+5, b-2);
        sessionID = html.substring(a+31, b-10);
        return;
	}
	
	public String getCourseListUrl()
	{
		if(valid == true)
			return "http://ecourse.elearning.ccu.edu.tw/php/Courses_Admin/"+PHPSESSID;
		else
			return null;
	}
	
	public boolean isDone()
	{
		return done;
	}
	
	public String getSessionID()
	{
		return sessionID;
	}
}
