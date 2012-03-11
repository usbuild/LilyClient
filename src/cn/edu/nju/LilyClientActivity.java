package cn.edu.nju;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class LilyClientActivity extends Activity {
	/** Called when the activity is first created. */
	private EditText editText1;
	private EditText editText2;
	private Button button1;
	private Button button2;
	private CheckBox checkBox1;
	private CheckBox checkBox2;
	private SharedPreferences preferences;
	private Intent intent;
	private Bundle bundle;
	private LilyHttpGet httpGet;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		File file = new File(LilyUtil.STORE_PATH);
		if(!file.exists()) {
			file.mkdir();
		}
		
		editText1 = (EditText) super.findViewById(R.id.editText1);
		editText2 = (EditText) super.findViewById(R.id.editText2);
		button1 = (Button) super.findViewById(R.id.button1);
		button2 = (Button) super.findViewById(R.id.button2);
		checkBox1 = (CheckBox) super.findViewById(R.id.checkBox1);
		checkBox2 = (CheckBox) super.findViewById(R.id.checkBox2);
		
		httpGet = new LilyHttpGet();
		
		preferences = super.getSharedPreferences("settings", MODE_PRIVATE);
		Boolean rememberme = preferences.getBoolean("rememberme", false);
		Boolean autologin = preferences.getBoolean("autologin", false);
		checkBox1.setChecked(rememberme);
		checkBox2.setChecked(autologin);
		
		LilyUtil.bundle.putString("sig", preferences.getString("sig", ""));
		LilyUtil.bundle.putBoolean("showpic", preferences.getBoolean("showpic", true));
		
		intent = new Intent(this, LilyMain.class);
		bundle = new Bundle();
		if (rememberme) {
			editText1.setText(preferences.getString("username", ""));
			editText2.setText(preferences.getString("password", ""));
			checkBox2.setEnabled(true);
			if (autologin)
				login();
		}

		checkBox1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				checkBox2.setEnabled(arg1);
			}
		});

		button1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				login();
			}
		});
		button2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				bundle.putString("cookie", null);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});

	}

	private void login() {
		httpGet.addParam("username", editText1.getText().toString());
		httpGet.addParam("password", editText2.getText().toString());
		httpGet.setUri("getCookie.php");

		String cookie = httpGet.execute();
		if (cookie == null) {
			Toast.makeText(this, "网络故障!", Toast.LENGTH_SHORT);
			return ;
		}
		if (cookie.length() > 5) {
			SharedPreferences.Editor editor = preferences.edit();

			editor.putBoolean("rememberme", checkBox1.isChecked());
			editor.putBoolean("autologin", checkBox2.isChecked());
			if (checkBox1.isChecked()) {
				editor.putString("username", editText1.getText().toString());
				editor.putString("password", editText2.getText().toString());
			}
			editor.commit();
			LilyUtil.bundle.putString("username", editText1.getText().toString());
			LilyUtil.bundle.putString("cookie", cookie);
			bundle.putString("cookie", cookie);
			intent.putExtras(bundle);
			Toast.makeText(this, "登录成功!", Toast.LENGTH_SHORT).show();
			startActivity(intent);
		} else {
			Toast.makeText(this, "登录失败!", Toast.LENGTH_SHORT).show();
		}

	}
}