package fi.harism.facebook.dao;

/**
 * Simple data storage for friend data.
 * 
 * @author harism
 */
public class DAOFriend {
	private String id;
	private String name;
	private String picture;

	public DAOFriend(String id, String name, String picture) {
		this.id = id;
		this.name = name;
		this.picture = picture;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public String getPicture() {
		return picture;
	}

}
