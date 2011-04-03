package fi.harism.facebook;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fi.harism.facebook.dao.FBBitmap;
import fi.harism.facebook.dao.FBPost;
import fi.harism.facebook.dao.FBFeed;
import fi.harism.facebook.dao.FBUser;
import fi.harism.facebook.request.RequestUI;
import fi.harism.facebook.util.BitmapUtils;
import fi.harism.facebook.util.FacebookURLSpan;
import fi.harism.facebook.util.StringUtils;
import fi.harism.facebook.view.ProfilePictureView;

/**
 * Feed Activity for showing feed listings.
 * 
 * @author harism
 */
public class FeedActivity extends BaseActivity {

	// Default picture used as sender's profile picture.
	private Bitmap mDefaultPicture = null;
	// Rounding radius for user picture.
	// TODO: Move this value to resources instead.
	private static final int PICTURE_ROUND_RADIUS = 7;
	// Span onClick observer for profile protocol.
	private SpanClickObserver mSpanClickObserver;
	// Feed path.
	private String mFeedPath;
	// Observer for post onClick events.
	private PostClickObserver mPostClickObserver;
	// Static protocol name for showing profile.
	private static final String PROTOCOL_SHOW_PROFILE = "showprofile://";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_feed);

		// Create default picture from resources.
		mDefaultPicture = getGlobalState().getDefaultPicture();
		mDefaultPicture = BitmapUtils.roundBitmap(mDefaultPicture,
				PICTURE_ROUND_RADIUS);

		mSpanClickObserver = new SpanClickObserver();
		mPostClickObserver = new PostClickObserver();

		TextView title = (TextView) findViewById(R.id.header);
		title.setText(getIntent().getStringExtra(
				"fi.harism.facebook.FeedActivity.title"));

		mFeedPath = getIntent().getStringExtra(
				"fi.harism.facebook.FeedActivity.path");
		final FBFeed fbFeed = getGlobalState().getFBFactory()
				.getFeed(mFeedPath);
		final Activity self = this;

		View updateButton = findViewById(R.id.activity_feed_button_update);
		updateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog();
				getGlobalState().getRequestQueue().removeRequests(self);
				FBFeedRequest request = new FBFeedRequest(self, fbFeed);
				getGlobalState().getRequestQueue().addRequest(request);
			}
		});

		if (fbFeed.getPosts().size() == 0) {
			showProgressDialog();
			FBFeedRequest request = new FBFeedRequest(this, fbFeed);
			getGlobalState().getRequestQueue().addRequest(request);
		} else {
			updateFeedView(fbFeed);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getGlobalState().getRequestQueue().removeRequests(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getGlobalState().getRequestQueue().setPaused(this, true);
	}

	@Override
	public void onResume() {
		super.onResume();
		getGlobalState().getRequestQueue().setPaused(this, false);
	}

	/**
	 * Creates new feed post View.
	 */
	private View createPostView(FBPost post) {
		// Create default Feed Item view.
		View postView = getLayoutInflater().inflate(R.layout.view_post, null);

		// Set sender's name.
		String fromId = post.getFromId();
		String fromName = post.getFromName();
		TextView fromView = (TextView) postView
				.findViewById(R.id.view_post_from);
		StringUtils.setTextLink(fromView, fromName, PROTOCOL_SHOW_PROFILE
				+ fromId, mSpanClickObserver);

		// Get message from feed item. Message is the one user can add as a
		// description to items posted.
		String message = post.getMessage();
		TextView messageView = (TextView) postView
				.findViewById(R.id.view_post_message);
		if (message != null) {
			StringUtils.setTextLinks(messageView, message, null);
		} else {
			messageView.setVisibility(View.GONE);
		}

		// Get name from feed item. Name is shortish description like string
		// for feed item.
		String name = post.getName();
		TextView nameView = (TextView) postView
				.findViewById(R.id.view_post_name);
		if (name != null) {
			if (post.getLink() != null) {
				StringUtils.setTextLink(nameView, name, post.getLink(), null);
			} else {
				nameView.setText(name);
			}
		} else {
			nameView.setVisibility(View.GONE);
		}

		String caption = post.getCaption();
		TextView captionView = (TextView) postView
				.findViewById(R.id.view_post_caption);
		if (caption != null) {
			StringUtils.setTextLinks(captionView, caption, null);
		} else {
			captionView.setVisibility(View.GONE);
		}

		// Get description from feed item. This is longer description for
		// feed item.
		String description = post.getDescription();
		TextView descriptionView = (TextView) postView
				.findViewById(R.id.view_post_description);
		if (description != null) {
			StringUtils.setTextLinks(descriptionView, description, null);
		} else {
			descriptionView.setVisibility(View.GONE);
		}

		// Convert created time to more readable format.
		String createdTime = post.getCreatedTime();
		createdTime = StringUtils.convertFBTime(createdTime);
		TextView detailsView = (TextView) postView
				.findViewById(R.id.view_post_details);
		detailsView.setText(getResources().getString(
				R.string.activity_feed_post_details, createdTime,
				post.getCommentsCount(), post.getLikesCount()));

		return postView;
	}

	/**
	 * Updates list of post views from given FBFeed.
	 */
	private void updateFeedView(FBFeed fbFeed) {
		LinearLayout contentView = (LinearLayout) findViewById(R.id.activity_feed_content);
		contentView.removeAllViews();
		for (FBPost post : fbFeed.getPosts()) {
			View postView = createPostView(post);
			postView.setTag(post.getId());

			ImageView imageView = (ImageView) postView
					.findViewById(R.id.view_post_picture);
			if (post.getPicture() != null) {
				imageView.setVisibility(View.VISIBLE);
				FBBitmap fbBitmap = getGlobalState().getFBFactory().getBitmap(
						post.getPicture());
				Bitmap bitmap = fbBitmap.getBitmap();
				if (bitmap != null) {
					imageView.setImageBitmap(bitmap);
				} else {
					PostPictureRequest request = new PostPictureRequest(this,
							imageView, fbBitmap);
					getGlobalState().getRequestQueue().addRequest(request);
				}
			} else {
				imageView.setVisibility(View.GONE);
			}

			ProfilePictureView profilePic = (ProfilePictureView) postView
					.findViewById(R.id.view_post_from_picture);
			FBUser fbUser = getGlobalState().getFBFactory().getUser(
					post.getFromId());
			if (fbUser.getLevel() == FBUser.Level.UNINITIALIZED) {
				profilePic.setBitmap(mDefaultPicture);
				FromPictureRequest request = new FromPictureRequest(this,
						profilePic, fbUser);
				getGlobalState().getRequestQueue().addRequest(request);
			} else {
				FBBitmap fbBitmap = getGlobalState().getFBFactory().getBitmap(
						fbUser.getPicture());
				Bitmap bitmap = fbBitmap.getBitmap();
				if (bitmap != null) {
					profilePic.setBitmap(BitmapUtils.roundBitmap(bitmap,
							PICTURE_ROUND_RADIUS));
				} else {
					profilePic.setBitmap(mDefaultPicture);
					FromPictureRequest request = new FromPictureRequest(this,
							profilePic, fbUser);
					getGlobalState().getRequestQueue().addRequest(request);
				}
			}
			
			postView.setOnClickListener(mPostClickObserver);
			contentView.addView(postView);
		}

	}

	/**
	 * Request for handling FBFeed loading.
	 */
	private final class FBFeedRequest extends RequestUI {

		private FBFeed mFBFeed;

		public FBFeedRequest(Activity activity, FBFeed fbFeed) {
			super(activity, activity);
			mFBFeed = fbFeed;
		}

		@Override
		public void execute() {
			try {
				mFBFeed.load();
			} catch (Exception ex) {
				// Hide progress dialog.
				hideProgressDialog();
				// We don't want to see this happening but just in case.
				showAlertDialog(ex.getLocalizedMessage());
			}
		}

		@Override
		public void executeUI() {
			updateFeedView(mFBFeed);
			hideProgressDialog();
		}

	}

	/**
	 * Private class for handling sender/from picture requests.
	 */
	private final class FromPictureRequest extends RequestUI {

		private ProfilePictureView mProfilePic;
		private FBUser mFBUser;
		private FBBitmap mFBBitmap;

		public FromPictureRequest(Activity activity,
				ProfilePictureView profilePic, FBUser fbUser) {
			super(activity, activity);
			mProfilePic = profilePic;
			mFBUser = fbUser;
		}

		@Override
		public void execute() throws Exception {
			mFBUser.load(FBUser.Level.DEFAULT);
			mFBBitmap = getGlobalState().getFBFactory().getBitmap(
					mFBUser.getPicture());
			mFBBitmap.load();
		}

		@Override
		public void executeUI() {
			Bitmap rounded = BitmapUtils.roundBitmap(mFBBitmap.getBitmap(),
					PICTURE_ROUND_RADIUS);
			mProfilePic.setBitmap(rounded);
		}
	}

	/**
	 * Private class for handling feed post picture requests.
	 */
	private final class PostPictureRequest extends RequestUI {

		private ImageView mImageView;
		private FBBitmap mFBBitmap;

		public PostPictureRequest(Activity activity, ImageView imageView,
				FBBitmap fbBitmap) {
			super(activity, activity);
			mImageView = imageView;
			mFBBitmap = fbBitmap;
		}

		@Override
		public void execute() throws Exception {
			mFBBitmap.load();
		}

		@Override
		public void executeUI() {
			mImageView.setImageBitmap(mFBBitmap.getBitmap());
			// TODO: Image size is (0, 0) and animation never takes place.
			Rect r = new Rect();
			if (mImageView.getLocalVisibleRect(r)) {
				AlphaAnimation inAnimation = new AlphaAnimation(0, 1);
				inAnimation.setDuration(700);
				mImageView.startAnimation(inAnimation);
			}
		}
	}

	/**
	 * Click listener for our own link protocol. Rest is handled by default
	 * handler.
	 */
	private final class SpanClickObserver implements
			FacebookURLSpan.ClickObserver {
		@Override
		public boolean onClick(FacebookURLSpan span) {
			String url = span.getURL();
			if (url.startsWith(PROTOCOL_SHOW_PROFILE)) {
				String userId = url.substring(PROTOCOL_SHOW_PROFILE.length());
				Intent i = createIntent(UserActivity.class);
				i.putExtra("fi.harism.facebook.UserActivity.user", userId);
				startActivity(i);
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Click observer for post items.
	 */
	private final class PostClickObserver implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			String postId = (String)v.getTag();
			Intent i = createIntent(PostActivity.class);
			i.putExtra(PostActivity.INTENT_FEED_PATH, mFeedPath);
			i.putExtra(PostActivity.INTENT_POST_ID, postId);
			startActivity(i);
		}
		
	}
}
