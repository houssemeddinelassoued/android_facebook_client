package fi.harism.facebook.util;

import java.util.HashMap;

import android.graphics.Bitmap;

/**
 * Very simple memory based Bitmap cache. Pictures are only stored as HashMap
 * using picture url as key.
 * 
 * @author harism
 */
public class BitmapCache {

	// Our Bitmap storage.
	private HashMap<String, Bitmap> bitmapMap = null;

	/**
	 * Default constructor.
	 */
	public BitmapCache() {
		bitmapMap = new HashMap<String, Bitmap>();
	}

	/**
	 * Returns Bitmap with given url, or null if there is no picture stored with
	 * one.
	 * 
	 * @param bitmapUrl
	 *            Url for Bitmap.
	 * @return Stored Bitmap instance.
	 */
	public Bitmap getBitmap(String bitmapUrl) {
		return bitmapMap.get(bitmapUrl);
	}

	/**
	 * Checker method for checking whether given Bitmap with given url is
	 * stored.
	 * 
	 * @param bitmapUrl
	 *            Url for Bitmap.
	 * @return True if Bitmap for given url exists, false otherwise.
	 */
	public boolean hasBitmap(String bitmapUrl) {
		return bitmapMap.containsKey(bitmapUrl);
	}

	/**
	 * Stores given Bitmap using url as a key.
	 * 
	 * @param bitmapUrl
	 *            Url for Bitmap.
	 * @param bitmap
	 *            Bitmap to be stored.
	 */
	public void setBitmap(String bitmapUrl, Bitmap bitmap) {
		bitmapMap.put(bitmapUrl, bitmap);
	}

}
