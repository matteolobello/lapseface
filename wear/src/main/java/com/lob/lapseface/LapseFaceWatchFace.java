package com.lob.lapseface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.lob.lapseface.shared.timelapse.TimeLapseManager;
import com.lob.lapseface.shared.util.SharedPreferencesUtil;
import com.lob.lapseface.shared.util.WearSyncConstants;
import com.lob.lapseface.util.TimeUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class LapseFaceWatchFace extends CanvasWatchFaceService {

    private static final Typeface NORMAL_TYPEFACE = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(60);
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {

        private final WeakReference<LapseFaceWatchFace.Engine> mWeakReference;

        public EngineHandler(LapseFaceWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            LapseFaceWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        private final Handler mUpdateTimeHandler = new EngineHandler(this);
        private final ArrayList<Movie> mMovies = new ArrayList<>();
        private final ArrayList<String> mTimeLapses = new ArrayList<>();
        private final String[] mGifs = TimeLapseManager.getHeaders();
        private boolean mRegisteredTimeZoneReceiver;
        private boolean mAmbient;
        private boolean mIsRound;
        private boolean mLowBitAmbient;
        private boolean mMustChangeVideo;
        private boolean mVisible;
        private boolean mIsWeekDayMonthDayMonth = true;
        private Paint mBackgroundPaint;
        private Paint mTextPaint;
        private Paint mSubTextPaint;
        private Time mTime;
        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        private float mXOffset;
        private float mYOffset;
        private float mSubXOffset;
        private long mMovieStart;
        private long mNow;
        private int mWhichVideo = 0;
        private Canvas mCanvas;
        private Rect mBounds;
        private Movie mMovie;
        private String mText;
        private String mSubText;
        private BroadcastReceiver mSettingsBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String newValue = intent.getStringExtra("newValue");
                if (newValue != null) {
                    mIsWeekDayMonthDayMonth = newValue.replace("-from_settings", "").equals("0");
                } else {
                    mMustChangeVideo = true;
                    int which = Integer.valueOf(intent.getStringExtra("newTimeLapse"));
                    if (which == -999) {
                        int tmpWhichVideo = new Random().nextInt(mMovies.size() + 1);
                        if (tmpWhichVideo == mWhichVideo) {
                            if (mMovies.get(tmpWhichVideo + 1) != null) {
                                mWhichVideo = tmpWhichVideo + 1;
                            } else {
                                mWhichVideo = tmpWhichVideo - 1;
                            }
                        } else {
                            mWhichVideo = tmpWhichVideo;
                        }
                    } else {
                        mWhichVideo = which;
                    }
                }
                init();
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            mNow = System.currentTimeMillis();

            setWatchFaceStyle(new WatchFaceStyle.Builder(LapseFaceWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setAcceptsTapEvents(true)
                    .setShowUnreadCountIndicator(true)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            Resources resources = LapseFaceWatchFace.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            mBackgroundPaint = new Paint();

            mTextPaint = new Paint();
            mTextPaint = createTextPaint(resources.getColor(R.color.digital_text));

            mSubTextPaint = new Paint();
            mSubTextPaint = createTextPaint(resources.getColor(R.color.digital_text));

            mTime = new Time();

            LocalBroadcastManager.getInstance(getApplicationContext())
                    .registerReceiver(mSettingsBroadcastReceiver, new IntentFilter(WearSyncConstants.PATH));

            try {
                for (String aGif : mGifs) {
                    mTimeLapses.add(aGif);
                    mMovies.add(Movie.decodeStream(getResources().getAssets().open(aGif)));
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            LocalBroadcastManager.getInstance(getApplicationContext())
                    .unregisterReceiver(mSettingsBroadcastReceiver);
            super.onDestroy();
        }

        @Override
        public void onTapCommand(@TapType int tapType, int x, int y, long eventTime) {
            super.onTapCommand(tapType, x, y, eventTime);
            switch (tapType) {
                case TAP_TYPE_TAP:
                    mMustChangeVideo = true;
                    init();
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            mVisible = visible;

            if (visible) {
                registerReceiver();
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            updateTimer();
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            Resources resources = LapseFaceWatchFace.this.getResources();
            mIsRound = insets.isRound();

            mSubXOffset = resources.getDimension(mIsRound
                    ? R.dimen.sub_digital_x_offset_round
                    : R.dimen.sub_digital_x_offset);

            float textSize = resources.getDimension(mIsRound
                    ? R.dimen.digital_text_size_round
                    : R.dimen.digital_text_size);

            float subTextSize = resources.getDimension(mIsRound
                    ? R.dimen.digital_text_size_round
                    : R.dimen.digital_text_size);

            mTextPaint.setTextSize(textSize);
            mSubTextPaint.setTextSize(subTextSize);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                init();
                if (mLowBitAmbient) {
                    mTextPaint.setAntiAlias(!inAmbientMode);
                    mSubTextPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            updateTimer();
        }

        @Override
        public void onDraw(final Canvas canvas, Rect bounds) {
            this.mCanvas = canvas;
            this.mBounds = bounds;
            init();
        }

        private void init() {
            mTime.setToNow();
            mCanvas.drawRect(0, 0, mBounds.width(), mBounds.height(), mBackgroundPaint);

            mText = String.format("%d:%02d", mTime.hour, mTime.minute);

            if (mIsWeekDayMonthDayMonth) {
                mSubText = TimeUtil.getWeekDay(getResources(), mTime.weekDay)
                        + " " + mTime.monthDay
                        + " " + TimeUtil.getMonth(getResources(), mTime.month + 1);
            } else {
                mSubText = mTime.monthDay
                        + " " + TimeUtil.getMonth(getResources(), mTime.month + 1)
                        + " " + TimeUtil.getWeekDay(getResources(), mTime.weekDay);
            }

            if (!isInAmbientMode() && mVisible) {
                startTimeLapse();
                drawTextElements();
            } else {
                setBlackBackground();
            }
        }

        private void drawTextElements() {
            mXOffset = mTextPaint.measureText(mText) / 2;
            mSubXOffset = mSubTextPaint.measureText(mSubText) / 2;

            float textSize = getResources().getDimension(mIsRound
                    ? R.dimen.sub_digital_text_size_round
                    : R.dimen.sub_digital_text_size);
            mSubTextPaint.setTextSize(textSize);

            mTextPaint.setAntiAlias(!isInAmbientMode());
            mSubTextPaint.setAntiAlias(!isInAmbientMode());

            mCanvas.drawText(mText, mBounds.centerX() - mXOffset, mYOffset, mTextPaint);
            mCanvas.drawText(mSubText, mBounds.centerX() - mSubXOffset, mYOffset + (mYOffset / 2), mSubTextPaint);
        }

        private void setBlackBackground() {
            mBackgroundPaint.setColor(Color.BLACK);
            mTextPaint.setAntiAlias(!mAmbient);
            float textSize = getResources().getDimension(mIsRound
                    ? R.dimen.sub_digital_text_size_round
                    : R.dimen.sub_digital_text_size);
            mSubTextPaint.setTextSize(textSize);
            mTextPaint.setAntiAlias(!isInAmbientMode());
            mSubTextPaint.setAntiAlias(!isInAmbientMode());
            mCanvas.drawText(mText, mBounds.centerX() - mXOffset, mYOffset, mTextPaint);
            mCanvas.drawText(mSubText, mBounds.centerX() - mSubXOffset, mYOffset + (mYOffset / 2), mSubTextPaint);
        }

        private void startTimeLapse() {
            try {
                if (mMustChangeVideo) {
                    mWhichVideo++;
                    try {
                        mMovie = mMovies.get(mWhichVideo);
                    } catch (IndexOutOfBoundsException exception) {
                        setFirstTimeLapse();
                    }
                    mMustChangeVideo = false;
                    SharedPreferencesUtil.setSecondsForTimeLapse(getApplicationContext(),
                            TimeLapseManager.getTimeLapseIndex(mTimeLapses.get(mWhichVideo - 1 != -1 ? mWhichVideo - 1 : mTimeLapses.size() - 1)),
                            (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - mNow));

                    mNow = System.currentTimeMillis();
                } else {
                    mMovie = mMovies.get(mWhichVideo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mMovie != null) {
                long now = SystemClock.uptimeMillis();
                if (mMovieStart == 0) {
                    mMovieStart = now;
                }
                int relTime = (int) ((now - mMovieStart) % mMovie.duration());
                mMovie.setTime(relTime);
                mMovie.draw(mCanvas, 0, 0, mBackgroundPaint);
                invalidate();
            }
        }

        private void setFirstTimeLapse() {
            mWhichVideo = 0;
            mMovie = mMovies.get(mWhichVideo);
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            LapseFaceWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            LapseFaceWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }
}
