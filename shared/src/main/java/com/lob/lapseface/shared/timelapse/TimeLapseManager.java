package com.lob.lapseface.shared.timelapse;

import com.lob.lapseface.shared.exception.NotSameArrayLengthException;

import java.util.ArrayList;

public class TimeLapseManager {

    private static final String[] TITLES = {
            "Street Lights",
            "Space Earth",
            "Taxi",
            "Train Subway"
    };

    private static final String[] HEADERS = {
            "street_lights.gif",
            "space_earth.gif",
            "taxi.gif",
            "train_subway.gif"
    };

    private static final String[] ANIMATED_TIME_LAPSE_URLS = {
            "http://i.imgur.com/j6oqwl7.gif",
            "http://i.imgur.com/T0muIyE.gif",
            "http://i.imgur.com/hSaG2WF.gif",
            "http://i.imgur.com/7Nvi8wm.gif"
    };

    private static final String[] STATIC_TIME_LAPSE_URLS = {
            "http://i.imgur.com/ttZqa5j.gif",
            "http://i.imgur.com/s8DaRhP.gif",
            "http://i.imgur.com/xKbn190.gif",
            "http://i.imgur.com/s4g2PpW.gif"
    };

    private static ArrayList<TimeLapse> TIME_LAPSES = new ArrayList<>();

    private static void setTimeLapses() {
        TIME_LAPSES.clear();

        if (TITLES.length != HEADERS.length) {
            try {
                throw new NotSameArrayLengthException();
            } catch (NotSameArrayLengthException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < TITLES.length; i++) {
            TIME_LAPSES.add(new TimeLapse(TITLES[i], HEADERS[i]));
        }
    }

    public static ArrayList<TimeLapse> getTimeLapses() {
        if (TIME_LAPSES.size() > 0) {
            return TIME_LAPSES;
        } else {
            setTimeLapses();
            return TIME_LAPSES;
        }
    }

    public static String[] getAnimatedTimeLapseUrls() {
        return ANIMATED_TIME_LAPSE_URLS;
    }

    public static String[] getStaticTimeLapseUrls() {
        return STATIC_TIME_LAPSE_URLS;
    }

    public static String[] getHeaders() {
        return HEADERS;
    }

    public static int getTimeLapseIndex(String timeLapse) {
        for (int i = 0; i < HEADERS.length; i++) {
            if (HEADERS[i].equals(timeLapse))
                return i - 1;
        }
        return -999;
    }
}
