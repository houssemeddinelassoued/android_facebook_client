package fi.harism.facebook.dialog;

import java.io.StringWriter;
import java.util.Iterator;

import org.json.JSONObject;

import fi.harism.facebook.BaseActivity;
import fi.harism.facebook.R;
import fi.harism.facebook.request.Request;
import fi.harism.facebook.request.RequestQueue;
import fi.harism.facebook.request.FacebookRequest;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.TextView;

public class ProfileDialog extends Dialog {

	private RequestQueue requestController;
	private String profilePath;

	public ProfileDialog(BaseActivity baseActivity,
			RequestQueue requestController, String profilePath) {
		super(baseActivity);
		this.requestController = requestController;
		this.profilePath = profilePath;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_profile);
		
		//FacebookRequest.Observer observer = new FacebookProfileObserver();
		//FacebookRequest request = requestController.createFacebookRequest(profilePath, observer);
		//request.setPriority(Request.PRIORITY_HIGH + 1);
		//requestController.addRequest(request);
	}
	
	private class FacebookProfileObserver implements FacebookRequest.Observer {

		@Override
		public void onComplete(FacebookRequest facebookRequest) {
			JSONObject resp = facebookRequest.getResponse();
			
			StringWriter out = new StringWriter();
			Iterator it = resp.keys();
			while (it.hasNext()) {
				String key = (String)it.next();
				String value = resp.optString(key);
				out.write(key);
				out.write(": ");
				out.write(value);
				out.write("\n");
			}
			
			TextView textView = (TextView)findViewById(R.id.dialog_profile_text);
			textView.setText(out.toString());
		}

		@Override
		public void onError(Exception ex) {
		}
		
	}

}
