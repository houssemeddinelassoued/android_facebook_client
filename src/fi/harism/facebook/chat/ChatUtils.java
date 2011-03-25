package fi.harism.facebook.chat;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

import android.util.Base64;

public class ChatUtils {

	public static boolean find(XmlPullParser parser, String name, String nameEnd)
			throws Exception {
		return find(parser, name, nameEnd, null);
	}

	public static boolean find(XmlPullParser parser, String name,
			String nameEnd, ChatLogger logger) throws Exception {
		int tag = parser.getEventType();
		while (tag != XmlPullParser.END_TAG
				|| !parser.getName().equals(nameEnd)) {
			tag = parser.next();
			if (logger != null) {
				log(parser, logger);
			}
			if (tag == XmlPullParser.END_DOCUMENT) {
				throw new Exception("Unexpected end of document.");
			}
			if (tag == XmlPullParser.START_TAG && parser.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public static String getValue(XmlPullParser parser, String attribute)
			throws Exception {
		String value = optValue(parser, attribute);
		if (value == null) {
			throw new Exception("Attribute " + attribute + " not found.");
		}
		return value;
	}

	public static void log(XmlPullParser parser, ChatLogger logger)
			throws Exception {
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

	public static void skip(XmlPullParser parser, int tag, String name)
			throws Exception {
		skip(parser, tag, name, null);
	}

	public static void skip(XmlPullParser parser, int tag, String name,
			ChatLogger logger) throws Exception {
		int t = parser.getEventType();
		while (t != tag || !parser.getName().equals(name)) {
			t = parser.next();
			if (logger != null) {
				log(parser, logger);
			}
			if (t == XmlPullParser.END_DOCUMENT) {
				throw new Exception("Unexpected end of document.");
			}
		}
		parser.require(tag, null, name);
	}

}
