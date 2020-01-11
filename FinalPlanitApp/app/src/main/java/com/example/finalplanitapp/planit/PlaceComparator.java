package com.example.finalplanitapp.planit;

import java.util.Comparator;
import java.util.Map;

public class PlaceComparator implements Comparator<Place> {

    private Map<Place, Double> placeToScore;

    public PlaceComparator(Map<Place, Double> placeToScore) {
        this.placeToScore = placeToScore;
    }

    @Override
    public int compare(Place a, Place b) {

        double scoreA = placeToScore.get(a);
        double scoreB = placeToScore.get(b);

        if (scoreA > scoreB)
            return 1;
        else if (scoreA < scoreB)
            return -1;
        else
            return 0;
    }
}
