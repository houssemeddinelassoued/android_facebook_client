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
	
	//public Vector<FBPost> newsFeedList = null;
	//public Vector<FBPost> profileFeedList = null;
	public HashMap<String, FBFeed> feedMap;
	
	public DataCache imageCache;
	
	public ChatHandler chatHandler;
	
	public HashMap<String, FBUser> userMap;
	public Vector<String> friendIdList;
	
	public FBStorage(RequestQueue requestQueue, FBClient fbClient) {
		this.requestQueue = requestQueue;
		this.fbClient = fbClient;
		
		imageCache = new DataCache(1024000);
		
		//newsFeedList = new Vector<FBPost>();
		//profileFeedList = new Vector<FBPost>();
		feedMap = new HashMap<String, FBFeed>();
		
		chatHandler = new ChatHandler();
		
		userMap = new HashMap<String, FBUser>();
		friendIdList = new Vector<String>();
	}
	
	public void reset() {
		//newsFeedList.clear();
		//profileFeedList.clear();
		feedMap.clear();
		
		requestQueue.removeRequests();
		
		chatHandler.disconnect();
		
		userMap.clear();
		friendIdList.clear();
	}

}
