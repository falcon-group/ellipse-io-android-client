package com.io.ellipse.service

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.io.ellipse.R
import com.io.ellipse.data.bluetooth.connection.*
import com.io.ellipse.data.bluetooth.state.BluetoothStateManager
import com.io.ellipse.data.notification.AppNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@AndroidEntryPoint
class BluetoothReceiverService : Service() {

    companion object {

        private const val STATUS_NOTIFICATION_ID = 101

        private const val FLAG_START_SERVICE = 0x001
        private const val FLAG_STOP_SERVICE = 0x002

        private const val KEY_FLAG = "service.flag"
        private const val KEY_DEVICE = "connection.device"

        fun connect(context: Context, device: BluetoothDevice) = with(context) {
            val data = Intent(this, BluetoothReceiverService::class.java)
            data.putExtra(KEY_DEVICE, device)
            data.putExtra(KEY_FLAG, FLAG_START_SERVICE)
            ContextCompat.startForegroundService(context, data)
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
    lateinit var bluetoothBluetoothConnectionManager: BluetoothConnectionManager

    @Inject
    lateinit var bluetoothStateManager: BluetoothStateManager

    private var connectionJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val flag = intent?.getIntExtra(KEY_FLAG, FLAG_STOP_SERVICE) ?: FLAG_STOP_SERVICE
        when (flag) {
            FLAG_START_SERVICE -> {
                val device: BluetoothDevice? = intent?.getParcelableExtra(KEY_DEVICE)
                connectionJob = device?.also {
                    val notification = appNotificationManager.createNotification()
                        .setContentTitle(getString(R.string.app_name))
                        .setSubText("Attempt to connect to ${it.address}")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .build()
                    startForeground(STATUS_NOTIFICATION_ID, notification)
                }
                    ?.let { connectInternally(it) }
                    ?: return super.onStartCommand(intent, flags, startId).also { stopSelf() }
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
                bluetoothBluetoothConnectionManager.connect(device)
            }
            bluetoothBluetoothConnectionManager.connectionState
                .flowOn(Dispatchers.IO)
                .collect { proceedState(it) }
        }
    }

    private fun proceedState(connectionState: BluetoothState) {
        val notification = when (connectionState) {
            is Connecting -> appNotificationManager.createNotification()
                .setContentTitle(getString(R.string.app_name))
                .setSubText("Connecting to ${connectionState.device.address}")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
            is Connected -> appNotificationManager.createNotification()
                .setContentTitle(getString(R.string.app_name))
                .setSubText("Connected to ${connectionState.device.address}")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
            is Disconnecting -> appNotificationManager.createNotification()
                .setContentTitle(getString(R.string.app_name))
                .setSubText("Disconnecting to ${connectionState.device.address}")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
            is Disconnected -> appNotificationManager.createNotification()
                .setContentTitle(getString(R.string.app_name))
                .setSubText("Disconnected to ${connectionState.device.address}")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
            is ErrorState -> appNotificationManager.createNotification()
                .setContentTitle(getString(R.string.app_name))
                .setSubText("Error while connecting to ${connectionState.device.address}")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
            else -> null
        }
        notification?.let { appNotificationManager.showNotification(STATUS_NOTIFICATION_ID, it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        GlobalScope.launch(Dispatchers.IO) { bluetoothBluetoothConnectionManager.disconnect() }
    }
}