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
	
	public String userId = null;
	public String userName = null;
	public String userPictureUrl = null;
	public String userStatus = null;
	
	public Vector<FBFriend> friendList = null;
	public Vector<FBFeedItem> newsFeedList = null;
	public Vector<FBFeedItem> profileFeedList = null;
	
	public DataCache imageCache;
	
	public ChatHandler chatHandler;
	public FBSession fbSession;
	public HashMap<String, FBChatUser> chatUserMap;
	
	public FBStorage(RequestQueue requestQueue, FBClient fbClient) {
		this.requestQueue = requestQueue;
		this.fbClient = fbClient;
		
		imageCache = new DataCache(1024000);
		
		friendList = new Vector<FBFriend>();
		newsFeedList = new Vector<FBFeedItem>();
		profileFeedList = new Vector<FBFeedItem>();
		
		chatHandler = new ChatHandler();
		fbSession = new FBSession(this);
		chatUserMap = new HashMap<String, FBChatUser>();
	}
	
	public void reset() {
		userId = null;
		userName = null;
		userPictureUrl = null;
		
		friendList.removeAllElements();
		newsFeedList.removeAllElements();
		profileFeedList.removeAllElements();
		
		requestQueue.removeAllRequests();
		
		chatHandler.disconnect();
		fbSession = new FBSession(this);
		chatUserMap.clear();
	}

}
