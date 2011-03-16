package fi.harism.facebook.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.TextView;
import fi.harism.facebook.R;
import fi.harism.facebook.dao.DAOProfile;

public class ProfileDialog extends Dialog {

	private DAOProfile profile;

	public ProfileDialog(Activity activity, DAOProfile profile) {
		super(activity);
		this.profile = profile;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_profile);
		
		TextView textView = (TextView) findViewById(R.id.dialog_profile_text);
		textView.setText(profile.getName());
	}

}
