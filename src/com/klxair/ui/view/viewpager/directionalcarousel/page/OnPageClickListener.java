package com.klxair.ui.view.viewpager.directionalcarousel.page;

import android.os.Parcelable;
import android.view.View;

public interface OnPageClickListener<T extends Parcelable> {
	void onSingleTap(View view, T item);

	void onDoubleTap(View view, T item);
}
