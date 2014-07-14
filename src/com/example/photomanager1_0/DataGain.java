package com.example.photomanager1_0;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;

public class DataGain {
	private Cursor cursor;
	private int n;
	private boolean[] got,ing;
	private ContentResolver cr;
	private TimelineActivity mContext;
	private Handler mHandler;
	private int p;
	private int done,doing;
	private int cacheOrder[];
	//signal for thread security
	private int[] signal;
	private int sp,sn;
	private ArrayList<PicInfo> mPicInfoList;
	private ArrayList<ArrayList<Integer>> mSet2,mSet3,mSet4;
	private ArrayList<Integer> PSet;
	private boolean isPSet = false; // if tSet has been calculated
	private static final String[] STORE_IMAGES = {
		MediaStore.Images.Media.DATE_TAKEN,
        MediaStore.Images.Media.LATITUDE,
        MediaStore.Images.Media.LONGITUDE,
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATA
	};
	ExecutorService pool = Executors.newFixedThreadPool(3);
	public DataGain(ContentResolver contentResolver,
			TimelineActivity context, Handler handler) {
		// TODO Auto-generated constructor stub
		cr = contentResolver;
		cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, STORE_IMAGES,
				null, null, ""+MediaStore.Images.Media.DATE_TAKEN + " DESC");
		mContext = context;
		n = cursor.getCount();
		got = new boolean[n];
		ing = new boolean[n];
		p=0;
		done=0;doing=0;
		mHandler = handler;
		signal = new int[n];
		sp=0;sn=0;
		cacheOrder = new int[n];
		preData();
	}
	
	@SuppressLint("SimpleDateFormat")
	private void preData(){
		mPicInfoList = new ArrayList<PicInfo>();
		cursor.moveToFirst();
		do{
			PicInfo info = new PicInfo();
			info.mdate = Calendar.getInstance();
			info.mdate.setTimeInMillis(cursor.getLong(0));
			info.title = ""+info.mdate.get(Calendar.MONTH)+"��"+info.mdate.get(Calendar.DATE)+"��";
			if (info.mdate.get(Calendar.YEAR)!=Calendar.getInstance().get(Calendar.YEAR))
				info.title = ""+info.mdate.get(Calendar.YEAR)+"��"+info.title;
			info.lalitude = cursor.getDouble(1);
        	info.longitude = cursor.getDouble(2);
        	info.id = cursor.getLong(3);
        	info.text = "" + info.lalitude + " : " + info.longitude;
        	info.fileRoute = cursor.getString(4);
			mPicInfoList.add(info);
		}while (cursor.moveToNext());
		
		mSet2 = new ArrayList<ArrayList<Integer>>();
		mSet3 = new ArrayList<ArrayList<Integer>>();
		mSet4 = new ArrayList<ArrayList<Integer>>();
		
		int p = 0;
		while (p<n){
			Calendar ed = (Calendar)mPicInfoList.get(p).mdate.clone();
			ed.set(Calendar.HOUR, 0);
			ed.set(Calendar.MINUTE, 0);
			ed.set(Calendar.SECOND, 0);
			ed.set(Calendar.MILLISECOND, 0);
			PicSet ps = new PicSet(ed);
			ps.searchStartAt(p);
			p = ps.searchEndAt();
			mSet2.add(ps.getArrayList());
		}
		int st=0;
		boolean temp[] = new boolean[n];
		for (int i=0;i<mSet2.size();i++){
			cacheOrder[st] = mSet2.get(i).get(0);
			temp[mSet2.get(i).get(0)] = true;
			st++;
		}
		for (int i=0;i<n;i++) if (!temp[i]){
			cacheOrder[st] = i;
			st++;
		}
		
		
		p = 0;
		while (p<n){
			Calendar ed = (Calendar)mPicInfoList.get(p).mdate.clone();
			ed.set(Calendar.DATE, 1);
			ed.set(Calendar.HOUR, 0);
			ed.set(Calendar.MINUTE, 0);
			ed.set(Calendar.SECOND, 0);
			ed.set(Calendar.MILLISECOND, 0);
			PicSet ps = new PicSet(ed);
			ps.searchStartAt(p);
			p = ps.searchEndAt();
			mSet3.add(ps.getArrayList());
		}

		p = 0;
		while (p<n){
			Calendar ed = (Calendar)mPicInfoList.get(p).mdate.clone();
			ed.set(Calendar.MONTH, Calendar.JANUARY);
			ed.set(Calendar.DATE, 1);
			ed.set(Calendar.HOUR, 0);
			ed.set(Calendar.MINUTE, 0);
			ed.set(Calendar.SECOND, 0);
			ed.set(Calendar.MILLISECOND, 0);
			PicSet ps = new PicSet(ed);
			ps.searchStartAt(p);
			p = ps.searchEndAt();
			mSet4.add(ps.getArrayList());
		}
		
	}
	public ArrayList<ArrayList<Integer>> getSet(int granularity){
		if (granularity == 2) return mSet2;
		if (granularity == 3) return mSet3;
		if (granularity == 4) return mSet4;
		return null;
	}
	public ArrayList<PicInfo> getPicInfoList(){
		return this.mPicInfoList;
	}
	public ArrayList<Integer> getSetWithPlace(){
		if (isPSet) return PSet;
		PSet = new ArrayList<Integer>();
		for (int i=0;i<n;i++) if (mPicInfoList.get(i).lalitude!=0){
			PSet.add(i);
		}
		return PSet;
	}
	public int getCount(){
		return n;
	}
	public void getData(int index){
		if (got[index] || ing[index]) return;
		MyThread t = new MyThread(index);
		ing[index] = true;
		doing++;
		pool.execute(t);
	}
	public void delData(int index){
		
	}
	private class MyThread implements Runnable{
		private int id;
		public MyThread(int index){
			id = index;
		}
		@SuppressLint("SimpleDateFormat")
		@Override
		public void run() {
			// TODO Auto-generated method stub
        	BitmapFactory.Options op = new BitmapFactory.Options();
        	String filename=""+mPicInfoList.get(id).id+".thunb";
        	File f=new File(filename);
        	Boolean fileExists = false;;
        	try {
				FileInputStream s = mContext.openFileInput(filename);
				mPicInfoList.get(id).bitmap = BitmapFactory.decodeStream(s);
				fileExists = s!=null;
        		s.close();
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	if (!fileExists){
        		mPicInfoList.get(id).bitmap = ThumbnailUtils.extractThumbnail(MediaStore.Images.Thumbnails.getThumbnail(
    					cr, mPicInfoList.get(id).id, Thumbnails.MINI_KIND, null), 250, 250);
        		try {
        			f.mkdirs();
        			FileOutputStream s = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
        			//ByteArrayOutputStream bs = new ByteArrayOutputStream(s);
        			mPicInfoList.get(id).bitmap.compress(Bitmap.CompressFormat.PNG, 100, s);
					s.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        	}
        	Message msg = new Message();
        	msg.what = id;
        	mHandler.sendMessage(msg);
        	//mContext.doneWork(id, info);
        	got[id] = true;
        	done++;
        	if (doing-done<=1) {
        		while (p<n-1 && (ing[cacheOrder[p]]||got[cacheOrder[p]])) p++;
        		if (!ing[cacheOrder[p]] && !got[cacheOrder[p]]){
        			ing[cacheOrder[p]] = true;
        			doing++;
        			MyThread t = new MyThread(cacheOrder[p]);
        			pool.execute(t);
        		}
        	}
		}
	}
	private class PicSet{
		ArrayList<Integer> ids;
		int p;
		private Calendar ed;
		public PicSet(Calendar ed_date){
			this.ed = ed_date;
			ids = new ArrayList<Integer>();
		}
		public void searchStartAt(int id){
			p = id;
			while (p<n && mPicInfoList.get(p).mdate.after(ed)){
				ids.add(p);
				p++;
			}
		}
		
		public int searchEndAt(){
			return p;
		}
		
		public ArrayList<Integer> getArrayList(){
			return ids;
		}
	}
}
