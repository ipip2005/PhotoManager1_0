package com.photomanager.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import utils.DataGainUtil;

import com.photomagner.widgets.JazzyViewPager;
import com.photomagner.widgets.JazzyViewPager.TransitionEffect;
import com.polites.android.GestureImageView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * 
 * @author ipip
 *
 */
public class ShowImageActivity extends Activity {
	private LinearLayout ll;
	private InfoDialog mDialog;
	private String fileRoute;
	private ImageViewPager mViewPager;
	private MyViewPagerAdapter mAdapter;
	// the number of the bitmap in PicInfoList, which is not the source ID
	private int id;
	private int n;
	// origin width and height of the bitmap
	private Animation anim_s;
	private Button b;
	private float px = 0, py = 0;
	private Handler mHandler;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.image_show);
		id = getIntent().getIntExtra("image", -1);
		n = TimelineActivity.PicInfoList.size();
		
		InitViewPager();
		ll = (LinearLayout) findViewById(R.id.ivImageCover);
		ll.bringToFront();
		b = (Button) findViewById(R.id.ivBackButton);
		b.setText(" < 相册(" + (id + 1) + "/"
				+ TimelineActivity.PicInfoList.size() + ")");
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ShowImageActivity.this.finish();
			}

		});
		initAnimation();
	}

	private void InitViewPager() {
		mViewPager = new ImageViewPager(this);
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.ivImageFrame);
		rl.addView(mViewPager);
		mAdapter = new MyViewPagerAdapter(this);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setCurrentItem(id);
		mViewPager.setFadeEnabled(true);
		mViewPager.setTransitionEffect(TransitionEffect.Tablet);
		mViewPager.setPageMargin(30);
		LayoutParams l = mViewPager.getLayoutParams();
		l.height = LayoutParams.WRAP_CONTENT;
		l.width = LayoutParams.WRAP_CONTENT;
		mViewPager.setLayoutParams(l);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener(){

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPageSelected(int index) {
				// TODO Auto-generated method stub
				id = index;
				b.setText(" < 相册(" + (id + 1) + "/"
						+ TimelineActivity.PicInfoList.size() + ")");
			}
			
		});
		
	}

	private class MyViewPagerAdapter extends PagerAdapter {
		private Context mContext;
		private View mCurrentView;
		public MyViewPagerAdapter(Context context) {
			mContext = context;
		}
		private int mChildCount = 0;
		 
	    @Override
	    public void notifyDataSetChanged() {         
	    	mChildCount = getCount();
	    	super.notifyDataSetChanged();
	    }
	    @Override
	    public void setPrimaryItem(ViewGroup container, int position, Object object){
	    	mCurrentView = (View)object;
	    }
	    public View getCurrentView(){
	    	return mCurrentView;
	    }
	    @Override
	    public int getItemPosition(Object object)   {          
	    	if ( mChildCount > 0) {
	        mChildCount --;
	        return POSITION_NONE;
	        }
	        return super.getItemPosition(object);
	    }
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return n;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == (View) arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			GestureImageView iv = new GestureImageView(mContext);
			iv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			int type = DataGainUtil.SMALL;
			switch (ShowImageActivity.this.getResources().getConfiguration().orientation){
			case Configuration.ORIENTATION_LANDSCAPE:
				type = DataGainUtil.LARGE0;
				break;
			case Configuration.ORIENTATION_PORTRAIT:
				type = DataGainUtil.LARGE1;
			}
			String key = DataGainUtil.generateKey(position, type);
			DataGainUtil.getDataGain().getDataForImageView(position, iv, key);
			((ViewPager) container).addView(iv, 0);
			mViewPager.setObjectForPosition(iv, position);
			return iv;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View view = (View) object;  
            container.removeView(view);  
		}
	}


	private void initAnimation() {
		anim_s = new ScaleAnimation(1f, 1.2f, 1f, 1.2f,
				Animation.RELATIVE_TO_PARENT, 0.5f,
				Animation.RELATIVE_TO_PARENT, 0.5f);
		anim_s.setDuration(4000);
		anim_s.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				slideNext();
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
	}

	private int last_id(int id) {
		return (id + n - 1) % n;
	}

	private int next_id(int id) {
		return (id + 1) % n;
	}


	/*
	 * when click 'info' button, invoke this
	 */
	public void listInfomation(View v) {
		// create a dialog to list picture parameters
		mDialog = new InfoDialog(this);
		// Button in the dialog

		// use WindowManager to manager mydialog's attributes
		Window mWindow = mDialog.getWindow();
		WindowManager.LayoutParams lp = mWindow.getAttributes();
		mWindow.setGravity(Gravity.CENTER);
		lp.alpha = 0.6f;

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		lp.width = (int) (dm.widthPixels * 0.95);
		lp.height = (int) (dm.heightPixels * 0.8);
		mDialog.getWindow().setAttributes(lp);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		mDialog.show();
	}

	/*
	 * invoke in cycles, show next picture
	 */
	private void slideNext() {
		id = next_id(id);
		mViewPager.setCurrentItem(id);
		startSlide();
	}

	/*
	 * set the animation of the picture on sliding
	 */
	public void startSlide() {
		Log.i("photo", "slide");
		ImageView iv = (ImageView) mViewPager.getChildAt(id);
		iv.clearAnimation();
		iv.setAnimation(anim_s);
		iv.startAnimation(anim_s);
	}

	public void slidePictures(View v) {
		startSlide();
	}

	/*
	 * when the image was clicked, stop sliding
	 */
	public void stopSliding() {

	}

	@SuppressLint("InlinedApi")
	private ArrayList<String> getPicInfo() {
		fileRoute = TimelineActivity.PicInfoList.get(id).fileRoute;
		ArrayList<String> info = new ArrayList<String>();
		ExifInterface et = null;
		try {
			et = new ExifInterface(fileRoute);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (et == null) {
			info.add("信息获取失败...");
			return info;
		}

		int fileSplitPlace = fileRoute.lastIndexOf('/');
		info.add("名称:" + fileRoute.substring(fileSplitPlace + 1));
		info.add("路径:" + fileRoute.substring(0, fileSplitPlace - 1));
		int fileSize = 0;
		File f = new File(fileRoute);
		if (f.exists()) {
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
		if (fileSize < 1048576) {
			strSize = "" + fileSize / 1024 + "KB";
		} else
			strSize = "" + 1.0 * (int) (1.0 * fileSize / 1048576 * 100) / 100
					+ "MB";
		BitmapFactory.Options op = new BitmapFactory.Options();
		op.inJustDecodeBounds = true;
		op.inSampleSize = 1;
		BitmapFactory.decodeFile(TimelineActivity.PicInfoList.get(id).fileRoute, op);
		info.add("文件大小: " + strSize + "(" + fileSize + "Byte)");
		info.add("宽高: " + op.outWidth + "*" + op.outHeight);
		if (et.getAttribute(ExifInterface.TAG_DATETIME) != null)
			info.add("拍摄日期: "
					+ et.getAttribute(ExifInterface.TAG_DATETIME)
							.replaceFirst(":", "/").replaceFirst(":", "/"));
		if (TimelineActivity.PicInfoList.get(id).pl != null)
			info.add("经纬度: " + TimelineActivity.PicInfoList.get(id).pl.latitude
					+ "," + TimelineActivity.PicInfoList.get(id).pl.longitude);
		if (et.getAttribute(ExifInterface.TAG_APERTURE) != null)
			info.add("光圈值: " + et.getAttribute(ExifInterface.TAG_APERTURE));
		if (et.getAttribute(ExifInterface.TAG_MAKE) != null)
			info.add("制造者: " + et.getAttribute(ExifInterface.TAG_MAKE));
		if (et.getAttribute(ExifInterface.TAG_MODEL) != null)
			info.add("设备型号: " + et.getAttribute(ExifInterface.TAG_MODEL));
		if (et.getAttribute(ExifInterface.TAG_FLASH) != null)
			info.add("闪光灯: " + et.getAttribute(ExifInterface.TAG_FLASH));
		info.add("焦距: "
				+ et.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, -1)
				+ "mm");
		if (et.getAttribute(ExifInterface.TAG_EXPOSURE_TIME) != null)
			info.add("曝光时间: "
					+ et.getAttribute(ExifInterface.TAG_EXPOSURE_TIME) + "ms");
		if (et.getAttribute(ExifInterface.TAG_WHITE_BALANCE) != null)
			info.add("白平衡: "
					+ ((et.getAttribute(ExifInterface.TAG_WHITE_BALANCE)
							.equals(String
									.valueOf(ExifInterface.WHITEBALANCE_AUTO))) ? "自动"
							: "手动"));
		if (et.getAttribute(ExifInterface.TAG_ISO) != null)
			info.add("感光度: " + et.getAttribute(ExifInterface.TAG_ISO));
		if (et.getAttribute(ExifInterface.TAG_ORIENTATION) != null) {
			String ori = "";
			switch (Integer.valueOf(et
					.getAttribute(ExifInterface.TAG_ORIENTATION))) {
			case (ExifInterface.ORIENTATION_NORMAL):
				ori = "正常";
				break;
			case (ExifInterface.ORIENTATION_UNDEFINED):
				ori = "未定义的方向";
				break;
			case (ExifInterface.ORIENTATION_FLIP_VERTICAL):
				ori = "垂直翻转";
				break;
			case (ExifInterface.ORIENTATION_FLIP_HORIZONTAL):
				ori = "水平翻转";
				break;
			case (ExifInterface.ORIENTATION_ROTATE_90):
				ori = "旋转90度";
				break;
			case (ExifInterface.ORIENTATION_ROTATE_180):
				ori = "旋转180度";
				break;
			case (ExifInterface.ORIENTATION_ROTATE_270):
				ori = "旋转270度";
				break;
			case (ExifInterface.ORIENTATION_TRANSPOSE):
				ori = "翻转";
				break;
			case (ExifInterface.ORIENTATION_TRANSVERSE):
				ori = "横向";
				break;
			}
			info.add("拍摄方向: " + ori);
		}
		return info;
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	private void deleteThisImage(){
		ImageView iv = (ImageView)(mAdapter.getCurrentView());
		iv.clearAnimation();
		Animation a = new AlphaAnimation(1.0f, 0f);
		final int anim_time = 1500;
		a.setDuration(anim_time);
		iv.startAnimation(a);
		
		new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				DataGainUtil.getDataGain().delData(id);
				if (TimelineActivity.PicInfoList.size() == 0) ShowImageActivity.this.finish();
				mViewPager.setCurrentItem(id);
				b.setText(" < 相册(" + (id + 1) + "/"
						+ TimelineActivity.PicInfoList.size() + ")");
				mAdapter.notifyDataSetChanged();
				Animation b = new AlphaAnimation(0f, 1f);
				b.setDuration(1000);
				mViewPager.clearAnimation();
				mViewPager.startAnimation(b);
			}
			
		}, anim_time);
		
	}
	public void delImage(View v){
		Dialog dialog = new AlertDialog.Builder(this).setTitle("删除图片")
				.setMessage("确定要删除这张照片吗").setIcon(R.drawable.discard2)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						deleteThisImage();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				}).create();
		dialog.show();
	}
	
	private class InfoDialog extends Dialog {

		public InfoDialog(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		public InfoDialog(Context context, int theme) {
			super(context, theme);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			this.setContentView(R.layout.image_show_info);
			Button okButton = (Button) findViewById(R.id.ivInfoOkButton);
			okButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mDialog.dismiss();
				}
			});

			ListView lv = (ListView) findViewById(R.id.ivInfoListView);
			ArrayList<String> info = getPicInfo();
			lv.setAdapter(new ArrayAdapter<String>(ShowImageActivity.this,
					R.layout.small_font, R.id.info_tv, info));
		}
	}
	
	private class ImageViewPager extends JazzyViewPager{

		public ImageViewPager(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
		@Override
		public boolean dispatchTouchEvent(MotionEvent ev){
			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				px = ev.getX();
				py = ev.getY();
				break;
			case MotionEvent.ACTION_UP:
				if (Math.abs(px - ev.getX()) < 5 && Math.abs(py - ev.getY()) < 5) {
					if (ll.getVisibility() == View.VISIBLE)
						ll.setVisibility(View.INVISIBLE);
					else
						ll.setVisibility(View.VISIBLE);
				}
				break;
			}
			return super.dispatchTouchEvent(ev);
			
		}
	}
}
