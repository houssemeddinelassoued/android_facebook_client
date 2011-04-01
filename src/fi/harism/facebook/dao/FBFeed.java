package fi.harism.facebook.dao;

import java.io.IOException;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import fi.harism.facebook.net.FBClient;

/**
 * Class presenting different feeds. Let it be "me/home", "me/feed" or
 * "USER_ID/home".
 * 
 * @author harism
 */
public class FBFeed {

	private FBClient fbClient;
	private String feedPath;
	private Vector<FBPost> feedPosts = null;

	/**
	 * Default constructor.
	 * 
	 * @param fbClient
	 * @param feedPath
	 */
	FBFeed(FBClient fbClient, String feedPath) {
		this.fbClient = fbClient;
		this.feedPath = feedPath;
		this.feedPosts = new Vector<FBPost>();
	}

	/**
	 * Returns path of this feed.
	 */
	public String getPath() {
		return feedPath;
	}

	/**
	 * Returns posts in this feed.
	 */
	public Vector<FBPost> getPosts() {
		return feedPosts;
	}

	/**
	 * Loads certain amount of posts in this feed.<br>
	 * TODO: It might be a good idea to move response parsing to FBPost instead.
	 */
	public void load() throws IOException, JSONException {
		Bundle params = new Bundle();
		params.putString(
				"fields",
				"id,type,from,message,picture,link,name,caption,description,created_time,comments,likes");
		JSONObject resp = fbClient.request(feedPath, params);
		JSONArray feedItems = resp.getJSONArray("data");

		Vector<FBPost> posts = new Vector<FBPost>();

		for (int i = 0; i < feedItems.length(); ++i) {
			JSONObject item = feedItems.getJSONObject(i);

			FBPost post = new FBPost(fbClient, item.getString("id"));
			post.type = item.getString("type");
			post.fromId = item.getJSONObject("from").getString("id");
			post.fromName = item.getJSONObject("from").getString("name");
			post.message = item.optString("message", null);
			post.picture = item.optString("picture", null);
			post.link = item.optString("link", null);
			post.name = item.optString("name", null);
			post.caption = item.optString("caption", null);
			post.description = item.optString("description", null);
			post.createdTime = item.optString("created_time", null);

			if (item.optJSONObject("comments") != null) {
				JSONObject comments = item.getJSONObject("comments");
				JSONArray commentsData = comments.optJSONArray("data");
				if (commentsData != null) {
					for (int j = 0; j < commentsData.length(); ++j) {
						JSONObject comment = commentsData.getJSONObject(j);
						FBComment c = new FBComment(comment.getString("id"));
						// TODO: What to do with null values here.
						if (comment.optJSONObject("from") != null) {
							c.fromId = comment.getJSONObject("from").getString(
									"id");
							c.fromName = comment.getJSONObject("from")
									.getString("name");
						} else {
							c.fromId = null;
							c.fromName = null;
						}
						c.message = comment.getString("message");
						c.createdTime = comment.getString("created_time");
						post.comments.add(c);
					}
				}
				post.commentsCount = comments.getInt("count");
			} else {
				post.commentsCount = 0;
			}

			if (item.optJSONObject("likes") != null) {
				post.likesCount = item.getJSONObject("likes").getInt("count");
			} else {
				post.likesCount = 0;
			}

			posts.add(post);
		}

		feedPosts.removeAllElements();
		feedPosts.addAll(posts);
	}

}
