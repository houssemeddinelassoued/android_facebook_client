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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.ImageRequest;
import fi.harism.facebook.request.RequestController;
import fi.harism.facebook.util.BitmapUtils;

/**
 * Friends list Activity. Once created it first loads "me/friends" from Facebook
 * Graph API. Once friend list is received it creates corresponding friend items
 * to view and triggers asynchronous loading of profile pictures.
 * 
 * This Activity implements also search functionality for friend list.
 * 
 * @author harism
 */
public class FriendsActivity extends BaseActivity {

	// RequestController instance.
	private RequestController requestController;
	// Default profile picture.
	private Bitmap defaultPicture = null;
	// Radius value for rounding profile images.
	private static final int PICTURE_ROUND_RADIUS = 7;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends);

		requestController = new RequestController(this);

		// Add text changed observer to search editor.
		SearchEditorObserver searchObserver = new SearchEditorObserver();
		EditText searchEditor = (EditText) findViewById(R.id.friends_edit_search);
		searchEditor.addTextChangedListener(searchObserver);

		// Create default picture shared among friend items.
		defaultPicture = getGlobalState().getDefaultPicture();
		defaultPicture = BitmapUtils.roundBitmap(defaultPicture,
				PICTURE_ROUND_RADIUS);

		// Show progress dialog.
		showProgressDialog();
		// Trigger asynchronous friend list loading.
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

	/**
	 * This method creates a new View instance for user item. It sets user name
	 * to given value and stores userId as a tag to view for later use.
	 * 
	 * @param userId
	 *            User Id.
	 * @param name
	 *            User name.
	 * @return New friend item view.
	 */
	private final View createFriendItem(String userId, String name) {
		// Create new friend item View.
		View friendItemView = getLayoutInflater().inflate(
				R.layout.friends_item, null);

		// Find name TextView and set its value.
		TextView nameTextView = (TextView) friendItemView
				.findViewById(R.id.friends_item_name);
		nameTextView.setText(name);

		// Find picture ImageView and set default profile picture into it.
		ImageView imageView = (ImageView) friendItemView
				.findViewById(R.id.friends_item_picture);
		imageView.setImageBitmap(defaultPicture);

		// Store user id as a tag to friend item View.
		friendItemView.setTag(R.id.view_user_id, userId);

		// This is rather useless at the moment, as we are calling this method
		// mostly once search text has not been changed. But just in case for
		// the future, toggle friend item View's visibility according to search
		// text.
		EditText searchEditText = (EditText) findViewById(R.id.friends_edit_search);
		String searchText = searchEditText.getText().toString();
		toggleFriendItemVisibility(friendItemView, searchText);

		// Add onClick listener.
		friendItemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Do something more creative with user id here. Also it's
				// rather pointless to create new observer for every friend item
				// View.
				showAlertDialog((String) v.getTag(R.id.view_user_id));
			}
		});

		return friendItemView;
	}

	/**
	 * Searches for given userId as a tag through user item Views. Returns one
	 * with given tag, if found, or null otherwise.
	 * 
	 * @param userId
	 *            User id to search friend item Views for.
	 * @return Friend item View with a userId tag. Or null if not found.
	 */
	private final View findFriendItem(String userId) {
		// By default we return null;
		View friendItemView = null;

		// Get LinearLayout containing all friend item Views.
		LinearLayout scrollView = (LinearLayout) findViewById(R.id.friends_list);

		// Iterate through friend items.
		for (int i = 0; i < scrollView.getChildCount(); ++i) {
			// Get friend item View at position i.
			View v = scrollView.getChildAt(i);
			// Get tag from View.
			String view_user_id = (String) v.getTag(R.id.view_user_id);
			if (userId.equals(view_user_id)) {
				// We have a match, break from the loop.
				friendItemView = v;
				break;
			}
		}

		return friendItemView;
	}

	/**
	 * Triggers a Facebook "me/friends" request.
	 */
	private final void getFriendsList() {
		// Facebook request parameters.
		Bundle parameters = new Bundle();
		// We are only interested in id and name.
		parameters.putString("fields", "id,name,picture");
		// Observer for receiving response asynchronously.
		FacebookRequest.Observer observer = new FacebookMeFriendsObserver();
		// Create actual request.
		FacebookRequest facebookRequest = requestController
				.createFacebookRequest("me/friends", parameters, observer);
		// Add request to processing queue.
		requestController.addRequest(facebookRequest);
	}

	/**
	 * Method for handling completed ImageRequests. First it gets user_id from
	 * request Bundle and then tries to locate corresponding friend item View.
	 * If one is found it sets picture of that friend item view to one received.
	 * 
	 * @param imageRequest
	 *            Successfully completed ImageRequest object.
	 */
	private final void onPictureReceived(ImageRequest imageRequest) {
		// Get stored user id from ImageRequest.
		Bundle bundle = imageRequest.getBundle();
		String userId = bundle.getString("id");

		// Search for corresponding friend item View.
		View friendView = findFriendItem(userId);

		// If we found one.
		if (friendView != null) {
			// Try to find picture ImageView.
			ImageView imageView = (ImageView) friendView
					.findViewById(R.id.friends_item_picture);
			// Bitmap we received.
			Bitmap picture = imageRequest.getBitmap();
			// Round its corners.
			picture = BitmapUtils.roundBitmap(picture, PICTURE_ROUND_RADIUS);
			// Update ImageView's bitmap with one received.
			imageView.setImageBitmap(picture);
		}
	}

	/**
	 * Method for processing friends array received from Facebook Graph API.
	 * First array is sorted, then corresponding friend items are added to
	 * scrollable view. This method triggers also asynchronous profile picture
	 * loading.
	 * 
	 * @param friendArray
	 *            JSONArray containing friends list.
	 */
	private final void processFriendsList(JSONArray friendArray) {
		// First create a Vector containing all friend JSONObjects.
		Vector<JSONObject> friendList = new Vector<JSONObject>();
		for (int i = 0; i < friendArray.length(); ++i) {
			try {
				JSONObject f = friendArray.getJSONObject(i);
				friendList.add(f);
			} catch (Exception ex) {
			}
		}

		// Comparator for sorting friend JSONObjects by name.
		Comparator<JSONObject> comparator = new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg0, JSONObject arg1) {
				String arg0Name = arg0.optString("name");
				String arg1Name = arg1.optString("name");
				return arg0Name.compareToIgnoreCase(arg1Name);
			}
		};

		// Sort friends Vector.
		Collections.sort(friendList, comparator);

		// Observer for receiving profile pictures.
		ImageRequest.Observer pictureObserver = new PictureObserver();
		// LinearLayout which is inside ScrollView.
		LinearLayout scrollView = (LinearLayout) findViewById(R.id.friends_list);

		for (int i = 0; i < friendList.size(); ++i) {
			try {
				JSONObject friend = friendList.elementAt(i);

				String userId = friend.getString("id");
				String name = friend.getString("name");
				String pictureUrl = friend.getString("picture");

				// Create default friend item view.
				View friendItemView = createFriendItem(userId, name);
				// Add friend item view to scrollable list.
				scrollView.addView(friendItemView);

				// Create profile picture request.
				ImageRequest imageRequest = requestController
						.createImageRequest(pictureUrl, pictureObserver);

				// Add user id to ImageRequest, we are using it to update
				// corresponding ImageView once image is received.
				Bundle bundle = new Bundle();
				bundle.putString("id", userId);
				imageRequest.setBundle(bundle);

				// Add profile picture request to queue.
				requestController.addRequest(imageRequest);
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * Toggles friend item View's visibility according to given search text.
	 * Method tries to find given search text within the name TextView found
	 * inside given friendItem.
	 * 
	 * @param friendItem
	 *            Friend item View.
	 * @param searchText
	 *            Current search text.
	 */
	private final void toggleFriendItemVisibility(View friendItem,
			String searchText) {
		// We are not case sensitive.
		searchText = searchText.toLowerCase();

		// Locate name TextView.
		TextView nameTextView = (TextView) friendItem
				.findViewById(R.id.friends_item_name);
		// Get name from TextView.
		String friendName = nameTextView.getText().toString();
		// We are still not case sensitive.
		friendName = friendName.toLowerCase();

		// Toggle friend item visibility regarding to if searchText is found
		// within name. This is rather naive approach but works good enough :)
		if (friendName.contains(searchText)) {
			friendItem.setVisibility(View.VISIBLE);
		} else {
			friendItem.setVisibility(View.GONE);
		}
	}

	/**
	 * Observer for handling "me/friends" request.
	 */
	private final class FacebookMeFriendsObserver implements
			FacebookRequest.Observer {
		@Override
		public void onComplete(FacebookRequest facebookRequest) {
			// First hide progress dialog.
			hideProgressDialog();
			try {
				// Friend array is found under name "data"
				JSONArray friendArray = facebookRequest.getJSONObject()
						.getJSONArray("data");
				// Handle friendArray processing to appropriate method.
				processFriendsList(friendArray);
			} catch (Exception ex) {
			}
		}

		@Override
		public void onError(Exception ex) {
			// On error only hide progress dialog.
			hideProgressDialog();
		}
	}

	/**
	 * Observer for handling profile picture loading.
	 */
	private final class PictureObserver implements ImageRequest.Observer {
		@Override
		public void onComplete(ImageRequest imageRequest) {
			// Handle imageRequest to appropriate method.
			onPictureReceived(imageRequest);
		}

		@Override
		public void onError(Exception ex) {
			// We don't care about errors.
		}
	}

	/**
	 * Observer for handling search text changes.
	 */
	private final class SearchEditorObserver implements TextWatcher {
		@Override
		public void afterTextChanged(Editable editable) {
			// Get editor text.
			String searchText = editable.toString();
			// Find LinearLayout containing our friend items.
			LinearLayout friendList = (LinearLayout) findViewById(R.id.friends_list);
			// Iterate through all child Views.
			for (int i = 0; i < friendList.getChildCount(); ++i) {
				LinearLayout friendItem = (LinearLayout) friendList
						.getChildAt(i);
				toggleFriendItemVisibility(friendItem, searchText);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// We are not interested in this callback.
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// We are not interested in this callback.
		}
	}

}
