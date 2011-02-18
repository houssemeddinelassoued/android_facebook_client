package fi.harism.facebook.request;

import java.util.ArrayDeque;

public final class RequestController {

	private static RequestController instance = null;
	private ArrayDeque<Request> requests = null;
	private Request currentRequest = null;

	private RequestController() {
		requests = new ArrayDeque<Request>();
	}

	public final static RequestController getRequestController() {
		if (instance == null) {
			instance = new RequestController();
		}
		return instance;
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

	public final void processNextRequest() {
		if (currentRequest == null || currentRequest.hasStopped()) {
			if (!requests.isEmpty()) {
				currentRequest = requests.removeFirst();
				new Thread(currentRequest).start();
			}
		}
	}

}
