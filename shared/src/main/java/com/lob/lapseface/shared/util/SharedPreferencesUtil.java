package com.lob.lapseface.shared.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.kogitune.wearsharedpreference.WearSharedPreference;

import java.util.ArrayList;

public final class SharedPreferencesUtil {

    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static WearSharedPreference getWearSharedPreferences(Context context) {
        return new WearSharedPreference(context);
    }

    public static void setSecondsForTimeLapse(final Context context, final int timeLapseIndex, final int value) {
        WearSharedPreference wearSharedPreference = getWearSharedPreferences(context);
        wearSharedPreference.put("TimeLapse_Number_" + String.valueOf(timeLapseIndex), value + getSecondsForTimeLapse(context, timeLapseIndex));
        wearSharedPreference.sync(new WearSharedPreference.OnSyncListener() {
            @Override
            public void onSuccess() {
                Log.d("SharedPreferences", "Success!");
            }

            @Override
            public void onFail(Exception e) {
                StoreSyncLaterValues.addElement(timeLapseIndex, value);
            }
        });
    }

    public static int getSecondsForTimeLapse(Context context, int timeLapseIndex) {
        return getWearSharedPreferences(context)
                .get("TimeLapse_Number_" + String.valueOf(timeLapseIndex), 0);
    }

    public static class StoreSyncLaterValues {

        private static ArrayList<Integer> indexesArrayList = new ArrayList<>();
        private static ArrayList<Integer> valuesArrayList = new ArrayList<>();

        public static ArrayList<Integer> getIndexes() {
            return indexesArrayList;
        }

        public static ArrayList<Integer> getValues() {
            return valuesArrayList;
        }

        public static void addElement(int timeLapseIndex, int value) {
            indexesArrayList.add(timeLapseIndex);
            valuesArrayList.add(value);
        }

        public static void clearAll() {
            indexesArrayList.clear();
            valuesArrayList.clear();
        }
    }
}
