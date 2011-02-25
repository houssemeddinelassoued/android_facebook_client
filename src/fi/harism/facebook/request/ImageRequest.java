package fi.harism.facebook.request;

import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import fi.harism.facebook.BaseActivity;
import fi.harism.facebook.util.BitmapCache;

/**
 * ImageRequest class for loading images asynchronously.
 * 
 * @author harism
 */
public class ImageRequest extends Request {

	// Image URL.
	private String url;
	// Observer for ImageRequest.
	private ImageRequest.Observer observer;
	// Bitmap we loaded.
	private Bitmap bitmap;
	// Flag whether Bitmap should be cached.
	private boolean cacheBitmap;
	// Caller activity.
	private BaseActivity activity;

	/**
	 * Constructor for ImageRequest.
	 * 
	 * @param activity
	 *            Activity to which use for runOnUiThread.
	 * @param url
	 *            Image URL.
	 * @param observer
	 *            ImageRequest observer.
	 */
	public ImageRequest(BaseActivity activity, String url,
			ImageRequest.Observer observer) {
		super(activity);
		this.activity = activity;
		this.url = url;
		this.observer = observer;
		bitmap = null;
		cacheBitmap = false;
	}

	/**
	 * Once ImageRequest is completed successfully this method returns loaded
	 * Bitmap.
	 * 
	 * @return Loaded Bitmap or null on error.
	 */
	public Bitmap getBitmap() {
		return bitmap;
	}

	@Override
	public void runOnThread() throws Exception {
		BitmapCache bitmapCache = activity.getGlobalState().getBitmapCache();
		if (bitmapCache.hasBitmap(url)) {
			bitmap = bitmapCache.getBitmap(url);
		} else {
			try {
				URL u = new URL(url);
				InputStream is = u.openStream();
				bitmap = BitmapFactory.decodeStream(is);
				if (cacheBitmap) {
					bitmapCache.setBitmap(url, bitmap);
				}
			} catch (Exception ex) {
				observer.onError(ex);
				throw ex;
			}
		}
	}

	@Override
	public void runOnUiThread() throws Exception {
		observer.onComplete(this);
	}

	public void setCacheBitmap(boolean cacheBitmap) {
		this.cacheBitmap = cacheBitmap;
	}

	/**
	 * ImageRequest observer interface.
	 */
	public interface Observer {
		/**
		 * Called once ImageRequest is done successfully.
		 * 
		 * @param imageRequest
		 *            ImageRequest object being completed.
		 */
		public void onComplete(ImageRequest imageRequest);

		/**
		 * Called if ImageRequest failed.
		 * 
		 * @param ex
		 *            Exception explaining the cause for failure.
		 */
		public void onError(Exception ex);
	}
}
