package com.photomanager.main;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;

public class DataGainUtil {
	private static DataGainUtil mData= null;
	private static DataGain dataGain = null;
	private static Context mContext;
	public final static int
		SMALL = 2,
		LARGE0 = 0,
		LARGE1 = 1;
	protected DataGainUtil(){
	}
	public static DataGainUtil getInstance(){
		if (mData == null){
			mData = new DataGainUtil();
		}
		return mData;
	}
	public static DataGain getDataGain(){
		return dataGain;
	}
	public static DataGain getDataGainInstance(Context context, Handler handler){
		mContext = context;
		if (dataGain == null){
			dataGain = new DataGain(context, handler);
		}
		return dataGain;
	}
	/**
	 * 由图片列表的编号和需要图片的类型生成一个唯一key，用户申请dataGain获取图片
	 * @param id	在mPicInfoList中的index
	 * @param type	SMALL=2小型图片，默认200*x大小,LARGE0和LARGE1用于区分横屏和竖屏
	 * @return	一个String表示生成的key
	 */
	public static String generateKey(int id, int type){
		if (id >= TimelineActivity.PicInfoList.size()) return null;
		return "" + TimelineActivity.PicInfoList.get(id).id + type;
	}
	public static int getStandarLength(){
		DisplayMetrics dm = new DisplayMetrics();
		((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
		return (int)(dm.densityDpi / 160 * 100);
	}
}
