package com.lob.lapseface.shared.timelapse;

import com.lob.lapseface.shared.exception.NotSameArrayLengthException;

import java.util.ArrayList;

public class TimeLapseManager {

    private static final String[] mTitles = {
            "Street Lights",
            "Space Earth",
            "Taxi",
            "Train Subway"
    };

    private static final String[] mHeaders = {
            "street_lights.gif",
            "space_earth.gif",
            "taxi.gif",
            "train_subway.gif"
    };

    private static final String[] mAnimatedTimeLapseUrls = {
            "http://i.imgur.com/j6oqwl7.gif",
            "http://i.imgur.com/T0muIyE.gif",
            "http://i.imgur.com/hSaG2WF.gif",
            "http://i.imgur.com/7Nvi8wm.gif"
    };

    private static final String[] mStaticTimeLapseUrls = {
            "http://i.imgur.com/ttZqa5j.gif",
            "http://i.imgur.com/s8DaRhP.gif",
            "http://i.imgur.com/xKbn190.gif",
            "http://i.imgur.com/s4g2PpW.gif"
    };

    private static ArrayList<TimeLapse> mTimeLapses = new ArrayList<>();

    private static void setTimeLapses() {
        mTimeLapses.clear();

        if (mTitles.length != mHeaders.length) {
            try {
                throw new NotSameArrayLengthException();
            } catch (NotSameArrayLengthException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < mTitles.length; i++) {
            mTimeLapses.add(new TimeLapse(mTitles[i], mHeaders[i]));
        }
    }

    public static ArrayList<TimeLapse> getTimeLapses() {
        if (mTimeLapses.size() > 0) {
            return mTimeLapses;
        } else {
            setTimeLapses();
            return mTimeLapses;
        }
    }

    public static String[] getAnimatedTimeLapseUrls() {
        return mAnimatedTimeLapseUrls;
    }

    public static String[] getStaticTimeLapseUrls() {
        return mStaticTimeLapseUrls;
    }

    public static String[] getHeaders() {
        return mHeaders;
    }

    public static int getTimeLapseIndex(String timeLapse) {
        for (int i = 0; i < mHeaders.length; i++) {
            if (mHeaders[i].equals(timeLapse))
                return i - 1;
        }
        return -999;
    }
}
