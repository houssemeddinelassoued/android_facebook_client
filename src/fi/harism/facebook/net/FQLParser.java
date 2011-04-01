package fi.harism.facebook.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.json.JSONException;
import org.json.JSONStringer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Facebook FQL query parser. Utility methods for converting response xml into
 * JSONObject.
 * 
 * Examples: https://api.facebook.com/method/fql.query?query= SELECT
 * uid,name,pic_square FROM user WHERE uid=1111111 SELECT uuid FROM user WHERE
 * uid=111111
 * 
 * @author harism
 */
public class FQLParser {

	/**
	 * Checks if current tag is a list.
	 * 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static boolean isList(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, null, null);
		for (int i = 0; i < parser.getAttributeCount(); ++i) {
			if (parser.getAttributeName(i).equals("list")) {
				return parser.getAttributeValue(i).equals("true");
			}
		}
		return false;
	}

	/**
	 * Parses xml from given InputStream and converts it to JSON presentation.
	 * 
	 * @param is
	 *            Xml InputStream.
	 * @return JSON String presentation for xml.
	 * @throws IOException
	 * @throws JSONException
	 * @throws XmlPullParserException
	 */
	public static final String parse(InputStream is) throws IOException,
			JSONException, XmlPullParserException {

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
		if (isList(parser)) {
			parseArray(stringer, parser);
		} else {
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
	 * @throws IOException
	 * @throws JSONException
	 * @throws XmlPullParserException
	 */
	public static void parseArray(JSONStringer stringer, XmlPullParser parser)
			throws IOException, JSONException, XmlPullParserException {
		String name = parser.getName();
		parser.require(XmlPullParser.START_TAG, null, name);

		stringer.array();
		parser.nextTag();
		while (parser.getEventType() != XmlPullParser.END_TAG) {
			if (isList(parser)) {
				parseArray(stringer, parser);
			} else {
				parseObject(stringer, parser);
			}
			parser.nextTag();
		}
		stringer.endArray();
		parser.require(XmlPullParser.END_TAG, null, name);
	}

	/**
	 * Parses object content.
	 * 
	 * @param stringer
	 * @param parser
	 * @throws IOException
	 * @throws JSONException
	 * @throws XmlPullParserException
	 */
	public static void parseContent(JSONStringer stringer, XmlPullParser parser)
			throws IOException, JSONException, XmlPullParserException {
		String name = parser.getName();
		parser.require(XmlPullParser.START_TAG, null, name);
		stringer.key(name);

		if (isList(parser)) {
			parseArray(stringer, parser);
		} else {
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
	 * Parses object element (==> there is no list="true" attribute).
	 * 
	 * @param stringer
	 * @param parser
	 * @throws IOException
	 * @throws JSONException
	 * @throws XmlPullParserException
	 */
	public static void parseObject(JSONStringer stringer, XmlPullParser parser)
			throws IOException, JSONException, XmlPullParserException {
		String name = parser.getName();
		parser.require(XmlPullParser.START_TAG, null, name);

		stringer.object();
		parser.nextTag();
		while (parser.getEventType() != XmlPullParser.END_TAG) {
			parseContent(stringer, parser);
			parser.nextTag();
		}
		stringer.endObject();

		parser.require(XmlPullParser.END_TAG, null, name);
	}

}
