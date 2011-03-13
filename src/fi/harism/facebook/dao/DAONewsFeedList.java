package fi.harism.facebook.dao;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;

import fi.harism.facebook.net.FacebookClient;
import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.RequestQueue;

public class DAONewsFeedList {
	
	private RequestQueue requestQueue = null;
	private FacebookClient facebookClient = null;
	private Vector<DAONewsFeedItem> feedItemList = null;
	
	public DAONewsFeedList(RequestQueue requestQueue, FacebookClient facebookClient) {
		this.requestQueue = requestQueue;
		this.facebookClient = facebookClient;
	}
	
	public void getInstance(Activity activity, final DAOObserver<DAONewsFeedList> observer) {
		final DAONewsFeedList self = this;
		if (feedItemList != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					observer.onComplete(self);
				}
			});
		} else {
			Bundle b = new Bundle();
			b.putString("fields",
					"id,type,from,message,picture,name,description,created_time");
			FacebookRequest r = new FacebookRequest(activity, "me/home", b,
					facebookClient, new FacebookRequest.Observer() {
						@Override
						public void onComplete(FacebookRequest facebookRequest) {
							try {
								JSONArray feedItems = facebookRequest
										.getResponse().getJSONArray("data");
								feedItemList = new Vector<DAONewsFeedItem>();
								for (int i = 0; i < feedItems.length(); ++i) {
									JSONObject item = feedItems
											.getJSONObject(i);
									String id = item.getString("id");
									String type = item.getString("type");
									String fromId = item.getJSONObject("from")
											.getString("id");
									String fromName = item
											.getJSONObject("from").getString(
													"name");
									String message = item.optString("message",
											null);
									String picture = item.optString("picture",
											null);
									String name = item.optString("name", null);
									String description = item.optString(
											"description", null);
									String createdTime = item.optString(
											"created_time", null);
									feedItemList.add(new DAONewsFeedItem(id,
											type, fromId, fromName, message,
											picture, name, description,
											createdTime));
								}
								observer.onComplete(self);
							} catch (Exception ex) {
								observer.onError(ex);
							}
						}

						@Override
						public void onError(Exception ex) {
							observer.onError(ex);
						}
					});
			requestQueue.addRequest(r);
		}		
	}
	
	public int size() {
		return feedItemList == null ? 0 : feedItemList.size();
	}

	public DAONewsFeedItem at(int index) {
		return feedItemList.elementAt(index);
	}

}
