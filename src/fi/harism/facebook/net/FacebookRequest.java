package fi.harism.facebook.net;

import org.json.JSONObject;

import fi.harism.facebook.LoginActivity;
import android.app.Activity;
import android.os.Bundle;

public class FacebookRequest extends Request {

	private String path;
	private Bundle bundle;
	private FacebookRequestObserver observer;
	private JSONObject response;

	public FacebookRequest(Activity activity, String path,
			FacebookRequestObserver observer) {
		super(activity);
		this.path = path;
		this.bundle = null;
		this.observer = observer;
	}

	public FacebookRequest(Activity activity, String path, Bundle bundle,
			FacebookRequestObserver observer) {
		super(activity);
		this.path = path;
		this.bundle = bundle;
		this.observer = observer;
	}

	@Override
	public void runOnThread() throws Exception {
		try {
			String r;
			if (bundle != null) {
				r = LoginActivity.facebook.request(path, bundle);
			} else {
				r = LoginActivity.facebook.request(path);
			}

			response = new JSONObject(r);

			if (response.has("error")) {
				JSONObject err = response.getJSONObject("error");
				Exception ex = new Exception(err.getString("type") + " : "
						+ err.getString("message"));
				throw ex;
			}
		} catch (Exception ex) {
			observer.requestError(ex);
			throw ex;
		}
	}

	@Override
	public void runOnUiThread() throws Exception {
		observer.requestDone(response);
	}

}
