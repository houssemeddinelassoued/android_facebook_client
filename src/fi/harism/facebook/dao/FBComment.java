package fi.harism.facebook.dao;

public class FBComment {
	
	private String id;
	String fromId;
	String fromName;
	String message;
	String createdTime;
	
	FBComment(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public String getFromId() {
		return fromId;
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
