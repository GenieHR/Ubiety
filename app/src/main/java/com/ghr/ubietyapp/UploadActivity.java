package com.ghr.ubietyapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.ghr.ubietyapp.AndroidMultiPartEntity.ProgressListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UploadActivity extends Activity {
    // LogCat tag
    private static final String TAG = AttendanceActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private String filePath = null;
    private TextView txtPercentage;
    private ImageView imgPreview;
    private VideoView vidPreview;
    private Button btnUpload;
    long totalSize = 0;

    public LocationData LocationData;
    LocationData locDat;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        vidPreview = (VideoView) findViewById(R.id.videoPreview);

        // Receiving the data from previous activity

        Intent i = getIntent();

        // image or video path that is captured in previous activity
        filePath = i.getStringExtra("filePath");

        // boolean flag to identify the media type, image or video
        boolean isImage = i.getBooleanExtra("isImage", true);

        if (filePath != null) {
            // Displaying the image or video on the screen
            previewMedia(isImage);
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry, file path is missing!", Toast.LENGTH_LONG).show();
        }

        btnUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // uploading the file to server

                SharedPreferences prefs = getSharedPreferences(Config.PREFS_NAME, MODE_PRIVATE);
                LocationData = new LocationData(prefs.getString("EmpNum", "Error"));

                new UploadFileToServer().execute(LocationData);
                locDat = new LocationData();

                Integer[] attendanceValues = new Integer[2];
                attendanceValues[0] = prefs.getInt("EmpId", -1);
                attendanceValues[1] = 1; //TODO: Check later //Shift Status

                new PunchAttendance().execute(attendanceValues);
            }
        });
    }

    /**
     * Displaying captured image/video on the screen
     * */

    private void previewMedia(boolean isImage) {
        // Checking whether captured media is image or video
        if (isImage) {
            imgPreview.setVisibility(View.VISIBLE);
            vidPreview.setVisibility(View.GONE);
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // down sizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

            imgPreview.setImageBitmap(bitmap);
        } else {
            imgPreview.setVisibility(View.GONE);
            vidPreview.setVisibility(View.VISIBLE);
            vidPreview.setVideoPath(filePath);
            // start playing
            vidPreview.start();
        }
    }


    private class LocationData {
        Double Latitude, Longitude;
        String EmployeeName;
        int markCount;
        GPSTracker gpsTracker = new GPSTracker(UploadActivity.this);

        public LocationData(String employeeName) {
            this.EmployeeName = employeeName;
        }

        public LocationData() {
            this.Latitude = gpsTracker.getLatitude();
            this.Longitude = gpsTracker.getLongitude();
            this.markCount = 1;
        }
    }

    private class PunchAttendance extends  AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... AttendanceDetails) {
            String s = "";
            try {
                Double lat = locDat.Latitude;
                Double lon = locDat.Longitude;
                Integer markCount = locDat.markCount;

                String urlString = Config.API_URL + Config.ATTENDANCE_METHOD +  Integer.toString(AttendanceDetails[0]) + "/" + Integer.toString(AttendanceDetails[1]) + "/" + Double.toString(lat) + "/" + Double.toString(lon) + "/" + Integer.toString(markCount) + "/";
                URL url = new URL(urlString);
                Log.i("URLString", urlString);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                s = httpURLConnection.getResponseMessage();

            } catch (IOException ex) {

            }
            return s;
        }
    }

    /**
     * Uploading the file to server
     * */


    private class UploadFileToServer extends AsyncTask<LocationData, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(LocationData... params) {
            return uploadFile(params[0].EmployeeName);
        }

        private String uploadFile(String name) {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Config.PHOTO_UPLOAD_URL);

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File sourceFile = new File(filePath);

                // Adding file data to http body

                entity.addPart("image", new FileBody(sourceFile));

                // Extra parameters if you want to pass to server

                entity.addPart("name", new StringBody(name, ContentType.TEXT_PLAIN));
//                entity.addPart("latitude", new StringBody(Double.toString(Latitude), ContentType.TEXT_PLAIN));
//                entity.addPart("longitude", new StringBody(Double.toString(Longitude), ContentType.TEXT_PLAIN));


                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call

                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = "Attendance Marked Successfully.";
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Response from server: " + result);

            // showing the server response in an alert dialog
            showAlert(result);

            super.onPostExecute(result);
        }

    }

    /**
     * Method to show alert dialog
     * */

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Message")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}