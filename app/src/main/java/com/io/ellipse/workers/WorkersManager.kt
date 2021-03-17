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

    fun startSynchronizingParamWork(delay: Long = 0, unit: TimeUnit = TimeUnit.MILLISECONDS) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncWorkRequest: OneTimeWorkRequest = OneTimeWorkRequestBuilder<SyncParamsWorker>()
                .setInitialDelay(delay, unit)
                .setConstraints(constraints)
                .build()
        manager.enqueueUniqueWork(PERIODICAL_WORK, ExistingWorkPolicy.REPLACE, syncWorkRequest)
    }

    fun cancelPeriodicalWork() {
        manager.cancelUniqueWork(PERIODICAL_WORK)
    }
}