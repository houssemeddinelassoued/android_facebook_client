package fi.harism.facebook;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import fi.harism.facebook.dao.DAOObserver;
import fi.harism.facebook.dao.DAOProfile;
import fi.harism.facebook.dialog.ProfileDialog;
import fi.harism.facebook.net.FacebookLoginObserver;
import fi.harism.facebook.net.FacebookLogoutObserver;
import fi.harism.facebook.net.RequestController;
import fi.harism.facebook.util.BitmapUtils;

/**
 * Main Activity of this application. Once Activity is launched it starts to
 * fetch default information from currently logged in user in Facebook API.
 * 
 * @author harism
 */
public class MainActivity extends BaseActivity {

	// Profile picture corner rounding radius.
	private static final int PICTURE_ROUND_RADIUS = 0;
	// Global instance of RequestController.
	private RequestController requestController = null;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		requestController.loginCallback(requestCode, resultCode, data);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

		requestController = getGlobalState().getRequestController();
		
		// It's possible our application hasn't been killed.
		if (requestController.isAuthorized()) {
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
		requestController.removeRequests(this);
		requestController = null;
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
	
	public final void showLoginView() {
		setContentView(R.layout.login);

		final Activity self = this;
		// Add onClickListener to 'login' button.
		View loginButton = findViewById(R.id.login_button_login);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LoginObserver loginObserver = new LoginObserver();
				requestController.login(self, loginObserver);
			}
		});
	}

	public final void showMainView() {
		setContentView(R.layout.main);
		final Activity self = this;

		// Set default picture as user picture.
		ImageView pictureView = (ImageView) findViewById(R.id.main_user_image);
		Bitmap picture = getGlobalState().getDefaultPicture();
		picture = BitmapUtils.roundBitmap(picture, PICTURE_ROUND_RADIUS);
		pictureView.setImageBitmap(picture);

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
				showProgressDialog();
				requestController.getProfile(self, "me",
						new DAOObserver<DAOProfile>() {
							@Override
							public void onComplete(DAOProfile response) {
								hideProgressDialog();
								new ProfileDialog(self, response).show();
							}

							@Override
							public void onError(Exception error) {
								hideProgressDialog();
							}
						});
			}
		});

		// Add onClick listener to "Logout" button.
		View logoutButton = findViewById(R.id.main_button_logout);
		logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LogoutObserver observer = new LogoutObserver();
				requestController.logout(self, observer);
			}
		});

		// Start loading user information asynchronously.
		requestController.getProfile(this, "me", new DAOProfileObserver(this));
	}

	/**
	 * Private ImageRequest observer for handling profile picture loading.
	 */
	private final class BitmapObserver implements DAOObserver<Bitmap> {
		@Override
		public void onComplete(Bitmap bitmap) {
			ImageView iv = (ImageView) findViewById(R.id.main_user_image);
			iv.setImageBitmap(BitmapUtils.roundBitmap(bitmap,
					PICTURE_ROUND_RADIUS));
		}

		@Override
		public void onError(Exception ex) {
			// We don't care about errors here.
		}

	}

	/**
	 * Private FacebookRequest observer for handling "me" request.
	 */
	private final class DAOProfileObserver implements DAOObserver<DAOProfile> {

		private Activity activity = null;

		public DAOProfileObserver(Activity activity) {
			this.activity = activity;
		}

		@Override
		public void onComplete(DAOProfile profile) {
			TextView nameView = (TextView) findViewById(R.id.main_user_name);
			nameView.setText(profile.getName());
			
			TextView statusView = (TextView) findViewById(R.id.main_user_status);
			statusView.setText(profile.getStatus());			

			requestController.getBitmap(activity, profile.getPictureUrl(),
					new BitmapObserver());
		}

		@Override
		public void onError(Exception ex) {
			// We don't care about errors here.
		}
	}

	/**
	 * LoginObserver observer for Facebook authentication procedure.
	 */
	private final class LoginObserver implements FacebookLoginObserver {
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
	private final class LogoutObserver implements FacebookLogoutObserver {
		@Override
		public void onComplete() {
			// First hide progress dialog.
			hideProgressDialog();
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
