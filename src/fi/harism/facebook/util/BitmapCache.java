package fi.harism.facebook.util;

import java.util.HashMap;

/**
 * Very simple memory based Bitmap cache. Pictures are only stored as HashMap
 * using picture url as key.
 * 
 * TODO: Rename class and methods as this class stores byte arrays instead.
 * 
 * @author harism
 */
public class BitmapCache {

	// Our Bitmap storage.
	private HashMap<String, byte[]> bitmapMap = null;

	/**
	 * Default constructor.
	 */
	public BitmapCache() {
		bitmapMap = new HashMap<String, byte[]>();
	}

	/**
	 * Returns Bitmap with given url, or null if there is no picture stored with
	 * one.
	 * 
	 * @param bitmapUrl
	 *            Url for Bitmap.
	 * @return Stored Bitmap instance.
	 */
	public byte[] getBitmap(String bitmapUrl) {
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
	public void setBitmap(String bitmapUrl, byte[] bitmap) {
		bitmapMap.put(bitmapUrl, bitmap);
	}

}
