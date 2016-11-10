package com.klxair.directionalcarouseldemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import com.klxair.ui.view.viewpager.directionalcarousel.CarouselPagerAdapter;
import com.klxair.ui.view.viewpager.directionalcarousel.CarouselViewPager;
import com.klxair.ui.view.viewpager.directionalcarousel.page.OnPageClickListener;

public class MainActivity extends AppCompatActivity implements OnPageClickListener<MyPageItem> {
	private CarouselViewPager<MyPageItem> mViewPager;
	private CarouselPagerAdapter<MyPageItem> mPagerAdapter;
	private ArrayList<MyPageItem> mItems;

	/** 播放的间隔时间 */
	private int sleepTime = 5000;
	/** 播放的方向 */
	private int playingDirection = 0;
	/** 播放方向方式（1顺序播放和0来回播放） */
	private int playType = 0;
	/** 当前页数 */
	int i = 0;
	/** 播放动画 */
	boolean anim = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			int size = 2;
			mItems = new ArrayList<MyPageItem>(size);
			for (int i = 0; i < size; i++) {
				mItems.add(new MyPageItem(
						"https://ss0.bdstatic.com/94oJfD_bAAcT8t7mm9GUKT-xh_/timg?image&quality=100&size=b4000_4000&sec=1477474555&di=a14f1611850494e3418030a7ca76f392&src=http://img3.duitang.com/uploads/item/201603/16/20160316211953_vk2An.jpeg"));
				mItems.add(new MyPageItem(
						"https://ss0.bdstatic.com/94oJfD_bAAcT8t7mm9GUKT-xh_/timg?image&quality=100&size=b4000_4000&sec=1477476394&di=b5314882fbffbd3159b02b31ae9f4c14&src=http://pic.58pic.com/58pic/13/41/01/49e58PIC9iu_1024.jpg"));
			}
		} else {
			mItems = savedInstanceState.getParcelableArrayList("items");
		}

		mViewPager = (CarouselViewPager) findViewById(R.id.carousel_pager);
		mPagerAdapter = new CarouselPagerAdapter<MyPageItem>(getSupportFragmentManager(), MyPageFragment.class,
				R.layout.page_layout, mItems);
		mPagerAdapter.setOnPageClickListener(this);

		mViewPager.setAdapter(mPagerAdapter);
		initView();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList("items", mItems);
	}

	@Override
	public void onSingleTap(View view, MyPageItem item) {
		Toast.makeText(getApplicationContext(), "单击: " + item.getTitle(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDoubleTap(View view, MyPageItem item) {
		Toast.makeText(getApplicationContext(), "双击: " + item.getTitle(), Toast.LENGTH_SHORT).show();
	}

	private void initView() {
	}

	@Override
	public void onStart() {
		// 设置是否轮播
		mViewPager.initPlayConfigure(mViewPager, mItems, sleepTime, playingDirection, playType, anim);
		super.onStart();
	}

	@Override
	public void onStop() {
		mViewPager.stopPlay();
		super.onStop();
	}

}
