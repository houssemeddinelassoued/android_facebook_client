package fi.harism.facebook;

import android.graphics.Bitmap;
import fi.harism.facebook.net.RequestController;

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
	 * Returns application wide instance of RequestController. Creates one once
	 * this method is called for the first time.
	 * 
	 * @return RequestController instance.
	 */
	public RequestController getRequestController();

}
