package fi.harism.facebook;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.ImageRequest;
import fi.harism.facebook.request.RequestController;
import fi.harism.facebook.util.BitmapUtils;

public class FriendsActivity extends BaseActivity {

	private RequestController requestController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends);

		requestController = new RequestController(this);

		SearchEditorObserver searchObserver = new SearchEditorObserver();
		EditText searchEditor = (EditText) findViewById(R.id.friends_edit_search);
		searchEditor.addTextChangedListener(searchObserver);

		showProgressDialog();
		getFriendsList();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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

	private void getFriendsList() {
		Bundle parameters = new Bundle();
		parameters.putString("fields", "id,name,picture");
		FacebookRequest.Observer observer = new FacebookMeFriendsObserver();
		FacebookRequest facebookRequest = requestController
				.createFacebookRequest("me/friends", parameters, observer);
		requestController.addRequest(facebookRequest);
	}

	private final void onImageReceived(ImageRequest imageRequest) {
		Bundle bundle = imageRequest.getBundle();
		View friendView = getLayoutInflater().inflate(R.layout.friends_item,
				null);

		TextView nameTextView = (TextView) friendView
				.findViewById(R.id.friends_item_name);
		nameTextView.setText(bundle.getString("name"));

		ImageView imageView = (ImageView) friendView
				.findViewById(R.id.friends_item_image);
		Bitmap scaled = BitmapUtils.scaleToHeight(imageRequest.getBitmap(), 30);
		imageView.setImageBitmap(BitmapUtils.roundBitmap(scaled, 3));

		EditText searchEditText = (EditText) findViewById(R.id.friends_edit_search);
		String searchText = searchEditText.getText().toString();
		toggleFriendItemVisibility(friendView, searchText);

		LinearLayout scrollView = (LinearLayout) findViewById(R.id.friends_list);
		scrollView.addView(friendView);

		friendView.setTag(R.id.view_user_id, bundle.getString("id"));
		friendView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showAlertDialog((String) v.getTag(R.id.view_user_id));
			}
		});
	}

	private final void processFriendsList(JSONArray friendArray) {
		Vector<JSONObject> friendList = new Vector<JSONObject>();
		for (int i = 0; i < friendArray.length(); ++i) {
			try {
				JSONObject f = friendArray.getJSONObject(i);
				friendList.add(f);
			} catch (Exception ex) {
			}
		}

		Comparator<JSONObject> comparator = new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg0, JSONObject arg1) {
				String arg0Name = arg0.optString("name");
				String arg1Name = arg1.optString("name");
				return arg0Name.compareToIgnoreCase(arg1Name);
			}
		};

		Collections.sort(friendList, comparator);

		for (int i = 0; i < friendList.size(); ++i) {
			try {
				JSONObject friend = friendList.elementAt(i);
				String id = friend.getString("id");
				String picture = friend.getString("picture");
				String name = friend.getString("name");

				ImageRequest.Observer pictureObserver = new PictureObserver();
				ImageRequest imageRequest = requestController
						.createImageRequest(picture, pictureObserver);

				Bundle bundle = new Bundle();
				bundle.putString("id", id);
				bundle.putString("name", name);
				imageRequest.setBundle(bundle);

				requestController.addRequest(imageRequest);
			} catch (Exception ex) {
			}
		}
	}

	private final void toggleFriendItemVisibility(View friendItem,
			String searchText) {
		searchText = searchText.toLowerCase();
		TextView friendTextView = (TextView) friendItem
				.findViewById(R.id.friends_item_name);
		String friendName = friendTextView.getText().toString();
		friendName = friendName.toLowerCase();

		if (friendName.contains(searchText)) {
			friendItem.setVisibility(View.VISIBLE);
		} else {
			friendItem.setVisibility(View.GONE);
		}
	}

	private final class FacebookMeFriendsObserver implements
			FacebookRequest.Observer {
		@Override
		public void onComplete(FacebookRequest facebookRequest) {
			hideProgressDialog();
			try {
				processFriendsList(facebookRequest.getJSONObject()
						.getJSONArray("data"));
			} catch (Exception ex) {
			}
		}

		@Override
		public void onError(Exception ex) {
			hideProgressDialog();
		}
	}

	private final class PictureObserver implements ImageRequest.Observer {
		@Override
		public void onComplete(ImageRequest imageRequest) {
			onImageReceived(imageRequest);
		}

		@Override
		public void onError(Exception ex) {
		}
	}

	private final class SearchEditorObserver implements TextWatcher {
		@Override
		public void afterTextChanged(Editable editable) {
			String searchText = editable.toString();
			ViewGroup friendList = (LinearLayout) findViewById(R.id.friends_list);
			for (int i = 0; i < friendList.getChildCount(); ++i) {
				LinearLayout friendItem = (LinearLayout) friendList
						.getChildAt(i);
				toggleFriendItemVisibility(friendItem, searchText);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}
	}

}
