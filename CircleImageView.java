package com.turtlecorp.crave.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.turtlecorp.crave.R;

public class CircleImageView extends AppCompatImageView {

	private static final int DEF_PRESS_HIGHLIGHT_COLOR = 0x32000000;

	private Shader mBitmapShader;
	private Matrix mShaderMatrix;

	private RectF mBitmapDrawBounds;
	private RectF mStrokeBounds;

	private Bitmap mBitmap;

	private Paint mBitmapPaint;
	private Paint mStrokePaint;
	private Paint mPressedPaint;

	private boolean mInitialized;
	private boolean mPressed;
	private boolean mHighlightEnable;

	private RectF topLeftOval;
	private RectF topRightOval;
	private RectF bottomLeftOval;
	private RectF bottomRightOval;
	private Path path;

	private int topLeftCornerRadius = 0;
	private int topRightCornerRadius = 0;
	private int bottomLeftCornerRadius = 0;
	private int bottomRightCornerRadius = 0;

	public CircleImageView(Context context) {
		this(context, null);
	}

	public CircleImageView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		path = new Path();

		int strokeColor = Color.TRANSPARENT;
		float strokeWidth = 0;
		boolean highlightEnable = true;
		int highlightColor = DEF_PRESS_HIGHLIGHT_COLOR;

		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, 0, 0);

			strokeColor = a.getColor(R.styleable.CircleImageView_strokeColor, Color.TRANSPARENT);
			strokeWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_strokeWidth, 0);

			// Nayan
			topRightCornerRadius = a.getDimensionPixelSize(R.styleable.CircleImageView_topRightCornerRadius, 0);
			topLeftCornerRadius = a.getDimensionPixelSize(R.styleable.CircleImageView_topLeftCornerRadius, 0);
			bottomRightCornerRadius = a.getDimensionPixelSize(R.styleable.CircleImageView_bottomRightCornerRadius, 0);
			bottomLeftCornerRadius = a.getDimensionPixelSize(R.styleable.CircleImageView_bottomLeftCornerRadius, 0);

			highlightEnable = a.getBoolean(R.styleable.CircleImageView_highlightEnable, true);
			highlightColor = a.getColor(R.styleable.CircleImageView_highlightColor, DEF_PRESS_HIGHLIGHT_COLOR);

			a.recycle();
		}


		topLeftOval = new RectF(0, 0, topLeftCornerRadius * 2, topLeftCornerRadius * 2);
		topRightOval = new RectF(getWidth() - (topRightCornerRadius * 2), 0, getWidth(), topRightCornerRadius * 2);
		bottomLeftOval = new RectF(0, getHeight() - (bottomLeftCornerRadius * 2 ), bottomLeftCornerRadius * 2, getHeight());
		bottomRightOval = new RectF(getWidth() - (bottomRightCornerRadius * 2), getHeight() - (bottomRightCornerRadius * 2), getWidth(), getHeight());

		mShaderMatrix = new Matrix();
		mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mStrokeBounds = new RectF();
		mBitmapDrawBounds = new RectF();
		mStrokePaint.setColor(strokeColor);
		mStrokePaint.setStyle(Paint.Style.STROKE);
		mStrokePaint.setStrokeWidth(strokeWidth);

		mPressedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPressedPaint.setColor(highlightColor);
		mPressedPaint.setStyle(Paint.Style.FILL);

		mHighlightEnable = highlightEnable;
		mInitialized = true;

		setupBitmap();
	}

	@Override
	public void setImageResource(@DrawableRes int resId) {
		super.setImageResource(resId);
		setupBitmap();
	}

	@Override
	public void setImageDrawable(@Nullable Drawable drawable) {
		super.setImageDrawable(drawable);
		setupBitmap();
	}

	@Override
	public void setImageBitmap(@Nullable Bitmap bm) {
		super.setImageBitmap(bm);
		setupBitmap();
	}

	@Override
	public void setImageURI(@Nullable Uri uri) {
		super.setImageURI(uri);
		setupBitmap();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		float halfStrokeWidth = mStrokePaint.getStrokeWidth() / 2f;
		updateCircleDrawBounds(mBitmapDrawBounds);
		mStrokeBounds.set(mBitmapDrawBounds);
		mStrokeBounds.inset(halfStrokeWidth, halfStrokeWidth);

		// Nayan
		topLeftOval = new RectF(0, 0, topLeftCornerRadius * 2, topLeftCornerRadius * 2);
		topRightOval = new RectF(w - (topRightCornerRadius * 2), 0, w, topRightCornerRadius * 2);
		bottomLeftOval = new RectF(0, h - (bottomLeftCornerRadius * 2 ), bottomLeftCornerRadius * 2, h);
		bottomRightOval = new RectF(w - (bottomRightCornerRadius * 2), h - (bottomRightCornerRadius * 2), w, h);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean processed = false;
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (!isInCircle(event.getX(), event.getY())) {
					return false;
				}
				processed = true;
				mPressed = true;
				invalidate();
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				processed = true;
				mPressed = false;
				invalidate();
				if (!isInCircle(event.getX(), event.getY())) {
					return false;
				}
				break;
		}
		return super.onTouchEvent(event) || processed;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		drawBitmap(canvas);
		drawStroke(canvas);
		drawHighlight(canvas);
	}

	public boolean isHighlightEnable() {
		return mHighlightEnable;
	}

	public void setHighlightEnable(boolean enable) {
		mHighlightEnable = enable;
		invalidate();
	}

	@ColorInt
	public int getHighlightColor() {
		return mPressedPaint.getColor();
	}

	public void setHighlightColor(@ColorInt int color) {
		mPressedPaint.setColor(color);
		invalidate();
	}

	@ColorInt
	public int getStrokeColor() {
		return mStrokePaint.getColor();
	}

	public void setStrokeColor(@ColorInt int color) {
		mStrokePaint.setColor(color);
		invalidate();
	}

	@Dimension
	public float getStrokeWidth() {
		return mStrokePaint.getStrokeWidth();
	}

	public void setStrokeWidth(@Dimension float width) {
		mStrokePaint.setStrokeWidth(width);
		invalidate();
	}

	protected void drawHighlight(Canvas canvas) {
		if (mHighlightEnable && mPressed) {
			path.moveTo(0f, topLeftCornerRadius);
			path.lineTo(0f, getHeight() - bottomLeftCornerRadius);
			if(bottomLeftCornerRadius != 0)
				path.arcTo(bottomLeftOval, -180, -90, false);
			path.lineTo(getWidth() - bottomRightCornerRadius, getHeight());
			if(bottomRightCornerRadius != 0)
				path.arcTo(bottomRightOval, 90, -90, false);
			path.lineTo(getWidth(), topRightCornerRadius);
			if(topRightCornerRadius != 0)
				path.arcTo(topRightOval, 0, -90, false);
			path.lineTo(topLeftCornerRadius, 0);
			if(topLeftCornerRadius != 0)
				path.arcTo(topLeftOval, -90, -90, false);
			path.close();
			canvas.drawPath(path, mPressedPaint);
//			canvas.drawOval(mBitmapDrawBounds, mPressedPaint);
		}
	}

	protected void drawStroke(Canvas canvas) {
		if (mStrokePaint.getStrokeWidth() > 0f) {
			path.moveTo(0f, topLeftCornerRadius);
			path.lineTo(0f, getHeight() - bottomLeftCornerRadius);
			if(bottomLeftCornerRadius != 0)
				path.arcTo(bottomLeftOval, -180, -90, false);
			path.lineTo(getWidth() - bottomRightCornerRadius, getHeight());
			if(bottomRightCornerRadius != 0)
				path.arcTo(bottomRightOval, 90, -90, false);
			path.lineTo(getWidth(), topRightCornerRadius);
			if(topRightCornerRadius != 0)
				path.arcTo(topRightOval, 0, -90, false);
			path.lineTo(topLeftCornerRadius, 0);
			if(topLeftCornerRadius != 0)
				path.arcTo(topLeftOval, -90, -90, false);
			path.close();
			canvas.drawPath(path, mStrokePaint);
//			canvas.drawOval(mStrokeBounds, mStrokePaint);
		}
	}
	private void updateBitmapSize() {
		if (mBitmap == null) return;

		float dx;
		float dy;
		float scale;

		// scale up/down with respect to this view size and maintain aspect ratio
		// translate bitmap position with dx/dy to the center of the image
		if (mBitmap.getWidth() < mBitmap.getHeight()) {
			scale = mBitmapDrawBounds.width() / (float)mBitmap.getWidth();
			dx = mBitmapDrawBounds.left;
			dy = mBitmapDrawBounds.top - (mBitmap.getHeight() * scale / 2f) + (mBitmapDrawBounds.width() / 2f);
		} else {
			scale = mBitmapDrawBounds.height() / (float)mBitmap.getHeight();
			dx = mBitmapDrawBounds.left - (mBitmap.getWidth() * scale / 2f) + (mBitmapDrawBounds.width() / 2f);
			dy = mBitmapDrawBounds.top - (mBitmap.getHeight() * scale / 2f) + (mBitmapDrawBounds.width() / 2f);
		}
		mShaderMatrix.setScale(scale, scale);
		mShaderMatrix.postTranslate(dx, dy);
		mBitmapShader.setLocalMatrix(mShaderMatrix);
	}

	protected void drawBitmap(Canvas canvas) {
		path.moveTo(0f, topLeftCornerRadius);
		path.lineTo(0f, getHeight() - bottomLeftCornerRadius);
		if(bottomLeftCornerRadius != 0)
			path.arcTo(bottomLeftOval, -180, -90, false);
		path.lineTo(getWidth() - bottomRightCornerRadius, getHeight());
		if(bottomRightCornerRadius != 0)
			path.arcTo(bottomRightOval, 90, -90, false);
		path.lineTo(getWidth(), topRightCornerRadius);
		if(topRightCornerRadius != 0)
			path.arcTo(topRightOval, 0, -90, false);
		path.lineTo(topLeftCornerRadius, 0);
		if(topLeftCornerRadius != 0)
			path.arcTo(topLeftOval, -90, -90, false);
		path.close();

		canvas.drawPath(path, mBitmapPaint);
//		canvas.drawOval(mBitmapDrawBounds, mBitmapPaint);
	}

	protected void updateCircleDrawBounds(RectF bounds) {
		float contentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
		float contentHeight = getHeight() - getPaddingTop() - getPaddingBottom();

		bounds.set( - contentWidth / 2, -  contentHeight / 2, contentWidth, contentHeight);
	}

	private void setupBitmap() {
		if (!mInitialized) {
			return;
		}
		mBitmap = getBitmapFromDrawable(getDrawable());
		if (mBitmap == null) {
			return;
		}

		mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
		mBitmapPaint.setShader(mBitmapShader);

	}


	private Bitmap getBitmapFromDrawable(Drawable drawable) {
		if (drawable == null) {
			return null;
		}

		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(
				drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	private boolean isInCircle(float x, float y) {
		// find the distance between center of the view and x,y point
		double distance = Math.sqrt(
				Math.pow(mBitmapDrawBounds.centerX() - x, 2) + Math.pow(mBitmapDrawBounds.centerY() - y, 2)
		);
		return distance <= (mBitmapDrawBounds.width() / 2);
	}
}