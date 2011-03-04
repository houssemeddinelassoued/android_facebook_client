package fi.harism.facebook;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.ImageRequest;
import fi.harism.facebook.request.Request;
import fi.harism.facebook.request.RequestController;
import fi.harism.facebook.util.BitmapUtils;

public class FeedActivity extends BaseActivity {

	private RequestController requestController;
	private Bitmap defaultPicture = null;
	private static final int PICTURE_ROUND_RADIUS = 7;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed);

		defaultPicture = getGlobalState().getDefaultPicture();
		defaultPicture = BitmapUtils.roundBitmap(defaultPicture,
				PICTURE_ROUND_RADIUS);

		requestController = new RequestController(this);

		Bundle requestParameters = new Bundle();
		requestParameters.putString("fields",
				"id,from,message,picture,name,description,created_time");
		FacebookRequest.Observer observer = new FacebookFeedObserver();
		FacebookRequest request = requestController.createFacebookRequest(
				"me/home", requestParameters, observer);
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
				fromName = fromObject.optString("name");
				fromId = fromObject.optString("id", null);

				TextView tv = (TextView) feedItemView
						.findViewById(R.id.feed_item_from_text);
				tv.setText(fromName);
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
			TextView nameView = (TextView) feedItemView
					.findViewById(R.id.feed_item_name_text);
			if (name != null) {
				nameView.setText(name);
			} else {
				nameView.setVisibility(View.GONE);
			}

			String description = feedItemObject.optString("description", null);
			TextView descriptionView = (TextView) feedItemView
					.findViewById(R.id.feed_item_description_text);
			if (description != null) {
				descriptionView.setText(description);
			} else {
				descriptionView.setVisibility(View.GONE);
			}

			String created = feedItemObject.optString("created_time", null);
			TextView createdView = (TextView) feedItemView
					.findViewById(R.id.feed_item_created_text);
			if (created != null) {
				createdView.setText(created);
			} else {
				createdView.setVisibility(View.GONE);
			}

			ImageView fromPictureImage = (ImageView) feedItemView
					.findViewById(R.id.feed_item_from_image);
			fromPictureImage.setImageBitmap(defaultPicture);

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
				r.setPriority(Request.PRIORITY_HIGH + 1);
				Bundle b = new Bundle();
				b.putString("itemId", itemId);
				r.setBundle(b);
				requestController.addRequest(r);
			}

		}
	}

	private final class FacebookFeedObserver implements
			FacebookRequest.Observer {

		@Override
		public void onComplete(FacebookRequest facebookRequest) {
			hideProgressDialog();
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
			showAlertDialog(ex.getLocalizedMessage());
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
				r.setPriority(Request.PRIORITY_HIGH);
				r.setBundle(facebookRequest.getBundle());
				r.setCacheBitmap(true);
				requestController.addRequest(r);
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
					Bitmap bitmap = imageRequest.getBitmap();
					iv.setImageBitmap(BitmapUtils.roundBitmap(bitmap,
							PICTURE_ROUND_RADIUS));
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
					iv.setVisibility(View.VISIBLE);
				}
			}
		}

		@Override
		public void onError(Exception ex) {
			// We don't care about errors.
		}
	}
}
