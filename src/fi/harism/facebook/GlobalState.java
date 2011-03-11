package fi.harism.facebook;

import android.graphics.Bitmap;
import fi.harism.facebook.data.DataCache;
import fi.harism.facebook.data.FacebookController;

/**
 * GlobalState interface.
 * 
 * @author harism
 */
public interface GlobalState {

	/**
	 * Returns application wide instance of DataCache. Creates one once this
	 * method is called for the first time.
	 * 
	 * @return DataCache instance.
	 */
	public DataCache getDataCache();

	/**
	 * Returns instance of default profile picture. Creates one once this method
	 * is called for the first time.
	 * 
	 * @return Default profile picture Bitmap.
	 */
	public Bitmap getDefaultPicture();

	/**
	 * Returns application wide instance of FacebookController. Creates one once
	 * this method is called for the first time.
	 * 
	 * @return FacebookController instance.
	 */
	public FacebookController getFacebookController();

}
