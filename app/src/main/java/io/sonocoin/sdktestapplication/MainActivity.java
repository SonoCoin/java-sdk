package io.sonocoin.sdktestapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        {
            int permission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.d("SonoCoin SDK. App itself",
                        String.format("Check for permission failed: %d", permission));
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 5);
            }
            Log.d("SonoCoin SDK. App itself", String.format("Check for permission: %d", permission));
        }
        {
            int permission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.INTERNET);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.d("SonoCoin SDK. App itself", String.format("Check for permission failed: %d", permission));
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.INTERNET}, 5);
            }
            Log.d("SonoCoin SDK. App itself",
                    String.format("Check for permission: %d", permission));
        }
    }

}
