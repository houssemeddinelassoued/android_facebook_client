package fi.harism.facebook;

import android.app.Application;
import fi.harism.facebook.util.FacebookController;

public class GlobalState extends Application {

	private FacebookController facebookController = null;

	public FacebookController getFacebookController() {
		return facebookController;
	}

	public void setFacebookController(FacebookController facebookController) {
		this.facebookController = facebookController;
	}

}
