package com.ghr.ubietyapp;

/**
 * Created by laks on 19/07/2015.
 */
public class Config {
    // File upload url (replace the ip with your server address)
    public static final String PHOTO_UPLOAD_URL = "http://ubietyapi.azurewebsites.net/services/photoupload.aspx";
    public static final String API_URL = "http://ubietyapi.azurewebsites.net/api/";
    public  static final String METHOD_EMPDETAILS = "Employees/GetEmpDetails/";
    public static final String ATTENDANCE_METHOD = "attendance/punch/";

    public static final String PREFS_NAME = "UbietyPreferences";

    // Directory name to store captured images and videos

    public static final String IMAGE_DIRECTORY_NAME = "Android File Upload";

    public static final int total_mark_count = 20;

}

