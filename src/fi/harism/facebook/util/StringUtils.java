package fi.harism.facebook.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {
	
	public static final String convertFBTime(String fbTime) {
		String ret;
		try {
			SimpleDateFormat fbFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			Date eventTime = fbFormat.parse(fbTime);
			Date curTime = new Date();
			
			long diffMillis = curTime.getTime() - eventTime.getTime();
			long diffSeconds = diffMillis / 1000;
			long diffMinutes = diffMillis / 1000 / 60;
			long diffHours = diffMillis / 1000 / 60 / 60;
			if (diffSeconds < 60) {
				ret = diffSeconds + " seconds ago";
			}
			else if (diffMinutes < 60) {
				ret = diffMinutes + " minutes ago";
			}
			else if (diffHours < 24) {
				ret = diffHours + " hours ago";
			}
			else {
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

}
