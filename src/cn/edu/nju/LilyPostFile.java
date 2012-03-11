package cn.edu.nju;

import java.io.File;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class LilyPostFile {
	private DefaultHttpClient client;
	private HttpPost httpPost;
	private String url;
	private File mFile;

	public LilyPostFile(String uri, File file, String board, String cookie) {
		client = new DefaultHttpClient();
		httpPost = new HttpPost();
		url = "http://usbuild.sinaapp.com/lily/" + uri + "?";
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("board", board));
		params.add(new BasicNameValuePair("cookie", cookie));
		String paramString = URLEncodedUtils.format(params, "utf-8");
		httpPost.setURI(URI.create(url + paramString));
		mFile = file;
	}

	public String execute() {
		try {
			
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			entity.addPart("file", new FileBody(mFile));
			httpPost.setEntity(entity);
			HttpResponse response = client.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(response.getEntity(),
						"utf-8");
				if (result.indexOf("http") > -1)
					return result;
				else
					return "fail";
			} else {
				return "";
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
