package fi.harism.facebook;

import org.json.JSONObject;

import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.ImageRequest;
import fi.harism.facebook.request.RequestController;
import fi.harism.facebook.util.BitmapUtils;
import fi.harism.facebook.util.FacebookController;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends BaseActivity {

	private FacebookController facebookController = null;
	private RequestController requestController = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		facebookController = FacebookController.getFacebookController();
		requestController = new RequestController();
		
		Button friendsButton = (Button)findViewById(R.id.main_button_friends);
		friendsButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				showFriendsList();
			}
		});
		
		fetchNameAndImage();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		facebookController = null;
		requestController.clear();
		requestController = null;
	}
	
	private final void fetchNameAndImage() {
		Bundle b = new Bundle();
		b.putString("fields", "name,picture");
		b.putString(FacebookController.TOKEN,
				facebookController.getAccessToken());
		FacebookRequest request = new FacebookRequest(this, requestController, "me", b,
				new FacebookRequest.Observer() {
					@Override
					public void onError(Exception ex) {
					}

					@Override
					public void onComplete(JSONObject response) {
						meReceived(response);
					}
				});
		requestController.addRequest(request);		
	}

	private final void meReceived(JSONObject response) {
		try {
			String name = response.getString("name");
			TextView tv = (TextView) findViewById(R.id.main_user_name);
			tv.setText(name);
		} catch (Exception ex) {
		}

		try {
			String picture = response.getString("picture");
			ImageRequest request = new ImageRequest(this, requestController, picture,
					new ImageRequest.Observer() {
						@Override
						public void onError(Exception ex) {
						}

						@Override
						public void onComplete(ImageRequest imageRequest) {
							imageReceived(imageRequest.getBitmap());
						}
					});
			requestController.addRequest(request);
		} catch (Exception ex) {
		}
	}

	private final void imageReceived(Bitmap bitmap) {
		ImageView iv = (ImageView) findViewById(R.id.main_user_image);
		iv.setImageBitmap(BitmapUtils.roundBitmap(bitmap, 10));
	}
	
	private final void showFriendsList() {
		Intent i = new Intent(this, FriendsActivity.class);
		i.putExtra("user_id", "me");
		startActivity(i);
	}

}
