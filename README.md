# CarouselViewPager
轮播banner，实现旋转木马效果，Galler 效果，可无限循环自动播放、拖拽

基于上次修改完善，以解决解决与SlidingMenu的冲突，并完善改写自动轮播方法，可实现轮播banner，旋转木马效果，可自定义是否无限循环自动播放，可拖拽滑动，可自定义轮播时间、滑动时间，高自定义。

源码在最下方

## 效果图

![baidu](http://h.hiphotos.baidu.com/image/pic/item/d1160924ab18972b9b88bdfaefcd7b899e510a4b.jpg)  

## 主要特点
1、适应横竖屏切换

2、解决轮播与scrollview嵌套冲突

3、高自定义

4、自动滑动

5、果冻拖拽弹回

6、自定义背景

7、解决解决与SlidingMenu的冲突

## 使用步骤
### 导入项目

#### main_activity.xml布局

* 说明

  设置android:orientation="horizontal"横向滑动

  设置android:orientation="vertical"纵向滑动
  
* （注：CarouselViewPager的父布局必须是FrameLayout不然会抛UnsupportedOperationException异常: 父布局应该是 FrameLayout.）

```xml
  <com.klxair.ui.view.viewpager.directionalcarousel.CarouselViewPager
                    android:id="@+id/carousel_pager"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:orientation="horizontal"
                    carousel:infinite="true"
                    carousel:scrollScalingMode="bigCurrent" />
```
#### MainActivity类

* 说明

  play	播放的开关
  
  sleepTime	播放的间隔时间
  
  playingDirection	播放的方向
  
  playType	播放方向方式（1顺序播放和0来回播放）
  
  i	当前页数
  


```java
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
```

#### page_content.xml布局

* 说明

  （com.klxair.ui.view.imageview.RoundImageView该控件为自己写的圆角图片类，如果你们自己用的话替换成ImageView就可以了。如果有朋友想要这个工具类的话，也可以联系我）
  
  android:foreground	点击后显示的背景色
```xml
  <com.klxair.ui.view.viewpager.directionalcarousel.page.PageLayout         xmlns:android="http://schemas.android.com/apk/res/android"
      xmlns:tools="http://schemas.android.com/tools"
      xmlns:app="http://schemas.android.com/apk/res/com.klxair.directionalcarouseldemo"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      android:gravity="center"
      android:orientation="vertical" >

      <com.klxair.ui.view.viewpager.directionalcarousel.fglayout.ForegroundLinearLayout
          android:id="@+id/page_content"
          android:layout_width="@dimen/page_content_width"
          android:layout_height="@dimen/page_content_height"
          android:clickable="true"
          android:foreground="@drawable/item_selector"
          android:gravity="center" >

          <com.klxair.ui.view.imageview.RoundImageView
              android:id="@+id/iv_pho"
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:scaleType="centerCrop"
              android:src="@drawable/ic_launcher"
              app:type="round" >
          </com.klxair.ui.view.imageview.RoundImageView>
      </com.klxair.ui.view.viewpager.directionalcarousel.fglayout.ForegroundLinearLayout>

  </com.klxair.ui.view.viewpager.directionalcarousel.page.PageLayout>
```

#### MyPageFragment类

* 说明

  （ImageUtils.setImagePic(iv_pho, pageItem.getTitle(), null);网络加载图片工具，项目里也没有的，如果你们自己用的话替换成自己长用的网络加载图片类就可以了，我这个是基于Picasa写的。如果有朋友想要这个工具类的话，也可以联系我）

```java
  public class MyPageFragment extends PageFragment<MyPageItem> {
    @Override
    public View setupPage(PageLayout pageLayout, MyPageItem pageItem) {
        View pageContent = pageLayout.findViewById(R.id.page_content);
        RoundImageView iv_pho = (RoundImageView) pageContent.findViewById(R.id.iv_pho);
        ImageUtils.setImagePic(iv_pho, pageItem.getTitle(), null);
        return pageContent;
    }
  }
```

#### MyPageItem类 

* 说明

```java
 public class MyPageItem implements Parcelable {
    private String mTitle;

    public MyPageItem(String title) {
        mTitle = title;
    }

    private MyPageItem(Parcel in) {
        mTitle = in.readString();
    }

    public String getTitle() {
        return mTitle;
    }

    public static final Parcelable.Creator<MyPageItem> CREATOR =
            new Parcelable.Creator<MyPageItem>() {
                @Override
                public MyPageItem createFromParcel(Parcel in) {
                    return new MyPageItem(in);
                }

                @Override
                public MyPageItem[] newArray(int size) {
                    return new MyPageItem[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mTitle);
    }
}
```

###感谢大家支持！


[我的CSDN博客](http://blog.csdn.net/ls498297458/article/details/52938606)
