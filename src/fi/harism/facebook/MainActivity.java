package fi.harism.facebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
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

		// Add onClick listener to "Friends" button. Button friendsButton =
		View.OnClickListener friendsListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// On click trigger friends activity.
				Intent i = createIntent(FriendsActivity.class);
				startActivity(i);
			}
		};
		updateButton(R.id.activity_main_button_friends,
				R.drawable.pic_button_friends,
				R.string.activity_main_button_friends, friendsListener);

		// Add onClick listener to "News Feed" button.
		View.OnClickListener newsListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// On click trigger feed activity.
				Intent i = createIntent(FeedActivity.class);
				i.putExtra("fi.harism.facebook.FeedActivity.path", "me/home");
				i.putExtra(
						"fi.harism.facebook.FeedActivity.title",
						getResources().getString(
								R.string.activity_feed_news_title));
				startActivity(i);
			}
		};
		updateButton(R.id.activity_main_button_news,
				R.drawable.pic_button_feed, R.string.activity_main_button_news,
				newsListener);

		// Add onClick listener to "Wall" button. Button wallButton =
		View.OnClickListener wallListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// On click trigger feed activity.
				Intent i = createIntent(FeedActivity.class);
				i.putExtra("fi.harism.facebook.FeedActivity.path", "me/feed");
				i.putExtra(
						"fi.harism.facebook.FeedActivity.title",
						getResources().getString(
								R.string.activity_feed_profile_title));
				startActivity(i);
			}
		};
		updateButton(R.id.activity_main_button_wall,
				R.drawable.pic_button_feed, R.string.activity_main_button_wall,
				wallListener);

		// Add onClick listener to "Profile" button. Button profileButton =
		View.OnClickListener profileListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = createIntent(UserActivity.class);
				i.putExtra("fi.harism.facebook.UserActivity.user", "me");
				startActivity(i);
			}
		};
		updateButton(R.id.activity_main_button_profile,
				R.drawable.pic_button_profile,
				R.string.activity_main_button_profile, profileListener);

		// Add onClick listener to "Chat" button. Button chatButton =
		View.OnClickListener chatListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = createIntent(ChatActivity.class);
				startActivity(i);
			}
		};
		updateButton(R.id.activity_main_button_chat,
				R.drawable.pic_button_chat, R.string.activity_main_button_chat,
				chatListener);

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

	/**
	 * Updates 'Button' view with given values.
	 */
	private void updateButton(int buttonId, int pictureRes, int textRes,
			View.OnClickListener observer) {
		View v = findViewById(buttonId);
		ImageView image = (ImageView) v
				.findViewById(R.id.view_main_button_image);
		image.setImageResource(pictureRes);
		TextView text = (TextView) v.findViewById(R.id.view_main_button_text);
		text.setText(textRes);
		v.setOnClickListener(observer);
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
		public void executeUI() {
			updateProfilePicture(fbBitmap);
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
			try {
				mFBUser.load(FBUser.Level.FULL);
			} catch (Exception ex) {
				// TODO: This is rather disastrous situation actually.
				showAlertDialog(ex.toString());
				throw ex;
			}
		}

		@Override
		public void executeUI() {
			updateProfileInfo(mFBUser);
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

}
