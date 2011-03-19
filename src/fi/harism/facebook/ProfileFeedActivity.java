package fi.harism.facebook;

import fi.harism.facebook.dao.DAOFeedList;
import fi.harism.facebook.dao.DAOObserver;
import android.os.Bundle;
import android.widget.TextView;

public class ProfileFeedActivity extends FeedActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView tv = (TextView) findViewById(R.id.feed_title_text);
		tv.setText(R.string.feed_profile_text);
	}
	
	@Override
	public void getFeed(DAOObserver<DAOFeedList> observer) {
		getGlobalState().getRequestController().getProfileFeed(this, observer);
	}	
	
}
