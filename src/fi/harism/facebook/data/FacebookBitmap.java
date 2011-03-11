package fi.harism.facebook.data;

import android.graphics.Bitmap;

public class FacebookBitmap {
	
	private String id;
	private Bitmap bitmap;
	
	public FacebookBitmap(String id, Bitmap bitmap) {
		this.id = id;
		this.bitmap = bitmap;
	}
	
	public String getId() {
		return id;
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}

}
