package fi.harism.facebook.dao;

public interface DAOObserver<T> {
	public void onComplete(T response);
	public void onError(Exception error);
}
