package fi.harism.facebook.dao;

import android.graphics.Bitmap;

public class FBBitmap {
	
	private String id;
	private Bitmap bitmap;
	
	public FBBitmap(String id, Bitmap bitmap) {
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
