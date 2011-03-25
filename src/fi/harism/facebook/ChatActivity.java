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
import fi.harism.facebook.dao.FBChatUser;
import fi.harism.facebook.dao.FBObserver;
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
		Vector<FBChatUser> users = fbChat.getUsers();
		for (FBChatUser user : users) {
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
	public void onPresenceChanged(final FBChatUser user) {
		if (user.getName() == null) {
			fbChat.getUserInfo(user, this, new FBChatUserObserver());
		} else {
			runOnUiThread(new Runnable() {
				public void run() {
					handlePresenceChange(user);
				}
			});
		}
	}
	
	@Override
	public void onMessage(FBChatUser user, String message) {
	}
	
	private void handlePresenceChange(FBChatUser user) {
		LinearLayout list = (LinearLayout) findViewById(R.id.chat_user_list);
		View v = list.findViewWithTag(user.getId());
		
		if (v != null && user.getPresence() == FBChatUser.PRESENCE_GONE) {
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
			v.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View item) {
					Intent i = createIntent(ChatSessionActivity.class);
					i.putExtra("with", (String)item.getTag());
					startActivity(i);
				}
			});
			
			list.addView(v);
			
			if (user.getPictureUrl() != null) {
				fbBitmap.load(user.getPictureUrl(), this, new FBBitmapObserver(user.getId()));
			}
		}
	}
	
	private class FBChatUserObserver implements FBObserver<FBChatUser> {
		@Override
		public void onComplete(FBChatUser response) {
			handlePresenceChange(response);
		}
		@Override
		public void onError(Exception error) {
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
