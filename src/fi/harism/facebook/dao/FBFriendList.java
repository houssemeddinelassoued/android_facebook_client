package fi.harism.facebook.dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import fi.harism.facebook.net.FBClient;

/**
 * Class for handling friend list.
 * 
 * @author harism
 */
public class FBFriendList {

	private FBClient fbClient;
	private HashMap<String, FBUser> userMap;
	private Vector<String> friendIds;

	private static final String SELECT = " uid, name, pic_square, affiliations, birthday,sex, hometown_location, current_location, status, website, email ";

	/**
	 * Default constructor.
	 * 
	 * @param fbClient
	 * @param userMap
	 * @param friendIds
	 */
	FBFriendList(FBClient fbClient, HashMap<String, FBUser> userMap,
			Vector<String> friendIds) {
		this.fbClient = fbClient;
		this.userMap = userMap;
		this.friendIds = friendIds;
	}

	/**
	 * Returns list of friends in no particular order.
	 */
	public Vector<FBUser> getFriends() {
		Vector<FBUser> friends = new Vector<FBUser>();
		for (String id : friendIds) {
			friends.add(userMap.get(id));
		}
		return friends;
	}

	/**
	 * Loads/updates friend list.
	 */
	public void load() throws IOException, JSONException, XmlPullParserException {
		StringBuilder query = new StringBuilder();
		query.append("SELECT");
		query.append(SELECT);
		query.append("FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = me())");

		JSONObject resp = fbClient.requestFQL(query.toString());
		JSONArray data = resp.getJSONArray("data");

		// TODO: It might be a good idea to move user creation/updating to
		// FBUser instead.
		Vector<FBUser> tempList = new Vector<FBUser>();
		for (int i = 0; i < data.length(); ++i) {
			JSONObject userObj = data.getJSONObject(i);

			FBUser user = userMap.get(userObj.getString("uid"));
			if (user == null) {
				user = new FBUser(fbClient, userObj.getString("uid"));
			}

			user.name = userObj.getString("name");
			user.picture = userObj.getString("pic_square");

			JSONObject statusObj = userObj.optJSONObject("status");
			if (statusObj != null) {
				user.status = statusObj.getString("message");
			}

			tempList.add(user);
		}

		friendIds.clear();
		for (FBUser friend : tempList) {
			friendIds.add(friend.getId());
			userMap.put(friend.getId(), friend);
		}
	}

}
