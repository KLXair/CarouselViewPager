package com.klxair.ui.view.viewpager.directionalcarousel;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.klxair.directionalcarouseldemo.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * @author KLXair
 * 
 *         反向转盘式滚轮轮播图
 * 
 */
@SuppressLint({ "HandlerLeak", "RtlHardcoded", "ClickableViewAccessibility" })
public class CarouselViewPager<T> extends ViewPager implements OnTouchListener {
	public static final float DEFAULT_SIDE_PAGES_VISIBLE_PART = 0.5f;
	private static final String TAG = CarouselViewPager.class.getSimpleName();
	private static final boolean DEBUG = false;

	private int mViewPagerWidth;
	private int mViewPagerHeight;

	// 页面之间的距离总是大于这个值（即使缩放）
	private int mMinPagesOffset;
	private float mSidePagesVisiblePart;
	private int mWrapPadding;

	private boolean mSizeChanged;
	private float gravityOffset;

	private Resources mResources;
	private CarouselConfig mConfig;

	private GestureDetector mGestureDetector;
	private OnGestureListener mGestureListener;
	private View mTouchedView;
	private Parcelable mTouchedItem;

	private final String mPackageName;
	private int mPageContentWidthId;// 内容页的宽
	private int mPageContentHeightId;// 内容页的高

	/** 播放的开关 */
	private boolean play = false;
	/** 是否有播放的动画true有，false无 */
	private boolean anim = true;
	/** 播放的间隔时间 */
	private int sleepTime = 0;
	/** 播放的方向 0逆时针,1顺时针 */
	private int playingDirection = 0;
	/** 播放方向方式（1顺序播放和0来回播放） */
	private int playType = 0;
	/** 当前页数 */
	int i = 0;
	/** 播放的CarouselViewPager */
	private CarouselViewPager<T> mPager;
	/** 播放的内容序列 */
	private ArrayList<T> mItems;

	/**
	 * 描述：为解决CarouselViewPager与SliDingMenu一起使用时mResources.getDimensionPixelSize(
	 * mPageContentHeightId);无法获取到对应资源报错;
	 * 
	 * 如果CarouselViewPager与SliDingMenu一起使用则抛出出NotFoundException，
	 * 并去拿取mChildPageContentWidthId和mChildPageContentHeightId的值
	 */
	private int mChildPageContentWidthId = 500;// 取值1000-0，两个画册中间的宽度
	private int mChildPageContentHeightId;

	public CarouselViewPager(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		setMyScroller();
	}

	public CarouselViewPager(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);

		mConfig = CarouselConfig.getInstance();
		mConfig.pagerId = getId();
		mResources = context.getResources();
		mPackageName = context.getPackageName();
		mPageContentWidthId = mResources.getIdentifier("page_content_width", "dimen", mPackageName);
		mPageContentHeightId = mResources.getIdentifier("page_content_height", "dimen", mPackageName);

