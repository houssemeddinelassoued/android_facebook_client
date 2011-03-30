package fi.harism.facebook.dao;

import java.io.Serializable;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;

import fi.harism.facebook.net.FBClient;

public class FBUser implements Serializable {

	private static final long serialVersionUID = 8817570826057668756L;

	String id = null;
	String jid = null;
	String name = null;
	String picture = null;
	String status = null;
	String hometownLocation = null;
	String currentLocation = null;
	String email = null;
	String website = null;
	Vector<String> affiliations = null;

	public enum Presence {
		CHAT, AWAY, GONE
	};

	Presence presence;

	public enum Level {
		DEFAULT, FULL, UNINITIALIZED
	};

	Level level;

	private FBClient fbClient;

	private static final String SELECT = " uid, name, pic_square, affiliations, birthday,sex, hometown_location, current_location, status, website, email ";

	FBUser(FBClient fbClient, String id) {
		this.fbClient = fbClient;
		this.id = id;
		this.presence = Presence.GONE;
		this.level = Level.UNINITIALIZED;
	}

	public Level getLevel() {
		return level;
	}

	public Presence getPresence() {
		return presence;
	}

	public Vector<String> getAffiliations() {
		return affiliations;
	}

	public String getCurrentLocation() {
		return currentLocation;
	}

	public String getEmail() {
		return email;
	}

	public String getHometownLocation() {
		return hometownLocation;
	}

	public String getId() {
		return id;
	}

	public String getJid() {
		return jid;
	}

	public String getName() {
		return name;
	}

	public String getPicture() {
		return picture;
	}

	public String getStatus() {
		return status;
	}

	public String getWebsite() {
		return website;
	}

	public void load(Level level) throws Exception {
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
				throw new Exception("Received more than 1 user information.");
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

}
