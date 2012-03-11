package cn.edu.nju;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import cn.edu.nju.MyLocation.LocationResult;

public class PostArticle extends Activity {
	private static final int PICK_PICTURE = 10;
	private static final int TAKE_PICTURE = 11;
	private static final int PICK_FILE = 12;
	private static final int EASY_DRAW = 17;
	private static final String TAG = "PostArticle";
	private static final int UPLD_SUCS = 1000;
	private static final int UPLD_FAIL = 1001;
	private static final int UPLD_ERROR = 1002;
	private static final int LOC_SUCS = 1007;
	private static final int LOC_FAIL = 1008;
	private static final String EXT_PATH = Environment
			.getExternalStorageDirectory() + "/LilyClient/";

	private static HashMap<Integer, String> EMOLIST = new HashMap<Integer, String>();
	static {
		int[] emoDrawable = new int[] { R.drawable.emo19, R.drawable.emo20,
				R.drawable.emo21, R.drawable.emo26, R.drawable.emo27,
				R.drawable.emo32, R.drawable.emo18, R.drawable.emo11,
				R.drawable.emo10, R.drawable.emo15, R.drawable.emo14,
				R.drawable.emo13, R.drawable.emo12, R.drawable.emo9,
				R.drawable.emo0, R.drawable.emo2, R.drawable.emo3,
				R.drawable.emo6, R.drawable.emo7, R.drawable.emo16,
				R.drawable.emo25, R.drawable.emo29, R.drawable.emo34,
				R.drawable.emo36, R.drawable.emo39, R.drawable.emo4,
				R.drawable.emo40, R.drawable.emo41, R.drawable.emo42,
				R.drawable.emo43, R.drawable.emo44, R.drawable.emo47,
				R.drawable.emo49, R.drawable.emo50, R.drawable.emo51,
				R.drawable.emo52, R.drawable.emo53, R.drawable.emo54 };
		String[] emoString = new String[] { "[:T]", "[;P]", "[;-D]", "[:!]",
				"[:L]", "[:?]", "[:Q]", "[:@]", "[:-|]", "[:(]", "[:)]",
				"[:D]", "[:P]", "[:'(]", "[:O]", "[:s]", "[:|]", "[:$]",
				"[:X]", "[:U]", "[:K]", "[:C-]", "[;X]", "[:H]", "[;bye]",
				"[;cool]", "[:-b]", "[:-8]", "[;PT]", "[;-C]", "[:hx]", "[;K]",
				"[:E]", "[:-(]", "[;hx]", "[:B]", "[:-v]", "[;xx]" };
		for (int i = 0; i < emoDrawable.length; i++) {
			EMOLIST.put(emoDrawable[i], emoString[i]);
		}
	}

	private Intent intent;
	private String cookie;
	private String board;

	private EditText titleEt;
	private EditText textEt;
	private CheckBox userSigCb;
	private ImageButton moreOptionIb;
	private Button submit;
	private LilyHttpPost post;

	private ProgressDialog pd = null;
	private Handler mHandler;

