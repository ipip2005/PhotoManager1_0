package com.photomanager.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

/**
 * @author ipip
 *  2014年7月16日下午2:50:27
 */
public class DataGain {
	private Cursor cursor;
	private int n;
	private ContentResolver cr;
	private TimelineActivity mContext;
	private Handler mHandler;
	private boolean gotPoi[];
	private static LruCache<String, Bitmap> cache;
	// signal for thread security
	private ArrayList<PicInfo> mPicInfoList;
	private ArrayList<ArrayList<Integer>> mSet1, mSet2, mSet3, mSet4;
	private ArrayList<Integer> mSetWithPlace;
	private static final String[] STORE_IMAGES = {
			MediaStore.Images.Media.DATE_TAKEN,
			MediaStore.Images.Media.LATITUDE,
			MediaStore.Images.Media.LONGITUDE, 
			MediaStore.Images.Media._ID,
			MediaStore.Images.Media.DATA, };
	ExecutorService pool = Executors.newFixedThreadPool(4);
	/**
	 * @param contentResolver
	 * @param context
	 * @param handler
	 */
	public DataGain(ContentResolver contentResolver, TimelineActivity context,
			Handler handler) {
		// TODO Auto-generated constructor stub
		cr = contentResolver;
		cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				STORE_IMAGES, null, null, ""
						+ MediaStore.Images.Media.DATE_TAKEN + " DESC");
		mContext = context;
		n = cursor.getCount();
		gotPoi = new boolean[n];
		mHandler = handler;
		preData();
		int maxMemory = (int)(Runtime.getRuntime().maxMemory());
		int cacheSize = maxMemory/4;
		Log.i("DataGain", "Cache Size: "+cacheSize);
		cache = new LruCache<String, Bitmap>(cacheSize){
			@Override
			protected int sizeOf(String key, Bitmap value)
			{
				return value.getRowBytes() * value.getHeight();
			};
		};;
	}
	@SuppressLint("SimpleDateFormat")
	/**
	 * preData()
	 * 准备需要的数据
	 */
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
			temp[mSet2.get(i).get(0)] = true;
			st++;
		}
		for (int i = 0; i < n; i++)
			if (!temp[i]) {
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
	private void addBitmapToLruCache(String key, Bitmap bitmap)
	{
		if (cache.get(key) == null)
		{
			if (bitmap != null)
				cache.put(key, bitmap);
		}
	}
	public ArrayList<PicInfo> getPicInfoList() {
		return this.mPicInfoList;
	}

	public ArrayList<Integer> getSetWithPlace() {
		return mSetWithPlace;
	}

	public int getCount() {
		return n;
	}
	
	/*public Bitmap getData(int index) {
		if (cache.get(String.valueOf(index)) != null) {
			Log.i("DataGain","hasData: "+index);
			return cache.get(String.valueOf(index));
		}
		MyThread t = new MyThread(index, null);
		pool.execute(t);
		return null;
	}*/
	public void getData(final int index, final ImageView iv){
		Log.i("DataGain", "count: " + cache.size());
		iv.setTag(String.valueOf(index));
		if (mHandler == null){
			mHandler = new Handler(){
				public void handleMessage(Message msg) {
					Holder h = (Holder)msg.obj;
					if (h.iv != null && h.iv.getTag().toString().equals(h.tag)){
						h.iv.setImageBitmap(h.bm);
					}
				}
			};
		}
		Bitmap bm = cache.get(String.valueOf(index));
		if (bm != null) {
			Message m = Message.obtain();
			m.obj = new Holder(iv, bm, String.valueOf(index));
			mHandler.sendMessage(m);
			return;
		} else
		pool.execute(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				int id = index;
				String filename = "" + mPicInfoList.get(id).id + ".thumb";
				Boolean fileExists = false;
				try {
					FileInputStream s = mContext.openFileInput(filename);
					fileExists = s != null;
					if (fileExists){
						Bitmap bm = BitmapFactory.decodeStream(s);
						synchronized (cache){
							addBitmapToLruCache(String.valueOf(id), bm);
						}
						Message m = Message.obtain();
						m.obj = new Holder(iv, bm, String.valueOf(id));
						mHandler.sendMessage(m);
						return;
					}
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
					BitmapFactory.decodeFile(mPicInfoList.get(id).fileRoute, op);
					if (op.outWidth > op.outHeight){
						op.inSampleSize = op.outWidth/200;
						op.outWidth = 200;
						op.outHeight = op.outHeight * 200 / op.outWidth;
					} else {
						op.inSampleSize = op.outHeight/200;
						op.outHeight = 200;
						op.outWidth = op.outWidth * 200 / op.outHeight;
					}
					op.inPurgeable = true;
					op.inInputShareable = true;
					op.inPreferredConfig = Bitmap.Config.RGB_565;
					op.inJustDecodeBounds = false;
					Bitmap bitmap = BitmapFactory.decodeFile(mPicInfoList.get(id).fileRoute, op);
					addBitmapToLruCache(String.valueOf(id), bitmap);
					Message m = Message.obtain();
					m.obj = new Holder(iv, bitmap, String.valueOf(id));
					mHandler.sendMessage(m);
					try {
						FileOutputStream s = mContext.openFileOutput(filename,
								Context.MODE_PRIVATE);
						bitmap.compress(Bitmap.CompressFormat.PNG, 100, s);
						s.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}	
		});
	}
	public void requirePoiDataAndWrite(int i){
		LatLng sourceLatLng = mPicInfoList.get(i).pl; 
		CoordinateConverter converter  = new CoordinateConverter();  
		converter.from(CoordType.GPS);  
		// sourceLatLng待转换坐标  
		converter.coord(sourceLatLng);  
		LatLng desLatLng = converter.convert();
		mPicInfoList.get(i).pl = desLatLng;
	}
	public void checkAllPoiData(){
		mSetWithPlace = new ArrayList<Integer>();
		for (int i=0;i<n;i++) if (mPicInfoList.get(i).pl!=null){
			requirePoiDataAndWrite(i);
			mSetWithPlace.add(i);
		}
	}
	
	public void delData(int index) {
		
	}

	private class Holder{
		public ImageView iv;
		public Bitmap bm;
		public String tag;
		public Holder(ImageView iv, Bitmap bm, String tag){
			this.iv = iv;
			this.bm = bm;
			this.tag = tag;
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
