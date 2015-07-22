package com.ghr.ubietyapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class splash extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (isOnline())
        {
            if (isRegistered())
            {
                //forward to login screen
                Intent intent = new Intent(splash.this, AttendanceActivity.class);
                startActivity(intent);
            }
            else
            {
                //forward to regsiter screen
                Intent RegisterActivityIntent = new Intent(this, RegisterActivity.class);
                startActivity(RegisterActivityIntent);
            }
        }
        else
        {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(splash.this);
            alertDialog.setTitle("No Internet");
            alertDialog.setMessage("You are not connected to internet. Please connect and try again!!");
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    dialog.cancel();
                }
            });
            alertDialog.show();
        }
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

    public boolean isRegistered()
    {
        SharedPreferences prefs = getSharedPreferences(Config.PREFS_NAME, MODE_PRIVATE);

        if(!prefs.contains("isRegistered"))
        {
            SharedPreferences.Editor settings = getSharedPreferences(Config.PREFS_NAME, MODE_PRIVATE).edit();
            settings.putBoolean("isRegistered", false);
            settings.commit();
        }
        return prefs.getBoolean("isRegistered", false);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
