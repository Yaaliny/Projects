package com.example.finalplanitapp;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
// import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import com.example.finalplanitapp.ExpandableListAdapter;
import com.example.finalplanitapp.itinerary.DefiniteItem;
import com.example.finalplanitapp.itinerary.Interval;
import com.example.finalplanitapp.itinerary.Journey;
import com.example.finalplanitapp.planit.Place;

import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.Toast;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class Itinerary extends AppCompatActivity {

    public ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itin);

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        Button button = (Button)findViewById(R.id.map_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMap();
            }
        });
    }

    public void openMap() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        ArrayList<String> placesandtimes = new ArrayList<>();
        Journey journey = JourneyListActivity.journey;
        List<DefiniteItem> schedule = journey.getSchedule();
        for(int i = schedule.size() - 1; i >= 0; i--) {

            DefiniteItem item = schedule.get(i);
            Place place = item.getPlace();
            Interval interval = item.getInterval();

            listDataHeader.add(place.getName() + " " + interval);

            List<String> items = new ArrayList<>();

            items.add("Address: " + place.getAddress());
            items.add("Average Stay Time: " + item.getAverageStayDuration());
            items.add("Rating: " + place.getRating());

            String priceString = null;
            switch(place.getPriceLvl()) {
                case 0:
                    priceString = "Free";
                    break;
                case 1:
                    priceString = "Inexpensive";
                    break;
                case 2:
                    priceString = "Moderate";
                    break;
                case 3:
                    priceString = "Expensive";
                    break;
                case 4:
                    priceString = "Very Expensive";
                    break;
            }

            if (priceString != null)
                items.add("Price: " + priceString);

            listDataChild.put(listDataHeader.get(listDataHeader.size() - 1), items);
        }
    }
}
