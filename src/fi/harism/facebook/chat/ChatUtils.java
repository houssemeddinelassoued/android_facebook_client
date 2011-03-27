package fi.harism.facebook.chat;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

import android.util.Base64;

/**
 * Some Facebook Chat related utility methods.
 * 
 * @author harism
 */
public class ChatUtils {

	/**
	 * XmlPullParser helper method for searching certain tag until end condition
	 * is reached. Throws an exception if there is error while reading xml or
	 * end of document is reached unexpectedly.<br>
	 * <br>
	 * E.g. for xml:<br>
	 * &lt;list&gt;<br>
	 * &nbsp;&nbsp;&lt;item&gt;<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;name/&gt;<br>
	 * &nbsp;&nbsp;&lt;/item&gt;<br>
	 * &lt;list&gt;<br>
	 * <br>
	 * find(parser, "name", "list", null)<br>
	 * - leaves parser at start tag "name" and returns true.<br>
	 * find(parser, "picture", "list", null)<br>
	 * - leaves parser at end tag "list" and returns false.<br>
	 * 
	 * @param parser
	 *            XmlPullParser
	 * @param name
	 *            Name of tag we are looking for.
	 * @param nameEnd
	 *            Name of end tag we end search at.
	 * @param logger
	 *            ChatLogger instance, may be null.
	 * @return True if 'name' tag was found before 'nameEnd' tag.
	 * @throws Exception
	 */
	public static boolean find(XmlPullParser parser, String name,
			String nameEnd, ChatLogger logger) throws Exception {
		int tag = parser.getEventType();
		// Iterate until end condition is reached.
		while (tag != XmlPullParser.END_TAG
				|| !parser.getName().equals(nameEnd)) {
			tag = parser.next();
			log(parser, logger);
			// Unexpected end of document.
			if (tag == XmlPullParser.END_DOCUMENT) {
				throw new Exception("Unexpected end of document.");
			}
			// Tag we were looking for has been found.
			if (tag == XmlPullParser.START_TAG && parser.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Searches for given attribute within current START_TAG. Throws an
	 * exception if attribute was not found, value of the attribute otherwise.<br>
	 * <br>
	 * E.g. for xml:<br>
	 * &lt;item type="name"/&gt;<br>
	 * <br>
	 * getValue(parser, "type")<br>
	 * - returns "name"<br>
	 * getValue(parser, "picture")<br>
	 * - throws an exception.<br>
	 * <br>
	 * @see optValue(XmlPullParser, String)
	 * 
	 * @param parser
	 *            XmlPullParser - must be in START_TAG state.
	 * @param attribute
	 *            Name of attribute we are looking for.
	 * @return Value of attribute.
	 * @throws Exception
	 */
	public static String getValue(XmlPullParser parser, String attribute)
			throws Exception {
		String value = optValue(parser, attribute);
		if (value == null) {
			throw new Exception("Attribute " + attribute + " not found.");
		}
		return value;
	}

	/**
	 * Writes current state of parser into logger.
	 * 
	 * @param parser
	 *            XmlPullParser
	 * @param logger
	 *            Logger instance, may be null.
	 * @throws Exception
	 */
	public static void log(XmlPullParser parser, ChatLogger logger)
			throws Exception {

		if (logger == null) {
			return;
		}

		int tag = parser.getEventType();
		switch (tag) {
		case XmlPullParser.START_DOCUMENT:
			logger.println("Document Start.");
			break;
		case XmlPullParser.END_DOCUMENT:
			logger.println("Document End.");
			break;
		case XmlPullParser.START_TAG:
			for (int i = 0; i < parser.getDepth(); ++i) {
				logger.print(" ");
			}
			logger.print("<" + parser.getName());
			for (int i = 0; i < parser.getAttributeCount(); ++i) {
				logger.print(" " + parser.getAttributeName(i));
				logger.print("=\"" + parser.getAttributeValue(i));
				logger.print("\"");
			}
			logger.println(">");
			break;
		case XmlPullParser.END_TAG:
			for (int i = 0; i < parser.getDepth(); ++i) {
				logger.print(" ");
			}
			logger.println("</" + parser.getName() + ">");
			break;
		case XmlPullParser.TEXT:
			logger.println(parser.getText());
			break;
		default:
			logger.println("log: unknown tag.");
			break;
		}
	}

	/**
	 * Method for retrieving certain attribute value from START_TAG.
	 * XmlPullParser must be in START_TAG state, otherwise exception is thrown.
	 * If attribute is found, returns its value, null otherwise.<br>
	 * <br>
	 * E.g. for xml:<br>
	 * &lt;item type="name"/&gt;<br>
	 * <br>
	 * optValue(parser, "type")<br>
	 * - returns "name".<br>
	 * optValue(parser, "picture")<br>
	 * - returns null.<br>
	 * 
	 * @param parser
	 *            XmlPullParser instance, must be in START_TAG state.
	 * @param attribute
	 *            Name of attribute.
	 * @return Value of attribute or null if attribute was not found.
	 * @throws Exception
	 */
	public static String optValue(XmlPullParser parser, String attribute)
			throws Exception {
		parser.require(XmlPullParser.START_TAG, null, null);
		for (int i = 0; i < parser.getAttributeCount(); ++i) {
			if (parser.getAttributeName(i).equals(attribute)) {
				return parser.getAttributeValue(i);
			}
		}
		return null;
	}

	/**
	 * Method for generating challenge response.<br>
	 * <br>
	 * TODO: Move this somewhere else.<br>
	 * 
	 * @param challenge
	 * @param sessionKey
	 * @param sessionSecret
	 * @return
	 * @throws Exception
	 */
	public static String responseChallenge(String challenge, String sessionKey,
			String sessionSecret) throws Exception {

		challenge = new String(Base64.decode(challenge, Base64.DEFAULT));

		String params[] = challenge.split("&");
		HashMap<String, String> paramMap = new HashMap<String, String>();
		for (int i = 0; i < params.length; ++i) {
			String p[] = params[i].split("=");
			p[0] = URLDecoder.decode(p[0]);
			p[1] = URLDecoder.decode(p[1]);
			paramMap.put(p[0], p[1]);
		}

		String api_key = "297611c6411cacc8549dfc403a1b2492";
		String call_id = "" + System.currentTimeMillis();
		String method = paramMap.get("method");
		String nonce = paramMap.get("nonce");
		String v = "1.0";

		StringBuffer sigBuffer = new StringBuffer();
		sigBuffer.append("api_key=" + api_key);
		sigBuffer.append("call_id=" + call_id);
		sigBuffer.append("method=" + method);
		sigBuffer.append("nonce=" + nonce);
		sigBuffer.append("session_key=" + sessionKey);
		sigBuffer.append("v=" + v);
		sigBuffer.append(sessionSecret);

		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(sigBuffer.toString().getBytes());
		byte[] digest = md.digest();

		StringBuffer sig = new StringBuffer();
		for (int i = 0; i < digest.length; ++i) {
			sig.append(Integer.toHexString(0xFF & digest[i]));
		}

		StringBuffer response = new StringBuffer();
		response.append("api_key=" + URLEncoder.encode(api_key));
		response.append("&call_id=" + URLEncoder.encode(call_id));
		response.append("&method=" + URLEncoder.encode(method));
		response.append("&nonce=" + URLEncoder.encode(nonce));
		response.append("&session_key=" + URLEncoder.encode(sessionKey));
		response.append("&v=" + URLEncoder.encode(v));
		response.append("&sig=" + URLEncoder.encode(sig.toString()));

		StringBuffer buf = new StringBuffer();
		buf.append("<response xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>");
		buf.append(Base64.encodeToString(response.toString().getBytes(),
				Base64.NO_WRAP));
		buf.append("</response>");

		return buf.toString();
	}

	/**
	 * Skips tags until end condition is reached, throws exception if there is
	 * an error with parser or unexpected end of document is reached.<br>
	 * <br>
	 * TODO: Make this more generic eventually, now this works only with
	 * START_TAG and END_TAG.<br>
	 * 
	 * @param parser
	 *            XmlPullParser instance.
	 * @param tag
	 *            XmlPullParser tag type.
	 * @param name
	 *            Name of tag.
	 * @param logger
	 *            Logger instance, may be null.
	 * @throws Exception
	 */
	public static void skip(XmlPullParser parser, int tag, String name,
			ChatLogger logger) throws Exception {
		int t = parser.getEventType();
		while (t != tag || !parser.getName().equals(name)) {
			t = parser.next();
			log(parser, logger);
			if (t == XmlPullParser.END_DOCUMENT) {
				throw new Exception("Unexpected end of document.");
			}
		}
		parser.require(tag, null, name);
	}

}
