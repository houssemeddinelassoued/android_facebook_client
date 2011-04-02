package fi.harism.facebook.dao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import fi.harism.facebook.util.DataCache;

/**
 * Class for handling image loading and caching.
 * 
 * @author harism
 */
public class FBBitmap {

	// Internal data storage.
	private DataCache mImageCache;
	// Url for this image.
	private String mUrl;
	// Image data.
	private byte[] mBitmapData;

	/**
	 * Default constructor.
	 * 
	 * @param imageCache
	 *            DataCache instance.
	 * @param url
	 *            Url for image.
	 */
	FBBitmap(DataCache imageCache, String url) {
		mImageCache = imageCache;
		mUrl = url;
		// Set bitmapData from imageCache, will be null if not found.
		mBitmapData = imageCache.getData(url);
	}

	/**
	 * Returns Bitmap object if image has been loaded, null otherwise.
	 */
	public Bitmap getBitmap() {
		if (mBitmapData != null) {
			return BitmapFactory.decodeByteArray(mBitmapData, 0,
					mBitmapData.length);
		} else {
			return null;
		}
	}

	/**
	 * Returns url for this image.
	 */
	public String getUrl() {
		return mUrl;
	}

	/**
	 * Loads image from given url. If there is a image loaded already this
	 * method will reload image anyway. Returns loaded Bitmap and stores it for
	 * further getBitmap() calls.
	 * 
	 * @return Bitmap loaded.
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public Bitmap load() throws IOException, MalformedURLException {
		// Open InputStream for given url.
		URL u = new URL(mUrl);
		InputStream is = u.openStream();
		ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();

		// Read actual data from InputStream.
		int readLength;
		byte buffer[] = new byte[1024];
		while ((readLength = is.read(buffer)) != -1) {
			imageBuffer.write(buffer, 0, readLength);
		}

		mBitmapData = imageBuffer.toByteArray();
		mImageCache.setData(mUrl, mBitmapData);

		return BitmapFactory.decodeByteArray(mBitmapData, 0, mBitmapData.length);
	}

}
