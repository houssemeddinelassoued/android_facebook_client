package fi.harism.facebook;

import fi.harism.facebook.net.FBClient;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

/**
 * Activity for handling login.
 * 
 * TODO: Execute login a bit differently.
 * 
 * @author harism
 */
public class LoginActivity extends BaseActivity {
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		getGlobalState().getFBClient().authorizeCallback(requestCode,
				resultCode, data);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_login);
		
		final Activity self = this;
		// Add onClickListener to 'login' button.
		View loginButton = findViewById(R.id.activity_login_button);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LoginObserver loginObserver = new LoginObserver();
				getGlobalState().getFBClient().authorize(self, loginObserver);
			}
		});
	}
	
	/**
	 * LoginObserver observer for Facebook authentication procedure.
	 */
	private final class LoginObserver implements FBClient.LoginObserver {
		@Override
		public void onCancel() {
			// We are not interested in doing anything if user cancels Facebook
			// authorization dialog. Let them click 'login' again or close the
			// application.
		}

		@Override
		public void onComplete() {
			// On successful login switch to main view.
			Intent intent = createIntent(MainActivity.class);
			startActivity(intent);
			finish();
		}

		@Override
		public void onError(Exception ex) {
			// If there was an error during authorization show an alert to user.
			showAlertDialog(ex.getLocalizedMessage());
		}
	}

}
