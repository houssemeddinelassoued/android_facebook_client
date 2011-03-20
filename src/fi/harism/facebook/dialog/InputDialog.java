package fi.harism.facebook.dialog;

import fi.harism.facebook.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class InputDialog extends Dialog {
	
	private InputObserver observer;

	public InputDialog(Context context, InputObserver observer) {
		super(context);
		this.observer = observer;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_input);
		
		View sendView = findViewById(R.id.dialog_input_send);
		sendView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				sendClicked();
			}
		});
	}
	
	private void sendClicked() {
		EditText edit = (EditText) findViewById(R.id.dialog_input_edit);
		observer.onComplete(edit.getText().toString());
		dismiss();
	}
	
	public interface InputObserver {
		public void onComplete(String text);
	}

}
