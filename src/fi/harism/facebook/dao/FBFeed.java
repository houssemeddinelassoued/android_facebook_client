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

	private FBClient mFBClient;
	private String mFeedPath;
	private Vector<FBPost> mFeedPosts = null;

	/**
	 * Default constructor.
	 * 
	 * @param fbClient
	 * @param feedPath
	 */
	FBFeed(FBClient fbClient, String feedPath) {
		mFBClient = fbClient;
		mFeedPath = feedPath;
		mFeedPosts = new Vector<FBPost>();
	}

	/**
	 * Returns path of this feed.
	 */
	public String getPath() {
		return mFeedPath;
	}

	/**
	 * Returns posts in this feed.
	 */
	public Vector<FBPost> getPosts() {
		return mFeedPosts;
	}

	/**
	 * Loads certain amount of posts in this feed.<br>
	 */
	public void load() throws IOException, JSONException {
		Bundle params = new Bundle();
		params.putString("fields", FBPost.FIELDS);
		JSONObject resp = mFBClient.request(mFeedPath, params);
		JSONArray feedItems = resp.getJSONArray("data");

		Vector<FBPost> posts = new Vector<FBPost>();
		for (int i = 0; i < feedItems.length(); ++i) {
			JSONObject item = feedItems.getJSONObject(i);
			FBPost post = new FBPost(mFBClient, item.getString("id"));
			post.update(item);
			posts.add(post);
		}

		mFeedPosts.removeAllElements();
		mFeedPosts.addAll(posts);
	}

}
