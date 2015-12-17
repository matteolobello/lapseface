package com.lob.lapseface.wear;


import android.app.Activity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.lob.lapseface.activity.SettingsActivity;

public class SendToDataLayerThread extends Thread {

    private String mPath;
    private String mMessage;
    private GoogleApiClient mApiClient;
    private boolean mFromSettings;

    public SendToDataLayerThread(Activity activity, GoogleApiClient api, String path, String message) {
        this.mPath = path;
        this.mMessage = message;
        this.mApiClient = api;
        this.mFromSettings = SettingsActivity.class.getClass().getSimpleName()
                .equals(activity.getClass().getSimpleName());
    }

    @Override
    public void run() {
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
        for (Node node : nodes.getNodes()) {
            if (mFromSettings) {
                mMessage = mMessage + "-from_settings";
            }
            Wearable.MessageApi.sendMessage(mApiClient, node.getId(), mPath, mMessage.getBytes()).await();
        }
    }
}