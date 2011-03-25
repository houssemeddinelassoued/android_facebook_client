package fi.harism.facebook.chat;

import java.util.HashMap;
import java.util.Vector;

public class ChatHandler implements ChatObserver {

	private ChatConnection chatConnection;
	private Vector<Observer> observers;
	private HashMap<String, ChatUser> userMap;
	
	public ChatHandler() {
		chatConnection = new ChatConnection(this);
		observers = new Vector<Observer>();
		userMap = new HashMap<String, ChatUser>();
	}
	
	public void addObserver(Observer observer) {
		if (!observers.contains(observer)) {
			observers.add(observer);
		}
	}
	
	public void sendMessage(ChatUser to, String message) {
		chatConnection.sendMessage(to.getJID(), message);
	}
	
	public ChatUser getUser(String jid) {
		return userMap.get(jid);
	}
	
	public void removeObserver(Observer observer) {
		while(observers.removeElement(observer));
	}
	
	public String getLog() {
		return chatConnection.getLog();
	}
	
	public Vector<ChatUser> getUsers() {
		return new Vector<ChatUser>(userMap.values());
	}
	
	public void connect(String sessionKey, String sessionSecret) {
		chatConnection.connect(sessionKey, sessionSecret);
	}
	
	public void disconnect() {
		chatConnection.disconnect();
		userMap.clear();
	}

	@Override
	public void onChatConnected() {
		for (Observer observer : observers) {
			observer.onConnected();
		}
	}

	@Override
	public void onChatDisconnected() {
		for (Observer observer : observers) {
			observer.onDisconnected();
		}
	}

	@Override
	public void onChatError(Exception ex) {
		for (Observer observer : observers) {
			observer.onDisconnected();
		}
	}

	@Override
	public void onChatPresenceChanged(String jid, String presence) {
		int p = ChatUser.PRESENCE_AWAY;
		if (presence.equals("chat")) {
			p = ChatUser.PRESENCE_CHAT;
		} else if (presence.equals("gone")) {
			p = ChatUser.PRESENCE_GONE;
		}
		
		ChatUser user = new ChatUser(jid, p);
		
		if (user.getPresence() == ChatUser.PRESENCE_GONE) {
			userMap.remove(jid);
		} else {
			userMap.put(jid, user);
		}
		
		for (Observer observer : observers) {
			observer.onPresenceChanged(user);
		}
		
	}
	
	@Override
	public void onChatMessage(String jid, String message) {
		ChatUser user = userMap.get(jid);
		if (user != null) {
			for (Observer observer : observers) {
				observer.onMessage(user, message);
			}
		}
	}
	
	public interface Observer {
		public void onConnected();
		public void onDisconnected();
		public void onPresenceChanged(ChatUser user);
		public void onMessage(ChatUser from, String message);
	}
	
}
