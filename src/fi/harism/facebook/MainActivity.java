package fi.harism.facebook;

import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import fi.harism.facebook.dao.FBBitmap;
import fi.harism.facebook.dao.FBUser;
import fi.harism.facebook.net.FBClient;
import fi.harism.facebook.request.RequestUI;
import fi.harism.facebook.view.UserView;

/**
 * Main Activity of this application. Once Activity is launched it starts to
 * fetch default information from currently logged in user in Facebook API.
 * 
 * @author harism
 */
public class MainActivity extends BaseActivity {

	private static final int ID_WALL = 1;
	private static final int ID_NEWS = 2;
	private static final int ID_FRIENDS = 3;
	private static final int ID_CHAT = 4;
	private static final int ID_PROFILE = 5;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		getGlobalState().getFBClient().authorizeCallback(requestCode,
				resultCode, data);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);
		final Activity self = this;

		GridView gridView = (GridView) findViewById(R.id.activity_main_grid);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int pos,
					long id) {
				switch ((int) id) {
				case ID_NEWS: {
					Intent i = createIntent(FeedActivity.class);
					i.putExtra("fi.harism.facebook.FeedActivity.path",
							"me/home");
					i.putExtra(
							"fi.harism.facebook.FeedActivity.title",
							getResources().getString(
									R.string.activity_feed_news_title));
					startActivity(i);
					break;
				}
				case ID_WALL: {
					Intent i = createIntent(FeedActivity.class);
					i.putExtra("fi.harism.facebook.FeedActivity.path",
							"me/feed");
					i.putExtra(
							"fi.harism.facebook.FeedActivity.title",
							getResources().getString(
									R.string.activity_feed_profile_title));
					startActivity(i);
					break;
				}
				case ID_FRIENDS: {
					Intent i = createIntent(FriendsActivity.class);
					startActivity(i);
					break;
				}
				case ID_CHAT: {
					Intent i = createIntent(ChatActivity.class);
					startActivity(i);
					break;
				}
				case ID_PROFILE: {
					Intent i = createIntent(UserActivity.class);
					i.putExtra("fi.harism.facebook.UserActivity.user", "me");
					startActivity(i);
					break;
				}
				}
			}
		});

		GridAdapter gridAdapter = new GridAdapter();
		gridAdapter.addItem(R.drawable.pic_button_friends,
				R.string.activity_main_button_friends, ID_FRIENDS);
		gridAdapter.addItem(R.drawable.pic_button_feed,
				R.string.activity_main_button_news, ID_NEWS);
		gridAdapter.addItem(R.drawable.pic_button_feed,
				R.string.activity_main_button_wall, ID_WALL);
		gridAdapter.addItem(R.drawable.pic_button_profile,
				R.string.activity_main_button_profile, ID_PROFILE);
		gridAdapter.addItem(R.drawable.pic_button_chat,
				R.string.activity_main_button_chat, ID_CHAT);
		gridView.setAdapter(gridAdapter);

		// Add onClick listener to "Logout" button.
		View logoutButton = findViewById(R.id.activity_main_button_logout);
		logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LogoutObserver observer = new LogoutObserver(self);
				getGlobalState().getFBClient().logout(self, observer);
			}
		});

		// TODO: Should take care of situation in which user uses 'back' button
		// on login dialog.
		View userView = findViewById(R.id.activity_main_current_user);
		userView.setVisibility(View.GONE);

		if (getGlobalState().getFBClient().isAuthorized()) {
			loadUserInfo();
		} else {
			LoginObserver observer = new LoginObserver(this);
			getGlobalState().getFBClient().authorize(this, observer);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getGlobalState().getRequestQueue().removeRequests(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getGlobalState().getRequestQueue().setPaused(this, true);
	}

	@Override
	public void onResume() {
		super.onResume();
		getGlobalState().getRequestQueue().setPaused(this, false);
	}

	/**
	 * Loads currently logged in user's information.
	 */
	private void loadUserInfo() {
		// Update user information asynchronously if needed.
		FBUser fbUserMe = getGlobalState().getFBFactory().getUser("me");
		if (fbUserMe.getLevel() == FBUser.Level.FULL) {
			updateProfileInfo(fbUserMe);
		} else {
			FBUserRequest meRequest = new FBUserRequest(this, fbUserMe);
			getGlobalState().getRequestQueue().addRequest(meRequest);
		}
	}

	/**
	 * Updates user information to screen.
	 */
	private void updateProfileInfo(FBUser fbUserMe) {
		UserView userView = (UserView) findViewById(R.id.activity_main_current_user);
		userView.setVisibility(View.VISIBLE);

		userView.setName(fbUserMe.getName());
		userView.setContent(fbUserMe.getStatus());

		FBBitmap fbBitmapMe = getGlobalState().getFBFactory().getBitmap(
				fbUserMe.getPicture());
		if (fbBitmapMe.getBitmap() != null) {
			updateProfilePicture(fbBitmapMe);
		} else {
			userView.setPicture(getGlobalState().getDefaultPicture());
			FBBitmapRequest request = new FBBitmapRequest(this, fbBitmapMe);
			getGlobalState().getRequestQueue().addRequest(request);
		}
	}

	/**
	 * Updates user's picture.
	 */
	private void updateProfilePicture(FBBitmap fbBitmapMe) {
		UserView userView = (UserView) findViewById(R.id.activity_main_current_user);
		userView.setPicture(fbBitmapMe.getBitmap());
	}

	/**
	 * Class for handling profile picture request.
	 */
	private final class FBBitmapRequest extends RequestUI {

		private FBBitmap fbBitmap;

		public FBBitmapRequest(Activity activity, FBBitmap fbBitmap) {
			super(activity, activity);
			this.fbBitmap = fbBitmap;
		}

		@Override
		public void execute() throws Exception {
			fbBitmap.load();
		}

		@Override
		public void executeUI(Exception ex) {
			if (ex == null) {
				updateProfilePicture(fbBitmap);
			}
		}
	}

	/**
	 * Class for handling "me" request.
	 */
	private final class FBUserRequest extends RequestUI {

		private FBUser mFBUser;

		public FBUserRequest(Activity activity, FBUser fbUser) {
			super(activity, activity);
			mFBUser = fbUser;
		}

		@Override
		public void execute() throws Exception {
			mFBUser.load(FBUser.Level.FULL);
		}

		@Override
		public void executeUI(Exception ex) {
			if (ex == null) {
				updateProfileInfo(mFBUser);
			} else {
				// TODO: This is rather disastrous situation actually.
				showAlertDialog(ex.toString());
			}
		}

	}

	/**
	 * LoginObserver observer for Facebook authentication procedure.
	 */
	private final class LoginObserver implements FBClient.LoginObserver {

		private Activity mActivity;

		public LoginObserver(Activity activity) {
			mActivity = activity;
		}

		@Override
		public void onCancel() {
			// If user cancels login dialog let's simply close app.
			finish();
		}

		@Override
		public void onComplete() {
			// On successful login start loading logged in user's information.
			loadUserInfo();
		}

		@Override
		public void onError(Exception ex) {
			// On error trigger new login dialog.
			LoginObserver observer = new LoginObserver(mActivity);
			getGlobalState().getFBClient().authorize(mActivity, observer);
			// If there was an error during authorization show an alert to user.
			showAlertDialog(ex.getLocalizedMessage());
		}
	}

	/**
	 * LogoutObserver for handling asynchronous logout procedure.
	 */
	private final class LogoutObserver implements FBClient.LogoutObserver {

		private Activity mActivity;

		public LogoutObserver(Activity activity) {
			mActivity = activity;
		}

		@Override
		public void onComplete() {
			// First hide progress dialog.
			hideProgressDialog();
			getGlobalState().getFBFactory().reset();
			View userView = findViewById(R.id.activity_main_current_user);
			userView.setVisibility(View.GONE);
			// Trigger new login dialog.
			LoginObserver observer = new LoginObserver(mActivity);
			getGlobalState().getFBClient().authorize(mActivity, observer);
		}

		@Override
		public void onError(Exception ex) {
			// Hide progress dialog.
			hideProgressDialog();
			// Show error alert.
			showAlertDialog(ex.getLocalizedMessage());
		}
	}

	/**
	 * ListAdapter for populating grid view.
	 */
	private class GridAdapter extends BaseAdapter {

		private Vector<GridItem> mItems;

		public GridAdapter() {
			mItems = new Vector<GridItem>();
		}

		public void addItem(int pictureId, int textId, int id) {
			GridItem item = new GridItem();
			item.mPictureId = pictureId;
			item.mTextId = textId;
			item.mId = id;
			mItems.add(item);
		}

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mItems.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return mItems.get(arg0).mId;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			if (arg1 == null) {
				arg1 = getLayoutInflater().inflate(R.layout.view_imagebutton,
						null);
			}
			TextView text = (TextView) arg1
					.findViewById(R.id.view_imagebutton_text);
			ImageView picture = (ImageView) arg1
					.findViewById(R.id.view_imagebutton_image);
			text.setText(mItems.get(arg0).mTextId);
			picture.setImageResource(mItems.get(arg0).mPictureId);
			return arg1;
		}

		private class GridItem {
			int mTextId;
			int mPictureId;
			int mId;
		}
	}

}
