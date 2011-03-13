package fi.harism.facebook.dao;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import fi.harism.facebook.net.FacebookClient;
import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.RequestQueue;

public class DAOFriendList {

	private RequestQueue requestQueue = null;
	private FacebookClient facebookClient = null;
	private Vector<DAOFriend> friendList = null;

	public DAOFriendList(RequestQueue requestQueue,
			FacebookClient facebookClient) {
		this.requestQueue = requestQueue;
		this.facebookClient = facebookClient;
	}

	public DAOFriend at(int index) {
		return friendList.elementAt(index);
	}

	public void getInstance(Activity activity,
			final DAOObserver<DAOFriendList> observer) {
		final DAOFriendList self = this;
		if (friendList != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					observer.onComplete(self);
				}
			});
		} else {
			Bundle b = new Bundle();
			b.putString("fields", "id,name,picture");
			FacebookRequest r = new FacebookRequest(activity, "me/friends", b,
					facebookClient, new FacebookRequest.Observer() {
						@Override
						public void onComplete(FacebookRequest facebookRequest) {
							try {
								JSONArray friendArray = facebookRequest
										.getResponse().getJSONArray("data");

								friendList = new Vector<DAOFriend>();
								for (int i = 0; i < friendArray.length(); ++i) {
									JSONObject f = friendArray.getJSONObject(i);
									String id = f.getString("id");
									String name = f.getString("name");
									String picture = f.getString("picture");
									friendList.add(new DAOFriend(id, name,
											picture));
								}

								observer.onComplete(self);
							} catch (Exception ex) {
								observer.onError(ex);
							}
						}

						@Override
						public void onError(Exception ex) {
							observer.onError(ex);
						}
					});
			requestQueue.addRequest(r);
		}

	}

	public int size() {
		return friendList == null ? 0 : friendList.size();
	}

	public void sort() {
		// Comparator for sorting friend JSONObjects by
		// name.
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

}
