package fi.harism.facebook.dao;

public class FBChatUser {
	
	private String id;
	private String jid;
	private String name;
	private String pictureUrl;
	private int presence;
	
	public static final int PRESENCE_CHAT = 1;
	public static final int PRESENCE_AWAY = 2;
	public static final int PRESENCE_GONE = 3;
	public static final int PRESENCE_INVALID = 4;
	
	public FBChatUser(String id, String jid, String name, String pictureUrl, int presence) {
		this.id = id;
		this.jid = jid;
		this.name = name;
		this.pictureUrl = pictureUrl;
		this.presence = presence;
	}
	
	public FBChatUser(FBChatUser user, int presence) {
		this.id = user.getId();
		this.jid = user.getJID();
		this.name = user.getName();
		this.pictureUrl = user.getPictureUrl();
		this.presence = presence;
	}
	
	public String getId() {
		return id;
	}
	
	public String getJID() {
		return jid;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPictureUrl() {
		return pictureUrl;
	}
	
	public int getPresence() {
		return presence;
	}

}
