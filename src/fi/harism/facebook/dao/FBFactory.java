package fi.harism.facebook.dao;

import java.util.HashMap;
import java.util.Vector;

import fi.harism.facebook.chat.ChatHandler;
import fi.harism.facebook.net.FBClient;
import fi.harism.facebook.request.RequestQueue;
import fi.harism.facebook.util.DataCache;

public class FBFactory {

	private RequestQueue mRequestQueue;
	private FBClient mFBClient;

	private HashMap<String, FBFeed> mFeedMap;
	private DataCache mImageCache;
	private ChatHandler mChatHandler;
	private HashMap<String, FBUser> mUserMap;
	private Vector<String> mFriendIdList;

	public FBFactory(RequestQueue requestQueue, FBClient fbClient) {
		mRequestQueue = requestQueue;
		mFBClient = fbClient;

		mFeedMap = new HashMap<String, FBFeed>();
		mImageCache = new DataCache(1024000);
		mChatHandler = new ChatHandler();
		mUserMap = new HashMap<String, FBUser>();
		mFriendIdList = new Vector<String>();
	}

	public FBBitmap getBitmap(String url) {
		return new FBBitmap(mImageCache, url);
	}

	public FBChat getChat(FBChat.Observer observer) {
		return new FBChat(mChatHandler, mFBClient, mUserMap, observer);
	}

	public FBFeed getFeed(String path) {
		FBFeed feed = mFeedMap.get(path);
		if (feed == null) {
			feed = new FBFeed(mFBClient, path);
			mFeedMap.put(path, feed);
		}
		return feed;
	}

	public FBFriendList getFriendList() {
		return new FBFriendList(mFBClient, mUserMap, mFriendIdList);
	}

	public FBUser getUser(String id) {
		FBUser user = mUserMap.get(id);
		if (user == null) {
			user = new FBUser(mFBClient, id);
			mUserMap.put(id, user);
		}
		return user;
	}

	public void reset() {
		mFeedMap.clear();
		mRequestQueue.removeRequests();
		mChatHandler.disconnect();
		mUserMap.clear();
		mFriendIdList.clear();		
	}

}
