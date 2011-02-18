package fi.harism.facebook.net;

import org.json.JSONObject;

public abstract class FacebookRequestObserver {

	public abstract void requestError(Exception ex);

	public abstract void requestDone(JSONObject response);

}
