package fi.harism.facebook.dao;

import java.util.HashMap;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import fi.harism.facebook.net.FacebookClient;
import fi.harism.facebook.request.FacebookRequest;
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
			// Create Facebook request.
			Bundle b = new Bundle();
			b.putString(FacebookClient.TOKEN, facebookClient.getAccessToken());
			b.putString("fields", "id,name,picture");
			FacebookRequest r = new FacebookRequest(activity, userId, b,
					facebookClient, new FacebookRequest.Observer() {
						@Override
						public void onComplete(FacebookRequest facebookRequest) {
							try {
								JSONObject resp = facebookRequest.getResponse();
								String id = resp.getString("id");
								String name = resp.getString("name");
								String picture = resp.getString("picture");
								DAOProfile r = new DAOProfile(id, name, picture);
								profileMap.put(id, r);
								observer.onComplete(r);
							} catch (Exception ex) {
								observer.onError(ex);
							}
						}

						@Override
						public void onError(Exception ex) {
							observer.onError(ex);
						}
					});
			requestQueue.addRequest(r);
		}
	}

}
