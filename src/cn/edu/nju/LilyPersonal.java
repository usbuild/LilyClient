package cn.edu.nju;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LilyPersonal extends Activity {

	private LilyHttpGet httpGet;
	private TextView usernameTv;
	private TextView genderTv;
	private TextView constellaTv;
	private TextView upCountsTv;
	private TextView articlesTv;
	private TextView expTv;
	private TextView appTv;
	private TextView lifeTv;
	private TextView lastTv;
	private TextView lastIpTv;
	private TextView jobTv;
	private TextView statusTv;
	private TextView sigTv;
	private JSONObject jsonObj;

	private ProgressDialog pd = null;
	private String result;
	private Handler mHandler;

	private void setClickable(TextView textView) {
		textView.setMovementMethod(LinkMovementMethod.getInstance());
		Spannable sp = (Spannable) textView.getText();
		ImageSpan[] images = sp.getSpans(0, textView.getText().length(),
				ImageSpan.class);

		for (ImageSpan span : images) {
			final String image_src = span.getSource();
			final int start = sp.getSpanStart(span);
			final int end = sp.getSpanEnd(span);

			ClickableSpan click_span = new ClickableSpan() {
				@Override
				public void onClick(View widget) {
					String[] strs = image_src.split("/");
					String filePath = Environment.getExternalStorageDirectory()
							.getAbsolutePath()
							+ "/LilyClient/"
							+ strs[strs.length - 2]
							+ "-"
							+ strs[strs.length - 1];

					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setType("image/*");
					intent.setDataAndType(Uri.fromFile(new File(filePath)),
							"image/*");
					LilyPersonal.this.startActivity(intent);

				}
			};
			ClickableSpan[] click_spans = sp.getSpans(start, end,
					ClickableSpan.class);
			if (click_spans.length != 0) {
				for (ClickableSpan c_span : click_spans) {
					sp.removeSpan(c_span);
				}
			}
			sp.setSpan(click_span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personalinfo);

		usernameTv = (TextView) super.findViewById(R.id.textView2);
		genderTv = (TextView) super.findViewById(R.id.textView4);
		constellaTv = (TextView) super.findViewById(R.id.textView6);
		upCountsTv = (TextView) super.findViewById(R.id.textView8);
		articlesTv = (TextView) super.findViewById(R.id.textView10);
		expTv = (TextView) super.findViewById(R.id.textView12);
		appTv = (TextView) super.findViewById(R.id.textView14);
		lifeTv = (TextView) super.findViewById(R.id.textView16);
		lastTv = (TextView) super.findViewById(R.id.textView18);
		lastIpTv = (TextView) super.findViewById(R.id.textView20);
		jobTv = (TextView) super.findViewById(R.id.textView22);
		statusTv = (TextView) super.findViewById(R.id.textView24);
		sigTv = (TextView) super.findViewById(R.id.textView26);
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					try {
						sigTv.setText(Html.fromHtml(
								jsonObj.getString("sig"),
								new NetImageGetter(LilyPersonal.this, mHandler),
								null));
						setClickable(sigTv);

					} catch (JSONException e) {
						e.printStackTrace();
					}
					break;
				case LilyUtil.DOWLOADED:
					pd.dismiss();
					result = httpGet.getResult();
					refresh();
					break;
				}
			}
		};

		httpGet = new LilyHttpGet();
		httpGet.setUri("getPerson.php");
		httpGet.addParam("username", getIntent().getExtras().getString("id"));
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
				jsonObj = new JSONObject(result);
				usernameTv.setText(Html.fromHtml(jsonObj.getString("id")
						+ "(<font color=\"green\">" + jsonObj.getString("name")
						+ "</font>)"));
				String gender = jsonObj.getString("gender");
				if (gender.equals("male"))
					genderTv.setText("男生");
				else if (gender.equals("female"))
					genderTv.setText("女生");
				else
					genderTv.setText("未知");

				String constell = jsonObj.getString("constellation");
				if (constell.length() > 2)
					constellaTv.setText(constell);
				else
					constellaTv.setText("未知");
				upCountsTv.setText(jsonObj.getString("upCounts"));
				articlesTv.setText(jsonObj.getString("articles"));
				expTv.setText(jsonObj.getString("exp"));
				appTv.setText(jsonObj.getString("appearance"));
				lifeTv.setText(jsonObj.getString("life"));
				lastTv.setText(jsonObj.getString("lastDate"));
				lastIpTv.setText(jsonObj.getString("lastIp"));

				String manager = jsonObj.getString("manager");
				if (manager.length() > 2) {
					JSONArray jArr = jsonObj.optJSONArray("manager");
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < jArr.length(); i++) {
						sb.append(jArr.get(i) + " ");
					}
					jobTv.setText("现任 " + sb.toString() + "版版主");
				}

				else
					jobTv.setText("无");
				String status = jsonObj.getString("status");
				if (status.equals("online"))
					statusTv.setText("在线");
				else if (status.equals("offline"))
					statusTv.setText("不在线");
				else
					statusTv.setText("未知");
				String sig = jsonObj.getString("sig");
				if (sig.length() < 2) {
					sigTv.setText(Html
							.fromHtml("<font color=\"red\">无签名档</font>"));
					sigTv.setTextSize(20);
				} else {
					sigTv.setText(Html.fromHtml(jsonObj.getString("sig"),
							new NetImageGetter(this, mHandler), null));
					LilyUtil.setClickable(sigTv);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

}
