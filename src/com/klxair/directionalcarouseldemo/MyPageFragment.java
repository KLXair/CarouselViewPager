package com.klxair.directionalcarouseldemo;

import android.view.View;
import android.widget.ImageView;

import com.klxair.ui.view.viewpager.directionalcarousel.page.PageFragment;
import com.klxair.ui.view.viewpager.directionalcarousel.page.PageLayout;
import com.squareup.picasso.Picasso;

public class MyPageFragment extends PageFragment<MyPageItem> {

	// …Ë÷√Pager
	@Override
	public View setupPage(PageLayout pageLayout, MyPageItem pageItem) {
		View pageContent = pageLayout.findViewById(R.id.page_content);
		ImageView iv_pho = (ImageView) pageContent.findViewById(R.id.iv_pho);
		Picasso.with(getActivity()).load(pageItem.getTitle()).error(R.drawable.ic_launcher).into(iv_pho, null);
		return pageContent;
	}
}
