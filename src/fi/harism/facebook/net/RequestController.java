package fi.harism.facebook.net;

import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import fi.harism.facebook.dao.DAOBitmap;
import fi.harism.facebook.dao.DAOComment;
import fi.harism.facebook.dao.DAOCommentsMap;
import fi.harism.facebook.dao.DAOFriendList;
import fi.harism.facebook.dao.DAOFeedList;
import fi.harism.facebook.dao.DAOObserver;
import fi.harism.facebook.dao.DAOProfile;
import fi.harism.facebook.dao.DAOProfileMap;
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
	private RequestQueue requestQueue = null;

	// Friend list.
	private DAOFriendList friendList = null;
	// News Feed list.
	private DAOFeedList newsFeedList = null;
	// Profile Feed list.
	private DAOFeedList profileFeedList = null;
	// Profile map.
	private DAOProfileMap profileMap = null;
	// Bitmap handling.
	private DAOBitmap bitmap = null;
	// Comments map.
	private DAOCommentsMap commentsMap = null;

	/**
	 * Default constructor.
	 */
	public RequestController() {
		facebookClient = new FacebookClient();
		requestQueue = new RequestQueue();

		friendList = new DAOFriendList(requestQueue, facebookClient);
		newsFeedList = new DAOFeedList(requestQueue, facebookClient,
				DAOFeedList.NEWS_FEED);
		profileFeedList = new DAOFeedList(requestQueue, facebookClient,
				DAOFeedList.PROFILE_FEED);
		profileMap = new DAOProfileMap(requestQueue, facebookClient);
		bitmap = new DAOBitmap(requestQueue);
		commentsMap = new DAOCommentsMap(requestQueue, facebookClient);
	}

	public void getBitmap(Activity activity, String imageUrl,
			DAOObserver<Bitmap> observer) {
		bitmap.getBitmap(activity, imageUrl, observer);
	}

	public void getComments(Activity activity, String postId,
			DAOObserver<Vector<DAOComment>> observer) {
		commentsMap.getComments(activity, postId, observer);
	}

	public void getFriendList(Activity activity,
			DAOObserver<DAOFriendList> observer) {
		friendList.getInstance(activity, observer);
	}

	public void getNewsFeed(Activity activity, DAOObserver<DAOFeedList> observer) {
		newsFeedList.getInstance(activity, observer);
	}

	public void getProfile(Activity activity, String userId,
			DAOObserver<DAOProfile> observer) {
		profileMap.getProfile(activity, userId, observer);
	}

	public void getProfileFeed(Activity activity,
			DAOObserver<DAOFeedList> observer) {
		profileFeedList.getInstance(activity, observer);
	}

	public boolean isAuthorized() {
		return facebookClient.isAuthorized();
	}

	public void login(Activity activity, FacebookLoginObserver observer) {
		facebookClient.authorize(activity, observer);
	}

	public void loginCallback(int requestCode, int resultCode, Intent data) {
		facebookClient.authorizeCallback(requestCode, resultCode, data);
	}

	public void logout(final Activity activity,
			final FacebookLogoutObserver observer) {
		new Thread() {
			@Override
			public void run() {
				try {
					facebookClient.logout(activity);

					// Remove all active requests from queue.
					requestQueue.removeAllRequests();
					// Initiate new DAO objects.
					friendList = new DAOFriendList(requestQueue, facebookClient);
					newsFeedList = new DAOFeedList(requestQueue,
							facebookClient, DAOFeedList.NEWS_FEED);
					profileFeedList = new DAOFeedList(requestQueue,
							facebookClient, DAOFeedList.PROFILE_FEED);
					profileMap = new DAOProfileMap(requestQueue, facebookClient);
					bitmap = new DAOBitmap(requestQueue);
					commentsMap = new DAOCommentsMap(requestQueue,
							facebookClient);

					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							observer.onComplete();
						}
					});
				} catch (final Exception ex) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							observer.onError(ex);
						}
					});
				}
			}
		}.start();
	}

	/**
	 * Removes all requests made from given Activity. This method should be
	 * called once Activity's onDestroy is called.
	 * 
	 * @param activity
	 *            Activity for which remove all requests.
	 */
	public void removeRequests(Activity activity) {
		requestQueue.removeRequests(activity);
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
		requestQueue.setPaused(activity, paused);
	}

}
