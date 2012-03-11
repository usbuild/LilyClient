package cn.edu.nju;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

public class LilyUtil {
	public static final int DOWLOADED = 3851;
	public static String STORE_PATH = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/LilyClient/";
	public static  ArrayList<Map<String, Object>> decodeJSONArray(JSONArray jArray) {
		ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Boolean b = true;
		for (int i = 0; i < jArray.length(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			JSONObject jObj = jArray.optJSONObject(i);
			Iterator<String> itr = jObj.keys();
			try {
				while (itr.hasNext()) {
					String key = itr.next();
					map.put(key, jObj.getString(key));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			list.add(map);
		}
		return list;
	}
	public static void setClickable(final TextView textView) {
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
					textView.getContext().startActivity(intent);

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

	public static Bundle bundle = new Bundle();
	
}
