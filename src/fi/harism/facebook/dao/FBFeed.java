package fi.harism.facebook.dao;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import fi.harism.facebook.net.FBClient;
import fi.harism.facebook.request.Request;

public class FBFeed {

	private FBClient fbClient;
	private String feedPath;
	private Vector<FBPost> feedPosts = null;
	//public static final String NEWS_FEED = "/me/home";
	//public static final String PROFILE_FEED = "/me/feed";

	FBFeed(FBClient fbClient, String feedPath) {
		this.fbClient = fbClient;
		this.feedPath = feedPath;
		this.feedPosts = new Vector<FBPost>();
	}
	
	public Vector<FBPost> getPosts() {
		return feedPosts;
	}
	
	public void load() throws Exception {
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
					for (int j=0; j<commentsData.length(); ++j) {
						JSONObject comment = commentsData.getJSONObject(j);
						FBComment c = new FBComment(comment.getString("id"));
						// TODO: What to do with null values here.
						if (comment.optJSONObject("from") != null) {
							c.fromId = comment.getJSONObject("from").getString("id");
							c.fromName = comment.getJSONObject("from").getString("name");
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
