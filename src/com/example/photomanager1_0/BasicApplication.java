package com.example.photomanager1_0;

import com.baidu.mapapi.SDKInitializer;

import android.app.Application;

public class BasicApplication extends Application{
	@Override
	public void onCreate() {
		super.onCreate();
		// ��ʹ�� SDK �����֮ǰ��ʼ�� context ��Ϣ������ ApplicationContext
		SDKInitializer.initialize(this);
	}
}
