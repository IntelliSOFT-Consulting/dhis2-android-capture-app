package com.nacare.capture.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.android.androidskeletonapp.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.service.SyncStatusHelper;
import com.nacare.capture.ui.v2.EventsFragment;
import com.nacare.capture.ui.v2.ProgramsFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.nacare.capture.data.service.LogOutService;

import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.domain.aggregated.data.AggregatedD2Progress;
import org.hisp.dhis.android.core.tracker.exporter.TrackerD2Progress;
import org.hisp.dhis.android.core.user.User;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private CompositeDisposable compositeDisposable;

    private FloatingActionButton syncMetadataButton;
    private FloatingActionButton syncDataButton;

    private TextView syncStatusText;
    private ProgressBar progressBar;

    private boolean isSyncing = false;

    public static Intent getMainActivityIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        compositeDisposable = new CompositeDisposable();

        User user = getUser();

        inflateMainView();
        createNavigationView(user);
        loadPrograms();
        if (savedInstanceState == null) {
            loadFragment(new ProgramsFragment());
        }
//        loadActiveProgram()
    }

    /* private fun loadActiveProgram() {
         val programs=SyncStatusHelper.programCount()
     }*/
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null); // Add to the back stack, so the user can navigate back
        transaction.commit();
    }

    @SuppressLint("CheckResult")
    private void loadPrograms() {
        Sdk.d2().programModule().programs()
                .get()
                .subscribe(programs -> {
                    Log.e("TAG", "Programs Founds" + programs);
                }); //List<Program>

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSyncDataAndButtons();
    }

    private User getUser() {
        return Sdk.d2().userModule().user().blockingGet();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawerLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
    }

    private void inflateMainView() {
    /*    syncMetadataButton = findViewById(R.id.syncMetadataButton);
        syncDataButton = findViewById(R.id.syncDataButton);

        syncStatusText = findViewById(R.id.notificator);
        progressBar = findViewById(R.id.syncProgressBar);

        syncMetadataButton.setOnClickListener(view -> {
            setSyncing();
            Snackbar.make(view, "Syncing metadata", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            syncStatusText.setText(R.string.syncing_metadata);
            syncMetadata();
        });

        syncDataButton.setOnClickListener(view -> {
            setSyncing();
            Snackbar.make(view, "Syncing data", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            syncStatusText.setText(R.string.syncing_data);
            downloadData();
        });*/
    }

    private void setSyncing() {
        isSyncing = true;
        progressBar.setVisibility(View.VISIBLE);
        syncStatusText.setVisibility(View.VISIBLE);
        updateSyncDataAndButtons();
    }

    private void setSyncingFinished() {
       /* isSyncing = false;
        progressBar.setVisibility(View.GONE);
        syncStatusText.setVisibility(View.GONE);
        updateSyncDataAndButtons();*/
    }

    private void disableAllButtons() {
     /*   setEnabledButton(syncMetadataButton, false);
        setEnabledButton(syncDataButton, false);*/
    }

    private void enablePossibleButtons(boolean metadataSynced) {
      /*  if (!isSyncing) {
            setEnabledButton(syncMetadataButton, true);
            if (metadataSynced) {
                setEnabledButton(syncDataButton, true);
            }
        }*/
    }

    private void setEnabledButton(FloatingActionButton floatingActionButton, boolean enabled) {
        floatingActionButton.setEnabled(enabled);
        floatingActionButton.setAlpha(enabled ? 1.0f : 0.3f);
    }

    private void updateSyncDataAndButtons() {
        disableAllButtons();

        int programCount = SyncStatusHelper.programCount();
        int dataSetCount = SyncStatusHelper.dataSetCount();
        int trackedEntityInstanceCount = SyncStatusHelper.trackedEntityInstanceCount();
        int singleEventCount = SyncStatusHelper.singleEventCount();
        int dataValueCount = SyncStatusHelper.dataValueCount();

        enablePossibleButtons(programCount + dataSetCount > 0);

    /*    TextView downloadedProgramsText = findViewById(R.id.programsDownloadedText);
        TextView downloadedDataSetsText = findViewById(R.id.dataSetsDownloadedText);
        TextView downloadedTeisText = findViewById(R.id.trackedEntityInstancesDownloadedText);
        TextView singleEventsDownloadedText = findViewById(R.id.singleEventsDownloadedText);
        TextView downloadedDataValuesText = findViewById(R.id.dataValuesDownloadedText);
        downloadedProgramsText.setText(MessageFormat.format("{0}", programCount));
        downloadedDataSetsText.setText(MessageFormat.format("{0}", dataSetCount));
        downloadedTeisText.setText(MessageFormat.format("{0}", trackedEntityInstanceCount));
        singleEventsDownloadedText.setText(MessageFormat.format("{0}", singleEventCount));
        downloadedDataValuesText.setText(MessageFormat.format("{0}", dataValueCount));*/
    }

    private void createNavigationView(User user) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navView);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        TextView firstName = headerView.findViewById(R.id.firstName);
        TextView email = headerView.findViewById(R.id.email);
        firstName.setText(user.firstName());
        email.setText(user.email());

    }

    private void syncMetadata() {
        compositeDisposable.add(downloadMetadata()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(Throwable::printStackTrace)
                .doOnComplete(this::setSyncingFinished)
                .subscribe());
    }

    private Observable<D2Progress> downloadMetadata() {
        return Sdk.d2().metadataModule().download();
    }

    private void downloadData() {
        compositeDisposable.add(
                Observable.merge(
                                downloadTrackedEntityInstances(),
                                downloadSingleEvents(),
                                downloadAggregatedData()
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete(this::setSyncingFinished)
                        .doOnError(Throwable::printStackTrace)
                        .subscribe());
    }

    private Observable<TrackerD2Progress> downloadTrackedEntityInstances() {
        return Sdk.d2().trackedEntityModule().trackedEntityInstanceDownloader()
                .limit(10).limitByOrgunit(false).limitByProgram(false).download();
    }

    private Observable<TrackerD2Progress> downloadSingleEvents() {
        return Sdk.d2().eventModule().eventDownloader()
                .limit(10).limitByOrgunit(false).limitByProgram(false).download();
    }

    private Observable<AggregatedD2Progress> downloadAggregatedData() {
        return Sdk.d2().aggregatedModule().data().download();
    }

    private void uploadData() {
        compositeDisposable.add(
                Sdk.d2().trackedEntityModule().trackedEntityInstances().upload()
                        .concatWith(Sdk.d2().dataValueModule().dataValues().upload())
                        .concatWith(Sdk.d2().eventModule().events().upload())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete(this::setSyncingFinished)
                        .doOnError(Throwable::printStackTrace)
                        .subscribe());
    }

    private void wipeData() {
        compositeDisposable.add(
                Observable
                        .fromCallable(() -> {
                            Sdk.d2().wipeModule().wipeData();
                            return "Done wipeData";
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(Throwable::printStackTrace)
                        .doOnComplete(this::setSyncingFinished)
                        .subscribe());
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            loadFragment(new ProgramsFragment());
        } else if (id == R.id.nav_cases) {
            loadFragment(new EventsFragment());
        } else if (id == R.id.nav_logout) {
            compositeDisposable.add(LogOutService.logOut(this));
        }

        DrawerLayout drawer = findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