		////////////////////////////////////////////////////////////////////////////////////////// 扩展用
		// mChildPageContentWidthId =
		// mResources.getIdentifier("page_content_width", "dimen",
		// mPackageName);
		// mChildPageContentHeightId =
		// mResources.getIdentifier("page_content_height", "dimen",
		// mPackageName);
		////////////////////////////////////////////////////////////////////////////////////////// 扩展用
		DisplayMetrics dm = mResources.getDisplayMetrics();
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CarouselViewPager, defStyle, 0);
		try {
			if (a != null) {
				mConfig.orientation = a.getInt(R.styleable.CarouselViewPager_android_orientation,
						CarouselConfig.HORIZONTAL);
				mConfig.infinite = a.getBoolean(R.styleable.CarouselViewPager_infinite, true);
				mConfig.scrollScalingMode = a.getInt(R.styleable.CarouselViewPager_scrollScalingMode,
						CarouselConfig.SCROLL_MODE_BIG_CURRENT);

				float bigScale = a.getFloat(R.styleable.CarouselViewPager_bigScale, CarouselConfig.DEFAULT_BIG_SCALE);
				if (bigScale > 1.0f || bigScale < 0.0f) {
					bigScale = CarouselConfig.DEFAULT_BIG_SCALE;
					Log.w(TAG, "无效大尺度属性。默认值 " + CarouselConfig.DEFAULT_BIG_SCALE + " 将被使用。");
				}
				mConfig.bigScale = bigScale;

				float smallScale = a.getFloat(R.styleable.CarouselViewPager_smallScale,
						CarouselConfig.DEFAULT_SMALL_SCALE);
				if (smallScale > 1.0f || smallScale < 0.0f) {
					smallScale = CarouselConfig.DEFAULT_SMALL_SCALE;
					Log.w(TAG, "无效小尺度属性。默认值 " + CarouselConfig.DEFAULT_SMALL_SCALE + " 将被使用。");
				} else if (smallScale > bigScale) {
					smallScale = bigScale;
					Log.w(TAG, "无效小尺度属性。值 " + bigScale + " 将被使用。");
				}
				mConfig.smallScale = smallScale;

				mMinPagesOffset = (int) a.getDimension(R.styleable.CarouselViewPager_minPagesOffset,
						TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, dm));
				mSidePagesVisiblePart = a.getFloat(R.styleable.CarouselViewPager_sidePagesVisiblePart,
						DEFAULT_SIDE_PAGES_VISIBLE_PART);
				mWrapPadding = (int) a.getDimension(R.styleable.CarouselViewPager_wrapPadding,
						TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, dm));
			}
		} finally {
			if (a != null) {
				a.recycle();
			}
		}

		mGestureListener = new SimpleOnGestureListener() {
			@SuppressWarnings("unchecked")
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (getCarouselAdapter() != null) {
					getCarouselAdapter().sendSingleTap(mTouchedView, mTouchedItem);
				}
				mTouchedView = null;
				mTouchedItem = null;
				return true;
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				if (getCarouselAdapter() != null) {
					getCarouselAdapter().sendDoubleTap(mTouchedView, mTouchedItem);
				}
				mTouchedView = null;
				mTouchedItem = null;
				return true;
			}
		};

		mGestureDetector = new GestureDetector(context, mGestureListener);
	}

	@SuppressWarnings({ "deprecation", "rawtypes" })
	@Override
	public void setAdapter(PagerAdapter adapter) {
		super.setAdapter(adapter);
		if (adapter == null) {
			return;
		}

		if (adapter.getClass() != CarouselPagerAdapter.class) {
			throw new ClassCastException("适配器必须carouselpageradapter类实例。");
		}

		setOnPageChangeListener((OnPageChangeListener) adapter);
		setCurrentItem(((CarouselPagerAdapter) adapter).getFirstPosition());
	}

	@SuppressWarnings("rawtypes")
	private CarouselPagerAdapter getCarouselAdapter() {
		PagerAdapter adapter = getAdapter();
		if (adapter == null) {
			return null;
		}
		return (CarouselPagerAdapter) adapter;
	}

	@Override
	public Parcelable onSaveInstanceState() {
		CarouselState ss = new CarouselState(super.onSaveInstanceState());
		ss.position = getCurrentItem();

		PagerAdapter adapter = getAdapter();
		if (adapter == null) {
			ss.itemsCount = 0;
		} else {
			ss.itemsCount = adapter.getCount();
		}
		ss.infinite = mConfig.infinite;
		return ss;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (!(state instanceof CarouselState)) {
			super.onRestoreInstanceState(state);
			return;
		}

		CarouselState ss = (CarouselState) state;
		super.onRestoreInstanceState(ss.getSuperState());

		if (getAdapter() == null) {
			return;
		}

		if (ss.infinite && !mConfig.infinite) {
			int itemsCount = getAdapter().getCount();
			if (itemsCount == 0) {
				return;
			}

			int offset = (ss.position - ss.itemsCount / 2) % itemsCount;
			if (offset >= 0) {
				setCurrentItem(offset);
			} else {
				setCurrentItem(ss.itemsCount / CarouselConfig.LOOPS + offset);
			}
		} else if (!ss.infinite && mConfig.infinite) {
			setCurrentItem(ss.itemsCount * CarouselConfig.LOOPS / 2 + ss.position);
		} else {
			setCurrentItem(ss.position);
		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent e) {
		mTouchedView = view;
		mTouchedItem = (Parcelable) view.getTag();
		return mGestureDetector.onTouchEvent(e);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mSizeChanged = true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		/**
		 * 描述：为解决CarouselViewPager与SliDingMenu一起使用时mResources.
		 * getDimensionPixelSize( mPageContentHeightId);无法获取到对应资源报错;
		 * 
		 * 获取其孩子来实现拿到资源
		 */
		int childWidth = getDefaultSize(0, widthMeasureSpec);
		int childHeight = getDefaultSize(0, heightMeasureSpec);
		setMeasuredDimension(childWidth, childHeight);

		// mChildPageContentWidthId = getChildMeasureSpec(widthMeasureSpec, 0,
		// childWidth);//目前不需要直接获取到子资源值，留作后期用
		mChildPageContentHeightId = getChildMeasureSpec(heightMeasureSpec, 0, childHeight);
		// mTouchedView.measure(mChildPageContentWidthId,
		// mChildPageContentHeightId);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int height = MeasureSpec.getSize(heightMeasureSpec);

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		if (DEBUG) {
			Log.d(TAG, "w=" + width + " h=" + height);
			Log.d(TAG, "wMode=" + getModeDescription(widthMode) + " hMode=" + getModeDescription(heightMode));
		}

		// FIXME 只支持match_parent和wrap_content属性
		if (mConfig.orientation == CarouselConfig.VERTICAL) {
			int pageContentWidth = getPageContentWidth();
			int newWidth = width;
			if (widthMode == MeasureSpec.AT_MOST || pageContentWidth + 2 * mWrapPadding > width) {

				newWidth = pageContentWidth + 2 * mWrapPadding;
				widthMeasureSpec = MeasureSpec.makeMeasureSpec(newWidth, widthMode);
			}

			ViewGroup.LayoutParams lp = getLayoutParams();
			// FIXME 只支持FrameLayout作为母
			if (lp instanceof FrameLayout.LayoutParams) {
				if (!parentHasExactDimensions()) {
					throw new UnsupportedOperationException("父布局应该有确切的 " + "dimensions.");
				}

				FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) lp;
				if (!mSizeChanged) {
					gravityOffset = 0.0f;
					int hGrav = params.gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
					if (hGrav == Gravity.CENTER_HORIZONTAL || hGrav == Gravity.CENTER) {
						gravityOffset = (width - newWidth) * 0.5f;
					}
					if (hGrav == Gravity.RIGHT) {
						gravityOffset = width - newWidth;
					}
				}

				setRotation(90);
				setTranslationX((newWidth - height) * 0.5f + gravityOffset);
				setTranslationY(-(newWidth - height) * 0.5f);
				params.gravity = Gravity.NO_GRAVITY;
				setLayoutParams(params);
			} else {
				throw new UnsupportedOperationException("父布局应该是 " + "FrameLayout.");
			}

			mSizeChanged = true;
			super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		} else {
			int pageContentHeight = getPageContentHeight();
			if (heightMode == MeasureSpec.AT_MOST || pageContentHeight + 2 * mWrapPadding > height) {

				int newHeight = pageContentHeight + 2 * mWrapPadding;
				heightMeasureSpec = MeasureSpec.makeMeasureSpec(newHeight, heightMode);
			}

			// FIXME 只支持FrameLayout作为母
			if (!(getLayoutParams() instanceof FrameLayout.LayoutParams)) {
				throw new UnsupportedOperationException("父布局应该是 " + "FrameLayout.");
			} else {
				if (!parentHasExactDimensions()) {
					throw new UnsupportedOperationException("父布局应该有确切的 " + "dimensions.");
				}
			}

			mSizeChanged = true;
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}

		mViewPagerWidth = getMeasuredWidth();
		mViewPagerHeight = getMeasuredHeight();

		if (calculatePageLimitAndMargin()) {
			setOffscreenPageLimit(mConfig.pageLimit);
			setPageMargin(mConfig.pageMargin);
		}

		if (DEBUG) {
			Log.d(TAG, mConfig.toString());
		}
	}

	private boolean parentHasExactDimensions() {
		ViewGroup.LayoutParams params = ((ViewGroup) getParent()).getLayoutParams();
		return !(params.width == ViewGroup.LayoutParams.WRAP_CONTENT
				|| params.height == ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	private String getModeDescription(int mode) {
		if (mode == MeasureSpec.AT_MOST) {
			return "AT_MOST";
		}

		if (mode == MeasureSpec.EXACTLY) {
			return "EXACTLY";
		}

		if (mode == MeasureSpec.UNSPECIFIED) {
			return "UNSPECIFIED";
		}

		return "UNKNOWN";
	}

	private int getPageContentWidth() {
		try {
			return mResources.getDimensionPixelSize(mPageContentWidthId);
		} catch (NotFoundException e) {
			Log.e("NotFoundException", "未找到指定的资源，拿取mChildPageContentWidthId的值");
		} finally {

		}
		return mChildPageContentWidthId;

		/**
		 * 旧的方式，作为痕迹保留
		 * 
		 * 描述：为解决CarouselViewPager与SliDingMenu一起使用时mResources.
		 * getDimensionPixelSize( mPageContentHeightId);无法获取到对应资源报错;
		 * 
		 * 获取其孩子来实现拿到资源
		 */
		// if (hasSliding) {
		// return 500;
		// } else {
		// return mResources.getDimensionPixelSize(mPageContentWidthId);
		// }
	}

	private int getPageContentHeight() {
		try {
			return mResources.getDimensionPixelSize(mPageContentHeightId);
		} catch (NotFoundException e) {
			Log.e("NotFoundException", "未找到指定的资源，拿取mChildPageContentHeightId的值");
		} finally {

		}
		return mChildPageContentHeightId;

		/**
		 * 旧的方式，作为痕迹保留
		 * 
		 * 描述：为解决CarouselViewPager与SliDingMenu一起使用时mResources.
		 * getDimensionPixelSize( mPageContentHeightId);无法获取到对应资源报错;
		 * 
		 * 获取其孩子来实现拿到资源
		 */
		// if (hasSliding) {
		// return mChildPageContentHeightId;
		// } else {
		// return mResources.getDimensionPixelSize(mPageContentHeightId);
		// }
	}

	/**
	 * @return true，如果配置成功更新。
	 */
	private boolean calculatePageLimitAndMargin() {
		if (mViewPagerWidth == 0 || mViewPagerHeight == 0) {
			return false;
		}

		int contentSize;
		if (mConfig.orientation == CarouselConfig.HORIZONTAL) {
			contentSize = getPageContentWidth();
		} else {
			contentSize = getPageContentHeight();
		}

		int viewSize = mViewPagerWidth;
		int minOffset = 0;
		switch (mConfig.scrollScalingMode) {
		case CarouselConfig.SCROLL_MODE_BIG_CURRENT: {
			minOffset = (int) (mConfig.getDiffScale() * contentSize / 2) + mMinPagesOffset;
			contentSize *= mConfig.smallScale;
			break;
		}
		case CarouselConfig.SCROLL_MODE_BIG_ALL: {
			minOffset = (int) (mConfig.getDiffScale() * contentSize) + mMinPagesOffset;
			contentSize *= mConfig.smallScale;
			break;
		}
		case CarouselConfig.SCROLL_MODE_NONE: {
			minOffset = mMinPagesOffset;
			break;
		}
		}

		if (contentSize + 2 * minOffset > viewSize) {
			if (DEBUG) {
				Log.d(TAG, "页面内容太大。");
			}
			return false;
		}

		while (true) {
			if (mSidePagesVisiblePart < 0.0f) {
				mSidePagesVisiblePart = 0.0f;
				break;
			}
			if (contentSize + 2 * contentSize * (mSidePagesVisiblePart) + 2 * minOffset <= viewSize) {
				break;
			}
			mSidePagesVisiblePart -= 0.1f;
		}

		int fullPages = 1;
		final int s = viewSize - (int) (2 * contentSize * mSidePagesVisiblePart);

		while (minOffset + (fullPages + 1) * (contentSize + minOffset) <= s) {
			fullPages++;
		}

		if (fullPages != 0 && fullPages % 2 == 0) {
			fullPages--;
		}

		int offset = (s - fullPages * contentSize) / (fullPages + 1);
		int pageLimit;
		if (Math.abs(mSidePagesVisiblePart) > 1e-6) {
			pageLimit = (fullPages + 2) - 1;
		} else {
			if (fullPages > 1) {
				pageLimit = fullPages - 1;
			} else {
				pageLimit = 1;
			}
		}
		// 正确滚动的准备页面
		pageLimit = 2 * pageLimit + pageLimit / 2;
		mConfig.pageLimit = pageLimit;

		mConfig.pageMargin = -(viewSize - contentSize - offset);
		return true;
	}

	public static class CarouselState extends BaseSavedState {
		int position;
		int itemsCount;
		boolean infinite;

		public CarouselState(Parcelable superState) {
			super(superState);
		}

		private CarouselState(Parcel in) {
			super(in);
			position = in.readInt();
			itemsCount = in.readInt();
			infinite = in.readInt() == 1;
		}

		public static final Parcelable.Creator<CarouselState> CREATOR = new Parcelable.Creator<CarouselState>() {
			@Override
			public CarouselState createFromParcel(Parcel in) {
				return new CarouselState(in);
			}

			@Override
			public CarouselState[] newArray(int size) {
				return new CarouselState[0];
			}
		};

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(position);
			out.writeInt(itemsCount);
			out.writeInt(infinite ? 1 : 0);
		}
	}

	/**
	 * 描述：自动轮播
	 */
	private void setMyScroller() {
		try {
			Class<?> viewpager = ViewPager.class;
			Field scroller = viewpager.getDeclaredField("mScroller");
			scroller.setAccessible(true);
			scroller.set(this, new MyScroller(getContext()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 描述：设置duration可实现不同的跳转播放速度
	 */
	public class MyScroller extends Scroller {

		public MyScroller(Context context) {
			super(context, new DecelerateInterpolator());
		}

		@Override
		public void startScroll(int startX, int startY, int dx, int dy, int duration) {
			super.startScroll(startX, startY, dx, dy, 1000);
		}
	}

	/**
	 * 轮播的配置，若需要实现自动轮播则实现该方法即可
	 * 
	 * 注意：正确使用应在onStart()方法中调用initPlayConfigure方法，在onStop()调用stopPlay()方法
	 *
	 * 切记：要在onStop()方法中调用stopPlay()方法，防止退出后计时器还在报空指针异常
	 * 
	 * @param mPager
	 *            播放的CarouselViewPager
	 * @param mItems
	 *            播放的内容序列
	 * @param sleepTime
	 *            播放的间隔时间单位ms
	 * @param playingDirection
	 *            播放的方向 0逆时针,1顺时针
	 * @param playType
	 *            播放方向方式（1顺序播放和0来回播放）
	 * @param anim
	 *            是否有播放的动画true有，false无
	 * 
	 */
	public void initPlayConfigure(CarouselViewPager<T> mPager, ArrayList<T> mItems, int sleepTime, int playingDirection,
			int playType, boolean anim) {
		this.mPager = mPager;
		this.mItems = mItems;
		this.sleepTime = sleepTime;
		this.playingDirection = playingDirection;
		this.playType = playType;
		this.anim = anim;
		startPlay();
	}

	/** 用与轮换的 handler. */
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				// 显示播放的页面
				ShowPlay();
				if (play) {
					handler.postDelayed(runnable, sleepTime);
				}
			}
		}

	};

	/** 用于轮播的线程. */
	private Runnable runnable = new Runnable() {
		public void run() {
			if (mPager != null) {
				handler.sendEmptyMessage(0);

			}
		}
	};

	/**
	 * 描述：自动轮播. sleepTime 播放的间隔时间
	 */
	public void startPlay() {
		if (handler != null) {
			play = true;
			handler.postDelayed(runnable, sleepTime);
		}
	}

	/**
	 * 描述：要在onStop()方法中调用此方法，防止退出后计时器还在报空指针异常
	 */
	public void stopPlay() {
		if (handler != null) {
			play = false;
			handler.removeCallbacks(runnable);
		}
	}

	/**
	 * 描述：播放显示界面（1顺序播放和0来回播放） playType 为0表示来回播放，为1表示顺序播放
	 */
	public void ShowPlay() {
		// 总页数
		int count = mItems.size();
		// 当前显示的页数
		// int i = mViewPager.getCurrentItem();
		i = mPager.getCurrentItem();
		switch (playType) {
		case 0:
			// 来回播放
			if (playingDirection == 0) {
				if (i == count - 1) {
					playingDirection = -1;
					i--;
				} else {
					i++;
				}
			} else {
				if (i == 0) {
					playingDirection = 0;
					i++;
				} else {
					i--;
				}
			}
			break;
		case 1:
			// 顺序播放
			if (i == count - 1) {
				i = 0;
			} else {
				i++;
			}

			break;

		default:
			break;
		}
		// 设置显示第几页
		mPager.setCurrentItem(i, anim);
		Log.e("i", i + "");
	}

}
