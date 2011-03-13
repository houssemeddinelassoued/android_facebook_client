package fi.harism.facebook.net;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import fi.harism.facebook.dao.DAOBitmap;
import fi.harism.facebook.dao.DAOFriendList;
import fi.harism.facebook.dao.DAONewsFeedList;
import fi.harism.facebook.dao.DAOObserver;
import fi.harism.facebook.dao.DAOProfile;
import fi.harism.facebook.dao.DAOProfileMap;
import fi.harism.facebook.dao.DAOStatus;
import fi.harism.facebook.dao.DAOStatusMap;
import fi.harism.facebook.request.RequestQueue;

public class RequestController {

	private FacebookClient facebookClient = null;
	private RequestQueue requestController = null;
	
	private DAOFriendList friendList = null;
	private DAONewsFeedList newsFeedList = null;
	private DAOStatusMap statusMap = null;
	private DAOProfileMap profileMap = null;
	private DAOBitmap bitmap = null;

	public RequestController() {
		facebookClient = new FacebookClient();
		requestController = new RequestQueue();
		
		friendList = new DAOFriendList(requestController, facebookClient);
		newsFeedList = new DAONewsFeedList(requestController, facebookClient);
		statusMap = new DAOStatusMap(requestController, facebookClient);
		profileMap = new DAOProfileMap(requestController, facebookClient);
		bitmap = new DAOBitmap(requestController);
	}

	public void authorize(Activity activity, AuthorizeObserver observer) {
		facebookClient.authorize(activity, observer);
	}

	public void authorizeCallback(int requestCode, int resultCode, Intent data) {
		facebookClient.authorizeCallback(requestCode, resultCode, data);
	}

	public void getBitmap(Activity activity, String imageUrl,
			DAOObserver<Bitmap> observer) {
		bitmap.getBitmap(activity, imageUrl, observer);
	}

	public void getFriendList(Activity activity,
			DAOObserver<DAOFriendList> observer) {
		friendList.getInstance(activity, observer);
	}

	public void getNewsFeed(Activity activity,
			final DAOObserver<DAONewsFeedList> observer) {
		newsFeedList.getInstance(activity, observer);
	}

	public void getProfile(Activity activity, String userId,
			DAOObserver<DAOProfile> observer) {
		profileMap.getProfile(activity, userId, observer);
	}

	public void getStatus(Activity activity, String userId,
			DAOObserver<DAOStatus> observer) {
		statusMap.getStatus(activity, userId, observer);
	}
	
	public void removeRequests(Activity activity) {
		requestController.removeRequests(activity);
	}

	public void setPaused(Activity activity, boolean paused) {
		requestController.setPaused(activity, paused);
	}

	public interface AuthorizeObserver {
		public void onCancel();

		public void onComplete();

		public void onError(Exception error);
	}

}
