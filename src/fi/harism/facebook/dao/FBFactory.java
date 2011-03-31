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

	public FBFeed getFeed(String path) {
		FBFeed feed = fbStorage.feedMap.get(path);
		if (feed == null) {
			feed = new FBFeed(fbStorage.fbClient, path);
			fbStorage.feedMap.put(path, feed);
		}
		return feed;
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
			user = new FBUser(id);
			fbStorage.userMap.put(id, user);
		}
		return user;
	}

	public FBFriendList getFriendList() {
		return new FBFriendList(fbStorage.fbClient, fbStorage.userMap,
				fbStorage.friendIdList);
	}

}
