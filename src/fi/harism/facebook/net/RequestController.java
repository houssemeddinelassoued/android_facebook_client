package fi.harism.facebook.net;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import fi.harism.facebook.dao.DAOFriendList;
import fi.harism.facebook.dao.DAONewsFeedItem;
import fi.harism.facebook.dao.DAONameAndPicture;
import fi.harism.facebook.dao.DAOMessage;
import fi.harism.facebook.dao.DAONewsFeedList;
import fi.harism.facebook.dao.DAOObserver;
import fi.harism.facebook.dao.DAOProfile;
import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.ImageRequest;
import fi.harism.facebook.request.RequestQueue;

public class RequestController {

	private FacebookClient facebookClient = null;
	private DataCache imageCache = null;
	private RequestQueue requestController = null;
	
	private DAOFriendList friendList = null;
	private DAONewsFeedList newsFeedList = null;

	//private Vector<String> friendIdList = null;
	private HashMap<String, DAONameAndPicture> nameAndPictureMap = null;
	private HashMap<String, DAOMessage> statusMap = null;
	//private Vector<DAONewsFeedItem> newsFeedList = null;
	private HashMap<String, DAOProfile> profileMap = null;

	public RequestController() {
		facebookClient = new FacebookClient();
		imageCache = new DataCache(1024000);
		requestController = new RequestQueue();
		nameAndPictureMap = new HashMap<String, DAONameAndPicture>();
		statusMap = new HashMap<String, DAOMessage>();
		profileMap = new HashMap<String, DAOProfile>();
		
		friendList = new DAOFriendList(requestController, facebookClient);
		newsFeedList = new DAONewsFeedList(requestController, facebookClient);
	}

	public void authorize(Activity activity, AuthorizeObserver observer) {
		facebookClient.authorize(activity, observer);
	}

	public void authorizeCallback(int requestCode, int resultCode, Intent data) {
		facebookClient.authorizeCallback(requestCode, resultCode, data);
	}

	public void getBitmap(Activity activity, final String url,
			final RequestObserver<Bitmap> observer) {

		if (imageCache.containsKey(url)) {
			byte[] data = imageCache.getData(url);
			final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
					data.length);

			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					observer.onComplete(bitmap);
				}
			});
		} else {
			ImageRequest r = new ImageRequest(activity, url,
					new ImageRequest.Observer() {

						@Override
						public void onComplete(ImageRequest imageRequest) {
							imageCache.setData(url,
									imageRequest.getBitmapData());
							observer.onComplete(imageRequest.getBitmap());
						}

						@Override
						public void onError(Exception ex) {
							observer.onError(ex);
						}
					});
			requestController.addRequest(r);
		}
	}

	public void getFriendList(Activity activity,
			DAOObserver<DAOFriendList> observer) {
		friendList.getInstance(activity, observer);
	}

	public void getLatestStatus(Activity activity, final String id,
			final RequestObserver<DAOMessage> observer) {

		if (statusMap.containsKey(id)) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					observer.onComplete(statusMap.get(id));
				}
			});
		} else {
			Bundle b = new Bundle();
			b.putString("limit", "1");
			b.putString("fields", "message");
			FacebookRequest r = new FacebookRequest(activity, id + "/statuses",
					b, facebookClient, new FacebookRequest.Observer() {
						@Override
						public void onComplete(FacebookRequest facebookRequest) {
							try {
								JSONObject resp = facebookRequest.getResponse();
								String message = resp.getJSONArray("data")
										.getJSONObject(0).getString("message");
								DAOMessage r = new DAOMessage(message);
								statusMap.put(id, r);
								observer.onComplete(r);
							} catch (Exception ex) {
								observer.onError(ex);
							}
						}

						@Override
						public void onError(Exception ex) {
							observer.onError(ex);
						}
					});
			requestController.addRequest(r);
		}
	}

	public void getNameAndPicture(Activity activity, final String id,
			final RequestObserver<DAONameAndPicture> observer) {

		if (nameAndPictureMap.containsKey(id)) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					observer.onComplete(nameAndPictureMap.get(id));
				}
			});
		} else {
			Bundle b = new Bundle();
			b.putString(FacebookClient.TOKEN,
					facebookClient.getAccessToken());
			b.putString("fields", "id,name,picture");
			FacebookRequest r = new FacebookRequest(activity, id, b,
					facebookClient, new FacebookRequest.Observer() {
						@Override
						public void onComplete(FacebookRequest facebookRequest) {
							try {
								JSONObject resp = facebookRequest.getResponse();
								String id = resp.getString("id");
								String name = resp.getString("name");
								String picture = resp.getString("picture");
								DAONameAndPicture r = new DAONameAndPicture(id,
										name, picture);
								nameAndPictureMap.put(id, r);
								observer.onComplete(r);
							} catch (Exception ex) {
								observer.onError(ex);
							}
						}

						@Override
						public void onError(Exception ex) {
							observer.onError(ex);
						}
					});
			requestController.addRequest(r);
		}
	}

	public void getNewsFeed(Activity activity,
			final DAOObserver<DAONewsFeedList> observer) {
		newsFeedList.getInstance(activity, observer);
	}
	
	public void getProfile(Activity activity, final String id, final RequestObserver<DAOProfile> observer) {
		if (profileMap.containsKey(id)) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					observer.onComplete(profileMap.get(id));
				}
			});
		} else {
			FacebookRequest r = new FacebookRequest(activity, id, null,
					facebookClient, new FacebookRequest.Observer() {
						@Override
						public void onComplete(FacebookRequest facebookRequest) {
							try {
								JSONObject resp = facebookRequest.getResponse();
								StringWriter out = new StringWriter();
								
								Iterator keys = resp.keys();
								while (keys.hasNext()) {
									String key = (String)keys.next();
									String value = resp.getString(key);									
									out.write(key + ": ");
									out.write(value + "\n");
								}
								
								DAOProfile r = new DAOProfile(out.toString());
								profileMap.put(id, r);
								observer.onComplete(r);
							} catch (Exception ex) {
								observer.onError(ex);
							}
						}

						@Override
						public void onError(Exception ex) {
							observer.onError(ex);
						}
					});
			requestController.addRequest(r);
		}
	}

	public void removeRequests(Activity activity) {
		requestController.removeRequests(activity);
	}

	public void setPaused(Activity activity, boolean paused) {
		requestController.setPaused(activity, paused);
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
