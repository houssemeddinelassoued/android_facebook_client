package fi.harism.facebook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;

/**
 * Base class for all activities within this application. This class provides
 * some common functions to be used application wide.
 * 
 * @author harism
 */
public class BaseActivity extends Activity {

	// ProgressDialog instance.
	private ProgressDialog mProgressDialog = null;

	/**
	 * Creates an intent for given class. This method makes it easier to create
	 * such intents as you can't have 'this' pointer from e.g. embedded observer
	 * implementations.
	 * 
	 * @param cls
	 *            Class this intent is created for.
	 * @return New Intent instance.
	 */
	public Intent createIntent(Class<?> cls) {
		Intent i = new Intent(this, cls);
		return i;
	}

	/**
	 * This function returns global GlobalState instance.
	 * 
	 * @return Global GlobalState instance.
	 */
	public GlobalState getGlobalState() {
		// Our application is actually GlobalState so we have only have to use
		// getApplication() and cast it to GlobalState.
		return (GlobalState) getApplication();
	}

	/**
	 * Hides progress dialog.
	 * 
	 * @see showProgressDialog()
	 */
	public void hideProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	/**
	 * Shows an alert dialog with given text.
	 * 
	 * @param message
	 *            Message for this alert dialog.
	 */
	public void showAlertDialog(String message) {
		AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
		dlgBuilder.setMessage(message);
		AlertDialog dlg = dlgBuilder.create();
		dlg.show();
	}

	/**
	 * Show progress dialog. Progress dialog is made not cancelable so you have
	 * to make sure hideProgressDialog is called after wards.
	 */
	public void showProgressDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(this);
			// TODO: Either give message as parameter to this function or make
			// it a constant string resource.
			mProgressDialog.setMessage("Loading..");
			mProgressDialog.setCancelable(false);
		}
		mProgressDialog.show();
	}

}
