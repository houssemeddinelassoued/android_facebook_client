package fi.harism.facebook.dialog;

import fi.harism.facebook.R;
import fi.harism.facebook.dao.DAOComment;
import fi.harism.facebook.dao.DAOCommentList;
import fi.harism.facebook.dao.DAOObserver;
import fi.harism.facebook.util.StringUtils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CommentsDialog extends Dialog {

	private DAOCommentList comments;

	public CommentsDialog(Context context, DAOCommentList comments) {
		super(context);
		this.comments = comments;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_comments);
		
		View footer = findViewById(R.id.dialog_comments_footer);
		footer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				InputDialog dlg = new InputDialog(getContext(), new InputDialogObserver());
				dlg.show();
			}
		});
		
		updateCommentList(comments);
	}
	
	private void updateCommentList(DAOCommentList commentList) {
		LinearLayout itemList = (LinearLayout) findViewById(R.id.dialog_comments_item_list);
		itemList.removeAllViews();
		for (DAOComment comment : commentList) {
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
	
	private void postComment(String message) {
		ProgressDialog progressDialog = new ProgressDialog(getContext());
		progressDialog.setMessage("Sending..");
		progressDialog.setCancelable(false);
		progressDialog.show();
		comments.postComment(new DAOCommentListObserver(progressDialog), message);
	}	
	
	private class DAOCommentListObserver implements DAOObserver<DAOCommentList> {
		private ProgressDialog progressDialog;
		public DAOCommentListObserver(ProgressDialog progressDialog) {
			this.progressDialog = progressDialog;
		}
		@Override
		public void onComplete(DAOCommentList response) {
			progressDialog.dismiss();
			updateCommentList(response);
		}
		@Override
		public void onError(Exception error) {
			progressDialog.dismiss();
		}
	}
	
	private class InputDialogObserver implements InputDialog.InputObserver {
		@Override
		public void onComplete(String text) {
			if (text.trim().length() > 0) {
				postComment(text);
			}
		}
	}
	
}
