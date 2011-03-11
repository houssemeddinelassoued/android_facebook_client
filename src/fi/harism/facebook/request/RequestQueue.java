package fi.harism.facebook.request;

import java.util.ArrayList;

import android.app.Activity;

/**
 * RequestController provides a queue for handling Requests. Every
 * RequestController creates one WorkerThread which is used for executing one
 * Request asynchronously at time.
 * 
 * @author harism
 */
public final class RequestQueue {

	private ArrayList<Activity> pausedList = null;
	// List of requests.
	private ArrayList<Request> requestList = null;
	// Currently processed request.
	private Request currentRequest = null;
	// WorkerThread for this RequestController instance.
	private WorkerThread workerThread = null;

	/**
	 * Constructor for RequestController.
	 * 
	 * @param activity
	 *            Activity this controller is created for.
	 */
	public RequestQueue() {
		pausedList = new ArrayList<Activity>();
		requestList = new ArrayList<Request>();
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
	
	public final void setPaused(Activity activity, boolean paused) {
		if (paused == true) {
			if (!pausedList.contains(activity)) {
				pausedList.add(activity);
			}
		}
		else {
			pausedList.remove(activity);
		}
		synchronized (requestList) {
			requestList.notify();
		}
	}
	
	public final void removeRequests(Activity activity) {
		for (int i=0; i<requestList.size();) {
			Request r = requestList.get(i);
			if (r.getActivity() == activity) {
				requestList.remove(i);
			}
			else {
				++i;
			}
		}
		// If there is a current Request, stop its execution at once.
		if (currentRequest != null && currentRequest.getActivity() == activity) {
			currentRequest.stop();
			currentRequest = null;
		}
		pausedList.remove(activity);
		synchronized (requestList) {
			requestList.notify();
		}
	}

	/**
	 * Destroys all data related to this RequestController. This method should
	 * be called once Activity is being destroyed.
	 */
	public final void destroy() {
		workerThread.destroyWorker();
		// Send notification in case run() method is in wait state.
		synchronized (requestList) {
			requestList.notify();
		}
		workerThread = null;
		requestList.clear();
		requestList = null;
		// If there is a current Request, stop its execution at once.
		if (currentRequest != null) {
			currentRequest.stop();
			currentRequest = null;
		}
	}

	/**
	 * Private WorkerThread implementation.
	 */
	private final class WorkerThread extends Thread {
		// Boolean to indicate this worker should keep running.
		private boolean keepRunning = true;

		/**
		 * Destroys this worker. Execution of run() will be ended as soon as
		 * possible, and if there is a Request being processed its execution is
		 * stopped.
		 */
		public void destroyWorker() {
			// Mark this worker as done.
			keepRunning = false;
		}

		@Override
		public void run() {
			// Lets keep looping until keepRunning is set to false.
			while (keepRunning) {
				synchronized (requestList) {
					// Let's idle here while isPaused is set, or requestList is
					// empty. We are expecting a notification being sent to us
					// once there is a change in execution state.
					while (keepRunning && requestList.isEmpty()) {
						try {
							// Wait until someone calls notify on requestList.
							requestList.wait();
						} catch (Exception ex) {
						}
					}
					// If keepRunning is true there should be a request
					// available for processing.
					if (keepRunning) {
						for (int i=0; i<requestList.size(); ++i) {
							Request r = requestList.get(i);
							if (!pausedList.contains(r.getActivity())) {
								currentRequest = requestList.remove(i);
								break;
							}
						}
						//currentRequest = requestList.remove(0);
					}
				}
				// Lets check keepRunning again, and if it's set currentRequest
				// should contain next Request for processing.
				if (keepRunning && currentRequest != null) {
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
