package fi.harism.facebook.request;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import fi.harism.facebook.data.FacebookController;

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
	// FacebookController instance.
	private FacebookController facebookController;
	// Reuqest observer.
	private FacebookRequest.Observer observer;
	// Response from server.
	private JSONObject response;

	/**
	 * Constructor for Facebook request.
	 * 
	 * @param activity
	 *            Activity used for identifying this request.
	 * @param requestPath
	 *            Facebook Graph API path.
	 * @param requestBundle
	 *            Facebook Graph API parameters.
	 * @param facebookController
	 * 		      FacebookController instance.
	 * @param observer
	 *            Facebook request observer.
	 */
	public FacebookRequest(Activity activity, String requestPath,
			Bundle requestBundle, FacebookController facebookController, FacebookRequest.Observer observer) {
		super(activity);
		this.requestPath = requestPath;
		this.requestBundle = requestBundle;
		this.facebookController = facebookController;
		this.observer = observer;
		response = null;
	}

	/**
	 * Returns response from the server as JSONObject if this request has been
	 * completed successfully, null otherwise.
	 * 
	 * @return Response we received from the server.
	 */
	public JSONObject getResponse() {
		return response;
	}

	@Override
	public void runOnThread() throws Exception {
		try {
			String r;
			if (requestBundle != null) {
				r = facebookController.request(requestPath, requestBundle);
			} else {
				r = facebookController.request(requestPath);
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
