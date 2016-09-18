package com.ai.ipu.ipu_pathmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
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
public class PathMenu extends RelativeLayout implements OnTouchListener {
	private PathMenuLayout mPathMenuLayout;
	private ImageView mHintView;// 中心按钮显示图片
	private WindowManager mWindowManager;// 当前view的窗口管理器
	// WindowMananger的params，控制这个值可以将自定义的view设置到窗口管理器中
	private WindowManager.LayoutParams mWmParams;
	private float mTouchStartX;// 记录首次按下的位置x
	private float mTouchStartY;// 记录首次按下的位置y

	private int mScreenWidth;// 屏幕宽度
	private int mScreenHeight;// 屏幕高度
	private boolean mDraging;// 是否拖动中
	private boolean mExpanded = false;// 子菜单是否展开
	private Context mContext;

	private int position;// 按钮的位置
	private int LEFT_TOP = 1;
	private int CENTER_TOP = 2;
	private int RIGHT_TOP = 3;
	private int LEFT_CENTER = 4;
	private int CENTER = 5;
	private int RIGHT_CENTER = 6;
	private int LEFT_BOTTOM = 7;
	private int CENTER_BOTTOM = 8;
	private int RIGHT_BOTTOM = 9;


	public PathMenu(Context context) {
		super(context);
		this.mContext = context;
		init(mContext);
	}

