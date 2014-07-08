package com.example.photomanager1_0;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ShowImageActivity extends Activity implements OnClickListener{
	Bitmap bitmap =null;
	private ImageView iv;
	private LinearLayout ll;
	private FrameLayout fl;
	private InfoDialog mDialog;
	private String fileRoute;
	//the number of the bitmap in PicInfoList, which is not the source ID
	private int id;
	//origin width and height of the bitmap
	private int o_width,o_height;
	private Animation a;
	private Button b;
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.image_show);
		id = getIntent().getIntExtra("image",-1);
		fileRoute = TimelineActivity.PicInfoList.get(id).fileRoute;
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().
				  appendPath(Long.toString(TimelineActivity.PicInfoList.get(id).id)).build();
		try {
			bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		createScaledBitmap();
		iv = (ImageView)findViewById(R.id.ivImageShow);
		iv.setImageBitmap(bitmap);
		iv.setOnClickListener(this);
		ll=(LinearLayout)findViewById(R.id.ivImageCover);
		fl=(FrameLayout)findViewById(R.id.ivImageFrame);
		b = (Button)findViewById(R.id.ivBackButton);
		b.setText(" < 相册("+(id+1)+"/"+TimelineActivity.PicInfoList.size()+")");
		b.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ShowImageActivity.this.finish();
			}
			
		});
		initAnimation();
	}
	private void initAnimation(){
		a = new ScaleAnimation(1f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f);
		a.setDuration(4000);
		a.setRepeatMode(Animation.RESTART);
		a.setRepeatCount(Animation.INFINITE);
		a.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub		
				getNextBitmap();
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	private void getNextBitmap(){
		int n = TimelineActivity.PicInfoList.size();
		if (id == n - 1) id = 0; else id++;
		iv.setImageBitmap(null);
		bitmap.recycle();
		bitmap=null;
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().
				  appendPath(Long.toString(TimelineActivity.PicInfoList.get(id).id)).build();
		try {
			bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		createScaledBitmap();
		iv.setImageBitmap(bitmap);
		b.setText(" < 相册("+(id+1)+"/"+TimelineActivity.PicInfoList.size()+")");
	}
	private void createScaledBitmap(){
		o_width = bitmap.getWidth();
		o_height = bitmap.getHeight();
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels+1;
		int height = dm.heightPixels+1;
		int t_width;
		int t_height;
		if (bitmap.getWidth()>width || bitmap.getHeight()>height){
			t_width = width;
			t_height = bitmap.getHeight()*width/bitmap.getWidth();
			if (t_height>height){
				t_width = t_width*height/t_height;
				t_height = height;
			}
		} else
		if (bitmap.getWidth()<width && bitmap.getHeight()<height){
			t_width = width;
			t_height = bitmap.getHeight()*width/bitmap.getWidth();
			if (t_height>height){
				t_width = t_width*height/t_height;
				t_height = height;
			}
		} else {
			t_width = bitmap.getWidth();
			t_height = bitmap.getHeight();
		}
		Bitmap temp = bitmap;
		bitmap = Bitmap.createScaledBitmap(temp, t_width, t_height, true);
		temp.recycle();
	}
	
	public void listInfomation(View v){
		//create a dialog to list picture parameters
		mDialog = new InfoDialog(this);
		//Button in the dialog
		
		//use WindowManager to manager mydialog's attributes
		Window mWindow = mDialog.getWindow();
		WindowManager.LayoutParams lp = mWindow.getAttributes();
		mWindow.setGravity(Gravity.CENTER);
		lp.alpha = 0.6f;
		
		DisplayMetrics dm = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(dm);
		lp.width = (int)(dm.widthPixels*0.95);
		lp.height = (int)(dm.heightPixels*0.9);
		mDialog.getWindow().setAttributes(lp);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		mDialog.show();
	}
	
	public void slidePictures(View v){
		Log.i("photo","slide");
		iv.clearAnimation();
		fl.removeView(ll);
		iv.setAnimation(a);
		iv.startAnimation(a);
	}
	@SuppressLint("InlinedApi")
	private ArrayList<String> getPicInfo(){
		ArrayList<String> info = new ArrayList<String>();
		ExifInterface et = null;
		try {
			et = new ExifInterface(fileRoute);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (et==null){
			info.add("信息获取失败...");
			return info;
		}
		
		int fileSplitPlace = fileRoute.lastIndexOf('/');
		info.add("名称:"+fileRoute.substring(fileSplitPlace+1));
		info.add("路径:"+fileRoute.substring(0, fileSplitPlace-1));
		int fileSize = 0;
		File f = new File(fileRoute);
		if (f.exists()){
			FileInputStream fis;
			try {
				fis = new FileInputStream(f);
				fileSize = fis.available();
				fis.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String strSize;
		if (fileSize<1048576){
			strSize = ""+fileSize/1024+"KB";
		} else strSize = ""+1.0*(int)(1.0*fileSize/1048576*100)/100+"MB";
		info.add("文件大小: "+strSize+"("+fileSize+"Byte)");
		info.add("宽高: "+o_width+"*"+o_height);
		if (et.getAttribute(ExifInterface.TAG_DATETIME)!=null)
			info.add("拍摄日期: "+et.getAttribute(ExifInterface.TAG_DATETIME).replaceFirst(":", "/").replaceFirst(":", "/"));
		if (TimelineActivity.PicInfoList.get(id).lalitude!=0)
			info.add("经纬度: "+TimelineActivity.PicInfoList.get(id).lalitude+","+TimelineActivity.PicInfoList.get(id).longitude);
		if (et.getAttribute(ExifInterface.TAG_APERTURE)!=null)
			info.add("光圈值: "+et.getAttribute(ExifInterface.TAG_APERTURE));
		if (et.getAttribute(ExifInterface.TAG_MAKE)!=null)
			info.add("制造者: "+et.getAttribute(ExifInterface.TAG_MAKE));
		if (et.getAttribute(ExifInterface.TAG_MODEL)!=null)
			info.add("设备型号: "+et.getAttribute(ExifInterface.TAG_MODEL));
		if (et.getAttribute(ExifInterface.TAG_FLASH)!=null)
			info.add("闪光灯: "+et.getAttribute(ExifInterface.TAG_FLASH));
		info.add("焦距: "+et.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH,-1)+"mm");
		if (et.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)!=null)
			info.add("曝光时间: "+et.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)+"ms");
		if (et.getAttribute(ExifInterface.TAG_WHITE_BALANCE)!=null)
			info.add("白平衡: "+((et.getAttribute(ExifInterface.TAG_WHITE_BALANCE).equals(String.valueOf(ExifInterface.WHITEBALANCE_AUTO)))?"自动":"手动"));
		if (et.getAttribute(ExifInterface.TAG_ISO)!=null)
			info.add("感光度: "+et.getAttribute(ExifInterface.TAG_ISO));
		if (et.getAttribute(ExifInterface.TAG_ORIENTATION)!=null){
			String ori="";
			switch (Integer.valueOf(et.getAttribute(ExifInterface.TAG_ORIENTATION))){
			case(ExifInterface.ORIENTATION_NORMAL):
				ori = "正常";break;
			case(ExifInterface.ORIENTATION_UNDEFINED):
				ori = "未定义的方向";break;
			case(ExifInterface.ORIENTATION_FLIP_VERTICAL):
				ori = "垂直翻转";break;
			case(ExifInterface.ORIENTATION_FLIP_HORIZONTAL):
				ori = "水平翻转";break;
			case(ExifInterface.ORIENTATION_ROTATE_90):
				ori = "旋转90度";break;
			case(ExifInterface.ORIENTATION_ROTATE_180):
				ori = "旋转180度";break;
			case(ExifInterface.ORIENTATION_ROTATE_270):
				ori = "旋转270度";break;
			case(ExifInterface.ORIENTATION_TRANSPOSE):
				ori = "翻转";break;
			case(ExifInterface.ORIENTATION_TRANSVERSE):
				ori = "横向";break;
			}
			info.add("拍摄方向: "+ori);
		}
		return info;
	}
	@Override
	public void finish()
	{
		if (bitmap!=null){
			bitmap.recycle();
		}
		super.finish();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		iv.clearAnimation();
		if (fl.getChildCount()==1){
			fl.addView(ll);
		} else{
			fl.removeView(ll);
		}
	}
	private class InfoDialog extends Dialog{
		Context context;
		public InfoDialog(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			this.context = context;
		}
		public InfoDialog(Context context, int theme) {
			super(context,theme);
			// TODO Auto-generated constructor stub
			this.context = context;
		}
		@Override
		protected void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			this.setContentView(R.layout.image_show_info);
			Button okButton = (Button)findViewById(R.id.ivInfoOkButton);
			okButton.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mDialog.dismiss();
				}
			});
			
			ListView lv = (ListView) findViewById(R.id.ivInfoListView);
			ArrayList<String> info = getPicInfo();
			lv.setAdapter(new ArrayAdapter<String>(ShowImageActivity.this,R.layout.small_font,R.id.info_tv,info));
		}
	}
}
