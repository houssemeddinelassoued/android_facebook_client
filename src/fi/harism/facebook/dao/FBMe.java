package fi.harism.facebook.dao;

import org.json.JSONObject;

import fi.harism.facebook.net.FBClient;
import fi.harism.facebook.request.Request;

import android.app.Activity;
import android.os.Bundle;

public class FBMe {

	private FBStorage fbStorage;
	
	public FBMe(FBStorage fbStorage) {
		this.fbStorage = fbStorage;
	}
	
	public String getId() {
		return fbStorage.userId;
	}
	
	public String getName() {
		return fbStorage.userName;
	}
	
	public String getPictureUrl() {
		return fbStorage.userPictureUrl;
	}
	
	public String getStatus() {
		return fbStorage.userStatus;
	}
	
	public void setPaused(boolean paused) {
		fbStorage.requestQueue.setPaused(this, paused);
	}
	
	public void cancel() {
		fbStorage.requestQueue.removeRequests(this);
	}
	
	public void load() throws Exception {
		if (fbStorage.userId == null) {
			Bundle params = new Bundle();
			params.putString(FBClient.TOKEN, fbStorage.fbClient.getAccessToken());
			params.putString("fields", "id,name,picture");
			JSONObject resp = fbStorage.fbClient.request("me", params);
			String id = resp.getString("id");
			String name = resp.getString("name");
			String picture = resp.getString("picture");
		
			params = new Bundle();
			params.putString(FBClient.TOKEN, fbStorage.fbClient.getAccessToken());
			params.putString("limit", "1");
			params.putString("fields", "message");
			String status;
			try {
				resp = fbStorage.fbClient.request("me/statuses", params);
				status = resp.getJSONArray("data").getJSONObject(0).getString("message");
			} catch (Exception ex) {
				status = "Status error: " + ex.getLocalizedMessage();
			}
			
			fbStorage.userId = id;
			fbStorage.userName = name;
			fbStorage.userPictureUrl = picture;
			fbStorage.userStatus = status;
		}		
	}
	
	public void load(Activity activity, final FBObserver<FBMe> observer) {
		if (fbStorage.userId != null) {
			final FBMe self = this;
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					observer.onComplete(self);
				}
			});
		} else {
			MeRequest request = new MeRequest(activity, this, observer);
			fbStorage.requestQueue.addRequest(request);
		}
	}
	
	private class MeRequest extends Request {
		private FBMe parent;
		private FBObserver<FBMe> observer;

		public MeRequest(Activity activity, FBMe parent, FBObserver<FBMe> observer) {
			super(activity, parent);
			this.parent = parent;
			this.observer = observer;
		}

		@Override
		public void runOnThread() throws Exception {
			try {
				load();
			} catch (Exception ex) {
				observer.onError(ex);
				throw ex;
			}
		}

		@Override
		public void runOnUiThread() throws Exception {
			observer.onComplete(parent);
		}
		
	}
	
}
