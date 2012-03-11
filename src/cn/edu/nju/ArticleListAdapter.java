package cn.edu.nju;

import java.io.File;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ArticleListAdapter extends BaseAdapter {

	private Context context;
	private List<Map<String, Object>> listItems;
	private LayoutInflater listContainer;
	private Handler mHandler;

	private Context getContext() {
		return this.context;
	}
	public final class ListItemView {
		public Button button1;
		public TextView textView1;
		public TextView textView2;
		public TextView textView3;
		public TextView textView4;
	}

	public ArticleListAdapter(Context context,
			List<Map<String, Object>> listItems, Handler handler) {
		super();
		this.context = context;
		listContainer = LayoutInflater.from(context);
		this.listItems = listItems;
		this.mHandler = handler;
	}

	@Override
	public int getCount() {
		return listItems.size();
	}

	@Override
	public Object getItem(int arg0) {
		return listItems.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}
	
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int selectID = position;
		ListItemView listItemView = null;
		if (convertView == null) {
			listItemView = new ListItemView();

			convertView = listContainer.inflate(R.layout.artlist, null);
			listItemView.button1 = (Button) convertView
					.findViewById(R.id.button1);
			listItemView.textView1 = (TextView) convertView
					.findViewById(R.id.textView1);
			listItemView.textView2 = (TextView) convertView
					.findViewById(R.id.textView2);
			listItemView.textView3 = (TextView) convertView
					.findViewById(R.id.textView3);
			listItemView.textView4 = (TextView) convertView
					.findViewById(R.id.textView4);
			convertView.setTag(listItemView);
		}
			listItemView = (ListItemView) convertView.getTag();
			listItemView.textView1.setText((CharSequence) listItems.get(
					position).get("count"));
			
			listItemView.textView2.setText(Html.fromHtml((String)listItems.get(
					position).get("text"), new NetImageGetter(context, mHandler), null));
			listItemView.textView3.setText((CharSequence) listItems.get(
					position).get("time"));
			listItemView.textView4.setText(Html.fromHtml((String) listItems.get(
					position).get("author") +"(<font color=\"green\">"+ (String) listItems.get(position).get("name") + "</font>)"));

			final Map item = listItems.get(position);
			listItemView.textView4.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Message msg = new Message();
					msg.what = 3;
					msg.obj = item.get("author");
					mHandler.sendMessage(msg);
				}
			});
			
			listItemView.textView2.setMovementMethod(LinkMovementMethod
					.getInstance());

			Spannable sp = (Spannable) listItemView.textView2.getText();
			ImageSpan[] images = sp.getSpans(0, listItemView.textView2
					.getText().length(), ImageSpan.class);

			
			for (ImageSpan span : images) {
				final String image_src = span.getSource();
				final int start = sp.getSpanStart(span);
				final int end = sp.getSpanEnd(span);

				ClickableSpan click_span = new ClickableSpan() {
					@Override
					public void onClick(View widget) {
						String[] strs = image_src.split("/");
						String filePath = Environment
								.getExternalStorageDirectory()
								.getAbsolutePath()
								+ "/LilyClient/"
								+ strs[strs.length - 2]
								+ "-"
								+ strs[strs.length - 1];

						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_VIEW);
						intent.setType("image/*"); 
						intent.setDataAndType(Uri.fromFile(new File(filePath)), "image/*");
						getContext().startActivity(intent);
						
					}
				};
				ClickableSpan[] click_spans = sp.getSpans(start, end,
						ClickableSpan.class);
				if (click_spans.length != 0) {
					for (ClickableSpan c_span : click_spans) {
						sp.removeSpan(c_span);
					}
				}
				sp.setSpan(click_span, start, end,
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			listItemView.button1.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Message msg = new Message();
					msg.what = 2;
					msg.obj = item.get("author");
					mHandler.sendMessage(msg);
				}
			});
		return convertView;
	}
}
