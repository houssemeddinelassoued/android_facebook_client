package fi.harism.facebook.request;

import android.app.Activity;

/**
 * Base class for all requests being handled by RequestController.
 * 
 * @author harism
 */
public abstract class Request implements Runnable {

	// Predefined priority values.
	public static final int PRIORITY_HIGH = 600;
	public static final int PRIORITY_NORMAL = 400;
	public static final int PRIORITY_LOW = 200;
	
	// Boolean for marking execution stopped state.
	private boolean executionStopped = false;
	// Activity we execute runOnUiThread on.
	private Activity activity = null;
	// Priority for this Request.
	private int priority = PRIORITY_NORMAL;

	/**
	 * Constructor for Request objects.
	 * 
	 * @param activity
	 *            Activity we run runOnUiThread on.
	 */
	public Request(Activity activity) {
		this.activity = activity;
	}

	/**
	 * This method is supposed to be called from separate thread. At first
	 * abstract runOnThread method is called. After that this Request is sent to
	 * UI thread using Activity.runOnUiThread method. This method returns only
	 * after run() method has been called from UI thread.
	 */
	public final void execute() {
		// First check if execution should be stopped stopped.
		if (!executionStopped) {
			try {
				// Execute code meant to be ran on separate thread.
				runOnThread();
				// Send this Request to UI thread queue.
				activity.runOnUiThread(this);
			} catch (Exception ex) {
				// On error simply mark this Request as stopped.
				stop();
			}
		}
		// It is possible this Request has been marked as stopped at this point.
		// This might happen in a situation in which run() method has finished
		// before reaching this code. Let's wait only if execution has not
		// stopped.
		if (!executionStopped) {
			try {
				// Execution is blocked until notify() is called. This
				// should happen in run() method once UI thread handles our
				// request.
				synchronized (this) {
					wait();
				}
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * Getter for Request priority value.
	 * 
	 * @return Request priority value.
	 */
	public final int getPriority() {
		return priority;
	}

	@Override
	public final void run() {
		// Execute runOnUiThread only if this Request is still active.
		if (!executionStopped) {
			try {
				runOnUiThread();
			} catch (Exception ex) {
			}
		}
		// Mark this Request as done. Calling stop() also sends notification to
		// execute() method which is possibly waiting for it.
		stop();
	}

	/**
	 * Abstract method classes extending this base class have to implement. This
	 * method is called when Request is supposed to do execution within a
	 * Thread.
	 * 
	 * @throws Exception
	 */
	public abstract void runOnThread() throws Exception;

	/**
	 * Abstract method classes extending this base class have to implement. This
	 * method is called when Request is supposed to do execution within UI
	 * Thread.
	 * 
	 * @throws Exception
	 */
	public abstract void runOnUiThread() throws Exception;
	
	public final Activity getActivity() {
		return activity;
	}
	
	/**
	 * Setter for Request priority value. Default priority is PRIORITY_NORMAL.
	 * 
	 * @param priority
	 *            Priority for this Request.
	 */
	public final void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * Marks this Request as stopped.
	 */
	public final void stop() {
		executionStopped = true;
		synchronized (this) {
			notify();
		}
	}
}
