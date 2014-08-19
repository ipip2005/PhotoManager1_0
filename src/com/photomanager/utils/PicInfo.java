package com.photomanager.utils;

import java.util.Calendar;

import com.baidu.mapapi.model.LatLng;

import android.graphics.Bitmap;

public class PicInfo {
	public Bitmap bitmap=null;
	public String title=null;
	public String text=null;
	public Calendar mdate;
	public LatLng pl=null;
	public Long id;
	public String fileRoute;
	public boolean poiConverted = false;
	public String folderName;
}
