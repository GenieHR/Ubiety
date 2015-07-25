package com.ghr.ubietyapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RegisterActivity extends AppCompatActivity {
    JSONObject jsonobject;
String mobNum;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
//
//        TelephonyManager tMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
//        final String mPhoneNumber = tMgr.getSimOperatorName();

        final TextView    tvEmail ;
        final TextView    tvMobile ;
        final TextView    tvEmpNum ;

        tvEmail = (TextView) findViewById(R.id.tvEmail);
             tvMobile = (TextView) findViewById(R.id.tvMobile);
             tvEmpNum = (TextView) findViewById(R.id.tvEmpNum);

        Button button = (Button) findViewById(R.id.btnGetOTP);
        button.setOnClickListener(new View.OnClickListener() {
            String returnString;
            public void onClick(View v) {
                try {

//                    if (!mPhoneNumber.equals(tvMobile)){
//                        Toast.makeText(getApplicationContext(), mPhoneNumber + " _ " + R.string.register_incorrect_mobile_number, Toast.LENGTH_LONG).show();
//                        return;
//                    }
                    mobNum = tvMobile.getText().toString();

                    String urlString = Config.API_URL + Config.METHOD_EMPDETAILS + tvEmpNum.getText() + "/" +tvEmail.getText() + "/" + tvMobile.getText();
                    returnString = new registerTexts().execute(urlString).get();
                    JSONArray obj;

                    obj = new JSONArray(returnString);

                    if (obj.length() == 0)
                    {
                        Toast.makeText(getApplicationContext(), R.string.register_incorrect_details, Toast.LENGTH_LONG).show();
                    }
                    else {
                        jsonobject = obj.getJSONObject(0);
                        String EmpStatus = jsonobject.getString("EmpStatus");
                        Integer OTP = jsonobject.getInt("Otp");
                        if (!EmpStatus.equals("1"))
                        {
                            Toast.makeText(getApplicationContext(), R.string.register_employee_not_authorized , Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            SharedPreferences.Editor settings = getSharedPreferences(Config.PREFS_NAME, MODE_PRIVATE).edit();

                            Calendar c = Calendar.getInstance();
                            SimpleDateFormat df = new SimpleDateFormat("yyyyMMMdd");

                            settings.putString("EmpName", jsonobject.getString("EmpName"));
                            settings.putInt("EmpId", jsonobject.getInt("EmpId"));
                            settings.putString("EmpNum", jsonobject.getString("EmpNum"));
                            settings.putString("Email", jsonobject.getString("Email"));
                            String strDateTime = df.format(c.getTime());
                            settings.putString("Today",strDateTime);
                            settings.putInt("MarkCount", 0);

                            settings.commit();

                            String[] smsInfo = new String[2];

                            smsInfo[0] = Integer.toString(OTP);
                            smsInfo[1] = mobNum;

                            new sendSMS().execute(smsInfo);

                            Intent intent = new Intent(RegisterActivity.this, OTPActivity.class);
                            intent.putExtra("otp", OTP);
                            startActivity(intent);
                        }
                    }
                }
                catch (Exception ex){
                    Toast.makeText(getApplicationContext(), ex.getMessage() , Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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

    private class sendSMS extends AsyncTask<String, Void, String>{

        String s = "";

        @Override
        protected String doInBackground(String... params) {

            try {
                String postData = "";
                postData = "http://smsc.biz/httpapi/send?username=lakshman.pilaka@gmail.com&password=Laks@5347&sender_id=PROMOTIONAL&route=P&phonenumber="+ params[1] +"&message="+ params[0] +"%20Is%20OTP%20for%20accessing%20GenieHR%20Solutions%20Application." ;
                URL url = new URL(postData);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                s = httpURLConnection.getResponseMessage();
            }
            catch (Exception ex) {
                Log.e("SendSMSError: ", ex.getMessage());
            }
            return s;
        }
    }

    private class registerTexts extends AsyncTask<String, Void, String> {

        private String getJSON(String url, int timeout) {
            HttpURLConnection c = null;
            try {
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("GET");
                c.setRequestProperty("Content-length", "0");
                c.setUseCaches(false);
                c.setAllowUserInteraction(false);
                c.setConnectTimeout(timeout);
                c.setReadTimeout(timeout);
                c.connect();
                int status = c.getResponseCode();

                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line+"\n");
                        }
                        br.close();
                        return sb.toString();
                }

            } catch (MalformedURLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            return null;
        }

        @Override
        protected String doInBackground(String... params) {
            return  getJSON(params[0],10000);
        }

    }
}
