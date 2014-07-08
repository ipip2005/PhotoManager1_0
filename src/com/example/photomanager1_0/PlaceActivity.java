package com.example.photomanager1_0;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class PlaceActivity extends Activity{
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView textView=new TextView(this);
		textView.setText("Home");
		setContentView(textView);
		//setContentView(R.layout.activity_place);
	}
}
