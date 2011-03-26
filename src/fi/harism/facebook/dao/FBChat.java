package fi.harism.facebook.dao;

import java.util.Vector;

import android.os.Bundle;
import fi.harism.facebook.chat.ChatHandler;
import fi.harism.facebook.chat.ChatUser;
import fi.harism.facebook.request.Request;

public class FBChat {

	private FBStorage fbStorage;
	private FBChatObserver chatObserver;
	private Observer observer;
	private FBUserMap fbUserMap;

	public FBChat(FBStorage fbStorage, Observer observer) {
		this.fbStorage = fbStorage;
		this.observer = observer;
		chatObserver = new FBChatObserver();
		fbStorage.chatHandler.addObserver(chatObserver);
		fbUserMap = new FBUserMap(fbStorage);
	}

	public void onDestroy() {
		fbStorage.chatHandler.removeObserver(chatObserver);
	}

	public String getLog() {
		return fbStorage.chatHandler.getLog();
	}

	public void connect() {
		SessionRequest request = new SessionRequest(this);
		fbStorage.requestQueue.addRequest(request);
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

	private class FBChatObserver implements ChatHandler.Observer {

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
			if (u != null) {
				u.setJid(jid);
				u.setPresence(presence);
				observer.onPresenceChanged(u);
			} else {
				FBUserRequest request = new FBUserRequest(this, id, jid, presence);
				fbStorage.requestQueue.addRequest(request);
				//try {
				//	u = fbUserMap.getUser(id);
				//	u.setJid(jid);
				//	u.setPresence(presence);
				//	observer.onPresenceChanged(u);
				//} catch (Exception ex) {
				//}
			}
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

	private class FBUserRequest extends Request {

		private FBUser user;
		String id;
		String jid;
		FBUser.Presence presence;

		public FBUserRequest(Object key,
				String id, String jid, FBUser.Presence presence) {
			super(key);
			this.id = id;
			this.jid = jid;
			this.presence = presence;
		}

		@Override
		public void run() {
			try {
				user = fbUserMap.getUser(id);
				user.setJid(jid);
				user.setPresence(presence);
				observer.onPresenceChanged(user);
			} catch (Exception ex) {
			}
		}

		@Override
		public void stop() {
			// TODO:
		}
	}

	private class SessionRequest extends Request {

		public SessionRequest(Object key) {
			super(key);
		}

		@Override
		public void run() {
			try {
				Bundle params = new Bundle();
				params.putString("method", "auth.promoteSession");
				String secret = fbStorage.fbClient.request(params);
				// Access token is a string of form aaaa|bbbb|cccc
				// where bbbb is session key.
				String[] split = fbStorage.fbClient.getAccessToken().split(
						"\\|");
				if (split.length != 3) {
					// It is possible FB changes access token eventually.
					throw new Exception("Malformed access token.");
				}
				String sessionKey = split[1];
				String sessionSecret = secret.replace("\"", "");

				fbStorage.chatHandler.connect(sessionKey, sessionSecret);
			} catch (Exception ex) {
				observer.onDisconnected();
			}
		}

		@Override
		public void stop() {
			// TODO:
		}

	}

}
