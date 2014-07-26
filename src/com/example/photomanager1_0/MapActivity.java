package com.example.photomanager1_0;

import java.util.ArrayList;








import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfigeration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.MyLocationConfigeration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * @author ipip 2014年7月16日下午2:49:25
 */
public class MapActivity extends Activity implements OnGetPoiSearchResultListener, OnGetSuggestionResultListener {
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	private PoiSearch mSearch;
	private SuggestionSearch mSuggestionSearch = null;
	private MyLocationData locData;
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	/**
	 * 搜索关键字输入窗口
	 */
	private boolean first = true;
	private AutoCompleteTextView keyWorldsView = null;
	private ArrayAdapter<String> sugAdapter = null;
	private int load_index, index;
	private ArrayList<PicInfo> PicInfoList = TimelineActivity.PicInfoList;
	private DataGain dg = TimelineActivity.dg;
	private ArrayList<Integer> mSet;
	//private MyOverlay mOverlay;
	private ImageDialog mDialog;
	private ArrayList<ArrayList<Integer>> mPicSet;
	private GridView mRel;
	private LinearLayout ll;
	private boolean llVisible = false;
	private BitmapDescriptor[] cache;
	private int cacheCount=0;
	private MyOverlay pics;
	private MyPoiOverlay poiOverlay;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		initComponents();
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.removeViewAt(2);
		mMapView.removeViewAt(1);
		// 初始化搜索模块，注册搜索事件监听
		mSearch = PoiSearch.newInstance();
		mSearch.setOnGetPoiSearchResultListener(this);
		mSuggestionSearch = SuggestionSearch.newInstance();
		mSuggestionSearch.setOnGetSuggestionResultListener(this);
		keyWorldsView = (AutoCompleteTextView) findViewById(R.id.searchkey);
		sugAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line);
		keyWorldsView.setAdapter(sugAdapter);
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(14.0f);
		mBaiduMap.setMapStatus(u);
		initMapView();
		/**
		 * 当输入关键字变化时，动态更新建议列表
		 */
		keyWorldsView.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				if (cs.length() <= 0) {
					return;
				}
				String city = ((EditText) findViewById(R.id.city)).getText()
						.toString();
				/**
				 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
				 */

				mSuggestionSearch
				.requestSuggestion(new SuggestionSearchOption()
						.keyword(cs.toString()).city(city));
			}
		});
		setLocationManager();
		resetCenterPoint(null);
	}

	private void initComponents() {
		EditText et = (EditText) findViewById(R.id.city);
		et.setSelectAllOnFocus(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}


	private void initMapView() {
		mMapView.setLongClickable(true);
		mBaiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {
			@Override
			public void onMapStatusChange(MapStatus status) {
				
			}

			@Override
			public void onMapStatusChangeFinish(MapStatus arg0) {
				// TODO Auto-generated method stub
				addPicOverlays();
			}

			@Override
			public void onMapStatusChangeStart(MapStatus arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		cache = new BitmapDescriptor[dg.getCount()];
		pics = new MyOverlay(mBaiduMap);
		mBaiduMap.setOnMarkerClickListener(pics);
	}

	private void addPicOverlays() {
		//mBaiduMap.clear();
		mSet = dg.getSetWithPlace();
		pics.removeFromMap();
		pics.clear();
		for (int i=0;i<cacheCount;i++) if (cache[i]!=null){
			cache[i].recycle();
			cache[i]=null;
		}
		cacheCount=0;
		mPicSet = new ArrayList<ArrayList<Integer>>();
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = (int) (dm.widthPixels), height = (int) (dm.heightPixels);
		int length = (int)(Math.min(width, height)*0.20);
		//Log.i("photo", "dm.w: "+dm.widthPixels+" dm.h: "+dm.heightPixels+" dm.d"+dm.density);
		for (int i = 0; i < mSet.size(); i++) {
			PicInfo info = PicInfoList.get(mSet.get(i));
			Point screenOn = new Point();
			screenOn = mBaiduMap.getProjection().toScreenLocation(info.pl);
			if (screenOn.x < 0 || screenOn.y < 0 || screenOn.x > width
					|| screenOn.y > height)
				continue;
			boolean side = false;
			for (int j = 0; j < mPicSet.size(); j++) {
				ArrayList<Integer> p = mPicSet.get(j);
				PicInfo infoP = PicInfoList.get(p.get(0));
				Point screenOn1 = new Point();
				screenOn1 = mBaiduMap.getProjection().toScreenLocation(infoP.pl);
				if (Math.abs(screenOn.x - screenOn1.x) < 200
						&& Math.abs(screenOn.y - screenOn1.y) < 200) {
					p.add(mSet.get(i));
					side = true;
					break;
				}
			}
			if (!side) {
				ArrayList<Integer> p = new ArrayList<Integer>();
				p.add(mSet.get(i));
				mPicSet.add(p);
			}
		}
		//Log.i("pic", ""+mPicSet.size());
		for (int i = 0; i < mPicSet.size(); i++) {
			PicInfo[] info = new PicInfo[3];
			int m = 3;
			for (int j = 0; j < 3; j++)
				if (j < mPicSet.get(i).size()) {
					info[j] = PicInfoList.get(mPicSet.get(i).get(j));
					if (info[j].bitmap == null) {
						m = j;
						break;
					}
				} else {
					m = j;
					break;
				}
			if (m == 0)
				continue;
			
			Bitmap b = Bitmap.createBitmap(length - 20 + m * 20, length - 15 + m * 15,
					info[0].bitmap.getConfig());
			Canvas canvas = new Canvas(b);
			for (int j = m - 1; j >= 0; j--) {
				canvas.drawBitmap(info[j].bitmap,
						new Rect(0, 0, info[j].bitmap.getWidth(),
								info[j].bitmap.getHeight()), new RectF(j * 20,
										j * 15, length + j * 20,length + j * 15), null);
			} // draw at most three picture in one set;
			cache[cacheCount] = BitmapDescriptorFactory.fromBitmap(new BitmapDrawable(null, b).getBitmap());
			LatLng gp =info[0].pl;
			OverlayOptions oo = new MarkerOptions().position(gp).icon(cache[cacheCount])
					.zIndex(-1).title(String.valueOf(i));
			pics.add(oo);
			cacheCount++;
		}
		pics.addToMap();
		//if (poiOverlay!=null) poiOverlay.addToMap();
	}

	private void setLocationManager() {
		mLocClient = new LocationClient(getApplicationContext());	
		LocationMode mCurrentMode = LocationMode.NORMAL;
		mBaiduMap
				.setMyLocationConfigeration(new MyLocationConfigeration(
						mCurrentMode, false, null));
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(100000);
		mLocClient.setLocOption(option);
	} 

	// 重置地图中心点
	public void resetCenterPoint(View v) {  
		mBaiduMap.setMyLocationEnabled(true);
		first = true;
		mLocClient.start();
		mLocClient.requestLocation();
		Toast.makeText(MapActivity.this, "正在定位……", Toast.LENGTH_SHORT).show();
	}

	public void showSearchPanel(View v) {
		ll = (LinearLayout) findViewById(R.id.mapSearchBlock);
		if (llVisible){
			llVisible = false;
			Animation a = new AlphaAnimation(1.0f, 0.0f);
			a.setDuration(800);
			a.setAnimationListener(new AnimationListener(){

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					ll.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
			});
			ll.startAnimation(a);
		}
		else{
			llVisible = true;
			Animation a = new AlphaAnimation(0.0f, 1.0f);
			a.setDuration(800);
			a.setAnimationListener(new AnimationListener(){

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					ll.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
			});
			ll.startAnimation(a);
		}
	}


	/**
	 * 影响搜索按钮点击事件
	 * 
	 * @param v
	 */
	public void searchButtonProcess(View v) {
		EditText editCity = (EditText) findViewById(R.id.city);
		EditText editSearchKey = (EditText) findViewById(R.id.searchkey);
		mSearch.searchInCity(new PoiCitySearchOption().city(editCity.getText().toString()).keyword(editSearchKey
				.getText().toString()).pageNum(load_index).pageCapacity(9));
		InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(MapActivity.this.getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}
	public void goToLastPage(View v){
		if (load_index == 0 ){
			Toast.makeText(this, "这是第一组数据", Toast.LENGTH_SHORT).show();;
		} else{
			load_index--;
			searchButtonProcess(null);
		}
	}
	public void goToNextPage(View v) {
		// 搜索下一组poi
		load_index++;
		searchButtonProcess(null);
	}

	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;
			locData = new MyLocationData.Builder()
			.accuracy(location.getRadius())
			// 此处设置开发者获取到的方向信息，顺时针0-360
			.latitude(location.getLatitude())
			.longitude(location.getLongitude()).build();
			// 此处可以设置 locData的方向信息, 如果定位 SDK 未返回方向信息，用户可以自己实现罗盘功能添加方向信息。
			mBaiduMap.setMyLocationData(locData);
			//mLocClient.stop();
			//mBaiduMap.setMyLocationEnabled(false);
			if (first) {
				first = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				//Log.i("gps","in "+ll.latitude+" "+ll.longitude);
				mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(ll));
				new Handler().postDelayed(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomTo(14));
					}
					
				}, 400);
				GeoCoder ss = GeoCoder.newInstance();
				ss.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener(){

					@Override
					public void onGetGeoCodeResult(GeoCodeResult arg0) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onGetReverseGeoCodeResult(
							ReverseGeoCodeResult res) {
						// TODO Auto-generated method stub
						EditText et = (EditText)MapActivity.this.findViewById(R.id.city);
						et.setText(res.getAddressDetail().city);
					}
					
				});
				ss.reverseGeoCode(new ReverseGeoCodeOption().location(ll));
			}
		}
		public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null) {
				return;
			}
		}
	}
	public class MyOverlay extends OverlayManager {
		private List<OverlayOptions> loo;
		public MyOverlay(BaiduMap arg0) {
			super(arg0);
			loo = new ArrayList<OverlayOptions>();
			// TODO Auto-generated constructor stub
		}
		public void add(OverlayOptions oo){
			loo.add(oo);
		}
		
		public void clear(){
			loo.clear();
		}
		
/*
		@Override
		public boolean onClick(int tapIndex) {
			index = tapIndex;
			if (index >= mPicSet.size())
				return false;
			mDialog = new ImageDialog(MapActivity.this);
			WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
			mDialog.getWindow().setGravity(Gravity.CENTER);
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			lp.width = WindowManager.LayoutParams.MATCH_PARENT;
			lp.height = (int) (dm.heightPixels * 0.9);
			mDialog.getWindow().setAttributes(lp);
			mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			PicInfo info = PicInfoList.get(mSet.get(mPicSet.get(index).get(0)));
			Point screenOn = new Point();
			screenOn = mBaiduMap.getProjection().toScreenLocation(info.pl);
			mDialog.showDialog(screenOn.x, screenOn.y);

			PictureAdapter pa = new PictureAdapter(MapActivity.this);
			mRel.setAdapter(pa);
			mRel.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MapActivity.this,
							ShowImageActivity.class);
					intent.putExtra("image",
							mSet.get(mPicSet.get(index).get(arg2)));
					startActivity(intent);
				}

			});
			return true;
		}
*/
		@Override
		public boolean onMarkerClick(Marker m) {
			// TODO Auto-generated method stub
			index = Integer.parseInt(m.getTitle());
			mDialog = new ImageDialog(MapActivity.this);
			WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
			mDialog.getWindow().setGravity(Gravity.CENTER);
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			lp.width = WindowManager.LayoutParams.MATCH_PARENT;
			lp.height = (int) (dm.heightPixels * 0.9);
			mDialog.getWindow().setAttributes(lp);
			mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			Log.i("",""+index+" "+mPicSet.size()+" ");
			PicInfo info = PicInfoList.get(mPicSet.get(index).get(0));
			Point screenOn = new Point();
			screenOn = mBaiduMap.getProjection().toScreenLocation(info.pl);
			mDialog.showDialog(screenOn.x, screenOn.y);

			PictureAdapter pa = new PictureAdapter(MapActivity.this);
			mRel.setAdapter(pa);
			mRel.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MapActivity.this,
							ShowImageActivity.class);
					intent.putExtra("image",mPicSet.get(index).get(arg2));
					startActivity(intent);
				}

			});
			return true;
		}

		@Override
		public List<OverlayOptions> getOverlayOptions() {
			// TODO Auto-generated method stub
			return loo;
		}

	}

	private class PictureAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public PictureAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ImageView holder;
			if (convertView == null) {
				int lid = R.layout.map_ontap_grid;
				;
				holder = new ImageView(MapActivity.this);
				convertView = mInflater.inflate(lid, null);
				holder = (ImageView) convertView
						.findViewById(R.id.mapgridimage);
				convertView.setTag(holder);
			} else {
				holder = (ImageView) convertView.getTag();
			}
			holder.setImageBitmap(PicInfoList.get(mPicSet.get(index)
					.get(position)).bitmap);
			ScaleAnimation a = new ScaleAnimation(1.5f, 0.95f,
					1.5f, 0.95f, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			a.setDuration(400);
			holder.setAnimation(a);
			holder.startAnimation(a);
			return convertView;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mPicSet.get(index).size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return PicInfoList.get(mSet.get(mPicSet.get(index).get(position))).bitmap;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

	}

	private class ImageDialog extends Dialog {
		private Window window;

		public ImageDialog(Context context) {
			super(context);
		}

		public ImageDialog(Context context, int theme) {
			super(context, theme);
		}

		public void showDialog(int x, int y) {
			setContentView(R.layout.map_tap_image_show);
			windowDeploy(x, y);
			show();
		}

		private void windowDeploy(int x, int y) {
			window = getWindow();
			window.setWindowAnimations(R.style.grid_animstyle);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mRel = (GridView) findViewById(R.id.maptapimageshowgridview);
			Button okButton = (Button) findViewById(R.id.maptapimageshowokbutton);
			okButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mDialog.dismiss();
				}
			});

		}
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult result) {
		// TODO Auto-generated method stub
		if (result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MapActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(MapActivity.this, "成功，查看详情页面", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onGetPoiResult(PoiResult result) {
		// TODO Auto-generated method stub
		if (poiOverlay!=null)poiOverlay.removeFromMap();
		if (result == null
				|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			Toast.makeText(this, "没有找到结果", Toast.LENGTH_SHORT);
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			poiOverlay = new MyPoiOverlay(mBaiduMap);
			poiOverlay.setPoiSearch(mSearch);
			mBaiduMap.setOnMarkerClickListener(poiOverlay);
			poiOverlay.setData(result);
			poiOverlay.addToMap();
			poiOverlay.zoomToSpan();
			return;
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

			// 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
			String strInfo = "在";
			for (CityInfo cityInfo : result.getSuggestCityList()) {
				strInfo += cityInfo.city;
				strInfo += ",";
			}
			strInfo += "找到结果";
			Toast.makeText(MapActivity.this, strInfo, Toast.LENGTH_LONG)
					.show();
		}

	}

	@Override
	public void onGetSuggestionResult(SuggestionResult res) {
		// TODO Auto-generated method stub
		if (res == null || res.getAllSuggestions() == null) {
			return;
		}
		sugAdapter.clear();
		for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
			if (info.key != null)
				sugAdapter.add(info.key);
		}
		sugAdapter.notifyDataSetChanged();
	}
}