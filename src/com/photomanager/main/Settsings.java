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

import android.content.Context;

public class Settsings {
	private Map<String, Object> settings;
	private Context context;
	private static final String filename = "settings.s";
	public Settsings(Context context){
		this.context = context;
		settings = new HashMap<String, Object>();
		if (!load()){
			settings.put("first-create", generateTimeStick());
		}
	}
	private boolean load(){
		try {
			FileInputStream s = context.openFileInput(filename);
			if (s == null) return false;
			ObjectInputStream o = new ObjectInputStream(s);
			String key;
			while ((key = (String)o.readObject())!=null){
				settings.put(key, o.readObject());
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			Iterator<Map.Entry<String, Object>> i = settings.entrySet().iterator();
			while (i.hasNext()){
				Map.Entry<String, Object> so = (Map.Entry<String, Object>) i.next();
				o.writeObject(so.getKey());
				o.writeObject(so.getValue());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void put(String key, Object item){
		settings.put(key, item);
	}
	public Object get(String key){
		return settings.get(key);
	}
	private String generateTimeStick(){
		Calendar c = Calendar.getInstance();
		c.getTimeInMillis();
		return c.toString();
	}
}
