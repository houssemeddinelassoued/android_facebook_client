package fi.harism.facebook.dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import android.os.Bundle;
import fi.harism.facebook.chat.ChatHandler;
import fi.harism.facebook.chat.ChatObserver;
import fi.harism.facebook.chat.ChatUser;
import fi.harism.facebook.net.FBClient;

/**
 * Class for chat handling.
 * 
 * @author harism
 */
public class FBChat {

	// Actual chat implementation instance.
	private ChatHandler chatHandler;
	// FBClient instance.
	private FBClient fbClient;
	// User map.
	private HashMap<String, FBUser> userMap;
	// Our ChatHandler observer.
	private FBChatObserver chatObserver;
	// Client observer.
	private Observer observer;

	/**
	 * Default constructor.
	 * 
	 * @param chatHandler
	 * @param fbClient
	 * @param userMap
	 * @param observer
	 */
	FBChat(ChatHandler chatHandler, FBClient fbClient,
			HashMap<String, FBUser> userMap, Observer observer) {
		this.chatHandler = chatHandler;
		this.fbClient = fbClient;
		this.userMap = userMap;
		this.observer = observer;
		chatObserver = new FBChatObserver();
		chatHandler.addObserver(chatObserver);
	}

	/**
	 * This method should be called once owner is about to be destroyed.
	 */
	public void onDestroy() {
		chatHandler.removeObserver(chatObserver);
	}

	/**
	 * Returns log for debugging and implementation causes.
	 */
	public String getLog() {
		return chatHandler.getLog();
	}

	/**
	 * Starts connecting procedure.
	 */
	public void connect() throws IOException {
		Bundle params = new Bundle();
		params.putString("method", "auth.promoteSession");
		String secret = fbClient.request(params);
		// Access token is a string of form aaaa|bbbb|cccc
		// where bbbb is session key.
		String[] split = fbClient.getAccessToken().split("\\|");
		if (split.length != 3) {
			// It is possible FB changes access token eventually.
			throw new IOException("Malformed access token.");
		}
		String sessionKey = split[1];
		String sessionSecret = secret.replace("\"", "");

		chatHandler.connect(sessionKey, sessionSecret);
	}

	/**
	 * Returns list of users online currently. This is relevant information if
	 * underlying ChatConnection has been running while calling class has not
	 * been observing presence changes.
	 */
	public Vector<FBUser> getUsers() {
		Vector<ChatUser> users = chatHandler.getUsers();
		Vector<FBUser> out = new Vector<FBUser>();
		for (ChatUser user : users) {
			String jid = user.getJID();
			String id = jid.substring(0, jid.indexOf('@'));
			if (id.charAt(0) == '-') {
				id = id.substring(1);
			}
			FBUser u = userMap.get(id);
			if (u == null) {
				u = new FBUser(fbClient, id);
				userMap.put(id, u);
			}
			FBUser.Presence presence;
			switch (user.getPresence()) {
			case AWAY:
				presence = FBUser.Presence.AWAY;
				break;
			case CHAT:
				presence = FBUser.Presence.CHAT;
				break;
			default:
				presence = FBUser.Presence.GONE;
				break;
			}
			u.presence = presence;
			out.add(u);
		}
		return out;
	}

	/**
	 * Sends message to given user.
	 */
	public void sendMessage(FBUser to, String message) {
		ChatUser user = chatHandler.getUser(to.getJid());
		if (user != null) {
			chatHandler.sendMessage(user, message);
		}
	}

	/**
	 * Disconnects underlying ChatConnection.
	 */
	public void disconnect() {
		chatHandler.disconnect();
	}

	public interface Observer {
		public void onConnected();

		public void onDisconnected();

		public void onPresenceChanged(FBUser user);

		public void onMessage(FBUser from, String message);
	}

	private class FBChatObserver implements ChatObserver.Handler {

		@Override
		public void onConnected() {
			observer.onConnected();
		}

		@Override
		public void onDisconnected() {
			observer.onDisconnected();
		}

		@Override
		public void onPresenceChanged(ChatUser user) {
			String jid = user.getJID();
			String id = jid.substring(0, jid.indexOf('@'));
			if (id.charAt(0) == '-') {
				id = id.substring(1);
			}
			FBUser.Presence presence;
			switch (user.getPresence()) {
			case AWAY:
				presence = FBUser.Presence.AWAY;
				break;
			case CHAT:
				presence = FBUser.Presence.CHAT;
				break;
			default:
				presence = FBUser.Presence.GONE;
				break;
			}

			FBUser u = userMap.get(id);
			if (u == null) {
				u = new FBUser(fbClient, id);
				userMap.put(id, u);
			}

			u.jid = jid;
			u.presence = presence;
			observer.onPresenceChanged(u);
		}

		@Override
		public void onMessage(ChatUser from, String message) {
			String jid = from.getJID();
			String id = jid.substring(0, jid.indexOf('@'));
			if (id.charAt(0) == '-') {
				id = id.substring(1);
			}
			FBUser user = userMap.get(id);
			if (user != null) {
				observer.onMessage(user, message);
			}
		}
	}

}
