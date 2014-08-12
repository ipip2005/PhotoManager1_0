package com.photomanager.main;

import java.util.Calendar;

import com.baidu.mapapi.model.LatLng;

import android.graphics.Bitmap;
import android.net.Uri;

public class PicInfo {
	Bitmap bitmap=null;
	String title=null;
	String text=null;
	Calendar mdate;
	LatLng pl=null;
	Long id;
	String fileRoute;
	Uri uri =null;
}
