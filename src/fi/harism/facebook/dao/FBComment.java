package fi.harism.facebook.dao;

/**
 * Storage class presenting single comment.
 * 
 * @author harism
 */
public class FBComment {

	// Comment id.
	private String mId;
	// Sender id.
	String mFromId;
	// Sender name.
	String mFromName;
	// Comment message/content.
	String mMessage;
	// Creation time.
	String mCreatedTime;

	FBComment(String id) {
		mId = id;
	}

	/**
	 * Returns String presentation for time when this comment was
	 * created/posted.
	 */
	public String getCreatedTime() {
		return mCreatedTime;
	}

	/**
	 * Returns user id for person who sent the comment.
	 */
	public String getFromId() {
		return mFromId;
	}

	/**
	 * Returns name of person who sent the comment.
	 */
	public String getFromName() {
		return mFromName;
	}

	/**
	 * Returns comment id.
	 */
	public String getId() {
		return mId;
	}

	/**
	 * Returns comment content/message.
	 */
	public String getMessage() {
		return mMessage;
	}

}
