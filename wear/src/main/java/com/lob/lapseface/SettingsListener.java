package com.lob.lapseface;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.lob.lapseface.shared.util.WearSyncConstants;

public class SettingsListener extends WearableListenerService {

    private final String FROM_SETTINGS = "-from_settings";
    private final String NEW_VALUE = "newValue";
    private final String NEW_TIMELAPSE = "newTimeLapse";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals(WearSyncConstants.PATH)) {
            String message = new String(messageEvent.getData());
            Intent intent = new Intent(WearSyncConstants.PATH);
            if (message.contains(FROM_SETTINGS)) {
                intent.putExtra(NEW_VALUE, message);
            } else {
                intent.putExtra(NEW_TIMELAPSE, message);
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}