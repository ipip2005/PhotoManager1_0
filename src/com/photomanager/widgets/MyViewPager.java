package com.photomanager.widgets;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class MyViewPager extends ViewPager {

	public MyViewPager(Context context) {
		super(context);
		init();
		// TODO Auto-generated constructor stub
	}

	public MyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	private float xDown;
	private float xMove;
	private float yDown;
	private float yMove;
	private boolean viewPagerScrolling = false;
	private boolean fatherScrolling = false;
	private void init(){
		
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (this.getChildCount() < 4)
			return false;
		return super.onTouchEvent(ev);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if (ev.getPointerCount() >= 2)
			return false;

		switch (ev.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			xDown = ev.getRawX();
			yDown = ev.getRawY();
			fatherScrolling = false;
			break;
		case MotionEvent.ACTION_MOVE:
			xMove = ev.getRawX();
			yMove = ev.getRawY();
			if (fatherScrolling) {
				return false;
			}
			if (viewPagerScrolling) {
				return super.dispatchTouchEvent(ev);
			}
			float dx = Math.abs(xMove - xDown), dy = Math.abs(yMove - yDown);
			if (dx > 3 && dx > dy && this.getChildCount() >= 4) {
				this.getParent().requestDisallowInterceptTouchEvent(true);
				viewPagerScrolling = true;
			} else if (dy > 3 && dy > dx) {
				fatherScrolling = true;
				return false;
			} else
				return false;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			viewPagerScrolling = false;
			if (ev.getPointerCount() == 1)
				this.getParent().requestDisallowInterceptTouchEvent(false);
			break;
		}
		return super.dispatchTouchEvent(ev);
	}
}
