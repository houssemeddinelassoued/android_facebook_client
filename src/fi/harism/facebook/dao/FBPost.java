package fi.harism.facebook.dao;

import java.io.IOException;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import fi.harism.facebook.net.FBClient;

/**
 * Simple storage class for feed post items.
 * 
 * @author harism
 */
public class FBPost {

	private FBClient fbClient;
	private String id;
	String type;
	String fromId;
	String fromName;
	String message;
	String picture;
	String link;
	String name;
	String caption;
	String description;
	String createdTime;
	Vector<FBComment> comments;
	int commentsCount;
	int likesCount;

	FBPost(FBClient fbClient, String id) {
		this.fbClient = fbClient;
		this.id = id;
		comments = new Vector<FBComment>();
	}

	public String getCaption() {
		return caption;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public String getDescription() {
		return description;
	}

	public String getFromId() {
		return fromId;
	}

	public String getFromName() {
		return fromName;
	}

	public String getId() {
		return id;
	}

	public String getLink() {
		return link;
	}

	public String getMessage() {
		return message;
	}

	public String getName() {
		return name;
	}

	public String getPicture() {
		return picture;
	}

	public String getType() {
		return type;
	}

	public int getLikesCount() {
		return likesCount;
	}

	public int getCommentsCount() {
		return commentsCount;
	}

	/**
	 * Loads all comments for this post. Usually there are 2-3 comments max
	 * loaded from FBFeed but not all.
	 * 
	 * @throws IOException
	 * @throws JSONException
	 */
	public void loadAllComments() throws IOException, JSONException {
		Bundle params = new Bundle();
		params.putString(FBClient.TOKEN, fbClient.getAccessToken());
		params.putString("fields", "id, from,message,created_time");
		params.putString("limit", "999999");

		JSONObject resp = fbClient.request(id + "/comments", params);

		comments.removeAllElements();
		JSONArray data = resp.getJSONArray("data");
		if (data != null) {
			for (int i = 0; i < data.length(); ++i) {
				JSONObject comment = data.getJSONObject(i);
				String id = comment.getString("id");
				String fromId = comment.getJSONObject("from").getString("id");
				String fromName = comment.getJSONObject("from").getString(
						"name");
				String message = comment.getString("message");
				String createdTime = comment.getString("created_time");
				FBComment c = new FBComment(id);
				c.fromId = fromId;
				c.fromName = fromName;
				c.message = message;
				c.createdTime = createdTime;
				comments.add(c);
			}
		}
	}

	/**
	 * Posts a comment to this post.
	 * 
	 * @param message
	 * @throws IOException
	 * @throws JSONException
	 */
	public void postComment(String message) throws IOException, JSONException {
		Bundle params = new Bundle();
		params.putString(FBClient.TOKEN, fbClient.getAccessToken());
		params.putString("message", message);
		fbClient.request(id + "/comments", params, "POST");
	}

}
