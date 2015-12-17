package com.lob.lapseface.shared.timelapse;

public class TimeLapse {

    private final String mTitle;
    private final String mGif;

    TimeLapse(String title, String gif) {
        this.mTitle = title;
        this.mGif = gif;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getGif() {
        return mGif;
    }
}
