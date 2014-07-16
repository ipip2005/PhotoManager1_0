package com.example.photomanager1_0;

import java.util.ArrayList;

import com.example.photomanager1_0.JazzyViewPager.TransitionEffect;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author ipip
 *  2014年7月16日下午2:50:04
 */
public class TimelineActivity extends Activity implements OnItemClickListener {
	static public ArrayList<PicInfo> PicInfoList;
	static public DataGain dg;
	private PictureAdapter mAdapter;
	private MyListView listView = null;
	// The zoom level of the listView
	private int granularity;
	// The handler which can receive informations from class DataGain.java and
	// refresh UI.
	private static Handler handler;
	// The origin y of mid-point of the two fingers.
	private double dst = -1;
	// The new y of mid-point of the two fingers.
	private double ndst;
	// The mapping set, to get the real index in PicInfoList.
	private ArrayList<ArrayList<Integer>> mSet;
	// The Animation for listView.
	private Animation myLayoutAnimation, myAnimation;
	private LayoutAnimationController lac;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		preData();
		listView = new MyListView(this);
		LinearLayout ll = (LinearLayout)findViewById(R.id.timeline_ll);
		ll.addView(listView);
		mAdapter = new PictureAdapter(this);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(this);
		myLayoutAnimation = new AlphaAnimation(0.8f, 1.0f);
		myLayoutAnimation.setDuration(500);
		lac = new LayoutAnimationController(myLayoutAnimation);
		lac.setDelay(0.1f);
		listView.setLayoutAnimation(lac);

		myAnimation = new ScaleAnimation(0.3f, 1.1f, 0.3f, 1.1f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		myAnimation.setDuration(250);
		listView.setAnimation(myAnimation);
	}

	/*
	 * preprocessing initialize DataGain.class and set callback for loading
	 * bitmaps.
	 */
	private void preData() {
		handler = new Handler() {
			public void handleMessage(Message msg) {
				mAdapter.notifyDataSetChanged();
			}
		};
		dg = new DataGain(getContentResolver(), TimelineActivity.this, handler);
		PicInfoList = dg.getPicInfoList();
		granularity = 2;
		mSet = dg.getSet(granularity);
		// Log.i("photo",""+PicInfoList.size());
	}

