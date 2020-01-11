package com.example.finalplanitapp.itinerary;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import com.example.finalplanitapp.planit.GeographyUtils;
import com.example.finalplanitapp.planit.GoogleAPI;
import com.example.finalplanitapp.planit.Place;
import com.example.finalplanitapp.planit.PlaceComparator;

public class PlanIt {
	
	class Node {
		
		private int index;
		private DefiniteItem item;
		private double accumulatedScore;
		private Node previous;
		private int totalTravelTime;
		
		private Set<Place> visitedPlaces;
		private Set<String> visitedTypes;

		public int getTotalTravelTime() {return this.totalTravelTime; };

		public double getScore() {
			return this.accumulatedScore;
		}
		
		public void setScore(double score) {
			this.accumulatedScore = score;
		}
		
		public void addVisitedPlace(Place place) {
			this.visitedPlaces.add(place);
		}
		
		public void addVisitedTypes(List<String> types) {
			this.visitedTypes.addAll(types);
		}
		
		public Node(int index, DefiniteItem item) {
			
			this.index = index;
			this.item = item;
			this.accumulatedScore = 0;
			this.previous = null;
			this.totalTravelTime = 0;
			
			visitedPlaces = new HashSet<>();
			visitedTypes = new HashSet<>();
			
			visitedPlaces.add(item.getPlace());
		}
		
		public Node branch(int index, DefiniteItem item, double score, int travelTime) {
			
			Node node = new Node(index, item);
			
			node.accumulatedScore = this.accumulatedScore + score;
			node.previous = this;
			
			node.visitedPlaces.addAll(this.visitedPlaces);
			node.visitedPlaces.add(item.getPlace());
			
			node.visitedTypes.addAll(this.visitedTypes);
			node.visitedTypes.addAll(item.getPlace().getTypes());

			node.totalTravelTime = this.totalTravelTime + travelTime;
			
			return node;
		}
	}
	
	private Itinerary itinerary;
	private Personalization personalize;
	private int searchRadius;
	private String travelMode;
	
	public PlanIt(Itinerary itinerary, Personalization personalize, int searchRadius, String travelMode) {
		
		this.itinerary = itinerary;
		this.personalize = personalize;
		this.searchRadius = searchRadius;
		this.travelMode = travelMode;
	}
	
