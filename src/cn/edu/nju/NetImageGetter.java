package cn.edu.nju;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html.ImageGetter;
import android.util.Log;

public class NetImageGetter implements ImageGetter {
	Context mContext;
	final Handler mHandler;
	final int DISPLAY_WIDTH;

	static final Bitmap defaultBm;
	static {
		int[] colors = new int[400];
		Arrays.fill(colors, 0xbb003300);
		defaultBm = Bitmap.createBitmap(colors, 20, 20, Config.ARGB_8888);
	};
	
	
	public NetImageGetter(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		DISPLAY_WIDTH = mContext.getResources().getDisplayMetrics().widthPixels;
	}

	@Override
	public Drawable getDrawable(String source) {
		Drawable drawable = null;
		if(!LilyUtil.bundle.getBoolean("showpic")) return null;
		String filePath = getFileLocalPath(source);

		Boolean isOk = false;
		File file = new File(filePath);
		File fileLock = new File(getFileLockPath(source));
		Bitmap bm = null;
		if (file.exists() && !fileLock.exists()) {
			try {
				bm = BitmapFactory.decodeFile(filePath);
				isOk = true;
			} catch (Exception e) {
			}

		}
		if (!isOk) {
			try {
				bm = defaultBm;
				new Thread(new downloadImage(source)).start();
			} catch (Exception e) {
				return null;
			}
		}

		drawable = new BitmapDrawable(bm);
		int width = bm.getWidth();
		int height = bm.getHeight();
		if (width > DISPLAY_WIDTH) {
			double ratio = width * 1.0 / DISPLAY_WIDTH;
			width = DISPLAY_WIDTH;
			height = (int) (height / ratio);
		}
		drawable.setBounds(0, 0, width, height);
		return drawable;
	}

	class downloadImage implements Runnable {
		private String mUrl;

		public downloadImage(String url) {
			mUrl = url;
		}

		public void run() {
			try {
				File file = new File(getFileLocalPath(mUrl));
				File fileLock = new File(getFileLockPath(mUrl));
				if (!fileLock.exists() && mHandler != null) {
					fileLock.createNewFile();
					Bitmap bm = BitmapFactory
							.decodeStream(new FlushedInputStream(new URL(mUrl)
									.openStream()));
					bm.compress(CompressFormat.PNG, 80, new FileOutputStream(
							file));
					fileLock.delete();
					Message msg = new Message();
					msg.what = 1;
					mHandler.sendMessage(msg);
				}
			} catch (Exception e) {
				Log.e("DOWNLOAD", e.toString());
			}
		}
	}

	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int bytes = read();
					if (bytes < 0) {
						break; // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}

	public static String getFileLocalPath(String source) {
		String[] strs = source.split("/");
		String path = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/LilyClient/";
		File fileDir = new File(path);
		if (!fileDir.exists()) {
			fileDir.mkdir();
		}
		String filePath = path + strs[strs.length - 2] + "-"
				+ strs[strs.length - 1];
		return filePath;
	}

	public static String getFileLockPath(String source) {
		return getFileLocalPath(source) + ".lck";
	}

}
