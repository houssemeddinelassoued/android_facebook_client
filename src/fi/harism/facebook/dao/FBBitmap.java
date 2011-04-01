package fi.harism.facebook.dao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import fi.harism.facebook.util.DataCache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Class for handling image loading and caching.
 * 
 * @author harism
 */
public class FBBitmap {

	// Internal data storage.
	private DataCache imageCache;
	// Url for this image.
	private String url;
	// Image data.
	private byte[] bitmapData;

	/**
	 * Default constructor.
	 * 
	 * @param imageCache
	 *            DataCache instance.
	 * @param url
	 *            Url for image.
	 */
	FBBitmap(DataCache imageCache, String url) {
		this.imageCache = imageCache;
		this.url = url;
		// Set bitmapData from imageCache, will be null if not found.
		this.bitmapData = imageCache.getData(url);
	}

	/**
	 * Returns url for this image.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Returns Bitmap object if image has been loaded, null otherwise.
	 */
	public Bitmap getBitmap() {
		if (bitmapData != null) {
			return BitmapFactory.decodeByteArray(bitmapData, 0,
					bitmapData.length);
		} else {
			return null;
		}
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
		URL u = new URL(url);
		InputStream is = u.openStream();
		ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();

		// Read actual data from InputStream.
		int readLength;
		byte buffer[] = new byte[1024];
		while ((readLength = is.read(buffer)) != -1) {
			imageBuffer.write(buffer, 0, readLength);
		}

		bitmapData = imageBuffer.toByteArray();
		imageCache.setData(url, bitmapData);

		return BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
	}

}