	public List<Journey> calculateJourneys() {
		
		// TODO: move these somewhere else
		int maxNumOfCandidates = 50;
		int numOfPaths = 3;
		int maxTravelTime = 30;
		int minScore = -1000;
		int averageStayDuration = 90;
		double similarityThreshold = 0.75;
		double travelTimeThreshhold = 1.5;

		// Step 1:
		// Get a bunch of places to potentially use for the journey
		Place origin = itinerary.getStartLocation().getPlace();
		Place destination = itinerary.getEndLocation().getPlace();
		int minItems = (int)Math.ceil((double)itinerary.getTotalTime() / (averageStayDuration * 60.0 * 1000.0));
		Log.d("CIARAN", "MIN ITEMS: " + minItems);

		List<Place> candidates = personalize.getPlaceCandidates(origin, destination, searchRadius);
		int numOfCandidates = Math.min(maxNumOfCandidates, candidates.size());

		// Step 2:
		// Pre-compute node scores
		HashMap<Place, Double> placeToNodeScore = personalize.precomputeNodeScores(candidates);
		placeToNodeScore.put(origin, 0.0);
		placeToNodeScore.put(destination, 0.0);


		Collections.sort(candidates, Collections.reverseOrder(new PlaceComparator(placeToNodeScore)));
		candidates = candidates.subList(0, numOfCandidates);

		candidates.add(0, origin);
		candidates.add(candidates.size() - 1, destination);

		double speed = GeographyUtils.getAverageTravelSpeed(travelMode);
		int[][] travelTimeMatrix = GeographyUtils.calculateTravelTimeMatrix(candidates, speed);

		// Step 3:
		// Find paths between the origin and destination
		
		// TODO: Store information about when a node branched from another node in order
		// to calculate similariy and fill paths with dissimilar paths
		
		// Create a priority queue prioritizing nodes with a
		// high score
		PriorityQueue<Node> q = new PriorityQueue<Node>(
			new Comparator<Node>() {
				public int compare(Node n1, Node n2) {
					if (n1.accumulatedScore > n2.accumulatedScore)
						return -1;
					else if (n1.accumulatedScore < n2.accumulatedScore)
						return 1;
					else
						return 0;
				}
			}
		);
	
		// Store the paths found in this list
		List<Journey> journeys = new ArrayList<>();
		// Insert the start location as the first node
		q.add(new Node(0, itinerary.getStartLocation()));
		
		// Path finding algorithm based on Dijkstra
		while (!q.isEmpty() && journeys.size() < numOfPaths) {

			// Remove the highest priority node from the priority queue
			Node node = q.remove();

			// If the node is the destination, then this is a valid path
			if (node.item.getPlace().equals(destination)) {

				//Build the journey
				Journey newJourney = buildJourney(node);

				if (newJourney.getLength() < minItems) {
					Log.d("PLANIT", "Journey not long enough.");
					continue;
				}

				// Ensure the new journey isn't too similar to any of the
				// other journeys
				int i = 0;
				boolean tooSimilar = false;
				while (i < journeys.size() && !tooSimilar) {
					Journey journey = journeys.get(i);
					tooSimilar = tooSimilar || personalize.getPathSimilarity(
							journey.getVisitedPlaces(),
							node.visitedPlaces) > similarityThreshold;
					i++;
				}
				if (tooSimilar) {
					Log.d("PLANIT", "Eliminated journey because it " +
							"was too similar to another journey.");
					continue;
				}

				// Ensure that the actual travel time isn't too much greater
				// than our estimated travel time
				// TODO: implement minTime and maxTime methods for each place
				// 	Check to see if the new travel time encroaches on the
				// 	minTime at a place
				int estimatedTravelTime = node.getTotalTravelTime();
				int actualTravelTime = newJourney.getTravelTime();
				double travelTimeRatio = actualTravelTime / (double)estimatedTravelTime;
				if (travelTimeRatio > travelTimeThreshhold) {
					Log.d("PLANIT", "Eliminated journey because the " +
							"travel time ratio was too high.");
					continue;
				}

				// If the journey isn't too similar, then we can use it
				journeys.add(newJourney);
				// Remove any paths that are too similar to the new journey
				q.removeIf(n -> personalize.getPathSimilarity(
						node.visitedPlaces,
						n.visitedPlaces) > similarityThreshold);

				continue;
			}


			// Get the next scheduled item in the itinerary
			// Generally this will just be the destination location
			ItineraryItem item = itinerary.getNextItem(node.item.getInterval().getEndDate());

			for (int i = 0; i < candidates.size(); i++) {

				Place place = candidates.get(i);

				// Skip this place if we have been here before or we are 
				if (place.equals(node.item.getPlace()) || node.visitedPlaces.contains(place))
					continue;

				// Calculate travel time in milliseconds from the previous node
				// to this one
				int travelTime = travelTimeMatrix[node.index][i];
				// Skip this candidate if the travel time is unreasonably high
				if (travelTime > maxTravelTime)
					continue;

				// Calculate when we would arrive at this event from the previous event
				// and when we would leave it
				Date arrivalTime = new Date(node.item.getInterval().getEndDate().getTime() + travelTime * 60 * 1000);
				Date leaveTime = new Date(arrivalTime.getTime() + place.getAverageStayTime() * 60 * 1000);
				Interval interval = new Interval(arrivalTime, leaveTime);

				// Ensure that we have enough time to do this event before the next
				// scheduled event in the itinerary
				if (!item.getInterval().getStartDate().before(leaveTime)) {

					// Calculate scores
					double edgeScore = personalize.getEdgeScore(place, interval, travelTime, node.visitedTypes);
					double nodeScore = placeToNodeScore.get(place);
					double score = edgeScore + nodeScore;

					// Exit here if the score is sufficiently low
					if (score < minScore)
						continue;

					// TODO: get rid of this check and make it more integral to
					// how the algorithm s structured
					if (!place.equals(destination)) {
						DefiniteItem newItem = new Event(place, interval);
						q.add(node.branch(i, newItem, score, travelTime));
					} else {
						q.add(node.branch(i, itinerary.getEndLocation(), score, travelTime));
					}
				}
			}
		}

		// STEP 5:
		// Ensure the actual travel time
		
		return journeys;
	}

	private Journey buildJourney(Node endNode) {

		List<DefiniteItem> schedule = new ArrayList<>();
		Node current = endNode.previous;
		while (current.previous != null) {
			schedule.add(current.item);
			current = current.previous;
		}

		return new Journey(schedule);
	}
}
