package com.example.finalplanitapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.finalplanitapp.planit.GoogleAPI;
import com.google.android.gms.maps.model.PolylineOptions;


import com.example.finalplanitapp.itinerary.DefiniteItem;
import com.example.finalplanitapp.planit.Place;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONException;

import java.io.IOException;
import java.lang.annotation.Inherited;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public List<List<String>> directions;
    public Button googleButton;
    public Button directionButton;
    public List<List<LatLng>> routes;
    public List<Marker> markers;
    public int markerCounter = 0;
    TextView i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        googleButton = (Button) findViewById(R.id.googleButton);

        directionButton = (Button) findViewById(R.id.showDirec);

        i = (TextView) findViewById(R.id.direc);

        directionButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (!(markerCounter == 0 )) {
                    if (i.getVisibility() == v.VISIBLE) {
                        i.setVisibility(v.INVISIBLE);
                    }
                    else {
                        i.setVisibility(v.VISIBLE);
                    }
                }
            }

        });

        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mMap.clear();
                Boolean first = true;
                if (markerCounter == markers.size() - 1) {

                    i.setVisibility(View.INVISIBLE);

                    i.setText("");


                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    for (Marker m : markers) {

                        if (first) {
                            mMap.addMarker(new MarkerOptions().position(m.getPosition()).title(m.getTitle())).showInfoWindow();
                            first = false;
                        }
                        else {
                            mMap.addMarker(new MarkerOptions().position(m.getPosition()).title(m.getTitle()));
                        }

                        builder.include(m.getPosition());
                    }

                    LatLngBounds bounds = builder.build();

                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);

                    mMap.moveCamera(cu);

                    for (List<LatLng> l : routes){
                        mMap.addPolyline(new PolylineOptions().addAll(l).width(15));
                    }
                    markerCounter = 0;

                }
                else {

                    i.setVisibility(View.VISIBLE);

                    mMap.addMarker(new MarkerOptions().position(markers.get(markerCounter).getPosition()).title(markers.get(markerCounter).getTitle())).showInfoWindow();
                    mMap.addMarker(new MarkerOptions().position(markers.get(markerCounter+1).getPosition()).title(markers.get(markerCounter+1).getTitle()));
                    mMap.addPolyline(new PolylineOptions().addAll(routes.get(markerCounter)).width(15));

                    String d = "From " + markers.get(markerCounter).getTitle() + " to " + markers.get(markerCounter+1).getTitle() + "\n";

                    for (String s : directions.get(markerCounter)) {
                        d = d.concat(s + "\n");
                    }

                    i.setText(d);

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    builder.include(markers.get(markerCounter).getPosition());
                    builder.include(markers.get(markerCounter+1).getPosition());

                    LatLngBounds bounds = builder.build();

                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);

                    mMap.moveCamera(cu);

                    markerCounter = markerCounter + 1;

                }




            }
        });
    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        i.setVisibility(View.INVISIBLE);

        directions = new ArrayList<List<String>>();

        routes = new ArrayList<List<LatLng>>();

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        markers = new ArrayList<Marker>();
        List<DefiniteItem> j = JourneyListActivity.journey.getSchedule();
        Boolean first = true;
        for (DefiniteItem item : j) {

            LatLng a = new LatLng(item.getPlace().getLongitude(), item.getPlace().getLatitude());

            Marker newMarker = null;

            if (first) {
                newMarker = mMap.addMarker(new MarkerOptions().position(a).title(item.getPlace().getName()));
                newMarker.showInfoWindow();
                first = false;
            }
            else {
                newMarker = mMap.addMarker(new MarkerOptions().position(a).title(item.getPlace().getName()));
            }
            markers.add(newMarker);


            if (markers.size() >= 2) {

                AsyncTaskRunner runner = new AsyncTaskRunner();
                runner.execute(markers.get(markers.size()-2).getPosition(), markers.get(markers.size()-1).getPosition());
            }

        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (Marker m : markers) {
            builder.include(m.getPosition());
        }
        LatLngBounds bounds = builder.build();

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);

        mMap.moveCamera(cu);

    }



    private class AsyncTaskRunner extends AsyncTask<LatLng,Void,List<LatLng>> {



        @Override
        protected List<LatLng> doInBackground(LatLng... markers) {
            List<LatLng> line = null;
            try {
                Pair<List<String>,List<LatLng>> answer = GoogleAPI.getDirections(markers[0], markers[1]);
                line = answer.second;
                routes.add(answer.second);
                directions.add(answer.first);
                System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
                System.out.println(directions);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return line;
        }

        @Override
        protected void onPostExecute(List<LatLng> line) {
            try {
                mMap.addPolyline(new PolylineOptions().addAll(line).width(15));
            }
            catch(NullPointerException a) {

            }
        }



    }

}
