package fi.harism.facebook.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_profile);
		
		TextView idView = (TextView) findViewById(R.id.dialog_profile_id_text);
		idView.setText(profile.getId());
		
		TextView nameView = (TextView) findViewById(R.id.dialog_profile_name_text);
		nameView.setText(profile.getName());
		
		TextView statusView = (TextView) findViewById(R.id.dialog_profile_status_text);
		statusView.setText(profile.getStatus());
	}

}
