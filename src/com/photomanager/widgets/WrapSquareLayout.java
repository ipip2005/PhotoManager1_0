package com.photomanager.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class WrapSquareLayout extends RelativeLayout {
    public WrapSquareLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
 
    public WrapSquareLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    public WrapSquareLayout(Context context) {
        super(context);
    }
 
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // For simple implementation, or internal size is always 0.
        // We depend on the container to specify the layout size of
        // our view. We can't really know what it is since we will be
        // adding and removing different arbitrary views and do not
        // want the layout to change as this happens.
    	/*int childCount = getChildCount() ;
    	if (childCount > 1){
    		throw new IllegalStateException("onMeasure() Wrap Layout can only have one child view");
    	}
    	View child = getChildAt(0);
    	child.measure(50, 50);*/
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
 
        // Children are just made to fill our space.
        //heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}

