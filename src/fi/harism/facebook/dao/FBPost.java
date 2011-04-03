package fi.harism.facebook.dao;

import java.io.IOException;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;
import fi.harism.facebook.net.FBClient;

/**
 * Simple storage class for feed post items.
 * 
 * @author harism
 */
public class FBPost {

	private FBClient mFBClient;
	private String mId;
	private String mType;
	private String mFromId;
	private String mFromName;
	private String mToId;
	private String mToName;
	private String mMessage;
	private String mPicture;
	private String mLink;
	private String mName;
	private String mCaption;
	private String mDescription;
	private String mCreatedTime;
	private Vector<FBComment> mComments;
	private int mCommentsCount;
	private int mLikesCount;

	static final String FIELDS = "id,type,from,to,message,picture,link,name,caption,description,created_time,comments,likes";

	FBPost(FBClient fbClient, String id) {
		mFBClient = fbClient;
		mId = id;
		mComments = new Vector<FBComment>();
	}

	public String getToId() {
		return mToId;
	}

	public String getToName() {
		return mToName;
	}

	public String getCaption() {
		return mCaption;
	}

	public int getCommentsCount() {
		return mCommentsCount;
	}

	public String getCreatedTime() {
		return mCreatedTime;
	}

	public String getDescription() {
		return mDescription;
	}

	public String getFromId() {
		return mFromId;
	}

	public String getFromName() {
		return mFromName;
	}

	public String getId() {
		return mId;
	}

	public int getLikesCount() {
		return mLikesCount;
	}

	public String getLink() {
		return mLink;
	}

	public String getMessage() {
		return mMessage;
	}

	public String getName() {
		return mName;
	}

	public String getPicture() {
		return mPicture;
	}

	public String getType() {
		return mType;
	}
	
	public Vector<FBComment> getComments() {
		return mComments;
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
		params.putString(FBClient.TOKEN, mFBClient.getAccessToken());
		params.putString("fields", "id, from,message,created_time");
		params.putString("limit", "999999");

		JSONObject resp = mFBClient.request(mId + "/comments", params);

		mComments.removeAllElements();
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
				c.mFromId = fromId;
				c.mFromName = fromName;
				c.mMessage = message;
				c.mCreatedTime = createdTime;
				mComments.add(c);
			}
		}
		mCommentsCount = mComments.size();
	}
	
	public void update() throws IOException, JSONException {
		Bundle params = new Bundle();
		params.putString(FBClient.TOKEN, mFBClient.getAccessToken());
		JSONObject resp = mFBClient.request(mId, params);
		
		JSONObject likesObj = resp.optJSONObject("likes");
		if (likesObj != null) {
			mLikesCount = likesObj.getInt("count");
		}
		
		mComments.clear();
		JSONObject commentsObj = resp.optJSONObject("comments");
		if (commentsObj != null) {
			mCommentsCount = commentsObj.getInt("count");
			JSONArray commentsData = commentsObj.optJSONArray("data");
			if (commentsData != null) {
				for (int i=0; i<commentsData.length(); ++i) {
					JSONObject comment = commentsData.getJSONObject(i);
					FBComment c = new FBComment(comment.getString("id"));
					// TODO: What to do with null values here.
					if (comment.optJSONObject("from") != null) {
						c.mFromId = comment.getJSONObject("from").getString(
								"id");
						c.mFromName = comment.getJSONObject("from").getString(
								"name");
					} else {
						c.mFromId = null;
						c.mFromName = null;
					}
					c.mMessage = comment.getString("message");
					c.mCreatedTime = comment.getString("created_time");
					mComments.add(c);					
				}
			}
		}	
	}

	void update(JSONObject postObj) throws JSONException {
		mType = postObj.getString("type");
		mFromId = postObj.getJSONObject("from").getString("id");
		mFromName = postObj.getJSONObject("from").getString("name");

		// TODO: Handle multiple receivers?
		JSONObject to = postObj.optJSONObject("to");
		if (to != null) {
			mToId = to.getJSONArray("data").getJSONObject(0).getString("id");
			mToName = to.getJSONArray("data").getJSONObject(0)
					.getString("name");
		} else {
			mToId = mToName = null;
		}

		mMessage = postObj.optString("message", null);
		mPicture = postObj.optString("picture", null);
		mLink = postObj.optString("link", null);
		mName = postObj.optString("name", null);
		mCaption = postObj.optString("caption", null);
		mDescription = postObj.optString("description", null);
		mCreatedTime = postObj.optString("created_time", null);

		mComments.clear();
		if (postObj.optJSONObject("comments") != null) {
			JSONObject comments = postObj.getJSONObject("comments");
			JSONArray commentsData = comments.optJSONArray("data");
			if (commentsData != null) {
				for (int j = 0; j < commentsData.length(); ++j) {
					JSONObject comment = commentsData.getJSONObject(j);
					FBComment c = new FBComment(comment.getString("id"));
					// TODO: What to do with null values here.
					if (comment.optJSONObject("from") != null) {
						c.mFromId = comment.getJSONObject("from").getString(
								"id");
						c.mFromName = comment.getJSONObject("from").getString(
								"name");
					} else {
						c.mFromId = null;
						c.mFromName = null;
					}
					c.mMessage = comment.getString("message");
					c.mCreatedTime = comment.getString("created_time");
					mComments.add(c);
				}
			}
			mCommentsCount = comments.getInt("count");
		} else {
			mCommentsCount = 0;
		}

		if (postObj.optJSONObject("likes") != null) {
			mLikesCount = postObj.getJSONObject("likes").getInt("count");
		} else {
			mLikesCount = 0;
		}
	}

	/**
	 * Posts a comment to this post.
	 * 
	 * @param message
	 * @throws IOException
	 * @throws JSONException
	 */
	public void sendComment(String message) throws IOException, JSONException {
		Bundle params = new Bundle();
		params.putString(FBClient.TOKEN, mFBClient.getAccessToken());
		params.putString("message", message);
		mFBClient.request(mId + "/comments", params, "POST");
	}

}
