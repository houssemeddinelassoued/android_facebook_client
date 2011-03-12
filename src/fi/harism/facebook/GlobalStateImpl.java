package fi.harism.facebook;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import fi.harism.facebook.net.NetController;

/**
 * GlobalStateImpl class extends Application and is used as base class for our
 * application. It is used for storing application wide data among Activities.
 * 
 * @author harism
 */
public class GlobalStateImpl extends Application implements GlobalState {

	// Instance of DataHandler.
	private NetController controller = null;
	// Default profile picture.
	private Bitmap defaultPicture = null;

	@Override
	public Bitmap getDefaultPicture() {
		if (defaultPicture == null) {
			defaultPicture = BitmapFactory.decodeResource(getResources(),
					R.drawable.picture_default);
		}
		return defaultPicture;
	}

	@Override
	public NetController getController() {
		if (controller == null) {
			controller = new NetController();
		}
		return controller;
	}

}
