package com.example.photomanager1_0;

import java.util.ArrayList;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionInfo;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;

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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
public class MapActivity extends Activity {
	private MapView mMapView = null;
	private BMapManager mBMapManager = null;
	LocationClient mLocClient;
	LocationData locData = null;
	public MyLocationListenner myListener = new MyLocationListenner();
	private MKSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用
	/**
	 * 搜索关键字输入窗口
	 */
	private boolean first = true;
	private AutoCompleteTextView keyWorldsView = null;
	private ArrayAdapter<String> sugAdapter = null;
	private int load_Index, index;
	private ArrayList<PicInfo> PicInfoList = TimelineActivity.PicInfoList;
	private DataGain dg = TimelineActivity.dg;
	private ArrayList<Integer> mSet;
	private MyOverlay mOverlay;
	private ImageDialog mDialog;
	private ArrayList<ArrayList<Integer>> mPicSet;
	private GridView mRel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * 使用地图sdk前需先初始化BMapManager. BMapManager是全局的，可为多个MapView共用，它需要地图模块创建前创建，
		 * 并在地图地图模块销毁后销毁，只要还有地图模块在使用，BMapManager就不应该销毁
		 */
		if (mBMapManager == null) {
			mBMapManager = new BMapManager(getApplicationContext());
			/**
			 * 如果BMapManager没有初始化则初始化BMapManager
			 */
			mBMapManager.init(new MyGeneralListener());
		}
		setContentView(R.layout.activity_map);
		initComponents();
		mMapView = (MapView) findViewById(R.id.bmapView);
		initMapView();
		// 初始化搜索模块，注册搜索事件监听
		mSearch = new MKSearch();
		mSearch.init(mBMapManager, new MKSearchListener() {
			// 在此处理详情页结果
			@Override
			public void onGetPoiDetailSearchResult(int type, int error) {
				if (error != 0) {
					Toast.makeText(MapActivity.this, "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(MapActivity.this, "成功，查看详情页面",
							Toast.LENGTH_SHORT).show();
				}
			}

			/**
			 * 在此处理poi搜索结果
			 */
			public void onGetPoiResult(MKPoiResult res, int type, int error) {
				// 错误号可参考MKEvent中的定义
				if (error != 0 || res == null) {
					Toast.makeText(MapActivity.this, "抱歉，未找到结果",
							Toast.LENGTH_LONG).show();
					return;
				}
				// 将地图移动到第一个POI中心点
				if (res.getCurrentNumPois() > 0) {
					// 将poi结果显示到地图上
					MyPoiOverlay poiOverlay = new MyPoiOverlay(
							MapActivity.this, mMapView, mSearch);
					poiOverlay.setData(res.getAllPoi());
					mMapView.getOverlays().clear();
					mMapView.getOverlays().add(mOverlay);
					mMapView.getOverlays().add(poiOverlay);
					Toast.makeText(MapActivity.this, "选择任意箭头查看",
							Toast.LENGTH_SHORT).show();
					mMapView.refresh();
					// 当ePoiType为2（公交线路）或4（地铁线路）时， poi坐标为空
					for (MKPoiInfo info : res.getAllPoi()) {
						if (info.pt != null) {
							mMapView.getController().setZoom(14);
							mMapView.getController().animateTo(info.pt);
							break;
						}
					}
				} else if (res.getCityListNum() > 0) {
					// 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
					String strInfo = "在";
					for (int i = 0; i < res.getCityListNum(); i++) {
						strInfo += res.getCityListInfo(i).city;
						strInfo += ",";
					}
					strInfo += "找到结果";
					Toast.makeText(MapActivity.this, strInfo, Toast.LENGTH_LONG)
							.show();
				}
			}

			public void onGetDrivingRouteResult(MKDrivingRouteResult res,
					int error) {
			}

			public void onGetTransitRouteResult(MKTransitRouteResult res,
					int error) {
			}

			public void onGetWalkingRouteResult(MKWalkingRouteResult res,
					int error) {
			}

			public void onGetAddrResult(MKAddrInfo res, int error) {
				EditText editCity = (EditText) MapActivity.this
						.findViewById(R.id.city);
				editCity.setText(res.addressComponents.city);
			}

			public void onGetBusDetailResult(MKBusLineResult result, int iError) {
			}

			/**
			 * 更新建议列表
			 */
			@Override
			public void onGetSuggestionResult(MKSuggestionResult res, int arg1) {
				if (res == null || res.getAllSuggestions() == null) {
					return;
				}
				sugAdapter.clear();
				for (MKSuggestionInfo info : res.getAllSuggestions()) {
					if (info.key != null)
						sugAdapter.add(info.key);
				}
				sugAdapter.notifyDataSetChanged();

			}

			@Override
			public void onGetShareUrlResult(MKShareUrlResult result, int type,
					int error) {
				// TODO Auto-generated method stub

			}
		});

