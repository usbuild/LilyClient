package cn.edu.nju;

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.os.Handler;

public class LilyHttpGet {
	private DefaultHttpClient client;
	private HttpGet httpGet;
	private List<NameValuePair> params;
	private String url;
	private String result;
	public LilyHttpGet() {
		client = new DefaultHttpClient();
		params = new LinkedList<NameValuePair>();
		httpGet = new HttpGet();
	}
	public void setUri(String uri) {
		url = "http://usbuild.duapp.com/lily/" + uri + "?";
	}
	public void addParam(String name, String value) {
		Iterator<NameValuePair> pair = params.iterator();
		while(pair.hasNext()) {
			NameValuePair nv = pair.next();
			if(nv.getName().equals(name)) 
				pair.remove();
		}
		params.add(new BasicNameValuePair(name, value));
	}
	
	public String execute() {
		try {
			String paramString = URLEncodedUtils.format(params, "utf-8");
			httpGet.setURI(URI.create(url + paramString));
			HttpResponse response = client.execute(httpGet);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				result = EntityUtils.toString(response.getEntity(), "utf-8");
				return result;
			} else {
				return "";
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	public String getResult() {
		return result;
	}
	public void start(Handler params) {
		new DownloadTask().execute(params);
	}
	public class DownloadTask extends AsyncTask<Handler, Void, Object> {
		@Override
		protected Object doInBackground(Handler... params) {
			result = LilyHttpGet.this.execute();
			params[0].sendEmptyMessage(LilyUtil.DOWLOADED);
			return result;
		}
	}
}
