package fi.harism.facebook.net;

import android.graphics.Bitmap;

public abstract class ImageRequestObserver {
	public abstract void requestError(Exception ex);
	public abstract void requestDone(Bitmap bitmap);
}
