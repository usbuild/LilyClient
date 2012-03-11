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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ViewArticle extends Activity {

	private ListView artView;
	
	private ProgressDialog pd = null;
	
	private Intent intent;
	private String cookie;
	private LilyHttpGet httpGet;
	private Handler mHandler;
	private String result;
	private String title;
	private Integer mStart = 0;

	public Handler getHandler() {
		return mHandler;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.article);
		artView = (ListView) super.findViewById(R.id.viewArticle);
		intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		this.cookie = bundle.getString("cookie");
		httpGet = new LilyHttpGet();
		httpGet.addParam("board", bundle.getString("board"));
		httpGet.addParam("file", bundle.getString("file"));
		httpGet.setUri("getArticle.php");
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Bundle bundle = new Bundle(getIntent().getExtras());
				Intent intent;
				switch (msg.what) {
				case 1:
					((BaseAdapter) artView.getAdapter()).notifyDataSetChanged();
					break;
				case 2:
					intent = new Intent(ViewArticle.this, PostArticle.class);
					bundle.putString("text", "\n[在[uid]" + msg.obj.toString() + "[/uid]的大作中提到]");
					bundle.putString("title", title);
					intent.putExtras(bundle);
					startActivity(intent);
					refresh();
					break;
				case 3: 
					intent = new Intent(ViewArticle.this, LilyPersonal.class);
					bundle.putString("id", msg.obj.toString());
					intent.putExtras(bundle);
					startActivity(intent);
					break;
				case LilyUtil.DOWLOADED:
					pd.dismiss();
					result = httpGet.getResult();
					refresh();
					break;
				}

			}
		};
		setContent(0);
	}

	private void refresh() {
		if (mStart < 0) {
			Toast.makeText(this, "已经是第一页", Toast.LENGTH_SHORT).show();
			mStart = 0;
			return;
		}
		if (result == null) {
			Toast.makeText(this, "网络故障", Toast.LENGTH_SHORT).show();
			return;
		}
		if (result.length() > 5) {
			try {
				JSONObject jsonObj = new JSONObject(result);
				this.setTitle(jsonObj.getString("title"));
				JSONArray jArray = jsonObj.getJSONArray("items");
				
				title = jsonObj.getString("title");
				List<Map<String, Object>> postList = LilyUtil
						.decodeJSONArray(jArray);
				if(postList.size() == 1 && mStart != 0)
				{
					Toast.makeText(this, "没有更多内容", Toast.LENGTH_SHORT).show();
					mStart -=30;
					return;
				}
				if (mStart != 0)
					postList.remove(0);
				artView.setAdapter(new ArticleListAdapter(this, postList,
						mHandler));
			} catch (Exception ex) {
				ex.printStackTrace();
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
		menu.add(0, POST_MENU_ID, 0, "回复");
		menu.add(0, REF_MENU_ID, 0, "刷新");
		return true;
	}

	public void setContent(Integer start) {
		if(start != null) {
			if(start < 0) {
				Toast.makeText(this, "已经是第一页", Toast.LENGTH_SHORT).show();
				return;
			}
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
			setContent(mStart - 30);
			break;
		case NEXT_MENU_ID:
			setContent(mStart + 30);
			break;
		case POST_MENU_ID:
			Intent intent = new Intent(this, PostArticle.class);
			Bundle bundle = getIntent().getExtras();
			bundle.putString("title", title);
			intent.putExtras(getIntent().getExtras());
			intent.putExtras(bundle);
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
