package fi.harism.facebook.dao;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import fi.harism.facebook.net.DataCache;
import fi.harism.facebook.request.ImageRequest;
import fi.harism.facebook.request.RequestQueue;

public class DAOBitmap {
	
	private RequestQueue requestQueue = null;
	private DataCache imageCache = null;
	
	public DAOBitmap(RequestQueue requestQueue) {
		this.requestQueue = requestQueue;
		imageCache = new DataCache(1024000);
	}
	
	public void getBitmap(Activity activity, final String imageUrl, final DAOObserver<Bitmap> observer) {
		if (imageCache.containsKey(imageUrl)) {
			byte[] data = imageCache.getData(imageUrl);
			final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
					data.length);

			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					observer.onComplete(bitmap);
				}
			});
		} else {
			ImageRequest r = new ImageRequest(activity, imageUrl,
					new ImageRequest.Observer() {

						@Override
						public void onComplete(ImageRequest imageRequest) {
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
