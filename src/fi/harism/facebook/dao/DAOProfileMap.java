package fi.harism.facebook.dao;

import java.util.HashMap;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import fi.harism.facebook.net.FacebookClient;
import fi.harism.facebook.request.Request;
import fi.harism.facebook.request.RequestQueue;

/**
 * Storage and fetching class for user profiles.
 * 
 * @author harism
 */
public class DAOProfileMap {

	// RequestQueue instance.
	private RequestQueue requestQueue = null;
	// FacebookClient instance.
	private FacebookClient facebookClient = null;
	// HashMap for storing loaded profiles.
	private HashMap<String, DAOProfile> profileMap = null;

	/**
	 * Default constructor.
	 * 
	 * @param requestQueue
	 *            RequestQueue instance.
	 * @param facebookClient
	 *            FacebookClient instance.
	 */
	public DAOProfileMap(RequestQueue requestQueue,
			FacebookClient facebookClient) {
		this.requestQueue = requestQueue;
		this.facebookClient = facebookClient;
		profileMap = new HashMap<String, DAOProfile>();
	}

	/**
	 * This method fetches profile information for given user id via Facebook
	 * Graph API. If it has been stored locally already this method calls
	 * observer asap.
	 * 
	 * @param activity
	 *            Activity which created this request.
	 * @param userId
	 *            User ID.
	 * @param observer
	 *            Observer for this request.
	 */
	public void getProfile(Activity activity, final String userId,
			final DAOObserver<DAOProfile> observer) {
		// Check if profile for userId is stored.
		if (profileMap.containsKey(userId)) {
			// Call observer from UI thread.
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					observer.onComplete(profileMap.get(userId));
				}
			});
		} else {
			ProfileRequest request = new ProfileRequest(activity, userId, observer);
			requestQueue.addRequest(request);
		}
	}
	
	private class ProfileRequest extends Request {
		
		private String userId;
		private DAOObserver<DAOProfile> observer;
		private DAOProfile profile;

		public ProfileRequest(Activity activity, String userId, DAOObserver<DAOProfile> observer) {
			super(activity);
			this.userId = userId;
			this.observer = observer;
		}

		@Override
		public void runOnThread() throws Exception {
			try {
				Bundle params = new Bundle();
				params.putString(FacebookClient.TOKEN, facebookClient.getAccessToken());
				params.putString("fields", "name,picture");
				JSONObject resp = facebookClient.request(userId, params);
				String name = resp.getString("name");
				String picture = resp.getString("picture");
				
				params = new Bundle();
				params.putString(FacebookClient.TOKEN, facebookClient.getAccessToken());
				params.putString("limit", "1");
				params.putString("fields", "message");
				String status;
				try {
					resp = facebookClient.request(userId + "/statuses", params);
					status = resp.getJSONArray("data").getJSONObject(0).getString("message");
				} catch (Exception ex) {
					status = "Status error: " + ex.getLocalizedMessage();
				}
				
				profile = new DAOProfile(userId, name, picture, status);
				profileMap.put(userId, profile);				
			} catch (Exception ex) {
				observer.onError(ex);
				throw ex;
			}
		}

		@Override
		public void runOnUiThread() throws Exception {
			observer.onComplete(profile);
		}
		
	}

}
