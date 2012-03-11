package cn.edu.nju;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LilyMain extends ListActivity {
	private Intent intent;
	private Bundle bundle;
	private String cookie;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1,
				getResources().getStringArray(R.array.mainlist)));
		intent = getIntent();
		bundle = intent.getExtras();
		cookie = bundle.getString("cookie");
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent newIntent = null;
		switch (position) {
		case 0:
			newIntent = new Intent(this, LilyTopTen.class);
			break;
		case 1:
			newIntent = new Intent(this, LilyHotBoard.class);
			break;
		case 2:
			newIntent = new Intent(this, LilyHotArticles.class);
			break;
		case 3:
			newIntent = new Intent(this, LilySections.class);
			break;
		case 4:
			newIntent = new Intent(this, LilyTopTen.class);
			break;
		case 5:
			newIntent = new Intent(this, LilySettings.class);
		default:
			break;
		}
		newIntent.putExtras(bundle);
		startActivity(newIntent);
	}
	
}
