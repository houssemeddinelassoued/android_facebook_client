package fi.harism.facebook.request;

import android.app.Activity;

/**
 * Helper class which splits Request into two methods. execute() is called from
 * separate thread, and executeUI from UI thread.
 * 
 * @author harism
 */
public abstract class RequestUI extends Request {

	private Activity activity;
	private boolean isCancelled = false;

	/**
	 * 
	 * @param key
	 *            Key for identifying this Request.
	 * @param activity
	 *            Activity to execute runOnUiThread() on.
	 */
	public RequestUI(Object key, Activity activity) {
		super(key);
		this.activity = activity;
	}

	@Override
	public final void cancel() {
		isCancelled = true;
	}

	/**
	 * This method is called from separate Thread. Implement this to execute
	 * asynchronous code. If Exception is thrown request execution ends at once.
	 */
	public abstract void execute() throws Exception;

	/**
	 * This method is called always from UI thread. Implement this to update UI.
	 * Exception is null, or one thrown from execute() method.
	 */
	public abstract void executeUI(Exception ex);

	/**
	 * Returns boolean indicating if this Request has been canceled.
	 * 
	 * @return
	 */
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public final void run() {
		if (!isCancelled) {
			Exception ex = null;
			try {
				execute();
			} catch (Exception e) {
				ex = e;
			} finally {
				activity.runOnUiThread(new RunnableUI(ex));
			}
		}
	}

	/**
	 * Runnable for UI thread execution.
	 * 
	 * @author harism
	 */
	private class RunnableUI implements Runnable {

		private Exception mEx;

		public RunnableUI(Exception ex) {
			mEx = ex;
		}

		@Override
		public void run() {
			if (!isCancelled) {
				executeUI(mEx);
			}
		}
	}

}
