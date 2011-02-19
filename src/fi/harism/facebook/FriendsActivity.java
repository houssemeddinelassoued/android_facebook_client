package fi.harism.facebook;

import org.json.JSONArray;
import org.json.JSONObject;

import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.ImageRequest;
import fi.harism.facebook.request.RequestController;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FriendsActivity extends BaseActivity {

	private String userId;
	private RequestController requestController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends);

		userId = getIntent().getStringExtra("user_id");
		requestController = new RequestController();
		
		showProgressDialog();
		getFriendsList();
	}

	private void getFriendsList() {
		Bundle bundle = new Bundle();
		bundle.putString("fields", "id,name,picture");
		FacebookRequest facebookRequest = new FacebookRequest(this,
				requestController, userId + "/friends", bundle,
				new FacebookRequest.Observer() {

					@Override
					public void onError(Exception ex) {
						hideProgressDialog();
					}

					@Override
					public void onComplete(JSONObject response) {
						hideProgressDialog();
						try {
							processFriendsList(response.getJSONArray("data"));
						} catch (Exception ex) {
						}
					}
				});

		requestController.addRequest(facebookRequest);
	}

	private final void processFriendsList(JSONArray friendList) {
		for (int i = 0; i < friendList.length(); ++i) {
			try {
				JSONObject friend = friendList.getJSONObject(i);
				String imageUrl = friend.getString("picture");
				String id = friend.getString("id");
				String name = friend.getString("name");

				ImageRequest imageRequest = new ImageRequest(this,
						requestController, imageUrl,
						new ImageRequest.Observer() {

							@Override
							public void onError(Exception ex) {
							}

							@Override
							public void onComplete(ImageRequest imageRequest) {
								handleImageReceived(imageRequest);
							}
						});

				Bundle bundle = new Bundle();
				bundle.putString("id", id);
				bundle.putString("name", name);
				imageRequest.setBundle(bundle);

				requestController.addRequest(imageRequest);

			} catch (Exception ex) {
			}
		}
	}

	private final void handleImageReceived(ImageRequest imageRequest) {
		Bundle bundle = imageRequest.getBundle();
		View friendView = getLayoutInflater().inflate(R.layout.friends_item,
				null);
		TextView nameTextView = (TextView) friendView
				.findViewById(R.id.friends_item_name);
		nameTextView.setText(bundle.getString("name"));
		ImageView imageView = (ImageView) friendView
				.findViewById(R.id.friends_item_image);
		imageView.setImageBitmap(imageRequest.getBitmap());

		LinearLayout scrollView = (LinearLayout) findViewById(R.id.friends_list);
		scrollView.addView(friendView);
	}

}
