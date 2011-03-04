package fi.harism.facebook.request;

import java.util.ArrayList;

import android.os.Bundle;
import fi.harism.facebook.BaseActivity;

/**
 * RequestController provides a queue for handling Requests. Every
 * RequestController creates one WorkerThread which is used for executing one
 * Request asynchronously at time.
 * 
 * TODO: It might be a good idea to have one worker thread application wide.
 * 
 * @author harism
 */
public final class RequestController {

	// List of requests.
	private ArrayList<Request> requestList = null;
	// Activity this RequestController was created for.
	private BaseActivity activity = null;
	// WorkerThread for this RequestController instance.
	private WorkerThread workerThread = null;

	/**
	 * Constructor for RequestController.
	 * 
	 * @param activity
	 *            Activity this controller is created for.
	 */
	public RequestController(BaseActivity activity) {
		requestList = new ArrayList<Request>();
		this.activity = activity;
		workerThread = new WorkerThread();
		workerThread.start();
	}

	/**
	 * Adds new Request object to queue and starts processing it if there are no
	 * other Requests being ran at the time. Request is added to list of Request
	 * based on its priority value. Priority should be set before calling this
	 * method. Changing priority later does not have effect.
	 * 
	 * @param request
	 *            Request object to be added to queue.
	 */
	public final void addRequest(Request request) {
		int index = 0;
		while (index < requestList.size()
				&& requestList.get(index).getPriority() >= request
						.getPriority()) {
			++index;
		}
		requestList.add(index, request);
		synchronized (requestList) {
			requestList.notify();
		}
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
		FacebookRequest request = new FacebookRequest(activity, requestPath,
				requestBundle, observer);
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
		FacebookRequest request = new FacebookRequest(activity, requestPath,
				null, observer);
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
		ImageRequest request = new ImageRequest(activity, url, observer);
		return request;
	}

	/**
	 * Destroys all data related to this RequestController. This method should
	 * be called once Activity is being destroyed.
	 */
	public final void destroy() {
		workerThread.destroyWorker();
		workerThread = null;
		requestList.clear();
		requestList = null;
		activity = null;
	}

	/**
	 * Sets this RequestController to paused state. This means no new requests
	 * are not being processed until resume() is called.
	 * 
	 * @see resume()
	 */
	public final void pause() {
		workerThread.pauseWorker();
	}

	/**
	 * Continues processing requests within this RequestController.
	 * 
	 * @see pause()
	 */
	public final void resume() {
		workerThread.resumeWorker();
	}

	/**
	 * Private WorkerThread implementation.
	 */
	private final class WorkerThread extends Thread {
		// Boolean to indicate this worker is in paused mode.
		private boolean isPaused = false;
		// Boolean to indicate this worker should keep running.
		private boolean keepRunning = true;
		// Currently processed request.
		private Request currentRequest = null;

		/**
		 * Destroys this worker. Execution of run() will be ended as soon as
		 * possible, and if there is a Request being processed its execution is
		 * stopped.
		 */
		public void destroyWorker() {
			// Mark this worker as done.
			keepRunning = false;
			// Send notification in case run() method is in wait state.
			synchronized (requestList) {
				requestList.notify();
			}
			// If there is a current Request, stop its execution at once.
			if (currentRequest != null) {
				currentRequest.stop();
			}
		}

		/**
		 * Puts this worker into paused mode. If there is a Request being
		 * processed when this method is called, it is finished before worker
		 * enters paused state.
		 */
		public void pauseWorker() {
			isPaused = true;
		}

		/**
		 * Puts this worker back to running mode.
		 */
		public void resumeWorker() {
			isPaused = false;
			synchronized (requestList) {
				requestList.notify();
			}
		}

		@Override
		public void run() {
			// Lets keep looping until keepRunning is set to false.
			while (keepRunning) {
				synchronized (requestList) {
					// Let's idle here while isPaused is set, or requestList is
					// empty. We are expecting a notification being sent to us
					// once there is a change in execution state.
					while (isPaused || (keepRunning && requestList.isEmpty())) {
						try {
							// Wait until someone calls notify on requestList.
							requestList.wait();
						} catch (Exception ex) {
						}
					}
					// If keepRunning is true there should be a request
					// available for processing.
					if (keepRunning) {
						currentRequest = requestList.remove(0);
					}
				}
				// Lets check keepRunning again, and if it's set currentRequest
				// should contain next Request for processing.
				if (keepRunning) {
					// execute() returns only after Request has been processed
					// totally. This is the reason we have to call execute()
					// separately from previous synchronized block.
					// TODO: Try to figure out rationale for this requirement.
					// It's tempting to have these two lines moved inside
					// previous if block.
					currentRequest.execute();
					currentRequest = null;
				}
			}
		}
	}
}
