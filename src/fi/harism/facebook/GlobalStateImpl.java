package fi.harism.facebook;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import fi.harism.facebook.data.DataCache;
import fi.harism.facebook.data.FacebookController;

/**
 * GlobalStateImpl class extends Application and is used as base class for our
 * application. It is used for storing application wide data among Activities.
 * 
 * @author harism
 */
public class GlobalStateImpl extends Application implements GlobalState {

	// Instance of FacebookController.
	private FacebookController facebookController = null;
	// Instance of DataCache.
	private DataCache dataCache = null;
	// Default profile picture.
	private Bitmap defaultPicture = null;

	@Override
	public DataCache getDataCache() {
		if (dataCache == null) {
			dataCache = new DataCache();
		}
		return dataCache;
	}

	@Override
	public Bitmap getDefaultPicture() {
		if (defaultPicture == null) {
			defaultPicture = BitmapFactory.decodeResource(getResources(),
					R.drawable.picture_default);
		}
		return defaultPicture;
	}

	@Override
	public FacebookController getFacebookController() {
		if (facebookController == null) {
			facebookController = new FacebookController();
		}
		return facebookController;
	}

}
