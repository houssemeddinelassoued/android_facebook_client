package fi.harism.facebook;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fi.harism.facebook.dao.DAONewsFeedItem;
import fi.harism.facebook.dao.DAONewsFeedList;
import fi.harism.facebook.dao.DAOObserver;
import fi.harism.facebook.dao.DAOProfile;
import fi.harism.facebook.net.RequestController;
import fi.harism.facebook.util.BitmapUtils;
import fi.harism.facebook.util.StringUtils;

/**
 * Feed Activity for showing latest News Feed events for logged in user.
 * 
 * @author harism
 */
public class FeedActivity extends BaseActivity {

	// RequestController instance.
	private RequestController requestController = null;
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

		requestController = getGlobalState().getRequestController();

		showProgressDialog();
		requestController.getNewsFeed(this, new DAONewsFeedListObserver());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		requestController.removeRequests(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		requestController.setPaused(this, true);
	}

	@Override
	public void onResume() {
		super.onResume();
		requestController.setPaused(this, false);
	}

	/**
	 * Adds new feed item to this Activity's view.
	 * 
	 * @param feedItemObject
	 *            Feed item JSONObject to be added.
	 */
	private void createFeedItem(DAONewsFeedItem feedItem) {
		String itemId = feedItem.getId();

		// Create default Feed Item view.
		View feedItemView = getLayoutInflater().inflate(R.layout.feed_item,
				null);
		// We use itemId to find this Feed Item if needed.
		feedItemView.setTag(itemId);

		// We need id of sender later on to trigger profile picture loading.
		String fromId = feedItem.getFromId();
		// Get sender's name or use empty string if none found.
		String fromName = feedItem.getFromName();

		// Set sender's name.
		TextView fromView = (TextView) feedItemView
				.findViewById(R.id.feed_item_from_text);
		fromView.setText(fromName);

		// Get message from feed item. Message is the one user can add as a
		// description to items posted.
		String message = feedItem.getMessage();
		TextView messageView = (TextView) feedItemView
				.findViewById(R.id.feed_item_message_text);
		if (message != null) {
			messageView.setText(message);
		} else {
			messageView.setVisibility(View.GONE);
		}

		// Get name from feed item. Name is shortish description like string
		// for feed item.
		String name = feedItem.getName();
		TextView nameView = (TextView) feedItemView
				.findViewById(R.id.feed_item_name_text);
		if (name != null) {
			nameView.setText(name);
		} else {
			nameView.setVisibility(View.GONE);
		}
		
		String caption = feedItem.getCaption();
		TextView captionView = (TextView) feedItemView
				.findViewById(R.id.feed_item_caption_text);
		if (caption != null) {
			captionView.setText(caption);
		} else {
			captionView.setVisibility(View.GONE);
		}

		// Get description from feed item. This is longer description for
		// feed item.
		String description = feedItem.getDescription();
		TextView descriptionView = (TextView) feedItemView
				.findViewById(R.id.feed_item_description_text);
		if (description != null) {
			descriptionView.setText(description);
		} else {
			descriptionView.setVisibility(View.GONE);
		}

		// Get created time from feed item.
		String created = feedItem.getCreatedTime();
		TextView createdView = (TextView) feedItemView
				.findViewById(R.id.feed_item_created_text);
		if (created != null) {
			createdView.setText(StringUtils.convertFBTime(created));
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

		requestController.getProfile(this, fromId, new DAOProfileObserver(this,
				itemId));

		if (feedItem.getPictureUrl() != null) {
			requestController.getBitmap(this, feedItem.getPictureUrl(),
					new FeedItemPictureObserver(itemId));
		}
	}

	/**
	 * Private class for handling "me/home" Facebook request.
	 * 
	 * @author harism
	 */
	private final class DAONewsFeedListObserver implements
			DAOObserver<DAONewsFeedList> {

		@Override
		public void onComplete(DAONewsFeedList newsFeedList) {
			// First hide progress dialog.
			hideProgressDialog();

			for (DAONewsFeedItem item : newsFeedList) {
				createFeedItem(item);
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
	private final class DAOProfileObserver implements DAOObserver<DAOProfile> {

		private Activity activity = null;
		private String itemId = null;

		public DAOProfileObserver(Activity activity, String itemId) {
			this.activity = activity;
			this.itemId = itemId;
		}

		@Override
		public void onComplete(DAOProfile profile) {
			requestController.getBitmap(activity, profile.getPictureUrl(),
					new FromPictureObserver(itemId));
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
	private final class FeedItemPictureObserver implements DAOObserver<Bitmap> {

		private String itemId = null;

		public FeedItemPictureObserver(String itemId) {
			this.itemId = itemId;
		}

		@Override
		public void onComplete(Bitmap bitmap) {
			// Get feed item list view.
			View itemList = findViewById(R.id.feed_list);
			// Find feed item using itemId.
			View itemView = itemList.findViewWithTag(itemId);
			// This shouldn't happen but just in case.
			if (itemView != null) {
				// Set image to feed item.
				ImageView iv = (ImageView) itemView
						.findViewById(R.id.feed_item_picture_image);
				iv.setImageBitmap(bitmap);
				iv.setVisibility(View.VISIBLE);
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
	private final class FromPictureObserver implements DAOObserver<Bitmap> {

		private String itemId = null;

		public FromPictureObserver(String itemId) {
			this.itemId = itemId;
		}

		@Override
		public void onComplete(Bitmap bitmap) {
			// Get feed item list view.
			View itemList = findViewById(R.id.feed_list);
			// Find our item view using itemId.
			View itemView = itemList.findViewWithTag(itemId);
			// This shouldn't happen but just in case.
			if (itemView != null) {
				// Set image to feed item view.
				ImageView iv = (ImageView) itemView
						.findViewById(R.id.feed_item_from_image);
				iv.setImageBitmap(BitmapUtils.roundBitmap(bitmap,
						PICTURE_ROUND_RADIUS));
			}
		}

		@Override
		public void onError(Exception ex) {
			// We don't care about errors.
		}
	}
}
