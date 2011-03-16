package fi.harism.facebook.dao;

import java.util.HashMap;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import fi.harism.facebook.net.FacebookClient;
import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.RequestQueue;

/**
 * This class implements storage and retrieval for user's latest status message.
 * 
 * @author harism
 */
public class DAOStatusMap {

	// RequestQueue instance.
	private RequestQueue requestQueue = null;
	// FacebookClient instance.
	private FacebookClient facebookClient = null;
	// HashMap for storing statuses per userId.
	private HashMap<String, DAOStatus> statusMap = null;

	/**
	 * Default constructor.
	 * 
	 * @param requestQueue
	 *            RequestQueue instance.
	 * @param facebookClient
	 *            FacebookClient instance.
	 */
	public DAOStatusMap(RequestQueue requestQueue, FacebookClient facebookClient) {
		this.requestQueue = requestQueue;
		this.facebookClient = facebookClient;
		statusMap = new HashMap<String, DAOStatus>();
	}

	/**
	 * Returns latest status message for given userId through observer. Status
	 * is loaded via Facebook Graph API if needed and a local copy is returned
	 * instantly if it's found on local storage.
	 * 
	 * @param activity
	 *            Activity which is creating this request.
	 * @param userId
	 *            User Id for whom to retrieve latest status.
	 * @param observer
	 *            Observer for this request.
	 */
	public void getStatus(Activity activity, final String userId,
			final DAOObserver<DAOStatus> observer) {
		// Check if we have latest status stored.
		if (statusMap.containsKey(userId)) {
			// Call observer via Activity UI thread.
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					observer.onComplete(statusMap.get(userId));
				}
			});
		} else {
			// Create status request.
			Bundle b = new Bundle();
			b.putString("limit", "1");
			b.putString("fields", "message");
			FacebookRequest r = new FacebookRequest(activity, userId
					+ "/statuses", b, facebookClient,
					new FacebookRequest.Observer() {
						@Override
						public void onComplete(FacebookRequest facebookRequest) {
							try {
								JSONObject resp = facebookRequest.getResponse();
								String message = resp.getJSONArray("data")
										.getJSONObject(0).getString("message");
								DAOStatus r = new DAOStatus(message);
								statusMap.put(userId, r);
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
