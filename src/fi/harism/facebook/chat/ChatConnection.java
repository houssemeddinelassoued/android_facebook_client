package fi.harism.facebook.chat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

import javax.net.SocketFactory;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Class for actual chat connection. Once connect method has been called it
 * opens a socket connection to chat server, starts login procedure etc., and
 * reports its state and events via observer.<br>
 * <br>
 * TODO: Facebook Chat supports chat state notifications.<br>
 * http://xmpp.org/extensions/xep-0085.html<br>
 * TODO: Facebook Chat sends confirmation once message has been delivered.<br>
 * 
 * @author harism
 */
public class ChatConnection {

	private ChatObserver.Connection observer;
	private ChatThread thread;

	private ChatLogger logger;

	private static final int STATE_READY = 1;
	private static final int STATE_THREAD = 2;
	private static final int STATE_CONNECTED = 3;
	private static final int STATE_RUNNING = 5;

	private static final String CHAT_ADDRESS = "chat.facebook.com";
	private static final int CHAT_PORT = 5222;

	private int currentState = STATE_READY;

	private String jid = null;

	/**
	 * Default constructor.
	 * 
	 * @param observer
	 *            Observer for chat connection events.
	 * @param logger
	 *            For debug reasons.
	 */
	public ChatConnection(ChatObserver.Connection observer, ChatLogger logger) {
		this.observer = observer;
		this.logger = logger;
	}

