package fi.harism.facebook.net;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.json.JSONStringer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Facebook FQL query parser. Utility methods for converting response xml into
 * JSONObject.
 * 
 * Examples:
 * https://api.facebook.com/method/fql.query?query=
 * SELECT uid,name,pic_square FROM user WHERE uid=1111111
 * SELECT uuid FROM user WHERE uid=111111
 * 
 * @author harism
 */
public class FQLParser {

	/**
	 * Parses xml from given InputStream and converts it to JSON presentation.
	 * 
	 * @param is
	 *            Xml InputStream.
	 * @return JSON String presentation for xml.
	 * @throws Exception
	 */
	public static final String parse(InputStream is) throws Exception {

		Reader reader = new InputStreamReader(is);

		XmlPullParser parser = XmlPullParserFactory.newInstance()
				.newPullParser();
		parser.setInput(reader);

		parser.nextTag();

		JSONStringer stringer = new JSONStringer();
		stringer.object();

		// We are expecting "fql_query_response" for successful query.
		String name = parser.getName();
		if (name.equals("fql_query_response")) {
			name = "data";
		} else {
			name = "error";
		}

		stringer.key(name);
		if (!parseArray(stringer, parser)) {
			parseObject(stringer, parser);
		}

		stringer.endObject();
		return stringer.toString();
	}

	/**
	 * Parses an array element (==> element has type="list" attribute).
	 * 
	 * @param stringer
	 * @param parser
	 * @return
	 * @throws Exception
	 */
	public static boolean parseArray(JSONStringer stringer, XmlPullParser parser)
			throws Exception {
		parser.require(XmlPullParser.START_TAG, null, null);

		if (optValue(parser, "list").equals("true")) {
			String name = parser.getName();

			stringer.array();
			parser.nextTag();
			while (parser.getEventType() != XmlPullParser.END_TAG) {
				if (!parseArray(stringer, parser)) {
					parseObject(stringer, parser);
				}
				parser.nextTag();
			}
			stringer.endArray();

			parser.require(XmlPullParser.END_TAG, null, name);
			return true;
		}
		return false;
	}

	/**
	 * Parses object element (==> there is no type attribute).
	 * 
	 * @param stringer
	 * @param parser
	 * @return
	 * @throws Exception
	 */
	public static boolean parseObject(JSONStringer stringer,
			XmlPullParser parser) throws Exception {
		parser.require(XmlPullParser.START_TAG, null, null);

		if (!optValue(parser, "list").equals("true")) {
			String name = parser.getName();

			stringer.object();
			parser.nextTag();

			while (parser.getEventType() != XmlPullParser.END_TAG) {
				parseContent(stringer, parser);
				parser.nextTag();
			}

			stringer.endObject();

			parser.require(XmlPullParser.END_TAG, null, name);
			return true;
		}

		return false;
	}

	/**
	 * Parses object content.
	 * 
	 * @param stringer
	 * @param parser
	 * @throws Exception
	 */
	public static void parseContent(JSONStringer stringer, XmlPullParser parser)
			throws Exception {
		parser.require(XmlPullParser.START_TAG, null, null);

		String name = parser.getName();
		stringer.key(name);

		if (!parseArray(stringer, parser)) {
			parser.next();
			String text = null;
			if (parser.getEventType() == XmlPullParser.TEXT) {
				text = parser.getText();
				parser.nextTag();
			}
			if (parser.getEventType() == XmlPullParser.END_TAG) {
				stringer.value(text);
			} else {
				stringer.object();
				while (parser.getEventType() != XmlPullParser.END_TAG) {
					parseContent(stringer, parser);
					parser.nextTag();
				}
				stringer.endObject();
			}

		}

		parser.require(XmlPullParser.END_TAG, null, name);
	}

	/**
	 * Searches for given attribute value from current tag.
	 * 
	 * @param parser
	 * @param attribute
	 * @return
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
		return "";
	}

}
