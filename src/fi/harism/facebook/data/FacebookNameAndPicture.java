package fi.harism.facebook.data;

public class FacebookNameAndPicture {
	
	private String id;
	private String name;
	private String picture;
	
	public FacebookNameAndPicture(String id, String name, String picture) {
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
