package fi.harism.facebook.dao;

import java.util.Vector;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import fi.harism.facebook.chat.ChatHandler;
import fi.harism.facebook.chat.ChatUser;
import fi.harism.facebook.request.Request;

public class FBChat {

	private FBStorage fbStorage;
	private FBChatObserver chatObserver;
	private Observer observer;

	public FBChat(FBStorage fbStorage, Observer observer) {
		this.fbStorage = fbStorage;
		this.observer = observer;
		chatObserver = new FBChatObserver();
		fbStorage.chatHandler.addObserver(chatObserver);
	}
	
	public void onDestroy() {
		fbStorage.chatHandler.removeObserver(chatObserver);
	}

	public String getLog() {
		return fbStorage.chatHandler.getLog();
	}

	public void connect(Activity activity) {
		fbStorage.fbSession.load(activity, new FBSessionObserver());
	}
	
	public FBChatUser getUser(String userId) {
		return fbStorage.chatUserMap.get(userId);
	}
	
	public Vector<FBChatUser> getUsers() {
		Vector<ChatUser> users = fbStorage.chatHandler.getUsers();
		Vector<FBChatUser> out = new Vector<FBChatUser>();
		for (ChatUser user : users) {
			String jid = user.getJID();
			String id = jid.substring(0, jid.indexOf('@'));
			if (id.charAt(0) == '-') {
				id = id.substring(1);
			}
			FBChatUser u = fbStorage.chatUserMap.get(id);
			if (u != null) {
				out.add(u);
			}
		}
		return out;
	}
	
	public void sendMessage(FBChatUser to, String message) {
		ChatUser user = fbStorage.chatHandler.getUser(to.getJID());
		if (user != null) {
			fbStorage.chatHandler.sendMessage(user, message);
		}
	}
	
	public FBChatUser getUserInfo(FBChatUser user) throws Exception {
		Bundle params = new Bundle();
		params.putString("fields", "id, name, picture");
		JSONObject resp = fbStorage.fbClient.request(user.getId(), params);
		String name = resp.getString("name");
		String picture = resp.getString("picture");
		
		FBChatUser u = new FBChatUser(user.getId(), user.getJID(), name, picture, user.getPresence());
		fbStorage.chatUserMap.put(user.getId(), u);
		return u;
	}
	
	public void getUserInfo(FBChatUser user, Activity activity, FBObserver<FBChatUser> observer) {
		FBChatUserRequest request = new FBChatUserRequest(activity, this, user, observer);
		fbStorage.requestQueue.addRequest(request);
	}
	
	public void disconnect() {
		fbStorage.chatHandler.disconnect();
	}

	public interface Observer {
		public void onConnected();

		public void onDisconnected();

		public void onPresenceChanged(FBChatUser user);
		
		public void onMessage(FBChatUser from, String message);
	}

	private class FBSessionObserver implements FBObserver<FBSession> {
		@Override
		public void onComplete(FBSession response) {
			fbStorage.chatHandler.connect(response.getSessionKey(),
					response.getSessionSecret());
		}
		@Override
		public void onError(Exception error) {
			observer.onDisconnected();
		}
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
			int presence;
			switch (user.getPresence()) {
			case ChatUser.PRESENCE_AWAY:
				presence = FBChatUser.PRESENCE_AWAY;
				break;
			case ChatUser.PRESENCE_CHAT:
				presence = FBChatUser.PRESENCE_CHAT;
				break;
			case ChatUser.PRESENCE_GONE:
				presence = FBChatUser.PRESENCE_GONE;
				break;
			default:
				presence = FBChatUser.PRESENCE_INVALID;
				break;
			}
			
			if (fbStorage.chatUserMap.containsKey(id)) {
				FBChatUser u = fbStorage.chatUserMap.get(id);
				u = new FBChatUser(u, presence);
				fbStorage.chatUserMap.put(id, u);
				observer.onPresenceChanged(u);
			} else {
				FBChatUser u = new FBChatUser(id, jid, null, null, presence);
				observer.onPresenceChanged(u);
			}
		}
		
		@Override
		public void onMessage(ChatUser from, String message) {
			String jid = from.getJID();
			String id = jid.substring(0, jid.indexOf('@'));
			if (id.charAt(0) == '-') {
				id = id.substring(1);
			}
			FBChatUser user = fbStorage.chatUserMap.get(id);
			if (user != null) {
				observer.onMessage(user, message);
			}
		}
	}
	
	private class FBChatUserRequest extends Request {
		
		private FBChatUser user;
		private FBObserver<FBChatUser> observer;

		public FBChatUserRequest(Activity activity, Object key, FBChatUser user, FBObserver<FBChatUser> observer) {
			super(activity, key);
			this.user = user;
			this.observer = observer;
		}

		@Override
		public void runOnThread() throws Exception {
			try {
				user = getUserInfo(user);
			} catch (Exception ex) {
				observer.onError(ex);
				throw ex;
			}
		}

		@Override
		public void runOnUiThread() throws Exception {
			observer.onComplete(user);
		}
		
	}

}
