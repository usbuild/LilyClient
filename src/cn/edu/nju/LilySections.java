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

public class LilySections extends ListActivity {
	private ProgressDialog pd = null;
	private String result;
	private Handler mHandler;

	private LilyHttpGet httpGet = new LilyHttpGet();
	private List<Map<String, Object>> listItems = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle("分类版区");
		httpGet.setUri("getForums.php");
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
			Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show();
		else if (result.length() < 10)
			Toast.makeText(this, "获取信息失败", Toast.LENGTH_SHORT).show();
		else {

			try {
				JSONObject jObj = new JSONObject(result);
				JSONArray jArr = jObj.optJSONArray("items");
				listItems = LilyUtil.decodeJSONArray(jArr);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		setListAdapter(new SimpleAdapter(this, listItems,
				android.R.layout.simple_list_item_1, new String[] { "name" },
				new int[] { android.R.id.text1 }));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String section = listItems.get(position).get("sec").toString();
		Intent intent = new Intent(this, LilyBoards.class);
		Bundle bundle = new Bundle();
		bundle.putString("sec", section);
		bundle.putString("cookie", getIntent().getExtras().getString("cookie"));
		intent.putExtras(bundle);
		startActivity(intent);
	}

}
