package fi.harism.facebook.net;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import fi.harism.facebook.dao.DAOFeedItem;
import fi.harism.facebook.dao.DAONameAndPicture;
import fi.harism.facebook.dao.DAOStatus;
import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.ImageRequest;
import fi.harism.facebook.request.RequestQueue;

public class NetController {
	
	private FacebookClient facebookController = null;
	private DataCache dataCache = null;
	private RequestQueue requestController = null;

	
	public NetController() {
		facebookController = new FacebookClient();
		dataCache = new DataCache();
		requestController = new RequestQueue();		
	}
	
	public void authorize(Activity activity, AuthorizeObserver observer) {
		facebookController.authorize(activity, observer);
	}
	
	public void authorizeCallback(int requestCode, int resultCode, Intent data) {
		facebookController.authorizeCallback(requestCode, resultCode, data);
	}
	
	public void getNameAndPicture(Activity activity, String id, final RequestObserver<DAONameAndPicture> observer) {
		Bundle b = new Bundle();
		b.putString(FacebookClient.TOKEN, facebookController.getAccessToken());
		b.putString("fields", "id,name,picture");
		FacebookRequest r = new FacebookRequest(activity, id, b, facebookController, new FacebookRequest.Observer() {
			@Override
			public void onError(Exception ex) {
				observer.onError(ex);
			}
			@Override
			public void onComplete(FacebookRequest facebookRequest) {
				try {
					JSONObject resp = facebookRequest.getResponse();
					String id = resp.getString("id");
					String name = resp.getString("name");
					String picture = resp.getString("picture");
					DAONameAndPicture r = new DAONameAndPicture(id, name, picture);					
					observer.onComplete(r);
				}
				catch (Exception ex) {
					observer.onError(ex);
				}
			}
		});
		requestController.addRequest(r);		
	}
	
	public void getStatus(Activity activity, String id, final RequestObserver<DAOStatus> observer) {
		Bundle b = new Bundle();
		b.putString("limit", "1");
		b.putString("fields", "message");
		FacebookRequest r = new FacebookRequest(activity, id + "/statuses", b, facebookController, new FacebookRequest.Observer() {
			@Override
			public void onError(Exception ex) {
				observer.onError(ex);
			}
			@Override
			public void onComplete(FacebookRequest facebookRequest) {
				try {
					JSONObject resp = facebookRequest.getResponse();
					String message = resp.getJSONArray("data").getJSONObject(0).getString("message");
					DAOStatus r = new DAOStatus(message);
					observer.onComplete(r);
				}
				catch (Exception ex) {
					observer.onError(ex);
				}
			}
		});
		requestController.addRequest(r);		
	}
	
	public void getFriendList(Activity activity, final RequestObserver<Vector<DAONameAndPicture>> observer) {
		Bundle b = new Bundle();
		b.putString("fields", "id,name,picture");
		FacebookRequest r = new FacebookRequest(activity, "me/friends", b, facebookController, new FacebookRequest.Observer() {
			@Override
			public void onError(Exception ex) {
				observer.onError(ex);
			}
			@Override
			public void onComplete(FacebookRequest facebookRequest) {
				try {
					JSONArray friendArray = facebookRequest.getResponse().getJSONArray("data");
					
					Vector<DAONameAndPicture> friendList = new Vector<DAONameAndPicture>();
					for (int i = 0; i < friendArray.length(); ++i) {
						JSONObject f = friendArray.getJSONObject(i);
						String id = f.getString("id");
						String name = f.getString("name");
						String picture = f.getString("picture");
						friendList.add(new DAONameAndPicture(id, name, picture));
					}

					// Comparator for sorting friend JSONObjects by name.
					Comparator<DAONameAndPicture> comparator = new Comparator<DAONameAndPicture>() {
						@Override
						public int compare(DAONameAndPicture arg0, DAONameAndPicture arg1) {
							String arg0Name = arg0.getName();
							String arg1Name = arg1.getName();
							return arg0Name.compareToIgnoreCase(arg1Name);
						}
					};

					// Sort friends Vector.
					Collections.sort(friendList, comparator);
					
					observer.onComplete(friendList);
					
					//JSONObject resp = facebookRequest.getResponse();
					//String message = resp.getJSONArray("data").getJSONObject(0).getString("message");
					//FacebookStatus r = new FacebookStatus(message);
					//observer.onComplete(r);
				}
				catch (Exception ex) {
					observer.onError(ex);
				}
			}
		});
		requestController.addRequest(r);		
	}
	
	public void getNewsFeed(Activity activity, final RequestObserver<Vector<DAOFeedItem>> observer) {
		Bundle b = new Bundle();
		b.putString("fields", "id,from,message,picture,name,description,created_time");
		FacebookRequest r = new FacebookRequest(activity, "me/home", b, facebookController, new FacebookRequest.Observer() {
			@Override
			public void onError(Exception ex) {
				observer.onError(ex);
			}
			@Override
			public void onComplete(FacebookRequest facebookRequest) {
				try {
					JSONArray feedItems = facebookRequest.getResponse().getJSONArray("data");
					Vector<DAOFeedItem> feedList = new Vector<DAOFeedItem>();
					for (int i=0; i<feedItems.length(); ++i) {
						JSONObject item = feedItems.getJSONObject(i);
						String id = item.getString("id");
						String fromId = item.getJSONObject("from").getString("id");
						String fromName = item.getJSONObject("from").getString("name");
						String message = item.optString("message", null);
						String picture = item.optString("picture", null);
						String name = item.optString("name", null);
						String description = item.optString("description", null);
						String createdTime = item.optString("created_time", null);
						feedList.add(new DAOFeedItem(id, fromId, fromName, message, picture, name, description, createdTime));
					}
					observer.onComplete(feedList);
				}
				catch (Exception ex) {
					observer.onError(ex);
				}
			}
		});
		requestController.addRequest(r);		
	}
	
	public void getBitmap(Activity activity, String url, final RequestObserver<Bitmap> observer) {
		ImageRequest r = new ImageRequest(activity, url, new ImageRequest.Observer() {
			
			@Override
			public void onError(Exception ex) {
				observer.onError(ex);
			}
			
			@Override
			public void onComplete(ImageRequest imageRequest) {
				observer.onComplete(imageRequest.getBitmap());
			}
		});
		requestController.addRequest(r);
	}
	
	public void setPaused(Activity activity, boolean paused) {
		requestController.setPaused(activity, paused);
	}
	
	public void removeRequests(Activity activity) {
		requestController.removeRequests(activity);
	}
	
	
	public interface AuthorizeObserver {
		public void onCancel();
		public void onComplete();
		public void onError(Exception error);
	}
	
	public interface RequestObserver<T> {
		public void onComplete(T result);
		public void onError(Exception error);
	}
}
