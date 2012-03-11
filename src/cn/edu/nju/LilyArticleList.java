package cn.edu.nju;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class LilyArticleList extends Activity {
	private ListView listView;
	private String board;
	private String cookie;
	private Integer mStart;
	private int mPrev;
	private int mNext;
	private Handler mHandler;
	private String result;
	private ProgressDialog pd = null;
	List<Map<String, Object>> postList;
	LilyHttpGet httpGet = new LilyHttpGet();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listView = new ListView(this);
		Bundle bundle = getIntent().getExtras();
		board = bundle.getString("board");
		cookie = bundle.getString("cookie");

		httpGet.setUri("getPosts.php");
		httpGet.addParam("board", board);
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

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int postion, long arg3) {
				Map<String, Object> item = postList.get(postion);
				Intent intent = new Intent(LilyArticleList.this,
						ViewArticle.class);
				Bundle bundle = new Bundle(getIntent().getExtras());
				bundle.putString("file", item.get("file").toString());
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});

		setContentView(listView);
	}

	private void refresh() {
		Integer start = mStart;
		if (start != null) {
			Log.i("start", mStart.toString());
			if (start < -29) {
				Toast.makeText(this, "已是第一页", Toast.LENGTH_SHORT).show();
				start = 0;
			}
		}
		if (result == null)
			Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show();
		else if (result.length() < 10)
			Toast.makeText(this, "获取信息失败", Toast.LENGTH_SHORT).show();
		else {
			try {
				JSONObject jsonObj = new JSONObject(result);
				this.setTitle(jsonObj.getString("brd"));
				
				mPrev = Integer.parseInt(jsonObj.getString("prev"));
				mNext = Integer.parseInt(jsonObj.getString("next"));

				JSONArray jsonArr = jsonObj.getJSONArray("items");
				postList = LilyUtil.decodeJSONArray(jsonArr);
				if (postList.size() == 0)
					Toast.makeText(this, "没有更多文章", Toast.LENGTH_SHORT).show();

				listView.setAdapter(new SimpleAdapter(this, postList,
						R.layout.articlelist, new String[] { "title", "reply",
								"time", "author" }, new int[] { R.id.artTitle,
								R.id.artReply, R.id.artBoard, R.id.artAuthor }));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static final int PREV_MENU_ID = Menu.FIRST;
	private static final int NEXT_MENU_ID = Menu.FIRST + 1;
	private static final int POST_MENU_ID = Menu.FIRST + 2;
	private static final int REF_MENU_ID = Menu.FIRST + 3;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, PREV_MENU_ID, 0, "上一页");
		menu.add(0, NEXT_MENU_ID, 0, "下一页");
		menu.add(0, POST_MENU_ID, 0, "发表");
		menu.add(0, REF_MENU_ID, 0, "刷新");
		return true;
	}

	private void setContent(Integer start) {
		if(start != null) {
			mStart = start;
			httpGet.addParam("start", mStart.toString());
		}
		this.pd = ProgressDialog.show(this, "", "正在下载数据");
		httpGet.start(mHandler);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case PREV_MENU_ID:
			if (mPrev == -1) {
				Toast.makeText(this, "已经是第一页", Toast.LENGTH_SHORT).show();
				break;
			}
			setContent(mPrev);
			break;
		case NEXT_MENU_ID:
			if (mNext == -1) {
				Toast.makeText(this, "已经是最后一页", Toast.LENGTH_SHORT).show();
				break;
			}
			setContent(mNext);
			break;
		case POST_MENU_ID:
			Intent intent = new Intent(this, PostArticle.class);
			intent.putExtras(getIntent().getExtras());
			startActivityForResult(intent, 0);
			break;
		case REF_MENU_ID:
			setContent(mStart);
			break;
		}
		return true;
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		setContent(mStart);
	}
}
