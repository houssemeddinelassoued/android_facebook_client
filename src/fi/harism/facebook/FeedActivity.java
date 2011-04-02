package fi.harism.facebook;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
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
 * Feed Activity for showing feed listings. TODO: This is a disaster.
 * 
 * @author harism
 */
public class FeedActivity extends BaseActivity {

	// Default picture used as sender's profile picture.
	private Bitmap defaultPicture = null;
	// Rounding radius for user picture.
	// TODO: Move this value to resources instead.
	private static final int PICTURE_ROUND_RADIUS = 7;

	// Span onClick observer for profile and comments protocols.
	private SpanClickObserver spanClickObserver = null;
	// Static protocol name for showing profile.
	private static final String PROTOCOL_SHOW_PROFILE = "showprofile://";
	// Static protocol name for showing comments.
	private static final String PROTOCOL_SHOW_COMMENTS = "showcomments://";
	// Static protocol name for showing likes.
	private static final String PROTOCOL_SHOW_LIKES = "showlikes://";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_feed);

		// Create default picture from resources.
		defaultPicture = getGlobalState().getDefaultPicture();
		defaultPicture = BitmapUtils.roundBitmap(defaultPicture,
				PICTURE_ROUND_RADIUS);

		spanClickObserver = new SpanClickObserver(this);

		TextView title = (TextView) findViewById(R.id.header);
		title.setText(getIntent().getStringExtra(
				"fi.harism.facebook.FeedActivity.title"));

		final FBFeed fbFeed = getGlobalState().getFBFactory().getFeed(
				getIntent().getStringExtra(
						"fi.harism.facebook.FeedActivity.path"));
		final Activity self = this;

		View updateButton = findViewById(R.id.feed_button_update);
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
	 * Creates new feed post.
	 * 
	 * @param feedItem
	 *            Feed FBPost to be added.
	 */
	private View createPostView(FBPost feedItem) {
		String itemId = feedItem.getId();

		// Create default Feed Item view.
		View feedItemView = getLayoutInflater().inflate(R.layout.view_post,
				null);

		// We need id of sender later on to trigger profile picture loading.
		String fromId = feedItem.getFromId();
		// Get sender's name or use empty string if none found.
		String fromName = feedItem.getFromName();

		// Set sender's name.
		TextView fromView = (TextView) feedItemView
				.findViewById(R.id.view_post_from);
		StringUtils.setTextLink(fromView, fromName, PROTOCOL_SHOW_PROFILE
				+ fromId, spanClickObserver);

		// Get message from feed item. Message is the one user can add as a
		// description to items posted.
		String message = feedItem.getMessage();
		TextView messageView = (TextView) feedItemView
				.findViewById(R.id.view_post_message);
		if (message != null) {
			StringUtils.setTextLinks(messageView, message, null);
		} else {
			messageView.setVisibility(View.GONE);
		}

		// Get name from feed item. Name is shortish description like string
		// for feed item.
		String name = feedItem.getName();
		TextView nameView = (TextView) feedItemView
				.findViewById(R.id.view_post_name);
		if (name != null) {
			if (feedItem.getLink() != null) {
				StringUtils.setTextLink(nameView, name, feedItem.getLink(),
						null);
			} else {
				nameView.setText(name);
			}
		} else {
			nameView.setVisibility(View.GONE);
		}

		String caption = feedItem.getCaption();
		TextView captionView = (TextView) feedItemView
				.findViewById(R.id.view_post_caption);
		if (caption != null) {
			StringUtils.setTextLinks(captionView, caption, null);
		} else {
			captionView.setVisibility(View.GONE);
		}

		// Get description from feed item. This is longer description for
		// feed item.
		String description = feedItem.getDescription();
		TextView descriptionView = (TextView) feedItemView
				.findViewById(R.id.view_post_description);
		if (description != null) {
			StringUtils.setTextLinks(descriptionView, description, null);
		} else {
			descriptionView.setVisibility(View.GONE);
		}

		// Get created time from feed item.
		String created = feedItem.getCreatedTime();
		TextView detailsView = (TextView) feedItemView
				.findViewById(R.id.view_post_details);
		String details = "";
		if (created != null) {
			details += StringUtils.convertFBTime(created);
			details += "  á  ";
		}
		int commentsSpanStart = details.length();
		details += "Comments (" + feedItem.getCommentsCount() + ")";
		int commentsSpanEnd = details.length();
		details += "  á  ";
		int likesSpanStart = details.length();
		details += "Likes (" + feedItem.getLikesCount() + ")";
		int likesSpanEnd = details.length();
		SpannableString detailsString = new SpannableString(details);
		FacebookURLSpan commentsSpan = new FacebookURLSpan(
				PROTOCOL_SHOW_COMMENTS + itemId);
		commentsSpan.setObserver(spanClickObserver);
		detailsString.setSpan(commentsSpan, commentsSpanStart, commentsSpanEnd,
				0);
		FacebookURLSpan likesSpan = new FacebookURLSpan(PROTOCOL_SHOW_LIKES
				+ itemId);
		likesSpan.setObserver(spanClickObserver);
		detailsString.setSpan(likesSpan, likesSpanStart, likesSpanEnd, 0);
		detailsView.setText(detailsString);
		detailsView.setMovementMethod(LinkMovementMethod.getInstance());

		// Set default picture as sender's picture.
		ProfilePictureView profilePic = (ProfilePictureView) feedItemView
				.findViewById(R.id.view_post_from_picture);
		profilePic.setBitmap(defaultPicture);

		return feedItemView;
	}

	private void updateFeedView(FBFeed fbFeed) {
		LinearLayout contentView = (LinearLayout) findViewById(R.id.feed_list);
		contentView.removeAllViews();
		for (FBPost post : fbFeed.getPosts()) {
			View postView = createPostView(post);
			postView.setTag(post.getId());

			if (post.getPicture() != null) {
				ImageView imageView = (ImageView) postView
						.findViewById(R.id.view_post_picture);
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
			}

			ProfilePictureView profilePic = (ProfilePictureView) postView
					.findViewById(R.id.view_post_from_picture);
			FBUser fbUser = getGlobalState().getFBFactory().getUser(
					post.getFromId());
			if (fbUser.getLevel() == FBUser.Level.UNINITIALIZED) {
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
					FromPictureRequest request = new FromPictureRequest(this,
							profilePic, fbUser);
					getGlobalState().getRequestQueue().addRequest(request);
				}
			}

			contentView.addView(postView);
		}

	}

	private final class FBFeedRequest extends RequestUI {

		private FBFeed fbFeed;

		public FBFeedRequest(Activity activity, FBFeed fbFeed) {
			super(activity, activity);
			this.fbFeed = fbFeed;
		}

		@Override
		public void execute() {
			try {
				fbFeed.load();
			} catch (Exception ex) {
				// Hide progress dialog.
				hideProgressDialog();
				// We don't want to see this happening but just in case.
				showAlertDialog(ex.getLocalizedMessage());
			}
		}

		@Override
		public void executeUI() {
			updateFeedView(fbFeed);
			hideProgressDialog();
		}

	}

	/**
	 * Private class for handling actual profile picture requests.
	 * 
	 * @author harism
	 */
	private final class FromPictureRequest extends RequestUI {

		private ProfilePictureView profilePic;
		private FBUser fbUser;
		private FBBitmap fbBitmap;

		public FromPictureRequest(Activity activity,
				ProfilePictureView profilePic, FBUser fbUser) {
			super(activity, activity);
			this.profilePic = profilePic;
			this.fbUser = fbUser;
		}

		@Override
		public void execute() throws Exception {
			fbUser.load(FBUser.Level.DEFAULT);
			fbBitmap = getGlobalState().getFBFactory().getBitmap(
					fbUser.getPicture());
			fbBitmap.load();
		}

		@Override
		public void executeUI() {
			Bitmap rounded = BitmapUtils.roundBitmap(fbBitmap.getBitmap(),
					PICTURE_ROUND_RADIUS);
			profilePic.setBitmap(rounded);
		}
	}

	/**
	 * Private class for handling feed post picture requests.
	 * 
	 * @author harism
	 */
	private final class PostPictureRequest extends RequestUI {

		private ImageView imageView;
		private FBBitmap bitmap;

		public PostPictureRequest(Activity activity, ImageView imageView,
				FBBitmap bitmap) {
			super(activity, activity);
			this.imageView = imageView;
			this.bitmap = bitmap;
		}

		@Override
		public void execute() throws Exception {
			bitmap.load();
		}

		@Override
		public void executeUI() {
			imageView.setImageBitmap(bitmap.getBitmap());
			// TODO: Image size is (0, 0) and animation never takes place.
			Rect r = new Rect();
			if (imageView.getLocalVisibleRect(r)) {
				AlphaAnimation inAnimation = new AlphaAnimation(0, 1);
				inAnimation.setDuration(700);
				imageView.startAnimation(inAnimation);
			}
		}
	}

	/**
	 * Click listener for our own protocols. Rest is handled by default handler.
	 */
	private final class SpanClickObserver implements
			FacebookURLSpan.ClickObserver {
		private BaseActivity activity = null;

		public SpanClickObserver(BaseActivity activity) {
			this.activity = activity;
		}

		@Override
		public boolean onClick(FacebookURLSpan span) {
			String url = span.getURL();
			if (url.startsWith(PROTOCOL_SHOW_PROFILE)) {
				showAlertDialog(url);
				return true;
			} else if (url.startsWith(PROTOCOL_SHOW_COMMENTS)) {
				showAlertDialog(url);
				return true;
			} else if (url.startsWith(PROTOCOL_SHOW_LIKES)) {
				showAlertDialog(url);
				return true;
			}
			return false;
		}
	}
}
