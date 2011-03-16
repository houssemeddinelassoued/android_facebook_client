package fi.harism.facebook.dao;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import fi.harism.facebook.net.DataCache;
import fi.harism.facebook.request.ImageRequest;
import fi.harism.facebook.request.RequestQueue;

/**
 * Storage and fetching for images. Every bitmap is stored into memory as long
 * as there's room for it in DataCache.
 * 
 * @author harism
 */
public class DAOBitmap {

	// RequestQueue instance.
	private RequestQueue requestQueue = null;
	// Local image cache.
	private DataCache imageCache = null;
	// Our image cache size.
	private final int IMAGE_CACHE_SIZE = 1024000;

	/**
	 * Default constructor. RequestQueue instance is needed for making
	 * asynchronous bitmap loading requests.
	 * 
	 * @param requestQueue
	 *            RequestQueue instance.
	 */
	public DAOBitmap(RequestQueue requestQueue) {
		this.requestQueue = requestQueue;
		imageCache = new DataCache(IMAGE_CACHE_SIZE);
	}

	/**
	 * Triggers an image request. If image is found on local cache this method
	 * triggers callback to observer instantly.
	 * 
	 * @param activity
	 *            Activity which tiggered this request.
	 * @param imageUrl
	 *            URL for image.
	 * @param observer
	 *            Observer for this request.
	 */
	public void getBitmap(Activity activity, final String imageUrl,
			final DAOObserver<Bitmap> observer) {
		// First check if image is in cache already.
		if (imageCache.containsKey(imageUrl)) {
			// Create Bitmap from image cache.
			byte[] data = imageCache.getData(imageUrl);
			final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
					data.length);

			// Trigger callback from UI thread.
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					observer.onComplete(bitmap);
				}
			});
		} else {
			// Create new image request.
			ImageRequest r = new ImageRequest(activity, imageUrl,
					new ImageRequest.Observer() {

						@Override
						public void onComplete(ImageRequest imageRequest) {
							// Store image to local cache.
							imageCache.setData(imageUrl,
									imageRequest.getBitmapData());
							observer.onComplete(imageRequest.getBitmap());
						}

						@Override
						public void onError(Exception ex) {
							observer.onError(ex);
						}
					});
			requestQueue.addRequest(r);
		}
	}

}
