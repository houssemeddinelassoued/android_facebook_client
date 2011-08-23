package fi.harism.facebook.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

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
	private Facebook mFacebook = null;

	/**
	 * Default constructor. Before using this class authorize should be called
	 * successfully.
	 */
	public FBClient() {
		mFacebook = new Facebook(FACEBOOK_APP_ID);
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
		// List of permissions our application needs.
		String permissions[] = {
				// For reading streams and posting comments.
				"read_stream", "publish_stream",
				// For latest status message.
				"user_status", "friends_status",
				// Needed for login to chat.facebook.com.
				"xmpp_login" };
		// Call actual authorization procedure.
		mFacebook.authorize(activity, permissions, Facebook.FORCE_DIALOG_AUTH,
				new Facebook.DialogListener() {
					@Override
					public void onCancel() {
						observer.onCancel();
					}

					@Override
					public void onComplete(Bundle values) {
						observer.onComplete();
					}

					@Override
					public void onError(DialogError ex) {
						observer.onError(new Exception(ex.getLocalizedMessage()));
					}

					@Override
					public void onFacebookError(FacebookError ex) {
						observer.onError(new Exception(ex.getLocalizedMessage()));
					}
				});
	}

	public void authorizeCallback(int requestCode, int resultCode, Intent data) {
		mFacebook.authorizeCallback(requestCode, resultCode, data);
	}

	public String getAccessToken() {
		return mFacebook.getAccessToken();
	}

	/**
	 * Checker method for testing if this FacebookClient instance has been
	 * authorized already.
	 * 
	 * @return Returns true if Facebook instance has been authorized.
	 */
	public boolean isAuthorized() {
		return mFacebook.isSessionValid();
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
					mFacebook.logout(activity);
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
	public String request(Bundle parameters) throws IOException {
		return mFacebook.request(parameters);
	}

	/**
	 * Synchronous Facebook Graph API call.
	 * 
	 * @param graphPath
	 *            Facebook Graph API path.
	 * @return JSON object for response.
	 * @throws IOException
	 * @throws JSONException
	 */
	public JSONObject request(String graphPath) throws IOException,
			JSONException {
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
	 * @throws IOException
	 * @throws JSONException
	 */
	public JSONObject request(String graphPath, Bundle requestParameters)
			throws IOException, JSONException {
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
	 * @throws IOException
	 * @throws JSONException
	 */
	public JSONObject request(String graphPath, Bundle requestParameters,
			String method) throws IOException, JSONException {
		String response = mFacebook.request(graphPath, requestParameters,
				method);
		try {
			return Util.parseJson(response);
		} catch (FacebookError error) {
			// TODO: Handle FacebookError more properly.. Try to catch
			// unauthorized especially.
			throw new IOException(error.getMessage());
		}
	}

	/**
	 * Executes a FQL query at
	 * https://api.facebook.com/method/fql.query?access_token=TOKEN&query=QUERY
	 * 
	 * @param query
	 *            FQL query string.
	 * @return JSON presentation for response.
	 * @throws IOException
	 * @throws JSONException
	 * @throws MalformedURLException
	 * @throws XmlPullParserException
	 */
	public JSONObject requestFQL(String query) throws IOException,
			JSONException, MalformedURLException, XmlPullParserException {

		String token = URLEncoder.encode(mFacebook.getAccessToken());
		query = URLEncoder.encode(query);

		URL url = new URL(
				"https://api.facebook.com/method/fql.query?access_token="
						+ token + "&query=" + query);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		InputStream is = connection.getInputStream();

		JSONObject out = new JSONObject(FQLParser.parse(is));

		JSONObject error = out.optJSONObject("error");
		if (error != null) {
			// TODO: It might be a good idea to check different error messages.
			throw new IOException("FQL error: " + error.getString("error_msg"));
		}

		return out;
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
