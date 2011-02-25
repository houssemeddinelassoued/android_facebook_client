package fi.harism.facebook;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.ImageRequest;
import fi.harism.facebook.request.RequestController;

public class FeedActivity extends BaseActivity {

	private RequestController requestController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed);

		requestController = new RequestController(this);

		FacebookRequest.Observer observer = new FacebookFeedObserver();
		FacebookRequest request = requestController.createFacebookRequest(
				"me/home", observer);
		requestController.addRequest(request);

		showProgressDialog();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		requestController.destroy();
		requestController = null;
	}

	@Override
	public void onPause() {
		super.onPause();
		requestController.pause();
	}

	@Override
	public void onResume() {
		super.onResume();
		requestController.resume();
	}

	private void createFeedItem(JSONObject feedItemObject) {
		String itemId = feedItemObject.optString("id", null);
		String fromName = "Error: 'from' not received.";
		String fromId = null;
		JSONObject fromObject = feedItemObject.optJSONObject("from");
		if (fromObject != null) {
			fromName = fromObject.optString("name",
					"Error: 'name' not reveived.");
			fromId = fromObject.optString("id", null);
		}

		String message = feedItemObject.optString("message",
				"Error: 'message' not received.");
		String created = feedItemObject.optString("created_time",
				"Error: 'created_time' not received.");

		View feedItemView = null;

		String type = feedItemObject.optString("type");
		if (type.equals("status")) {
			feedItemView = getLayoutInflater().inflate(
					R.layout.feed_item_status, null);
		} else if (type.equals("link")) {
			// TODO: Implement me.
			feedItemView = getLayoutInflater().inflate(
					R.layout.feed_item_status, null);
			message = "Description: " + feedItemObject.optString("description");
		} else if (type.equals("video")) {
			// TODO: Implement me.
			feedItemView = getLayoutInflater().inflate(
					R.layout.feed_item_status, null);
			message = "Description: " + feedItemObject.optString("description");
		} else if (type.equals("photo")) {
			// TODO: Implement me.
			feedItemView = getLayoutInflater().inflate(
					R.layout.feed_item_status, null);
			message = "Description: " + feedItemObject.optString("description");
		} else {
			showAlertDialog("Feed type '" + type + "' not implemented :(");
		}

		if (feedItemView != null) {
			if (itemId != null) {
				feedItemView.setTag(R.id.feed_item_id, itemId);
			}

			TextView nameView = (TextView) feedItemView
					.findViewById(R.id.feed_item_name);
			nameView.setText(fromName);
			TextView messageView = (TextView) feedItemView
					.findViewById(R.id.feed_item_message);
			messageView.setText(message);
			TextView createdView = (TextView) feedItemView
					.findViewById(R.id.feed_item_created);
			createdView.setText(created);

			LinearLayout itemList = (LinearLayout) findViewById(R.id.feed_list);
			itemList.addView(feedItemView);

			Bundle fromPictureParameters = new Bundle();
			fromPictureParameters.putString("fields", "id,picture");
			FacebookFromPictureObserver fromPictureObserver = new FacebookFromPictureObserver();
			FacebookRequest fromPictureRequest = requestController
					.createFacebookRequest(fromId, fromPictureParameters,
							fromPictureObserver);
			Bundle b = new Bundle();
			b.putString("itemId", itemId);
			fromPictureRequest.setBundle(b);
			requestController.addRequest(fromPictureRequest);
		}
	}

	private final class FacebookFromPictureObserver implements
			FacebookRequest.Observer {

		@Override
		public void onComplete(FacebookRequest facebookRequest) {
			String pictureUrl = facebookRequest.getJSONObject().optString(
					"picture", null);
			
			if (pictureUrl != null) {
				FromPictureObserver pictureObserver = new FromPictureObserver();
				ImageRequest r = requestController.createImageRequest(pictureUrl, pictureObserver);
				r.setBundle(facebookRequest.getBundle());
				r.setCacheBitmap(true);
				requestController.addRequestFirst(r);
			}
		}

		@Override
		public void onError(Exception ex) {
			// We don't care about errors.
		}

	}

	private final class FacebookFeedObserver implements
			FacebookRequest.Observer {

		@Override
		public void onComplete(FacebookRequest facebookRequest) {
			hideProgressDialog();
			// TODO: Sort feed array by creation time.
			JSONObject feedObject = facebookRequest.getJSONObject();
			JSONArray dataArray = feedObject.optJSONArray("data");
			if (dataArray != null) {
				for (int i = 0; i < dataArray.length(); ++i) {
					JSONObject feedItemObject = dataArray.optJSONObject(i);
					if (feedItemObject != null) {
						createFeedItem(feedItemObject);
					}
				}
			}
		}

		@Override
		public void onError(Exception ex) {
			hideProgressDialog();
		}

	}
	
	private final class FromPictureObserver implements ImageRequest.Observer {

		@Override
		public void onComplete(ImageRequest imageRequest) {
			String itemId = imageRequest.getBundle().getString("itemId");
			if (itemId != null) {
				LinearLayout itemList = (LinearLayout) findViewById(R.id.feed_list);
				for (int i=0; i<itemList.getChildCount(); ++i) {
					View v = itemList.getChildAt(i);
					if (itemId.equals(v.getTag(R.id.feed_item_id))) {
						ImageView iv = (ImageView)v.findViewById(R.id.feed_item_picture);
						iv.setImageBitmap(imageRequest.getBitmap());
						break;
					}
				}
			}
		}

		@Override
		public void onError(Exception ex) {
			// We don't care about errors.
		}
	}
}
