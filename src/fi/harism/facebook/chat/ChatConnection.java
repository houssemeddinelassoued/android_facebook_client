package fi.harism.facebook.chat;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import javax.net.SocketFactory;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class ChatConnection {

	private ChatObserver observer;
	private ChatThread thread;

	private Logger logger;

	private static final int STATE_READY = 1;
	private static final int STATE_THREAD = 2;
	private static final int STATE_CONNECTED = 3;
	private static final int STATE_RUNNING = 5;

	private int currentState = STATE_READY;

	private String jid = null;

	public ChatConnection(ChatObserver observer) {
		this.observer = observer;
		logger = new Logger();
	}

	public void connect(String sessionKey, String sessionSecret) {
		switch (currentState) {
		case STATE_READY:
			thread = new ChatThread(sessionKey, sessionSecret);
			thread.start();
			logger.println("New thread created.");
			break;
		case STATE_THREAD:
		case STATE_CONNECTED:
			logger.println("Thread running already.");
			break;
		default:
			logger.println("Thread running already.");
			observer.onChatConnected();
			break;
		}
	}

	public void disconnect() {
		switch (currentState) {
		case STATE_RUNNING:
			logger.println("Sending final presence.");
			thread.write("<presence from='" + jid + "' type='unavailable'/>");
		case STATE_CONNECTED:
			logger.println("Sending end stream.");
			thread.write("</stream:stream>");
			break;
		default:
			logger.println("Not connected.");
			observer.onChatDisconnected();
		}
	}

	public void sendMessage(String to, String message) {
		thread.write("<message to='" + to + "' from='" + jid
				+ "' type='chat'><body>" + message
				+ "</body></message>");
	}

	public String getLog() {
		return logger.toString();
	}

	private String createJID(XmlPullParser parser, Writer writer)
			throws Exception {
		String id = "bind_" + System.currentTimeMillis();
		writer.write("<iq type='set' id='" + id
				+ "'><bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'/></iq>");
		writer.flush();
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "iq");
		if (!ChatUtils.getValue(parser, "id").equals(id)) {
			throw new Exception("Unexpected <iq> id received.");
		}
		if (!ChatUtils.find(parser, "jid", "iq")) {
			throw new Exception("Bind failed.");
		}
		String jid = parser.nextText();
		ChatUtils.skip(parser, XmlPullParser.END_TAG, "iq");
		return jid;
	}

	private void createSession(XmlPullParser parser, Writer writer)
			throws Exception {
		String id = "session_" + System.currentTimeMillis();
		writer.write("<iq to='chat.facebook.com' type='set' id='"
				+ id
				+ "'><session xmlns='urn:ietf:params:xml:ns:xmpp-session'/></iq>");
		writer.flush();
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "iq");
		if (!ChatUtils.find(parser, "session", "iq")) {
			throw new Exception("Session creation failed.");
		}
		ChatUtils.skip(parser, XmlPullParser.END_TAG, "iq");
	}

	private boolean executeLogin(XmlPullParser parser, Reader reader,
			Writer writer, String sessionKey, String sessionSecret)
			throws Exception {

		parser.setInput(reader);
		writer.write("<stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' to='chat.facebook.com' version='1.0'>");
		writer.flush();

		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "stream:stream");

		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "stream:features");

		boolean mechanismFound = false;
		while (ChatUtils.find(parser, "mechanism", "stream:features")) {
			String mechanism = parser.nextText();
			if (mechanism.equals("X-FACEBOOK-PLATFORM")) {
				mechanismFound = true;
			}
		}
		parser.require(XmlPullParser.END_TAG, null, "stream:features");
		if (!mechanismFound) {
			throw new Exception("X-FACEBOOK-PLATFORM mechanism not supported.");
		}

		for (int i = 0; i < 10; ++i) {
			writer.write("<auth xmlns='urn:ietf:params:xml:ns:xmpp-sasl' mechanism='X-FACEBOOK-PLATFORM'/>");
			writer.flush();

			parser.nextTag();
			parser.require(XmlPullParser.START_TAG, null, "challenge");

			String challenge = parser.nextText();
			writer.write(ChatUtils.responseChallenge(challenge, sessionKey,
					sessionSecret));
			writer.flush();

			parser.nextTag();
			parser.require(XmlPullParser.START_TAG, null, null);

			if (parser.getName().equals("success")) {
				logger.println("Auth successful.");
				ChatUtils.skip(parser, XmlPullParser.END_TAG, "success");
				return true;
			}
			if (parser.getName().equals("failure")) {
				logger.println("Auth failed try=" + (i + 1) + ".");
				ChatUtils.skip(parser, XmlPullParser.END_TAG, "failure");
			}
		}

		writer.write("</stream:stream>");
		writer.flush();
		ChatUtils.skip(parser, XmlPullParser.END_TAG, "stream:stream");
		return false;
	}

	private void executeSession(XmlPullParser parser, Reader reader,
			Writer writer) throws Exception {
		parser.setInput(reader);
		writer.write("<stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' to='chat.facebook.com' version='1.0'>");
		writer.flush();

		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "stream:stream");

		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "stream:features");
		if (!ChatUtils.find(parser, "bind", "stream:features")) {
			throw new Exception("Bind not supported.");
		}
		if (!ChatUtils.find(parser, "session", "stream:features")) {
			throw new Exception("Session not supported");
		}
		ChatUtils.skip(parser, XmlPullParser.END_TAG, "stream:features");

		jid = createJID(parser, writer);
		createSession(parser, writer);

		writer.write("<presence/>");
		writer.flush();

		currentState = STATE_RUNNING;
		logger.println("Entering main loop.");
		observer.onChatConnected();

		parser.nextTag();
		while (parser.getEventType() != XmlPullParser.END_TAG) {
			String name = parser.getName();
			if (name.equals("presence")) {
				processPresence(parser);
			} else if (name.equals("message")) {
				processMessage(parser);
			}
			ChatUtils.skip(parser, XmlPullParser.END_TAG, name, logger);
			parser.nextTag();
		}
		parser.require(XmlPullParser.END_TAG, null, "stream:stream");
	}
	
	private void processMessage(XmlPullParser parser) throws Exception {
		parser.require(XmlPullParser.START_TAG, null, "message");
		String from = ChatUtils.getValue(parser, "from");
		String to = ChatUtils.getValue(parser, "to");
		if (to.equals(jid)) {
			if (ChatUtils.find(parser, "body", "message", logger)) {
				String body = parser.nextText();
				observer.onChatMessage(from, body);
			}
		}
		ChatUtils.skip(parser, XmlPullParser.END_TAG, "message", logger);
	}

	private void processPresence(XmlPullParser parser) throws Exception {
		parser.require(XmlPullParser.START_TAG, null, "presence");
		String from = ChatUtils.getValue(parser, "from");
		String to = ChatUtils.getValue(parser, "to");
		if (to.equals(jid)) {
			String type = ChatUtils.optValue(parser, "type");
			if (type == null) {
				String show = "chat";
				if (ChatUtils.find(parser, "show", "presence")) {
					show = parser.nextText();
				}
				logger.println("Presence " + from + " " + show + ".");
				observer.onChatPresenceChanged(from, show);
			} else if (type.equals("unavailable")) {
				logger.println("Presence " + from + " " + "gone.");
				observer.onChatPresenceChanged(from, "gone");
			}
		}
		ChatUtils.skip(parser, XmlPullParser.END_TAG, "presence");
	}

	private class ChatThread extends Thread {

		private String sessionKey;
		private String sessionSecret;
		private Socket socket = null;

		public ChatThread(String sessionKey, String sessionSecret) {
			this.sessionKey = sessionKey;
			this.sessionSecret = sessionSecret;
		}

		@Override
		public void run() {

			logger.println("Thread start.");

			switch (currentState) {
			case STATE_CONNECTED:
			case STATE_RUNNING:
				logger.println("Unexpected state.");
				return;
			}

			try {
				SocketFactory sf = SocketFactory.getDefault();
				socket = sf.createSocket("chat.facebook.com", 5222);

				currentState = STATE_CONNECTED;

				Reader reader = new InputStreamReader(socket.getInputStream());
				Writer writer = new OutputStreamWriter(socket.getOutputStream());

				XmlPullParser parser = XmlPullParserFactory.newInstance()
						.newPullParser();

				logger.println("Socket connected.");

				if (executeLogin(parser, reader, writer, sessionKey,
						sessionSecret)) {
					executeSession(parser, reader, writer);
				}

				socket.close();
				logger.println("Disconnected.");
				observer.onChatDisconnected();

			} catch (Exception ex) {
				if (socket != null) {
					try {
						socket.close();
					} catch (Exception exx) {
					}
				}
				logger.println(ex.toString());
				observer.onChatError(ex);
			}

			logger.println("Thread end.");
			currentState = STATE_READY;
		}

		public void write(String content) {
			try {
				Writer writer = new OutputStreamWriter(socket.getOutputStream());
				writer.write(content);
				writer.flush();
			} catch (Exception ex) {
			}
		}
	}

	private class Logger implements ChatLogger {

		private StringBuffer log = new StringBuffer();

		public String toString() {
			return log.toString();
		}

		@Override
		public void print(String text) {
			log.append(text);
		}

		@Override
		public void println(String text) {
			print(text + "\n");
		}

	}

}
