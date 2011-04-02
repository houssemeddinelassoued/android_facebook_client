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
	private ChatHandler mChatHandler;
	// FBClient instance.
	private FBClient mFBClient;
	// User map.
	private HashMap<String, FBUser> mUserMap;
	// Our ChatHandler observer.
	private FBChatObserver mChatObserver;
	// Client observer.
	private Observer mObserver;

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
		mChatHandler = chatHandler;
		mFBClient = fbClient;
		mUserMap = userMap;
		mObserver = observer;
		mChatObserver = new FBChatObserver();
		chatHandler.addObserver(mChatObserver);
	}

	/**
	 * Starts connecting procedure.
	 */
	public void connect() throws IOException {
		Bundle params = new Bundle();
		params.putString("method", "auth.promoteSession");
		String secret = mFBClient.request(params);
		// Access token is a string of form aaaa|bbbb|cccc
		// where bbbb is session key.
		String[] split = mFBClient.getAccessToken().split("\\|");
		if (split.length != 3) {
			// It is possible FB changes access token eventually.
			throw new IOException("Malformed access token.");
		}
		String sessionKey = split[1];
		String sessionSecret = secret.replace("\"", "");

		mChatHandler.connect(sessionKey, sessionSecret);
	}

	/**
	 * Disconnects underlying ChatConnection.
	 */
	public void disconnect() {
		mChatHandler.disconnect();
	}

	/**
	 * Returns log for debugging and implementation causes.
	 */
	public String getLog() {
		return mChatHandler.getLog();
	}

	/**
	 * Returns list of users online currently. This is relevant information if
	 * underlying ChatConnection has been running while calling class has not
	 * been observing presence changes.
	 */
	public Vector<FBUser> getUsers() {
		Vector<ChatUser> users = mChatHandler.getUsers();
		Vector<FBUser> out = new Vector<FBUser>();
		for (ChatUser user : users) {
			String jid = user.getJID();
			String id = jid.substring(0, jid.indexOf('@'));
			if (id.charAt(0) == '-') {
				id = id.substring(1);
			}
			FBUser u = mUserMap.get(id);
			if (u == null) {
				u = new FBUser(mFBClient, id);
				mUserMap.put(id, u);
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
			u.mPresence = presence;
			out.add(u);
		}
		return out;
	}

	/**
	 * This method should be called once owner is about to be destroyed.
	 */
	public void onDestroy() {
		mChatHandler.removeObserver(mChatObserver);
	}

	/**
	 * Sends message to given user.
	 */
	public void sendMessage(FBUser to, String message) {
		ChatUser user = mChatHandler.getUser(to.getJid());
		if (user != null) {
			mChatHandler.sendMessage(user, message);
		}
	}

	public interface Observer {
		public void onConnected();

		public void onDisconnected();

		public void onMessage(FBUser from, String message);

		public void onPresenceChanged(FBUser user);
	}

	private class FBChatObserver implements ChatObserver.Handler {

		@Override
		public void onConnected() {
			mObserver.onConnected();
		}

		@Override
		public void onDisconnected() {
			mObserver.onDisconnected();
		}

		@Override
		public void onMessage(ChatUser from, String message) {
			String jid = from.getJID();
			String id = jid.substring(0, jid.indexOf('@'));
			if (id.charAt(0) == '-') {
				id = id.substring(1);
			}
			FBUser user = mUserMap.get(id);
			if (user != null) {
				mObserver.onMessage(user, message);
			}
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

			FBUser u = mUserMap.get(id);
			if (u == null) {
				u = new FBUser(mFBClient, id);
				mUserMap.put(id, u);
			}

			u.mJid = jid;
			u.mPresence = presence;
			mObserver.onPresenceChanged(u);
		}
	}

}
