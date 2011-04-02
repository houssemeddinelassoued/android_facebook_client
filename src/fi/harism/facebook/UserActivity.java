package fi.harism.facebook;

import fi.harism.facebook.dao.FBBitmap;
import fi.harism.facebook.dao.FBUser;
import fi.harism.facebook.request.RequestUI;
import fi.harism.facebook.view.ProfilePictureView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class UserActivity extends BaseActivity {

	private String mUserId;
	private String mUserName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_user);

		mUserId = getIntent().getStringExtra(
				"fi.harism.facebook.UserActivity.user");
		FBUser fbUser = getGlobalState().getFBFactory().getUser(mUserId);
		if (fbUser.getLevel() != FBUser.Level.FULL) {
			showProgressDialog();
			ProfilePictureView picView = (ProfilePictureView) findViewById(R.id.activity_user_picture);
			picView.setBitmap(getGlobalState().getDefaultPicture());
			ProfileRequest request = new ProfileRequest(this, fbUser);
			getGlobalState().getRequestQueue().addRequest(request);
		} else {
			setUserInfo(fbUser);
		}

		Button wallButton = (Button) findViewById(R.id.activity_user_button_wall);
		wallButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = createIntent(FeedActivity.class);
				i.putExtra("fi.harism.facebook.FeedActivity.path", mUserId
						+ "/feed");
				String title = getResources().getString(
						R.string.activity_user_wall_title, mUserName);
				i.putExtra("fi.harism.facebook.FeedActivity.title", title);
				startActivity(i);
			}
		});
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

	private void setUserInfo(FBUser fbUser) {
		TextView nameView = (TextView) findViewById(R.id.activity_user_name);
		nameView.setText(fbUser.getName());
		mUserName = fbUser.getName();

		ProfilePictureView picView = (ProfilePictureView) findViewById(R.id.activity_user_picture);
		FBBitmap fbBitmap = getGlobalState().getFBFactory().getBitmap(
				fbUser.getPicture());
		Bitmap bitmap = fbBitmap.getBitmap();
		if (bitmap == null) {
			picView.setBitmap(getGlobalState().getDefaultPicture());
			PictureRequest request = new PictureRequest(this, picView, fbBitmap);
			getGlobalState().getRequestQueue().addRequest(request);
		} else {
			picView.setBitmap(bitmap);
		}
		
		LinearLayout details = (LinearLayout) findViewById(R.id.activity_user_content);
		
		if (fbUser.getStatus() != null) {
			details.addView(createDetailView("Status", fbUser.getStatus()));
		}
		if (fbUser.getBirthday() != null) {
			details.addView(createDetailView("Birthday", fbUser.getBirthday()));
		}
		if (fbUser.getSex() != null) {
			details.addView(createDetailView("Sex", fbUser.getSex()));
		}
		if (fbUser.getAffiliations().size() > 0) {
			String networks = TextUtils.join(", ", fbUser.getAffiliations());
			details.addView(createDetailView("Networks", networks));
		}
		if (fbUser.getEmail() != null) {
			details.addView(createDetailView("Email", fbUser.getEmail()));
		}
		if (fbUser.getWebsite() != null) {
			details.addView(createDetailView("Web Site", fbUser.getWebsite()));
		}
		if (fbUser.getHometownLocation() != null) {
			details.addView(createDetailView("Hometown", fbUser.getHometownLocation()));
		}
		if (fbUser.getCurrentLocation() != null) {
			details.addView(createDetailView("Current Location", fbUser.getCurrentLocation()));
		}
		
		
	}
	
	private View createDetailView(String name, String content) {
		View v = getLayoutInflater().inflate(R.layout.view_user_detail, null);
		TextView titleView = (TextView) v.findViewById(R.id.view_user_detail_title);
		titleView.setText(name);
		TextView contentView = (TextView) v.findViewById(R.id.view_user_detail_content);
		contentView.setText(content);
		return v;
	}

	private class PictureRequest extends RequestUI {

		private ProfilePictureView mPicView;
		private FBBitmap mFBBitmap;

		public PictureRequest(Activity activity, ProfilePictureView picView,
				FBBitmap fbBitmap) {
			super(activity, activity);
			mPicView = picView;
			mFBBitmap = fbBitmap;
		}

		@Override
		public void execute() throws Exception {
			mFBBitmap.load();
		}

		@Override
		public void executeUI() {
			mPicView.setBitmap(mFBBitmap.getBitmap());
		}
	}

	private class ProfileRequest extends RequestUI {

		private FBUser mFBUser;

		public ProfileRequest(Activity activity, FBUser fbUser) {
			super(activity, activity);
			mFBUser = fbUser;
		}

		@Override
		public void execute() throws Exception {
			try {
				mFBUser.load(FBUser.Level.FULL);
			} catch (Exception ex) {
				hideProgressDialog();
				throw ex;
			}
		}

		@Override
		public void executeUI() {
			setUserInfo(mFBUser);
			hideProgressDialog();
		}
	}

}
