package fi.harism.facebook.dao;

import org.json.JSONArray;
import org.json.JSONObject;

import fi.harism.facebook.request.Request;

public class FBUserMap {
	
	FBStorage fbStorage;
	
	public FBUserMap(FBStorage fbStorage) {
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
		} else {
			String uid = userId;
			if (userId.equals("me")) {
				uid = "me()";
			}			
			StringBuilder query = new StringBuilder();
			query.append("SELECT uid, name, pic_square, affiliations, ");
			query.append("birthday,sex, hometown_location, current_location, ");
			query.append("status, website,email FROM user WHERE uid = ");
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
	}
	
	public void getUser(String userId, FBObserver<FBUser> observer) {
		if (fbStorage.userMap.containsKey(userId)) {
			observer.onComplete(fbStorage.userMap.get(userId));
		} else {
			UserRequest request = new UserRequest(this, userId, observer);
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

		public UserRequest(Object key, String userId, FBObserver<FBUser> observer) {
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

}
