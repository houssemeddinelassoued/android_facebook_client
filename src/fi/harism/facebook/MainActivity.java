package fi.harism.facebook;

import org.json.JSONObject;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import fi.harism.facebook.data.Controller;
import fi.harism.facebook.data.FacebookBitmap;
import fi.harism.facebook.data.FacebookClient;
import fi.harism.facebook.data.FacebookNameAndPicture;
import fi.harism.facebook.data.FacebookStatus;
import fi.harism.facebook.dialog.ProfileDialog;
import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.ImageRequest;
import fi.harism.facebook.request.RequestQueue;
import fi.harism.facebook.util.BitmapUtils;

/**
 * Main Activity of this application. Once Activity is launched it starts to
 * fetch default information from currently logged in user in Facebook API.
 * 
 * @author harism
 */
public class MainActivity extends BaseActivity {

	// Global instance of FacebookController
	//private FacebookController facebookController = null;
	// RequestController instance for handling asynchronous requests.
	//private RequestController requestController = null;
	
	private Controller controller = null;

	private static final int ID_DIALOG_PROFILE = 1;

	// Profile picture corner rounding radius.
	private static final int PICTURE_ROUND_RADIUS = 7;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		controller = getGlobalState().getController();

		// Get global instance of FacebookController.
		//facebookController = getGlobalState().getFacebookController();
		// Create RequestController for this Activity.
		//requestController = new RequestController(this);

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

		// Add onClick listener to "Feed" button.
		Button feedButton = (Button) findViewById(R.id.main_button_feed);
		feedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// On click trigger feed activity.
				Intent i = createIntent(FeedActivity.class);
				startActivity(i);
			}
		});

		// Add onClick listener to "Profile" button.
		Button profileButton = (Button) findViewById(R.id.main_button_profile);
		profileButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// On click trigger feed activity.
				showDialog(ID_DIALOG_PROFILE);
			}
		});

		// Start loading user information asynchronously.
		loadProfileInfo();
	}

	@Override
	public final Dialog onCreateDialog(int id) {
		return onCreateDialog(id, null);
	}

	public final Dialog onCreateDialog(int id, Bundle bundle) {
		switch (id) {
		case ID_DIALOG_PROFILE:
			//ProfileDialog profileDialog = new ProfileDialog(this,
			//		requestController, "me");
			//return profileDialog;
		}
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		controller.removeRequests(this);
		controller = null;
	}

	@Override
	public void onPause() {
		super.onPause();
		controller.setPaused(this, true);
	}

	@Override
	public void onResume() {
		super.onResume();
		controller.setPaused(this, false);
	}

	/**
	 * Starts to load currently logged in user information.
	 */
	private final void loadProfileInfo() {
		controller.getNameAndPicture(this, "me", new FacebookMeObserver());
		controller.getStatus(this, "me", new FacebookStatusObserver());
	}

	/**
	 * Starts loading profile picture from given URL. Once picture is loaded it
	 * is being set to this Activity's profile picture view.
	 * 
	 * @param pictureUrl
	 *            URL for profile picture.
	 */
	private final void loadProfilePicture(String pictureUrl) {
		PictureObserver observer = new PictureObserver();
		controller.getBitmap(this, null, pictureUrl, observer);
		
		//ImageRequest request = requestController.createImageRequest(pictureUrl,
		//		observer);
		//request.setCacheBitmap(true);
		//requestController.addRequest(request);
	}

	/**
	 * Private FacebookRequest observer for handling "me" request.
	 */
	private final class FacebookMeObserver implements Controller.RequestObserver<FacebookNameAndPicture> {
		@Override
		public void onComplete(FacebookNameAndPicture resp) {
			TextView tv = (TextView) findViewById(R.id.main_user_name);
			tv.setText(resp.getName());
			loadProfilePicture(resp.getPicture());
		}

		@Override
		public void onError(Exception ex) {
			// We don't care about errors here.
		}
	}

	/**
	 * Private FacebookRequest observer for handling "me/statuses" request.
	 */
	private final class FacebookStatusObserver implements
			Controller.RequestObserver<FacebookStatus> {
		@Override
		public void onComplete(FacebookStatus response) {
			TextView tv = (TextView) findViewById(R.id.main_user_status);
			tv.setText(response.getMessage());
		}

		@Override
		public void onError(Exception ex) {
			// We don't care about errors here.
		}
	}

	/**
	 * Private ImageRequest observer for handling profile picture loading.
	 */
	private final class PictureObserver implements Controller.RequestObserver<FacebookBitmap> {
		@Override
		public void onComplete(FacebookBitmap bitmap) {
			ImageView iv = (ImageView) findViewById(R.id.main_user_image);
			Bitmap picture = bitmap.getBitmap();
			iv.setImageBitmap(BitmapUtils.roundBitmap(picture,
					PICTURE_ROUND_RADIUS));
		}

		@Override
		public void onError(Exception ex) {
			// We don't care about errors here.
		}

	}

}
