package cn.edu.nju;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;

public class EasyDraw extends Activity implements
		ColorPickerDialog.OnColorChangedListener {

	private MyView myView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myView = new MyView(this);
		setContentView(myView);
		this.setTitle("涂鸦板");

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(0xFFFF0000);
		mPaint.setColor(Color.BLACK);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(10);

	}

	private Paint mPaint;

	public void colorChanged(int color) {
		mPaint.setColor(color);
	}

	public class MyView extends View {
		private Bitmap mBitmap;
		private Canvas mCanvas;
		private Path mPath;

		public MyView(Context c) {
			super(c);
			mBitmap = Bitmap.createBitmap(
					c.getResources().getDisplayMetrics().widthPixels, c
							.getResources().getDisplayMetrics().heightPixels,
					Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mBitmap);
			mCanvas.drawColor(Color.WHITE);
			mPath = new Path();
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawColor(Color.WHITE);
			canvas.drawBitmap(mBitmap, 0, 0, new Paint());
			canvas.drawPath(mPath, mPaint);
		}

		private float mX, mY;
		private static final float TOUCH_TOLERANCE = 4;

		private void touch_start(float x, float y) {
			mPath.reset();
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
		}

		private void touch_move(float x, float y) {
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;
			}
		}

		private void touch_up() {
			mPath.lineTo(mX, mY);
			mCanvas.drawPath(mPath, mPaint);
			mPath.reset();
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				touch_start(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				touch_move(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				touch_up();
				invalidate();
				break;
			}
			return true;
		}
		public Bitmap getBitmap() {
			return mBitmap;
		}
	}

	private static final int COLOR_MENU_ID = Menu.FIRST;
	private static final int SIZE_MENU_ID = Menu.FIRST + 1;
	private static final int ERASE_MENU_ID = Menu.FIRST + 2;
	private static final int SAVE_MENU_ID = Menu.FIRST + 3;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, COLOR_MENU_ID, 0, "颜色");
		menu.add(0, SIZE_MENU_ID, 0, "粗细");
		menu.add(0, ERASE_MENU_ID, 0, "橡皮/画笔");
		menu.add(0, SAVE_MENU_ID, 0, "确认");
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mPaint.setAlpha(0xFF);

		switch (item.getItemId()) {
		case COLOR_MENU_ID:
			new ColorPickerDialog(this, this, mPaint.getColor()).show();
			return true;

		case SIZE_MENU_ID:
			int currentSize = (int) mPaint.getStrokeWidth();

			LinearLayout layout = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.setPadding(20, 20, 20, 20);
			final TextView tv = new TextView(this);
			tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.MATCH_PARENT));

			tv.setText("当前宽度:" + String.valueOf(currentSize));
			final SeekBar sb = new SeekBar(this);

			tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.MATCH_PARENT));
			sb.setMax(39);// Max Width
			sb.setProgress(currentSize - 1);
			sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onStopTrackingTouch(SeekBar arg0) {
				}

				@Override
				public void onStartTrackingTouch(SeekBar arg0) {
				}

				@Override
				public void onProgressChanged(SeekBar seekBar, int value,
						boolean fromUser) {
					tv.setText("当前宽度:" + String.valueOf(value + 1));
				}
			});
			layout.addView(tv);
			layout.addView(sb);

			new AlertDialog.Builder(this)
					.setTitle("笔尖粗细")
					.setView(layout)
					.setPositiveButton("確定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mPaint.setStrokeWidth(sb.getProgress() + 1);
								}
							}).setNegativeButton("取消", null).create().show();
			return true;
		case ERASE_MENU_ID:
			if(mPaint.getXfermode() == null)
				mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
			else
				mPaint.setXfermode(null);
			return true;
		case SAVE_MENU_ID:
			Bitmap bm = myView.getBitmap();
			String path = LilyUtil.STORE_PATH + String.valueOf(System.currentTimeMillis()) + ".jpg";
			try {
				bm.compress(Bitmap.CompressFormat.JPEG, 80, new FileOutputStream(new File(path)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			Intent intent = new Intent();
			intent.putExtra("path", path);
			this.setResult(RESULT_OK, intent);
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
