package fi.harism.facebook.util;

import java.util.Vector;

/**
 * Very simple memory based data cache. Data is stored as byte arrays into a
 * Vector of CacheItems using String as key.
 * 
 * @author harism
 */
public class DataCache {

	// Our Bitmap storage.
	private Vector<CacheItem> cacheList;
	// Current size of cache;
	private int cacheSize;
	// Maximum size of cache in bytes.
	private int cacheMaxSize;

	/**
	 * Default constructor.
	 */
	public DataCache(int cacheMaxSize) {
		cacheList = new Vector<CacheItem>();
		this.cacheMaxSize = cacheMaxSize;
		cacheSize = 0;
	}

	/**
	 * Checker method for testing whether data with given key is stored.
	 * 
	 * @param key
	 *            Key for data.
	 * @return True if data for given key exists, false otherwise.
	 */
	public boolean containsKey(String key) {
		return findCacheItem(key) != null;
	}

	/**
	 * Returns byte array with given key, or null if there is no data stored
	 * with one.
	 * 
	 * @param key
	 *            Key for data
	 * @return Stored byte array or null if none found.
	 */
	public byte[] getData(String key) {
		CacheItem cacheItem = findCacheItem(key);
		if (cacheItem != null) {
			// If we found CacheItem move it on top of cacheList.
			cacheList.remove(cacheItem);
			cacheList.add(cacheItem);
			return cacheItem.data;
		}
		return null;
	}

	/**
	 * Stores given data using given key.
	 * 
	 * @param key
	 *            Key for data.
	 * @param data
	 *            Data to be stored.
	 */
	public void setData(String key, byte[] data) {
		// First check if given key exists already.
		CacheItem cacheItem = findCacheItem(key);
		// If there is room to store data at all.
		if (data.length <= cacheMaxSize) {
			// If CacheItem was not found.
			if (cacheItem == null) {
				// Create new CacheItem.
				cacheItem = new CacheItem();
				cacheItem.key = key;
				cacheItem.data = data;
				// Remove CacheItems until there is room for new CacheItem.
				while (cacheSize + data.length > cacheMaxSize) {
					// Update cacheSize.
					cacheSize -= cacheList.get(0).data.length;
					// Remove CacheItem from cacheList.
					cacheList.remove(0);
				}
				// Update cacheSize.
				cacheSize += data.length;
				// Add new CacheItem to cacheList.
				cacheList.add(cacheItem);
			}
			// We found existing CacheItem with same key.
			else {
				// Move CacheItem to top of cacheList.
				cacheList.remove(cacheItem);
				cacheList.add(cacheItem);
				// Remove CacheItems until there is room for updated data size.
				while (cacheSize + data.length - cacheItem.data.length > cacheMaxSize) {
					// Update cacheSize and remove 'oldest' CacheItem.
					cacheSize -= cacheList.get(0).data.length;
					cacheList.remove(0);
				}
				// Update cacheSize with new data length.
				cacheSize += data.length - cacheItem.data.length;
				cacheItem.data = data;
			}
		} else if (cacheItem != null) {
			cacheSize -= cacheItem.data.length;
			cacheList.remove(cacheItem);
		}
	}

	/**
	 * Searches for CacheItem with given key. Returns it if one found or null
	 * otherwise.
	 * 
	 * @param key
	 *            Key for CacheItem.
	 * @return CacheItem with given key or null if not found.
	 */
	private CacheItem findCacheItem(String key) {
		for (int i = 0; i < cacheList.size(); ++i) {
			CacheItem cacheItem = cacheList.get(i);
			if (cacheItem.key.equals(key)) {
				return cacheItem;
			}
		}
		return null;
	}

	/**
	 * Private CacheItem class for storing key and cache data.
	 */
	private static final class CacheItem {
		public String key;
		public byte[] data;
	}

}
