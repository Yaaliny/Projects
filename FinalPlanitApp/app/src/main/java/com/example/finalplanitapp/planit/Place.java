package com.example.finalplanitapp.planit;


import com.example.finalplanitapp.itinerary.Interval;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import kotlin.ranges.IntRange;

public class Place {

	private String name;
	private List<IntRange> hours;
	private double lat;
	private double lng;
	private Set<String> types;
	private String place_id;
	private double rating;
	private String address;
	private int price_lvl;
	private int numberOfRatings;
	private int averageStayTime;
	
	public Place() {}
	
	@Override
	public int hashCode() {
		return place_id.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof Place))
			return false;
		
		Place p = (Place)obj;
		
		if (this.getPlaceId() == p.getPlaceId())
			return true;
		else if(this.getName() == p.getName() && p.getName() != "")
			return true;
		else
			return false;
	}

	public List<IntRange> getHours() {
		return this.hours;
	}
	
	public int getAverageStayTime() {
		return this.averageStayTime;
	}
	
	public double getRating() {
		return rating;
	}
	
	public int getNumberOfRatings() {
		return this.numberOfRatings;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public boolean openAt(Date date) {

		Calendar c = Calendar.getInstance();

		c.setTime(date);
		long startTimeDate = c.getTimeInMillis();

		// Get the beginning of the day
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long startTimeDay = c.getTimeInMillis();

		int relativeStartTime = (int)(startTimeDate - startTimeDay);

		int startDateIndex = c.get(Calendar.DAY_OF_WEEK);

		if (hours == null)
			return true;

		IntRange range = hours.get(startDateIndex - 1);

		if (range == null) {
			return true;
		}
		else {
			return range.contains(relativeStartTime);
		}
	}

	public boolean openDuring(Interval interval) {
		return openAt(interval.getStartDate()) && openAt(interval.getEndDate());
	}

	public void setHours(List<IntRange> hours) {
		this.hours = hours;
	}
	
	public void setLatLng(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
	}
	
	public void setTypes(Set<String> types) {
		this.types = types;
	}
	
	public void setPlaceId(String place_id) {
		this.place_id = place_id.trim();
	}
	
	public void setRating(double rating) {
		this.rating = rating;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public void setPriceLvl(int price_lvl) {
		this.price_lvl = price_lvl;
	}
	
	public void setNumberOfRatings(int numberOfRatings) {
		this.numberOfRatings = numberOfRatings;
	}
	
	public int getPriceLvl() {
		return this.price_lvl;
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public String getName() {
		
		return this.name;
	}
	
	public String getPlaceId() {
		return this.place_id;
	}
	
	public double getLatitude() {
		return this.lat;
	}
	
	public double getLongitude() {
		return this.lng;
	}

	public String toString () {
		
		String str = "{\n\tPlaceId: %s,\n\tName: %s,\n\tAddress: %s,\n\tHours: %s,\n\tLatitude: %f\n\tLongitude:%f,\n\tTypes: %s,\n\tRating: %f,\n\tPrice Level: %d,\n\tNumber of Ratings: %d\n}";
		
		return String.format(str, place_id, name, address, hours, lat, lng, types, rating, price_lvl, numberOfRatings);
		
	}
	
	public Set<String> getTypes() {
		return this.types;
	}
	
	
	
	public static class Builder {
		
		private String name;
		private List<IntRange> hours;
		private double lat;
		private double lng;
		private Set<String> types;
		private String place_id;
		private double rating;
		private String address;
		private int price_lvl;
		private int numberOfRatings;
		public int averageStayTime;
		
		public Builder(String address) {
			this.address = address;
		}
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public Builder hours(List<IntRange> hours) {
			this.hours = hours;
			return this;
		}
		
		public Builder latLng(double lat, double lng) {
			this.lat = lat;
			this.lng = lng;
			return this;
		}
		
		public Builder types(Set<String> types) {
			this.types = types;
			return this;
		}
		
		public Builder place_id(String place_id) {
			this.place_id = place_id;
			return this;
		}
		
		public Builder rating(double rating) {
			this.rating = rating;
			return this;
		}
		
		public Builder price_lvl(int price_lvl) {
			this.price_lvl = price_lvl;
			return this;
		}
		
		public Builder numberOfRatings(int numberOfRatings) {
			this.numberOfRatings = numberOfRatings;
			return this;
		}
		
		public Builder averageStayTime(int averageStayTime) {
			this.averageStayTime = averageStayTime;
			return this;
		}
		
		public Place build() {
			
			Place place = new Place();
			place.address = address;
			place.name = name;
			place.hours = hours;
			place.lat = lat;
			place.lng = lng;
			place.types = types;
			place.place_id = place_id;
			place.rating = rating;
			place.price_lvl = price_lvl;
			place.numberOfRatings = numberOfRatings;
			place.averageStayTime = averageStayTime;
			return place;
		}
	}
}
