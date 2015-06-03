package com.universal.yt;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.util.Log;

/**
 * Storing information about the video, and formatting the time
 */
@SuppressWarnings("serial")
public class Video implements Serializable {

	private String title;
	private String id;	
	private String updated;	
	private String description;	
	private String thumbUrl;
	private String image;
	
	public Video(String title, String id, String updated, String description, String thumbUrl, String image) {
		super();
		this.title = title;
		this.id = id;
		this.updated = updated;
		this.description = description;
		this.thumbUrl = thumbUrl;
		this.image = image;
		
	}

	public String getTitle(){
		return title;
	}
	
	public String getId() {
		return id;
	}
    public String getUpdated() {
    	String dataSemTimeZone = removerTimeZone(updated);
		String TimeDate = formatData(dataSemTimeZone);
		return TimeDate;
		//return updated;
    }
    
    public String getDescription() {
    	return description;
    }

	public String getThumbUrl() {
		return thumbUrl;
	}
	
	public String getImage() {
		return image;
	}
	
	@SuppressLint("SimpleDateFormat")
	private String formatData(String data){
		String strData = null;
		TimeZone tzUTC = TimeZone.getTimeZone("UTC");
		SimpleDateFormat formatEntry = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
		formatEntry.setTimeZone(tzUTC);
		SimpleDateFormat formatFinal = new SimpleDateFormat("EEE, dd/MM/yy, 'at' HH:mm");
		
		try {
			strData = formatFinal.format(formatEntry.parse(data));
		} catch (ParseException e) {
		Log.e("Error parsing data", Log.getStackTraceString(e));
		}
		return strData;
	}
	
	private String removerTimeZone(String data){
		// formatting the timezone
		return data.replaceFirst("(\\s[+|-]\\d{4})", "");
	}
	
	
}