package com.yongyida.robot.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.yongyida.robot.video.comm.log;

/**
 * 可以选中的圆形图片
 * 
 */
public class CircleSelectImageView extends ImageView {
	private int mRadius = 0;
	private boolean mSelected = false;

	public CircleSelectImageView(Context context) {
		super(context);
	}

	public CircleSelectImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CircleSelectImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public boolean getSelected() {
		return mSelected;
	}
	
	public void setSelected(boolean selected) {
		mSelected = selected;
		invalidate();
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int w = getWidth();
		int h = getHeight();
		mRadius = (w <= h ? w : h) /2;
	}

	@Override
	public void draw(Canvas canvas) {
		BitmapDrawable drawable = (BitmapDrawable)getDrawable();
		if (drawable == null) {
			log.e("CircleImageView", "BitmapDrawable null");
			return;
		}
		
		Bitmap bitmap = drawable.getBitmap();
		BitmapShader shader = new BitmapShader(bitmap,
				BitmapShader.TileMode.CLAMP,
				BitmapShader.TileMode.CLAMP);

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setShader(shader);
		int borderColor = mSelected ? Color.GREEN : Color.LTGRAY;
		drawCircleBorder(canvas, mRadius, mRadius, mRadius, borderColor);
		canvas.drawCircle(mRadius, mRadius, mRadius - 1, paint);
	}
	
	private void drawCircleBorder(Canvas canvas, int cx, int cy, int radius, int color) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(color);
		paint.setDither(true);
		paint.setFilterBitmap(true);
		canvas.drawCircle(cx, cy, radius, paint);
	}
}
