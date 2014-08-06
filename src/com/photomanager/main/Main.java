package com.photomanager.main;

import java.util.Calendar;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.Toast;

/**
 * @author ipip
 *  2014年7月16日下午2:49:12
 */
@SuppressWarnings("deprecation")
public class Main extends TabActivity implements OnCheckedChangeListener{
	private TabHost tHost;
	private RadioGroup mGroup;
	private Intent iTimeline,iPlace,iMap,iTag;
	private Calendar lastBack;
	public static Settings s;
	@Override 
	public boolean dispatchKeyEvent(KeyEvent event){
		if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK){
			if (lastBack == null) lastBack = Calendar.getInstance(); else{
				Calendar nowBack = Calendar.getInstance();
				if (nowBack.getTimeInMillis()-lastBack.getTimeInMillis()<2000){
					finish();
					return true;
				} else lastBack = nowBack;
			}
			Toast.makeText(getApplication(), "再按一次退出相册", Toast.LENGTH_SHORT).show();
			return false;
		} else 
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		switch(checkedId){
		case R.id.hostbutton_timeline:
			tHost.setCurrentTabByTag("iTimeline");
			break;
		/*case R.id.hostbutton_place:
			tHost.setCurrentTabByTag("iPlace");
			break;*/
		case R.id.hostbutton_map:
			tHost.setCurrentTabByTag("iMap");
			break;
		case R.id.hostbutton_tag:
			tHost.setCurrentTabByTag("iTag");
			break;
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		Log.i("Main", "new Settings()");
		s = new Settings(this);
		setTabHost();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
		
	}
	private void setTabHost(){
		tHost = (TabHost) this.getTabHost();
		mGroup = (RadioGroup) findViewById(R.id.main_tab);
		mGroup.setOnCheckedChangeListener(this);
		iTimeline = new Intent(Main.this, TimelineActivity.class);
		tHost.addTab(tHost.newTabSpec("iTimeline").setIndicator("时间轴", getResources().getDrawable(R.drawable.icon_1_n))
				.setContent(iTimeline));
		
		iPlace = new Intent(Main.this, PlaceActivity.class);
		tHost.addTab(tHost.newTabSpec("iPlace").setIndicator("地点", getResources().getDrawable(R.drawable.icon_2_n))
				.setContent(iPlace));
		
		iMap = new Intent(Main.this, MapActivity.class);
		tHost.addTab(tHost.newTabSpec("iMap").setIndicator("地图", getResources().getDrawable(R.drawable.icon_3_n))
				.setContent(iMap));
		
		iTag = new Intent(Main.this, TagActivity.class);
		tHost.addTab(tHost.newTabSpec("iTag").setIndicator("标签", getResources().getDrawable(R.drawable.icon_4_n))
				.setContent(iTag));
		
	}
}
