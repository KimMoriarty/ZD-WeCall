package com.wecall.contacts.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.wecall.contacts.constants.Constants;

/**
 * ����ҳ�࣬ʵ�ֶ�ҳ����
 * 
 * @author xiaoxin
 *
 * 2015-4-1
 */
public class SlidingPage extends HorizontalScrollView {

//	private static final String TAG = "SlidingPage";
	
	//ԭ����װ������ֵ�ˮƽ����
	private LinearLayout mWapper;
	//��˵�
	private ViewGroup mMenu;
	//����������
	private ViewGroup mContent;
	//�Ҳ˵�
	private ViewGroup mRightMenu;
	//��Ļ���
	private int mScreenWidth;
	//�˵����
	private int mMenuWidth;
	//�˵�������������Ŀ�Ȳ�
	private int mMenuRightPadding = 100;

	private boolean once;

	public SlidingPage(Context context) {
		this(context, null);
	}

	public SlidingPage(Context context, AttributeSet attrs) {
		super(context, attrs);
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		mScreenWidth = outMetrics.widthPixels;

		// ��dpת��Ϊpx
		mMenuRightPadding = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, Constants.INIT_MENU_PADDING,
				context.getResources().getDisplayMetrics());
	}

	public SlidingPage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/* �̳и����onMeasure���������������ӿؼ��Ŀ�Ⱥ͸߶ȵ���Ϣ
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		if (!once) {
			mWapper = (LinearLayout) getChildAt(0);
			mMenu = (ViewGroup) mWapper.getChildAt(0);
			mContent = (ViewGroup) mWapper.getChildAt(1);
			mRightMenu = (ViewGroup) mWapper.getChildAt(2);
			
			mMenuWidth = mMenu.getLayoutParams().width = mScreenWidth
					- mMenuRightPadding;
			mContent.getLayoutParams().width = mScreenWidth;
			mRightMenu.getLayoutParams().width = mMenuWidth;
			once = true;
		}

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	// ͨ������ƫ������menu����
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed) {
			this.scrollTo(mMenuWidth, 0);
		}
	}

	@SuppressLint("ClickableViewAccessibility") @Override
	public boolean onTouchEvent(MotionEvent ev) {
		int action = ev.getAction();

//		SideBar sideBar = (SideBar) mContent.findViewById(R.id.sidebar);
//		sideBar.dispatchTouchEvent(ev);
//		Log.v(TAG, "Move��"+action);
		
		switch (action) {
		case MotionEvent.ACTION_UP:
			int scrollX = getScrollX();
			Log.v("TAG", "a="+mMenuWidth+"b="+mScreenWidth+"c="+scrollX);
			if(scrollX<mMenuWidth/2){
				this.smoothScrollTo(0, 0);
			}else if(scrollX>mMenuWidth/5+mScreenWidth){
				this.smoothScrollTo(mMenuWidth+mScreenWidth, 0);
			}else {
				this.smoothScrollTo(mMenuWidth, 0);
			}
			return true;
		default:
			break;
		}

		return super.onTouchEvent(ev);
	}
}
