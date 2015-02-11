package com.photomanager.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.baidu.mapapi.utils.DistanceUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

/**
 * @author ipip 2014年7月16日下午2:50:27
 */
public class DataGain {
	private Cursor cursor;
	private int n;
	private ContentResolver cr;
	private Context mContext;
	private Handler mHandler;
	private boolean gotPoi[];
	private static LruCache<String, Bitmap> cache;
	// signal for thread security
	private ArrayList<PicInfo> mPicInfoList;
	private ArrayList<ArrayList<Integer>> mSet1, mSet2, mSet3, mSet4;
	private ArrayList<Integer> mSetWithPlace;
	private ArrayList<String> text;
	private static final String[] STORE_IMAGES = {
			MediaStore.Images.Media.DATE_TAKEN,
			MediaStore.Images.Media.LATITUDE,
			MediaStore.Images.Media.LONGITUDE, MediaStore.Images.Media._ID,
			MediaStore.Images.Media.DATA };
	private Uri MediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	ExecutorService pool = Executors.newFixedThreadPool(4);

	/**
	 * @param contentResolver
	 * @param context
	 * @param handle
	 */
	public DataGain(Context context, Handler handler) {
		// TODO Auto-generated constructor stub
		mHandler = handler;
		mContext = context;
		preData();
	}

