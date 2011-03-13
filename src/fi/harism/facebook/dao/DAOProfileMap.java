package fi.harism.facebook.dao;

import java.util.HashMap;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import fi.harism.facebook.net.FacebookClient;
import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.RequestQueue;

public class DAOProfileMap {
	
	private RequestQueue requestQueue = null;
	private FacebookClient facebookClient = null;
	private HashMap<String, DAOProfile> profileMap = null;
	
	public DAOProfileMap(RequestQueue requestQueue, FacebookClient facebookClient) {
		this.requestQueue = requestQueue;
		this.facebookClient = facebookClient;
		profileMap = new HashMap<String, DAOProfile>();
	}
	
	public void getProfile(Activity activity, final String userId, final DAOObserver<DAOProfile> observer) {
		if (profileMap.containsKey(userId)) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					observer.onComplete(profileMap.get(userId));
				}
			});
		} else {
			Bundle b = new Bundle();
			b.putString(FacebookClient.TOKEN,
					facebookClient.getAccessToken());
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
								DAOProfile r = new DAOProfile(id,
										name, picture);
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
