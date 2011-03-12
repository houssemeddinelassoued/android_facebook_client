package fi.harism.facebook;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import fi.harism.facebook.dao.DAONameAndPicture;
import fi.harism.facebook.dao.DAOStatus;
import fi.harism.facebook.net.NetController;
import fi.harism.facebook.util.BitmapUtils;

/**
 * Main Activity of this application. Once Activity is launched it starts to
 * fetch default information from currently logged in user in Facebook API.
 * 
 * @author harism
 */
public class MainActivity extends BaseActivity {

	// Global instance of NetController.
	private NetController netController = null;

	private static final int ID_DIALOG_PROFILE = 1;

	// Profile picture corner rounding radius.
	private static final int PICTURE_ROUND_RADIUS = 7;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		netController = getGlobalState().getNetController();

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
		netController.getNameAndPicture(this, "me", new FacebookMeObserver(this));
		netController.getLatestStatus(this, "me", new FacebookStatusObserver());
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
		netController.removeRequests(this);
		netController = null;
	}

	@Override
	public void onPause() {
		super.onPause();
		netController.setPaused(this, true);
	}

	@Override
	public void onResume() {
		super.onResume();
		netController.setPaused(this, false);
	}

	/**
	 * Private FacebookRequest observer for handling "me" request.
	 */
	private final class FacebookMeObserver implements NetController.RequestObserver<DAONameAndPicture> {
		
		private Activity activity = null;
		
		public FacebookMeObserver(Activity activity) {
			this.activity = activity;
		}
		
		@Override
		public void onComplete(DAONameAndPicture resp) {
			TextView tv = (TextView) findViewById(R.id.main_user_name);
			tv.setText(resp.getName());
			
			netController.getBitmap(activity, resp.getPicture(), new PictureObserver());
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
			NetController.RequestObserver<DAOStatus> {
		@Override
		public void onComplete(DAOStatus response) {
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
	private final class PictureObserver implements NetController.RequestObserver<Bitmap> {
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

}
