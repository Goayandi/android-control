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
 * 圆形图片
 * 
 */
public class CircleImageView extends ImageView {
	private int m_radius = 0;

	public CircleImageView(Context context) {
		super(context);
	}

	public CircleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int w = getWidth();
		int h = getHeight();
		m_radius = (w <= h ? w : h) /2;
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
		drawCircleBorder(canvas, m_radius, m_radius, m_radius, Color.LTGRAY);
		canvas.drawCircle(m_radius, m_radius, m_radius-1, paint);
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
