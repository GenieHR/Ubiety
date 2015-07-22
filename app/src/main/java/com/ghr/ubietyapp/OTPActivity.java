package com.ghr.ubietyapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class OTPActivity extends AppCompatActivity {
    Integer OTPReceived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        final TextView tvOtp;

        tvOtp = (TextView) findViewById(R.id.tvOtp);

        Bundle b = getIntent().getExtras();
        OTPReceived = b.getInt("otp");
        tvOtp.setText(Integer.toString(OTPReceived));

        Button button = (Button) findViewById(R.id.login);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (Integer.valueOf(tvOtp.getText().toString()).intValue() == OTPReceived.intValue()){

                    SharedPreferences.Editor settings = getSharedPreferences(Config.PREFS_NAME, MODE_PRIVATE).edit();
                    settings.putBoolean("isRegistered", true);

                    settings.commit();

                    Intent intent = new Intent(OTPActivity.this, AttendanceActivity.class);
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), R.string.otp_invalid_otp, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ot, menu);
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
