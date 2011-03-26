package fi.harism.facebook.chat;

public class ChatUser {
	
	private String jid;
	
	public enum Presence { CHAT, AWAY, GONE };
	private Presence presence;
	
	public ChatUser(String jid, Presence presence) {
		this.jid = jid;
		this.presence = presence;
	}
	
	public String getJID() {
		return jid;
	}
	
	public Presence getPresence() {
		return presence;
	}
	
}
