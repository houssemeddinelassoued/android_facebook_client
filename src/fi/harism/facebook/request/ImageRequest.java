package fi.harism.facebook.request;

import java.io.ByteArrayOutputStream;
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
	 * @param url
	 *            Image URL.
	 * @param observer
	 *            ImageRequest observer.
	 */
	public ImageRequest(Activity activity, String url,
			ImageRequest.Observer observer) {
		super(activity);
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
		//DataCache dataCache = activity.getGlobalState().getDataCache();
		// Always check dataCache for cached image.
		//if (dataCache.containsKey(url)) {
		//	byte bitmapData[] = dataCache.getData(url);
		//	bitmap = BitmapFactory.decodeByteArray(bitmapData, 0,
		//			bitmapData.length);
		//}
		// No cached data found.
		//else {
			try {
				// Open InputStream for given url.
				URL u = new URL(url);
				InputStream is = u.openStream();
				ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();

				// Read actual data from InputStream.
				int readLength;
				byte buffer[] = new byte[1024];
				while ((readLength = is.read(buffer)) != -1) {
					imageBuffer.write(buffer, 0, readLength);
				}

				buffer = imageBuffer.toByteArray();
				bitmap = BitmapFactory
						.decodeByteArray(buffer, 0, buffer.length);
			} catch (Exception ex) {
				observer.onError(ex);
				throw ex;
			}
		//}
	}

	@Override
	public void runOnUiThread() throws Exception {
		observer.onComplete(this);
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
