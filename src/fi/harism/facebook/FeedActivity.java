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
		if (itemId != null) {
			View feedItemView = getLayoutInflater().inflate(R.layout.feed_item,
					null);
			feedItemView.setTag(itemId);

			String fromName = null;
			String fromId = null;
			JSONObject fromObject = feedItemObject.optJSONObject("from");
			if (fromObject != null) {
				fromName = fromObject.optString("name", null);
				fromId = fromObject.optString("id", null);
			}

			View fromView = feedItemView
					.findViewById(R.id.feed_item_from_layout);
			if (fromName != null) {
				TextView tv = (TextView) fromView
						.findViewById(R.id.feed_item_from_text);
				tv.setText(fromName);
			} else {
				fromView.setVisibility(View.GONE);
			}

			String message = feedItemObject.optString("message", null);
			View messageView = feedItemView
					.findViewById(R.id.feed_item_message_layout);
			if (message != null) {
				TextView tv = (TextView) messageView
						.findViewById(R.id.feed_item_message_text);
				tv.setText(message);
			} else {
				messageView.setVisibility(View.GONE);
			}

			String name = feedItemObject.optString("name", null);
			View nameView = feedItemView
					.findViewById(R.id.feed_item_name_layout);
			if (name != null) {
				TextView tv = (TextView) nameView
						.findViewById(R.id.feed_item_name_text);
				tv.setText(name);
			} else {
				nameView.setVisibility(View.GONE);
			}

			String description = feedItemObject.optString("description", null);
			View descriptionView = feedItemView
					.findViewById(R.id.feed_item_description_layout);
			if (description != null) {
				TextView tv = (TextView) descriptionView
						.findViewById(R.id.feed_item_description_text);
				tv.setText(description);
			} else {
				descriptionView.setVisibility(View.GONE);
			}

			String created = feedItemObject.optString("created_time", null);
			View createdView = feedItemView
					.findViewById(R.id.feed_item_created_layout);
			if (created != null) {
				TextView tv = (TextView) createdView
						.findViewById(R.id.feed_item_created_text);
				tv.setText(created);
			} else {
				createdView.setVisibility(View.GONE);
			}

			LinearLayout itemList = (LinearLayout) findViewById(R.id.feed_list);
			itemList.addView(feedItemView);

			if (fromId != null) {
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

			String pictureUrl = feedItemObject.optString("picture", null);
			if (pictureUrl != null) {
				ItemPictureObserver pictureObserver = new ItemPictureObserver();
				ImageRequest r = requestController.createImageRequest(
						pictureUrl, pictureObserver);
				Bundle b = new Bundle();
				b.putString("itemId", itemId);
				r.setBundle(b);
				requestController.addRequestFirst(r);
			}

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

	private final class FacebookFromPictureObserver implements
			FacebookRequest.Observer {

		@Override
		public void onComplete(FacebookRequest facebookRequest) {
			String pictureUrl = facebookRequest.getJSONObject().optString(
					"picture", null);

			if (pictureUrl != null) {
				FromPictureObserver pictureObserver = new FromPictureObserver();
				ImageRequest r = requestController.createImageRequest(
						pictureUrl, pictureObserver);
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

	private final class FromPictureObserver implements ImageRequest.Observer {

		@Override
		public void onComplete(ImageRequest imageRequest) {
			String itemId = imageRequest.getBundle().getString("itemId");
			if (itemId != null) {
				View itemList = findViewById(R.id.feed_list);
				View itemView = itemList.findViewWithTag(itemId);
				if (itemView != null) {
					ImageView iv = (ImageView) itemView
							.findViewById(R.id.feed_item_from_image);
					iv.setImageBitmap(imageRequest.getBitmap());
				}
			}
		}

		@Override
		public void onError(Exception ex) {
			// We don't care about errors.
		}
	}

	private final class ItemPictureObserver implements ImageRequest.Observer {

		@Override
		public void onComplete(ImageRequest imageRequest) {
			String itemId = imageRequest.getBundle().getString("itemId");
			if (itemId != null) {
				View itemList = findViewById(R.id.feed_list);
				View itemView = itemList.findViewWithTag(itemId);
				if (itemView != null) {
					ImageView iv = (ImageView) itemView
							.findViewById(R.id.feed_item_picture_image);
					iv.setImageBitmap(imageRequest.getBitmap());

					View pictureLayout = itemView
							.findViewById(R.id.feed_item_picture_layout);
					pictureLayout.setVisibility(View.VISIBLE);
				}
			}
		}

		@Override
		public void onError(Exception ex) {
			// We don't care about errors.
		}
	}
}
