package fi.harism.facebook.request;

import org.json.JSONObject;

import android.os.Bundle;
import fi.harism.facebook.BaseActivity;
import fi.harism.facebook.util.FacebookController;

/**
 * FacebookRequest class is implemented for making asynchronous Facebook Graph
 * API calls.
 * 
 * @author harism
 */
public class FacebookRequest extends Request {

	// Facebook Graph API path.
	private String requestPath;
	// Facebook Graph API parameters.
	private Bundle requestBundle;
	// Observer.
	private FacebookRequest.Observer observer;
	// Response from server.
	private JSONObject response;
	// We need Activity to retrieve FacebookController instance.
	private BaseActivity baseActivity;

	/**
	 * Constructor for Facebook request.
	 * 
	 * @param activity
	 *            Activity needed for
	 * @param requestObserver
	 *            Request observer for base class.
	 * @param requestPath
	 *            Facebook Graph API path.
	 * @param requestBundle
	 *            Facebook Graph API parameters.
	 * @param observer
	 *            Facebook request observer.
	 */
	public FacebookRequest(BaseActivity activity,
			Request.Observer requestObserver, String requestPath,
			Bundle requestBundle, FacebookRequest.Observer observer) {
		super(activity, requestObserver);
		this.requestPath = requestPath;
		this.requestBundle = requestBundle;
		this.observer = observer;
		this.baseActivity = activity;
		response = null;
	}

	/**
	 * Returns response from the server as JSONObject if this request has been
	 * completed successfully, null otherwise.
	 * 
	 * @return Response we received from the server.
	 */
	public JSONObject getJSONObject() {
		return response;
	}

	@Override
	public void runOnThread() throws Exception {
		try {
			String r;
			// Get application wide instance of FacebookController.
			FacebookController c = baseActivity.getGlobalState()
					.getFacebookController();
			// Make actual request.
			if (requestBundle != null) {
				r = c.request(requestPath, requestBundle);
			} else {
				r = c.request(requestPath);
			}

			// Create JSONObject from response string.
			response = new JSONObject(r);

			// Check if response is an error JSONObject.
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

	/**
	 * Interface for listening to FacebookRequest.
	 */
	public interface Observer {
		/**
		 * Called once FacebookRequest finishes successfully.
		 * 
		 * @param facebookRequest
		 *            FacebookRequest that was completed.
		 */
		public void onComplete(FacebookRequest facebookRequest);

		/**
		 * Called once FacebookRequest fails.
		 * 
		 * @param ex
		 *            Exception containing reason for error.
		 */
		public void onError(Exception ex);
	}
}
