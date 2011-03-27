package fi.harism.facebook.chat;

/**
 * Logger class for debugging reasons.<br>
 * <br>
 * TODO: It might be a good idea to have different logging levels.
 * 
 * @author harism
 */
public class ChatLogger {

	private StringBuilder log;
	
	public ChatLogger() {
		log = new StringBuilder();
	}

	public void print(String text) {
		log.append(text);
	}

	public void println(String text) {
		log.append(text + "\n");
	}

	@Override
	public String toString() {
		return log.toString();
	}

}
