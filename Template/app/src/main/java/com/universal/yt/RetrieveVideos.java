package com.universal.yt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * This is class gets the videos from youtube and parses the result
 *
 */
public class RetrieveVideos {
	
	public static ReturnItem getVideos(String apiUrl){
		ArrayList<Video> videos = new ArrayList<Video>();
		String pagetoken = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpUriRequest request = new HttpGet(apiUrl);
			HttpResponse response = client.execute(request);
			String jsonString = convertToString(response.getEntity().getContent());
			JSONObject json = new JSONObject(jsonString);
			Log.v("INFO", json.toString());
			
			try {
				pagetoken = json.getString("nextPageToken");
			} catch (JSONException e){
				Log.v("YoutubeUserVideosTask", "JSONException: " + e);
			}
			
			JSONArray jsonArray = json.getJSONArray("items");
			
			// Create a list to store the videos in
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					JSONObject jsonSnippet = jsonArray.getJSONObject(i).getJSONObject("snippet");
					String title = jsonSnippet.getString("title");
					String updated = jsonSnippet.getString("publishedAt");
					String description = jsonSnippet.getString("description");
					String id;
					try {
						id = jsonSnippet.getJSONObject("resourceId").getString("videoId");
					} catch (Exception e) {
						id = jsonObject.getJSONObject("id").getString("videoId");
					}
					// For a sharper thumbnail change sq to hq, this will make the app slower though
					String thumbUrl = jsonSnippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
					String image = jsonSnippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");
				
					// save the video to the list
					videos.add(new Video(title, id, updated, description, thumbUrl, image));
					Log.v("INFO", title);
				} catch (JSONException e){
					Log.v("YoutubeUserVideosTask", "JSONException: " + e);
				}
			}						
		// catch and excute doThis
		} catch (ClientProtocolException e) {
			Log.v("YoutubeUserVideosTask", "ClientProtocolException" + e);
		} catch (IOException e) {
			Log.v("YoutubeUserVideosTask", "IOException" + e);
		} catch (JSONException e) {
			Log.v("YoutubeUserVideosTask", "JSONException: " + e);
		}		
		return (new ReturnItem(videos, pagetoken));
	}
	
   // Converting an inputstream to a more readable string
	public static String convertToString(InputStream inputStream) throws IOException {
		if (inputStream != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 1024);
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				inputStream.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}
	
}