package fi.harism.facebook.net;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import fi.harism.facebook.dao.DAONewsFeedItem;
import fi.harism.facebook.dao.DAONameAndPicture;
import fi.harism.facebook.dao.DAOMessage;
import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.ImageRequest;
import fi.harism.facebook.request.RequestQueue;

public class NetController {

	private FacebookClient facebookController = null;
	private DataCache imageCache = null;
	private RequestQueue requestController = null;

	private Vector<String> friendIdList = null;
	private HashMap<String, DAONameAndPicture> profileMap = null;
	private HashMap<String, DAOMessage> statusMap = null;
	private Vector<DAONewsFeedItem> newsFeedList = null;

	public NetController() {
		facebookController = new FacebookClient();
		imageCache = new DataCache(1024000);
		requestController = new RequestQueue();
		profileMap = new HashMap<String, DAONameAndPicture>();
		statusMap = new HashMap<String, DAOMessage>();
	}

	public void authorize(Activity activity, AuthorizeObserver observer) {
		facebookController.authorize(activity, observer);
	}

	public void authorizeCallback(int requestCode, int resultCode, Intent data) {
		facebookController.authorizeCallback(requestCode, resultCode, data);
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
			final RequestObserver<Vector<DAONameAndPicture>> observer) {

		if (friendIdList != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Vector<DAONameAndPicture> list = new Vector<DAONameAndPicture>();
					for (int i = 0; i < friendIdList.size(); ++i) {
						list.add(profileMap.get(friendIdList.elementAt(i)));
					}
					observer.onComplete(list);
				}
			});
		} else {
			Bundle b = new Bundle();
			b.putString("fields", "id,name,picture");
			FacebookRequest r = new FacebookRequest(activity, "me/friends", b,
					facebookController, new FacebookRequest.Observer() {
						@Override
						public void onComplete(FacebookRequest facebookRequest) {
							try {
								JSONArray friendArray = facebookRequest
										.getResponse().getJSONArray("data");

								Vector<DAONameAndPicture> friendList = new Vector<DAONameAndPicture>();
								for (int i = 0; i < friendArray.length(); ++i) {
									JSONObject f = friendArray.getJSONObject(i);
									String id = f.getString("id");
									String name = f.getString("name");
									String picture = f.getString("picture");
									friendList.add(new DAONameAndPicture(id,
											name, picture));
								}

								// Comparator for sorting friend JSONObjects by
								// name.
								Comparator<DAONameAndPicture> comparator = new Comparator<DAONameAndPicture>() {
									@Override
									public int compare(DAONameAndPicture arg0,
											DAONameAndPicture arg1) {
										String arg0Name = arg0.getName();
										String arg1Name = arg1.getName();
										return arg0Name
												.compareToIgnoreCase(arg1Name);
									}
								};

								// Sort friends Vector.
								Collections.sort(friendList, comparator);

								friendIdList = new Vector<String>();
								for (int i = 0; i < friendList.size(); ++i) {
									DAONameAndPicture profile = friendList
											.elementAt(i);
									friendIdList.add(profile.getId());
									profileMap.put(profile.getId(), profile);
								}

								observer.onComplete(friendList);
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

	public void getLatestStatus(Activity activity, final String id,
			final RequestObserver<DAOMessage> observer) {

		if (statusMap.containsKey(id)) {
			activity.runOnUiThread(new Runnable() {
				public void run() {
					observer.onComplete(statusMap.get(id));
				}
			});
		} else {
			Bundle b = new Bundle();
			b.putString("limit", "1");
			b.putString("fields", "message");
			FacebookRequest r = new FacebookRequest(activity, id + "/statuses",
					b, facebookController, new FacebookRequest.Observer() {
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

		if (profileMap.containsKey(id)) {
			activity.runOnUiThread(new Runnable() {
				public void run() {
					observer.onComplete(profileMap.get(id));
				}
			});
		} else {
			Bundle b = new Bundle();
			b.putString(FacebookClient.TOKEN,
					facebookController.getAccessToken());
			b.putString("fields", "id,name,picture");
			FacebookRequest r = new FacebookRequest(activity, id, b,
					facebookController, new FacebookRequest.Observer() {
						@Override
						public void onComplete(FacebookRequest facebookRequest) {
							try {
								JSONObject resp = facebookRequest.getResponse();
								String id = resp.getString("id");
								String name = resp.getString("name");
								String picture = resp.getString("picture");
								DAONameAndPicture r = new DAONameAndPicture(id,
										name, picture);
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
			final RequestObserver<Vector<DAONewsFeedItem>> observer) {

		if (newsFeedList != null) {
			activity.runOnUiThread(new Runnable() {
				public void run() {
					observer.onComplete(newsFeedList);
				}
			});
		} else {
			Bundle b = new Bundle();
			b.putString("fields",
					"id,type,from,message,picture,name,description,created_time");
			FacebookRequest r = new FacebookRequest(activity, "me/home", b,
					facebookController, new FacebookRequest.Observer() {
						@Override
						public void onComplete(FacebookRequest facebookRequest) {
							try {
								JSONArray feedItems = facebookRequest
										.getResponse().getJSONArray("data");
								newsFeedList = new Vector<DAONewsFeedItem>();
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
									newsFeedList.add(new DAONewsFeedItem(id,
											type, fromId, fromName, message,
											picture, name, description,
											createdTime));
								}
								observer.onComplete(newsFeedList);
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
