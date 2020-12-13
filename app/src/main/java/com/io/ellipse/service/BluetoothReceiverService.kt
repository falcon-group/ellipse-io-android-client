package com.io.ellipse.service

import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.io.ellipse.R
import com.io.ellipse.data.bluetooth.connection.*
import com.io.ellipse.data.bluetooth.state.BluetoothStateManager
import com.io.ellipse.data.network.http.JSON_SERIALIZER
import com.io.ellipse.data.network.socket.ClosedState
import com.io.ellipse.data.network.socket.FailureState
import com.io.ellipse.data.network.socket.IdleState
import com.io.ellipse.data.network.socket.NetworkSocketManager
import com.io.ellipse.data.notification.AppNotificationManager
import com.io.ellipse.data.persistence.database.entity.tracker.TrackerData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BluetoothReceiverService : Service() {

    companion object {

        private const val STATUS_NOTIFICATION_ID = 101

        private const val FLAG_START_SERVICE = 0x001
        private const val FLAG_STOP_SERVICE = 0x002
        private const val FLAG_IS_URGENT = 0x003

        private const val KEY_FLAG = "service.flag"
        private const val KEY_DEVICE = "connection.device"
        private const val KEY_IS_URGENT = "service.urgent"

        private const val REQUEST_CODE = 312

        fun connect(context: Context, device: BluetoothDevice) = with(context) {
            val data = Intent(this, BluetoothReceiverService::class.java)
            data.putExtra(KEY_DEVICE, device)
            data.putExtra(KEY_FLAG, FLAG_START_SERVICE)
            ContextCompat.startForegroundService(context, data)
        }

        fun changeUrgentFlag(context: Context, isUrgent: Boolean): Intent {
            val intent = Intent(context, BluetoothReceiverService::class.java)
            intent.putExtra(KEY_FLAG, FLAG_IS_URGENT)
            intent.putExtra(KEY_IS_URGENT, isUrgent)
            return intent
        }

        fun disconnect(context: Context) = with(context) {
            val data = Intent(this, BluetoothReceiverService::class.java)
            data.putExtra(KEY_FLAG, FLAG_STOP_SERVICE)
            context.startService(data)
        }
    }

    @Inject
    lateinit var appNotificationManager: AppNotificationManager

    @Inject
    lateinit var bluetoothConnectionManager: BluetoothConnectionManager

    @Inject
    lateinit var bluetoothStateManager: BluetoothStateManager

    @Inject
    lateinit var networkSocketManager: NetworkSocketManager

    private val isUrgent: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private var connectionJob: Job? = null

    private var trackerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val flag = intent?.getIntExtra(KEY_FLAG, FLAG_STOP_SERVICE) ?: FLAG_STOP_SERVICE
        when (flag) {
            FLAG_START_SERVICE -> {
                val device: BluetoothDevice = intent?.getParcelableExtra(KEY_DEVICE)
                    ?: return super.onStartCommand(intent, flags, startId).also { stopSelf() }
                connectionJob?.cancel()
                connectionJob = device.also {
                    val notification = appNotificationManager.createNotification()
                        .setContentTitle(getString(R.string.app_name))
                        .setSubText("Attempt to connect to ${it.address}")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .build()
                    startForeground(STATUS_NOTIFICATION_ID, notification)
                }.let {
                    connectInternally(it)
                }
                trackerJob?.cancel()
                trackerJob = startTrackingJob()
            }
            FLAG_IS_URGENT -> {
                isUrgent.value = intent?.getBooleanExtra(KEY_IS_URGENT, false) ?: false
            }
            FLAG_STOP_SERVICE -> {
                stopForeground(true)
                stopSelf()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun connectInternally(
        device: BluetoothDevice
    ) = GlobalScope.launch(Dispatchers.Main) {
        val isEnabled = bluetoothStateManager.isBluetoothEnabled
        if (isEnabled) {
            withContext(Dispatchers.IO) {
                bluetoothConnectionManager.connect(device)
            }
            bluetoothConnectionManager.connectionState
                .flowOn(Dispatchers.IO)
                .combine(isUrgent) { state, isUrgent ->
                    state to isUrgent
                }
                .collect { (state, isUrgent) -> proceedBlutoothDeviceState(state, isUrgent) }
        }
    }

    private fun startTrackingJob() = GlobalScope.launch(Dispatchers.IO) {
        networkSocketManager.state.map {
            when (it) {
                is IdleState, is FailureState, is ClosedState -> {
                    networkSocketManager.connect()
                    false
                }
                else -> {
                    true
                }
            }
        }.distinctUntilChanged().flatMapLatest {
            when (it) {
                true -> bluetoothConnectionManager.data
                else -> flow { }
            }
        }.combine(isUrgent) { data, urgent ->
            TrackerData(data.heartRate, urgent)
        }.catch {
            Timber.e(it)
        }.collect {
            val stringData: String = JSON_SERIALIZER.toJson(it)
            networkSocketManager.send(stringData)
        }
    }

    private fun proceedBlutoothDeviceState(connectionState: BluetoothState, isUrgent: Boolean) {
        val notification = when (connectionState) {
            is Connecting -> appNotificationManager.createNotification()
                .setContentTitle(getString(R.string.app_name))
                .setSubText(getString(R.string.placeholder_connecting, connectionState.device.address))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
            is Connected -> appNotificationManager.createNotification()
                .setContentTitle(getString(R.string.app_name))
                .setSubText(getString(R.string.placeholder_connected, connectionState.device.address))
                .also {
                    val action = if (isUrgent) {
                        NotificationCompat.Action(
                            null,
                            getString(R.string.title_stop_urgent),
                            PendingIntent.getService(
                                this,
                                REQUEST_CODE,
                                changeUrgentFlag(this, false),
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                        )
                    } else {
                        NotificationCompat.Action(
                            null,
                            getString(R.string.title_start_urgent),
                            PendingIntent.getService(
                                this,
                                REQUEST_CODE,
                                changeUrgentFlag(this, true),
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                        )
                    }
                    it.addAction(action)
                }
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
            is Disconnecting -> appNotificationManager.createNotification()
                .setContentTitle(getString(R.string.app_name))
                .setSubText(getString(R.string.placeholder_disconnecting, connectionState.device.address))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
            is Disconnected -> appNotificationManager.createNotification()
                .setContentTitle(getString(R.string.app_name))
                .setSubText(getString(R.string.placeholder_disconnected, connectionState.device.address))
                .setSubText("Disconnected to ${connectionState.device.address}")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
            is ErrorState -> appNotificationManager.createNotification()
                .setContentTitle(getString(R.string.app_name))
                .setSubText(getString(R.string.placeholder_error_while_connecting, connectionState.device.address))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
            else -> null
        }
        notification?.let { appNotificationManager.showNotification(STATUS_NOTIFICATION_ID, it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionJob?.cancel()
        GlobalScope.launch(Dispatchers.IO) { bluetoothConnectionManager.disconnect() }
    }
}