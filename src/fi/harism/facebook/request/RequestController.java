package fi.harism.facebook.request;

import java.util.ArrayDeque;

public final class RequestController implements Request.Observer {

	private ArrayDeque<Request> requests = null;
	private Request currentRequest = null;

	public RequestController() {
		requests = new ArrayDeque<Request>();
	}

	public final void clear() {
		requests.clear();
		if (currentRequest != null) {
			currentRequest.stop();
			currentRequest = null;
		}
	}

	public final void addRequest(Request request) {
		requests.addLast(request);
		processNextRequest();
	}

	private final void processNextRequest() {
		if (currentRequest == null || currentRequest.hasStopped()) {
			if (!requests.isEmpty()) {
				currentRequest = requests.removeFirst();
				new Thread(currentRequest).start();
			}
		}
	}

	@Override
	public void onComplete() {
		processNextRequest();
	}

}
