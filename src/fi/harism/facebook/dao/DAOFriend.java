package fi.harism.facebook.dao;

/**
 * Simple data storage for friend data.
 * 
 * @author harism
 */
public class DAOFriend {
	private String id;
	private String name;
	private String pictureUrl;

	public DAOFriend(String id, String name, String pictureUrl) {
		this.id = id;
		this.name = name;
		this.pictureUrl = pictureUrl;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPictureUrl() {
		return pictureUrl;
	}

}
