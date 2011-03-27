package fi.harism.facebook.chat;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static boolean find(XmlPullParser parser, String name,
			String nameEnd, ChatLogger logger) throws IOException,
			XmlPullParserException {
		int tag = parser.getEventType();
		// Iterate until end condition is reached.
		while (tag != XmlPullParser.END_TAG
				|| !parser.getName().equals(nameEnd)) {
			tag = parser.next();
			log(parser, logger);
			// Unexpected end of document.
			if (tag == XmlPullParser.END_DOCUMENT) {
				throw new XmlPullParserException("Unexpected end of document.",
						parser, null);
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
	 * 
	 * @see optValue(XmlPullParser, String)
	 * 
	 * @param parser
	 *            XmlPullParser - must be in START_TAG state.
	 * @param attribute
	 *            Name of attribute we are looking for.
	 * @return Value of attribute.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static String getValue(XmlPullParser parser, String attribute)
			throws IOException, XmlPullParserException {
		String value = optValue(parser, attribute);
		if (value == null) {
			throw new XmlPullParserException("Attribute " + attribute
					+ " not found.", parser, null);
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
	 * @throws XmlPullParserException
	 */
	public static void log(XmlPullParser parser, ChatLogger logger)
			throws XmlPullParserException {

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
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static String optValue(XmlPullParser parser, String attribute)
			throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, null, null);
		for (int i = 0; i < parser.getAttributeCount(); ++i) {
			if (parser.getAttributeName(i).equals(attribute)) {
				return parser.getAttributeValue(i);
			}
		}
		return null;
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
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static void skip(XmlPullParser parser, int tag, String name,
			ChatLogger logger) throws IOException, XmlPullParserException {
		int t = parser.getEventType();
		while (t != tag || !parser.getName().equals(name)) {
			t = parser.next();
			log(parser, logger);
			if (t == XmlPullParser.END_DOCUMENT) {
				throw new XmlPullParserException("Unexpected end of document.",
						parser, null);
			}
		}
		parser.require(tag, null, name);
	}

}
