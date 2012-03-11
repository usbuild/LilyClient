package cn.edu.nju;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class LilyTopTen extends Activity {

	private Intent intent;
	private String cookie;
	private ListView listView1;
	private LilyHttpGet httpGet;
	private ProgressDialog pd = null;
	private String result;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		listView1 = (ListView) super.findViewById(R.id.listView1);
		intent = super.getIntent();
		Bundle bundle = intent.getExtras();
		cookie = bundle.getString("cookie");
		httpGet = new LilyHttpGet();
		httpGet.setUri("getTop10.php");
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case LilyUtil.DOWLOADED:
					pd.dismiss();
					result = httpGet.getResult();
					refresh();
					break;
				}
			}
		};

		this.pd = ProgressDialog.show(this, "", "正在下载数据");

		httpGet.start(mHandler);

		listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				@SuppressWarnings("unchecked")
				HashMap<String, String> map = (HashMap<String, String>) arg0
						.getAdapter().getItem(arg2);
				Intent intent = new Intent(getApplicationContext(),
						ViewArticle.class);
				Bundle bundle = new Bundle();
				bundle.putString("cookie", cookie);
				bundle.putString("board", map.get("board"));
				bundle.putString("file", map.get("file"));
				intent.putExtras(bundle);
				startActivity(intent);
				java.lang.System.gc();
			}
		});
	}

	private void refresh() {
		if (result == null)
			Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show();
		else {
			try {
				JSONObject jsonObj = new JSONObject(result);
				this.setTitle(jsonObj.getString("title"));
				JSONArray jsonArr = jsonObj.getJSONArray("items");
				List<Map<String, Object>> data = LilyUtil
						.decodeJSONArray(jsonArr);
				listView1
						.setAdapter(new SimpleAdapter(this, data,
								R.layout.articlelist, new String[] { "title",
										"reply", "board", "author" },
								new int[] { R.id.artTitle, R.id.artReply,
										R.id.artBoard, R.id.artAuthor }));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
