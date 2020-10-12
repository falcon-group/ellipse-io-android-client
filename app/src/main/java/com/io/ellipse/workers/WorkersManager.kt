package com.io.ellipse.workers

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WorkersManager @Inject constructor(@ApplicationContext context: Context) {

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

}