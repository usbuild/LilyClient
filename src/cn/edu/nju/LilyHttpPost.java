package cn.edu.nju;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Xml.Encoding;

public class LilyHttpPost {
	private DefaultHttpClient client;
	private HttpPost httpPost;
	private List<NameValuePair> params;
	private String url;
	public LilyHttpPost() {
		client = new DefaultHttpClient();
		params = new LinkedList<NameValuePair>();
		httpPost = new HttpPost();
	}
	public void setUri(String uri) {
		url = "http://usbuild.duapp.com/lily/" + uri;
	}
	public void addParam(String name, String value) {
		params.add(new BasicNameValuePair(name, value));
	}
	public String execute() {
		try {
			httpPost.setURI(URI.create(url));
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = client.execute(httpPost);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(response.getEntity(), "utf-8");
				if(result.length() == 0)
					return "success";
			} else {
				return "";
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		return "fail";
	}
}
