package fi.harism.facebook.request;

import java.util.ArrayList;

import android.os.Bundle;
import fi.harism.facebook.BaseActivity;

/**
 * RequestController class implements simple Thread queue for asynchronous
 * requests. Each request is handled in a new Thread but RequestController tries
 * to limit amount of concurrent Threads.
 * 
 * @author harism
 */
public final class RequestController implements Request.Observer {

	// List of requests.
	private ArrayList<Request> requests = null;
	// Current request.
	private Request currentRequest = null;
	// Activity this RequestController was created for.
	private BaseActivity activity = null;
	// Boolean for pausing RequestController.
	private boolean paused;

	/**
	 * Constructor for RequestController.
	 * 
	 * @param activity
	 *            Activity this controller is created for.
	 */
	public RequestController(BaseActivity activity) {
		requests = new ArrayList<Request>();
		this.activity = activity;
		paused = false;
	}

	/**
	 * Adds new Request object to queue and starts processing it if there are no
	 * other Requests being ran at the time.
	 * 
	 * @param request
	 *            Request object to be added to queue.
	 */
	public final void addRequest(Request request) {
		requests.add(request);
		processNextRequest();
	}

	/**
	 * Helper method for creating new Facebook request.
	 * 
	 * @param requestPath
	 *            Facebook Graph API path.
	 * @param requestBundle
	 *            Request parameters.
	 * @param observer
	 *            Observer for listening to this request.
	 * @return New FacebookRequest object.
	 */
	public final FacebookRequest createFacebookRequest(String requestPath,
			Bundle requestBundle, FacebookRequest.Observer observer) {
		FacebookRequest request = new FacebookRequest(activity, this,
				requestPath, requestBundle, observer);
		return request;
	}

	/**
	 * Helper method for creating new Facebook request.
	 * 
	 * @param requestPath
	 *            Facebook Graph API path.
	 * @param observer
	 *            Observer for listening to this request.
	 * @return New FacebookRequest object.
	 */
	public final FacebookRequest createFacebookRequest(String requestPath,
			FacebookRequest.Observer observer) {
		FacebookRequest request = new FacebookRequest(activity, this,
				requestPath, null, observer);
		return request;
	}

	/**
	 * Helper method for creating new Image request.
	 * 
	 * @param url
	 *            Image URL.
	 * @param observer
	 *            Observer for listening to this request.
	 * @return New ImageRequest object.
	 */
	public final ImageRequest createImageRequest(String url,
			ImageRequest.Observer observer) {
		ImageRequest request = new ImageRequest(activity, this, url, observer);
		return request;
	}

	/**
	 * Destroys all data related to this RequestController. This method should
	 * be called once Activity is being destroyed.
	 */
	public final void destroy() {
		requests.clear();
		requests = null;
		if (currentRequest != null) {
			currentRequest.stop();
			currentRequest = null;
		}
		activity = null;
	}

	@Override
	public void onComplete() {
		// If Request was created using helper methods within this class, this
		// method is called from Request once it's being finished.
		processNextRequest();
	}

	/**
	 * Sets this RequestController to paused state. This means no new requests
	 * are not being processed until resume() is called.
	 * 
	 * @see resume()
	 */
	public final void pause() {
		paused = true;
	}

	/**
	 * Continues processing requests within this RequestController.
	 * 
	 * @see pause()
	 */
	public final void resume() {
		paused = false;
		processNextRequest();
	}

	/**
	 * Private method for starting to process next request from queue.
	 */
	private final void processNextRequest() {
		if (!paused && (currentRequest == null || currentRequest.hasStopped())) {
			if (!requests.isEmpty()) {
				currentRequest = requests.remove(0);
				new Thread(currentRequest).start();
			}
		}
	}
}
