package fi.harism.facebook.net;

/**
 * Observer interface for asynchronous logout procedure.
 * 
 * @author harism
 */
public interface FacebookLogoutObserver {

	/**
	 * Called once logout has been successfully done.
	 */
	public void onComplete();

	/**
	 * This method is called on error situations.
	 * 
	 * @param error
	 *            Execption causing logout to fail.
	 */
	public void onError(Exception error);

}
