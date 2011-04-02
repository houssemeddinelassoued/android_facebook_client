package fi.harism.facebook.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;

public class ProfilePictureView extends View {

	private Bitmap currentBitmap = null;
	private Bitmap nextBitmap = null;
	private int alpha = 255;
	private AlphaAnimation anim;
	private Paint paint;
	private Rect targetRect = new Rect();

	private static final int ANIM_LENGTH = 700;

	public ProfilePictureView(Context context) {
		super(context);
		init();
	}

	public ProfilePictureView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ProfilePictureView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		anim = new AlphaAnimation(1, 0);
		anim.setDuration(ANIM_LENGTH);
		anim.setAnimationListener(new AnimationListener());			
	}
	
	public void setBitmap(Bitmap bitmap) {
		Rect r = new Rect();
		boolean visible = getLocalVisibleRect(r);
		if (currentBitmap == null || !visible) {
			currentBitmap = bitmap;
			nextBitmap = null;
			if (visible) {
				invalidate();
			}
		} else {
			nextBitmap = bitmap;
			setAnimation(anim);
			postInvalidate();
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int x1 = getPaddingLeft();
		int x2 = getWidth() - getPaddingRight();
		int y1 = getPaddingTop();
		int y2 = getHeight() - getPaddingBottom();
		targetRect = new Rect(x1, y1, x2, y2);
	}

	@Override
	protected boolean onSetAlpha(int newAlpha) {
		super.onSetAlpha(newAlpha);
		
		if (getAnimation() != null) {
			alpha = newAlpha;
			return true;
		} else {
			alpha = 255;
			return true;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		Bitmap b1 = currentBitmap;
		Bitmap b2 = nextBitmap;

		if (b1 != null) {
			paint.setAlpha(alpha);
			canvas.drawBitmap(b1, null, targetRect, paint);
		}
		if (b2 != null) {
			paint.setAlpha(255 - alpha);
			canvas.drawBitmap(b2, null, targetRect, paint);
		}
	}

	private class AnimationListener implements Animation.AnimationListener {

		@Override
		public void onAnimationEnd(Animation arg0) {
			currentBitmap = nextBitmap;
			nextBitmap = null;
			setAnimation(null);
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
		}

		@Override
		public void onAnimationStart(Animation arg0) {
		}

	}

}
