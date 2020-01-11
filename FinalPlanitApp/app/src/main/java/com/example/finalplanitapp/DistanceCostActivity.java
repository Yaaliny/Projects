package com.example.finalplanitapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class DistanceCostActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //public static int distance;
    //public static String transport;

    private RadioGroup transRadioGroup, targetPriceRadioGroup;
    private TextView searchRadiusValue;
    private SeekBar distanceBar;

    public static int travelMode;
    public static int searchRadius;
    public static int targetPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_cost);

        TextView distanceText = (TextView) findViewById(R.id.distanceText);
        distanceBar = (SeekBar) findViewById(R.id.seekBar3);
        Button preferencesButton = (Button) findViewById(R.id.preferencesButton);
        searchRadiusValue = (TextView) findViewById(R.id.search_radius_value);
        transRadioGroup = (RadioGroup)findViewById(R.id.transport_radio_group);
        targetPriceRadioGroup = (RadioGroup) findViewById(R.id.target_price_radio_group);

        RadioButton transRadioButton = (RadioButton)findViewById(transRadioGroup.getCheckedRadioButtonId());
        travelMode = transRadioGroup.indexOfChild(transRadioButton);

        RadioButton targetPriceRadioButton = (RadioButton)findViewById(targetPriceRadioGroup.getCheckedRadioButtonId());
        targetPrice = targetPriceRadioGroup.indexOfChild(targetPriceRadioButton);

        searchRadiusValue.setText("" + distanceBar.getProgress());
        searchRadius = distanceBar.getProgress();

        distanceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress_value;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress_value = progress;
                searchRadius = progress;
                searchRadiusValue.setText("" + searchRadius);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });


        preferencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivities();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) { }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

    public void openActivities() {

        RadioButton transRadioButton = (RadioButton)findViewById(transRadioGroup.getCheckedRadioButtonId());
        travelMode = transRadioGroup.indexOfChild(transRadioButton);

        RadioButton targetPriceRadioButton = (RadioButton)findViewById(targetPriceRadioGroup.getCheckedRadioButtonId());
        targetPrice = targetPriceRadioGroup.indexOfChild(targetPriceRadioButton);

        searchRadiusValue.setText("" + distanceBar.getProgress());
        searchRadius = distanceBar.getProgress();

        Intent intent = new Intent(this, Activities.class);
        startActivity(intent);
    }
}
