package fi.harism.facebook.dao;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import fi.harism.facebook.request.Request;

public class FBBitmapCache {

	private FBStorage fbStorage;

	public FBBitmapCache(FBStorage fbStorage) {
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

	public void load(String imageUrl, String id, FBObserver<FBBitmap> observer) {
		if (fbStorage.imageCache.containsKey(imageUrl)) {
			try {
				Bitmap bitmap = load(imageUrl);
				observer.onComplete(new FBBitmap(id, bitmap));
			} catch (final Exception ex) {
				observer.onError(ex);
			}
		} else {
			BitmapRequest request = new BitmapRequest(this, id, imageUrl,
					observer);
			request.setPriority(Request.PRIORITY_HIGH);
			fbStorage.requestQueue.addRequest(request);
		}
	}

	private class BitmapRequest extends Request {

		private String id;
		private String imageUrl;
		private FBObserver<FBBitmap> observer;

		public BitmapRequest(Object key, String id, String imageUrl,
				FBObserver<FBBitmap> observer) {
			super(key);
			this.id = id;
			this.imageUrl = imageUrl;
			this.observer = observer;
		}

		@Override
		public void run() {
			try {
				Bitmap bitmap = load(imageUrl);
				observer.onComplete(new FBBitmap(id, bitmap));
			} catch (Exception ex) {
				observer.onError(ex);
			}
		}

		@Override
		public void stop() {
			// TODO:
		}

	}

}
