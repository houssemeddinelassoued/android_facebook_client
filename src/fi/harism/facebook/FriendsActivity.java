package fi.harism.facebook;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import fi.harism.facebook.dao.FBBitmap;
import fi.harism.facebook.dao.FBFriendList;
import fi.harism.facebook.dao.FBUser;
import fi.harism.facebook.request.RequestQueue;
import fi.harism.facebook.request.RequestUI;
import fi.harism.facebook.util.BitmapUtils;
import fi.harism.facebook.view.UserView;

/**
 * Friends list Activity. Once created it first loads "me/friends" from Facebook
 * Graph API. Once friend list is received it creates corresponding friend views
 * to view and triggers asynchronous loading of profile pictures.
 * 
 * This Activity implements also search functionality for friend list.
 * 
 * @author harism
 */
public class FriendsActivity extends BaseActivity {

	// RequestQueue instance.
	private RequestQueue mRequestQueue;
	// Default profile picture.
	private Bitmap mDefaultPicture;
	// OnClickListener for FriendViews.
	private FriendViewClickObserver mFriendViewClickObserver;
	// Radius value for rounding profile images.
	private static final int PICTURE_ROUND_RADIUS = 7;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_friends);

		// Add text changed observer to search editor.
		SearchEditorObserver searchObserver = new SearchEditorObserver();
		EditText searchEditor = (EditText) findViewById(R.id.activity_friends_search_edit);
		searchEditor.addTextChangedListener(searchObserver);

		// Create default picture shared among friend items.
		mDefaultPicture = getGlobalState().getDefaultPicture();
		mDefaultPicture = BitmapUtils.roundBitmap(mDefaultPicture,
				PICTURE_ROUND_RADIUS);

		mRequestQueue = getGlobalState().getRequestQueue();
		mFriendViewClickObserver = new FriendViewClickObserver();

		// Trigger asynchronous friend list loading if needed.
		FBFriendList fbFriendList = getGlobalState().getFBFactory()
				.getFriendList();
		if (fbFriendList.getFriends().size() > 0) {
			updateFriendList(fbFriendList);
		} else {
			// Show progress dialog.
			showProgressDialog();
			FBFriendListRequest request = new FBFriendListRequest(this,
					fbFriendList);
			mRequestQueue.addRequest(request);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mRequestQueue.removeRequests(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mRequestQueue.setPaused(this, true);
	}

	@Override
	public void onResume() {
		super.onResume();
		mRequestQueue.setPaused(this, false);
	}

	/**
	 * Toggles friend view's visibility according to given search text. Method
	 * tries to find given search text within the name TextView found inside
	 * given friendItem.
	 * 
	 * @param friendItem
	 *            Friend item View.
	 * @param searchText
	 *            Current search text.
	 */
	private final void toggleFriendViewVisibility(UserView friendView,
			String searchText) {
		// We are not case sensitive.
		searchText = searchText.toLowerCase();

		// Get name from TextView.
		String name = friendView.getName();
		// We are still not case sensitive.
		name = name.toLowerCase();

		// Toggle friend item visibility regarding to if searchText is found
		// within name. This is rather naive approach but works good enough :)
		if (name.contains(searchText)) {
			friendView.setVisibility(View.VISIBLE);
		} else {
			friendView.setVisibility(View.GONE);
		}
	}

	/**
	 * Updates friend list to screen.
	 */
	private final void updateFriendList(FBFriendList fbFriendList) {
		// LinearLayout which is inside ScrollView.
		LinearLayout friendsView = (LinearLayout) findViewById(R.id.activity_friends_content);
		friendsView.setVisibility(View.GONE);
		friendsView.removeAllViews();

		// Sort friend list.
		Vector<FBUser> friends = fbFriendList.getFriends();
		Comparator<FBUser> comparator = new Comparator<FBUser>() {
			@Override
			public int compare(FBUser object1, FBUser object2) {
				return object1.getName().compareToIgnoreCase(object2.getName());
			}
		};
		Collections.sort(friends, comparator);

		// Create FriendView for each friend.
		for (FBUser friend : friends) {
			String userId = friend.getId();
			String name = friend.getName();
			String pictureUrl = friend.getPicture();

			// Create default friend item view.
			UserView friendView = (UserView) getLayoutInflater().inflate(
					R.layout.view_user, null);
			friendView.setName(name);
			friendView.setTag(userId);
			friendView.setOnClickListener(mFriendViewClickObserver);

			String affiliations = TextUtils
					.join(", ", friend.getAffiliations());
			friendView.setDetails(affiliations);

			// Add friend item view to scrollable list.
			friendsView.addView(friendView);

			FBBitmap picture = getGlobalState().getFBFactory().getBitmap(
					pictureUrl);
			Bitmap bitmap = picture.getBitmap();
			if (bitmap != null) {
				friendView.setPicture(BitmapUtils.roundBitmap(bitmap,
						PICTURE_ROUND_RADIUS));
			} else {
				friendView.setPicture(mDefaultPicture);
				FBBitmapRequest request = new FBBitmapRequest(this, friendView,
						picture);
				mRequestQueue.addRequest(request);
			}
		}
		
		friendsView.setVisibility(View.VISIBLE);
	}

	/**
	 * Request for handling profile picture loading.
	 */
	private final class FBBitmapRequest extends RequestUI {

		private UserView mFriendView;
		private FBBitmap mFBBitmap;
		private Bitmap mBitmap;

		public FBBitmapRequest(Activity activity, UserView friendView,
				FBBitmap fbBitmap) {
			super(activity, activity);
			mFriendView = friendView;
			mFBBitmap = fbBitmap;
		}

		@Override
		public void execute() throws Exception {
			mFBBitmap.load();
			mBitmap = BitmapUtils.roundBitmap(mFBBitmap.getBitmap(),
					PICTURE_ROUND_RADIUS);
		}

		@Override
		public void executeUI(Exception ex) {
			if (ex == null) {
				mFriendView.setPicture(mBitmap);
			}
		}
	}

	/**
	 * Request for handling "me/friends" loading.
	 */
	private final class FBFriendListRequest extends RequestUI {

		private FBFriendList mFBFriendList;

		public FBFriendListRequest(Activity activity, FBFriendList fbFriendList) {
			super(activity, activity);
			mFBFriendList = fbFriendList;
		}

		@Override
		public void execute() throws Exception {
			mFBFriendList.load();
		}

		@Override
		public void executeUI(Exception ex) {
			if (ex == null) {
				updateFriendList(mFBFriendList);
			} else {
				showAlertDialog(ex.toString());				
			}
			hideProgressDialog();
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
			LinearLayout friendList = (LinearLayout) findViewById(R.id.activity_friends_content);
			// Iterate through all child Views.
			for (int i = 0; i < friendList.getChildCount(); ++i) {
				UserView friendView = (UserView) friendList.getChildAt(i);
				toggleFriendViewVisibility(friendView, searchText);
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
	 * Private class for handling FriendView clicking.
	 */
	private class FriendViewClickObserver implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			String userId = (String) v.getTag();
			Intent i = createIntent(UserActivity.class);
			i.putExtra("fi.harism.facebook.UserActivity.user", userId);
			startActivity(i);
		}
	}

}
