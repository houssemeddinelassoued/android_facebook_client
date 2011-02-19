package fi.harism.facebook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import fi.harism.facebook.util.FacebookController;

public class LoginActivity extends BaseActivity {

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
		facebookController.authorize(this,
				new FacebookController.LoginObserver() {
					@Override
					public void onError(Exception ex) {
						showAlertDialog(ex.getLocalizedMessage());
					}

					@Override
					public void onComplete() {
						showMainActivity();
					}

					@Override
					public void onCancel() {
					}
				});
	}

	private void showMainActivity() {
		finish();
		Intent i = new Intent(this, MainActivity.class);
		startActivity(i);
	}

}
