package fi.harism.facebook.net;

/**
 * Facebook API login observer.
 * 
 * @author harism
 */
public interface FacebookLoginObserver {

	/**
	 * Called if user cancels login dialog.
	 */
	public void onCancel();

	/**
	 * Called after login has succeeded.
	 */
	public void onComplete();

	/**
	 * Called on error situations.
	 * 
	 * @param error
	 *            Exception causing failure.
	 */
	public void onError(Exception error);

}
