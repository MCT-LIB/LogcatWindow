package com.mct.logcatwindow;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private LogWindow logWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logWindow = new LogWindow(this, getApplication());
        findViewById(R.id.btn_show_logcat).setOnClickListener(v -> {
            if (logWindow.isAdd()) {
                logWindow.removeLogView();
            } else {
                logWindow.createLogView();
            }
        });
    }

}