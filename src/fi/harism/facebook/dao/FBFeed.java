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
	 * TODO: It might be a good idea to move response parsing to FBPost instead.
	 */
	public void load() throws IOException, JSONException {
		Bundle params = new Bundle();
		params.putString(
				"fields",
				"id,type,from,message,picture,link,name,caption,description,created_time,comments,likes");
		JSONObject resp = mFBClient.request(mFeedPath, params);
		JSONArray feedItems = resp.getJSONArray("data");

		Vector<FBPost> posts = new Vector<FBPost>();

		for (int i = 0; i < feedItems.length(); ++i) {
			JSONObject item = feedItems.getJSONObject(i);

			FBPost post = new FBPost(mFBClient, item.getString("id"));
			post.mType = item.getString("type");
			post.mFromId = item.getJSONObject("from").getString("id");
			post.mFromName = item.getJSONObject("from").getString("name");
			post.mMessage = item.optString("message", null);
			post.mPicture = item.optString("picture", null);
			post.mLink = item.optString("link", null);
			post.mName = item.optString("name", null);
			post.mCaption = item.optString("caption", null);
			post.mDescription = item.optString("description", null);
			post.mCreatedTime = item.optString("created_time", null);

			if (item.optJSONObject("comments") != null) {
				JSONObject comments = item.getJSONObject("comments");
				JSONArray commentsData = comments.optJSONArray("data");
				if (commentsData != null) {
					for (int j = 0; j < commentsData.length(); ++j) {
						JSONObject comment = commentsData.getJSONObject(j);
						FBComment c = new FBComment(comment.getString("id"));
						// TODO: What to do with null values here.
						if (comment.optJSONObject("from") != null) {
							c.mFromId = comment.getJSONObject("from").getString(
									"id");
							c.mFromName = comment.getJSONObject("from")
									.getString("name");
						} else {
							c.mFromId = null;
							c.mFromName = null;
						}
						c.mMessage = comment.getString("message");
						c.mCreatedTime = comment.getString("created_time");
						post.mComments.add(c);
					}
				}
				post.mCommentsCount = comments.getInt("count");
			} else {
				post.mCommentsCount = 0;
			}

			if (item.optJSONObject("likes") != null) {
				post.mLikesCount = item.getJSONObject("likes").getInt("count");
			} else {
				post.mLikesCount = 0;
			}

			posts.add(post);
		}

		mFeedPosts.removeAllElements();
		mFeedPosts.addAll(posts);
	}

}
