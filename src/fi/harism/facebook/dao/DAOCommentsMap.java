package fi.harism.facebook.dao;

import java.util.HashMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import fi.harism.facebook.net.FacebookClient;
import fi.harism.facebook.request.Request;
import fi.harism.facebook.request.RequestQueue;

public class DAOCommentsMap {

	// RequestQueue instance.
	private RequestQueue requestQueue = null;
	// FacebookClient instance.
	private FacebookClient facebookClient = null;
	// HashMap for storing loaded profiles.
	private HashMap<String, Vector<DAOComment>> commentsMap = null;

	public DAOCommentsMap(RequestQueue requestQueue,
			FacebookClient facebookClient) {
		this.requestQueue = requestQueue;
		this.facebookClient = facebookClient;
		commentsMap = new HashMap<String, Vector<DAOComment>>();
	}

	public void getComments(Activity activity, final String itemId,
			final DAOObserver<Vector<DAOComment>> observer) {
		if (commentsMap.containsKey(itemId)) {
			// Call observer from UI thread.
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					observer.onComplete(commentsMap.get(itemId));
				}
			});
		} else {
			CommentsRequest request = new CommentsRequest(activity, itemId,
					observer);
			requestQueue.addRequest(request);
		}
	}

	private class CommentsRequest extends Request {

		private String itemId;
		private DAOObserver<Vector<DAOComment>> observer;
		private Vector<DAOComment> comments;

		public CommentsRequest(Activity activity, String itemId,
				DAOObserver<Vector<DAOComment>> observer) {
			super(activity);
			this.itemId = itemId;
			this.observer = observer;
		}

		@Override
		public void runOnThread() throws Exception {
			try {
				// Do actual request.
				Bundle params = new Bundle();
				params.putString(FacebookClient.TOKEN,
						facebookClient.getAccessToken());
				params.putString("fields", "from,message,created_time");
				params.putString("limit", "999999");
				JSONObject resp = facebookClient.request(itemId + "/comments",
						params);
				
				comments = new Vector<DAOComment>();
				JSONArray data = resp.getJSONArray("data");
				if (data != null) {
					for (int i = 0; i < data.length(); ++i) {
						JSONObject comment = data.getJSONObject(i);
						String fromName = comment.getJSONObject("from")
								.getString("name");
						String message = comment.getString("message");
						String createdTime = comment.getString("created_time");
						DAOComment c = new DAOComment(fromName, message,
								createdTime);
						comments.add(c);
					}
				}
				commentsMap.put(itemId, comments);
			} catch (Exception ex) {
				observer.onError(ex);
				throw ex;
			}
		}

		@Override
		public void runOnUiThread() throws Exception {
			observer.onComplete(comments);
		}

	}
}
