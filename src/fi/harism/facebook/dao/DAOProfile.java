package fi.harism.facebook.dao;

/**
 * Simple storage class for Profile data.
 * 
 * @author harism
 */
public class DAOProfile {

	private String id;
	private String name;
	private String pictureUrl;
	private String status;

	public DAOProfile(String id, String name, String pictureUrl, String status) {
		this.id = id;
		this.name = name;
		this.pictureUrl = pictureUrl;
		this.status = status;
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
	
	public String getStatus() {
		return status;
	}

}
