package cn.edu.nju;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

public class LilyHotArticles extends ExpandableListActivity {

	private ProgressDialog pd = null;
	private String result;
	private Handler mHandler;

	private LilyHttpGet httpGet = new LilyHttpGet();
	private List<Map<String, Object>> listGroup = null;
	private List<List<Map<String, Object>>> listItems = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		httpGet.setUri("getHotArticles.php");
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
		listGroup = new ArrayList<Map<String, Object>>();
		listItems = new ArrayList<List<Map<String, Object>>>();
		if (result == null)
			Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show();
		else if (result.length() < 10)
			Toast.makeText(this, "获取信息失败", Toast.LENGTH_SHORT).show();
		else {
			try {
				JSONObject jObj = new JSONObject(result);
				this.setTitle(jObj.get("title").toString());
				JSONArray jArray = jObj.optJSONArray("items");
				for (int i = 0; i < jArray.length(); i++) {
					JSONObject jItem = jArray.getJSONObject(i);
					Map<String, Object> mapItem = new HashMap<String, Object>();
					mapItem.put("sec", jItem.getString("sec"));
					mapItem.put("name", jItem.getString("name"));
					listGroup.add(mapItem);

					JSONArray arrItem = jItem.optJSONArray("items");
					listItems.add(LilyUtil.decodeJSONArray(arrItem));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			setListAdapter(new SimpleExpandableListAdapter(this, listGroup,
					android.R.layout.simple_expandable_list_item_1,
					new String[] { "name" }, new int[] { android.R.id.text1 },
					listItems, android.R.layout.simple_expandable_list_item_2,
					new String[] { "title", "board" }, new int[] {
							android.R.id.text1, android.R.id.text2 }));
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		Map<String, Object> item = listItems.get(groupPosition).get(
				childPosition);
		String file = item.get("file").toString();
		String board = item.get("board").toString();
		Intent intent = new Intent(this, ViewArticle.class);
		Bundle bundle = new Bundle();
		bundle.putString("board", board);
		bundle.putString("file", file);
		bundle.putString("cookie", getIntent().getExtras().getString("cookie"));
		intent.putExtras(bundle);
		startActivity(intent);
		return super.onChildClick(parent, v, groupPosition, childPosition, id);
	}

}
