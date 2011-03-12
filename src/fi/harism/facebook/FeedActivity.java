package fi.harism.facebook;

import java.util.Vector;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fi.harism.facebook.dao.DAOFeedItem;
import fi.harism.facebook.dao.DAONameAndPicture;
import fi.harism.facebook.net.NetController;
import fi.harism.facebook.util.BitmapUtils;

/**
 * Feed Activity for showing latest News Feed events for logged in user.
 * 
 * @author harism
 */
public class FeedActivity extends BaseActivity {

	// NetController instance.
	private NetController netController = null;
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

		netController = getGlobalState().getNetController();
		netController.getNewsFeed(this, new FacebookFeedObserver());

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		netController.removeRequests(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		netController.setPaused(this, true);
	}

	@Override
	public void onResume() {
		super.onResume();
		netController.setPaused(this, false);
	}

	/**
	 * Adds new feed item to this Activity's view.
	 * 
	 * @param feedItemObject
	 *            Feed item JSONObject to be added.
	 */
	private void createFeedItem(DAOFeedItem feedItem) {
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
		View messageView = feedItemView
				.findViewById(R.id.feed_item_message_layout);
		if (message != null) {
			TextView messageText = (TextView) messageView
					.findViewById(R.id.feed_item_message_text);
			messageText.setText(message);
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
		// TODO: Convert time to more readable format.
		String created = feedItem.getCreatedTime();
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

		netController.getNameAndPicture(this, fromId,
				new FacebookFromPictureObserver(this, itemId));

		if (feedItem.getPicture() != null) {
			netController.getBitmap(this, feedItem.getPicture(),
					new ItemPictureObserver(itemId));
		}
	}

	/**
	 * Private class for handling "me/home" Facebook request.
	 * 
	 * @author harism
	 */
	private final class FacebookFeedObserver implements
			NetController.RequestObserver<Vector<DAOFeedItem>> {

		@Override
		public void onComplete(Vector<DAOFeedItem> resp) {
			// First hide progress dialog.
			hideProgressDialog();

			for (int i = 0; i < resp.size(); ++i) {
				createFeedItem(resp.elementAt(i));
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
			NetController.RequestObserver<DAONameAndPicture> {

		private Activity activity = null;
		private String itemId = null;

		public FacebookFromPictureObserver(Activity activity, String itemId) {
			this.activity = activity;
			this.itemId = itemId;
		}

		@Override
		public void onComplete(DAONameAndPicture resp) {
			netController.getBitmap(activity, resp.getPicture(),
					new FromPictureObserver(itemId));
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
	private final class FromPictureObserver implements
			NetController.RequestObserver<Bitmap> {

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

	/**
	 * Private class for handling feed item picture requests.
	 * 
	 * @author harism
	 */
	private final class ItemPictureObserver implements
			NetController.RequestObserver<Bitmap> {

		private String itemId = null;

		public ItemPictureObserver(String itemId) {
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
}
