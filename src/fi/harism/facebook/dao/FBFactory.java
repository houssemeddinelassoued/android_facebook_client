package fi.harism.facebook.dao;

import java.util.HashMap;
import java.util.Vector;

import fi.harism.facebook.chat.ChatHandler;
import fi.harism.facebook.net.FBClient;
import fi.harism.facebook.request.RequestQueue;
import fi.harism.facebook.util.DataCache;

public class FBFactory {

	private RequestQueue requestQueue;
	private FBClient fbClient;

	private HashMap<String, FBFeed> feedMap;
	private DataCache imageCache;
	private ChatHandler chatHandler;
	private HashMap<String, FBUser> userMap;
	private Vector<String> friendIdList;

	public FBFactory(RequestQueue requestQueue, FBClient fbClient) {
		this.requestQueue = requestQueue;
		this.fbClient = fbClient;

		feedMap = new HashMap<String, FBFeed>();
		imageCache = new DataCache(1024000);
		chatHandler = new ChatHandler();
		userMap = new HashMap<String, FBUser>();
		friendIdList = new Vector<String>();
	}

	public FBBitmap getBitmap(String url) {
		return new FBBitmap(imageCache, url);
	}

	public FBChat getChat(FBChat.Observer observer) {
		return new FBChat(chatHandler, fbClient, userMap, observer);
	}

	public FBFeed getFeed(String path) {
		FBFeed feed = feedMap.get(path);
		if (feed == null) {
			feed = new FBFeed(fbClient, path);
			feedMap.put(path, feed);
		}
		return feed;
	}

	public FBFriendList getFriendList() {
		return new FBFriendList(fbClient, userMap, friendIdList);
	}

	public FBUser getUser(String id) {
		FBUser user = userMap.get(id);
		if (user == null) {
			user = new FBUser(fbClient, id);
			userMap.put(id, user);
		}
		return user;
	}

	public void reset() {
		feedMap.clear();
		requestQueue.removeRequests();
		chatHandler.disconnect();
		userMap.clear();
		friendIdList.clear();		
	}

}
