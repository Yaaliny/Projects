package com.example.finalplanitapp.planit;

import android.text.Html;
import android.util.Pair;
import android.view.ViewStructure;

import androidx.core.text.HtmlCompat;

import com.example.finalplanitapp.itinerary.Interval;
import com.google.android.gms.maps.model.LatLng;

import com.google.maps.android.MarkerManager;
import com.google.maps.android.PolyUtil;

import com.example.finalplanitapp.DistanceCostActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kotlin.ranges.IntRange;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GoogleAPI {
	
	private static final String API_KEY = "AIzaSyAHl3UXuY5-RBcIXNWPm_Ik3OYA1qctQdc";
	private static OkHttpClient client = new OkHttpClient();
	
	public static ArrayList nameToLatLng(String place) throws IOException, JSONException {
		URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?address="+ URLEncoder.encode(place, "UTF-8") +"&key=" + API_KEY);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		
		JSONObject response = new JSONObject(content.toString());
		
		con.disconnect();
		double lat = response.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
		double lng = response.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
		
		ArrayList location = new ArrayList();
		
		location.add(lat);
		location.add(lng);
		
		return location;
	}
	
	
	public static ArrayList<Place> DataExtraction(String type, int distance, String cityName) throws IOException, JSONException, ParseException {
		ArrayList<Place> data = new ArrayList<Place>();
		
		ArrayList geo = nameToLatLng(cityName);
		
		URL url = new URL(
				"https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
				"location=" + geo.get(0) + "," + geo.get(1) +
				"&radius=" 	+ distance +
				"&type=" 	+ URLEncoder.encode(type, "UTF-8") +
				"&key=" 	+ API_KEY);
		
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		
		JSONObject response = new JSONObject(content.toString());
		con.disconnect();
		
		JSONArray a = response.getJSONArray("results");
		
		
		for (int i = 0; i < a.length(); i++) {
		
			JSONObject item = a.getJSONObject(i);
			
			Set<String> types = new HashSet<String>(JsonToArray(item.getJSONArray("types")));
			String name = item.getString("name");
			double lng = item.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
			double lat = item.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
			String place_id = item.getString("place_id");
			double rating;
			String address = item.getString("vicinity");
			int numberOfRatings;
			int price_lvl;
			ArrayList<IntRange> hours = getHours(place_id);

			try {
				price_lvl = item.getInt("price_level");
			}
			catch(Exception JSONException) {
				price_lvl = -1; 
			}
			
			try {
				numberOfRatings = item.getInt("user_ratings_total");
			}
			catch(Exception JSONException) {
				numberOfRatings = -1; 
			}
			
			try {
				rating = item.getDouble("rating");
			}
			catch(Exception JSONException) {
				rating = -1; 
			}
			
			Place p = new Place.Builder(address)
					.name(name)
					.hours(hours)
					.latLng(lat, lng)
					.types(types)
					.place_id(place_id)
					.rating(rating)
					.numberOfRatings(numberOfRatings)
					.price_lvl(price_lvl)
					.averageStayTime(30)
					.build();
			
			data.add(p);
		}
		
		return data;
	}
	
	private static ArrayList<String> JsonToArray(JSONArray old) throws JSONException {
		ArrayList<String> a = new ArrayList<String>();
		for (int i = 0; i < old.length(); i++) {
			a.add(old.getString(i));
		}
		return a;
		
	}
	
	/*
	Hours is an arraylist of size 7 (0-6) of IntRange which are in miliseconds
	0 = Sunday
	6 = Saturday
	If it is open 24 hours or has no info on hours then everything is null
	If it is closed on a  certain day only that spot in the arraylist is null
	 */
	private static ArrayList<IntRange> getHours(String place_id) throws JSONException, IOException, ParseException {

		URL url = new URL("https://maps.googleapis.com/maps/api/place/details/json?place_id="+place_id+"&fields=opening_hours&key=" + API_KEY);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		
		JSONObject response = new JSONObject(content.toString());
		
		con.disconnect();

		ArrayList<IntRange> hour = new ArrayList<IntRange>(7);

		for (int i = 0; i < 7; i++) {
			hour.add(null);
		}

		JSONArray hours = null;

		try {
			hours = response.getJSONObject("result").getJSONObject("opening_hours").getJSONArray("periods");
		}
		catch(JSONException a) {
			return hour;
		}


		// place is open 24 hours all the time
		if (hours.length()<=1) {
			return hour;
		}
		// check each day of the week
		else {
			for (int i =0; i <hours.length(); i++) {
				int day = hours.getJSONObject(i).getJSONObject("close").getInt("day");
				int close = hours.getJSONObject(i).getJSONObject("close").getInt("time");
				int open = hours.getJSONObject(i).getJSONObject("open").getInt("time");

				String c;
				String o;

				c = Integer.toString(close);
				o = Integer.toString(open);

				if (c.length() != 4) {
					c = String.format("%04d" , close);
				}

				if (o.length() != 4) {
					o = String.format("%04d" , open);
				}


				IntRange range = new IntRange(Integer.parseInt(o.substring(0, 2))*60*60*1000 + Integer.parseInt(o.substring(2, 4))*60*1000, Integer.parseInt(c.substring(0, 2))*60*60*1000 + Integer.parseInt(c.substring(2, 4))*60*1000);

				hour.set(day, range);
			}
		}


		return hour;
		
		
	}

	public static int[][] getDurationMatrix(List<Place> places) throws IOException, JSONException {
		
		
		String origins="";
		String destinations="";
		
		for (Place p : places) {
			origins = origins +"place_id:" +p.getPlaceId() + "|";
			destinations = destinations +"place_id:"+ p.getPlaceId() + "|";
		}
		
		origins = origins.substring(0, origins.length() - 1);
		
		destinations = destinations.substring(0, destinations.length() - 1);
		
		
		String url_request = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" + origins + "&destinations=" + destinations + "&mode=driving&key=" + API_KEY;
		
		Request request = new Request.Builder()
				.url(url_request)
				.build();
		
		Response response = client.newCall(request).execute();
		
		JSONObject r = new JSONObject(response.body().string());
		
		int length = r.getJSONArray("origin_addresses").length();
		
		int [][] duration = new int [length][length];
		
		JSONArray rows = r.getJSONArray("rows");
		
		for (int i = 0; i < rows.length(); i++) {
			JSONObject elements = rows.getJSONObject(i);
			//System.out.println(elements);
			JSONArray jsonArray2 = elements.getJSONArray("elements");
			//System.out.println(jsonArray2);
			for (int j = 0; j < jsonArray2.length(); j++) {
				JSONObject a = jsonArray2.getJSONObject(j);
				JSONObject b = a.getJSONObject("duration");
				int c = (int)Math.ceil(b.getInt("value") / 60.0);
				//System.out.println(c);
				duration[j][i]=c;
			}
		}
		
		return duration;
	}

	public static Place getPlace(String address) throws IOException, JSONException, ParseException {
		
		// Construct URL
		URL url = new URL(
				"https://maps.googleapis.com/maps/api/place/findplacefromtext/json?" + 
				"input=" + URLEncoder.encode(address, "UTF-8") +
				"&inputtype=textquery" +
				"&fields=place_id,geometry,formatted_address,name,types,opening_hours,price_level,rating" +
				"&key=" + API_KEY);
	
		// Set up connection
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("GET");
		
		// Read into the string buffer
		Reader inputStream = new InputStreamReader(connection.getInputStream());
		BufferedReader in = new BufferedReader(inputStream);
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		connection.disconnect();
		
		// Interpret the response as a JSON
		JSONObject response = new JSONObject(content.toString());
		JSONObject candidate = response.getJSONArray("candidates").getJSONObject(0);
		
		// Extract fields from the JSON
		Set<String> types = new HashSet<String>(JsonToArray(candidate.getJSONArray("types")));
		String name = candidate.getString("name");
		double lng = candidate.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
		double lat = candidate.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
		String place_id = candidate.getString("place_id");
		double rating;
		String formattedAddress = candidate.getString("formatted_address");
		int numberOfRatings;
		int price_lvl;
		ArrayList<IntRange> hours = getHours(place_id);

		try {
			price_lvl = candidate.getInt("price_level");
		}
		catch(Exception JSONException) {
			price_lvl = -1; 
		}
		
		try {
			numberOfRatings = candidate.getInt("user_ratings_total");
		}
		catch(Exception JSONException) {
			numberOfRatings = -1; 
		}
		
		try {
			rating = candidate.getDouble("rating");
		}
		catch(Exception JSONException) {
			rating = -1; 
		}
		
		// Build the place
		Place p = new Place.Builder(formattedAddress)
				.name(name)
				.hours(hours)
				.latLng(lat, lng)
				.types(types)
				.place_id(place_id)
				.rating(rating)
				.numberOfRatings(numberOfRatings)
				.price_lvl(price_lvl)
				.averageStayTime(30)
				.build();
		
		return p;
	}

	public static String getTravelMode(int index) {

		switch(index) {
			case 0:
				return "driving";
			case 1:
				return "transit";
			case 2:
				return "walking";
			case 3:
				return "bicycling";
			default:
				return "";
		}
	}

	public static Pair<List<String>,List<LatLng>> getDirections(LatLng origin, LatLng destination) throws IOException, JSONException {
		URL url = new URL("https://maps.googleapis.com/maps/api/directions/json?" +
				"origin=" + origin.latitude + "," + origin.longitude +
				"&destination=" + destination.latitude + "," + destination.longitude +
				"&mode=" + getTravelMode(DistanceCostActivity.travelMode) +
				"&key=" + API_KEY);

		List<String> directions = new ArrayList<String>();

		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("GET");

		// Read into the string buffer
		Reader inputStream = new InputStreamReader(connection.getInputStream());
		BufferedReader in = new BufferedReader(inputStream);
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		connection.disconnect();

		JSONObject response = new JSONObject(content.toString());

		JSONArray direction = response.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");

		for (int i =0; i<direction.length(); i++) {
			directions.add(Html.fromHtml(direction.getJSONObject(i).getString("html_instructions")).toString());
		}

		List<LatLng> line = PolyUtil.decode(response.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points"));

		Pair<List<String>,List<LatLng>> answer = new Pair<List<String>,List<LatLng>>(directions, line);

		return answer;
	}

}
