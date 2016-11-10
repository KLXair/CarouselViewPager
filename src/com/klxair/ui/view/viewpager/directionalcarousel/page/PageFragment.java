package com.klxair.ui.view.viewpager.directionalcarousel.page;

import com.klxair.ui.view.viewpager.directionalcarousel.CarouselConfig;
import com.klxair.ui.view.viewpager.directionalcarousel.CarouselViewPager;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class PageFragment<T extends Parcelable> extends Fragment {
	/**
	 * @param pageLayout
	 *            页面布局。
	 * @param pageItem
	 *            自定义页面内容视图的项目。
	 * @return View of page content.
	 */
	public abstract View setupPage(PageLayout pageLayout, T pageItem);

	public static Bundle createArgs(int pageLayoutId, Parcelable item) {
		Bundle args = new Bundle();
		args.putInt("page_layout_id", pageLayoutId);
		args.putParcelable("item", item);
		return args;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

		if (container.getClass() != CarouselViewPager.class) {
			throw new IllegalArgumentException("PageFragment must be attached to " + "CarouselViewPager.");
		}

		int pageLayoutId = getArguments().getInt("page_layout_id");
		PageLayout pageLayout = (PageLayout) inflater.inflate(pageLayoutId, container, false);
		if (pageLayout == null) {
			throw new IllegalStateException("PageFragment root layout must have id " + "R.id.page.");
		}

		CarouselConfig config = CarouselConfig.getInstance();
		float scale;
		if (config.scrollScalingMode == CarouselConfig.SCROLL_MODE_BIG_CURRENT
				|| config.scrollScalingMode == CarouselConfig.SCROLL_MODE_BIG_ALL) {
			scale = config.smallScale;
		} else {
			scale = config.bigScale;
		}
		pageLayout.setScaleBoth(scale);

		if (config.orientation == CarouselConfig.VERTICAL) {
			pageLayout.setRotation(-90);
		}

		T item = getArguments().getParcelable("item");
		View pageContent = setupPage(pageLayout, item);
		if (pageContent != null) {
			pageContent.setOnTouchListener((CarouselViewPager) container);
			pageContent.setTag(item);
		}
		return pageLayout;
	}
}