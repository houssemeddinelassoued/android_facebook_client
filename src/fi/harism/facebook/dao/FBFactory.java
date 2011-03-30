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
		return new FBFeedList(fbStorage, FBFeedList.NEWS_FEED,
				fbStorage.newsFeedList);
	}

	public FBFeedList getProfileFeed() {
		return new FBFeedList(fbStorage, FBFeedList.PROFILE_FEED,
				fbStorage.profileFeedList);
	}

	public FBBitmap getBitmap(String url) {
		return new FBBitmap(fbStorage, url);
	}

	public FBChat getChat(FBChat.Observer observer) {
		return new FBChat(fbStorage, observer);
	}

	public FBUser getUser(String id) {
		FBUser user = fbStorage.userMap.get(id);
		if (user == null) {
			user = new FBUser(fbStorage.fbClient, id);
		}
		return user;
	}

	public FBFriendList getFriendList() {
		return new FBFriendList(fbStorage.fbClient, fbStorage.userMap,
				fbStorage.friendIdList);
	}

}
