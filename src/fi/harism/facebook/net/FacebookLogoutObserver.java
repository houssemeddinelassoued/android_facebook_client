package fi.harism.facebook.net;

public interface FacebookLogoutObserver {
	public void onComplete();
	public void onError(Exception error);
}
