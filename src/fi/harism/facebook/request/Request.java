package fi.harism.facebook.request;

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
	
	private Object key = null;
	// Priority for this Request.
	private int priority = PRIORITY_NORMAL;

	/**
	 * Constructor for Request objects.
	 * 
	 * @param key
	 *            Identifier for this Request.
	 */
	public Request(Object key) {
		this.key = key;
	}

	public final Object getKey() {
		return key;
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
	public abstract void run();
	public abstract void stop();

	/**
	 * Setter for Request priority value. Default priority is PRIORITY_NORMAL.
	 * 
	 * @param priority
	 *            Priority for this Request.
	 */
	public final void setPriority(int priority) {
		this.priority = priority;
	}

}
