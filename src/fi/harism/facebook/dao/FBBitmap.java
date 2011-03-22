package fi.harism.facebook.dao;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import fi.harism.facebook.request.Request;

public class FBBitmap {

	private FBStorage fbStorage;

	public FBBitmap(FBStorage fbStorage) {
		this.fbStorage = fbStorage;
	}
	
	public void setPaused(boolean paused) {
		fbStorage.requestQueue.setPaused(this, paused);
	}
	
	public void cancel() {
		fbStorage.requestQueue.removeRequests(this);
	}

	public Bitmap load(String imageUrl) throws Exception {
		if (fbStorage.imageCache.containsKey(imageUrl)) {
			// Create Bitmap from image cache.
			byte[] data = fbStorage.imageCache.getData(imageUrl);
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			return bitmap;
		} else {
			// Open InputStream for given url.
			URL u = new URL(imageUrl);
			InputStream is = u.openStream();
			ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();

			// Read actual data from InputStream.
			int readLength;
			byte buffer[] = new byte[1024];
			while ((readLength = is.read(buffer)) != -1) {
				imageBuffer.write(buffer, 0, readLength);
			}

			byte[] bitmapData = imageBuffer.toByteArray();
			fbStorage.imageCache.setData(imageUrl, bitmapData);

			Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0,
					bitmapData.length);
			return bitmap;
		}
	}

	public void load(String imageUrl, Activity activity,
			final FBObserver<Bitmap> observer) {
		if (fbStorage.imageCache.containsKey(imageUrl)) {
			try {
				final Bitmap bitmap = load(imageUrl);
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						observer.onComplete(bitmap);
					}
				});
			} catch (final Exception ex) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						observer.onError(ex);
					}
				});
			}
		} else {
			BitmapRequest request = new BitmapRequest(activity, this, imageUrl,
					observer);
			fbStorage.requestQueue.addRequest(request);
		}
	}

	private class BitmapRequest extends Request {

		private Bitmap bitmap;
		private String imageUrl;
		private FBObserver<Bitmap> observer;

		public BitmapRequest(Activity activity, Object key, String imageUrl,
				FBObserver<Bitmap> observer) {
			super(activity, key);
			this.imageUrl = imageUrl;
			this.observer = observer;
		}

		@Override
		public void runOnThread() throws Exception {
			try {
				bitmap = load(imageUrl);
			} catch (Exception ex) {
				observer.onError(ex);
				throw ex;
			}
		}

		@Override
		public void runOnUiThread() throws Exception {
			observer.onComplete(bitmap);
		}

	}

}
