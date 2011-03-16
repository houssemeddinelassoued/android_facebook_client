package fi.harism.facebook.net;

public interface FacebookLoginObserver {

	public void onCancel();

	public void onComplete();

	public void onError(Exception error);

}
