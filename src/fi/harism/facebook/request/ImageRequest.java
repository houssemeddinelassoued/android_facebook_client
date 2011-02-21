package fi.harism.facebook.request;

import java.io.InputStream;
import java.net.URL;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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

	/**
	 * Constructor for ImageRequest.
	 * 
	 * @param activity
	 *            Activity to which use for runOnUiThread.
	 * @param requestObserver
	 *            Observer for Request base class.
	 * @param url
	 *            Image URL.
	 * @param observer
	 *            ImageRequest observer.
	 */
	public ImageRequest(Activity activity, Request.Observer requestObserver,
			String url, ImageRequest.Observer observer) {
		super(activity, requestObserver);
		this.url = url;
		this.observer = observer;
		bitmap = null;
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
		try {
			URL u = new URL(url);
			InputStream is = u.openStream();
			bitmap = BitmapFactory.decodeStream(is);
		} catch (Exception ex) {
			observer.onError(ex);
			throw ex;
		}
	}

	@Override
	public void runOnUiThread() throws Exception {
		observer.onComplete(this);
	}

	/**
	 * ImageRequest observer.
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
