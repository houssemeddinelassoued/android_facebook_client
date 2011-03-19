package fi.harism.facebook.util;

import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;

/**
 * Our own URLSpan implementation for disabling underlining and handling onClick
 * events.
 * 
 * @author harism
 */
public class FacebookURLSpan extends URLSpan {

	private ClickObserver observer = null;

	public FacebookURLSpan(String url) {
		super(url);
	}

	@Override
	public void onClick(View view) {
		if (observer == null || !observer.onClick(this)) {
			super.onClick(view);
		}
	}

	public void setObserver(ClickObserver observer) {
		this.observer = observer;
	}

	@Override
	public void updateDrawState(TextPaint tp) {
		tp.setColor(tp.linkColor);
	}

	/**
	 * Click observer interface.
	 */
	public interface ClickObserver {
		/**
		 * Do your onClick handling here. If you return false URLSpan default
		 * onClick method will be used instead.
		 * 
		 * @param span
		 *            Span that was clicked.
		 * @return True if you handled onClick event, false otherwise.
		 */
		public boolean onClick(FacebookURLSpan span);
	}

}
