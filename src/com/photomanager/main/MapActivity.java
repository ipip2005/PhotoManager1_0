package com.photomanager.main;

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
import com.baidu.mapapi.model.LatLngBounds;
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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
 * 
 * @author ipip 2014/8/11 13:59
 *
 */
public class MapActivity extends Activity implements OnGetPoiSearchResultListener, OnGetSuggestionResultListener {
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	private PoiSearch mSearch;
	private SuggestionSearch mSuggestionSearch = null;
	private MyLocationData locData;
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
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
	private MyOverlay pics, myLocOverlay;
	private MyPoiOverlay poiOverlay;
	private LatLngBounds llb;
	private int addCount = 0, markerLength = 0;
	private  Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			Pair<Integer, Integer> p = (Pair<Integer, Integer>)msg.obj;
			if (p.first.intValue() != addCount) return;
			refreshOverlayMarker(p.second);
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		initComponents();
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.removeViewAt(2);
		mMapView.removeViewAt(1);
		mSearch = PoiSearch.newInstance();
		mSearch.setOnGetPoiSearchResultListener(this);
		mSuggestionSearch = SuggestionSearch.newInstance();
		mSuggestionSearch.setOnGetSuggestionResultListener(this);
		keyWorldsView = (AutoCompleteTextView) findViewById(R.id.searchkey);
		sugAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line);
		keyWorldsView.setAdapter(sugAdapter);
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(13.0f);
		mBaiduMap.setMapStatus(u);
		initMapView();
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
		if (Main.s.get("city")!=null)
			et.setText((String)Main.s.get("city"));
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
		//Log.i("MapActivity", "last_"+Main.s.get("last-location-x"));
		
		if ((Main.s.get("last-location-x"))!=null){
			mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(
					new LatLng(Double.parseDouble(Main.s.get("last-location-x").toString()),
							   Double.parseDouble(Main.s.get("last-location-y").toString()))));
		}
	}

	private void addPicOverlays() {
		//mBaiduMap.clear();
		addCount++;
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
		markerLength = (int)(Math.min(width, height)*0.20);
		//Log.i("photo", "dm.w: "+dm.widthPixels+" dm.h: "+dm.heightPixels+" dm.d"+dm.density);
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (int i = 0; i < mSet.size(); i++) {
			PicInfo info = PicInfoList.get(mSet.get(i)); 
			builder.include(info.pl);		
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
		llb = builder.build();
		//Log.i("pic", ""+mPicSet.size());
		for (int i = 0; i < mPicSet.size(); i++){
			refreshOverlayMarker(i);
			cacheCount++;
		}
		for (int i = 0; i < mPicSet.size(); i++){
			for (int j = 0; j < 3; j++) if (j < mPicSet.get(i).size()){
				int id = mPicSet.get(i).get(j);
				String key = DataGainUtil.getInstance().generateKey(id, DataGainUtil.SMALL);
				if (TimelineActivity.dg.getDataNow(key) == null)
					TimelineActivity.dg.getData(id, new Pair<Integer, Integer>(addCount, i), 
							key, mHandler);
			}
		}
		pics.addToMap();
	}
	private void refreshOverlayMarker(int set_id){
		int i = set_id;
		Bitmap[] bitmaps = new Bitmap[4];
		int m = 3;
		for (int j = 0; j < 4; j++)
			if (j < mPicSet.get(i).size()) {
				String key = DataGainUtil.getInstance().generateKey(mPicSet.get(i).get(j), DataGainUtil.SMALL);
				bitmaps[j] = TimelineActivity.dg.getDataNow(key);
			} else {
				m = j;
				break;
			}
		if (bitmaps[0] == null) return;
		Bitmap b = Bitmap.createBitmap(markerLength - 20 + m * 20, markerLength - 10 + m * 10,
				bitmaps[0].getConfig());
		Canvas canvas = new Canvas(b);
		for (int j = m - 1; j >= 0; j--) {
			if (bitmaps[j] != null){
				int bitmap_length = Math.min(bitmaps[j].getWidth(), bitmaps[j].getHeight());
				canvas.drawBitmap(bitmaps[j], new Rect(0, 0, bitmap_length, bitmap_length), new RectF(j * 20,
						j * 10, markerLength + j * 20, markerLength + j * 10), null);
			}
			else {
				Paint p = new Paint();
				p.setColor(Color.argb(255, 200, 200, 200));
				canvas.drawRect(new RectF(j * 20,
						j * 10, markerLength + j * 20, markerLength + j * 10), p);
			} 
				
		}
		if (cache[i] != null){
			cache[i].recycle();
			cache[i] = null;
		}
		cache[i] = BitmapDescriptorFactory.fromBitmap(new BitmapDrawable(null, b).getBitmap());
		LatLng gp = TimelineActivity.PicInfoList.get(mPicSet.get(i).get(0)).pl;
		pics.setOverlayOption(i, new MarkerOptions().position(gp).icon(cache[i])
				.zIndex(-1).title(String.valueOf(i)));
		pics.addToMap();
	}
	/**
	 * establish a Location Overlay
	 */
	private void createMyOverlay(){
		myLocOverlay.removeFromMap();
		myLocOverlay.clear();
		BitmapDescriptor p  = BitmapDescriptorFactory.fromResource(R.drawable.icon_myloc);
		OverlayOptions oo = new MarkerOptions().position(new LatLng(locData.latitude,locData.longitude))
				.icon(p).zIndex(0).title("here");
		myLocOverlay.add(oo);
		myLocOverlay.addToMap();
	}
	private void setLocationManager() {
		mLocClient = new LocationClient(getApplicationContext());	
		LocationMode mCurrentMode = LocationMode.NORMAL;
		mBaiduMap
				.setMyLocationConfigeration(new MyLocationConfigeration(
						mCurrentMode, false, null));
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setCoorType("bd09ll");
		option.setScanSpan(100000);
		mLocClient.setLocOption(option);
		myLocOverlay = new MyOverlay(mBaiduMap);
	} 

	public void resetCenterPoint(View v) {  
		mBaiduMap.setMyLocationEnabled(true);
		mLocClient.start();
		mLocClient.requestLocation();
		Toast.makeText(MapActivity.this, "定位中...", Toast.LENGTH_SHORT).show();
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
	public void spanMapToSeePics(View v){
		if (mSet != null && mSet.size() > 0 && pics != null){
			pics.zoomToSpan();
			MapStatusUpdate m = MapStatusUpdateFactory.newLatLngBounds(llb);
			//mBaiduMap.setMapStatus(m);
			mBaiduMap.animateMapStatus(m);
			Toast.makeText(this, "所有图片可见", Toast.LENGTH_SHORT).show();
		} else 
		if (mSet != null && mSet.size() == 0){
			Toast.makeText(this, "无带坐标图片，请确认拍照时开启定位。", Toast.LENGTH_SHORT).show();
		} else{
			Toast.makeText(this, "未载入...请重试", Toast.LENGTH_SHORT).show();
		}
	}

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
			Toast.makeText(this, "这已是最前页索引", Toast.LENGTH_SHORT).show();;
		} else{
			load_index--;
			searchButtonProcess(null);
		}
	}
	public void goToNextPage(View v) {
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
			.latitude(location.getLatitude())
			.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			LatLng ll = new LatLng(location.getLatitude(),
					location.getLongitude());
			mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(ll),400);
			new Handler().postDelayed(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomTo(14));
				}
				
			}, 500);
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
					Main.s.put("city", res.getAddressDetail().city);
				}
				
			});
			ss.reverseGeoCode(new ReverseGeoCodeOption().location(ll));
			createMyOverlay();
			mBaiduMap.setMyLocationEnabled(false);
			mLocClient.stop();
			Main.s.put("last-location-x", ll.latitude);
			Main.s.put("last-location-y", ll.longitude);
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
		
		@Override
		public boolean onMarkerClick(Marker m) {
			// TODO Auto-generated method stub
			if (m.getTitle().equals("here")) {
				Toast.makeText(MapActivity.this, "我在这里", Toast.LENGTH_SHORT).show();
				return true;
			}
			index = Integer.parseInt(m.getTitle());
			mDialog = new ImageDialog(MapActivity.this, R.style.dialog);
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
		
		public OverlayOptions getOverlayOption(int index){
			return loo.get(index);
		}
		
		public void setOverlayOption(int index, OverlayOptions oo){
			if (index <= loo.size()) loo.add(oo); else
				loo.set(index, oo);
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
			SquareLayout holder;
			if (convertView == null) {
				int lid = R.layout.map_tap_image_show_image;
				holder = new SquareLayout(MapActivity.this);
				convertView = mInflater.inflate(lid, null);
				holder = (SquareLayout) convertView
						.findViewById(R.id.square);
				convertView.setTag(holder);
			} else {
				holder = (SquareLayout) convertView.getTag();
			}
			ImageView iv = (ImageView)holder.findViewById(R.id.image);
			iv.setImageBitmap(null);
			int id = mPicSet.get(index).get(position);
			String key = DataGainUtil.getInstance().generateKey(id, DataGainUtil.SMALL);
			TimelineActivity.dg.getData(id, iv, key);
			ScaleAnimation a = new ScaleAnimation(0.7f, 0.95f,
					0.7f, 0.95f, Animation.RELATIVE_TO_SELF, 0.5f,
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
			return PicInfoList.get(mPicSet.get(index).get(position)).bitmap;
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
			Toast.makeText(MapActivity.this, "加载完成", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(MapActivity.this, "网络错误", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onGetPoiResult(PoiResult result) {
		// TODO Auto-generated method stub
		if (poiOverlay!=null)poiOverlay.removeFromMap();
		if (result == null
				|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			Toast.makeText(this, "没有找到内容", Toast.LENGTH_SHORT);
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
			String strInfo = "在";
			for (CityInfo cityInfo : result.getSuggestCityList()) {
				strInfo += cityInfo.city;
				strInfo += ",";
			}
			strInfo += "找到相关内容";
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