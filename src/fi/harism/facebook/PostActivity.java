package fi.harism.facebook;

import java.util.Vector;

import fi.harism.facebook.dao.FBComment;
import fi.harism.facebook.dao.FBFeed;
import fi.harism.facebook.dao.FBPost;
import fi.harism.facebook.request.RequestUI;
import fi.harism.facebook.util.StringUtils;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PostActivity extends BaseActivity {

	public static final String INTENT_FEED_PATH = "fi.harism.facebook.PostActivity.feedPath";
	public static final String INTENT_POST_ID = "fi.harism.facebook.PostActivity.postId";

	private FBPost mFBPost = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_post);

		String feedPath = getIntent().getStringExtra(INTENT_FEED_PATH);
		String postId = getIntent().getStringExtra(INTENT_POST_ID);

		FBFeed fbFeed = getGlobalState().getFBFactory().getFeed(feedPath);
		Vector<FBPost> posts = fbFeed.getPosts();
		for (FBPost post : posts) {
			if (post.getId().equals(postId)) {
				mFBPost = post;
				break;
			}
		}
		if (mFBPost == null) {
			finish();
			return;
		}

		Button sendButton = (Button) findViewById(R.id.activity_post_button_send);
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendComment();
			}
		});

		updateComments();
	}

	private void updateComments() {
		LinearLayout container = (LinearLayout) findViewById(R.id.activity_post_content);
		container.removeAllViews();

		for (FBComment comment : mFBPost.getComments()) {
			View commentView = getLayoutInflater().inflate(
					R.layout.view_comment, null);

			TextView fromView = (TextView) commentView
					.findViewById(R.id.view_comment_from);
			fromView.setText(comment.getFromName());

			TextView messageView = (TextView) commentView
					.findViewById(R.id.view_comment_message);
			messageView.setText(comment.getMessage());

			TextView detailsView = (TextView) commentView
					.findViewById(R.id.view_comment_details);
			detailsView.setText(StringUtils.convertFBTime(comment.getCreatedTime()));

			container.addView(commentView);
		}
	}

	private void sendComment() {
		showProgressDialog();
		SendRequest request = new SendRequest(this,
				(EditText) findViewById(R.id.activity_post_edit));
		getGlobalState().getRequestQueue().addRequest(request);
	}

	private class SendRequest extends RequestUI {

		private EditText mEditText;
		private String mMessage;

		public SendRequest(Activity activity, EditText editText) {
			super(activity, activity);
			mEditText = editText;
			mMessage = mEditText.getText().toString().trim();
		}

		@Override
		public void execute() throws Exception {
			try {
				if (mMessage.length() != 0) {
					mFBPost.sendComment(mMessage);
					mMessage = "";
					mFBPost.update();
				}
			} catch (Exception ex) {
				hideProgressDialog();
				showAlertDialog(ex.toString());
			}
		}

		@Override
		public void executeUI() {
			mEditText.setText(mMessage);
			updateComments();
			hideProgressDialog();
		}

	}

}
