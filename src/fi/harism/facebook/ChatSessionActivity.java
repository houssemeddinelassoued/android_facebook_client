package fi.harism.facebook;

import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import fi.harism.facebook.dao.FBChat;
import fi.harism.facebook.dao.FBChatUser;

public class ChatSessionActivity extends BaseActivity implements
		FBChat.Observer {

	private FBChat fbChat;
	private FBChatUser fbChatUser;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.chat_session);

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

		Button sendButton = (Button) findViewById(R.id.chat_session_button_send);
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText edit = (EditText) findViewById(R.id.chat_session_edit);
				Editable editable = edit.getText();
				String message = editable.toString().trim();
				if (message.length() > 0) {
					fbChat.sendMessage(fbChatUser, message);
					addText("Me:\n" + message + "\n\n");
					editable.clear();
				}
			}
		});

		fbChat = getGlobalState().getFBFactory().getChat(this);

		String withId = getIntent().getStringExtra("with");
		addText("Chatting with\nid=" + withId + "\n");

		fbChatUser = fbChat.getUser(withId);
		addText("name=" + fbChatUser.getName() + "\n\n");
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

	private void addText(String text) {
		TextView tv = (TextView) findViewById(R.id.chat_session_text);
		CharSequence currentText = tv.getText();
		String newText = currentText + text;
		tv.setText(newText);
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
		runOnUiThread(new Runnable() {
			public void run() {
				handlePresenceChange(user);
			}
		});
	}

	@Override
	public void onMessage(final FBChatUser user, final String message) {
		if (user.getId().equals(fbChatUser.getId())) {
			runOnUiThread(new Runnable() {
				public void run() {
					addText(user.getName() + ":\n" + message + "\n\n");
				}
			});
		}
	}

	private void handlePresenceChange(FBChatUser user) {
		if (user.getId().equals(fbChatUser.getId())) {
			switch (user.getPresence()) {
			case FBChatUser.PRESENCE_AWAY:
				addText(user.getName() + " is away.\n\n");
				break;
			case FBChatUser.PRESENCE_CHAT:
				addText(user.getName() + " is online.\n\n");
				break;
			case FBChatUser.PRESENCE_GONE:
				addText(user.getName() + " went offline.\n\n");
				break;
			}
		}
	}

}
