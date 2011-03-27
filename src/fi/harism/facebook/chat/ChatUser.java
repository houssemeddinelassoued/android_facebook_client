package fi.harism.facebook.chat;

/**
 * Chat user container class.
 * 
 * @author harism
 */
public class ChatUser {

	private String jid;

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

	public enum Presence {
		CHAT, AWAY, GONE
	}

}
