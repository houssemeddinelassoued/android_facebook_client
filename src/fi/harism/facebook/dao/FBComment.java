package fi.harism.facebook.dao;

/**
 * Storage class presenting single comment.
 * 
 * @author harism
 */
public class FBComment {

	// Comment id.
	private String id;
	// Sender id.
	String fromId;
	// Sender name.
	String fromName;
	// Comment message/content.
	String message;
	// Creation time.
	String createdTime;

	FBComment(String id) {
		this.id = id;
	}

	/**
	 * Returns String presentation for time when this comment was
	 * created/posted.
	 */
	public String getCreatedTime() {
		return createdTime;
	}

	/**
	 * Returns user id for person who sent the comment.
	 */
	public String getFromId() {
		return fromId;
	}

	/**
	 * Returns name of person who sent the comment.
	 */
	public String getFromName() {
		return fromName;
	}

	/**
	 * Returns comment id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns comment content/message.
	 */
	public String getMessage() {
		return message;
	}

}
