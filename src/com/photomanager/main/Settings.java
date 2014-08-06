package com.photomanager.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.baidu.mapapi.model.LatLng;

import android.content.Context;
import android.util.Log;

public class Settings {
	private SettingsMap settings;
	private Context context;
	private static final String filename = "settings.s";
	public Settings(Context context){
		this.context = context;
		settings = new SettingsMap();
		if (!load()){
			Log.i("Settings", "failed loading");
			settings.put("first-create", generateTimeStick());
		}
		Log.i("Settings", ""+(String)settings.get("first-create"));
	}
	private void clear(){
		context.deleteFile(filename);
	}
	private boolean load(){
		try {
			FileInputStream s = context.openFileInput(filename);
			if (s == null) return false;
			ObjectInputStream o = new ObjectInputStream(s);
			settings = (SettingsMap)o.readObject();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	public void save(){
		try {
			FileOutputStream s = context.openFileOutput(filename, Context.MODE_PRIVATE);
			ObjectOutputStream o = new ObjectOutputStream(s);
			o.writeObject(settings);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void put(String key, Object item){
		Log.i("Settings", "put: "+key+" "+item);
		settings.put(key, item);
		save();
	}
	public Object get(String key){
		return settings.get(key);
	}
	private String generateTimeStick(){
		Calendar c = Calendar.getInstance();
		return String.valueOf(c.getTimeInMillis());
	}
}