	// 此处应该使用策略模式
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post);

		post = new LilyHttpPost();
		post.setUri("post.php");
		titleEt = (EditText) findViewById(R.id.editText1);
		textEt = (EditText) findViewById(R.id.editText2);
		userSigCb = (CheckBox) findViewById(R.id.checkBox1);
		moreOptionIb = (ImageButton) findViewById(R.id.imageButton1);
		submit = (Button) findViewById(R.id.button1);

		intent = getIntent();
		Bundle bundle = intent.getExtras();
		this.cookie = LilyUtil.bundle.getString("cookie");
		if (cookie == null || cookie.length() < 5) {
			Toast.makeText(this, "对不起,您还没有登陆！", Toast.LENGTH_SHORT).show();
			this.finish();
		}
		this.board = bundle.getString("board");
		this.setTitle("发表在: " + board);

		String title = bundle.getString("title");
		String text = bundle.getString("text");
		String file = bundle.getString("file");
		if (title != null)
			titleEt.setText("Re:" + title);
		if (text != null)
			textEt.setText(text);
		if (file != null) {
			post.setUri("postAfter.php");
			post.addParam("file", file);
		}

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String text = "";
				switch (msg.what) {
				case LOC_SUCS:
					text = "获取位置信息成功";
					textEt.append("\n" + getGoogleMap((Location) msg.obj)
							+ "\n");
					break;
				case LOC_FAIL:
					text = "获取位置信息失败";
					break;
				case UPLD_SUCS:
					text = "上传成功";
					textEt.append("\n" + msg.obj.toString() + "\n");
					break;
				case UPLD_FAIL:
					text = "上传失败";
					break;
				case UPLD_ERROR:
					text = "网络故障";
					break;
				}
				pd.dismiss();
				Toast.makeText(getApplicationContext(), text,
						Toast.LENGTH_SHORT).show();
			}
		};

		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (titleEt.getText().toString().length() == 0)
					Toast.makeText(getApplicationContext(), "标题不能为空",
							Toast.LENGTH_SHORT).show();
				else {
					String title = titleEt.getText().toString();
					String text = textEt.getText().toString();
					if (PostArticle.this.userSigCb.isChecked()) {
						text += "\n----\n" + LilyUtil.bundle.getString("sig") + "\n";
					}

					post.addParam("cookie", cookie);
					post.addParam("board", board);
					post.addParam("title", title);
					post.addParam("text", text);
					String result = post.execute();
					if (result.equals("success")) {
						Toast.makeText(getApplicationContext(), "发表成功",
								Toast.LENGTH_SHORT).show();
						PostArticle.this.finish();
					} else
						Toast.makeText(getApplicationContext(), "发表失败",
								Toast.LENGTH_SHORT).show();
				}

			}
		});
		moreOptionIb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDialog(0);
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return new AlertDialog.Builder(PostArticle.this)
				.setIcon(android.R.drawable.ic_menu_add)
				.setTitle("附加")
				.setItems(R.array.attach,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent;
								switch (which) {
								case 0:
									intent = new Intent(
											Intent.ACTION_GET_CONTENT);
									intent.setType("image/*");
									startActivityForResult(intent, PICK_PICTURE);
									break;
								case 1:
									intent = new Intent(
											MediaStore.ACTION_IMAGE_CAPTURE);
									intent.putExtra(
											MediaStore.EXTRA_OUTPUT,
											Uri.fromFile(new File(EXT_PATH
													+ "temp.jpg")));
									intent.putExtra(
											MediaStore.EXTRA_VIDEO_QUALITY, 1);
									startActivityForResult(intent, TAKE_PICTURE);
									break;
								case 2:
									intent = new Intent(
											Intent.ACTION_GET_CONTENT);
									intent.setType("*/*");
									intent.addCategory(Intent.CATEGORY_OPENABLE);
									startActivityForResult(intent, PICK_FILE);
									break;
								case 3:
									AlertDialog aDialog = new AlertDialog.Builder(
											PostArticle.this).create();
									aDialog.setView(getTableLayout(EMOLIST,
											aDialog));
									aDialog.show();
									break;
								case 4:
									break;
								case 5:
									final View dlgLayout = LayoutInflater.from(
											PostArticle.this).inflate(
											R.layout.artfont, null);
									new AlertDialog.Builder(PostArticle.this)
											.setView(dlgLayout)
											.setPositiveButton(
													"确认",
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface arg0,
																int arg1) {
															Spinner spinner = (Spinner) dlgLayout
																	.findViewById(R.id.spinner1);
															EditText editText = (EditText) dlgLayout
																	.findViewById(R.id.editText1);
															int index = spinner
																	.getSelectedItemPosition();
															String color = (String) getResources()
																	.getTextArray(
																			R.array.colorref)[index];
															String text = editText
																	.getText()
																	.toString();
															PostArticle.this.textEt
																	.append("[color="
																			+ color
																			+ "]"
																			+ text
																			+ "[/color]");
														}
													})
											.setNegativeButton(
													"取消",
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {
														}
													}).create().show();
									break;
								case 6:
									addLocation();
									break;
								case 7:
									intent = new Intent(PostArticle.this,
											EasyDraw.class);
									startActivityForResult(intent, EASY_DRAW);
									break;

								}
							}
						}).create();
	}

	private String getGoogleMap(Location location) {
		return "http://maps.googleapis.com/maps/api/staticmap?center="
				+ String.valueOf(location.getLatitude()) + "%2C"
				+ String.valueOf(location.getLongitude())
				+ "&zoom=15&size=500x150&maptype=roadmap&markers="
				+ String.valueOf(location.getLatitude()) + "%2C"
				+ String.valueOf(location.getLongitude())
				+ "&language=zh_CN&sensor=false&format=.jpg";
	}

	private View getTableLayout(final HashMap<Integer, String> map,
			final AlertDialog dialog) {
		final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
		final int FP = ViewGroup.LayoutParams.WRAP_CONTENT;

		TableLayout layout = new TableLayout(this);
		layout.setStretchAllColumns(true);
		final int COLS = 8;
		final int padding = 4;
		final Set<Integer> keySet = map.keySet();

		final int rows = keySet.size() / COLS + 1;
		Iterator<Integer> iterator = keySet.iterator();
		for (int i = 0; i < rows; i++) {
			TableRow row = new TableRow(this);
			for (int j = 0; iterator.hasNext() && j < COLS; j++) {
				ImageButton iv = new ImageButton(this);
				iv.setBackgroundColor(android.R.color.transparent);
				final Integer key = iterator.next();
				iv.setImageDrawable(getResources().getDrawable(key));
				iv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						PostArticle.this.textEt.append((String) map.get(key));
						dialog.hide();
					}
				});

				row.addView(iv);
				iv.setPadding(padding, padding, padding, padding);
			}
			layout.addView(row, new TableLayout.LayoutParams(WC, WC));
		}
		return layout;
	}

	private void addLocation() {
		MyLocation myLocation = new MyLocation();
		LocationResult locationResult;
		myLocation = new MyLocation();

		locationResult = new LocationResult() {
			@Override
			public void gotLocation(final Location location) {
				if (location != null) {
					Message msg = new Message();
					msg.what = LOC_SUCS;
					msg.obj = location;
					mHandler.sendMessage(msg);
				} else {
					mHandler.sendEmptyMessage(LOC_FAIL);
				}
			}
		};
		myLocation.getLocation(this, locationResult);
		this.pd = ProgressDialog.show(this, "", "正在获取您的位置");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		if (resultCode == Activity.RESULT_OK)
			switch (requestCode) {
			case PICK_PICTURE:
				uploadFile(new File(getRealPath(data.getData())));
				break;
			case TAKE_PICTURE:
				try {
					BitmapFactory.Options opt = new BitmapFactory.Options();
					opt.inSampleSize = 3;
					Bitmap bm = BitmapFactory.decodeFile(EXT_PATH + "temp.jpg",
							opt);
					File file = new File(EXT_PATH + board + "-"
							+ String.valueOf(System.currentTimeMillis())
							+ ".jpg");
					bm.compress(Bitmap.CompressFormat.JPEG, 70,
							new FileOutputStream(file));
					bm.recycle();
					uploadFile(file);
				} catch (Exception e) {
					Log.i(TAG, e.toString());
				}
				break;
			case PICK_FILE:
				try {
					uploadFile(new File(new URI(data.getData().toString())));
				} catch (Exception e) {
				}
				break;
			case EASY_DRAW:
				String path = data.getStringExtra("path");
				if (path != null) {
					uploadFile(new File(path));
				}
				break;
			}
	}

	private void uploadFile(File file) {
		this.pd = ProgressDialog.show(this, "", "正在上传数据");

		new DowloadTask(file).start();
	}

	public class DowloadTask extends Thread {
		private File mFile;

		@Override
		public void run() {
			LilyPostFile postFile = new LilyPostFile("uploadFile.php", mFile,
					board, cookie);
			String result = postFile.execute();
			if (result == null)
				mHandler.sendEmptyMessage(UPLD_ERROR);
			if (result == "fail")
				mHandler.sendEmptyMessage(UPLD_FAIL);

			else {
				Message msg = new Message();
				msg.what = UPLD_SUCS;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		}

		public DowloadTask(File file) {
			mFile = file;
		}
	}

	private String getRealPath(Uri uri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
		int actual_image_column_index = actualimagecursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		actualimagecursor.moveToFirst();
		String img_path = actualimagecursor
				.getString(actual_image_column_index);
		return img_path;
	}
}
