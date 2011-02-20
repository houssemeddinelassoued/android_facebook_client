package fi.harism.facebook.request;

import java.util.ArrayList;

import android.os.Bundle;
import fi.harism.facebook.BaseActivity;

public final class RequestController implements Request.Observer {

	private ArrayList<Request> requests = null;
	private Request currentRequest = null;
	private BaseActivity activity = null;
	private boolean paused;

	public RequestController(BaseActivity activity) {
		requests = new ArrayList<Request>();
		this.activity = activity;
		paused = false;
	}

	public final void addRequest(Request request) {
		requests.add(request);
		processNextRequest();
	}

	public final FacebookRequest createFacebookRequest(String requestPath,
			Bundle requestBundle, FacebookRequest.Observer observer) {
		FacebookRequest request = new FacebookRequest(activity, this,
				requestPath, requestBundle, observer);
		return request;
	}

	public final FacebookRequest createFacebookRequest(String requestPath,
			FacebookRequest.Observer observer) {
		FacebookRequest request = new FacebookRequest(activity, this,
				requestPath, observer);
		return request;
	}

	public final ImageRequest createImageRequest(String url,
			ImageRequest.Observer observer) {
		ImageRequest request = new ImageRequest(activity, this, url, observer);
		return request;
	}

	public final void destroy() {
		requests.clear();
		requests = null;
		if (currentRequest != null) {
			currentRequest.stop();
			currentRequest = null;
		}
		activity = null;
	}

	@Override
	public void onComplete() {
		processNextRequest();
	}

	public final void pause() {
		paused = true;
	}

	public final void resume() {
		paused = false;
		processNextRequest();
	}

	private final void processNextRequest() {
		if (!paused && (currentRequest == null || currentRequest.hasStopped())) {
			if (!requests.isEmpty()) {
				currentRequest = requests.remove(0);
				new Thread(currentRequest).start();
			}
		}
	}

}
