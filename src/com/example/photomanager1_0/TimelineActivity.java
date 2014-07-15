package com.example.photomanager1_0;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TimelineActivity extends Activity implements OnTouchListener, OnItemClickListener{
	static public ArrayList<PicInfo> PicInfoList;
	static public DataGain dg;
	private PictureAdapter mAdapter;
	private ListView listView = null;
	//The zoom level of the listView
	private int granularity;
	//The handler which can receive informations from class DataGain.java and refresh UI.
	private static Handler handler;
	//The number of fingers on the screen.
	private int mode;
	//The origin y of mid-point of the two fingers.
	private double dst;
	//The new y of mid-point of the two fingers.
	private double ndst;
	//The mapping set, to get the real index in PicInfoList.
	private ArrayList<ArrayList<Integer>> mSet;
	//The Animation for listView.
	private Animation myLayoutAnimation,myAnimation;
	private LayoutAnimationController lac;
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		preData();
		listView = (ListView)findViewById(R.id.MyListView);
		mAdapter = new PictureAdapter(this);
		listView.setAdapter(mAdapter);
		listView.setOnTouchListener(this);
		listView.setOnItemClickListener(this);
		myLayoutAnimation = new AlphaAnimation(0.8f,1.0f);
		myLayoutAnimation.setDuration(500);
		lac = new LayoutAnimationController(myLayoutAnimation);
		lac.setDelay(0.1f);
		listView.setLayoutAnimation(lac);
		
		myAnimation = new ScaleAnimation(0.1f, 1.1f, 0.1f, 1.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		myAnimation.setDuration(200);
		listView.setAnimation(myAnimation);
	}
	private void preData(){
		handler = new Handler(){
			public void handleMessage(Message msg){
				mAdapter.notifyDataSetChanged();
			}
		};
		dg = new DataGain(getContentResolver(), TimelineActivity.this, handler);
		PicInfoList = dg.getPicInfoList();
		granularity = 2;
		mSet = dg.getSet(granularity);
		//Log.i("photo",""+PicInfoList.size());
	}
	
	private class PictureAdapter extends BaseAdapter{
    	private LayoutInflater mInflater;
    	
    	public PictureAdapter(Context context){
    		this.mInflater = LayoutInflater.from(context);
    	}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (granularity == 1)
				return PicInfoList.size(); else
				return mSet.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			if (granularity == 1)
				return PicInfoList.get(arg0); else
				return mSet.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return	arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			if (convertView == null){ 
				int lid;
				holder = new ViewHolder();
				lid = R.layout.simple_list_item;
				convertView = mInflater.inflate(lid, null);
				holder.title = (TextView) convertView.findViewById(R.id.item_title);
				holder.image = (ImageView) convertView.findViewById(R.id.imageView);
				holder.subimage[0] = (ImageView) convertView.findViewById(R.id.imageView1);
				holder.subimage[1] = (ImageView) convertView.findViewById(R.id.imageView2);
				holder.subimage[2] = (ImageView) convertView.findViewById(R.id.imageView3);
				convertView.setTag(holder);
			} else{
				holder = (ViewHolder) convertView.getTag();
			}
			for (int i=0;i<3;i++) holder.subimage[i].setImageBitmap(null);
			holder.image.setImageBitmap(null);
			if (granularity == 1){
				holder.title.setText(PicInfoList.get(position).title);
				if (PicInfoList.get(position).bitmap == null){				
					dg.getData(position);
				} else{
					holder.image.setImageBitmap(PicInfoList.get(position).bitmap);
				}
			} else {
				int id_first = mSet.get(position).get(0);
				String openFlag = "";
				if (mSet.get(position).size()>1) openFlag = ">>";
				holder.title.setText(dealTitle(mSet.get(position))+openFlag);
				if (PicInfoList.get(id_first).bitmap == null){
					dg.getData(id_first);
				} else{
					holder.image.setImageBitmap(PicInfoList.get(id_first).bitmap);
				}
				for (int i=0;i<3;i++) if (i+1<mSet.get(position).size()){
					if (PicInfoList.get(id_first+i+1).bitmap ==null){
						dg.getData(id_first+i+1);
					} else {
						holder.subimage[i].setImageBitmap(PicInfoList.get(id_first+i+1).bitmap);
					}
				} else break;
			}
			
			return convertView;
		}
    	
    }
	private String dealTitle(ArrayList<Integer> list){
		String st = PicInfoList.get(list.get(0)).title;
		String ed = PicInfoList.get(list.get(list.size()-1)).title;
		if (st.equals(ed)) return st; else return ed+"-"+st;
	}
	private double measureFingers(MotionEvent event){
		double x = event.getX(0) - event.getX(1);
		double y = event.getY(0) - event.getY(1);
		return Math.sqrt(x * x + y * y);
	}
	private void upGranularity(){
		if (granularity == 4 || mSet.size()==1) return;
		granularity++;
		mSet = dg.getSet(granularity);
		mAdapter.notifyDataSetChanged();
		listView.setSelection(0);
		listView.startLayoutAnimation();
		listView.clearAnimation();
		listView.setAnimation(myAnimation);
		listView.startAnimation(myAnimation);
	}
	private void downGranularity(int index){
		granularity--;
		if (granularity == 1){
			mAdapter.notifyDataSetChanged();
			listView.setSelection(mSet.get(index).get(0));
		} else {
			int id = mSet.get(index).get(0);
			mSet = dg.getSet(granularity);
			mAdapter.notifyDataSetChanged();
			for (int i=1;i<=mSet.size();i++){
				if (i==mSet.size()) listView.setSelection(i-1); else
				if (PicInfoList.get(id).mdate.after(PicInfoList.get(mSet.get(i).get(0)).mdate)){
					listView.setSelection(i-1);
					break;
				}
			}
		}
		listView.startLayoutAnimation();
		listView.clearAnimation();
		listView.setAnimation(myAnimation);
		listView.startAnimation(myAnimation);
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction() & MotionEvent.ACTION_MASK){
		case MotionEvent.ACTION_DOWN:
			mode = 1;
			ndst=dst;
			break;
		case MotionEvent.ACTION_UP:
			mode = 0 ;
			if (ndst<dst){
				upGranularity();
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			mode++;
			if (mode == 2) dst = measureFingers(event);
			break;
		case MotionEvent.ACTION_POINTER_UP:
			mode--;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode >= 2){
				ndst = measureFingers(event);
			}
		}
		if (mode>=2) return true;
		return super.onTouchEvent(event);
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if (granularity==1){
			Intent intent = new Intent(TimelineActivity.this,ShowImageActivity.class);
			intent.putExtra("image", arg2);
			startActivity(intent);
		} else {
			if (mSet.get(arg2).size()==1){
				Intent intent = new Intent(TimelineActivity.this,ShowImageActivity.class);
				intent.putExtra("image", mSet.get(arg2).get(0));
				startActivity(intent);
				return;
			}
			this.downGranularity(arg2);
		}
	}
}
