package fi.harism.facebook;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import fi.harism.facebook.request.FacebookRequest;
import fi.harism.facebook.request.RequestController;

public class FeedActivity extends BaseActivity {

	private RequestController requestController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed);

		requestController = new RequestController(this);

		FacebookRequest.Observer observer = new FacebookFeedObserver();
		FacebookRequest request = requestController.createFacebookRequest(
				"me/home", observer);
		requestController.addRequest(request);

		showProgressDialog();
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

	private void createFeedItem(JSONObject feedItemObject) {
		String type = feedItemObject.optString("type");
		if (type.equals("status")) {
			String name = "";
			JSONObject fromObject = feedItemObject.optJSONObject("from");
			if (fromObject != null) {
				name = fromObject.optString("name");
			}
			String message = feedItemObject.optString("message");
			String created = feedItemObject.optString("created_time");

			View feedItemView = getLayoutInflater().inflate(R.layout.feed_item,
					null);

			TextView nameView = (TextView) feedItemView
					.findViewById(R.id.feed_item_name);
			nameView.setText(name);
			TextView messageView = (TextView) feedItemView
					.findViewById(R.id.feed_item_message);
			messageView.setText(message);
			TextView createdView = (TextView) feedItemView
					.findViewById(R.id.feed_item_created);
			createdView.setText(created);

			LinearLayout itemList = (LinearLayout) findViewById(R.id.feed_list);
			itemList.addView(feedItemView);
		}
	}

	private final class FacebookFeedObserver implements
			FacebookRequest.Observer {

		@Override
		public void onComplete(FacebookRequest facebookRequest) {
			hideProgressDialog();
			JSONObject feedObject = facebookRequest.getJSONObject();
			JSONArray dataArray = feedObject.optJSONArray("data");
			if (dataArray != null) {
				for (int i = 0; i < dataArray.length(); ++i) {
					JSONObject feedItemObject = dataArray.optJSONObject(i);
					if (feedItemObject != null) {
						createFeedItem(feedItemObject);
					}
				}
			}

			int i = 0;
			++i;
		}

		@Override
		public void onError(Exception ex) {
			hideProgressDialog();
			int i = 0;
			i++;
		}

	}

}
