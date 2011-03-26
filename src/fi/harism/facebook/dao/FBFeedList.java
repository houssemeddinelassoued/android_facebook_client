package fi.harism.facebook.dao;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import fi.harism.facebook.request.Request;

public class FBFeedList implements Iterable<FBFeedItem> {

	private FBStorage fbStorage;
	private String feedPath;
	private Vector<FBFeedItem> feedItemList = null;
	public static final String NEWS_FEED = "/me/home";
	public static final String PROFILE_FEED = "/me/feed";

	public FBFeedList(FBStorage fbStorage, String feedPath, Vector<FBFeedItem> feedItemList) {
		this.fbStorage = fbStorage;
		this.feedPath = feedPath;
		this.feedItemList = feedItemList;
	}
	
	public void setPaused(boolean paused) {
		fbStorage.requestQueue.setPaused(this, paused);
	}
	
	public void cancel() {
		fbStorage.requestQueue.removeRequests(this);
	}

	public FBFeedItem at(int index) {
		return feedItemList.elementAt(index);
	}

	public void load() throws Exception {
		Bundle params = new Bundle();
		params.putString(
				"fields",
				"id,type,from,message,picture,link,name,caption,description,created_time,comments,likes");
		JSONObject resp = fbStorage.fbClient.request(feedPath, params);
		JSONArray feedItems = resp.getJSONArray("data");

		Vector<FBFeedItem> itemList = new Vector<FBFeedItem>();

		for (int i = 0; i < feedItems.length(); ++i) {
			JSONObject item = feedItems.getJSONObject(i);

			String id = item.getString("id");
			String type = item.getString("type");
			String fromId = item.getJSONObject("from").getString("id");
			String fromName = item.getJSONObject("from").getString("name");
			String message = item.optString("message", null);
			String picture = item.optString("picture", null);
			String link = item.optString("link", null);
			String name = item.optString("name", null);
			String caption = item.optString("caption", null);
			String description = item.optString("description", null);
			String createdTime = item.optString("created_time", null);

			// TODO: Figure out what to do with profile pictures.
			// This is somewhat fast even though it uses http redirect.
			String fromPicture = "http://graph.facebook.com/" + fromId
					+ "/picture";
			// Getting direct profile picture url from Graph API is much slower.
			// params = new Bundle();
			// params.putString("fields", "picture");
			// String fromPicture = null;
			// try {
			// resp = facebookClient.request(fromId, params);
			// fromPicture = resp.getString("picture");
			// } catch (Exception ex) {
			// }

			itemList.add(new FBFeedItem(id, type, fromId, fromName,
					fromPicture, message, picture, link, name, caption,
					description, createdTime));
		}

		feedItemList.removeAllElements();
		feedItemList.addAll(itemList);
	}

	public void load(FBObserver<FBFeedList> observer) {
		final FBFeedList self = this;
		if (feedItemList.size() > 0) {
			observer.onComplete(self);
		} else {
			FeedRequest request = new FeedRequest(this, observer);
			fbStorage.requestQueue.addRequest(request);
		}
	}

	@Override
	public Iterator<FBFeedItem> iterator() {
		Vector<FBFeedItem> copy = new Vector<FBFeedItem>(feedItemList);
		return copy.iterator();
	}

	public int size() {
		return feedItemList.size();
	}

	private class FeedRequest extends Request {

		private FBFeedList parent;
		private FBObserver<FBFeedList> observer;

		public FeedRequest(FBFeedList parent,
				FBObserver<FBFeedList> observer) {
			super(parent);
			this.parent = parent;
			this.observer = observer;
		}

		@Override
		public void run() {
			try {
				load();
				observer.onComplete(parent);
			} catch (Exception ex) {
				observer.onError(ex);
			}
		}

		@Override
		public void stop() {
			// TODO:
		}

	}
}
