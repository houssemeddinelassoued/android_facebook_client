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
import fi.harism.facebook.dao.FBBitmapCache;
import fi.harism.facebook.dao.FBObserver;
import fi.harism.facebook.dao.FBUser;
import fi.harism.facebook.dao.FBUserCache;
import fi.harism.facebook.net.FBClient;

/**
 * Main Activity of this application. Once Activity is launched it starts to
 * fetch default information from currently logged in user in Facebook API.
 * 
 * @author harism
 */
public class MainActivity extends BaseActivity {

	private FBUserCache fbUserMap;
	private FBBitmapCache fbBitmapCache;

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

		fbBitmapCache = getGlobalState().getFBFactory().getBitmapCache();
		fbUserMap = getGlobalState().getFBFactory().getUserCache();

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
		fbUserMap.cancel();
		fbBitmapCache.cancel();
	}

	@Override
	public void onPause() {
		super.onPause();
		fbUserMap.pause();
		fbBitmapCache.setPaused(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		fbUserMap.resume();
		fbBitmapCache.setPaused(false);
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

		// Start loading user information asynchronously.
		fbUserMap.getUser("me", new FBMeObserver());
	}

	/**
	 * Private ImageRequest observer for handling profile picture loading.
	 */
	private final class BitmapObserver implements FBObserver<FBBitmap>,
			Runnable {

		private FBBitmap bitmap;

		@Override
		public void onComplete(final FBBitmap bitmap) {
			this.bitmap = bitmap;
			runOnUiThread(this);
		}

		@Override
		public void onError(Exception ex) {
			// We don't care about errors here.
		}

		@Override
		public void run() {
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

			topImage.setImageBitmap(bitmap.getBitmap());
		}

	}

	/**
	 * Private FacebookRequest observer for handling "me" request.
	 */
	private final class FBMeObserver implements FBObserver<FBUser>, Runnable {

		private FBUser me;

		@Override
		public void onComplete(final FBUser me) {
			this.me = me;
			runOnUiThread(this);
		}

		@Override
		public void onError(Exception ex) {
			// We don't care about errors here.
		}

		@Override
		public void run() {
			TextView nameView = (TextView) findViewById(R.id.main_user_name);
			nameView.setText(me.getName());

			TextView statusView = (TextView) findViewById(R.id.main_user_status);
			if (me.getStatus() == null || me.getStatus().length() == 0) {
				statusView.setVisibility(View.GONE);
			} else {
				statusView.setText(me.getStatus());
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

			fbBitmapCache.load(me.getPicture(), null, new BitmapObserver());
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
