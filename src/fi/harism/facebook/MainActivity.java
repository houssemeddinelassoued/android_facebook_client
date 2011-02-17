package fi.harism.facebook;

import java.io.InputStream;
import java.net.URL;

import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

	MyRunnable runnable;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		runnable = new MyRunnable(this);

		new Thread() {
			@Override
			public void run() {
				try {
					Bundle b = new Bundle();
					b.putString("fields", "id,name,picture");
					b.putString("access_token",
							LoginActivity.facebook.getAccessToken());

					String res = LoginActivity.facebook.request("me", b);
					JSONObject jsonObj = new JSONObject(res);

					String name = jsonObj.getString("name");

					URL url = new URL(jsonObj.getString("picture"));
					InputStream is = url.openStream();
					Bitmap bitmap = BitmapFactory.decodeStream(is);

					Bitmap roundedBitmap = Bitmap.createBitmap(
							bitmap.getWidth(), bitmap.getHeight(),
							Config.ARGB_8888);
					Canvas canvas = new Canvas(roundedBitmap);

					final int color = 0xff424242;
					final Paint paint = new Paint();
					final Rect rect = new Rect(0, 0, bitmap.getWidth(),
							bitmap.getHeight());
					final RectF rectF = new RectF(rect);
					final float roundPx = 12;

					paint.setAntiAlias(true);
					canvas.drawARGB(0, 0, 0, 0);
					paint.setColor(color);
					canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

					paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
					canvas.drawBitmap(bitmap, rect, rect, paint);

					runnable.setNameBitmap(name, roundedBitmap);
					runOnUiThread(runnable);

				} catch (Exception ex) {
				}
			}
		}.start();
	}

	class MyRunnable implements Runnable {
		MainActivity mainActivity;
		String name;
		Bitmap bitmap;

		MyRunnable(MainActivity mainActivity) {
			this.mainActivity = mainActivity;
		}

		public void setNameBitmap(String name, Bitmap bitmap) {
			this.name = name;
			this.bitmap = bitmap;
		}

		@Override
		public void run() {
			TextView tv = (TextView) findViewById(R.id.main_user_name);
			tv.setText(name);
			ImageView iv = (ImageView) findViewById(R.id.main_user_image);
			iv.setImageBitmap(bitmap);
		}
	}

}
