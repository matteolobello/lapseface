package com.lob.lapseface.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.lob.lapseface.R;
import com.lob.lapseface.adapter.TimeLapseSelectAdapter;
import com.lob.lapseface.shared.timelapse.TimeLapseManager;
import com.lob.lapseface.shared.util.SharedPreferencesUtil;
import com.lob.lapseface.util.IntentUtil;
import com.lob.lapseface.util.PersonalInfo;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(new TimeLapseSelectAdapter(this, TimeLapseManager.getTimeLapses()));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Snackbar.make(fab, R.string.settings, Snackbar.LENGTH_SHORT).show();
                return true;
            }
        });

        SharedPreferences settings = SharedPreferencesUtil.getSharedPreferences(getApplicationContext());

        if (settings.getBoolean("first_time", true)) {
            Snackbar.make(fab, R.string.first_time_message, Snackbar.LENGTH_LONG).show();
            settings.edit().putBoolean("first_time", false).apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_contact_the_dev) {

            AlertDialog alertDialog;

            CharSequence[] items = {
                    getString(R.string.google_plus), getString(R.string.email), getString(R.string.personal_website)
            };

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.contact_the_dev);
            builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0:
                            startActivity(IntentUtil.browser(PersonalInfo.GOOGLE_PLUS));
                            break;
                        case 1:
                            startActivity(IntentUtil.mail(PersonalInfo.MAIL_ADDRESS));
                            break;
                        case 2:
                            startActivity(IntentUtil.browser(PersonalInfo.WEBSITE));
                            break;
                    }
                    dialog.cancel();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        } else if (item.getItemId() == R.id.action_library) {
            new LibsBuilder()
                    .withFields(R.string.class.getFields())
                    .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                    .start(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(IntentUtil.home());
    }
}
