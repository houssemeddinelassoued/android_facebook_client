package fi.harism.facebook.net;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

/**
 * FacebookClient class encapsulates all Facebook Android API functionality.
 * 
 * @author harism
 */
public class FacebookClient {

	// Constant TOKEN string.
	public static final String TOKEN = Facebook.TOKEN;
	// Our application id.
	private static final String FACEBOOK_APP_ID = "190087744355420";
	// Private Facebook instance.
	private Facebook facebook = null;
	// Flag for checking if Facebook instance has been authorized.
	private boolean facebookAuthorized = false;

	/**
	 * Default constructor. Before using this class authorize should be called
	 * successfully.
	 */
	public FacebookClient() {
		facebook = new Facebook(FACEBOOK_APP_ID);
	}

	/**
	 * This method triggers authorization procedure.
	 * 
	 * @param activity
	 *            Calling activity.
	 * @param observer
	 *            Observer for this request.
	 */
	public void authorize(Activity activity,
			final FacebookAuthorizeObserver observer) {
		// Check if we have authorized Facebook instance already.
		if (facebookAuthorized) {
			observer.onComplete();
		} else {
			// List of permissions our application needs.
			String permissions[] = { "user_status", "friends_status",
					"read_stream" };
			// Call actual authorization procedure.
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

	/**
	 * Synchronous Facebook Graph API call.
	 * 
	 * @param path
	 *            Facebook Graph API path.
	 * @return JSON string presentation for response.
	 * @throws Exception
	 */
	public String request(String path) throws Exception {
		return facebook.request(path);
	}

	/**
	 * Synchronous Facebook Graph API call.
	 * 
	 * @param path
	 *            Facebook Graph API path.
	 * @param requestParameters
	 *            Additional request parameters.
	 * @return JSON string presentation for response.
	 * @throws Exception
	 */
	public String request(String path, Bundle requestParameters)
			throws Exception {
		return facebook.request(path, requestParameters);
	}

}
