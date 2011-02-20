package fi.harism.facebook.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

public class FacebookController {

	private static FacebookController instance = null;
	public static final String TOKEN = Facebook.TOKEN;

	private static final String FACEBOOK_APP_ID = "190087744355420";

	public static final FacebookController getFacebookController() {
		if (instance == null) {
			instance = new FacebookController();
		}
		return instance;
	}

	private Facebook facebook = null;

	private boolean facebookAuthorized;

	private FacebookController() {
		facebook = new Facebook(FACEBOOK_APP_ID);
		facebookAuthorized = false;
	}

	public void authorize(Activity activity, final LoginObserver observer) {

		if (facebookAuthorized) {
			observer.onComplete();
		} else {
			String permissions[] = { "user_status", "friends_status",
					"read_stream" };
			facebook.authorize(activity, permissions,
					new Facebook.DialogListener() {
						@Override
						public void onCancel() {
							observer.onCancel();
						}

						@Override
						public void onComplete(Bundle values) {
							facebookAuthorized = true;
							observer.onComplete();
						}

						@Override
						public void onError(DialogError e) {
							Exception ex = new Exception(e
									.getLocalizedMessage());
							observer.onError(ex);
						}

						@Override
						public void onFacebookError(FacebookError e) {
							Exception ex = new Exception(e
									.getLocalizedMessage());
							observer.onError(ex);
						}
					});
		}
	}

	public void authorizeCallback(int requestCode, int resultCode, Intent data) {
		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	public String getAccessToken() {
		return facebook.getAccessToken();
	}

	public String request(String path) throws Exception {
		return facebook.request(path);
	}

	public String request(String path, Bundle bundle) throws Exception {
		return facebook.request(path, bundle);
	}

	public interface LoginObserver {
		public void onCancel();

		public void onComplete();

		public void onError(Exception ex);
	}

}
