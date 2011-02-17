package fi.harism.facebook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

public class LoginActivity extends Activity {
	
	private static final String FACEBOOK_APP_ID = "190087744355420";
	public static final Facebook facebook = new Facebook(FACEBOOK_APP_ID);
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        Button b = (Button)findViewById(R.id.login_button);
        b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				facebookAuthorize();
			}
		});
    }
    
    private void facebookAuthorize() {
    	facebook.authorize(this, new Facebook.DialogListener() {
			@Override
			public void onFacebookError(FacebookError e) {
				showAlert(
						getText(R.string.login_alert_facebook_error_title),
						e.getLocalizedMessage(),
						false);
			}
			@Override
			public void onError(DialogError e) {
				showAlert(
						getText(R.string.login_alert_dialog_error_title),
						e.getLocalizedMessage(),
						false);
			}
			@Override
			public void onComplete(Bundle values) {
				showAlert(
						getText(R.string.login_alert_complete_title),
						getText(R.string.login_alert_complete_message),
						true);
			}
			@Override
			public void onCancel() {
				showAlert(
						getText(R.string.login_alert_cancel_title),
						getText(R.string.login_alert_cancel_message),
						false);
			}
		});
    }
      
    private void showAlert(CharSequence title, CharSequence text, final boolean showMainActivity) {
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
