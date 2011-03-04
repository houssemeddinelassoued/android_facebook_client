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

/**
 * Feed Activity for showing latest News Feed events for logged in user.
 * 
 * @author harism
 */
public class FeedActivity extends BaseActivity {

	// Local instance of RequestController.
	private RequestController requestController;
	// Default picture used as sender's profile picture.
	private Bitmap defaultPicture = null;
	// Rounding radius for user picture.
	// TODO: Move this value to resources instead.
	private static final int PICTURE_ROUND_RADIUS = 7;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed);

		// Create default picture from resources.
		defaultPicture = getGlobalState().getDefaultPicture();
		defaultPicture = BitmapUtils.roundBitmap(defaultPicture,
				PICTURE_ROUND_RADIUS);

		// Our RequestController instance.
		requestController = new RequestController(this);

		// Trigger asynchronous news feed request.
		Bundle requestParameters = new Bundle();
		// We are interested only in this fields.
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

	/**
	 * Adds new feed item to this Activity's view.
	 * 
	 * @param feedItemObject
	 *            Feed item JSONObject to be added.
	 */
	private void createFeedItem(JSONObject feedItemObject) {
		// Every item is supposed to have id.
		String itemId = feedItemObject.optString("id", null);
		if (itemId != null) {
			// Create default Feed Item view.
			View feedItemView = getLayoutInflater().inflate(R.layout.feed_item,
					null);
			// We use itemId to find this Feed Item if needed.
			feedItemView.setTag(itemId);

			// We need id of sender later on to trigger profile picture loading.
			String fromId = null;
			// Supposedly every feed item has from object.
			JSONObject fromObject = feedItemObject.optJSONObject("from");
			if (fromObject != null) {
				// Get sender's name or use empty string if none found.
				String fromName = fromObject.optString("name");
				// Get sender's id or set it to null if none found.
				fromId = fromObject.optString("id", null);

				// Set sender's name.
				TextView tv = (TextView) feedItemView
						.findViewById(R.id.feed_item_from_text);
				tv.setText(fromName);
			}

			// Get message from feed item. Message is the one user can add as a
			// description to items posted.
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

			// Get name from feed item. Name is shortish description like string
			// for feed item.
			String name = feedItemObject.optString("name", null);
			TextView nameView = (TextView) feedItemView
					.findViewById(R.id.feed_item_name_text);
			if (name != null) {
				nameView.setText(name);
			} else {
				nameView.setVisibility(View.GONE);
			}

			// Get description from feed item. This is longer description for
			// feed item.
			String description = feedItemObject.optString("description", null);
			TextView descriptionView = (TextView) feedItemView
					.findViewById(R.id.feed_item_description_text);
			if (description != null) {
				descriptionView.setText(description);
			} else {
				descriptionView.setVisibility(View.GONE);
			}

			// Get created time from feed item.
			// TODO: Convert time to more readable format.
			String created = feedItemObject.optString("created_time", null);
			TextView createdView = (TextView) feedItemView
					.findViewById(R.id.feed_item_created_text);
			if (created != null) {
				createdView.setText(created);
			} else {
				createdView.setVisibility(View.GONE);
			}

			// Set default picture as sender's picture.
			ImageView fromPictureImage = (ImageView) feedItemView
					.findViewById(R.id.feed_item_from_image);
			fromPictureImage.setImageBitmap(defaultPicture);

			// Add feed item to viewable list of items.
			LinearLayout itemList = (LinearLayout) findViewById(R.id.feed_list);
			itemList.addView(feedItemView);

			// If we have fromId trigger loading of profile picture.
			if (fromId != null) {
				// We are interested in picture url only.
				Bundle fromPictureParameters = new Bundle();
				fromPictureParameters.putString("fields", "id,picture");
				FacebookFromPictureObserver fromPictureObserver = new FacebookFromPictureObserver();
				FacebookRequest fromPictureRequest = requestController
						.createFacebookRequest(fromId, fromPictureParameters,
								fromPictureObserver);
				// There's no rush for fetching profile pictures.
				fromPictureRequest.setPriority(Request.PRIORITY_LOW);
				// Add itemId to request so that we find this feed item once
				// request is completed.
				Bundle b = new Bundle();
				b.putString("itemId", itemId);
				fromPictureRequest.setBundle(b);
				requestController.addRequest(fromPictureRequest);
			}

			// If this feed item contains a picture trigger request to load it.
			String pictureUrl = feedItemObject.optString("picture", null);
			if (pictureUrl != null) {
				ItemPictureObserver pictureObserver = new ItemPictureObserver();
				ImageRequest r = requestController.createImageRequest(
						pictureUrl, pictureObserver);
				// Set priority to really high. We want to load these pictures
				// as soon as possible because they affect view's layout the
				// most.
				r.setPriority(Request.PRIORITY_HIGH);
				// Add itemId to this request so that we find this feed item
				// once request is done.
				Bundle b = new Bundle();
				b.putString("itemId", itemId);
				r.setBundle(b);
				requestController.addRequest(r);
			}

		}
	}

	/**
	 * Private class for handling "me/home" Facebook request.
	 * 
	 * @author harism
	 */
	private final class FacebookFeedObserver implements
			FacebookRequest.Observer {

		@Override
		public void onComplete(FacebookRequest facebookRequest) {
			// First hide progress dialog.
			hideProgressDialog();

			// Our news feed object.
			JSONObject feedObject = facebookRequest.getJSONObject();
			// Array of feed items.
			JSONArray dataArray = feedObject.optJSONArray("data");
			if (dataArray != null) {
				// Iterate through feed items.
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
			// Hide progress dialog.
			hideProgressDialog();
			// We don't want to see this happening but just in case.
			showAlertDialog(ex.getLocalizedMessage());
		}

	}

	/**
	 * Private class for handling sender's picture request.
	 * 
	 * @author harism
	 */
	private final class FacebookFromPictureObserver implements
			FacebookRequest.Observer {

		@Override
		public void onComplete(FacebookRequest facebookRequest) {
			// Get picture url from response.
			String pictureUrl = facebookRequest.getJSONObject().optString(
					"picture", null);

			// Trigger profile picture loading if we got an url.
			if (pictureUrl != null) {
				FromPictureObserver pictureObserver = new FromPictureObserver();
				ImageRequest r = requestController.createImageRequest(
						pictureUrl, pictureObserver);
				r.setPriority(Request.PRIORITY_NORMAL);
				// Forward request bundle to this new request.
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

	/**
	 * Private class for handling actual profile picture requests.
	 * 
	 * @author harism
	 */
	private final class FromPictureObserver implements ImageRequest.Observer {

		@Override
		public void onComplete(ImageRequest imageRequest) {
			// Get itemId from request.
			String itemId = imageRequest.getBundle().getString("itemId");
			// This shouldn't happen but just in case.
			if (itemId != null) {
				// Get feed item list view.
				View itemList = findViewById(R.id.feed_list);
				// Find our item view using itemId.
				View itemView = itemList.findViewWithTag(itemId);
				// This shouldn't happen but just in case.
				if (itemView != null) {
					// Set image to feed item view.
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

	/**
	 * Private class for handling feed item picture requests.
	 * 
	 * @author harism
	 */
	private final class ItemPictureObserver implements ImageRequest.Observer {

		@Override
		public void onComplete(ImageRequest imageRequest) {
			// Get itemId from request.
			String itemId = imageRequest.getBundle().getString("itemId");
			// This shouldn't happen but just in case.
			if (itemId != null) {
				// Get feed item list view.
				View itemList = findViewById(R.id.feed_list);
				// Find feed item using itemId.
				View itemView = itemList.findViewWithTag(itemId);
				// This shouldn't happen but just in case.
				if (itemView != null) {
					// Set image to feed item.
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
