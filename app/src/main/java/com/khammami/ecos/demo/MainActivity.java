package com.khammami.ecos.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.khammami.ecos.views.Speedometer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Speedometer speedometerView = findViewById(R.id.speedometer);
        TextView speedTextView = findViewById(R.id.speedTextView);

        speedometerView.setSpeedChangeListener(
                newSpeedValue -> speedTextView.setText(String.valueOf(newSpeedValue))
        );
    }
}