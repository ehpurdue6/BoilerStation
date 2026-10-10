package com.example.boilerstation;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    DownloadFeedback feedback;
    CalendarEvents calendar;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        feedback = new DownloadFeedback();
        calendar = new CalendarEvents(feedback);
        context = this;
    }
    public class DownloadFeedback {
        public void DataGreenFlag() {
            // Code that executes if the app loads the data.
            ((TextView) findViewById(R.id.helloWorld)).setText(
                    calendar.getTestString() );
        }

        public void DataRedFlag(Exception e) {
            try {
                calendar.ReadAndSetEvents();
                ((TextView) findViewById(R.id.helloWorld)).setText(
                        calendar.getTestString() );
            } catch (Exception f) {
                // Code that executes if the app fails to load the data.
                ((TextView) findViewById(R.id.helloWorld)).setText(
                        "\n" + e.toString() + "\n" + f.toString());
                f.printStackTrace();
            }
        }

        Context getContext() { return context; }
    }
}