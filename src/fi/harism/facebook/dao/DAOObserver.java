package fi.harism.facebook.dao;

/**
 * Generic implementation for all DAO request observers.
 * 
 * @author harism
 * @param <T>
 *            Class this generic presents.
 */
public interface DAOObserver<T> {

	/**
	 * Called once request is complete. This method is always called from UI
	 * thread for Activity given as parameter when creating request.
	 * 
	 * @param response
	 *            DAO object for this request.
	 */
	public void onComplete(T response);

	/**
	 * Called if request fails. This method is always called from UI thread for
	 * Activity which was given as parameter when creating request.
	 * 
	 * @param error
	 *            Exception which caused this request to fail.
	 */
	public void onError(Exception error);
}
