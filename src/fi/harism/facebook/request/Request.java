package fi.harism.facebook.request;

import android.app.Activity;

public abstract class Request implements Runnable {
	
	private final static int EXECUTION_NOT_STARTED = 0;
	private final static int EXECUTION_THREAD = 1;
	private final static int EXECUTION_UI_THREAD = 2;
	private final static int EXECUTION_STOPPED = 3;
	
	private int executionState;
	private Activity activity = null;

	public Request(Activity activity) {
		this.activity = activity;
		executionState = EXECUTION_NOT_STARTED;
	}

	public final void stop() {
		executionState = EXECUTION_STOPPED;
	}

	public final boolean hasStopped() {
		return executionState == EXECUTION_UI_THREAD
				|| executionState == EXECUTION_STOPPED;
	}

	public final void run() {
		switch (executionState) {
		case EXECUTION_NOT_STARTED:
			executionState = EXECUTION_THREAD;
			try {
				runOnThread();
				activity.runOnUiThread(this);
			} catch (Exception ex) {
				stop();
				processNextRequest();
			}
			break;
		case EXECUTION_THREAD:
			executionState = EXECUTION_UI_THREAD;
			try {
				runOnUiThread();
			} catch (Exception ex) {
			}
			stop();
			processNextRequest();
			break;
		}
	}

	private final void processNextRequest() {
		RequestController.getRequestController().processNextRequest();
	}

	public abstract void runOnThread() throws Exception;

	public abstract void runOnUiThread() throws Exception;
}
