package com.example.demo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeHelper {

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss a");
        Date date = new Date();

        return sdf.format(date) + "--";
    }
}
