package fi.harism.facebook;

import android.graphics.Bitmap;
import fi.harism.facebook.dao.FBFactory;
import fi.harism.facebook.net.FBClient;

/**
 * GlobalState interface.
 * 
 * @author harism
 */
public interface GlobalState {

	/**
	 * Returns instance of default profile picture. Creates one once this method
	 * is called for the first time.
	 * 
	 * @return Default profile picture Bitmap.
	 */
	public Bitmap getDefaultPicture();

	/**
	 * Returns application wide instance of FBClient. Creates one once this
	 * method is called for the first time.
	 * 
	 * @return FBClient instance.
	 */
	public FBClient getFBClient();

	/**
	 * Returns application wide instance of FBFactory. Creates one once this
	 * method is called for the first time.
	 * 
	 * @return FBFactory instance.
	 */
	public FBFactory getFBFactory();

}
