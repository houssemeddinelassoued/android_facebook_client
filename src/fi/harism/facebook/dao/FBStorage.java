package fi.harism.facebook.dao;

import java.util.HashMap;
import java.util.Vector;

import fi.harism.facebook.chat.ChatHandler;
import fi.harism.facebook.net.FBClient;
import fi.harism.facebook.request.RequestQueue;
import fi.harism.facebook.util.DataCache;

public class FBStorage {
	
	public RequestQueue requestQueue;
	public FBClient fbClient;
	
	public Vector<FBFriend> friendList = null;
	public Vector<FBFeedItem> newsFeedList = null;
	public Vector<FBFeedItem> profileFeedList = null;
	
	public DataCache imageCache;
	
	public ChatHandler chatHandler;
	
	public HashMap<String, FBUser> userMap;
	
	public FBStorage(RequestQueue requestQueue, FBClient fbClient) {
		this.requestQueue = requestQueue;
		this.fbClient = fbClient;
		
		imageCache = new DataCache(1024000);
		
		friendList = new Vector<FBFriend>();
		newsFeedList = new Vector<FBFeedItem>();
		profileFeedList = new Vector<FBFeedItem>();
		
		chatHandler = new ChatHandler();
		
		userMap = new HashMap<String, FBUser>();
	}
	
	public void reset() {
		friendList.removeAllElements();
		newsFeedList.removeAllElements();
		profileFeedList.removeAllElements();
		
		requestQueue.removeAllRequests();
		
		chatHandler.disconnect();
		
		userMap.clear();
	}

}
