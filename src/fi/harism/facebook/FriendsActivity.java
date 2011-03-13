package fi.harism.facebook;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fi.harism.facebook.dao.DAOFriend;
import fi.harism.facebook.dao.DAOFriendList;
import fi.harism.facebook.dao.DAOObserver;
import fi.harism.facebook.net.RequestController;
import fi.harism.facebook.util.BitmapUtils;

/**
 * Friends list Activity. Once created it first loads "me/friends" from Facebook
 * Graph API. Once friend list is received it creates corresponding friend items
 * to view and triggers asynchronous loading of profile pictures.
 * 
 * This Activity implements also search functionality for friend list.
 * 
 * @author harism
 */
public class FriendsActivity extends BaseActivity {

	// RequestController instance.
	private RequestController requestController = null;
	// Default profile picture.
	private Bitmap defaultPicture = null;
	// Radius value for rounding profile images.
	private static final int PICTURE_ROUND_RADIUS = 7;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends);

		requestController = getGlobalState().getRequestController();

		// Add text changed observer to search editor.
		SearchEditorObserver searchObserver = new SearchEditorObserver();
		EditText searchEditor = (EditText) findViewById(R.id.friends_edit_search);
		searchEditor.addTextChangedListener(searchObserver);

		// Create default picture shared among friend items.
		defaultPicture = getGlobalState().getDefaultPicture();
		defaultPicture = BitmapUtils.roundBitmap(defaultPicture,
				PICTURE_ROUND_RADIUS);

		// Show progress dialog.
		showProgressDialog();
		// Trigger asynchronous friend list loading.
		requestController.getFriendList(this, new DAOFriendListObserver(this));
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
	 * This method creates a new View instance for user item. It sets user name
	 * to given value and stores userId as a tag to view for later use.
	 * 
	 * @param userId
	 *            User Id.
	 * @param name
	 *            User name.
	 * @return New friend item view.
	 */
	private final View createFriendItem(String userId, String name) {
		// Create new friend item View.
		View friendItemView = getLayoutInflater().inflate(
				R.layout.friends_item, null);

		// Find name TextView and set its value.
		TextView nameTextView = (TextView) friendItemView
				.findViewById(R.id.friends_item_name);
		nameTextView.setText(name);

		// Find picture ImageView and set default profile picture into it.
		ImageView imageView = (ImageView) friendItemView
				.findViewById(R.id.friends_item_picture);
		imageView.setImageBitmap(defaultPicture);

		// Store user id as a tag to friend item View.
		friendItemView.setTag(userId);

		// This is rather useless at the moment, as we are calling this method
		// mostly once search text has not been changed. But just in case for
		// the future, toggle friend item View's visibility according to search
		// text.
		EditText searchEditText = (EditText) findViewById(R.id.friends_edit_search);
		String searchText = searchEditText.getText().toString();
		toggleFriendItemVisibility(friendItemView, searchText);

		// Add onClick listener.
		friendItemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Do something more creative with user id here. Also it's
				// rather pointless to create new observer for every friend item
				// View.
				showAlertDialog((String) v.getTag());
			}
		});

		return friendItemView;
	}

	/**
	 * Toggles friend item View's visibility according to given search text.
	 * Method tries to find given search text within the name TextView found
	 * inside given friendItem.
	 * 
	 * @param friendItem
	 *            Friend item View.
	 * @param searchText
	 *            Current search text.
	 */
	private final void toggleFriendItemVisibility(View friendItem,
			String searchText) {
		// We are not case sensitive.
		searchText = searchText.toLowerCase();

		// Locate name TextView.
		TextView nameTextView = (TextView) friendItem
				.findViewById(R.id.friends_item_name);
		// Get name from TextView.
		String friendName = nameTextView.getText().toString();
		// We are still not case sensitive.
		friendName = friendName.toLowerCase();

		// Toggle friend item visibility regarding to if searchText is found
		// within name. This is rather naive approach but works good enough :)
		if (friendName.contains(searchText)) {
			friendItem.setVisibility(View.VISIBLE);
		} else {
			friendItem.setVisibility(View.GONE);
		}
	}

	/**
	 * Observer for handling "me/friends" request.
	 */
	private final class DAOFriendListObserver implements
			DAOObserver<DAOFriendList> {
		
		private Activity activity = null;

		public DAOFriendListObserver(Activity activity) {
			this.activity = activity;
		}

		@Override
		public void onComplete(DAOFriendList friendList) {
			// First hide progress dialog.
			hideProgressDialog();

			// LinearLayout which is inside ScrollView.
			LinearLayout scrollView = (LinearLayout) findViewById(R.id.friends_list);

			for (int i = 0; i < friendList.size(); ++i) {
				DAOFriend friend = friendList.at(i);

				String userId = friend.getId();
				String name = friend.getName();
				String pictureUrl = friend.getPictureUrl();

				// Create default friend item view.
				View friendItemView = createFriendItem(userId, name);
				// Add friend item view to scrollable list.
				scrollView.addView(friendItemView);

				requestController.getBitmap(activity, pictureUrl,
						new PictureObserver(userId));
			}
		}

		@Override
		public void onError(Exception ex) {
			// On error only hide progress dialog.
			hideProgressDialog();
		}
	}

	/**
	 * Observer for handling profile picture loading.
	 */
	private final class PictureObserver implements
			DAOObserver<Bitmap> {

		private String userId;

		public PictureObserver(String userId) {
			this.userId = userId;
		}

		@Override
		public void onComplete(Bitmap bitmap) {
			// Search for corresponding friend item View.
			View friendItemsView = findViewById(R.id.friends_list);
			View friendView = friendItemsView.findViewWithTag(userId);

			// If we found one.
			if (friendView != null) {
				// Try to find picture ImageView.
				ImageView imageView = (ImageView) friendView
						.findViewById(R.id.friends_item_picture);
				// Round its corners.
				bitmap = BitmapUtils.roundBitmap(bitmap, PICTURE_ROUND_RADIUS);
				// Update ImageView's bitmap with one received.
				imageView.setImageBitmap(bitmap);
			}
		}

		@Override
		public void onError(Exception ex) {
			// We don't care about errors.
		}
	}

	/**
	 * Observer for handling search text changes.
	 */
	private final class SearchEditorObserver implements TextWatcher {
		@Override
		public void afterTextChanged(Editable editable) {
			// Get editor text.
			String searchText = editable.toString();
			// Find LinearLayout containing our friend items.
			LinearLayout friendList = (LinearLayout) findViewById(R.id.friends_list);
			// Iterate through all child Views.
			for (int i = 0; i < friendList.getChildCount(); ++i) {
				LinearLayout friendItem = (LinearLayout) friendList
						.getChildAt(i);
				toggleFriendItemVisibility(friendItem, searchText);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// We are not interested in this callback.
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// We are not interested in this callback.
		}
	}

}
