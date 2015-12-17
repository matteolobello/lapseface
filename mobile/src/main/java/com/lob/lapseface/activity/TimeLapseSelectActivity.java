package com.lob.lapseface.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.lob.lapseface.R;
import com.lob.lapseface.adapter.TimeLapseSelectAdapter;
import com.lob.lapseface.shared.timelapse.TimeLapseManager;
import com.lob.lapseface.shared.util.GoogleApiClientUtil;
import com.lob.lapseface.shared.util.SharedPreferencesUtil;
import com.lob.lapseface.shared.util.WearSyncConstants;
import com.lob.lapseface.wear.SendToDataLayerThread;

public class TimeLapseSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_lapse_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final GoogleApiClient mApiClient = GoogleApiClientUtil.getInstance().getGoogleApiClient(getApplicationContext());
        mApiClient.connect();

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        final Activity activity = this;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.random_timelapse_applied, Snackbar.LENGTH_SHORT).show();
                new SendToDataLayerThread(activity, mApiClient,
                        WearSyncConstants.PATH,
                        String.valueOf(-999)).start();
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Snackbar.make(view, R.string.apply_random_timelapse, Snackbar.LENGTH_SHORT).show();
                return true;
            }
        });

        SharedPreferences settings = SharedPreferencesUtil.getSharedPreferences(getApplicationContext());

        if (settings.getBoolean("first_time_select_timelapse", true)) {
            Snackbar.make(fab, R.string.first_time_select_timelapse_message, Snackbar.LENGTH_LONG).show();
            settings.edit().putBoolean("first_time_select_timelapse", false).apply();
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(new TimeLapseSelectAdapter(this,
                TimeLapseManager.getTimeLapses()));
    }
}
