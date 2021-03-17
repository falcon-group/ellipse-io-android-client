package com.io.ellipse.workers

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.io.ellipse.data.map
import com.io.ellipse.data.network.http.rest.services.ParamsService
import com.io.ellipse.data.persistence.database.dao.ParamsDao
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class SyncParamsWorker @WorkerInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val paramsService: ParamsService,
    private val paramsDao: ParamsDao,
    private val workersManager: WorkersManager
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val result =  try {
            paramsDao.retrieveAll().first()
                .map { map(it) }
                .also { paramsService.create(it) }
            paramsDao.deleteAll()
            Result.success()
        } catch (ex: Exception) {
            Result.failure()
        }
        workersManager.startSynchronizingParamWork(2L, TimeUnit.HOURS)
        return result
    }
}