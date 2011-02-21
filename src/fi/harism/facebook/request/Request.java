package fi.harism.facebook.request;

import android.app.Activity;
import android.os.Bundle;

/**
 * Base class for all requests being handled by RequestController. Request uses
 * its run() method in two ways. First it is called from a Thread created by
 * RequestController once this Request is being handled. Once run() method
 * finishes it triggers a new request by calling Activity.runOnUiThread(). This
 * causes another call to run() method eventually.
 * 
 * @author harism
 */
public abstract class Request implements Runnable {

	// Static integers for execution state.
	private final static int EXECUTION_NOT_STARTED = 0;
	private final static int EXECUTION_THREAD = 1;
	private final static int EXECUTION_UI_THREAD = 2;
	private final static int EXECUTION_STOPPED = 3;

	private int executionState;

	// Activity we execute runOnUiThread.
	private Activity activity = null;
	// Request.Observer.
	private Request.Observer observer = null;
	// Data stored among with this Request.
	private Bundle bundle = null;

	/**
	 * Constructor for Request objects.
	 * 
	 * @param activity
	 *            Activity we run runOnUiThread on.
	 * @param observer
	 *            Request observer.
	 */
	public Request(Activity activity, Request.Observer observer) {
		this.activity = activity;
		this.observer = observer;
		executionState = EXECUTION_NOT_STARTED;
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

	/**
	 * Method for checking if this Request is done with its execution already.
	 * 
	 * @return True if this Request is stopped or done with its execution.
	 */
	public final boolean hasStopped() {
		return executionState == EXECUTION_UI_THREAD
				|| executionState == EXECUTION_STOPPED;
	}

	@Override
	public final void run() {
		switch (executionState) {
		case EXECUTION_NOT_STARTED:
			executionState = EXECUTION_THREAD;
			try {
				runOnThread();
				activity.runOnUiThread(this);
			} catch (Exception ex) {
				stop();
				observer.onComplete();
			}
			break;
		case EXECUTION_THREAD:
			executionState = EXECUTION_UI_THREAD;
			try {
				runOnUiThread();
			} catch (Exception ex) {
			}
			stop();
			observer.onComplete();
			break;
		}
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
	public final void stop() {
		executionState = EXECUTION_STOPPED;
	}

	/**
	 * Request.Observer interface.
	 */
	public interface Observer {
		/**
		 * Called if Request fails within Thread or once runOnUiThread is
		 * finished.
		 */
		public void onComplete();
	}
}
