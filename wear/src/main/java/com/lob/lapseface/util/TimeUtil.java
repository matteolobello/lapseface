package com.lob.lapseface.util;

import android.content.res.Resources;

import com.lob.lapseface.R;

public final class TimeUtil {

    public static String getMonth(Resources res, int timeMonth) {
        if (timeMonth == 1) {
            return res.getString(R.string.jenuary);
        } else if (timeMonth == 2) {
            return res.getString(R.string.february);
        } else if (timeMonth == 3) {
            return res.getString(R.string.march);
        } else if (timeMonth == 4) {
            return res.getString(R.string.april);
        } else if (timeMonth == 5) {
            return res.getString(R.string.may);
        } else if (timeMonth == 6) {
            return res.getString(R.string.june);
        } else if (timeMonth == 7) {
            return res.getString(R.string.july);
        } else if (timeMonth == 8) {
            return res.getString(R.string.august);
        } else if (timeMonth == 9) {
            return res.getString(R.string.september);
        } else if (timeMonth == 10) {
            return res.getString(R.string.october);
        } else if (timeMonth == 11) {
            return res.getString(R.string.november);
        } else {
            return res.getString(R.string.december);
        }
    }

    public static String getWeekDay(Resources res, int timeWeekDay) {
        if (timeWeekDay == 1) {
            return res.getString(R.string.monday);
        } else if (timeWeekDay == 2) {
            return res.getString(R.string.tuesday);
        } else if (timeWeekDay == 3) {
            return res.getString(R.string.wednesday);
        } else if (timeWeekDay == 4) {
            return res.getString(R.string.thursday);
        } else if (timeWeekDay == 5) {
            return res.getString(R.string.friday);
        } else if (timeWeekDay == 6) {
            return res.getString(R.string.saturday);
        } else {
            return res.getString(R.string.sunday);
        }
    }
}
