package fi.harism.facebook;

import java.util.Vector;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fi.harism.facebook.dao.FBBitmap;
import fi.harism.facebook.dao.FBBitmapCache;
import fi.harism.facebook.dao.FBFriend;
import fi.harism.facebook.dao.FBFriendList;
import fi.harism.facebook.dao.FBObserver;
import fi.harism.facebook.util.BitmapUtils;
import fi.harism.facebook.util.FacebookURLSpan;
import fi.harism.facebook.util.StringUtils;

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

	private FBBitmapCache fbBitmapCache;
	private FBFriendList fbFriendList;
	private FBBitmapObserver fbBitmapObserver;

	// Default profile picture.
	private Bitmap defaultPicture = null;
	// Radius value for rounding profile images.
	private static final int PICTURE_ROUND_RADIUS = 7;
	// Span onClick observer for profile and comments protocols.
	private SpanClickObserver spanClickObserver = null;
	// Static protocol name for showing profile.
	private static final String PROTOCOL_SHOW_PROFILE = "showprofile://";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.friends);

		spanClickObserver = new SpanClickObserver(this);
		fbBitmapCache = getGlobalState().getFBFactory().getBitmapCache();
		fbFriendList = getGlobalState().getFBFactory().getFriendList();
		fbBitmapObserver = new FBBitmapObserver();

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
		fbFriendList.load(new FBFriendListObserver());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		fbFriendList.cancel();
		fbBitmapCache.cancel();
	}

	@Override
	public void onPause() {
		super.onPause();
		fbFriendList.setPaused(true);
		fbBitmapCache.setPaused(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		fbFriendList.setPaused(false);
		fbBitmapCache.setPaused(false);
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
				.findViewById(R.id.friends_item_text_name);
		StringUtils.setTextLink(nameTextView, name, PROTOCOL_SHOW_PROFILE
				+ userId, spanClickObserver);
		// nameTextView.setText(name);

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
				.findViewById(R.id.friends_item_text_name);
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
	 * Observer for handling profile picture loading.
	 */
	private final class FBBitmapObserver implements Runnable,
			FBObserver<FBBitmap> {

		private Vector<FBBitmap> waitingList = new Vector<FBBitmap>();

		@Override
		public void onComplete(FBBitmap bitmap) {
			if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
				update(bitmap);
			} else {
				if (waitingList.size() == 0) {
					waitingList.addElement(bitmap);
					runOnUiThread(this);
				} else {
					waitingList.addElement(bitmap);					
				}
			}
		}

		@Override
		public void onError(Exception ex) {
			// We don't care about errors.
		}

		@Override
		public void run() {
			while (waitingList.size() > 0) {
				FBBitmap bitmap = waitingList.remove(0);
				update(bitmap);
			}
		}

		public void update(FBBitmap bitmap) {
			// Search for corresponding friend item View.
			View friendItemsView = findViewById(R.id.friends_list);
			View friendView = friendItemsView.findViewWithTag(bitmap.getId());

			// If we found one.
			if (friendView != null) {
				// Try to find picture ImageView.
				ImageView imageView = (ImageView) friendView
						.findViewById(R.id.friends_item_picture);
				// Round its corners.
				Bitmap rounded = BitmapUtils.roundBitmap(bitmap.getBitmap(),
						PICTURE_ROUND_RADIUS);
				// Update ImageView's bitmap with one received.
				imageView.setImageBitmap(rounded);
			}
		}
	}

	/**
	 * Observer for handling "me/friends" request.
	 */
	private final class FBFriendListObserver implements
			FBObserver<FBFriendList>, Runnable {

		private FBFriendList friendList;

		@Override
		public void onComplete(final FBFriendList friendList) {
			this.friendList = friendList;
			if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
				run();
			} else {
				runOnUiThread(this);
			}
		}

		@Override
		public void onError(Exception ex) {
			// On error only hide progress dialog.
			hideProgressDialog();
		}

		@Override
		public void run() {
			// First hide progress dialog.
			hideProgressDialog();

			// LinearLayout which is inside ScrollView.
			LinearLayout scrollView = (LinearLayout) findViewById(R.id.friends_list);

			for (FBFriend friend : friendList) {
				String userId = friend.getId();
				String name = friend.getName();
				String pictureUrl = friend.getPicture();

				// Create default friend item view.
				View friendItemView = createFriendItem(userId, name);
				// Add friend item view to scrollable list.
				scrollView.addView(friendItemView);

				fbBitmapCache.load(pictureUrl, userId, fbBitmapObserver);
			}
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

	/**
	 * Click listener for our own protocols. Rest is handled by default handler.
	 */
	private final class SpanClickObserver implements
			FacebookURLSpan.ClickObserver {
		private BaseActivity activity = null;

		public SpanClickObserver(BaseActivity activity) {
			this.activity = activity;
		}

		@Override
		public boolean onClick(FacebookURLSpan span) {
			String url = span.getURL();
			if (url.startsWith(PROTOCOL_SHOW_PROFILE)) {
				showAlertDialog(url);
				return true;
			}
			return false;
		}
	}

}
