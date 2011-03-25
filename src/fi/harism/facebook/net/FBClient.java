package fi.harism.facebook.net;

import org.json.JSONObject;

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
public class FBClient {

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
	public FBClient() {
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
	public void authorize(Activity activity, final LoginObserver observer) {
		// Check if we have authorized Facebook instance already.
		if (facebookAuthorized) {
			observer.onComplete();
		} else {
			// List of permissions our application needs.
			String permissions[] = {
					// For reading streams and posting comments.
					"read_stream", "publish_stream",
					// Not much in use at the moment.
					"user_status", "friends_status",
					// Needed to login to chat.facebook.com.
					"xmpp_login" };
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
						public void onError(DialogError ex) {
							observer.onError(new Exception(ex
									.getLocalizedMessage()));
						}

						@Override
						public void onFacebookError(FacebookError ex) {
							observer.onError(new Exception(ex
									.getLocalizedMessage()));
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
	 * Checker method for testing if this FacebookClient instance has been
	 * authorized already.
	 * 
	 * @return Returns true if Facebook instance has been authorized.
	 */
	public boolean isAuthorized() {
		return facebookAuthorized;
	}

	/**
	 * Sets this FacebookClient to logged out state.
	 * 
	 * @param activity
	 *            Should be same Context used for authorization.
	 * @param observer
	 *            LogoutObserver.
	 */
	public void logout(final Activity activity, final LogoutObserver observer) {
		new Thread() {
			@Override
			public void run() {
				try {
					if (facebookAuthorized) {
						facebook.logout(activity);
						facebookAuthorized = false;
					}
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							observer.onComplete();
						}
					});
				} catch (final Exception ex) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							observer.onError(ex);
						}
					});
				}
			}
		}.start();
	}

	/**
	 * Old/deprecated Facebook REST request call. This is used only for
	 * creating/requesting secret session key needed for chat authorization.
	 * 
	 * @param parameters
	 *            Request parameters.
	 * @return Response String.
	 */
	public String request(Bundle parameters) throws Exception {
		return facebook.request(parameters);
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

	public interface LoginObserver {
		public void onCancel();

		public void onComplete();

		public void onError(Exception error);
	}

	public interface LogoutObserver {
		public void onComplete();

		public void onError(Exception error);
	}

}
