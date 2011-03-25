package fi.harism.facebook.chat;

public class ChatUser {
	
	public static final int PRESENCE_CHAT = 1;
	public static final int PRESENCE_AWAY = 2;
	public static final int PRESENCE_GONE = 3;
	
	private String jid;
	private int presence;
	
	public ChatUser(String jid, int presence) {
		this.jid = jid;
		this.presence = presence;
	}
	
	public String getJID() {
		return jid;
	}
	
	public int getPresence() {
		return presence;
	}
	
}
