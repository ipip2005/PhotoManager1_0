package com.example.photomanager1_0;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class MyViewPager extends ViewPager{
	public MyViewPager(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public MyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	private float xDown;// 记录手指按下时的横坐标。  
    private float xMove;// 记录手指移动时的横坐标。  
    private float yDown;// 记录手指按下时的纵坐标。  
    private float yMove;// 记录手指移动时的纵坐标。  
    private boolean viewPagerScrolling = false;
    private boolean fatherScrolling = false;
    @Override
    public boolean onTouchEvent(MotionEvent ev){
    	if (this.getChildCount() < 4) return false;
    	return super.onTouchEvent(ev);
    }
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		//Log.w("ViewPager", "dispatch "+ev.getRawX()+" "+ev.getRawY()+" "+ev.getPointerCount() + " "+fatherScrolling);
		if (ev.getPointerCount()>=2) return false;
		switch (ev.getAction() & MotionEvent.ACTION_MASK){
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			//Log.w("ViewPager", "dispatch down");
			xDown = ev.getRawX();
			yDown = ev.getRawY();
			fatherScrolling = false;
			break;
		case MotionEvent.ACTION_MOVE:
			//Log.w("ViewPager", "dispatch move");
			xMove = ev.getRawX();
			yMove = ev.getRawY();
			if (fatherScrolling){
				return false;
			} 
			if (viewPagerScrolling){	
				return super.dispatchTouchEvent(ev);
			}
			
			if (Math.abs(yMove - yDown) < 10 && Math.abs(xMove - xDown) > 3){
				this.getParent().requestDisallowInterceptTouchEvent(true);
				viewPagerScrolling = true;
			} else
			if (Math.abs(yMove - yDown) >= 10){
				fatherScrolling = true;
				return false;
			} else
				return false;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			//Log.w("ViewPager", "dispatch up");
			viewPagerScrolling = false;
			if (ev.getPointerCount() == 1 )this.getParent().requestDisallowInterceptTouchEvent(false);
			break;
		}
		return super.dispatchTouchEvent(ev);
	}
}
