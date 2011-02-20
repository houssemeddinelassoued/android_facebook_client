package fi.harism.facebook.request;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import fi.harism.facebook.util.FacebookController;

public class FacebookRequest extends Request {

	private String requestPath;
	private Bundle requestBundle;
	private Observer observer;
	private JSONObject response;

	public FacebookRequest(Activity activity, Request.Observer requestObserver,
			String requestPath, Bundle requestBundle, Observer observer) {
		super(activity, requestObserver);
		this.requestPath = requestPath;
		this.requestBundle = requestBundle;
		this.observer = observer;
	}

	public FacebookRequest(Activity activity, Request.Observer requestObserver,
			String requestPath, Observer observer) {
		super(activity, requestObserver);
		this.requestPath = requestPath;
		this.requestBundle = null;
		this.observer = observer;
	}

	public JSONObject getJSONObject() {
		return response;
	}

	@Override
	public void runOnThread() throws Exception {
		try {
			String r;
			FacebookController c = FacebookController.getFacebookController();
			if (requestBundle != null) {
				r = c.request(requestPath, requestBundle);
			} else {
				r = c.request(requestPath);
			}

			response = new JSONObject(r);

			if (response.has("error")) {
				JSONObject err = response.getJSONObject("error");
				Exception ex = new Exception(err.getString("type") + " : "
						+ err.getString("message"));
				throw ex;
			}
		} catch (Exception ex) {
			observer.onError(ex);
			throw ex;
		}
	}

	@Override
	public void runOnUiThread() throws Exception {
		observer.onComplete(this);
	}

	public interface Observer {
		public void onComplete(FacebookRequest facebookRequest);

		public void onError(Exception ex);
	}

}
