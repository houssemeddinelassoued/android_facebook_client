package fi.harism.facebook;

import org.json.JSONObject;

import android.content.Intent;
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

public class MainActivity extends BaseActivity {

	private FacebookController facebookController = null;
	private RequestController requestController = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		facebookController = getGlobalState().getFacebookController();
		requestController = new RequestController(this);

		Button friendsButton = (Button) findViewById(R.id.main_button_friends);
		friendsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = createIntent(FriendsActivity.class);
				startActivity(i);
			}
		});

		Button feedButton = (Button) findViewById(R.id.main_button_feed);
		feedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = createIntent(FeedActivity.class);
				startActivity(i);
			}
		});

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

	private final void loadProfileInfo() {

		Bundle meParameters = new Bundle();
		meParameters.putString("fields", "name,picture");
		meParameters.putString(FacebookController.TOKEN,
				facebookController.getAccessToken());
		FacebookRequest.Observer meObserver = new FacebookMeObserver();
		FacebookRequest meRequest = requestController.createFacebookRequest(
				"me", meParameters, meObserver);
		requestController.addRequest(meRequest);

		Bundle meStatusesParameters = new Bundle();
		meStatusesParameters.putString("limit", "1");
		meStatusesParameters.putString("fields", "message");
		FacebookRequest.Observer meStatusesObserver = new FacebookMeStatusesObserver();
		FacebookRequest meStatusesRequest = requestController
				.createFacebookRequest("me/statuses", meStatusesParameters,
						meStatusesObserver);
		requestController.addRequest(meStatusesRequest);
	}

	private final void loadProfilePicture(String pictureUrl) {
		ImageRequest.Observer observer = new PictureObserver();
		ImageRequest request = requestController.createImageRequest(pictureUrl,
				observer);
		requestController.addRequest(request);
	}

	private final class FacebookMeObserver implements FacebookRequest.Observer {
		@Override
		public void onComplete(FacebookRequest facebookRequest) {
			JSONObject o = facebookRequest.getJSONObject();

			String name = o.optString("name");
			if (name.length() > 0) {
				TextView tv = (TextView) findViewById(R.id.main_user_name);
				tv.setText(name);
			}

			String pictureUrl = o.optString("picture");
			if (pictureUrl.length() > 0) {
				loadProfilePicture(pictureUrl);
			}
		}

		@Override
		public void onError(Exception ex) {
		}
	}

	private final class FacebookMeStatusesObserver implements
			FacebookRequest.Observer {
		@Override
		public void onComplete(FacebookRequest facebookRequest) {
			JSONObject o = facebookRequest.getJSONObject();
			try {
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

	private final class PictureObserver implements ImageRequest.Observer {
		@Override
		public void onComplete(ImageRequest imageRequest) {
			ImageView iv = (ImageView) findViewById(R.id.main_user_image);
			iv.setImageBitmap(BitmapUtils.roundBitmap(imageRequest.getBitmap(),
					10));
		}

		@Override
		public void onError(Exception ex) {
		}

	}

}
