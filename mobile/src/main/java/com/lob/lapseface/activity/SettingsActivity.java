package com.lob.lapseface.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.google.android.gms.common.api.GoogleApiClient;
import com.lob.lapseface.R;
import com.lob.lapseface.shared.util.GoogleApiClientUtil;
import com.lob.lapseface.shared.util.SharedPreferencesUtil;
import com.lob.lapseface.shared.util.WearSyncConstants;
import com.lob.lapseface.wear.SendToDataLayerThread;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static GoogleApiClient mApiClient;

    private static Activity mActivity;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(final Preference preference, Object value) {
            final String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

                new SendToDataLayerThread(mActivity, mApiClient, WearSyncConstants.PATH, stringValue).start();
            }
            return true;
        }
    };

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        if (preference != null) {
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            try {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                        SharedPreferencesUtil.getSharedPreferences(preference.getContext())
                                .getString(preference.getKey(), ""));
            } catch (Exception e) {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                        SharedPreferencesUtil.getSharedPreferences(preference.getContext())
                                .getBoolean(preference.getKey(), false));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        mActivity = this;

        mApiClient = GoogleApiClientUtil.getInstance()
                .getGoogleApiClient(getApplicationContext());
        mApiClient.connect();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || WatchPreferenceFragment.class.getName().equals(fragmentName)
                || PhonePreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent mainClassIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainClassIntent);
        super.onBackPressed();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class WatchPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_watch);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("list_date_format"));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PhonePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_phone);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("switch_legacy_mode"));
        }
    }
}
