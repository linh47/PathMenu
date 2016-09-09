package com.ai.ipu.ipu_pathmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * 自定义菜单
 * 
 * @author 何凌波
 *
 */
public class PathMenu extends RelativeLayout {
	private PathMenuLayout mPathMenuLayout;
	private ImageView mHintView;// 中心按钮显示图片

	public PathMenu(Context context) {
		super(context);
		init(context);
	}

	public PathMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		applyAttrs(attrs);
	}

	/**
	 * 初始化中心按钮布局，加载布局文件，设置触摸事件
	 * 
	 * @param context
	 */
	private void init(Context context) {
		LayoutInflater li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		li.inflate(R.layout.float_menu, this);
		
	 

		mPathMenuLayout = (PathMenuLayout) findViewById(R.id.item_layout);

		final ViewGroup controlLayout = (ViewGroup) findViewById(R.id.control_layout);
		controlLayout.setClickable(true);
		controlLayout.setOnTouchListener(new OnTouchListener() {
			// 点击展开

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					
					mHintView
							.startAnimation(createHintSwitchAnimation(mPathMenuLayout
									.isExpanded()));
					mPathMenuLayout.switchState(true);
				}
				
				return false;
			}
		});

		mHintView = (ImageView) findViewById(R.id.control_hint);
	}

	/**
	 * 应用自定义属性，设置弧度、子菜单项大小
	 * 
	 * @param attrs
	 */
	private void applyAttrs(AttributeSet attrs) {
		if (attrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(attrs,
					R.styleable.ArcLayout, 0, 0);

			float fromDegrees = a.getFloat(R.styleable.ArcLayout_fromDegrees,
					PathMenuLayout.DEFAULT_FROM_DEGREES);
			float toDegrees = a.getFloat(R.styleable.ArcLayout_toDegrees,
					PathMenuLayout.DEFAULT_TO_DEGREES);
			mPathMenuLayout.setArc(fromDegrees, toDegrees);

			int defaultChildSize = mPathMenuLayout.getChildSize();

			int newChildSize = a.getDimensionPixelSize(
					R.styleable.ArcLayout_childSize, defaultChildSize);
			mPathMenuLayout.setChildSize(newChildSize);

			a.recycle();
		}
	}

	/**
	 * 添加子菜单项和对应的点击事件
	 * 
	 * @param item
	 * @param listener
	 */
	public void addItem(View item, OnClickListener listener) {
		mPathMenuLayout.addView(item);
		item.setOnClickListener(getItemClickListener(listener));
	}

	/**
	 * 子菜单项的点击事件
	 * 
	 * @param listener
	 * @return
	 */
	private OnClickListener getItemClickListener(final OnClickListener listener) {
		return new OnClickListener() {
			// 点击,child放大,其他child消失
			@Override
			public void onClick(final View viewClicked) {
				Animation animation = bindItemAnimation(viewClicked, true, 400);
				animation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {

					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						postDelayed(new Runnable() {

							@Override
							public void run() {
								itemDidDisappear();
							}
						}, 0);
					}
				});
				// child 被点击的时候，自身放大，其他child消失的效果
				final int itemCount = mPathMenuLayout.getChildCount();
				for (int i = 0; i < itemCount; i++) {
					View item = mPathMenuLayout.getChildAt(i);
					if (viewClicked != item) {
						bindItemAnimation(item, false, 300);
					}
				}
				// 中心控制点动画 旋转
				mPathMenuLayout.invalidate();
				mHintView.startAnimation(createHintSwitchAnimation(true));

				if (listener != null) {
					listener.onClick(viewClicked);
				}
			}
		};
	}

	/**
	 * 绑定子菜单项动画
	 * 
	 * @param child
	 * @param isClicked
	 * @param duration
	 * @return
	 */
	private Animation bindItemAnimation(final View child,
			final boolean isClicked, final long duration) {
		Animation animation = createItemDisapperAnimation(duration, isClicked);
		child.setAnimation(animation);

		return animation;
	}

	/**
	 * 子菜单项关闭时消失
	 */
	private void itemDidDisappear() {
		final int itemCount = mPathMenuLayout.getChildCount();
		for (int i = 0; i < itemCount; i++) {
			View item = mPathMenuLayout.getChildAt(i);
			item.clearAnimation();
		}

		mPathMenuLayout.switchState(false);
	}

	/**
	 * 子菜单消失动画
	 * 
	 * @param duration
	 * @param isClicked
	 * @return
	 */
	private static Animation createItemDisapperAnimation(final long duration,
			final boolean isClicked) {
		AnimationSet animationSet = new AnimationSet(true);
		// 放大缩小动画，isClicked 为true,放大两倍；为false,缩小至0
		animationSet.addAnimation(new ScaleAnimation(1.0f, isClicked ? 2.0f
				: 0.0f, 1.0f, isClicked ? 2.0f : 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f));
		// Alpha改为0，child消失
		animationSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));

		animationSet.setDuration(duration);
		animationSet.setInterpolator(new DecelerateInterpolator());
		animationSet.setFillAfter(true);

		return animationSet;
	}

	/**
	 * 中心按钮点击旋转45度动画
	 * 
	 * @param expanded
	 * @return
	 */
	private static Animation createHintSwitchAnimation(final boolean expanded) {
		Animation animation = new RotateAnimation(expanded ? 45 : 0,
				expanded ? 0 : 45, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setStartOffset(0);
		animation.setDuration(100);
		animation.setInterpolator(new DecelerateInterpolator());
		animation.setFillAfter(true);

		return animation;
	}
}
