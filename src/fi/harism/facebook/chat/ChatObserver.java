package fi.harism.facebook.chat;

/**
 * Interfaces for chat events. Namely ChatObserver.Handler for ChatHandler and
 * ChatObserver.Connection for ChatConnection.
 * 
 * @author harism
 */
public interface ChatObserver {

	/**
	 * Observer for ChatConnection events.
	 * 
	 * @author harism
	 */
	public interface Connection {

		/**
		 * Called once chat connection has been established. After this event
		 * it's possible to send messages etc.
		 */
		public void onConnected();

		/**
		 * Called once chat connection has been disconnected.
		 */
		public void onDisconnected();

		/**
		 * Called on error situations.
		 * 
		 * @param ex
		 *            Exception causing the failure.
		 */
		public void onError(Exception ex);

		/**
		 * Called once currently logged in user receives a message.
		 * 
		 * @param jid
		 *            User JID from whom message is from.
		 * @param message
		 *            Message content.
		 */
		public void onMessage(String jid, String message);

		/**
		 * Called once currently logged in user's friend's presence changes.<br>
		 * <br>
		 * NOTE: On top of XMPP IM presence values: "away", "chat", "dnd" and
		 * "xa", chat connection sends also "gone" presence through this method.<br>
		 * 
		 * @param jid
		 *            User JID who's presence has changed.
		 * @param presence
		 *            New presence value.
		 */
		public void onPresenceChanged(String jid, String presence);

	}

	/**
	 * Observer for ChatHandler events.
	 * 
	 * @author harism
	 */
	public interface Handler {

		/**
		 * Called once connection procedure has been accomplished.
		 */
		public void onConnected();

		/**
		 * Called once connection has been closed.
		 */
		public void onDisconnected();

		/**
		 * Called once receiving message.
		 * 
		 * @param from
		 *            User who sent you a message.
		 * @param message
		 *            Message content.
		 */
		public void onMessage(ChatUser from, String message);

		/**
		 * Called once users presence has changed.
		 * 
		 * @param user
		 *            User who's presence has changed.
		 */
		public void onPresenceChanged(ChatUser user);
	}

}
