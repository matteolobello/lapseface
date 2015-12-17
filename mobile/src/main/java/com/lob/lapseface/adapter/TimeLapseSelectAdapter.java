package com.lob.lapseface.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.lob.lapseface.R;
import com.lob.lapseface.activity.MainActivity;
import com.lob.lapseface.activity.TimeLapseSelectActivity;
import com.lob.lapseface.shared.timelapse.TimeLapse;
import com.lob.lapseface.shared.timelapse.TimeLapseManager;
import com.lob.lapseface.shared.util.GoogleApiClientUtil;
import com.lob.lapseface.shared.util.SharedPreferencesUtil;
import com.lob.lapseface.shared.util.WearSyncConstants;
import com.lob.lapseface.util.InternetUtil;
import com.lob.lapseface.wear.SendToDataLayerThread;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TimeLapseSelectAdapter extends RecyclerView.Adapter<TimeLapseSelectAdapter.ViewHolder> {

    private Activity mActivity;

    private GoogleApiClient mApiClient;

    private boolean mFromMainActivity;
    private boolean mWasAnimating;

    private ArrayList<TimeLapse> mTimeLapses;

    public TimeLapseSelectAdapter(Activity activity, ArrayList<TimeLapse> timeLapses) {
        this.mTimeLapses = timeLapses;
        this.mActivity = activity;

        String mainActivityClassName = MainActivity.class.getSimpleName();
        String currentActivityClassName = activity.getClass().getSimpleName();
        this.mFromMainActivity = currentActivityClassName.equals(mainActivityClassName);

        mApiClient = GoogleApiClientUtil.getInstance().getGoogleApiClient(activity);
        mApiClient.connect();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new ViewHolder(inflater.inflate(R.layout.card_view_time_lapse_item, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final View rootView = holder.rootView;
        final ImageView header = holder.mHeader;
        final TextView title = holder.mTitle;
        final TextView subTitle = holder.mSubTitle;

        final TimeLapse timeLapse = mTimeLapses.get(position);

        final int seconds = SharedPreferencesUtil.getSecondsForTimeLapse(
                mActivity, TimeLapseManager.getTimeLapseIndex(timeLapse.getGif()));

        title.setText(mFromMainActivity ? mActivity.getString(R.string.time_lapse) : timeLapse.getTitle());
        title.setTextColor(Color.parseColor(mActivity.getString(R.string.card_title_color)));

        if (!mFromMainActivity) {
            String message;
            if (seconds < 60) {
                message = seconds + " " + (seconds == 1 ? mActivity.getString(R.string.second) : mActivity.getString(R.string.seconds));
            } else {
                message = getMinutesFromSeconds(seconds);
            }
            subTitle.setText(message);
        }
        subTitle.setTextColor(Color.parseColor(mActivity.getString(R.string.card_subtitle_color)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rootView.setForeground(getSelectedItemDrawable());
        }

        setStaticHeader(header, timeLapse);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!mFromMainActivity) {
                            new SendToDataLayerThread(mActivity, mApiClient, WearSyncConstants.PATH,
                                    String.valueOf(TimeLapseManager.getTimeLapseIndex(timeLapse.getGif()))).start();
                        } else {
                            Intent timeLapseSelectIntent = new Intent(mActivity, TimeLapseSelectActivity.class);
                            timeLapseSelectIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mActivity.startActivity(timeLapseSelectIntent);
                        }
                    }
                }).start();
            }
        });
        if (!isLegacyModeOn()) {
            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    animateHeader(header, timeLapse);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mFromMainActivity)
            return 1;
        return mTimeLapses.size();
    }

    private Drawable getSelectedItemDrawable() {
        int[] attrs = new int[]{R.attr.selectableItemBackground};
        TypedArray ta = mActivity.obtainStyledAttributes(attrs);
        Drawable selectedItemDrawable = ta.getDrawable(0);
        ta.recycle();
        return selectedItemDrawable;
    }

    private String getMinutesFromSeconds(int seconds) {
        final int finalMilliseconds = (seconds * 1000);
        final int finalMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(finalMilliseconds);
        final int finalSeconds = (int) (TimeUnit.MILLISECONDS.toSeconds(finalMilliseconds)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalMilliseconds)));
        return finalMinutes
                + " "
                + (finalMinutes == 1 ? mActivity.getString(R.string.minute) : mActivity.getString(R.string.minutes))
                + ", "
                + finalSeconds
                + " "
                + (finalSeconds == 1 ? mActivity.getString(R.string.second) : mActivity.getString(R.string.seconds));
    }

    private boolean isLegacyModeOn() {
        return SharedPreferencesUtil.getSharedPreferences(mActivity)
                .getBoolean("switch_legacy_mode", false);
    }

    private void setStaticHeader(ImageView header, TimeLapse timeLapse) {
        if (InternetUtil.hasActiveInternetConnection(mActivity) && !isLegacyModeOn()) {
            String[] timeLapseUrls = TimeLapseManager.getStaticTimeLapseUrls();
            Glide.with(mActivity)
                    .load(timeLapseUrls[TimeLapseManager.getTimeLapseIndex(timeLapse.getGif()) + 1])
                    .asGif()
                    .dontAnimate()
                    .into(header);
        } else {
            header.setVisibility(View.GONE);
        }
    }

    private void animateHeader(ImageView header, TimeLapse timeLapse) {
        if (InternetUtil.hasActiveInternetConnection(mActivity)) {
            if (!mWasAnimating) {
                String[] timeLapseUrls = TimeLapseManager.getAnimatedTimeLapseUrls();
                Glide.with(mActivity)
                        .load(timeLapseUrls[TimeLapseManager.getTimeLapseIndex(timeLapse.getGif()) + 1])
                        .asGif()
                        .crossFade()
                        .into(header);
                mWasAnimating = true;
            } else {
                setStaticHeader(header, timeLapse);
                mWasAnimating = false;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View rootView;
        public ImageView mHeader;
        public TextView mTitle;
        public TextView mSubTitle;

        public ViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.mHeader = (ImageView) rootView.findViewById(R.id.card_image);
            this.mTitle = (TextView) rootView.findViewById(R.id.card_title);
            this.mSubTitle = (TextView) rootView.findViewById(R.id.card_subtitle);
        }
    }
}
