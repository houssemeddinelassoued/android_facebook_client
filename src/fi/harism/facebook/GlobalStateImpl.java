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

	// RequestQueue instance.
	private RequestQueue mRequestQueue = null;
	// FBClient instance.
	private FBClient mFBClient = null;
	// FBFactory instance;
	private FBFactory mFBFactory = null;
	// Default profile picture.
	private Bitmap mDefaultPicture = null;

	@Override
	public Bitmap getDefaultPicture() {
		if (mDefaultPicture == null) {
			mDefaultPicture = BitmapFactory.decodeResource(getResources(),
					R.drawable.default_profile_picture);
		}
		return mDefaultPicture;
	}

	@Override
	public FBClient getFBClient() {
		if (mFBClient == null) {
			mFBClient= new FBClient();
		}
		return mFBClient;
	}
	
	@Override
	public FBFactory getFBFactory() {
		if (mFBFactory == null) {
			mFBFactory= new FBFactory(getRequestQueue(), getFBClient());
		}
		return mFBFactory;
	}
	
	@Override
	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = new RequestQueue();
		}
		return mRequestQueue;
	}

}
