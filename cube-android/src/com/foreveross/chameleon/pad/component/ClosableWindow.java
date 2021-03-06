package com.foreveross.chameleon.pad.component;

import android.R.style;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.csair.impc.R;


/**
 * @author fengweili [功能点] 1.可关�?2.可缩�?3.可拖 �?
 */
public class ClosableWindow extends LinearLayout {

	/** 标题�? **/
	private TitleArea titleArea = null;
	/** 内容�? **/
	private ContentArea contentArea = null;
	/** 是否�?���? **/
	private boolean isMax = false;
	/** 标题栏高�? **/
	private static final int TITLE_BAR_HEIGHT = 50;

//	public ClosableWindow(Context context, AttributeSet attrs, int defStyle) {
////		super(context, attrs, defStyle);
//		afterCreation();
//		attrsSet(attrs);
//	}

	public ClosableWindow(Context context, AttributeSet attrs) {
		super(context, attrs);
		afterCreation();
		attrsSet(attrs);
	}

	public void attrsSet(AttributeSet attrs) {
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs,
				R.styleable.ClosableWindow);
		String windowTitle = typedArray
				.getString(R.styleable.ClosableWindow_title);
		this.setTitle(windowTitle);
		int content_id = typedArray.getInt(
				R.styleable.ClosableWindow_content_window_id, 0);
		contentArea.setId(content_id);
	}
	private View bindView;
	public ClosableWindow(Context context,View bindView) {
		super(context);
		this.bindView = bindView;
		afterCreation();
	}


	public void emptyBG(){
//		this.setBackground(null);
		this.setBackgroundColor(Color.WHITE);
	}

	public int getScreenWidth() {
		
		DisplayMetrics dm = new DisplayMetrics();
		// 取得窗口属性
		WindowManager mWm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
		mWm.getDefaultDisplay().getMetrics(dm);
		// 窗口的宽度
		int screenWidth = dm.widthPixels;
		return screenWidth;
	}

	private void afterCreation() {
		this.setOrientation(LinearLayout.VERTICAL);
		this.setBackgroundResource(R.drawable.window_shadow);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		this.setLayoutParams(params);
		this.setOnLongClickListener(new OnLongClickListener() {
			final Drawable shadow = new ColorDrawable(Color.LTGRAY);

			@Override
			public boolean onLongClick(View v) {
//				ClosableWindow.this.startDrag(null,
//						new View.DragShadowBuilder() { // 定义一个回调方法，用于把拖动阴影的大小和触摸点位置返回给系统
//							@Override
//							public void onProvideShadowMetrics(Point size,
//									Point touch) {
//
//								// 定义本地变量
//								int width, height;
//
//								// 把阴影的宽度设为原始View的一半
//								width = ClosableWindow.this.getWidth() / 2;
//
//								// 把阴影的高度设为原始View的一半
//								height = ClosableWindow.this.getHeight() / 2;
//
//								// 拖动阴影是一个ColorDrawable对象。
//								// 下面把它设为与系统给出的Canvas一样大小。这样，拖动阴影将会填满整个Canvas。
//								shadow.setBounds(0, 0, width, height);
//
//								// 设置长宽值，通过size参数返回给系统。
//								size.set(width, height);
//
//								// 把触摸点的位置设为拖动阴影的中心
//								touch.set(width / 2, height / 2);
//							}
//
//							// 定义回调方法，用于在Canvas上绘制拖动阴影，Canvas由系统根据onProvideShadowMetrics()传入的尺寸参数创建。
//							@Override
//							public void onDrawShadow(Canvas canvas) {
//								// 在系统传入的Canvas上绘制ColorDrawable
//								shadow.draw(canvas);
//							}
//						}, null, 0);
				return true;
			}
		});
		titleArea = new TitleArea(getContext());
		titleArea.setVisibility(View.GONE);
		titleArea.setBackgroundResource(R.drawable.top_round_corners);
		contentArea = new ContentArea(getContext());

		this.addView(titleArea, LayoutParams.MATCH_PARENT, TITLE_BAR_HEIGHT);
		this.addView(contentArea, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

	}

	public void setTitle(String title) {
		titleArea.setTitle(title);
	}

//	/**
//	 * 放大或缩小窗�?
//	 */
	public void scale() {
		if (isMax) {
			min();
		} else {
			max();
		}
		isMax = !isMax;
	}
//
//	// TODO[fengweili]确认此种方式是否能动态改变尺�?
	private void max() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		this.setLayoutParams(params);
	}
