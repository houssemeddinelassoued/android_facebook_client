package fi.harism.facebook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import fi.harism.facebook.net.NetController;

/**
 * Our main activity, in a sense it's the first activity user sees once our
 * application is started. This activity is rather pointless as it provides only
 * a button to trigger Facebook API authorize procedure. And all this
 * could/should be implemented in MainActivity instead.
 * 
 * TODO: Remove this activity once MainActivity is able to handle Facebook API
 * authorization procedure.
 * 
 * @author harism
 */
public class LoginActivity extends BaseActivity {

	// Instance of NetController.
	private NetController netController = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		// This is the first time getNetController is called and our
		// application wide instance of NetController is created.
		netController = getGlobalState().getNetController();

		// Add onClickListener to 'login' button.
		Button b = (Button) findViewById(R.id.login_button);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				facebookAuthorize();
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		netController = null;
	}

	/**
	 * Calling this method triggers the Facebook API authorization procedure.
	 */
	private final void facebookAuthorize() {
		NetController.AuthorizeObserver loginObserver = new FacebookAuthorizeObserver();
		netController.authorize(this, loginObserver);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		netController.authorizeCallback(requestCode, resultCode, data);
	}

	/**
	 * FacebookAuthorizeObserver observer for Facebook authentication procedure.
	 */
	private final class FacebookAuthorizeObserver implements
			NetController.AuthorizeObserver {
		@Override
		public void onCancel() {
			// We are not interested in doing anything if user cancels Facebook
			// authorization dialog. Let them click 'login' again or close the
			// application.
		}

		@Override
		public void onComplete() {
			// Finish this activity.
			finish();
			// Trigger MainActivity into view.
			Intent i = createIntent(MainActivity.class);
			startActivity(i);
		}

		@Override
		public void onError(Exception ex) {
			// If there was an error during authorization show an alert to user.
			showAlertDialog(ex.getLocalizedMessage());
		}
	}

}
