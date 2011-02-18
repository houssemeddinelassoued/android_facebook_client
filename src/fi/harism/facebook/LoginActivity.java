package fi.harism.facebook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import fi.harism.facebook.util.FacebookController;

public class LoginActivity extends Activity {
	
	private FacebookController facebookController = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		facebookController = FacebookController.getFacebookController();

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
		facebookController = null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		facebookController.authorizeCallback(requestCode, resultCode, data);
	}

	private void facebookAuthorize() {
		facebookController.authorize(this, new FacebookController.LoginObserver() {
			@Override
			public void onError(Exception ex) {
				showAlert(getText(R.string.login_alert_facebook_error_title),
						ex.getLocalizedMessage(), false);
			}

			@Override
			public void onComplete() {
				showAlert(getText(R.string.login_alert_complete_title),
						getText(R.string.login_alert_complete_message), true);
			}

			@Override
			public void onCancel() {
				showAlert(getText(R.string.login_alert_cancel_title),
						getText(R.string.login_alert_cancel_message), false);
			}
		});
	}

	private void showAlert(CharSequence title, CharSequence text,
			final boolean showMainActivity) {
		AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);

		dlgBuilder.setTitle(title);
		dlgBuilder.setMessage(text);

		AlertDialog dlg = dlgBuilder.create();

		dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (showMainActivity) {
					showMainActivity();
				}
			}
		});

		dlg.show();
	}

	private void showMainActivity() {
		finish();
		Intent i = new Intent(this, MainActivity.class);
		startActivity(i);
	}

}
