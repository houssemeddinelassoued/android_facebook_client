package fi.harism.facebook.dao;

import android.app.Activity;
import android.os.Bundle;
import fi.harism.facebook.request.Request;

public class FBSession {

	private FBStorage fbStorage;
	private String sessionKey = null;
	private String sessionSecret = null;

	public FBSession(FBStorage fbStorage) {
		this.fbStorage = fbStorage;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public String getSessionSecret() {
		return sessionSecret;
	}

	public void load() throws Exception {
		Bundle params = new Bundle();
		params.putString("method", "auth.promoteSession");
		String secret = fbStorage.fbClient.request(params);
		// Access token is a string of form aaaa|bbbb|cccc
		// where bbbb is session key.
		String[] split = fbStorage.fbClient.getAccessToken().split("\\|");
		if (split.length != 3) {
			// It is possible FB changes access token eventually.
			throw new Exception("Malformed access token.");
		}
		sessionKey = split[1];
		sessionSecret = secret.replace("\"", "");
	}

	public void load(Activity activity, FBObserver<FBSession> observer) {
		if (sessionKey == null || sessionSecret == null) {
			SessionRequest request = new SessionRequest(activity, this, observer);
			fbStorage.requestQueue.addRequest(request);
		}
		else
		{
			observer.onComplete(this);
		}
	}

	private class SessionRequest extends Request {

		private FBSession parent;
		private FBObserver<FBSession> observer;

		public SessionRequest(Activity activity, FBSession parent,
				FBObserver<FBSession> observer) {
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
