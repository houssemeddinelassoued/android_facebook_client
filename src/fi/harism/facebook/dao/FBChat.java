package fi.harism.facebook.dao;

import java.util.Vector;

import android.os.Bundle;
import fi.harism.facebook.chat.ChatObserver;
import fi.harism.facebook.chat.ChatUser;

public class FBChat {

	private FBStorage fbStorage;
	private FBChatObserver chatObserver;
	private Observer observer;

	public FBChat(FBStorage fbStorage,
			Observer observer) {
		this.fbStorage = fbStorage;
		this.observer = observer;
		chatObserver = new FBChatObserver();
	}

	public void onDestroy() {
		fbStorage.chatHandler.removeObserver(chatObserver);
	}

	public String getLog() {
		return fbStorage.chatHandler.getLog();
	}

	public void connect() throws Exception{
		Bundle params = new Bundle();
		params.putString("method", "auth.promoteSession");
		String secret = fbStorage.fbClient.request(params);
		// Access token is a string of form aaaa|bbbb|cccc
		// where bbbb is session key.
		String[] split = fbStorage.fbClient.getAccessToken().split("\\|");
		if (split.length != 3) {
			// It is possible FB changes access token eventually.
			throw new Exception("Malformed access token.");
		}
		String sessionKey = split[1];
		String sessionSecret = secret.replace("\"", "");

		fbStorage.chatHandler.addObserver(chatObserver);
		fbStorage.chatHandler.connect(sessionKey, sessionSecret);
	}

	public Vector<FBUser> getUsers() {
		Vector<ChatUser> users = fbStorage.chatHandler.getUsers();
		Vector<FBUser> out = new Vector<FBUser>();
		for (ChatUser user : users) {
			String jid = user.getJID();
			String id = jid.substring(0, jid.indexOf('@'));
			if (id.charAt(0) == '-') {
				id = id.substring(1);
			}
			FBUser u = fbStorage.userMap.get(id);
			if (u != null) {
				out.add(u);
			}
		}
		return out;
	}

	public void sendMessage(FBUser to, String message) {
		ChatUser user = fbStorage.chatHandler.getUser(to.getJid());
		if (user != null) {
			fbStorage.chatHandler.sendMessage(user, message);
		}
	}

	public void disconnect() {
		fbStorage.chatHandler.disconnect();
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

			FBUser u = fbStorage.userMap.get(id);
			if (u == null) {
				u = new FBUser(fbStorage.fbClient, id);
				fbStorage.userMap.put(id, u);
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
			FBUser user = fbStorage.userMap.get(id);
			if (user != null) {
				observer.onMessage(user, message);
			}
		}
	}

}