	/**
	 * preData() 准备需要的数据
	 */
	public void preData() {
		cr = mContext.getContentResolver();
		cursor = cr.query(MediaUri, STORE_IMAGES, null, null, ""
				+ MediaStore.Images.Media.DATE_TAKEN + " DESC");
		n = cursor.getCount();
		gotPoi = new boolean[n];

		int maxMemory = (int) (Runtime.getRuntime().maxMemory());
		int cacheSize = maxMemory / 6;
		cache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getByteCount();
			};
		};
		mPicInfoList = new ArrayList<PicInfo>();
		cursor.moveToFirst();
		do {
			PicInfo info = new PicInfo();
			info.mdate = Calendar.getInstance();
			info.mdate.setTimeInMillis(cursor.getLong(0));
			info.title = "" + (info.mdate.get(Calendar.MONTH) + 1) + "-"
					+ info.mdate.get(Calendar.DATE);
			if (info.mdate.get(Calendar.YEAR) != Calendar.getInstance().get(
					Calendar.YEAR))
				info.title = "" + info.mdate.get(Calendar.YEAR) + "-"
						+ info.title;
			if (cursor.getDouble(1) > 0)
				info.pl = new LatLng(cursor.getDouble(1), cursor.getDouble(2));
			info.id = cursor.getLong(3);
			info.fileRoute = cursor.getString(4);
			String folder = info.fileRoute.substring(0,
					info.fileRoute.lastIndexOf('/') - 1);
			info.folderName = folder.substring(folder.lastIndexOf('/') + 1);
			// Log.i("DataGain", info.folderName);
			mPicInfoList.add(info);
		} while (cursor.moveToNext());
		cursor.close();
		makeSets();
	}

	private void makeSets() {
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
		boolean temp[] = new boolean[n];
		for (int i = 0; i < mSet2.size(); i++) {
			temp[mSet2.get(i).get(0)] = true;
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
		for (int i = 0; i < n; i++)
			gotPoi[i] = false;
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

	private void addBitmapToLruCache(String key, Bitmap bitmap) {
		if (cache.get(key) == null) {
			if (bitmap != null)
				cache.put(key, bitmap);
		}
		// Log.i("DataGain", "now: " + cache.size() + " " + cache.maxSize());
	}

	public ArrayList<PicInfo> getPicInfoList() {
		return this.mPicInfoList;
	}

	public ArrayList<Integer> getSetWithPlace() {
		return mSetWithPlace;
	}

	private double calcDist(LatLng a, LatLng b) {
		return DistanceUtil.getDistance(a, b);
	}

	public ArrayList<Integer> getSetInOrderDistance(LatLng center, final boolean isRevert) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		final ArrayList<Double> dis = new ArrayList<Double>();
		for (int i = 0; i < mSetWithPlace.size(); i++) {
			ret.add(i);
			dis.add(calcDist(center, mPicInfoList.get(mSetWithPlace.get(i)).pl));
		}
		Collections.sort(ret, new Comparator<Integer>() {

			@Override
			public int compare(Integer p, Integer q) {
				// TODO Auto-generated method stub
				if (!isRevert)
					return dis.get(p).compareTo(dis.get(q));
				else 
					return dis.get(q).compareTo(dis.get(p));
			}

		});
		text = new ArrayList<String>();
		for (int i = 0; i < ret.size(); i++) {
			long meter = Math.round(dis.get(ret.get(i)));
			if (meter >= 10000)
				text.add("" + meter / 1000 + "km");
			else
				text.add("" + meter + "m");
			ret.set(i, mSetWithPlace.get(ret.get(i)));
		}
		return ret;
	}

	public ArrayList<String> getSetDistanceMeter() {
		return text;
	}

	public int getCount() {
		return n;
	}

	public Bitmap getDataNow(final String key) {
		if (key == null)
			return null;
		Bitmap ret = cache.get(key);
		if (ret == null || ret.isRecycled())
			return null;
		return ret;
	}

	public void getDataForImageView(final int index, final ImageView iv,
			final String key) {
		// Log.i("DataGain", "count: " + cache.size());
		if (key == null)
			return;
		iv.setTag(key);
		final boolean isSmall = key
				.endsWith(String.valueOf(DataGainUtil.SMALL));
		if (mHandler == null) {
			mHandler = new Handler() {
				public void handleMessage(Message msg) {
					Holder h = (Holder) msg.obj;
					if (h.iv != null && h.iv.getTag().toString().equals(h.tag)) {
						h.iv.setImageBitmap(h.bm);
					}
				}
			};
		}
		Bitmap bm = cache.get(key);
		if (bm != null) {
			Message m = Message.obtain();
			m.obj = new Holder(iv, bm, key);
			mHandler.sendMessage(m);
			return;
		} else
			pool.execute(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (!iv.getTag().toString().equals(key))
						return;
					// Log.i("DataGain", "obtain: " + index);
					int id = index;
					String filename = key + mPicInfoList.get(id).id + ".thumb";
					Boolean fileExists = false;
					try {
						FileInputStream s = mContext.openFileInput(filename);
						fileExists = s != null;
						if (fileExists) {
							// Log.i("DataGain", "read from file");
							Bitmap bm = BitmapFactory.decodeStream(s);
							addBitmapToLruCache(key, bm);
							Message m = Message.obtain();
							m.obj = new Holder(iv, bm, key);
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
						int length = DataGainUtil.getStandarLength() / 2;
						int sWidth = length, sHeight = length;
						BitmapFactory.decodeFile(
								mPicInfoList.get(id).fileRoute, op);
						if (!isSmall) {
							DisplayMetrics dm = new DisplayMetrics();
							((Activity) mContext).getWindowManager()
									.getDefaultDisplay().getMetrics(dm);
							sWidth = dm.widthPixels;
							sHeight = dm.heightPixels;
							// Log.i("DataGain", ""+op.outWidth+" " +
							// op.outHeight+" "+sWidth+" "+sHeight);
						}

						if (op.outWidth < sWidth && op.outHeight < sHeight) {
							op.inSampleSize = 1;
						} else {
							boolean s = op.outWidth * sHeight < op.outHeight
									* sWidth;
							Log.i("DataGain", "" + s + " " + isSmall);
							if (!s ^ isSmall) {
								op.inSampleSize = op.outWidth / sWidth;
								op.outHeight = (int) (1.0 * op.outHeight
										* sWidth / op.outWidth);
								op.outWidth = sWidth;
							} else {
								op.inSampleSize = op.outHeight / sHeight;
								op.outWidth = (int) (1.0 * op.outWidth
										* sHeight / op.outHeight);
								op.outHeight = sHeight;
							}
						}
						// Log.i("DataGain", ""+op.inSampleSize +
						// " "+op.outWidth+" "+op.outHeight);
						op.inPurgeable = true;
						op.inInputShareable = true;
						op.inPreferredConfig = Bitmap.Config.RGB_565;
						op.inJustDecodeBounds = false;
						Bitmap bitmap = BitmapFactory.decodeFile(
								mPicInfoList.get(id).fileRoute, op);
						addBitmapToLruCache(key, bitmap);
						Message m = Message.obtain();
						m.obj = new Holder(iv, bitmap, key);
						mHandler.sendMessage(m);
						if (!isSmall)
							return;
						try {
							FileOutputStream s = mContext.openFileOutput(
									filename, Context.MODE_PRIVATE);
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

	public void getDataForOther(final int index, final Object info,
			final String key, final Handler handler) {
		if (key == null)
			return;
		Bitmap bm = cache.get(key);
		if (bm != null) {
			Message m = Message.obtain();
			m.obj = info;
			handler.sendMessage(m);
			return;
		} else
			pool.execute(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					int id = index;
					String filename = key + mPicInfoList.get(id).id + ".thumb";
					Boolean fileExists = false;
					try {
						FileInputStream s = mContext.openFileInput(filename);
						fileExists = s != null;
						if (fileExists) {
							Bitmap bm = BitmapFactory.decodeStream(s);
							addBitmapToLruCache(key, bm);
							Message m = Message.obtain();
							m.obj = info;
							handler.sendMessage(m);
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
						BitmapFactory.decodeFile(
								mPicInfoList.get(id).fileRoute, op);
						int length = DataGainUtil.getStandarLength() / 2;
						if (op.outWidth < op.outHeight) {
							op.inSampleSize = op.outWidth / length;
							op.outHeight = (int) (1.0 * op.outHeight * length / op.outWidth);
							op.outWidth = length;

						} else {
							op.inSampleSize = op.outHeight / length;
							op.outWidth = (int) (1.0 * op.outWidth * length / op.outHeight);
							op.outHeight = length;

						}
						op.inPurgeable = true;
						op.inInputShareable = true;
						op.inPreferredConfig = Bitmap.Config.RGB_565;
						op.inJustDecodeBounds = false;
						Bitmap bitmap = BitmapFactory.decodeFile(
								mPicInfoList.get(id).fileRoute, op);
						addBitmapToLruCache(key, bitmap);
						Message m = Message.obtain();
						m.obj = info;
						handler.sendMessage(m);
						try {
							FileOutputStream s = mContext.openFileOutput(
									filename, Context.MODE_PRIVATE);
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

	public void requirePoiDataAndWrite(int i) {
		if (mPicInfoList.get(i).poiConverted)
			return;
		LatLng sourceLatLng = mPicInfoList.get(i).pl;
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.GPS);
		// sourceLatLng待转换坐标
		converter.coord(sourceLatLng);
		LatLng desLatLng = converter.convert();
		mPicInfoList.get(i).pl = desLatLng;
		mPicInfoList.get(i).poiConverted = true;
	}

	public void checkAllPoiData() {
		mSetWithPlace = new ArrayList<Integer>();
		for (int i = 0; i < n; i++)
			if (mPicInfoList.get(i).pl != null) {
				requirePoiDataAndWrite(i);
				mSetWithPlace.add(i);
			}
	}

	public void delData(int index) {
		this.mContext.getContentResolver().delete(MediaUri,
				MediaStore.Images.Media._ID + "=" + mPicInfoList.get(index).id,
				null);
		mPicInfoList.remove(index);
		n--;
		makeSets();
		checkAllPoiData();
	}

	private class Holder {
		public ImageView iv;
		public Bitmap bm;
		public String tag;

		public Holder(ImageView iv, Bitmap bm, String tag) {
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
