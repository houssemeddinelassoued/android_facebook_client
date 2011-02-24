package fi.harism.facebook;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.ImageRequest;
import fi.harism.facebook.request.RequestController;
import fi.harism.facebook.util.BitmapUtils;
import fi.harism.facebook.util.FacebookController;

/**
 * Main Activity of this application. Once Activity is launched it starts to
 * fetch default information from currently logged in user in Facebook API.
 * 
 * @author harism
 */
public class MainActivity extends BaseActivity {

	// Global instance of FacebookController
	private FacebookController facebookController = null;
	// RequestController instance for handling asynchronous requests.
	private RequestController requestController = null;

	// Profile picture corner rounding radius.
	private static final int PICTURE_ROUND_RADIUS = 7;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Get global instance of FacebookController.
		facebookController = getGlobalState().getFacebookController();
		// Create RequestController for this Activity.
		requestController = new RequestController(this);

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

		// Start loading user information asynchronously.
		loadProfileInfo();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		facebookController = null;
		requestController.destroy();
		requestController = null;
	}

	@Override
	public void onPause() {
		super.onPause();
		requestController.pause();
	}

	@Override
	public void onResume() {
		super.onResume();
		requestController.resume();
	}

	/**
	 * Starts to load currently logged in user information.
	 */
	private final void loadProfileInfo() {
		// We need to give some request parameters for FacebookRequest.
		Bundle meParameters = new Bundle();
		// Select only name and picture fields.
		meParameters.putString("fields", "name,picture");
		// Retrieving picture requires access token to be given.
		meParameters.putString(FacebookController.TOKEN,
				facebookController.getAccessToken());
		// Create observer for FacebookRequest.
		FacebookRequest.Observer meObserver = new FacebookMeObserver();
		// Create actual request, here we ask for http://graph.facebook.com/me.
		FacebookRequest meRequest = requestController.createFacebookRequest(
				"me", meParameters, meObserver);
		requestController.addRequest(meRequest);

		// Start loading latest status message for current user.
		Bundle meStatusesParameters = new Bundle();
		// We want only latest status.
		meStatusesParameters.putString("limit", "1");
		// We need only message value.
		meStatusesParameters.putString("fields", "message");
		// Create observer for FacebookRequest.
		FacebookRequest.Observer meStatusesObserver = new FacebookMeStatusesObserver();
		// Create actual request.
		FacebookRequest meStatusesRequest = requestController
				.createFacebookRequest("me/statuses", meStatusesParameters,
						meStatusesObserver);
		requestController.addRequest(meStatusesRequest);
	}

	/**
	 * Starts loading profile picture from given URL. Once picture is loaded it
	 * is being set to this Activity's profile picture view.
	 * 
	 * @param pictureUrl
	 *            URL for profile picture.
	 */
	private final void loadProfilePicture(String pictureUrl) {
		ImageRequest.Observer observer = new PictureObserver();
		ImageRequest request = requestController.createImageRequest(pictureUrl,
				observer);
		requestController.addRequest(request);
	}

	/**
	 * Private FacebookRequest observer for handling "me" request.
	 */
	private final class FacebookMeObserver implements FacebookRequest.Observer {
		@Override
		public void onComplete(FacebookRequest facebookRequest) {
			JSONObject o = facebookRequest.getJSONObject();

			// If response contains field "name" set it as name to view.
			String name = o.optString("name");
			if (name.length() > 0) {
				TextView tv = (TextView) findViewById(R.id.main_user_name);
				tv.setText(name);
			}

			// If response contains field "picture" trigger asynchronous loading
			// of profile picture.
			String pictureUrl = o.optString("picture");
			if (pictureUrl.length() > 0) {
				loadProfilePicture(pictureUrl);
			}
		}

		@Override
		public void onError(Exception ex) {
		}
	}

	/**
	 * Private FacebookRequest observer for handling "me/statuses" request.
	 */
	private final class FacebookMeStatusesObserver implements
			FacebookRequest.Observer {
		@Override
		public void onComplete(FacebookRequest facebookRequest) {
			JSONObject o = facebookRequest.getJSONObject();
			try {
				// Response should contain "data" field which should be an array
				// containing 1 JSONObject.
				String message = o.getJSONArray("data").getJSONObject(0)
						.getString("message");
				TextView tv = (TextView) findViewById(R.id.main_user_status);
				tv.setText(message);
			} catch (Exception ex) {
			}
		}

		@Override
		public void onError(Exception ex) {
		}
	}

	/**
	 * Private ImageRequest observer for handling profile picture loading.
	 */
	private final class PictureObserver implements ImageRequest.Observer {
		@Override
		public void onComplete(ImageRequest imageRequest) {
			ImageView iv = (ImageView) findViewById(R.id.main_user_image);
			Bitmap picture = imageRequest.getBitmap();
			iv.setImageBitmap(BitmapUtils.roundBitmap(picture,
					PICTURE_ROUND_RADIUS));
		}

		@Override
		public void onError(Exception ex) {
		}

	}

}
