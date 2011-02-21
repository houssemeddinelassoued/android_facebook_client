package fi.harism.facebook;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import fi.harism.facebook.util.FacebookController;

public class GlobalState extends Application {

	private FacebookController facebookController = null;
	private Bitmap defaultPicture = null;

	public FacebookController getFacebookController() {
		return facebookController;
	}

	public void setFacebookController(FacebookController facebookController) {
		this.facebookController = facebookController;
	}
	
	public Bitmap getDefaultPicture() {
		if (defaultPicture == null) {
			defaultPicture = BitmapFactory.decodeResource(getResources(), R.drawable.picture_default);
		}
		return defaultPicture;
	}

}
