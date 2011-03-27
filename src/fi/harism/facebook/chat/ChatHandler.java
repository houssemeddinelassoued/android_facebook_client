package fi.harism.facebook.chat;

import java.util.HashMap;
import java.util.Vector;

/**
 * Utility class for helping handle chat connection. This class supports
 * multiple observers and keeps an list of active users while left running in
 * the background.<br>
 * <br>
 * Also events are simplified as they are converted to ChatUser instead of using
 * JID only.
 * 
 * @author harism
 */
public class ChatHandler {

	private ChatConnection chatConnection;
	private Vector<ChatObserver.Handler> observers;
	private HashMap<String, ChatUser> userMap;
	private ChatLogger logger;
	private ChatConnectionObserver chatObserver;

	/**
	 * Default constructor.
	 */
	public ChatHandler() {
		logger = new ChatLogger();
		chatObserver = new ChatConnectionObserver();
		chatConnection = new ChatConnection(chatObserver, logger);
		observers = new Vector<ChatObserver.Handler>();
		userMap = new HashMap<String, ChatUser>();
	}

	public void addObserver(ChatObserver.Handler observer) {
		if (!observers.contains(observer)) {
			observers.add(observer);
		}
	}

	public void connect(String sessionKey, String sessionSecret) {
		chatConnection.connect(sessionKey, sessionSecret);
	}

	public void disconnect() {
		chatConnection.disconnect();
		userMap.clear();
	}

	public String getLog() {
		return logger.toString();
	}

	public ChatUser getUser(String jid) {
		return userMap.get(jid);
	}

	public Vector<ChatUser> getUsers() {
		return new Vector<ChatUser>(userMap.values());
	}

	public void removeObserver(ChatObserver.Handler observer) {
		while (observers.removeElement(observer))
			;
	}

	public void sendMessage(ChatUser to, String message) {
		chatConnection.sendMessage(to.getJID(), message);
	}

	private class ChatConnectionObserver implements ChatObserver.Connection {

		@Override
		public void onConnected() {
			for (ChatObserver.Handler observer : observers) {
				observer.onConnected();
			}
		}

		@Override
		public void onDisconnected() {
			for (ChatObserver.Handler observer : observers) {
				observer.onDisconnected();
			}
		}

		@Override
		public void onError(Exception ex) {
			for (ChatObserver.Handler observer : observers) {
				observer.onDisconnected();
			}
		}

		@Override
		public void onMessage(String jid, String message) {
			ChatUser user = userMap.get(jid);
			if (user != null) {
				for (ChatObserver.Handler observer : observers) {
					observer.onMessage(user, message);
				}
			}
		}

		@Override
		public void onPresenceChanged(String jid, String presence) {
			ChatUser.Presence p = ChatUser.Presence.AWAY;
			if (presence.equals("chat")) {
				p = ChatUser.Presence.CHAT;
			} else if (presence.equals("gone")) {
				p = ChatUser.Presence.GONE;
			}

			ChatUser user = new ChatUser(jid, p);

			if (p == ChatUser.Presence.GONE) {
				userMap.remove(jid);
			} else {
				userMap.put(jid, user);
			}

			for (ChatObserver.Handler observer : observers) {
				observer.onPresenceChanged(user);
			}

		}
	}

}
