package fi.harism.facebook;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import fi.harism.facebook.dao.FBBitmap;
import fi.harism.facebook.dao.FBObserver;
import fi.harism.facebook.dao.FBUser;
import fi.harism.facebook.net.FBClient;
import fi.harism.facebook.request.Request;

/**
 * Main Activity of this application. Once Activity is launched it starts to
 * fetch default information from currently logged in user in Facebook API.
 * 
 * @author harism
 */
public class MainActivity extends BaseActivity {

	private FBUser fbUserMe;
	private FBBitmap fbBitmapMe;

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

		fbUserMe = getGlobalState().getFBFactory().getUser("me");

		// It's possible our application hasn't been killed.
		if (getGlobalState().getFBClient().isAuthorized()) {
			showMainView();
		} else {
			showLoginView();
		}
	}

	@Override
	public final Dialog onCreateDialog(int id) {
		return onCreateDialog(id, null);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// fbBitmapCache.cancel();
	}

	@Override
	public void onPause() {
		super.onPause();
		// fbBitmapCache.setPaused(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		// fbBitmapCache.setPaused(false);
	}

	public final void showLoginView() {
		setContentView(R.layout.activity_login);

		final Activity self = this;
		// Add onClickListener to 'login' button.
		View loginButton = findViewById(R.id.login_button_login);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LoginObserver loginObserver = new LoginObserver();
				getGlobalState().getFBClient().authorize(self, loginObserver);
			}
		});
	}

	public final void showMainView() {
		setContentView(R.layout.activity_main);
		final Activity self = this;

		// Set default picture as user picture.
		View imageContainer = findViewById(R.id.main_user_image);
		ImageView bottomImage = (ImageView) imageContainer
				.findViewById(R.id.view_layered_image_bottom);
		Bitmap picture = getGlobalState().getDefaultPicture();
		bottomImage.setImageBitmap(picture);

		// Add onClick listener to "Friends" button.
		Button friendsButton = (Button) findViewById(R.id.main_button_friends);
		friendsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// On click trigger friends activity.
				Intent i = createIntent(FriendsActivity.class);
				startActivity(i);
			}
		});

		// Add onClick listener to "News Feed" button.
		Button newsFeedButton = (Button) findViewById(R.id.main_button_news_feed);
		newsFeedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// On click trigger feed activity.
				Intent i = createIntent(NewsFeedActivity.class);
				startActivity(i);
			}
		});

		// Add onClick listener to "Wall" button.
		Button wallButton = (Button) findViewById(R.id.main_button_wall);
		wallButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// On click trigger feed activity.
				Intent i = createIntent(ProfileFeedActivity.class);
				startActivity(i);
			}
		});

		// Add onClick listener to "Profile" button.
		Button profileButton = (Button) findViewById(R.id.main_button_profile);
		profileButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAlertDialog("Implement me..");
			}
		});

		// Add onClick listener to "Chat" button.
		Button chatButton = (Button) findViewById(R.id.main_button_chat);
		chatButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// On click trigger feed activity.
				Intent i = createIntent(ChatActivity.class);
				startActivity(i);
			}
		});

		// Add onClick listener to "Logout" button.
		View logoutButton = findViewById(R.id.main_button_logout);
		logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LogoutObserver observer = new LogoutObserver();
				getGlobalState().getFBClient().logout(self, observer);
			}
		});

		// Update user information asynchronously.
		if (fbUserMe.getLevel() == FBUser.Level.FULL) {
			updateProfileInfo();
		} else {
			FBUserRequest meRequest = new FBUserRequest(this);
			getGlobalState().getRequestQueue().addRequest(meRequest);
		}
	}

	private void updateProfileInfo() {
		TextView nameView = (TextView) findViewById(R.id.main_user_name);
		nameView.setText(fbUserMe.getName());

		TextView statusView = (TextView) findViewById(R.id.main_user_status);
		if (fbUserMe.getStatus() == null || fbUserMe.getStatus().length() == 0) {
			statusView.setVisibility(View.GONE);
		} else {
			statusView.setText(fbUserMe.getStatus());
		}

		TextView loadingView = (TextView) findViewById(R.id.main_loading_text);

		Rect r = new Rect();
		if (nameView.getLocalVisibleRect(r)
				&& statusView.getLocalVisibleRect(r)) {
			AlphaAnimation inAnimation = new AlphaAnimation(0, 1);
			inAnimation.setDuration(700);
			nameView.startAnimation(inAnimation);
			statusView.startAnimation(inAnimation);

			AlphaAnimation outAnimation = new AlphaAnimation(1, 0);
			outAnimation.setDuration(700);
			outAnimation.setFillAfter(true);
			loadingView.startAnimation(outAnimation);
		} else {
			loadingView.setVisibility(View.GONE);
		}

		fbBitmapMe = getGlobalState().getFBFactory().getBitmap(
				fbUserMe.getPicture());
		if (fbBitmapMe.getBitmap() != null) {
			updateProfilePicture();
		} else {
			FBBitmapRequest request = new FBBitmapRequest(this);
			getGlobalState().getRequestQueue().addRequest(request);
		}
	}

	private void updateProfilePicture() {
		View imageContainer = findViewById(R.id.main_user_image);
		ImageView bottomImage = (ImageView) imageContainer
				.findViewById(R.id.view_layered_image_bottom);
		ImageView topImage = (ImageView) imageContainer
				.findViewById(R.id.view_layered_image_top);

		Rect r = new Rect();
		if (imageContainer.getLocalVisibleRect(r)) {
			AlphaAnimation inAnimation = new AlphaAnimation(0, 1);
			AlphaAnimation outAnimation = new AlphaAnimation(1, 0);
			inAnimation.setDuration(700);
			outAnimation.setDuration(700);
			outAnimation.setFillAfter(true);

			topImage.setAnimation(inAnimation);
			bottomImage.startAnimation(outAnimation);
		} else {
			bottomImage.setAlpha(0);
		}

		topImage.setImageBitmap(fbBitmapMe.getBitmap());
	}

	/**
	 * Class for handling profile picture request.
	 */
	private final class FBBitmapRequest extends Request {

		public FBBitmapRequest(Object key) {
			super(key);
		}

		@Override
		public void run() {
			try {
				fbBitmapMe.load();
				runOnUiThread(new UIRunnable());
			} catch (Exception ex) {
			}
		}

		@Override
		public void stop() {
		}

		private class UIRunnable implements Runnable {
			@Override
			public void run() {
				updateProfilePicture();
			}
		}
	}

	/**
	 * Class for handling "me" request.
	 */
	private final class FBUserRequest extends Request {

		public FBUserRequest(Object key) {
			super(key);
		}

		@Override
		public void run() {
			try {
				fbUserMe.load(FBUser.Level.FULL);
				runOnUiThread(new UIRunnable());
			} catch (Exception ex) {
				showAlertDialog(ex.toString());
			}
		}

		@Override
		public void stop() {
		}

		private class UIRunnable implements Runnable {
			@Override
			public void run() {
				updateProfileInfo();
			}
		}

	}

	/**
	 * LoginObserver observer for Facebook authentication procedure.
	 */
	private final class LoginObserver implements FBClient.LoginObserver {
		@Override
		public void onCancel() {
			// We are not interested in doing anything if user cancels Facebook
			// authorization dialog. Let them click 'login' again or close the
			// application.
		}

		@Override
		public void onComplete() {
			// On successful login switch to main view.
			showMainView();
		}

		@Override
		public void onError(Exception ex) {
			// If there was an error during authorization show an alert to user.
			showAlertDialog(ex.getLocalizedMessage());
		}
	}

	/**
	 * LogoutObserver for handling asynchronous logout procedure.
	 */
	private final class LogoutObserver implements FBClient.LogoutObserver {
		@Override
		public void onComplete() {
			// First hide progress dialog.
			hideProgressDialog();
			getGlobalState().getFBFactory().reset();
			// Switch to login view.
			showLoginView();
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
