package fi.harism.facebook;

import java.util.Vector;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import fi.harism.facebook.dao.FBBitmap;
import fi.harism.facebook.dao.FBChat;
import fi.harism.facebook.dao.FBObserver;
import fi.harism.facebook.dao.FBUser;
import fi.harism.facebook.util.BitmapUtils;

public class ChatActivity extends BaseActivity implements FBChat.Observer {

	private FBChat fbChat;
	private FBBitmap fbBitmap;
	private Bitmap defaultPicture;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.chat);

		Button connectButton = (Button) findViewById(R.id.chat_button_connect);
		connectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				connect();
			}
		});

		Button closeButton = (Button) findViewById(R.id.chat_button_disconnect);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				close();
			}
		});
		
		Button logButton = (Button) findViewById(R.id.chat_button_showlog);
		logButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAlertDialog(fbChat.getLog());
			}
		});
		
		fbBitmap = getGlobalState().getFBFactory().getBitmap();
		Bitmap bitmap = getGlobalState().getDefaultPicture();
		defaultPicture = BitmapUtils.roundBitmap(bitmap, 7);
		
		fbChat = getGlobalState().getFBFactory().getChat(this);
		Vector<FBUser> users = fbChat.getUsers();
		for (FBUser user : users) {
			onPresenceChanged(user);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		fbChat.onDestroy();
	}

	private void connect() {
		fbChat.connect(this);
	}

	private void close() {
		fbChat.disconnect();
	}

	@Override
	public void onConnected() {
		runOnUiThread(new Runnable() {
			public void run() {
				showAlertDialog("Connected.");
			}
		});
	}

	@Override
	public void onDisconnected() {
		runOnUiThread(new Runnable() {
			public void run() {
				showAlertDialog("Disconnected.");
			}
		});
	}

	@Override
	public void onPresenceChanged(final FBUser user) {
		runOnUiThread(new Runnable() {
			public void run() {
				handlePresenceChange(user);
			}
		});
	}
	
	@Override
	public void onMessage(FBUser user, String message) {
	}
	
	private void handlePresenceChange(FBUser user) {
		LinearLayout list = (LinearLayout) findViewById(R.id.chat_user_list);
		View v = list.findViewWithTag(user.getId());
		
		if (v != null && user.getPresence() == FBUser.Presence.GONE) {
			list.removeView(v);
		} else if (v != null) {
			// Update user presence somehow.
		} else {
			v = getLayoutInflater().inflate(R.layout.chat_user, null);
			TextView tv = (TextView) v.findViewById(R.id.chat_user_name);
			tv.setText(user.getName());
			
			ImageView image = (ImageView) v.findViewById(R.id.chat_user_picture);
			image.setImageBitmap(defaultPicture);
			
			v.setTag(user.getId());
			v.setTag(R.id.view_storage, user);
			v.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View item) {
					Intent i = createIntent(ChatSessionActivity.class);
					i.putExtra("fi.harism.facebook.ChatSessionActivity", (FBUser)item.getTag(R.id.view_storage));
					startActivity(i);
				}
			});
			
			list.addView(v);
			
			if (user.getPicture() != null) {
				fbBitmap.load(user.getPicture(), this, new FBBitmapObserver(user.getId()));
			}
		}
	}
	
	private class FBBitmapObserver implements FBObserver<Bitmap> {
		
		private String userId;
		
		public FBBitmapObserver(String userId) {
			this.userId = userId;
		}

		@Override
		public void onComplete(Bitmap response) {
			LinearLayout list = (LinearLayout) findViewById(R.id.chat_user_list);
			View v = list.findViewWithTag(userId);
			if (v != null) {
				ImageView image = (ImageView) v.findViewById(R.id.chat_user_picture);
				image.setImageBitmap(BitmapUtils.roundBitmap(response, 7));
			}
		}

		@Override
		public void onError(Exception error) {
		}
		
	}

}
