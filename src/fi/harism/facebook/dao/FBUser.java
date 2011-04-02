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
	private FBClient mFBClient;
	// User id.
	private String mId = null;
	// User sex.
	String mSex = null;
	// User Jabber id.
	String mJid = null;
	// User birthday.
	String mBirthday = null;
	// User name.
	String mName = null;
	// User picture url.
	String mPicture = null;
	// Latest status message.
	String mStatus = null;
	// Home town name.
	String mHometownLocation = null;
	// Current location.
	String mCurrentLocation = null;
	// Email address.
	String mEmail = null;
	// Web site.
	String mWebsite = null;
	// 'Networks'.
	Vector<String> mAffiliations;
	// Current presence, chat related.
	Presence mPresence;
	// User information level.
	Level mLevel;
	// SELECT clause for FQL query.
	static final String SELECT = " uid, name, pic_square, affiliations, birthday,sex, hometown_location, current_location, status, website, email ";;

	/**
	 * Default constructor.
	 * 
	 * @param fbClient
	 *            FBClient instance.
	 * @param id
	 *            User id.
	 */
	FBUser(FBClient fbClient, String id) {
		mFBClient = fbClient;
		mId = id;
		mPresence = Presence.GONE;
		mLevel = Level.UNINITIALIZED;
		mAffiliations = new Vector<String>();
	}

	/**
	 * Returns list of user's affiliations/networks.
	 */
	public Vector<String> getAffiliations() {
		return mAffiliations;
	}

	/**
	 * Returns user's birthday.
	 */
	public String getBirthday() {
		return mBirthday;
	}

	/**
	 * Returns user's current location.
	 */
	public String getCurrentLocation() {
		return mCurrentLocation;
	}

	/**
	 * Returns user's email address.
	 */
	public String getEmail() {
		return mEmail;
	}
	
	/**
	 * Returns user's home town name.
	 */
	public String getHometownLocation() {
		return mHometownLocation;
	}

	/**
	 * Returns user id.
	 */
	public String getId() {
		return mId;
	}

	/**
	 * Returns Jabber ID for current user.
	 */
	public String getJid() {
		return mJid;
	}
	
	/**
	 * Returns information level for this FBUser instance.
	 */
	public Level getLevel() {
		return mLevel;
	}

	/**
	 * Returns user's name.
	 */
	public String getName() {
		return mName;
	}

	/**
	 * Returns user profile picture url.
	 */
	public String getPicture() {
		return mPicture;
	}

	/**
	 * Returns user's current chat presence.
	 */
	public Presence getPresence() {
		return mPresence;
	}

	/**
	 * Returns user's sex.
	 */
	public String getSex() {
		return mSex;
	}

	/**
	 * Returns user's latest status message.
	 */
	public String getStatus() {
		return mStatus;
	}

	/**
	 * Returns user's website url.
	 */
	public String getWebsite() {
		return mWebsite;
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
			JSONObject response = mFBClient.request(mId, params);
			update(response, Level.DEFAULT);
		} else if (level == Level.FULL) {
			String uid = mId;
			if (uid.equals("me")) {
				uid = "me()";
			}
			StringBuilder query = new StringBuilder();
			query.append("SELECT");
			query.append(SELECT);
			query.append("FROM user WHERE uid = ");
			query.append(uid);

			JSONObject resp = mFBClient.requestFQL(query.toString());
			JSONArray data = resp.getJSONArray("data");
			if (data.length() != 1) {
				throw new IOException("Received more than 1 user information.");
			}

			JSONObject userObj = data.getJSONObject(0);
			update(userObj, Level.FULL);
		}
	}
	
	/**
	 * Updates user information from provided JSONObject.
	 * 
	 * @param userObj
	 * @param level
	 * @throws JSONException
	 */
	void update(JSONObject userObj, Level level) throws JSONException {
		if (level == Level.DEFAULT) {
			mName = userObj.getString("name");
			mPicture = userObj.getString("picture");
			if (mLevel != Level.FULL) {
				mLevel = Level.DEFAULT;
			}
		} else if (level == Level.FULL) {
			mName = userObj.getString("name");
			mPicture = userObj.getString("pic_square");

			JSONObject statusObj = userObj.optJSONObject("status");
			if (statusObj != null) {
				mStatus = statusObj.getString("message");
			}
			
			mAffiliations.clear();
			JSONArray affiliations = userObj.optJSONArray("affiliations");
			if (affiliations != null) {
				for (int j=0; j<affiliations.length(); ++j) {
					mAffiliations.add(affiliations.getJSONObject(j)
							.getString("name"));
				}
			}
			
			mBirthday = userObj.optString("birthday", null);
			mSex = userObj.optString("sex", null);
			mWebsite = userObj.optString("website", null);
			mEmail = userObj.optString("email", null);
			
			mLevel = Level.FULL;
		}
		
	}

	public enum Level {
		DEFAULT, FULL, UNINITIALIZED
	}

	public enum Presence {
		CHAT, AWAY, GONE
	}

}
