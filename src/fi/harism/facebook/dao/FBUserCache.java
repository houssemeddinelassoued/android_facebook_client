package fi.harism.facebook.dao;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import fi.harism.facebook.request.Request;

public class FBUserCache {

	private FBStorage fbStorage;
	private static final String SELECT = " uid, name, pic_square, affiliations, birthday,sex, hometown_location, current_location, status, website, email ";

	public FBUserCache(FBStorage fbStorage) {
		this.fbStorage = fbStorage;
	}

	public void pause() {
		fbStorage.requestQueue.setPaused(this, true);
	}

	public void resume() {
		fbStorage.requestQueue.setPaused(this, false);
	}

	public void cancel() {
		fbStorage.requestQueue.removeRequests(this);
	}

	public FBUser getUser(String userId) throws Exception {
		
		if (fbStorage.userMap.containsKey(userId)) {
			return fbStorage.userMap.get(userId);
		}

		String uid = userId;
		if (userId.equals("me")) {
			uid = "me()";
		}
		StringBuilder query = new StringBuilder();
		query.append("SELECT");
		query.append(SELECT);
		query.append("FROM user WHERE uid = ");
		query.append(uid);

		JSONObject resp = fbStorage.fbClient.requestFQL(query.toString());
		JSONArray data = resp.getJSONArray("data");
		if (data.length() != 1) {
			throw new Exception("Received more than 1 user information.");
		}
		JSONObject userObj = data.getJSONObject(0);
		FBUser user = createUser(userObj);

		fbStorage.userMap.put(userId, user);
		return user;
	}

	public void getUser(String userId, FBObserver<FBUser> observer) {
		if (fbStorage.userMap.containsKey(userId)) {
			observer.onComplete(fbStorage.userMap.get(userId));
		} else {
			UserRequest request = new UserRequest(this, userId, observer);
			fbStorage.requestQueue.addRequest(request);
		}
	}

	public Vector<FBUser> getFriends() throws Exception {
		
		Vector<FBUser> list = new Vector<FBUser>();
		
		if (fbStorage.friendIdList.size() > 0) {
			for (String id : fbStorage.friendIdList) {
				list.add(fbStorage.userMap.get(id));
			}
			return list;
		}

		StringBuilder query = new StringBuilder();
		query.append("SELECT");
		query.append(SELECT);
		query.append("FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = me())");
		
		JSONObject resp = fbStorage.fbClient.requestFQL(query.toString());
		JSONArray data = resp.getJSONArray("data");
		
		for (int i=0; i<data.length(); ++i) {
			JSONObject userObj = data.getJSONObject(i);
			FBUser user = createUser(userObj);
			list.add(user);
			fbStorage.friendIdList.add(user.getId());
			fbStorage.userMap.put(user.getId(), user);
		}
		
		return list;
	}

	public void getFriends(FBObserver<Vector<FBUser>> observer) {
		if (fbStorage.friendIdList.size() > 0) {
			try {
				observer.onComplete(getFriends());
			} catch (Exception ex) {
				observer.onError(ex);
			}
		} else {
			FriendsRequest request = new FriendsRequest(this, observer);
			fbStorage.requestQueue.addRequest(request);
		}
	}

	private FBUser createUser(JSONObject userObj) throws Exception {

		FBUser user = new FBUser();
		user.setId(userObj.getString("uid"));
		user.setName(userObj.getString("name"));
		user.setPicture(userObj.getString("pic_square"));

		JSONObject status = userObj.optJSONObject("status");
		if (status != null) {
			user.setStatus(status.getString("message"));
		}

		return user;
	}

	private class UserRequest extends Request {

		private String userId;
		private FBObserver<FBUser> observer;

		public UserRequest(Object key, String userId,
				FBObserver<FBUser> observer) {
			super(key);
			this.userId = userId;
			this.observer = observer;
		}

		@Override
		public void run() {
			try {
				FBUser user = getUser(userId);
				observer.onComplete(user);
			} catch (Exception ex) {
				observer.onError(ex);
			}
		}

		@Override
		public void stop() {
			// TODO:
		}

	}
	
	private class FriendsRequest extends Request {

		private FBObserver<Vector<FBUser>> observer;

		public FriendsRequest(Object key,
				FBObserver<Vector<FBUser>> observer) {
			super(key);
			this.observer = observer;
		}

		@Override
		public void run() {
			try {
				Vector<FBUser> friends = getFriends();
				observer.onComplete(friends);
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
