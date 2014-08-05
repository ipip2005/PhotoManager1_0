package com.photomanager.main;


import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiSearch;

public class MyPoiOverlay extends PoiOverlay {
    PoiSearch mPoiSearch;
	public MyPoiOverlay(BaiduMap baiduMap) {
		super(baiduMap);
	}
	public void setPoiSearch(PoiSearch poiSearch){
		mPoiSearch = poiSearch;
	}
	@Override
	public boolean onPoiClick(int index) {
		super.onPoiClick(index);
		PoiInfo poi = getPoiResult().getAllPoi().get(index);
		if (poi.hasCaterDetails) {
			mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
					.poiUid(poi.uid));
		}
		return true;
	}

    
}
