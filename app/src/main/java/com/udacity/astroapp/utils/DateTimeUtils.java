package com.udacity.astroapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class DateTimeUtils {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public static String getCurrentLocalDate() {
        return formatter.format(getCalendar().getTime());
    }

    public static String getPreviousDate(String localDate) {
        Calendar calendar = getCalendar();
        try {
            calendar.setTime(Objects.requireNonNull(formatter.parse(localDate)));
            calendar.add(Calendar.DATE, -1);
            System.out.println(formatter.format(calendar.getTime()));
            return formatter.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Calendar getCalendar() {
        return Calendar.getInstance();
    }

    public static String getFormattedDate(Date date) {
        return formatter.format(date);
    }

    public static int getYear(String date) {
        return Integer.parseInt(date.substring(0, 4));
    }

    public static int getMonth(String date) {
        String monthString = date.substring(5, 7);
        int month = Integer.parseInt(monthString);
        month = month - 1;
        return month;
    }

    public static int getDay(String date) {
        return Integer.parseInt(date.substring(8, 10));
    }

    public static String getFormattedDateFromString (String date) {
        return date.substring(0, 10);
    }
}