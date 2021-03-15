package com.io.ellipse.workers

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkersManager @Inject constructor(@ApplicationContext context: Context) {

    companion object {
        private const val DURATION_IN_DAYS = 2L
        private const val PERIODICAL_WORK = "periodical_work"
    }

    private val manager = WorkManager.getInstance(context)

    fun startSynchronizingWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
        manager.enqueue(syncWorkRequest)
    }

    fun startSynchronizingParamWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncWorkRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<SyncWorker>(DURATION_IN_DAYS, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()
        manager.enqueueUniquePeriodicWork(
            PERIODICAL_WORK,
            ExistingPeriodicWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }

    fun cancelPeriodicalWork() {
        manager.cancelUniqueWork(PERIODICAL_WORK)
    }
}