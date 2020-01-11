package com.example.finalplanitapp.itinerary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.finalplanitapp.PlaceUtils;
import com.example.finalplanitapp.planit.Place;
import com.example.finalplanitapp.planit.PlaceManager;
import com.example.finalplanitapp.dao.UserDAO;

public class Personalization {

	private static final double INTEREST_WEIGHT = 6.0;
	private static final double SIMILAR_TYPE_WEIGHT = -10.0;

	private static final double RATING_WEIGHT = 2.0;
	private static final double POPULARITY_WEIGHT = 2.0;

	private static final double TRAVEL_WEIGHT = 4.0;
	private static final double COST_WEIGHT = -2.0;

	private static final double IDEAL_TIME_WEIGHT = 1.0;

	private static final double VISITED_TYPE_WEIGHT = 1.0;
	private static final double LIKE_WEIGHT = 0.0;
	private static final double DISLIKE_WEIGHT = -0.0;
	private static final double VISITED_WEIGHT = 0.0;
	
	private PlaceManager places;
	private UserDAO user;
	
	private Set<String> liked, disliked;
	
	public Personalization(PlaceManager places) {
		this.liked = new HashSet<>();
		this.disliked = new HashSet<>();
		//this.user = user;
		//this.liked = user.getLikedPlaceIds(LoginActivity.emailString);
		//this.disliked = user.getDislikedPlaceIds(LoginActivity.emailString);
		this.places = places;
	}
	
	
	public List<Place> getPlaceCandidates(Place origin, Place destination, int searchRadius) {
		/*
		 * Returns a list of potential events the user would want to
		 * do, based off the location, radius, start time, end time
		 * preferences (i.e. If they selected "Kid Friendly", the bars
		 * would be removed from the result.
		 */

		Set<Place> candidates = places.filter(origin.getAddress(), searchRadius);
		
		// TODO: remove places that have too high of a negative node score

		// TODO: rmeove places that are not open during the interval we are looking at
		
		// Add the origin and destination if they aren't already in there
		List<Place> candidatesList = new ArrayList<Place>(candidates);

		return candidatesList;
	}

	public double getEdgeScore(Place place, Interval interval, int travelTime, Set<String> visitedTypes) {
		// TODO: take in the interval
		/*
		 * Return a score based on transportation time and distance
		 * between the items.
		 */

		// Ensure that we do not visit somewhere that is not open
		if (!place.openDuring(interval))
			return Double.NEGATIVE_INFINITY;

		// Whether we have visited a lot of similar places
		// before on this path
		int similarElements = 0;
		for (String type : visitedTypes) {
			if (place.getTypes().contains(type)) {
				similarElements++;
			}
		}
		double similarityScore = similarElements / (double)place.getTypes().size();


		// Whether this is the ideal time for this type
		// of activity
		// i.e. Usually drink coffee in the morning
		double timeScore = 0.0;
		for (String type : place.getTypes()) {
			if (PlaceUtils.isIdealTime(type,interval.getStartDate())) {
				timeScore += 1.0;
			}
		}
		timeScore /= (double)place.getTypes().size();



		// Whether this path contains a lot of travel time
		double transportationScore = 1.0 - 1.0 / (1.0 + travelTime);


		double visitedScore = 0.0;
		for (String type : place.getTypes()) {
			if (visitedTypes.contains(type))
				visitedScore += 1.0;
		}
		visitedScore /= (double)place.getTypes().size();

		// TODO: use powers of 2
		double finalScore = similarityScore * SIMILAR_TYPE_WEIGHT +
				timeScore * IDEAL_TIME_WEIGHT +
				transportationScore * TRAVEL_WEIGHT +
				visitedScore * VISITED_TYPE_WEIGHT;

		return finalScore;
	}
	
	public double getNodeScore(Place place) {
		/*
		 * Return a user rating of the event based on whether they have
		 * liked that event, or similar, in the past. It also takes into
		 * account the cost of the event compared to the target cost, 
		 * among other things.
		 */


		// Whether this is a place the user is interested
		// in visiting
		double interestScore = 0;
		for (String type : place.getTypes()) {
			if (places.isInterested(type)) {
				interestScore += 1.0;
			}
		}
		interestScore /= (double)place.getTypes().size();


		// Whether or not the user has liked this place or
		// disliked it in the past
		Set<String> types = place.getTypes();
		double likeScore = 0.0;
		for (String type : types) {
			if (liked.contains(type)) {
				likeScore += LIKE_WEIGHT;
			}
			else if(disliked.contains(type)) {
				likeScore += DISLIKE_WEIGHT;
			}
		}

		// How much more does this place cost compared to the target price
		double costScore = 0.0;
		if (place.getPriceLvl() != -1 && places.getTargetPrice() != -1)
			costScore = Math.abs(place.getPriceLvl() - places.getTargetPrice()) / 4.0;

		// How popular this place is
		double popScore = 0.0;
		if (place.getNumberOfRatings() != -1)
			popScore = 1.0 - 1.0 / (1.0 + place.getNumberOfRatings());


		// How well this place is rated
		double ratingScore = 0.0;
		if (place.getRating() != -1)
			ratingScore = place.getRating() / 5.0;


		double finalScore = interestScore * INTEREST_WEIGHT +
							likeScore * LIKE_WEIGHT +
							costScore * COST_WEIGHT +
							(popScore * POPULARITY_WEIGHT) * (ratingScore * RATING_WEIGHT);

		return finalScore;
	}
	
	public HashMap<Place, Double> precomputeNodeScores(List<Place> places) {
		
		HashMap<Place, Double> placeToNodeScore = new HashMap<>();
		for (Place place : places) {
			placeToNodeScore.put(place, getNodeScore(place));
		}
		return placeToNodeScore;
	}

	public double getPathSimilarity(Set<Place> a, Set<Place> b) {

		int similarElements = 0;

		for (Place place : b) {
			if (a.contains(place)) {
				similarElements++;
			}
		}

		return similarElements / (double)b.size();
	}
}