	public PathMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init(mContext);
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
		mWindowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);// 获取系统的窗口服务
		DisplayMetrics dm = new DisplayMetrics();
		mWindowManager.getDefaultDisplay().getMetrics(dm);
		mScreenWidth = dm.widthPixels;// 根据当前屏幕信息拿到屏幕的宽高
		mScreenHeight = dm.heightPixels;

		this.mWmParams = new WindowManager.LayoutParams();// 获取窗口参数

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mWmParams.type = WindowManager.LayoutParams.TYPE_TOAST;// 等于API19或API19以下需要指定窗口参数type值为TYPE_TOAST才可以作为悬浮控件显示出来
		} else {
			mWmParams.type = WindowManager.LayoutParams.TYPE_PHONE;// API19以上侧只需指定为TYPE_PHONE即可
		}
		mWmParams.format = PixelFormat.RGBA_8888;// 当前窗口的像素格式为RGBA_8888,即为最高质量

		// NOT_FOCUSABLE可以是悬浮控件可以响应事件，LAYOUT_IN_SCREEN可以指定悬浮球指定在屏幕内，部分虚拟按键的手机，虚拟按键隐藏时，虚拟按键的位置则属于屏幕内，此时悬浮球会出现在原虚拟按键的位置
		mWmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		// 默认指定位置在屏幕的左上方，可以根据需要自己修改
		mWmParams.gravity = Gravity.LEFT | Gravity.TOP;
		position = 1;
		// 默认指定的横坐标为屏幕一半
		mWmParams.x = mScreenWidth / 2;
		// 默认指定的纵坐标为屏幕高度的一半，这里只是大概约束，因为上的flags参数限制，悬浮球不会出现在屏幕外
		mWmParams.y = mScreenHeight / 2;

		// 宽度指定为内容自适应
		mWmParams.width = LayoutParams.WRAP_CONTENT;
		mWmParams.height = LayoutParams.WRAP_CONTENT;

		mPathMenuLayout = (PathMenuLayout) findViewById(R.id.item_layout);
		final ViewGroup controlLayout = (ViewGroup) findViewById(R.id.control_layout);
		controlLayout.setClickable(true);
		controlLayout.setOnTouchListener(this);

		mHintView = (ImageView) findViewById(R.id.control_hint);
		mWindowManager.addView(this, mWmParams);
		

		controlLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mDraging) {
					mHintView
							.startAnimation(createHintSwitchAnimation(mPathMenuLayout
									.isExpanded()));
					mPathMenuLayout.switchState(true);
				}
			}
		});
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

	/**
	 * 触摸事件
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float x = (int) event.getRawX();
		float y = (int) event.getRawY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchStartX = event.getX();
			mTouchStartY = event.getY();
			mWmParams.alpha = 1f;
			mWindowManager.updateViewLayout(this, mWmParams);
			mDraging = false;
			break;

		case MotionEvent.ACTION_MOVE:
			float mMoveStartX = event.getX();
			float mMoveStartY = event.getY();
				if (Math.abs(mTouchStartX - mMoveStartX) > 2

				&& Math.abs(mTouchStartY - mMoveStartY) > 2) {
					mDraging = true;
					mWmParams.x = (int) (x - mTouchStartX);
					mWmParams.y = (int) (y - mTouchStartY);
					mWindowManager.updateViewLayout(this, mWmParams);
					return false;
				}
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			int wmX = mWmParams.x;
			int wmY = mWmParams.y;
				if (wmX <= mScreenWidth / 4) // 左边 竖区域
				{
					if (wmY <= mScreenHeight / 4) {
						position = LEFT_TOP;// 左上
					} else if (wmY >= mScreenHeight / 4
							&& wmY <= mScreenHeight * 3 / 4) {
						position = LEFT_CENTER;// 左中
					} else if (wmY >= mScreenHeight * 3 / 4) {
						position = LEFT_BOTTOM;// 左下
					}
				} else if (wmX >= mScreenWidth / 4
						&& wmX <= mScreenWidth * 3 / 4)// 中间
														// 竖区域
				{
					if (wmY <= mScreenHeight / 4) {
						position = CENTER_TOP;// 中上
					} else if (wmY >= mScreenHeight / 4
							&& wmY <= mScreenHeight * 3 / 4) {
						position = CENTER;// 中
					} else if (wmY >= mScreenHeight * 3 / 4) {
						position = CENTER_BOTTOM;// 中下
					}
				}

				else if (wmX >= mScreenWidth * 3 / 4)// 右边竖区域
				{
					if (wmY <= mScreenHeight / 4) {
						position = RIGHT_TOP;// 上右
					} else if (wmY >= mScreenHeight / 4
							&& wmY <= mScreenHeight * 3 / 4) {
						position = RIGHT_CENTER;// 中右
					} else if (wmY >= mScreenHeight * 3 / 4) {
						position = RIGHT_BOTTOM;// 下右
					}
				}
				refreshPathMenu(position);

				mWindowManager.updateViewLayout(this, mWmParams);
				mTouchStartX = mTouchStartY = 0;
			break;
		}
		return false;
	}

	/**
	 * 根据按钮位置改变子菜单方向
	 * 
	 * @param position
	 */
	private void refreshPathMenu(int position) {
		if (position == LEFT_TOP) {// 上左
			mPathMenuLayout.setArc(0, 90);
		} else if (position == CENTER_TOP) {// 上中
			mPathMenuLayout.setArc(0, 180);
		} else if (position == RIGHT_TOP) {// 上右
			mPathMenuLayout.setArc(90, 180);
		} else if (position == LEFT_CENTER) {// 中左
			mPathMenuLayout.setArc(270, 270 + 180);
		} else if (position == CENTER) {// 中
			mPathMenuLayout.setArc(0, 360);
		} else if (position == RIGHT_CENTER) {// 中右
			mPathMenuLayout.setArc(90, 270);
		} else if (position == LEFT_BOTTOM) { // 下左
			mPathMenuLayout.setArc(270, 360);
		} else if (position == CENTER_BOTTOM) {// 下中
			mPathMenuLayout.setArc(180, 360);
		} else if (position == RIGHT_BOTTOM) {// 下右
			mPathMenuLayout.setArc(180, 270);
		}

	}

	
	public void hidePathMenu()
	{
		try {
			 setVisibility(View.GONE);
		} catch (final IllegalArgumentException e) {
			Log.e("PathMenu", "hidePathMenu error!");
		}
	}
}
