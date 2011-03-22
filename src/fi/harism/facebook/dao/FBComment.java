package fi.harism.facebook.dao;

public class FBComment {
	
	private String fromName;
	private String message;
	private String createdTime;
	
	public FBComment(String fromName, String message, String createdTime) {
		this.fromName = fromName;
		this.message = message;
		this.createdTime = createdTime;
	}
	
	public String getFromName() {
		return fromName;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getCreatedTime() {
		return createdTime;
	}

}
