package fi.harism.facebook.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.ImageRequest;
import fi.harism.facebook.request.RequestQueue;

public class Controller {
	
	private FacebookClient facebookController = null;
	private DataCache dataCache = null;
	private RequestQueue requestController = null;

	
	public Controller() {
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
	
	public void getNameAndPicture(Activity activity, String id, final RequestObserver<FacebookNameAndPicture> observer) {
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
					FacebookNameAndPicture r = new FacebookNameAndPicture(id, name, picture);					
					observer.onComplete(r);
				}
				catch (Exception ex) {
					observer.onError(ex);
				}
			}
		});
		requestController.addRequest(r);		
	}
	
	public void getStatus(Activity activity, String id, final RequestObserver<FacebookStatus> observer) {
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
					FacebookStatus r = new FacebookStatus(message);
					observer.onComplete(r);
				}
				catch (Exception ex) {
					observer.onError(ex);
				}
			}
		});
		requestController.addRequest(r);		
	}
	
	public void getFriendList(Activity activity, final RequestObserver<Vector<FacebookNameAndPicture>> observer) {
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
					
					Vector<FacebookNameAndPicture> friendList = new Vector<FacebookNameAndPicture>();
					for (int i = 0; i < friendArray.length(); ++i) {
						JSONObject f = friendArray.getJSONObject(i);
						String id = f.getString("id");
						String name = f.getString("name");
						String picture = f.getString("picture");
						friendList.add(new FacebookNameAndPicture(id, name, picture));
					}

					// Comparator for sorting friend JSONObjects by name.
					Comparator<FacebookNameAndPicture> comparator = new Comparator<FacebookNameAndPicture>() {
						@Override
						public int compare(FacebookNameAndPicture arg0, FacebookNameAndPicture arg1) {
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
	
	public void getNewsFeed(Activity activity, final RequestObserver<Vector<FacebookFeedItem>> observer) {
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
					Vector<FacebookFeedItem> feedList = new Vector<FacebookFeedItem>();
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
						feedList.add(new FacebookFeedItem(id, fromId, fromName, message, picture, name, description, createdTime));
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
	
	public void getBitmap(Activity activity, final String id, String url, final RequestObserver<FacebookBitmap> observer) {
		ImageRequest r = new ImageRequest(activity, url, new ImageRequest.Observer() {
			
			@Override
			public void onError(Exception ex) {
				observer.onError(ex);
			}
			
			@Override
			public void onComplete(ImageRequest imageRequest) {
				FacebookBitmap r = new FacebookBitmap(id, imageRequest.getBitmap());
				observer.onComplete(r);
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
