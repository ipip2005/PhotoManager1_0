package com.photomanager.main;


import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.panoramaview.PanoramaView;
import com.baidu.lbsapi.panoramaview.PanoramaViewListener;
import com.baidu.mapapi.model.LatLng;
import com.photomanager.utils.DataGainUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class PanoramaActivity extends Activity implements PanoramaViewListener{
	private PanoramaView mPanoramaView;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //先初始化BMapManager
        BasicApplication app = (BasicApplication) this.getApplication();
        if (app.mBMapManager == null) {
            app.mBMapManager = new BMapManager(app);

            app.mBMapManager.init(new BasicApplication.MyGeneralListener());
        }
        setContentView(R.layout.activity_panorama);
        mPanoramaView = (PanoramaView)findViewById(R.id.panorama); 
        mPanoramaView.setPanoramaImageLevel(5);
        mPanoramaView.setShowTopoLink(true);
        mPanoramaView.setZoomGestureEnabled(true);
        mPanoramaView.setRotateGestureEnabled(true);
        mPanoramaView.setPanoramaViewListener(this);
        Intent intent = getIntent();
        mPanoramaView.setPanorama(intent.getDoubleExtra("longitude", -1), 
        		intent.getDoubleExtra("latitude", -1));
	}
	public void backToImageShow(View v){
		this.finish();
	}
	@Override
    protected void onPause() {
        super.onPause();
        mPanoramaView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPanoramaView.onResume();
    }

    @Override
    protected void onDestroy() {
        mPanoramaView.destroy();
        super.onDestroy();
    }
    
    @Override
	public void onLoadPanoramBegin() {
		Log.d("Paro", "loadPanoramBegin");
	}


	@Override
	public void onLoadPanoramaEnd() {
		Log.d("Paro", "loadPanoramaEnd");
	}


	@Override
	public void onLoadPanoramaError() {
		Log.d("Paro", "loadPanoramaError");
		Toast.makeText(this, "加载全景图出错,再试一试？",Toast.LENGTH_SHORT).show(); 
	}
}