	/**
	 * Starts connection procedure if there isn't open connection already.
	 * 
	 * @param sessionKey
	 *            Session key for logged in user.
	 * @param sessionSecret
	 *            Session secret for logged in user.
	 */
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
			observer.onConnected();
			break;
		}
	}

	/**
	 * Disconnects from chat server.
	 */
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
			observer.onDisconnected();
		}
	}

	/**
	 * Sends message to server.
	 * 
	 * @param to
	 *            JID of receiving entity.
	 * @param message
	 *            Message to be sent.
	 */
	public void sendMessage(String to, String message) {
		thread.write("<message to='" + to + "' from='" + jid
				+ "' type='chat'><body>" + message + "</body></message>");
	}

	/**
	 * Sends bind request to server and returns received JID.<br>
	 * <br>
	 * http://xmpp.org/rfcs/rfc3920.html#bind<br>
	 * 
	 * @param parser
	 *            XmlPullParser instance.
	 * @param writer
	 *            Writer to send bind request to.
	 * @return JID received from server.
	 * @throws Exception
	 */
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
		if (!ChatUtils.find(parser, "jid", "iq", null)) {
			throw new Exception("Bind failed.");
		}
		String jid = parser.nextText();
		ChatUtils.skip(parser, XmlPullParser.END_TAG, "iq", null);
		return jid;
	}

	/**
	 * Creates session to server.<br>
	 * <br>
	 * http://xmpp.org/rfcs/rfc3921.html#session<br>
	 * 
	 * @param parser
	 *            XmlPullParser instance.
	 * @param writer
	 *            Writer to send session request to.
	 * @throws Exception
	 */
	private void createSession(XmlPullParser parser, Writer writer)
			throws Exception {
		String id = "session_" + System.currentTimeMillis();
		writer.write("<iq to='chat.facebook.com' type='set' id='"
				+ id
				+ "'><session xmlns='urn:ietf:params:xml:ns:xmpp-session'/></iq>");
		writer.flush();
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "iq");
		if (!ChatUtils.find(parser, "session", "iq", null)) {
			throw new Exception("Session creation failed.");
		}
		ChatUtils.skip(parser, XmlPullParser.END_TAG, "iq", null);
	}

	/**
	 * Executes login procedure to server.
	 * <ol>
	 * <li>Open a stream to server.</li>
	 * <li>Verify "X-FACEBOOK-PLATFORM" authorization mechanism is supported.</li>
	 * <li>If it is, try to authorize user for a few times, and return false if
	 * it's not successful. For some reason it usually takes a few tries to
	 * accomplish authorization.</li>
	 * </ol>
	 * 
	 * http://xmpp.org/rfcs/rfc3920.html#streams<br>
	 * http://xmpp.org/rfcs/rfc3920.html#sasl<br>
	 * <br>
	 * TODO: Handle stream errors more properly.<br>
	 * TODO: Implement tls support.<br>
	 * 
	 * @param parser
	 *            XmlPullParser instance.
	 * @param reader
	 *            Reader connected to server.
	 * @param writer
	 *            Writer connected to server.
	 * @param sessionKey
	 *            Session key for current user.
	 * @param sessionSecret
	 *            Secret key for current user.
	 * @return True if login successful, false otherwise.
	 * @throws Exception
	 */
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
		while (ChatUtils.find(parser, "mechanism", "stream:features", null)) {
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
				ChatUtils.skip(parser, XmlPullParser.END_TAG, "success", null);
				return true;
			}
			if (parser.getName().equals("failure")) {
				logger.println("Auth failed try=" + (i + 1) + ".");
				ChatUtils.skip(parser, XmlPullParser.END_TAG, "failure", null);
			}
		}

		writer.write("</stream:stream>");
		writer.flush();
		ChatUtils.skip(parser, XmlPullParser.END_TAG, "stream:stream", null);
		return false;
	}

	/**
	 * Executes session creation and starts a session loop for handling incoming
	 * events - or stream end eventually.
	 * <ol>
	 * <li>Open a new stream to server.</li>
	 * <li>Verify that bind and session features are supported.</li>
	 * <li>Execute bind and session creation.</li>
	 * <li>Send initial presence - this triggers server to reply with presences
	 * of online friends eventually (Btw. we don't care about roster).</li>
	 * <li>Start executing session main loop.</li>
	 * </ol>
	 * 
	 * http://xmpp.org/rfcs/rfc3921.html#presence<br>
	 * http://xmpp.org/rfcs/rfc3921.html#messaging<br>
	 * 
	 * @param parser
	 *            XmlPullParser
	 * @param reader
	 *            Reader connected to the server.
	 * @param writer
	 *            Writer connected to the server.
	 * @throws Exception
	 */
	private void executeSession(XmlPullParser parser, Reader reader,
			Writer writer) throws Exception {
		parser.setInput(reader);
		writer.write("<stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' to='chat.facebook.com' version='1.0'>");
		writer.flush();

		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "stream:stream");

		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "stream:features");
		if (!ChatUtils.find(parser, "bind", "stream:features", null)) {
			throw new Exception("Bind not supported.");
		}
		if (!ChatUtils.find(parser, "session", "stream:features", null)) {
			throw new Exception("Session not supported");
		}
		ChatUtils.skip(parser, XmlPullParser.END_TAG, "stream:features", null);

		jid = createJID(parser, writer);
		createSession(parser, writer);

		writer.write("<presence/>");
		writer.flush();

		currentState = STATE_RUNNING;
		logger.println("Entering main loop.");
		observer.onConnected();

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

	/**
	 * Method for parsing &lt;message&gt; tag. Parser is expected to be at start
	 * of message tag - otherwise an exception is thrown. If this condition
	 * applies, method sends message event to observer and leaves parser at end
	 * of message tag.
	 * 
	 * @param parser
	 *            XmlPullParser instance.
	 * @throws Exception
	 */
	private void processMessage(XmlPullParser parser) throws Exception {
		parser.require(XmlPullParser.START_TAG, null, "message");
		String from = ChatUtils.getValue(parser, "from");
		String to = ChatUtils.getValue(parser, "to");
		if (to.equals(jid)) {
			if (ChatUtils.find(parser, "body", "message", logger)) {
				String body = parser.nextText();
				observer.onMessage(from, body);
			}
		}
		ChatUtils.skip(parser, XmlPullParser.END_TAG, "message", logger);
	}

	/**
	 * Processes &lt;presence&gt; tag. Parser is expected to be at start of
	 * presence tag - otherwise an exception is thrown. If this condition holds,
	 * method notifies observer about changed presence and leaves parser at the
	 * end of presence tag.
	 * 
	 * @param parser
	 *            XmlPullParser instance.
	 * @throws Exception
	 */
	private void processPresence(XmlPullParser parser) throws Exception {
		parser.require(XmlPullParser.START_TAG, null, "presence");
		String from = ChatUtils.getValue(parser, "from");
		String to = ChatUtils.getValue(parser, "to");
		if (to.equals(jid)) {
			String type = ChatUtils.optValue(parser, "type");
			if (type == null) {
				String show = "chat";
				if (ChatUtils.find(parser, "show", "presence", null)) {
					show = parser.nextText();
				}
				logger.println("Presence " + from + " " + show + ".");
				observer.onPresenceChanged(from, show);
			} else if (type.equals("unavailable")) {
				logger.println("Presence " + from + " " + "gone.");
				observer.onPresenceChanged(from, "gone");
			}
		}
		ChatUtils.skip(parser, XmlPullParser.END_TAG, "presence", null);
	}

	/**
	 * Private worker thread class.
	 * 
	 * @author harism
	 */
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
				socket = sf.createSocket(CHAT_ADDRESS, CHAT_PORT);

				currentState = STATE_CONNECTED;

				BufferedInputStream is = new BufferedInputStream(
						socket.getInputStream());
				BufferedOutputStream os = new BufferedOutputStream(
						socket.getOutputStream());

				Reader reader = new InputStreamReader(is);
				Writer writer = new OutputStreamWriter(os);

				XmlPullParser parser = XmlPullParserFactory.newInstance()
						.newPullParser();

				logger.println("Socket connected.");

				if (executeLogin(parser, reader, writer, sessionKey,
						sessionSecret)) {
					executeSession(parser, reader, writer);
				}

				socket.close();
				logger.println("Disconnected.");
				observer.onDisconnected();

			} catch (Exception ex) {
				if (socket != null) {
					try {
						socket.close();
					} catch (Exception exx) {
					}
				}
				logger.println(ex.toString());
				observer.onError(ex);
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

}
