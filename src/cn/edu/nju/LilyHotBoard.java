package cn.edu.nju;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class LilyHotBoard extends ListActivity {
	
	private ProgressDialog pd = null;
	private String result;
	private Handler mHandler;
	
	private LilyHttpGet httpGet;

	private List<Map<String, Object>> listItems;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		httpGet = new LilyHttpGet();
		httpGet.setUri("getHotBoard.php");
		setTitle("热门板块");
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
	}
	private void refresh() {
		if (result == null)
			Toast.makeText(this, "网络错误!", Toast.LENGTH_SHORT).show();
		else {
			try {
				JSONObject json = new JSONObject(result);
				JSONArray jsonArr = json.getJSONArray("hotBoard");
				listItems = LilyUtil.decodeJSONArray(jsonArr);
				setListAdapter(new SimpleAdapter(this,
						listItems, R.layout.hotbrdlst,
						new String[] { "board", "name", "manager", "on" },
						new int[] { R.id.textView1, R.id.textView2,
								R.id.textView3, R.id.textView4 }));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String board = listItems.get(position).get("board").toString();
		Intent intent = new Intent(this, LilyArticleList.class);
		Bundle bundle = new Bundle();
		bundle.putString("board", board);
		bundle.putString("cookie", getIntent().getExtras().getString("cookie"));
		intent.putExtras(bundle);
		startActivity(intent);
	}
}
