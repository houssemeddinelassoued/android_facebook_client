package fi.harism.facebook.dao;

public class DAOProfile {
	
	private String id;
	private String name;
	private String pictureUrl;
	
	public DAOProfile(String id, String name, String pictureUrl) {
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
