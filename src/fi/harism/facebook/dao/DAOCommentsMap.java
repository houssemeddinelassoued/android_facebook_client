package fi.harism.facebook.dao;

import java.util.HashMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;

import fi.harism.facebook.net.FacebookClient;
import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.RequestQueue;

public class DAOCommentsMap {

	// RequestQueue instance.
	private RequestQueue requestQueue = null;
	// FacebookClient instance.
	private FacebookClient facebookClient = null;
	// HashMap for storing loaded profiles.
	private HashMap<String, Vector<DAOComment>> commentsMap = null;

	public DAOCommentsMap(RequestQueue requestQueue, FacebookClient facebookClient) {
		this.requestQueue = requestQueue;
		this.facebookClient = facebookClient;
		commentsMap = new HashMap<String, Vector<DAOComment>>();
	}
	
	public void getComments(Activity activity, final String postId, final DAOObserver<Vector<DAOComment>> observer) {
		if (commentsMap.containsKey(postId)) {
			// Call observer from UI thread.
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					observer.onComplete(commentsMap.get(postId));
				}
			});
		} else {
			// Create Facebook request.
			Bundle b = new Bundle();
			b.putString(FacebookClient.TOKEN, facebookClient.getAccessToken());
			b.putString("fields", "from,message,created_time");
			FacebookRequest r = new FacebookRequest(activity, postId + "/comments", b,
					facebookClient, new FacebookRequest.Observer() {
						@Override
						public void onComplete(FacebookRequest facebookRequest) {
							try {
								JSONObject resp = facebookRequest.getResponse();
								Vector<DAOComment> comments = new Vector<DAOComment>();
								JSONArray data = resp.getJSONArray("data");
								if (data != null) {
									for (int i=0; i<data.length(); ++i) {
										JSONObject comment = data.getJSONObject(i);
										String fromName = comment.getJSONObject("from").getString("name");
										String message = comment.getString("message");
										String createdTime = comment.getString("created_time");
										DAOComment c = new DAOComment(fromName, message, createdTime);
										comments.add(c);
									}
								}
								commentsMap.put(postId, comments);
								observer.onComplete(comments);
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
