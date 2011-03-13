package fi.harism.facebook.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.TextView;
import fi.harism.facebook.R;
import fi.harism.facebook.dao.DAOObserver;
import fi.harism.facebook.dao.DAOProfile;
import fi.harism.facebook.net.RequestController;

public class ProfileDialog extends Dialog {

	private Activity activity;
	private RequestController netController;
	private String profileId;

	public ProfileDialog(Activity activity,
			RequestController netController, String profileId) {
		super(activity);
		this.activity = activity;
		this.netController = netController;
		this.profileId = profileId;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_profile);
		netController.getProfile(activity, profileId,
				new DAOProfileObserver());
	}

	private class DAOProfileObserver implements
			DAOObserver<DAOProfile> {

		@Override
		public void onComplete(DAOProfile profile) {
			TextView textView = (TextView) findViewById(R.id.dialog_profile_text);
			textView.setText(profile.getName());
		}

		@Override
		public void onError(Exception ex) {
		}

	}

}
