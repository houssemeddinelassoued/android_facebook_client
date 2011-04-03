package fi.harism.facebook.dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Bundle;

import fi.harism.facebook.net.FBClient;

/**
 * Class for handling friend list.
 * 
 * @author harism
 */
public class FBFriendList {

	private FBClient mFBClient;
	private HashMap<String, FBUser> mUserMap;
	private Vector<String> mFriendIds;

	/**
	 * Default constructor.
	 * 
	 * @param fbClient
	 * @param userMap
	 * @param friendIds
	 */
	FBFriendList(FBClient fbClient, HashMap<String, FBUser> userMap,
			Vector<String> friendIds) {
		mFBClient = fbClient;
		mUserMap = userMap;
		mFriendIds = friendIds;
	}

	/**
	 * Returns list of friends in no particular order.
	 */
	public Vector<FBUser> getFriends() {
		Vector<FBUser> friends = new Vector<FBUser>();
		for (String id : mFriendIds) {
			friends.add(mUserMap.get(id));
		}
		return friends;
	}

	/**
	 * Loads/updates friend list.
	 */
	public void load() throws IOException, JSONException, XmlPullParserException {	
		Bundle params = new Bundle();
		params.putString("fields", "id, name, picture");
		JSONObject resp = mFBClient.request("me/friends", params);
		
		JSONArray data = resp.getJSONArray("data");

		Vector<FBUser> tempList = new Vector<FBUser>();
		for (int i = 0; i < data.length(); ++i) {
			JSONObject userObj = data.getJSONObject(i);

			FBUser user = mUserMap.get(userObj.getString("id"));
			if (user == null) {
				user = new FBUser(mFBClient, userObj.getString("id"));
			}
			user.update(userObj, FBUser.Level.DEFAULT);
			tempList.add(user);
		}

		mFriendIds.clear();
		for (FBUser friend : tempList) {
			mFriendIds.add(friend.getId());
			mUserMap.put(friend.getId(), friend);
		}
	}

}
