package fi.harism.facebook;

import org.json.JSONObject;

import fi.harism.facebook.net.FacebookRequest;
import fi.harism.facebook.net.ImageRequest;
import fi.harism.facebook.net.RequestController;
import fi.harism.facebook.util.BitmapUtils;
import fi.harism.facebook.util.FacebookController;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private FacebookController facebookController = null;
	private RequestController requestController = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		facebookController = FacebookController.getFacebookController();
		requestController = RequestController.getRequestController();
		
		Bundle b = new Bundle();
		b.putString("fields", "id,name,picture");
		b.putString(FacebookController.TOKEN,
				facebookController.getAccessToken());
		FacebookRequest request = new FacebookRequest(this, "me", b,
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
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		facebookController = null;
		requestController = null;
	}

	private void meReceived(JSONObject response) {
		try {
			String name = response.getString("name");
			TextView tv = (TextView) findViewById(R.id.main_user_name);
			tv.setText(name);
		} catch (Exception ex) {
		}

		try {
			String picture = response.getString("picture");
			RequestController requestController = RequestController
					.getRequestController();
			ImageRequest request = new ImageRequest(this, picture,
					new ImageRequest.Observer() {
						@Override
						public void onError(Exception ex) {
						}

						@Override
						public void onComplete(Bitmap bitmap) {
							imageReceived(bitmap);
						}
					});
			requestController.addRequest(request);
		} catch (Exception ex) {
		}
	}

	private void imageReceived(Bitmap bitmap) {
		ImageView iv = (ImageView) findViewById(R.id.main_user_image);
		iv.setImageBitmap(BitmapUtils.roundBitmap(bitmap, 10));
	}

}
