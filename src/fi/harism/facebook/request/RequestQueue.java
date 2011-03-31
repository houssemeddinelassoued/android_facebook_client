package fi.harism.facebook.request;

import java.util.ArrayList;

/**
 * RequestController provides a queue for handling Requests. Every
 * RequestController creates one WorkerThread which is used for executing one
 * Request asynchronously at time.
 * 
 * @author harism
 */
public final class RequestQueue {

	private ArrayList<Object> pausedList = null;
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
		pausedList = new ArrayList<Object>();
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

	/**
	 * Destroys all data related to this RequestController.
	 */
	public final void destroy() {
		workerThread.destroyWorker();
		workerThread = null;
		// Send notification in case run() method is in wait state.
		synchronized (requestList) {
			requestList.notify();
			requestList.clear();
			requestList = null;
		}
		// If there is a current Request, stop its execution at once.
		if (currentRequest != null) {
			currentRequest.cancel();
			currentRequest = null;
		}
	}
	
	/**
	 * Removes all requests from this queue.
	 */
	public final void removeRequests() {
		synchronized (requestList) {
			pausedList.clear();
			requestList.clear();
		}
		// If there is a current Request, stop its execution at once.
		if (currentRequest != null) {
			currentRequest.cancel();
			currentRequest = null;
		}
	}

	/**
	 * Removes all Requests with given key from this queue. Requests are removed
	 * quietly but if there is one being executed cancel() will be called on it.
	 * 
	 * @param key
	 */
	public final void removeRequests(Object key) {
		synchronized (requestList) {
			for (int i = 0; i < requestList.size();) {
				Request r = requestList.get(i);
				if (r.getKey() == key) {
					requestList.remove(i);
				} else {
					++i;
				}
			}
			// If there is a current Request, stop its execution at once.
			if (currentRequest != null && currentRequest.getKey() == key) {
				currentRequest.cancel();
				currentRequest = null;
			}
			pausedList.remove(key);
			requestList.notify();
		}
	}

	/**
	 * Sets Requests with given key to paused state or resumes them.
	 * 
	 * @param key
	 * @param paused
	 */
	public final void setPaused(Object key, boolean paused) {
		if (paused == true) {
			if (!pausedList.contains(key)) {
				pausedList.add(key);
			}
		} else {
			pausedList.remove(key);
		}
		synchronized (requestList) {
			requestList.notify();
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
						for (int i = 0; i < requestList.size(); ++i) {
							Request r = requestList.get(i);
							if (!pausedList.contains(r.getKey())) {
								currentRequest = requestList.remove(i);
								break;
							}
						}
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
					currentRequest.run();
					currentRequest = null;
				}
			}
		}
	}
}
