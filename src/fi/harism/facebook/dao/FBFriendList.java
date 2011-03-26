package fi.harism.facebook.dao;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import fi.harism.facebook.request.Request;

public class FBFriendList implements Iterable<FBFriend> {

	// FBStorage instance.
	private FBStorage fbStorage;

	public FBFriendList(FBStorage fbStorage) {
		this.fbStorage = fbStorage;
	}

	public void setPaused(boolean paused) {
		fbStorage.requestQueue.setPaused(this, paused);
	}

	public void cancel() {
		fbStorage.requestQueue.removeRequests(this);
	}

	public FBFriend at(int index) {
		return fbStorage.friendList.elementAt(index);
	}

	public void load() throws Exception {
		Bundle params = new Bundle();
		params.putString("fields", "id,name,picture");
		JSONObject resp = fbStorage.fbClient.request("me/friends", params);
		JSONArray friendArray = resp.getJSONArray("data");

		Vector<FBFriend> tempList = new Vector<FBFriend>();
		for (int i = 0; i < friendArray.length(); ++i) {
			JSONObject f = friendArray.getJSONObject(i);
			String id = f.getString("id");
			String name = f.getString("name");
			String picture = f.getString("picture");
			tempList.add(new FBFriend(id, name, picture));
		}

		Comparator<FBFriend> comparator = new Comparator<FBFriend>() {
			@Override
			public int compare(FBFriend arg0, FBFriend arg1) {
				String arg0Name = arg0.getName();
				String arg1Name = arg1.getName();
				return arg0Name.compareToIgnoreCase(arg1Name);
			}
		};
		Collections.sort(tempList, comparator);

		fbStorage.friendList.removeAllElements();
		fbStorage.friendList.addAll(tempList);
	}

	public void load(FBObserver<FBFriendList> observer) {
		final FBFriendList self = this;
		if (fbStorage.friendList.size() > 0) {
			observer.onComplete(self);
		} else {
			FriendsRequest request = new FriendsRequest(this, observer);
			fbStorage.requestQueue.addRequest(request);
		}
	}

	@Override
	public Iterator<FBFriend> iterator() {
		Vector<FBFriend> copy = new Vector<FBFriend>(fbStorage.friendList);
		return copy.iterator();
	}

	public int size() {
		return fbStorage.friendList.size();
	}

	private class FriendsRequest extends Request {

		private FBFriendList parent;
		private FBObserver<FBFriendList> observer;

		public FriendsRequest(FBFriendList parent,
				FBObserver<FBFriendList> observer) {
			super(parent);
			this.parent = parent;
			this.observer = observer;
		}

		@Override
		public void run() {
			try {
				load();
				observer.onComplete(parent);
			} catch (Exception ex) {
				observer.onError(ex);
			}
		}

		@Override
		public void stop() {
			// TODO:
		}
	}

}
