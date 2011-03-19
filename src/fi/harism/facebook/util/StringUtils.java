package fi.harism.facebook.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.widget.TextView;

/**
 * Various String/Date etc related utility methods.
 * 
 * @author harism
 */
public class StringUtils {

	/**
	 * Converts Facebook time to more readable format. E.g "5 minutes ago",
	 * "1 hour ago" or "February 20, 2010 at 14:35".
	 * 
	 * @param fbTime
	 *            String presentation of Facebook time.
	 * @return Converted String.
	 */
	public static final String convertFBTime(String fbTime) {
		String ret;
		try {
			SimpleDateFormat fbFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ssZ");
			Date eventTime = fbFormat.parse(fbTime);
			Date curTime = new Date();

			long diffMillis = curTime.getTime() - eventTime.getTime();
			long diffSeconds = diffMillis / 1000;
			long diffMinutes = diffMillis / 1000 / 60;
			long diffHours = diffMillis / 1000 / 60 / 60;
			if (diffSeconds < 60) {
				ret = diffSeconds + " seconds ago";
			} else if (diffMinutes < 60) {
				ret = diffMinutes + " minutes ago";
			} else if (diffHours < 24) {
				ret = diffHours + " hours ago";
			} else {
				String dateFormat = "MMMMM d";
				if (eventTime.getYear() < curTime.getYear()) {
					dateFormat += ", yyyy";
				}
				dateFormat += "' at 'kk:mm";

				SimpleDateFormat calFormat = new SimpleDateFormat(dateFormat);
				ret = calFormat.format(eventTime);
			}
		} catch (Exception ex) {
			ret = "error: " + ex.toString();
		}
		return ret;
	}

	/**
	 * Sets given text as as link to TextView.
	 * 
	 * @param tv
	 *            TextView to set text/link to.
	 * @param text
	 *            Visible text.
	 * @param url
	 *            Underlying URL.
	 * @param observer
	 *            Observer for onClick callback, can be null.
	 */
	public static final void setTextLink(TextView tv, String text, String url,
			FacebookURLSpan.ClickObserver observer) {
		// First create our url span.
		FacebookURLSpan urlSpan = new FacebookURLSpan(url);
		urlSpan.setObserver(observer);
		// Create SpannableString.
		SpannableString textString = new SpannableString(text);
		// Set whole text as a link.
		textString.setSpan(urlSpan, 0, textString.length(), 0);
		tv.setText(textString);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	/**
	 * Searches given text for links and sets them as clickable to TextView.
	 * 
	 * @param tv
	 *            TextView to set text/links to.
	 * @param text
	 *            Visible text.
	 * @param observer
	 *            Observer for onClick callback, can be null.
	 */
	public static final void setTextLinks(TextView tv, String text,
			FacebookURLSpan.ClickObserver observer) {
		// Use Linkify for searching URLs, phone numbers and emails.
		SpannableString tempString = new SpannableString(text);
		Linkify.addLinks(tempString, Linkify.ALL);
		// Get list of found spans.
		URLSpan[] spans = tempString.getSpans(0, tempString.length(),
				URLSpan.class);
		// If we found some.
		if (spans.length > 0) {
			SpannableString textString = new SpannableString(text);
			for (int i = 0; i < spans.length; ++i) {
				URLSpan span = spans[i];
				FacebookURLSpan urlSpan = new FacebookURLSpan(span.getURL());
				urlSpan.setObserver(observer);
				textString.setSpan(urlSpan, tempString.getSpanStart(span),
						tempString.getSpanEnd(span), 0);
			}
			tv.setText(textString);
			tv.setMovementMethod(LinkMovementMethod.getInstance());
		} else {
			tv.setText(text);
		}
	}

}
