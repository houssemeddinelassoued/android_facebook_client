package fi.harism.facebook;

import fi.harism.facebook.dao.FBFeedList;
import android.os.Bundle;
import android.widget.TextView;

public class NewsFeedActivity extends FeedActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView tv = (TextView) findViewById(R.id.header);
		tv.setText(R.string.feed_news_text);
	}
	
	@Override
	public FBFeedList getFeedList() {
		return getGlobalState().getFBFactory().getNewsFeed();
	}

}
