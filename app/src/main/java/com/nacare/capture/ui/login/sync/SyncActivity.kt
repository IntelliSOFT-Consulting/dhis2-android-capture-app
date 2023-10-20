package com.nacare.capture.ui.login.sync

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.android.androidskeletonapp.R
import com.nacare.capture.data.Sdk
import com.nacare.capture.data.service.ActivityStarter
import com.nacare.capture.ui.main.MainActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.hisp.dhis.android.core.arch.call.D2Progress
import org.hisp.dhis.android.core.domain.aggregated.data.AggregatedD2Progress
import org.hisp.dhis.android.core.tracker.exporter.TrackerD2Progress
import org.hisp.dhis.android.core.user.User

class SyncActivity : AppCompatActivity() {
    private var isSyncing = false
    private var compositeDisposable: CompositeDisposable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync)
        compositeDisposable = CompositeDisposable()

        val user = getUser()
        if (user != null) {
            Log.e("TAG", "Logged In User ${user.displayName()}")
            downloadData()
        }

    }

    private fun getUser(): User? {
        return Sdk.d2().userModule().user().blockingGet()
    }

    private fun syncMetadata() {
        compositeDisposable!!.add(downloadMetadata()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { obj: Throwable -> obj.printStackTrace() }
            .doOnComplete { this.setSyncingFinished() }
            .subscribe())
    }

    private fun downloadMetadata(): Observable<D2Progress?> {
        return Sdk.d2().metadataModule().download()
    }

    private fun downloadData() {
        compositeDisposable!!.add(
            Observable.merge<D2Progress>(
                downloadTrackedEntityInstances(),
                downloadSingleEvents(),
                downloadAggregatedData()

            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete { this.syncMetadata() }
                .doOnError { obj: Throwable -> obj.printStackTrace() }
                .subscribe())
    }

    private fun downloadTrackedEntityInstances(): Observable<TrackerD2Progress>? {
        return Sdk.d2().trackedEntityModule().trackedEntityInstanceDownloader()
            .limit(100).limitByOrgunit(false).limitByProgram(false).download()
    }

    private fun downloadSingleEvents(): Observable<TrackerD2Progress>? {
        return Sdk.d2().eventModule().eventDownloader()
            .limit(100).limitByOrgunit(false).limitByProgram(false).download()
    }

    private fun downloadAggregatedData(): Observable<AggregatedD2Progress>? {
        return Sdk.d2().aggregatedModule().data().download()
    }

    private fun uploadData() {
        compositeDisposable!!.add(
            Sdk.d2().trackedEntityModule().trackedEntityInstances().upload()
                .concatWith(Sdk.d2().dataValueModule().dataValues().upload())
                .concatWith(Sdk.d2().eventModule().events().upload())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete { this.setSyncingFinished() }
                .doOnError { obj: Throwable -> obj.printStackTrace() }
                .subscribe())
    }

    private fun setSyncingFinished() {
        isSyncing = false
        ActivityStarter.startActivity(this, MainActivity.getMainActivityIntent(this), true)
    }

    companion object {
        @JvmStatic
        fun getMainActivityIntent(context: Context?): Intent {
            return Intent(context, SyncActivity::class.java)
        }


    }

}