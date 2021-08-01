package com.khammami.ecos.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import com.khammami.ecos.views.Speedometer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Speedometer speedometerView = findViewById(R.id.speedometer);
        TextView speedTextView = findViewById(R.id.speedTextView);

        SeekBar speedSeekBar = findViewById(R.id.seekBar);
        speedSeekBar.setMax(127);
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int speed, boolean b) {
                speedometerView.setCurrentSpeed(speed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        speedometerView.setSpeedChangeListener(
                newSpeedValue -> speedTextView.setText(String.valueOf(newSpeedValue))
        );
    }
}