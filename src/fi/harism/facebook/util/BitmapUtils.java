package fi.harism.facebook.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

public class BitmapUtils {

	public static final Bitmap roundBitmap(Bitmap bitmap, float radiusPx) {
		Bitmap roundedBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(roundedBitmap);

		final int colorFg = 0xFFFFFFFF;
		final int colorBg = 0x00000000;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawColor(colorBg);
		paint.setColor(colorFg);
		canvas.drawRoundRect(rectF, radiusPx, radiusPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return roundedBitmap;
	}

	public static final Bitmap scaleToHeight(Bitmap bitmap, int height) {
		int width = height * bitmap.getWidth() / bitmap.getHeight();
		return Bitmap.createScaledBitmap(bitmap, width, height, true);
	}

}
