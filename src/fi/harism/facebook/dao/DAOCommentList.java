package fi.harism.facebook.dao;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import fi.harism.facebook.net.FacebookClient;
import fi.harism.facebook.request.Request;
import fi.harism.facebook.request.RequestQueue;

public class DAOCommentList implements Iterable<DAOComment> {

	// RequestQueue instance.
	private RequestQueue requestQueue = null;
	// FacebookClient instance.
	private FacebookClient facebookClient = null;
	private Activity activity = null;
	private String itemId = null;
	// Comments list.
	private Vector<DAOComment> comments;

	public DAOCommentList(RequestQueue requestQueue,
			FacebookClient facebookClient, Activity activity, String itemId) {
		this.requestQueue = requestQueue;
		this.facebookClient = facebookClient;
		this.activity = activity;
		this.itemId = itemId;
		comments = new Vector<DAOComment>();
	}

	public void getComments(DAOObserver<DAOCommentList> observer) {
		CommentsRequest request = new CommentsRequest(this, observer);
		requestQueue.addRequest(request);
	}

	public void postComment(DAOObserver<DAOCommentList> observer, String message) {
		CommentsRequest request = new CommentsRequest(this, observer, message);
		requestQueue.addRequest(request);
	}

	public DAOComment at(int index) {
		return comments.elementAt(index);
	}

	public int size() {
		return comments.size();
	}

	@Override
	public Iterator<DAOComment> iterator() {
		Vector<DAOComment> copy = new Vector<DAOComment>(comments);
		return copy.iterator();
	}

	private class CommentsRequest extends Request {

		private DAOCommentList caller;
		private DAOObserver<DAOCommentList> observer;
		private String sendMessage = null;

		public CommentsRequest(DAOCommentList caller,
				DAOObserver<DAOCommentList> observer) {
			super(activity);
			this.caller = caller;
			this.observer = observer;
		}

		public CommentsRequest(DAOCommentList caller,
				DAOObserver<DAOCommentList> observer, String sendMessage) {
			super(activity);
			this.caller = caller;
			this.observer = observer;
			this.sendMessage = sendMessage;
		}

		@Override
		public void runOnThread() throws Exception {
			try {
				if (sendMessage != null) {
					Bundle params = new Bundle();
					params.putString(FacebookClient.TOKEN,
							facebookClient.getAccessToken());
					params.putString("message", sendMessage);
					facebookClient
							.request(itemId + "/comments", params, "POST");
				}
				
				// Do actual request.
				Bundle params = new Bundle();
				params.putString(FacebookClient.TOKEN,
						facebookClient.getAccessToken());
				params.putString("fields", "from,message,created_time");
				params.putString("limit", "999999");

				JSONObject resp = facebookClient.request(itemId + "/comments",
						params);

				comments.removeAllElements();
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
			} catch (Exception ex) {
				observer.onError(ex);
				throw ex;
			}
		}

		@Override
		public void runOnUiThread() throws Exception {
			observer.onComplete(caller);
		}

	}
}
