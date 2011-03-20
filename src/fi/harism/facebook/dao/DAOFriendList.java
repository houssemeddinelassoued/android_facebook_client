package fi.harism.facebook.dao;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import fi.harism.facebook.net.FacebookClient;
import fi.harism.facebook.request.Request;
import fi.harism.facebook.request.RequestQueue;

/**
 * Class for retrieving and storing friend list.
 * 
 * @author harism
 */
public class DAOFriendList implements Iterable<DAOFriend> {

	// RequestQueue instance.
	private RequestQueue requestQueue = null;
	// FacebookClient instance.
	private FacebookClient facebookClient = null;
	// List of friends.
	private Vector<DAOFriend> friendList = null;
	// Boolean for making only one friend list request.
	private boolean friendListLoaded = false;

	/**
	 * Default constructor.
	 * 
	 * @param requestQueue
	 *            RequestQueue instance.
	 * @param facebookClient
	 *            FacebookClient instance.
	 */
	public DAOFriendList(RequestQueue requestQueue,
			FacebookClient facebookClient) {
		this.requestQueue = requestQueue;
		this.facebookClient = facebookClient;
		friendList = new Vector<DAOFriend>();
	}

	/**
	 * Accessor for retrieving friend list size.
	 * 
	 * @param index
	 *            Index of DAOFriend item.
	 * @return DAOFriend item at given index.
	 */
	public DAOFriend at(int index) {
		return friendList.elementAt(index);
	}

	/**
	 * This method returns instance of the class through observer once friend
	 * list is loaded.
	 * 
	 * @param activity
	 *            Activity which triggered this request.
	 * @param observer
	 *            Observer for this request.
	 */
	public void getInstance(Activity activity,
			final DAOObserver<DAOFriendList> observer) {
		// We need "this" pointer later on.
		final DAOFriendList self = this;
		// If friend list has been loaded already.
		if (friendListLoaded) {
			// Call observer from UI thread.
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					observer.onComplete(self);
				}
			});
		} else {
			FriendsRequest request = new FriendsRequest(activity, this, observer);
			requestQueue.addRequest(request);
		}

	}

	@Override
	public Iterator<DAOFriend> iterator() {
		// Return Iterator for a copy Vector instead.
		Vector<DAOFriend> copy = new Vector<DAOFriend>(friendList);
		return copy.iterator();
	}

	/**
	 * Accessor for getting size of friend list.
	 * 
	 * @return Number of DAOFriend items in this list.
	 */
	public int size() {
		return friendList.size();
	}

	/**
	 * Private method for sorting friend list by name.
	 */
	private void sort() {
		Comparator<DAOFriend> comparator = new Comparator<DAOFriend>() {
			@Override
			public int compare(DAOFriend arg0, DAOFriend arg1) {
				String arg0Name = arg0.getName();
				String arg1Name = arg1.getName();
				return arg0Name.compareToIgnoreCase(arg1Name);
			}
		};
		// Sort friends Vector.
		Collections.sort(friendList, comparator);
	}
	
	private class FriendsRequest extends Request {
		
		private DAOFriendList caller;
		private DAOObserver<DAOFriendList> observer;

		public FriendsRequest(Activity activity, DAOFriendList caller, DAOObserver<DAOFriendList> observer) {
			super(activity);
			this.caller = caller;
			this.observer = observer;
		}

		@Override
		public void runOnThread() throws Exception {
			try {
				// Create friend list request.
				Bundle params = new Bundle();
				params.putString("fields", "id,name,picture");
				JSONObject resp = facebookClient.request("me/friends", params);
				JSONArray friendArray = resp.getJSONArray("data");

				friendList.clear();
				for (int i = 0; i < friendArray.length(); ++i) {
					JSONObject f = friendArray.getJSONObject(i);
					String id = f.getString("id");
					String name = f.getString("name");
					String picture = f.getString("picture");
					friendList.add(new DAOFriend(id, name,
											picture));
				}

				sort();
				friendListLoaded = true;

			} catch (Exception ex) {
				observer.onError(ex);
				throw ex;
			}
		}

		@Override
		public void runOnUiThread() throws Exception {
			observer.onComplete(caller);
		}
	}

}
