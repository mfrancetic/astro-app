package com.udacity.astroapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class DateTimeUtils {

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

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

    private static Calendar getCalendar() {
        return Calendar.getInstance();
    }
}