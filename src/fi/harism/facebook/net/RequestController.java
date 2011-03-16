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

/**
 * This class encapsulates all request related tasks. There's supposed to be
 * only one instance of this class application wide.
 * 
 * @author harism
 */
public class RequestController {

	// FacebookClient instance.
	private FacebookClient facebookClient = null;
	// RequestQueue instance.
	private RequestQueue requestController = null;

	// Friend list.
	private DAOFriendList friendList = null;
	// News Feed list.
	private DAONewsFeedList newsFeedList = null;
	// Latest status messages.
	private DAOStatusMap statusMap = null;
	// Profile map.
	private DAOProfileMap profileMap = null;
	// Bitmap handling.
	private DAOBitmap bitmap = null;

	/**
	 * Default constructor.
	 */
	public RequestController() {
		facebookClient = new FacebookClient();
		requestController = new RequestQueue();

		friendList = new DAOFriendList(requestController, facebookClient);
		newsFeedList = new DAONewsFeedList(requestController, facebookClient);
		statusMap = new DAOStatusMap(requestController, facebookClient);
		profileMap = new DAOProfileMap(requestController, facebookClient);
		bitmap = new DAOBitmap(requestController);
	}

	public void authorize(Activity activity, FacebookAuthorizeObserver observer) {
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

	/**
	 * Removes all requests made from given Activity. This method should be
	 * called once Activity's onDestroy is called.
	 * 
	 * @param activity
	 *            Activity for which remove all requests.
	 */
	public void removeRequests(Activity activity) {
		requestController.removeRequests(activity);
	}

	/**
	 * Sets all requests made by given Activity to paused or resumed state. This
	 * method should be called from Activity's onPause and onResume methods.
	 * 
	 * @param activity
	 *            Activity we want to resume or set to paused state.
	 * @param paused
	 *            Boolean whether to pause or resume requests made by given
	 *            Activity.
	 */
	public void setPaused(Activity activity, boolean paused) {
		requestController.setPaused(activity, paused);
	}

}
