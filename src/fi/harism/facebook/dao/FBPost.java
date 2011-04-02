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

	private FBClient mFBClient;
	private String mId;
	String mType;
	String mFromId;
	String mFromName;
	String mMessage;
	String mPicture;
	String mLink;
	String mName;
	String mCaption;
	String mDescription;
	String mCreatedTime;
	Vector<FBComment> mComments;
	int mCommentsCount;
	int mLikesCount;

	FBPost(FBClient fbClient, String id) {
		mFBClient = fbClient;
		mId = id;
		mComments = new Vector<FBComment>();
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
		params.putString(FBClient.TOKEN, mFBClient.getAccessToken());
		params.putString("message", message);
		mFBClient.request(mId + "/comments", params, "POST");
	}

}
