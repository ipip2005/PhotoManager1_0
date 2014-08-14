package com.photomanager.main;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class MyRecyclerViewAdapter extends
		RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

	private Context mContext;
	private ArrayList<Integer> mSet;

	public MyRecyclerViewAdapter(Context context, ArrayList<Integer> set) {
		super();
		mContext = context;
		mSet = set;
	}

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		return mSet.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder vh, int position) {
		// TODO Auto-generated method stub
		ImageView iv = new ImageView(mContext);
		vh.layout.removeAllViews();
		iv.setImageBitmap(null);
		final int index = mSet.get(position);
		String key = DataGainUtil.getInstance().generateKey(index,
				DataGainUtil.SMALL);
		DataGainUtil.getDataGain().getDataForImageView(index, iv, key);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		iv.setLayoutParams(lp);
		iv.setScaleType(ScaleType.CENTER_CROP);
		iv.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent ev) {
				// TODO Auto-generated method stub
				// Log.i("TimelineActivity",""+ev.getAction()+" "+ev.getRawX()+" "+ev.getRawY());

				switch (ev.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:
					// Log.i("TimelineActivity", "Down");
					Animation cAnimation = new ScaleAnimation(1.0f, 0.9f, 1.0f,
							0.9f, Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					cAnimation.setDuration(300);
					cAnimation.setFillAfter(true);
					v.setAnimation(cAnimation);
					v.startAnimation(cAnimation);
					break;
				case MotionEvent.ACTION_CANCEL:
					Animation dAnimation = new ScaleAnimation(0.95f, 1.0f,
							0.95f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					dAnimation.setDuration(300);
					v.setAnimation(dAnimation);
					v.startAnimation(dAnimation);
					break;
				case MotionEvent.ACTION_UP:
					Animation eAnimation = new ScaleAnimation(0.95f, 1.0f,
							0.95f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					eAnimation.setDuration(300);
					v.setAnimation(eAnimation);
					v.startAnimation(eAnimation);
					Intent intent = new Intent(mContext,
							ShowImageActivity.class);
					intent.putExtra("image", (Integer) index);
					mContext.startActivity(intent);
					break;
				}
				return true;
			}

		});
		vh.layout.addView(iv);
		vh.layout.setBackgroundColor(Color.BLUE);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup vg, int position) {
		// TODO Auto-generated method stub
		SquareLayout layout = new SquareLayout(mContext);

		ViewHolder holder = new ViewHolder(layout);
		return holder;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public SquareLayout layout;

		public ViewHolder(View itemView) {
			super(itemView);
			layout = (SquareLayout) itemView;
		}
	}

}