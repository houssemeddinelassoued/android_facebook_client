package fi.harism.facebook;

import fi.harism.facebook.dao.DAOFeedList;
import fi.harism.facebook.dao.DAOObserver;
import android.os.Bundle;
import android.widget.TextView;

public class NewsFeedActivity extends FeedActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView tv = (TextView) findViewById(R.id.feed_header);
		tv.setText(R.string.feed_news_text);
	}
	
	@Override
	public void getFeed(DAOObserver<DAOFeedList> observer) {
		getGlobalState().getRequestController().getNewsFeed(this, observer);
	}

}
