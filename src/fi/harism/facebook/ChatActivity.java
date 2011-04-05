package fi.harism.facebook;

import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import fi.harism.facebook.dao.FBBitmap;
import fi.harism.facebook.dao.FBChat;
import fi.harism.facebook.dao.FBUser;
import fi.harism.facebook.request.RequestUI;
import fi.harism.facebook.util.BitmapUtils;
import fi.harism.facebook.view.BitmapSwitcher;

/**
 * TODO: This is a disaster at the moment.
 * 
 * @author harism
 */
public class ChatActivity extends BaseActivity implements FBChat.Observer {

	private FBChat fbChat;
	private Bitmap defaultPicture;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_chat);

		Button connectButton = (Button) findViewById(R.id.activity_chat_button_connect);
		connectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				connect();
			}
		});

		Button closeButton = (Button) findViewById(R.id.activity_chat_button_disconnect);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				close();
			}
		});

		Button logButton = (Button) findViewById(R.id.activity_chat_button_showlog);
		logButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAlertDialog(fbChat.getLog());
			}
		});

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
		new Thread(new Runnable() {
			public void run() {
				try {
					fbChat.connect();
				} catch (Exception ex) {
					showAlertDialog(ex.toString());
				}
			}
		}).start();
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
				// TODO: It is possible there is presence request waiting in
				// request queue at this point.
				LinearLayout list = (LinearLayout) findViewById(R.id.activity_chat_user_list);
				list.removeAllViews();
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
		LinearLayout list = (LinearLayout) findViewById(R.id.activity_chat_user_list);
		View v = list.findViewWithTag(user.getId());

		if (v != null && user.getPresence() == FBUser.Presence.GONE) {
			list.removeView(v);
		} else if (v != null) {
			// TODO: Update user presence somehow.
		} else {
			v = getLayoutInflater().inflate(R.layout.view_user, null);
			TextView tv = (TextView) v.findViewById(R.id.view_friend_name);
			tv.setText(user.getName());

			v.setTag(user.getId());
			v.setTag(R.id.view_storage, user);
			v.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View item) {
					FBUser fbUser = (FBUser) item.getTag(R.id.view_storage);
					Intent i = createIntent(ChatSessionActivity.class);
					i.putExtra("fi.harism.facebook.ChatSessionActivity.user",
							fbUser.getId());
					startActivity(i);
				}
			});

			list.addView(v);

			// Set default profile picture.
			BitmapSwitcher profilePic = (BitmapSwitcher) v
					.findViewById(R.id.view_friend_picture);

			if (user.getLevel() != FBUser.Level.UNINITIALIZED) {
				TextView nameView = (TextView) v
						.findViewById(R.id.view_friend_name);
				nameView.setText(user.getName());

				FBBitmap fbBitmap = getGlobalState().getFBFactory().getBitmap(
						user.getPicture());
				Bitmap bitmap = fbBitmap.getBitmap();
				if (bitmap != null) {
					profilePic.setBitmap(bitmap);
				} else {
					profilePic.setBitmap(defaultPicture);
					FBBitmapRequest request = new FBBitmapRequest(this, v, user);
					getGlobalState().getRequestQueue().addRequest(request);
				}
			} else {
				profilePic.setBitmap(defaultPicture);
				FBBitmapRequest request = new FBBitmapRequest(this, v, user);
				getGlobalState().getRequestQueue().addRequest(request);
			}
		}
	}

	private class FBBitmapRequest extends RequestUI {

		private View friendView;
		private FBUser fbUser;
		private FBBitmap fbBitmap;

		public FBBitmapRequest(Activity activity, View friendView, FBUser fbUser) {
			super(activity, activity);
			this.friendView = friendView;
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
		public void executeUI(Exception ex) {
			if (ex == null) {
				TextView nameView = (TextView) friendView
						.findViewById(R.id.view_friend_name);
				nameView.setText(fbUser.getName());

				// Search picture Container and set default profile picture into
				// it.
				BitmapSwitcher profilePic = (BitmapSwitcher) friendView
						.findViewById(R.id.view_friend_picture);
				profilePic.setBitmap(BitmapUtils.roundBitmap(
						fbBitmap.getBitmap(), 7));
			}
		}
	}

}
