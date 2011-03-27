package fi.harism.facebook.chat;

/**
 * Exception which tells if its ok to try reconnecting to chat server.
 * 
 * @author harism
 */
public class ChatException extends Exception {

	private static final long serialVersionUID = -1576060033936490153L;
	private boolean reconnectingAllowed;

	/**
	 * Default constructor.
	 * 
	 * @param cause
	 * @param reconnectingAllowed
	 */
	public ChatException(String cause, boolean reconnectingAllowed) {
		super(cause);
		this.reconnectingAllowed = reconnectingAllowed;
	}

	/**
	 * Returns flag indicating whether reconnecting is allowed.
	 * 
	 * @return True if its ok to try reconnecting.
	 */
	public boolean reconnectingAllowed() {
		return reconnectingAllowed;
	}

}
