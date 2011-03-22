package fi.harism.facebook;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import fi.harism.facebook.dao.FBFactory;
import fi.harism.facebook.net.FBClient;
import fi.harism.facebook.request.RequestQueue;

/**
 * GlobalStateImpl class extends Application and is used as base class for our
 * application. It is used for storing application wide data among Activities.
 * 
 * @author harism
 */
public class GlobalStateImpl extends Application implements GlobalState {

	// FBClient instance.
	private FBClient fbClient = null;
	// FBFactory instance;
	private FBFactory fbFactory = null;
	// Default profile picture.
	private Bitmap defaultPicture = null;

	@Override
	public Bitmap getDefaultPicture() {
		if (defaultPicture == null) {
			defaultPicture = BitmapFactory.decodeResource(getResources(),
					R.drawable.default_profile_picture);
		}
		return defaultPicture;
	}

	@Override
	public FBClient getFBClient() {
		if (fbClient == null) {
			fbClient= new FBClient();
		}
		return fbClient;
	}
	
	@Override
	public FBFactory getFBFactory() {
		if (fbFactory == null) {
			fbFactory= new FBFactory(new RequestQueue(), getFBClient());
		}
		return fbFactory;
	}

}
