package fi.harism.facebook.dao;

import fi.harism.facebook.net.FBClient;
import fi.harism.facebook.request.RequestQueue;

public class FBFactory {
	
	private FBStorage fbStorage;

	public FBFactory(RequestQueue requestQueue, FBClient fbClient) {
		fbStorage = new FBStorage(requestQueue, fbClient);
	}
	
	public void reset() {
		fbStorage.reset();
	}
	
	public FBCommentList getCommentList(String itemId) {
		return new FBCommentList(fbStorage, itemId);
	}
	
	public FBFeedList getNewsFeed() {
		return new FBFeedList(fbStorage, FBFeedList.NEWS_FEED, fbStorage.newsFeedList);
	}
	
	public FBFeedList getProfileFeed() {
		return new FBFeedList(fbStorage, FBFeedList.PROFILE_FEED, fbStorage.profileFeedList);
	}
	
	public FBBitmap getBitmap() {
		return new FBBitmap(fbStorage);
	}
	
	public FBFriendList getFriendList() {
		return new FBFriendList(fbStorage);
	}
	
	public FBChat getChat(FBChat.Observer observer) {
		return new FBChat(fbStorage, observer);
	}
	
	public FBUserMap getUserMap() {
		return new FBUserMap(fbStorage);
	}
	
}
