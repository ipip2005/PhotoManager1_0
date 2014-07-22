package com.example.photomanager1_0;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;

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

/**
 * @author ipip
 *  2014年7月16日下午2:50:27
 */
public class DataGain {
	private Cursor cursor;
	private int n;
	private boolean[] got, ing;
	private ContentResolver cr;
	private TimelineActivity mContext;
	private Handler mHandler;
	private int p;
	private int done, doing;
	private int cacheOrder[];
	private boolean gotPoi[];
	// signal for thread security
	private ArrayList<PicInfo> mPicInfoList;
	private ArrayList<ArrayList<Integer>> mSet1, mSet2, mSet3, mSet4;
	private ArrayList<Integer> PSet;
	private static final String[] STORE_IMAGES = {
			MediaStore.Images.Media.DATE_TAKEN,
			MediaStore.Images.Media.LATITUDE,
			MediaStore.Images.Media.LONGITUDE, 
			MediaStore.Images.Media._ID,
			MediaStore.Images.Media.DATA, };
	ExecutorService pool = Executors.newFixedThreadPool(3);

	public DataGain(ContentResolver contentResolver, TimelineActivity context,
			Handler handler) {
		// TODO Auto-generated constructor stub
		cr = contentResolver;
		cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				STORE_IMAGES, null, null, ""
						+ MediaStore.Images.Media.DATE_TAKEN + " DESC");
		mContext = context;
		n = cursor.getCount();
		got = new boolean[n];
		ing = new boolean[n];
		gotPoi = new boolean[n];
		p = 0;
		done = 0;
		doing = 0;
		mHandler = handler;
		cacheOrder = new int[n];
		preData();
	}

	@SuppressLint("SimpleDateFormat")
	private void preData() {
		mPicInfoList = new ArrayList<PicInfo>();
		cursor.moveToFirst();
		do {
			PicInfo info = new PicInfo();
			info.mdate = Calendar.getInstance();
			info.mdate.setTimeInMillis(cursor.getLong(0));
			info.title = "" + (info.mdate.get(Calendar.MONTH) + 1) + "月"
					+ info.mdate.get(Calendar.DATE) + "日";
			if (info.mdate.get(Calendar.YEAR) != Calendar.getInstance().get(
					Calendar.YEAR))
				info.title = "" + info.mdate.get(Calendar.YEAR) + "年"
						+ info.title;
			if (cursor.getDouble(1)>0)
				info.pl = new LatLng(cursor.getDouble(1),cursor.getDouble(2));
			info.id = cursor.getLong(3);
			info.fileRoute = cursor.getString(4);
			mPicInfoList.add(info);
		} while (cursor.moveToNext());
		mSet1 = new ArrayList<ArrayList<Integer>>();
		mSet2 = new ArrayList<ArrayList<Integer>>();
		mSet3 = new ArrayList<ArrayList<Integer>>();
		mSet4 = new ArrayList<ArrayList<Integer>>();

		for (int i = 0; i < n; i++) {
			mSet1.add(new ArrayList<Integer>());
			mSet1.get(i).add(i);
		}

		int p = 0;
		while (p < n) {
			Calendar ed = (Calendar) mPicInfoList.get(p).mdate.clone();
			ed.set(Calendar.HOUR, 0);
			ed.set(Calendar.MINUTE, 0);
			ed.set(Calendar.SECOND, 0);
			ed.set(Calendar.MILLISECOND, 0);
			PicSet ps = new PicSet(ed);
			ps.searchStartAt(p);
			p = ps.searchEndAt();
			mSet2.add(ps.getArrayList());
		}
		int st = 0;
		boolean temp[] = new boolean[n];
		for (int i = 0; i < mSet2.size(); i++) {
			cacheOrder[st] = mSet2.get(i).get(0);
			temp[mSet2.get(i).get(0)] = true;
			st++;
		}
		for (int i = 0; i < n; i++)
			if (!temp[i]) {
				cacheOrder[st] = i;
				st++;
			}

		p = 0;
		while (p < n) {
			Calendar ed = (Calendar) mPicInfoList.get(p).mdate.clone();
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
		while (p < n) {
			Calendar ed = (Calendar) mPicInfoList.get(p).mdate.clone();
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
		
		for (int i=0;i<n;i++) gotPoi[i] = false;
		checkAllPoiData();
	}

	public ArrayList<ArrayList<Integer>> getSet(int granularity) {
		if (granularity == 1)
			return mSet1;
		if (granularity == 2)
			return mSet2;
		if (granularity == 3)
			return mSet3;
		if (granularity == 4)
			return mSet4;
		return null;
	}

	public ArrayList<PicInfo> getPicInfoList() {
		return this.mPicInfoList;
	}

	public ArrayList<Integer> getSetWithPlace() {
		PSet = new ArrayList<Integer>();
		for (int i = 0; i < n; i++)
			if (gotPoi[i]) {
				PSet.add(i);
			}
		return PSet;
	}

	public int getCount() {
		return n;
	}

	public void getData(int index) {
		if (got[index] || ing[index])
			return;
		MyThread t = new MyThread(index);
		ing[index] = true;
		doing++;
		pool.execute(t);
	}
	public boolean getBDPoiFromFile(int i){
		return false;
		/*String filename = "" + mPicInfoList.get(i).id + ".poi";
		File f = new File(filename);
		Boolean fileExists = false;
		try {
			FileInputStream s = mContext.openFileInput(filename);
			fileExists = s != null;
			s.close();
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (fileExists){
			ObjectInputStream o = new ObjectInputStream(s); 
			LatLng gp = (LatLng)o.readObject();
		}*/
	}
	public void requirePoiDataAndWrite(int i){
		LatLng sourceLatLng = mPicInfoList.get(i).pl; 
		CoordinateConverter converter  = new CoordinateConverter();  
		converter.from(CoordType.GPS);  
		// sourceLatLng待转换坐标  
		converter.coord(sourceLatLng);  
		LatLng desLatLng = converter.convert();
		mPicInfoList.get(i).pl = desLatLng;
		gotPoi[i]=true;
	}
	public void checkAllPoiData(){
		for (int i=0;i<n;i++) if (mPicInfoList.get(i).pl!=null && !gotPoi[i]){
			if (getBDPoiFromFile(i)) continue;
			requirePoiDataAndWrite(i);
		}
	}
	
	public void delData(int index) {

	}

	private class MyThread implements Runnable {
		private int id;

		public MyThread(int index) {
			id = index;
		}

		@SuppressLint("SimpleDateFormat")
		@Override
		public void run() {
			// TODO Auto-generated method stub
			String filename = "" + mPicInfoList.get(id).id + ".thunb";
			File f = new File(filename);
			Boolean fileExists = false;
			try {
				FileInputStream s = mContext.openFileInput(filename);
				mPicInfoList.get(id).bitmap = BitmapFactory.decodeStream(s);
				fileExists = s != null;
				s.close();
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (!fileExists) {
				BitmapFactory.Options op = new BitmapFactory.Options();
				op.inJustDecodeBounds = true;
				op.inSampleSize = 1;
				mPicInfoList.get(id).bitmap = MediaStore.Images.Thumbnails
						.getThumbnail(cr, mPicInfoList.get(id).id,
								Thumbnails.MINI_KIND, op);
				if (op.outWidth < op.outHeight){
					op.inSampleSize = op.outWidth/250;
					op.outWidth = 250;
					op.outHeight = op.outHeight * 250 / op.outWidth;
				} else {
					op.inSampleSize = op.outHeight/250;
					op.outHeight = 250;
					op.outWidth = op.outWidth * 250 / op.outHeight;
				}
				mPicInfoList.get(id).bitmap = ThumbnailUtils.extractThumbnail(MediaStore.Images.Thumbnails
						.getThumbnail(cr, mPicInfoList.get(id).id,
								Thumbnails.MINI_KIND, op), 250, 250);
				try {
					f.mkdirs();
					FileOutputStream s = mContext.openFileOutput(filename,
							Context.MODE_PRIVATE);
					// ByteArrayOutputStream bs = new ByteArrayOutputStream(s);
					mPicInfoList.get(id).bitmap.compress(
							Bitmap.CompressFormat.PNG, 100, s);
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
			// mContext.doneWork(id, info);
			got[id] = true;
			done++;
			if (doing - done <= 1) {
				while (p < n - 1 && (ing[cacheOrder[p]] || got[cacheOrder[p]]))
					p++;
				if (!ing[cacheOrder[p]] && !got[cacheOrder[p]]) {
					ing[cacheOrder[p]] = true;
					doing++;
					MyThread t = new MyThread(cacheOrder[p]);
					pool.execute(t);
				}
			}
		}
	}

	private class PicSet {
		ArrayList<Integer> ids;
		int p;
		private Calendar ed;

		public PicSet(Calendar ed_date) {
			this.ed = ed_date;
			ids = new ArrayList<Integer>();
		}

		public void searchStartAt(int id) {
			p = id;
			while (p < n && mPicInfoList.get(p).mdate.after(ed)) {
				ids.add(p);
				p++;
			}
		}

		public int searchEndAt() {
			return p;
		}

		public ArrayList<Integer> getArrayList() {
			return ids;
		}
	}
}