	private class PictureAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public PictureAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mSet.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return mSet.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			if (convertView == null) {
				int lid;
				holder = new ViewHolder();
				lid = R.layout.simple_list_item;
				convertView = mInflater.inflate(lid, null);
				holder.title = (TextView) convertView
						.findViewById(R.id.item_title);
				holder.text = (TextView) convertView
						.findViewById(R.id.item_more);
				holder.viewPager = (MyViewPager) convertView
						.findViewById(R.id.timeline_view_pager);
				holder.holder_id = position;
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.viewPager.removeAllViews();
			holder.holder_id = position;
			holder.viewPager.setAdapter(new MyViewPagerAdapter(
					TimelineActivity.this, mSet.get(position),
					holder.viewPager));
			// holder.jViewPager.setFadeEnabled(true);
			// holder.jViewPager.setTransitionEffect(TransitionEffect.Tablet);
			holder.viewPager.setPageMargin(30);
			holder.viewPager.setOffscreenPageLimit(5);
			// Log.w("ViewPager",
			// ""+mSet.get(position).size()+" "+holder.holder_id + " "+
			// position);
			holder.viewPager.setCurrentItem(0);
			String openFlag = "";
			if (mSet.get(position).size() > 1)
				openFlag = ">>>";
			holder.title.setText(dealTitle(mSet.get(position)));
			holder.text.setText(openFlag);
			return convertView;
		}
	}

	private class MyViewPagerAdapter extends PagerAdapter {
		private Context mContext;
		private ArrayList<Integer> set;
		private ViewPager mViewPager;

		public MyViewPagerAdapter(Context context, ArrayList<Integer> set,
				ViewPager viewPager) {
			mContext = context;
			this.set = set;
			mViewPager = viewPager;
		}

		@Override
		public float getPageWidth(int position) {
			return 0.25f;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return set.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == (View) arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ImageView iv = new ImageView(mContext);
			if (PicInfoList.get(set.get(position)).bitmap == null)
				dg.getData(set.get(position));
			else
				iv.setImageBitmap(PicInfoList.get(set.get(position)).bitmap);
			((ViewPager) container).addView(iv, 0);
			return iv;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}

	private String dealTitle(ArrayList<Integer> list) {
		String st = PicInfoList.get(list.get(0)).title;
		String ed = PicInfoList.get(list.get(list.size() - 1)).title;
		if (st.equals(ed))
			return st;
		else
			return ed + "-" + st;
	}

	private double measureFingers(MotionEvent event) {
		double x = event.getX(0) - event.getX(1);
		double y = event.getY(0) - event.getY(1);
		return Math.sqrt(x * x + y * y);
	}

	private void upGranularity() {
		if (granularity == 4 || mSet.size() == 1)
			return;
		granularity++;
		mSet = dg.getSet(granularity);
		mAdapter.notifyDataSetChanged();
		listView.setSelection(0);
		listView.startLayoutAnimation();
		listView.clearAnimation();
		listView.setAnimation(myAnimation);
		listView.startAnimation(myAnimation);
	}

	private void downGranularity(int index) {
		granularity--;
		if (granularity == 1) {
			mAdapter.notifyDataSetChanged();
			listView.setSelection(mSet.get(index).get(0));
		} else {
			int id = mSet.get(index).get(0);
			mSet = dg.getSet(granularity);
			mAdapter.notifyDataSetChanged();
			for (int i = 1; i <= mSet.size(); i++) {
				if (i == mSet.size())
					listView.setSelection(i - 1);
				else if (PicInfoList.get(id).mdate.after(PicInfoList.get(mSet
						.get(i).get(0)).mdate)) {
					listView.setSelection(i - 1);
					break;
				}
			}
		}
		listView.startLayoutAnimation();
		listView.clearAnimation();
		listView.setAnimation(myAnimation);
		listView.startAnimation(myAnimation);
	}
	/*
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		Log.w("ViewPager", "onTouch event.getPointerCount()"+event.getPointerCount());
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			if (event.getPointerCount() == 2)
				dst = measureFingers(event);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			
			if (event.getPointerCount() == 1 && ndst < dst) {
				upGranularity();
				dst = -1;
			}
			break;
		
		case MotionEvent.ACTION_MOVE:
			if (event.getPointerCount() >= 2) {
				ndst = measureFingers(event);
			}
			break;
		}
		if (event.getPointerCount() >= 2)
			return true;
		return super.onTouchEvent(event);
	}*/
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#dispatchTouchEvent(android.view.MotionEvent)
	
	
/*
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev){
		Log.w("ViewPager", ""+ev.getPointerCount()+" "+isScaling);
		
		switch (ev.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			Log.w("ViewPager", "Top dispatch down " +ev.getPointerCount());
			if (ev.getPointerCount() == 1){
				xDown = ev.getRawX();
				yDown = ev.getRawY();
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			Log.w("ViewPager", "Top dispatch up");
			if (ev.getPointerCount()==1) {
				/*if (isScaling){
					if (isTwoPointer) isTwoPointer = false; else
						isScaling = false;
				}
				isScaling = false;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			Log.w("ViewPager", "Top dispatch move");
			xMove = ev.getRawX();
			yMove = ev.getRawY();
			break;
		}
		if (ev.getPointerCount() >= 2) {
			isScaling = true;
			//isTwoPointer = true;
			return true;
		}
		if (isScaling) return true;
		return super.dispatchTouchEvent(ev);
	}*/
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if (granularity == 1) {
			Intent intent = new Intent(TimelineActivity.this,
					ShowImageActivity.class);
			intent.putExtra("image", arg2);
			startActivity(intent);
		} else {
			if (mSet.get(arg2).size() == 1) {
				Intent intent = new Intent(TimelineActivity.this,
						ShowImageActivity.class);
				intent.putExtra("image", mSet.get(arg2).get(0));
				startActivity(intent);
				return;
			}
			this.downGranularity(arg2);
		}
	}
	private class MyListView extends ListView{
		private boolean isScaling = false;
		//private boolean isTwoPointer = false;
		private float xDown;// 记录手指按下时的横坐标。  
	    private float xMove;// 记录手指移动时的横坐标。  
	    private float yDown;// 记录手指按下时的纵坐标。  
	    private float yMove;// 记录手指移动时的纵坐标。  
		public MyListView(Context context) {
			super(context);
			this.setDivider(null);
			// TODO Auto-generated constructor stub
		}
		/*
		 * (non-Javadoc)
		 * @see android.widget.AbsListView#onTouchEvent(android.view.MotionEvent)
		 * 处理listView的触摸事件
		 */
		@Override
		public boolean onTouchEvent(MotionEvent ev){
			Log.w("ViewPager", "onTouch event.getPointerCount()"+ev.getPointerCount() + " " +dst+" "+ndst);
			switch (ev.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				if (ev.getPointerCount() == 2)
					dst = measureFingers(ev);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				
				if (ev.getPointerCount() == 2 && ndst < dst) {
					upGranularity();
					dst = -1;
				}
				break;
			
			case MotionEvent.ACTION_MOVE:
				if (ev.getPointerCount() >= 2) {
					ndst = measureFingers(ev);
				}
				break;
			}
			if (ev.getPointerCount() >= 2)
				return true;
			return super.onTouchEvent(ev);
		}
		/*
		 * 		(non-Javadoc)
		 * @see android.view.ViewGroup#dispatchTouchEvent(android.view.MotionEvent)
		 *  * listView事件分发规则：
		 * 状态：双指操作中isScaling.
		 * 指头全放 ：isScaling = false;
		 * 任何状态为true时截断调用onTouch，否则分发给子View
		 */
		@Override
		public boolean dispatchTouchEvent(MotionEvent ev){
			Log.w("ViewPager", ""+ev.getPointerCount()+" "+isScaling);
			
			switch (ev.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				Log.w("ViewPager", "Top dispatch down " +ev.getPointerCount());
				if (ev.getPointerCount() == 1){
					xDown = ev.getRawX();
					yDown = ev.getRawY();
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				Log.w("ViewPager", "Top dispatch up");
				if (ev.getPointerCount()==1) {
					/*if (isScaling){
						if (isTwoPointer) isTwoPointer = false; else
							isScaling = false;
					}*/
					isScaling = false;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				Log.w("ViewPager", "Top dispatch move");
				xMove = ev.getRawX();
				yMove = ev.getRawY();
				break;
			}
			if (ev.getPointerCount() >= 2) isScaling = true;
			if (isScaling) Log.w("ViewPager", "should be dealed"); else
				Log.w("ViewPager", "should not be dealed");
			if (isScaling) return true;
			return super.dispatchTouchEvent(ev);
		}
	}
	
}
