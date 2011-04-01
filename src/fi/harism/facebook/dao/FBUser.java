package fi.harism.facebook.dao;

import java.io.IOException;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Bundle;

import fi.harism.facebook.net.FBClient;

/**
 * Class for holding and retrieving user information. There are three 'levels'
 * for user information.<br>
 * <ul>
 * <li>When first created, user is in UNINITIALIZED state. Meaning it has only
 * id value.</li>
 * <li>Loading user information with DEFAULT value guarantees user has name and
 * picture url set.</li>
 * <li>Level FULL extends DEFAULT level and rest of the fields are filled if
 * they are available. Not everyone shares all information so it's good to do
 * null checks.</li>
 * </ul>
 * 
 * @author harism
 */
public class FBUser {

	// FBClient instance.
	private FBClient fbClient;
	// User id.
	private String id = null;
	// User Jabber id.
	String jid = null;
	// User name.
	String name = null;
	// User picture url.
	String picture = null;
	// Latest status message.
	String status = null;
	// Home town name.
	String hometownLocation = null;
	// Current location.
	String currentLocation = null;
	// Email address.
	String email = null;
	// Web site.
	String website = null;
	// 'Networks'.
	Vector<String> affiliations = null;
	// Current presence, chat related.
	Presence presence;
	// User information level.
	Level level;
	// SELECT clause for FQL query.
	private static final String SELECT = " uid, name, pic_square, affiliations, birthday,sex, hometown_location, current_location, status, website, email ";;

	/**
	 * Default constructor.
	 * 
	 * @param fbClient
	 *            FBClient instance.
	 * @param id
	 *            User id.
	 */
	FBUser(FBClient fbClient, String id) {
		this.fbClient = fbClient;
		this.id = id;
		this.presence = Presence.GONE;
		this.level = Level.UNINITIALIZED;
	}

	/**
	 * Returns list of user's affiliations/networks.
	 */
	public Vector<String> getAffiliations() {
		return affiliations;
	}

	/**
	 * Returns user's current location.
	 */
	public String getCurrentLocation() {
		return currentLocation;
	}

	/**
	 * Returns user's email address.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Returns user's home town name.
	 */
	public String getHometownLocation() {
		return hometownLocation;
	}

	/**
	 * Returns user id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns Jabber ID for current user.
	 */
	public String getJid() {
		return jid;
	}

	/**
	 * Returns information level for this FBUser instance.
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * Returns user's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns user profile picture url.
	 */
	public String getPicture() {
		return picture;
	}

	/**
	 * Returns user's current chat presence.
	 */
	public Presence getPresence() {
		return presence;
	}

	/**
	 * Returns user's latest status message.
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Returns user's website url.
	 */
	public String getWebsite() {
		return website;
	}

	/**
	 * Loads/updates user information with given level of information.
	 * 
	 * @param level
	 * @throws IOException
	 * @throws JSONException
	 * @throws XmlPullParserException
	 */
	public void load(Level level) throws IOException, JSONException,
			XmlPullParserException {
		if (level == Level.DEFAULT) {
			Bundle params = new Bundle();
			params.putString("fields", "name, picture");
			JSONObject response = fbClient.request(id, params);
			this.name = response.getString("name");
			this.picture = response.getString("picture");
			if (this.level != Level.FULL) {
				this.level = Level.DEFAULT;
			}
		} else if (level == Level.FULL) {
			String uid = id;
			if (uid.equals("me")) {
				uid = "me()";
			}
			StringBuilder query = new StringBuilder();
			query.append("SELECT");
			query.append(SELECT);
			query.append("FROM user WHERE uid = ");
			query.append(uid);

			JSONObject resp = fbClient.requestFQL(query.toString());
			JSONArray data = resp.getJSONArray("data");
			if (data.length() != 1) {
				throw new IOException("Received more than 1 user information.");
			}

			JSONObject userObj = data.getJSONObject(0);

			this.name = userObj.getString("name");
			this.picture = userObj.getString("pic_square");

			JSONObject statusObj = userObj.optJSONObject("status");
			if (statusObj != null) {
				this.status = statusObj.getString("message");
			}

			this.level = Level.FULL;
		}
	}

	public enum Level {
		DEFAULT, FULL, UNINITIALIZED
	}

	public enum Presence {
		CHAT, AWAY, GONE
	}

}
