package com.mct.logcatwindow;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_show_logcat).setOnClickListener(v -> {
            LogWindow.init(this);
            LogWindow.instance().attachBubbleControl(this);
        });
        findViewById(R.id.btn_next).setOnClickListener(v -> {
            Log.i("ddd", "onCreate: new Activity");
            startActivity(new Intent(this, MainActivity.class));
        });
        /*/
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            int i = 0;
            @Override
            public void run() {
                Log.i("ddd", "Log: " + (++i));
                handler.postDelayed(this, 500);
            }
        });
        /*/
    }
}