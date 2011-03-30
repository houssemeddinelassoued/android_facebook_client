package fi.harism.facebook.dao;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class FBBitmap {

	private FBStorage fbStorage;
	private String url;
	private byte[] bitmapData;

	public FBBitmap(FBStorage fbStorage, String url) {
		this.fbStorage = fbStorage;
		this.url = url;
		this.bitmapData = fbStorage.imageCache.getData(url);
	}

	public String getUrl() {
		return url;
	}

	public Bitmap getBitmap() {
		if (bitmapData != null) {
			return BitmapFactory.decodeByteArray(bitmapData, 0,
					bitmapData.length);
		} else {
			return null;
		}
	}

	public Bitmap load() throws Exception {
		// Open InputStream for given url.
		URL u = new URL(url);
		InputStream is = u.openStream();
		ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();

		// Read actual data from InputStream.
		int readLength;
		byte buffer[] = new byte[1024];
		while ((readLength = is.read(buffer)) != -1) {
			imageBuffer.write(buffer, 0, readLength);
		}

		bitmapData = imageBuffer.toByteArray();
		fbStorage.imageCache.setData(url, bitmapData);

		return BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
	}

}
