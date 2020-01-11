package com.example.finalplanitapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StartEndLocation extends AppCompatActivity {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");

    Button save;
    EditText locationEditText;
    public static String location;
    public static String startTime;
    public static String endTime;

    public static Date startDate, endDate;

    Button startTimeButton, endTimeButton;
    TextView startTimeTextView, endTimeTextView;
    int startHour, startMinute, endHour, endMinute;

    Calendar startCalendar, endCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_end_location);

        // all the objects we have on screen
        save = (Button) findViewById(R.id.saveButton);
        locationEditText = (EditText) findViewById(R.id.locationEditText);

        startTimeButton = findViewById(R.id.startTimeButton);
        startTimeTextView = findViewById(R.id.start_time_text);

        endTimeButton = findViewById(R.id.endTimeButton);
        endTimeTextView = findViewById(R.id.end_time_text);

        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();

        startTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog startTimePickerDialog = new TimePickerDialog(StartEndLocation.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                            startCalendar.set(Calendar.HOUR_OF_DAY, hour);
                            startCalendar.set(Calendar.MINUTE, minute);
                            startCalendar.set(Calendar.MILLISECOND, 0);
                            startDate = startCalendar.getTime();
                            startTimeTextView.setText(dateFormat.format(startDate));
                        }
                }, startHour, startMinute, false);
                startTimePickerDialog.show();
            }
        });

        endTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog endTimePickerDialog = new TimePickerDialog(StartEndLocation.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                            endCalendar.set(Calendar.HOUR_OF_DAY, hour);
                            endCalendar.set(Calendar.MINUTE, minute);
                            endCalendar.set(Calendar.MILLISECOND, 0);
                            endDate = endCalendar.getTime();
                            endTimeTextView.setText(dateFormat.format(endDate));
                        }
                    }, endHour, endMinute, false);
                endTimePickerDialog.show();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                location = locationEditText.getText().toString();

                if (location.length() == 0)
                    Toast.makeText(getApplicationContext(), "Please enter a location.", Toast.LENGTH_SHORT).show();
                else if (startDate == null || endDate == null)
                    Toast.makeText(getApplicationContext(), "Please enter a start date and end date.", Toast.LENGTH_SHORT).show();
                else if (endDate.before(startDate))
                    Toast.makeText(getApplicationContext(), "End date must be after start date.", Toast.LENGTH_SHORT).show();
                else if (Math.abs(endDate.getTime() - startDate.getTime()) <= 60 * 60 * 1000)
                    Toast.makeText(getApplicationContext(), "Trip must be at least one hour long.", Toast.LENGTH_SHORT).show();
                else
                    openActivity();
            }
        });
    }

    public void openActivity() {
        Intent intent = new Intent(StartEndLocation.this, DistanceCostActivity.class);
        startActivity(intent);
    }
}