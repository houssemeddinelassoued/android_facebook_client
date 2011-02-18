package fi.harism.facebook.net;

import java.util.ArrayDeque;

public final class RequestController {

	private static RequestController instance = null;
	private ArrayDeque<Request> requests = null;
	private Request currentRequest = null;

	RequestController() {
		requests = new ArrayDeque<Request>();
	}

	public static RequestController getRequestController() {
		if (instance == null) {
			instance = new RequestController();
		}
		return instance;
	}

	public void clear() {
		requests.clear();
		if (currentRequest != null) {
			currentRequest.stop();
			currentRequest = null;
		}
	}

	public void addRequest(Request request) {
		requests.addLast(request);
		processNextRequest();
	}

	public void processNextRequest() {
		if (currentRequest == null || currentRequest.hasStopped()) {
			if (!requests.isEmpty()) {
				currentRequest = requests.removeFirst();
				new Thread(currentRequest).start();
			}
		}
	}

}
