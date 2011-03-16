package fi.harism.facebook.net;

public interface FacebookAuthorizeObserver {

	public void onCancel();

	public void onComplete();

	public void onError(Exception error);

}
