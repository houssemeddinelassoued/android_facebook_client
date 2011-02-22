package fi.harism.facebook.request;

import android.app.Activity;
import android.os.Bundle;

/**
 * Base class for all requests being handled by RequestController.
 * 
 * @author harism
 */
public abstract class Request implements Runnable {

	// Boolean for marking execution stopped state.
	private boolean executionStopped = false;
	// Activity we execute runOnUiThread on.
	private Activity activity = null;
	// Data stored among with this Request.
	private Bundle bundle = null;

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
	public final synchronized void execute() {
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
				wait();
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * Returns Bundle stored with this request.
	 * 
	 * @see setBundle(Bundle bundle)
	 * @return Bundle stored with this Request.
	 */
	public final Bundle getBundle() {
		return bundle;
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
		// execute() method which is possible waiting for it.
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

	/**
	 * Sets Bundle to be stored with this Request.
	 * 
	 * @see getBundle()
	 * @param bundle
	 *            Bundle to be stored with this Request.
	 */
	public final void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}

	/**
	 * Marks this Request as stopped.
	 */
	public final synchronized void stop() {
		executionStopped = true;
		notify();
	}
}
