package fi.harism.facebook.dialog;

import java.util.Vector;

import fi.harism.facebook.R;
import fi.harism.facebook.dao.DAOComment;
import fi.harism.facebook.util.StringUtils;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CommentsDialog extends Dialog {

	private Vector<DAOComment> comments;

	public CommentsDialog(Activity activity, Vector<DAOComment> comments) {
		super(activity);
		this.comments = comments;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_comments);
		
		LinearLayout itemList = (LinearLayout) findViewById(R.id.dialog_comments_item_list);
		for (DAOComment comment : comments) {
			View commentItem = getLayoutInflater().inflate(R.layout.dialog_comments_item, null);
			
			TextView nameView = (TextView)commentItem.findViewById(R.id.dialog_comments_item_from_text);
			nameView.setText(comment.getFromName());
			
			TextView messageView = (TextView)commentItem.findViewById(R.id.dialog_comments_item_message_text);
			messageView.setText(comment.getMessage());
			
			TextView createdView = (TextView)commentItem.findViewById(R.id.dialog_comments_item_created_text);
			createdView.setText(StringUtils.convertFBTime(comment.getCreatedTime()));
			
			itemList.addView(commentItem);
		}
		
	}
	
}
