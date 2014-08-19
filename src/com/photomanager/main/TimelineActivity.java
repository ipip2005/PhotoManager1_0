package com.photomanager.main;

import java.util.ArrayList;
import java.util.Calendar;

import utils.DataGain;
import utils.DataGainUtil;
import utils.PicInfo;
import utils.ViewHolder;

import com.photomagner.widgets.MyRecyclerGallery;
import com.photomanager.adapters.MyRecyclerGalleryAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author ipip 2014年7月16日下午2:50:04
 */
public class TimelineActivity extends Activity implements OnItemClickListener {
	private DataGain dg;
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
	private Animation myLayoutAnimation, myAnimation, myAnimationB;
	private LayoutAnimationController lac;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		preData();
		listView = new MyListView(this);
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.timeline_rl);
		rl.addView(listView);
		mAdapter = new PictureAdapter(this);
		listView.setAdapter(mAdapter);
		listView.setLayoutParams(new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		listView.setOnItemClickListener(this);
		myLayoutAnimation = new AlphaAnimation(0.8f, 1.0f);
		myLayoutAnimation.setDuration(500);
		lac = new LayoutAnimationController(myLayoutAnimation);
		lac.setDelay(0.1f);
		listView.setLayoutAnimation(lac);
		listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		myAnimation = new ScaleAnimation(0.3f, 1.1f, 0.3f, 1.1f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		myAnimation.setDuration(250);

		myAnimationB = new ScaleAnimation(1.5f, 0.9f, 1.5f, 0.9f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		myAnimationB.setDuration(400);
	}

	/**
	 * pre-processing initialize DataGain.class and set callback for loading
	 * bitmaps.
	 */
	private void preData() {
		dg = DataGainUtil.getDataGainInstance(this, handler);
		granularity = 2;
		mSet = dg.getSet(granularity);
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
				holder.view = (MyRecyclerGallery) convertView
						.findViewById(R.id.timeline_recycler_view);
				holder.holder_id = position;
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.view.setAdapter(new MyRecyclerGalleryAdapter(
					TimelineActivity.this, mSet.get(position)));
			LinearLayoutManager llm = new LinearLayoutManager(
					TimelineActivity.this);
			llm.setOrientation(LinearLayoutManager.HORIZONTAL);
			holder.view.setLayoutManager(llm);
			String openFlag = "";
			if (mSet.get(position).size() > 1)
				openFlag = ">>>";
			holder.title.setText(dealTitle(mSet.get(position))+" ("+mSet.get(position).size()+")");
			holder.text.setText(openFlag);
			return convertView;
		}
	}
	/**
	 * adding spre-zero for numbers in calender.
	 * @param i
	 * @return
	 */
	private String addZero(int i) {

		if (i < 10)
			return "0" + i;
		return "" + i;
	}

	private String dealTitle(ArrayList<Integer> list) {
		String st = DataGainUtil.getDataGain().getPicInfoList()
				.get(list.get(0)).title;
		String ed = DataGainUtil.getDataGain().getPicInfoList()
				.get(list.get(list.size() - 1)).title;
		String ret = st;
		Calendar c = DataGainUtil.getDataGain().getPicInfoList()
				.get(list.get(0)).mdate;
		if (list.size() == 1) {
			ret += " " + addZero(c.get(Calendar.HOUR_OF_DAY)) + ":"
					+ addZero(c.get(Calendar.MINUTE));
			return ret;
		}
		if (!st.equals(ed))
			ret = ed + "-" + st;
		if (granularity == 4) {
			if (c.get(Calendar.YEAR) == Calendar.getInstance().get(
					Calendar.YEAR)) {
				ret = "今年 " + ret;
			} else
				ret = c.get(Calendar.YEAR) + "年 " + ret;
		}
		return ret;
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
		listView.setAnimation(myAnimationB);
		listView.startAnimation(myAnimationB);
	}

	private void downGranularity(int index) {
		granularity--;
		int id = mSet.get(index).get(0);
		mSet = dg.getSet(granularity);
		mAdapter.notifyDataSetChanged();
		for (int i = 0; i < mSet.size(); i++) {
			if (i == mSet.size() - 1 || id < mSet.get(i + 1).get(0)) {
				Log.w("ViewPager", "" + id + " " + i + " " + mSet.get(i).get(0));
				listView.setTag(i);
				new Handler().post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						listView.setSelection((Integer) listView.getTag());
					}

				});
				break;
			}
		}
		listView.startLayoutAnimation();
		listView.clearAnimation();
		listView.setAnimation(myAnimation);
		listView.startAnimation(myAnimation);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if (mSet.get(arg2).size() > 1)
			this.downGranularity(arg2);
	}

	/**
	 * 自定义listview 用于事件分发的处理。
	 * 
	 * @author ipip 2014年8月4日上午10:46:53
	 */
	private class MyListView extends ListView {
		private static final int MAX_OVERSCROLL_DISTANCE = 100;
		private int mMaxOverScrollDistance;

		public MyListView(Context context) {
			super(context);
			init();
			// TODO Auto-generated constructor stub
		}

		public MyListView(Context context, AttributeSet attrs) {
			super(context, attrs);
			init();
		}

		private void init() {
			this.setDivider(null);
			final DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			this.mMaxOverScrollDistance = (int) (dm.density * MAX_OVERSCROLL_DISTANCE);
		}

		/**
		 * 处理listView的触摸事件
		 */
		@Override
		public boolean onTouchEvent(MotionEvent ev) {
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
				} else if (ev.getPointerCount() == 2 && ndst > dst) {
					int st = listView.getFirstVisiblePosition();
					int ed = listView.getLastVisiblePosition();
					for (int i = (st + ed) / 2, j = 1, k = 1; i <= ed
							&& i >= st; i = i + j * k, j++, k = -k) {
						if (mSet.get(i).size() > 1){
							downGranularity(i);
							break;
						}
					}
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

		/**
		 * 
		 */
		@Override
		public boolean dispatchTouchEvent(MotionEvent ev) {
			if (ev.getPointerCount() >= 2) {
				return onTouchEvent(ev);
			}
			return super.dispatchTouchEvent(ev);
		}

		@Override
		protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
				int scrollY, int scrollRangeX, int scrollRangeY,
				int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
			return super.overScrollBy(deltaX, deltaY, scrollX, scrollY,
					scrollRangeX, scrollRangeY, maxOverScrollX,
					mMaxOverScrollDistance, isTouchEvent);
		}
	}

	@Override
	public void onResume() {
		if (dg != null)
			mSet = dg.getSet(granularity);
		super.onResume();
		// PicInfoList = dg.getPicInfoList();
	}
}