//
//	// TODO[fengweili]确认此种方式是否能动态改变尺�?
//	/**
//	 * 缩小窗口并位于布�?���?
//	 */
	private void min() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				this.getWidth() / 2, LayoutParams.MATCH_PARENT);
		this.setLayoutParams(params);
	}

	/**
	 * 关闭窗口
	 */
	public void close() {
		AlphaAnimation myAnimation_Alpha = new AlphaAnimation(1f, 0.1f);
		myAnimation_Alpha.setDuration(1000);

		myAnimation_Alpha.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				bindView.setVisibility(View.GONE);		
			}
		});
		this.startAnimation(myAnimation_Alpha);
//		this.invalidate();

	}

	//
	public void addContentView(View child) {
		contentArea.addView(child);
	}

//	@Override
//	public boolean onDragEvent(DragEvent event) {
//		return this.onDragEvent(event);
//	}

	class TitleArea extends RelativeLayout implements OnTouchListener,
			OnGestureListener {

		private TextView tv_title = null;
		private Button btn_scale = null;
		private Button btn_close = null;

		public void setTitle(String title) {
			tv_title.setText(title);

		}

		private void innerCreation() {
			this.setOnTouchListener(this);

			tv_title = new TextView(getContext());
			tv_title.setTextAppearance(getContext(),
					style.TextAppearance_Medium);
			tv_title.setTextColor(Color.WHITE);
			btn_scale = new Button(getContext());
			btn_scale.setBackgroundResource(R.drawable.scale);
			btn_scale.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					scale();
				}
			});
			btn_close = new Button(getContext());
			btn_close.setId(100);
			btn_close.setBackgroundResource(R.drawable.close);
			btn_close.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					close();
				}
			});
			RelativeLayout.LayoutParams centerInHV = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, TITLE_BAR_HEIGHT);
			centerInHV.addRule(RelativeLayout.CENTER_IN_PARENT,
					RelativeLayout.TRUE);
			
			RelativeLayout.LayoutParams leftOfParent = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			leftOfParent.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
					RelativeLayout.TRUE);
			leftOfParent.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
			RelativeLayout.LayoutParams leftOfCloseBtn = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			leftOfCloseBtn.addRule(RelativeLayout.LEFT_OF, btn_close.getId());
			tv_title.setSingleLine(true);
			tv_title.setGravity(Gravity.CENTER);
			
			this.addView(tv_title, centerInHV);
//			this.addView(btn_scale, leftOfCloseBtn);
			this.addView(btn_close, leftOfParent);
			

		}

		public TitleArea(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			innerCreation();
		}

		public TitleArea(Context context, AttributeSet attrs) {
			super(context, attrs);
			innerCreation();
		}

		public TitleArea(Context context) {
			super(context);
			innerCreation();
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		private GestureDetector mGestureDetector = new GestureDetector(
				getContext(), this);
		private int verticalMinDistance = 20;
		private int minVelocity = 0;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (e1.getX() - e2.getX() > 5 && Math.abs(velocityX) > minVelocity) {
				//
			} else if (e2.getX() - e1.getX() > verticalMinDistance
					&& Math.abs(velocityX) > minVelocity) {
				close();
			}

			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {

		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return false;
		}
	}

	class ContentArea extends LinearLayout implements OnTouchListener {

		public ContentArea(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs);
			setOnTouchListener(this);

		}

		public ContentArea(Context context, AttributeSet attrs) {
			super(context, attrs);
			setOnTouchListener(this);
		}

		public ContentArea(Context context) {
			super(context);
			setOnTouchListener(this);

		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return this.onTouchEvent(event);
		}

	}

}
