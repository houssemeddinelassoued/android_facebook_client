package fi.harism.facebook.chat;

public interface ChatObserver {
	
	public void onChatConnected();
	
	public void onChatDisconnected();

	public void onChatError(Exception ex);

	public void onChatPresenceChanged(String jid, String presence);
	
	public void onChatMessage(String jid, String message);

}
