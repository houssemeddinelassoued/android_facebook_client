package fi.harism.facebook.dialog;

import fi.harism.facebook.R;
import fi.harism.facebook.dao.DAOProfile;
import fi.harism.facebook.net.NetController;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.TextView;

public class ProfileDialog extends Dialog {

	private Activity activity;
	private NetController netController;
	private String profileId;

	public ProfileDialog(Activity activity,
			NetController netController, String profileId) {
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
				new FacebookProfileObserver());
	}

	private class FacebookProfileObserver implements
			NetController.RequestObserver<DAOProfile> {

		@Override
		public void onComplete(DAOProfile profile) {
			TextView textView = (TextView) findViewById(R.id.dialog_profile_text);
			textView.setText(profile.getData());
		}

		@Override
		public void onError(Exception ex) {
		}

	}

}
