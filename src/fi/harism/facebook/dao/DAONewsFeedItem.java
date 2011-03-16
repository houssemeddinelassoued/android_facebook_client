package fi.harism.facebook.dao;

/**
 * Simple storage class for News Feed items.
 * 
 * @author harism
 */
public class DAONewsFeedItem {

	private String id;
	private String type;
	private String fromId;
	private String fromName;
	private String message;
	private String pictureUrl;
	private String name;
	private String description;
	private String createdTime;

	public DAONewsFeedItem(String id, String type, String fromId,
			String fromName, String message, String pictureUrl, String name,
			String description, String createdTime) {
		this.id = id;
		this.type = type;
		this.fromId = fromId;
		this.fromName = fromName;
		this.message = message;
		this.pictureUrl = pictureUrl;
		this.name = name;
		this.description = description;
		this.createdTime = createdTime;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public String getDescription() {
		return description;
	}

	public String getFromId() {
		return fromId;
	}

	public String getFromName() {
		return fromName;
	}

	public String getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public String getName() {
		return name;
	}

	public String getPictureUrl() {
		return pictureUrl;
	}

	public String getType() {
		return type;
	}

}
