package fi.harism.facebook.net;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
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

	public void authorizeCallback(int requestCode, int resultCode, Intent data) {
		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	public String getAccessToken() {
		return facebook.getAccessToken();
	}

	/**
	 * Checker method for testing if this FacebookClient instance has been
	 * authorized already.
	 * 
	 * @return Returns true if Facebook instance has been authorized.
	 */
	public boolean isAuthorized() {
		return facebookAuthorized;
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
			final FacebookLoginObserver observer) {
		// Check if we have authorized Facebook instance already.
		if (facebookAuthorized) {
			observer.onComplete();
		} else {
			// List of permissions our application needs.
			String permissions[] = { "user_status", "friends_status",
					"read_stream", "publish_stream" };
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

	/**
	 * Sets this FacebookClient to logged out state.
	 * 
	 * @param context
	 *            Should be same context used for authorization.
	 * @throws Exception
	 */
	public void logout(Context context) throws Exception {
		if (facebookAuthorized) {
			facebook.logout(context);
			facebookAuthorized = false;
		}
	}

	/**
	 * Synchronous Facebook Graph API call.
	 * 
	 * @param graphPath
	 *            Facebook Graph API path.
	 * @return JSON object for response.
	 * @throws Exception
	 */
	public JSONObject request(String graphPath) throws Exception {
		return request(graphPath, null);
	}

	/**
	 * Synchronous Facebook Graph API call.
	 * 
	 * @param graphPath
	 *            Facebook Graph API path.
	 * @param requestParameters
	 *            Additional request parameters.
	 * @return JSON object for response.
	 * @throws Exception
	 */
	public JSONObject request(String graphPath, Bundle requestParameters)
			throws Exception {
		return request(graphPath, requestParameters, "GET");
	}

	/**
	 * Synchronous Facebook Graph API call.
	 * 
	 * @param graphPath
	 *            Facebook Graph API path.
	 * @param requestParameters
	 *            Additional request parameters.
	 * @param method
	 *            HTTP method e.g. "GET", "POST".
	 * @return JSON Object for response.
	 * @throws Exception
	 */
	public JSONObject request(String graphPath, Bundle requestParameters,
			String method) throws Exception {
		try {
			String response = facebook.request(graphPath, requestParameters,
					method);
			// Create JSONObject from response string.
			JSONObject responseObject = new JSONObject(response);

			// Check if response is an error JSONObject.
			if (responseObject.has("error")) {
				JSONObject err = responseObject.getJSONObject("error");
				Exception ex = new Exception(err.getString("type") + " : "
						+ err.getString("message"));
				throw ex;
			}
			return responseObject;
		} catch (Exception ex) {
			throw ex;
		}
	}

}
