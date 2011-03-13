package fi.harism.facebook.dao;

import java.util.HashMap;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import fi.harism.facebook.net.FacebookClient;
import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.RequestQueue;

public class DAOStatusMap {
	
	private RequestQueue requestQueue = null;
	private FacebookClient facebookClient = null;
	private HashMap<String, DAOStatus> statusMap = null;
	
	public DAOStatusMap(RequestQueue requestQueue, FacebookClient facebookClient) {
		this.requestQueue = requestQueue;
		this.facebookClient = facebookClient;
		statusMap = new HashMap<String, DAOStatus>();
	}
	
	public void getStatus(Activity activity, final String userId, final DAOObserver<DAOStatus> observer) {
		if (statusMap.containsKey(userId)) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					observer.onComplete(statusMap.get(userId));
				}
			});
		} else {
			Bundle b = new Bundle();
			b.putString("limit", "1");
			b.putString("fields", "message");
			FacebookRequest r = new FacebookRequest(activity, userId + "/statuses",
					b, facebookClient, new FacebookRequest.Observer() {
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
