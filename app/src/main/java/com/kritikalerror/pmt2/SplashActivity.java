package com.kritikalerror.pmt2;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class SplashActivity extends ActionBarActivity {

    private static final int REQUEST_CAMERA = 0;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Button proceedButton = (Button) findViewById(R.id.proceed);
        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                finish();
            }
        });

        Button exitButton = (Button) findViewById(R.id.exit);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });


        // Make sure user goes through this before switching activities
        //if (initializeWrapper()) {
        //    Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        //    SplashActivity.this.startActivity(mainIntent);
        //    finish();
        //}

        initializeWrapper();
    }

    private boolean initializeWrapper() {
        int hasCameraPermission = ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.SEND_SMS);
        if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this,
                    Manifest.permission.SEND_SMS)) {
                showOKAlertMessage("You need to allow app to send SMS for the app to function properly",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(SplashActivity.this,
                                        new String[]{Manifest.permission.SEND_SMS},
                                        REQUEST_CODE_ASK_PERMISSIONS);
                            }
                        });
            }
            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[] {Manifest.permission.SEND_SMS},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }

        int hasWriteStoragePermission = ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.READ_CONTACTS);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this,
                    Manifest.permission.READ_CONTACTS)) {
                showOKAlertMessage("You need to allow access to external storage to save photos for the app to function properly",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(SplashActivity.this,
                                        new String[]{Manifest.permission.READ_CONTACTS},
                                        REQUEST_CODE_ASK_PERMISSIONS);
                            }
                        });
            }
            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[] {Manifest.permission.READ_CONTACTS},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }
        return true;
    }

    private void showOKAlertMessage(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(SplashActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
