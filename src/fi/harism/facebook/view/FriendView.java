package fi.harism.facebook.view;

import fi.harism.facebook.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FriendView extends LinearLayout {
	
	private String mName = "";

	public FriendView(Context context) {
		super(context);
	}
	
	public FriendView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public FriendView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
	}
	
	public String getName() {
		return mName;
	}
	
	public void setName(String name) {
		if (name == null) {
			name = "";
		}
		mName = name;
		
		TextView tv = (TextView)findViewById(R.id.view_friend_name);
		tv.setText(mName);
		if (mName.length() == 0) {
			tv.setVisibility(View.GONE);
		} else {
			tv.setVisibility(View.VISIBLE);
		}
	}
	
	public void setContent(String content) {
		if (content == null) {
			content = "";
		}
		
		TextView tv = (TextView)findViewById(R.id.view_friend_content);
		tv.setText(content);
		if (content.length() == 0) {
			tv.setVisibility(View.GONE);
		} else {
			tv.setVisibility(View.VISIBLE);
		}
	}
	
	public void setDetails(String details) {
		if (details == null) {
			details = "";
		}
		
		TextView tv = (TextView)findViewById(R.id.view_friend_details);
		tv.setText(details);
		if (details.length() == 0) {
			tv.setVisibility(View.GONE);
		} else {
			tv.setVisibility(View.VISIBLE);
		}
	}

	public void setPicture(Bitmap bitmap) {
		ProfilePictureView pv = (ProfilePictureView) findViewById(R.id.view_friend_picture);
		pv.setBitmap(bitmap);
	}
	
}
