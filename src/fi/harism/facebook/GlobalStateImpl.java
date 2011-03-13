package fi.harism.facebook;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import fi.harism.facebook.net.RequestController;

/**
 * GlobalStateImpl class extends Application and is used as base class for our
 * application. It is used for storing application wide data among Activities.
 * 
 * @author harism
 */
public class GlobalStateImpl extends Application implements GlobalState {

	// Instance of RequestController.
	private RequestController requestController = null;
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
	public RequestController getRequestController() {
		if (requestController == null) {
			requestController = new RequestController();
		}
		return requestController;
	}

}
