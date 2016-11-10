package com.klxair.ui.view.viewpager.directionalcarousel.page;

import com.klxair.ui.view.viewpager.directionalcarousel.CarouselConfig;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class PageLayout extends LinearLayout {
	private float mScale = CarouselConfig.getInstance().bigScale;

	public PageLayout(Context context) {
		super(context);
		setWillNotDraw(false);
	}

	public PageLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(false);
	}

	public PageLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
	}

	public void setScaleBoth(float scale) {
		mScale = scale;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.scale(mScale, mScale, getWidth() / 2, getHeight() / 2);
	}
}
