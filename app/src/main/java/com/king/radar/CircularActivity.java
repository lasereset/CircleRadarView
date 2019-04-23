package com.king.radar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.king.view.radarview.CircleSeekbar1;

/**
 * Created by xinchen on 19-4-23.
 */

public class CircularActivity extends AppCompatActivity {
    CircleSeekbar1 circularSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cricleseekbar);

        circularSeekBar = (CircleSeekbar1) findViewById(R.id.circularSeekBar1);
    }
}