		keyWorldsView = (AutoCompleteTextView) findViewById(R.id.searchkey);
		sugAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line);
		keyWorldsView.setAdapter(sugAdapter);

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
				mSearch.suggestionSearch(cs.toString(), city);
			}
		});
		setLocationManager();
		resetCenterPoint();
	}

	private void initComponents() {
		EditText et = (EditText) findViewById(R.id.city);
		et.setSelectAllOnFocus(true);
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mMapView.destroy();
		mSearch.destory();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mMapView.onRestoreInstanceState(savedInstanceState);
	}

	private void initMapView() {
		mMapView.setLongClickable(true);
		mMapView.getController().setZoom(14);
		mMapView.getController().enableClick(true);
		mMapView.regMapViewListener(mBMapManager, new MKMapViewListener() {

			@Override
			public void onClickMapPoi(MapPoi arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetCurrentMap(Bitmap arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMapAnimationFinish() {
				// TODO Auto-generated method stub
				addPicOverlays();

			}

			@Override
			public void onMapLoadFinish() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMapMoveFinish() {
				// TODO Auto-generated method stub
				addPicOverlays();
			}

		});
		mSet = dg.getSetWithPlace();
		mOverlay = new MyOverlay(getResources().getDrawable(
				R.drawable.icon_gcoding), mMapView);
		mMapView.getOverlays().add(mOverlay);
	}

	private void addPicOverlays() {
		mOverlay.removeAll();
		mPicSet = new ArrayList<ArrayList<Integer>>();
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = (int) (dm.widthPixels), height = (int) (dm.heightPixels);
		for (int i = 0; i < mSet.size(); i++) {
			PicInfo info = PicInfoList.get(mSet.get(i));
			Point screenOn = new Point();
			mMapView.getProjection().toPixels(
					new GeoPoint((int) (1e6 * info.lalitude),
							(int) (1e6 * info.longitude)), screenOn);
			if (screenOn.x < 0 || screenOn.y < 0 || screenOn.x > width
					|| screenOn.y > height)
				continue;
			boolean side = false;
			for (int j = 0; j < mPicSet.size(); j++) {
				ArrayList<Integer> p = mPicSet.get(j);
				PicInfo infoP = PicInfoList.get(mSet.get(p.get(0)));
				Point screenOn1 = new Point();
				mMapView.getProjection().toPixels(
						new GeoPoint((int) (1e6 * infoP.lalitude),
								(int) (1e6 * infoP.longitude)), screenOn1);
				if (Math.abs(screenOn.x - screenOn1.x) < 200
						&& Math.abs(screenOn.y - screenOn1.y) < 200) {
					p.add(i);
					side = true;
					break;
				}
			}
			if (!side) {
				ArrayList<Integer> p = new ArrayList<Integer>();
				p.add(i);
				mPicSet.add(p);
			}
		}
		for (int i = 0; i < mPicSet.size(); i++) {
			PicInfo[] info = new PicInfo[3];
			int m = 3;
			for (int j = 0; j < 3; j++)
				if (j < mPicSet.get(i).size()) {
					info[j] = PicInfoList.get(mSet.get(mPicSet.get(i).get(j)));
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
			GeoPoint gp = new GeoPoint((int) (info[0].lalitude * 1e6),
					(int) (info[0].longitude * 1e6));
			OverlayItem oi = new OverlayItem(gp, "", "");
			Bitmap b = Bitmap.createBitmap(180 + m * 20, 230,
					info[0].bitmap.getConfig());
			Canvas canvas = new Canvas(b);
			for (int j = m - 1; j >= 0; j--) {
				canvas.drawBitmap(info[j].bitmap,
						new Rect(0, 0, info[j].bitmap.getWidth(),
								info[j].bitmap.getHeight()), new RectF(j * 20,
								j * 15, 200 + j * 20, 200 + j * 15), null);
			} // draw at most three picture in one set;
			oi.setMarker(new BitmapDrawable(null, b));
			mOverlay.addItem(oi);
		}
		mMapView.refresh();
	}

	private void setLocationManager() {
		mLocClient = new LocationClient(getApplicationContext());
		locData = new LocationData();
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		option.setServiceName("com.baidu.location.service");
		mLocClient.setLocOption(option);
	}

	// 重置地图中心点
	public void resetCenterPoint() {
		mLocClient.start();
		mLocClient.requestLocation();

		Toast.makeText(MapActivity.this, "正在定位……", Toast.LENGTH_SHORT).show();
	}

	public void resetCenterPoint(View v) {
		resetCenterPoint();
	}

	public void showSearchPanel(View v) {
		LinearLayout ll = (LinearLayout) findViewById(R.id.mapSearchBlock);
		if (ll.getVisibility() == View.VISIBLE)
			ll.setVisibility(View.GONE);
		else
			ll.setVisibility(View.VISIBLE);
	}

	public void closeSearch(View v) {
		LinearLayout ll = (LinearLayout) findViewById(R.id.mapSearchBlock);
		ll.setVisibility(View.GONE);
	}

	/**
	 * 影响搜索按钮点击事件
	 * 
	 * @param v
	 */
	public void searchButtonProcess(View v) {
		EditText editCity = (EditText) findViewById(R.id.city);
		EditText editSearchKey = (EditText) findViewById(R.id.searchkey);
		mSearch.poiSearchInCity(editCity.getText().toString(), editSearchKey
				.getText().toString());
	}

	public void goToNextPage(View v) {
		// 搜索下一组poi
		int flag = mSearch.goToPoiPage(++load_Index);
		if (flag != 0) {
			Toast.makeText(MapActivity.this, "先搜索开始，然后再搜索下一组数据",
					Toast.LENGTH_SHORT).show();
		}
	}

	private class MyGeneralListener implements MKGeneralListener {

		@Override
		public void onGetNetworkState(int iError) {
			if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
				Toast.makeText(MapActivity.this, "您的网络出错啦！", Toast.LENGTH_LONG)
						.show();
			} else if (iError == MKEvent.ERROR_NETWORK_DATA) {
				Toast.makeText(MapActivity.this, "输入正确的检索条件！",
						Toast.LENGTH_LONG).show();
			}
			// ...
		}

		@Override
		public void onGetPermissionState(int iError) {
			// 非零值表示key验证未通过
			if (iError != 0) {
				// 授权Key错误：
				Toast.makeText(
						MapActivity.this,
						"AndroidManifest.xml 文件输入正确的授权Key,并检查您的网络连接是否正常！error: "
								+ iError, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(MapActivity.this, "key认证成功", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;

			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();
			// 如果不显示定位精度圈，将accuracy赋值为0即可
			locData.accuracy = location.getRadius();
			// 此处可以设置 locData的方向信息, 如果定位 SDK 未返回方向信息，用户可以自己实现罗盘功能添加方向信息。
			locData.direction = location.getDerect();
			// 移动地图到定位点
			MKPoiInfo pInfo = new MKPoiInfo();
			pInfo.pt = new GeoPoint((int) (locData.latitude * 1e6),
					(int) (locData.longitude * 1e6));
			// 移动完成
			pInfo.name = "我在这儿";
			ArrayList<MKPoiInfo> myP = new ArrayList<MKPoiInfo>();
			myP.add(pInfo);

			MyPoiOverlay poiOverlay = new MyPoiOverlay(MapActivity.this,
					mMapView, mSearch);
			poiOverlay.setData(myP);
			mMapView.getOverlays().clear();
			mMapView.getOverlays().add(mOverlay);
			mMapView.getOverlays().add(poiOverlay);
			mMapView.getController().setZoom(14);
			mMapView.getController().animateTo(pInfo.pt);
			mMapView.refresh();
			mLocClient.stop();
			if (first) {
				mSearch.reverseGeocode(pInfo.pt);
				first = false;
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null) {
				return;
			}
		}
	}

	public class MyOverlay extends ItemizedOverlay {

		public MyOverlay(Drawable defaultMarker, MapView mapView) {
			super(defaultMarker, mapView);
		}

		@Override
		public boolean onTap(int tapIndex) {
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
			mMapView.getProjection().toPixels(
					new GeoPoint((int) (1e6 * info.lalitude),
							(int) (1e6 * info.longitude)), screenOn);
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
		/*
		 * @Override public boolean onTap(GeoPoint pt , MapView mMapView){ if
		 * (pop != null){ pop.hidePop(); mMapView.removeView(button); } return
		 * false; }
		 */

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
			holder.setImageBitmap(PicInfoList.get(mSet.get(mPicSet.get(index)
					.get(position))).bitmap);
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
		private Context context;
		private Window window;

		public ImageDialog(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			this.context = context;

		}

		public ImageDialog(Context context, int theme) {
			super(context, theme);
			// TODO Auto-generated constructor stub
			this.context = context;
		}

		public void showDialog(int x, int y) {
			setContentView(R.layout.map_tap_image_show);
			windowDeploy(x, y);
			show();
		}

		private void windowDeploy(int x, int y) {
			window = getWindow();
			window.setWindowAnimations(R.style.grid_animstyle);
			/*
			 * WindowManager.LayoutParams l = window.getAttributes(); l.gravity
			 * = Gravity.LEFT | Gravity.TOP; l.x = x; l.y = y;
			 * window.setAttributes(l);
			 */
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
}