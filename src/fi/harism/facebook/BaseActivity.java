package fi.harism.facebook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;

public class BaseActivity extends Activity {

	private ProgressDialog progressDialog = null;

	public void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Loading..");
			progressDialog.show();
		}
	}

	public void hideProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	public void showAlertDialog(String message) {
		AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
		dlgBuilder.setMessage(message);
		AlertDialog dlg = dlgBuilder.create();
		dlg.show();
	}

	public Intent createIntent(Class<?> cls) {
		Intent i = new Intent(this, cls);
		return i;
	}

}
