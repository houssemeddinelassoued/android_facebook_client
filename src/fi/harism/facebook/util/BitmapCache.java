package fi.harism.facebook.util;

import java.util.Vector;

/**
 * Very simple memory based Bitmap cache. Pictures are stored into a Vector of
 * CacheItems using picture url as key.
 * 
 * TODO: Rename class and methods as this class stores byte arrays instead.
 * 
 * @author harism
 */
public class BitmapCache {

	// Our Bitmap storage.
	private Vector<CacheItem> cacheList;
	// Current size of cache;
	private int cacheSize;
	// Maximum size of cache in bytes.
	private static final int CACHE_MAX_SIZE = 1024000;

	/**
	 * Default constructor.
	 */
	public BitmapCache() {
		cacheList = new Vector<CacheItem>();
		cacheSize = 0;
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
		CacheItem cacheItem = findCacheItem(bitmapUrl);
		if (cacheItem != null) {
			cacheList.remove(cacheItem);
			cacheList.add(cacheItem);
			return cacheItem.data;
		}
		return null;
	}

	/**
	 * Checker method for testing whether Bitmap with given url is stored.
	 * 
	 * @param bitmapUrl
	 *            Url for Bitmap.
	 * @return True if Bitmap for given url exists, false otherwise.
	 */
	public boolean hasBitmap(String bitmapUrl) {
		return findCacheItem(bitmapUrl) != null;
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
		CacheItem cacheItem = findCacheItem(bitmapUrl);
		if (bitmap.length <= CACHE_MAX_SIZE) {
			if (cacheItem == null) {
				cacheItem = new CacheItem();
				cacheItem.url = bitmapUrl;
				cacheItem.data = bitmap;
				while (cacheSize + bitmap.length > CACHE_MAX_SIZE) {
					cacheSize -= cacheList.get(0).data.length;
					cacheList.remove(0);
				}
				cacheSize += bitmap.length;
				cacheList.add(cacheItem);
			} else {
				cacheList.remove(cacheItem);
				cacheList.add(cacheItem);
				while (cacheSize + bitmap.length - cacheItem.data.length > CACHE_MAX_SIZE) {
					cacheSize -= cacheList.get(0).data.length;
					cacheList.remove(0);
				}
				cacheSize += bitmap.length - cacheItem.data.length;
				cacheItem.data = bitmap;
			}
		} else if (cacheItem != null) {
			cacheSize -= cacheItem.data.length;
			cacheList.remove(cacheItem);
		}
	}

	/**
	 * Searches for CacheItem with given url. Returns one found or null
	 * otherwise.
	 * 
	 * @param itemUrl
	 *            Url we are looking for.
	 * @return CacheItem with given Url or null if not found.
	 */
	private CacheItem findCacheItem(String itemUrl) {
		for (int i = 0; i < cacheList.size(); ++i) {
			CacheItem cacheItem = cacheList.get(i);
			if (cacheItem.url.equals(itemUrl)) {
				return cacheItem;
			}
		}
		return null;
	}

	/**
	 * Private CacheItem class for storing url and cache data.
	 */
	private static final class CacheItem {
		public String url;
		public byte[] data;
	}

}
