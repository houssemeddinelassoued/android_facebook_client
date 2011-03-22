package fi.harism.facebook.dao;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import fi.harism.facebook.net.FBClient;
import fi.harism.facebook.request.Request;

public class FBCommentList implements Iterable<FBComment> {

	private FBStorage fbStorage;
	private String itemId;
	private Vector<FBComment> comments;

	public FBCommentList(FBStorage fbStorage, String itemId) {
		this.fbStorage = fbStorage;
		this.itemId = itemId;
		comments = new Vector<FBComment>();
	}
	
	public void setPaused(boolean paused) {
		fbStorage.requestQueue.setPaused(this, paused);
	}
	
	public void cancel() {
		fbStorage.requestQueue.removeRequests(this);
	}

	public FBComment at(int index) {
		return comments.elementAt(index);
	}

	@Override
	public Iterator<FBComment> iterator() {
		Vector<FBComment> copy = new Vector<FBComment>(comments);
		return copy.iterator();
	}

	public void load() throws Exception {
		Bundle params = new Bundle();
		params.putString(FBClient.TOKEN, fbStorage.fbClient.getAccessToken());
		params.putString("fields", "from,message,created_time");
		params.putString("limit", "999999");

		JSONObject resp = fbStorage.fbClient.request(itemId + "/comments", params);

		comments.removeAllElements();
		JSONArray data = resp.getJSONArray("data");
		if (data != null) {
			for (int i = 0; i < data.length(); ++i) {
				JSONObject comment = data.getJSONObject(i);
				String fromName = comment.getJSONObject("from").getString(
						"name");
				String message = comment.getString("message");
				String createdTime = comment.getString("created_time");
				FBComment c = new FBComment(fromName, message, createdTime);
				comments.add(c);
			}
		}
	}

	public void load(Activity activity,
			FBObserver<FBCommentList> observer) {
		CommentsRequest request = new CommentsRequest(activity, this, observer);
		fbStorage.requestQueue.addRequest(request);
	}

	public void postComment(String message) throws Exception {
		Bundle params = new Bundle();
		params.putString(FBClient.TOKEN, fbStorage.fbClient.getAccessToken());
		params.putString("message", message);
		fbStorage.fbClient.request(itemId + "/comments", params, "POST");
	}

	public void postComment(String message, Activity activity,
			FBObserver<FBCommentList> observer) {
		CommentsRequest request = new CommentsRequest(activity, this, observer,
				message);
		fbStorage.requestQueue.addRequest(request);
	}

	public int size() {
		return comments.size();
	}

	private class CommentsRequest extends Request {

		private FBCommentList parent;
		private FBObserver<FBCommentList> observer;
		private String sendMessage;

		public CommentsRequest(Activity activity, FBCommentList parent,
				FBObserver<FBCommentList> observer) {
			super(activity, parent);
			this.parent = parent;
			this.observer = observer;
			this.sendMessage = null;
		}

		public CommentsRequest(Activity activity, FBCommentList parent,
				FBObserver<FBCommentList> observer, String sendMessage) {
			super(activity, parent);
			this.parent = parent;
			this.observer = observer;
			this.sendMessage = sendMessage;
		}

		@Override
		public void runOnThread() throws Exception {
			try {
				if (sendMessage != null) {
					postComment(sendMessage);
				}
				load();
			} catch (Exception ex) {
				observer.onError(ex);
				throw ex;
			}
		}

		@Override
		public void runOnUiThread() throws Exception {
			observer.onComplete(parent);
		}

	}
}
