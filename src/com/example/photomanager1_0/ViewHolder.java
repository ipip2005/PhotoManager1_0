package com.example.photomanager1_0;

import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolder {
	public ViewHolder(){
		subimage = new ImageView[3];
	}
	public TextView title;
	public TextView text;
	public ImageView image;
	public ImageView[] subimage;
}
