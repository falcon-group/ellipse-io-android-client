package com.io.ellipse.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import com.io.ellipse.R
import com.io.ellipse.common.android.WindowsManagerGestureListener
import com.io.ellipse.common.android.changeState
import com.io.ellipse.common.android.createDefaultParams
import com.io.ellipse.common.android.vibrate
import com.io.ellipse.data.bluetooth.device.core.SupporterFacade
import com.io.ellipse.data.bluetooth.gatt.exceptions.GattConnectionException
import com.io.ellipse.data.bluetooth.state.ErrorState
import com.io.ellipse.data.bluetooth.state.HeartRateState
import com.io.ellipse.data.bluetooth.state.ReceiverState
import com.io.ellipse.data.network.socket.NetworkSocketManager
import com.io.ellipse.data.network.utils.isConnected
import com.io.ellipse.data.notification.AppNotificationManager
import com.io.ellipse.data.persistence.database.dao.ParamsDao
import com.io.ellipse.data.persistence.database.entity.tracker.ParamsData
import com.io.ellipse.data.persistence.preferences.proto.auth.AuthPreferences
import com.io.ellipse.data.utils.AppOverlayManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class RecieverService : Service(), CompoundButton.OnCheckedChangeListener {

    companion object {
        private const val STATUS_NOTIFICATION_ID = 101

        private const val FLAG_START_SERVICE = 0x001
        private const val FLAG_STOP_SERVICE = 0x002
        private const val FLAG_IS_URGENT = 0x003

        private const val TIMEOUT = 24 * 60L * 1000L * 1000L

        private const val KEY_FLAG = "service.flag"
        private const val KEY_DEVICE = "service.device"
        private const val KEY_IS_URGENT = "service.urgent"


        fun connect(context: Context, device: BluetoothDevice) = with(context) {
            val data = Intent(this, RecieverService::class.java)
            data.putExtra(KEY_DEVICE, device)
            data.putExtra(KEY_FLAG, FLAG_START_SERVICE)
            ContextCompat.startForegroundService(context, data)
        }

        fun changeUrgentFlag(context: Context, isUrgent: Boolean): Intent {
            val intent = Intent(context, RecieverService::class.java)
            intent.putExtra(KEY_FLAG, FLAG_IS_URGENT)
            intent.putExtra(KEY_IS_URGENT, isUrgent)
            return intent
        }

        fun disconnect(context: Context) = with(context) {
            val data = Intent(this, RecieverService::class.java)
            data.putExtra(KEY_FLAG, FLAG_STOP_SERVICE)
            context.startService(data)
        }
    }

    @Inject
    lateinit var appNotificationManager: AppNotificationManager

    @Inject
    lateinit var supporterFacade: SupporterFacade

    @Inject
    lateinit var networkSocketManager: NetworkSocketManager

    @Inject
    lateinit var appOverlayManager: AppOverlayManager

    @Inject
    lateinit var authPreferences: AuthPreferences

    @Inject
    lateinit var paramsDao: ParamsDao

    private var isShown: Boolean = false

    private val isUrgentState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val rootJob: Job = SupervisorJob()

    private var connectionJob: Job? = null

    private var wakeLock: PowerManager.WakeLock? = null

    private var view: ToggleButton? = null

    override fun onCreate() {
        super.onCreate()
        val notification = appNotificationManager.createNotification()
            .setContentTitle(getString(R.string.app_name))
            .setSubText(getString(R.string.message_connection_started))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(0, getString(android.R.string.cancel), createPendingIntent())
            .build()
        startForeground(STATUS_NOTIFICATION_ID, notification)
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, packageName)
    }

    @SuppressLint("InvalidWakeLockTag")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val flag = intent?.getIntExtra(KEY_FLAG, FLAG_STOP_SERVICE) ?: FLAG_STOP_SERVICE
        when (flag) {
            FLAG_START_SERVICE -> {
                showIfNeeded()
                supporterFacade.disconnect()
                connectionJob?.cancel()
                connectionJob = Job(parent = rootJob)
                wakeLock?.takeIf { it.isHeld }?.release()
                wakeLock?.acquire(TIMEOUT)
                val device: BluetoothDevice? = intent
                    ?.getParcelableExtra(KEY_DEVICE)
                    ?: return START_STICKY
                GlobalScope.launch(Dispatchers.Main + connectionJob!!) {
                    device?.let { connectInternally(it) }
                }
            }
            FLAG_STOP_SERVICE -> {
                hideIfNeeded()
                wakeLock?.takeIf { it.isHeld }?.release()
                GlobalScope.launch(Dispatchers.IO) {
                    rootJob.cancelAndJoin()
                    supporterFacade.disconnect()
                    networkSocketManager.disconnect()
                    stopForeground(true)
                    stopSelf()
                }
            }
            FLAG_IS_URGENT -> {
                isUrgentState.value = !isUrgentState.value
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        hideIfNeeded()
        wakeLock?.takeIf { it.isHeld }?.release()
        GlobalScope.launch(Dispatchers.IO) {
            supporterFacade.disconnect()
            networkSocketManager.disconnect()
        }
        rootJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        isUrgentState.value = isChecked
        buttonView?.vibrate()
    }

    private suspend fun connectInternally(device: BluetoothDevice) = channelFlow<ReceiverState> {
        supporterFacade.connectAndReceive(device) { send(HeartRateState(it)) }
        awaitClose { }
    }.flowOn(Dispatchers.IO).retry {
        it is GattConnectionException
    }.onStart {
        val token = authPreferences.data.first().authorizationToken
        networkSocketManager.disconnect()
        networkSocketManager.connect(token)
    }.flowOn(Dispatchers.IO).catch {
        emit(ErrorState(it))
    }.combine(isUrgentState) { state, isUrgent ->
        state to isUrgent
    }.collect { (state, isUrgent) ->
        when (state) {
            is ErrorState -> proceedError(device, state.throwable)
            is HeartRateState -> sendData(state.heartRate, isUrgent)
        }
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, RecieverService::class.java)
            .putExtra(KEY_FLAG, FLAG_STOP_SERVICE)
        return PendingIntent.getService(
            this,
            STATUS_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showIfNeeded() {
        if (!appOverlayManager.canDrawOverlays || isShown) {
            return
        }
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(this)
        val layout = inflater.inflate(R.layout.layout_overlay, null)
        val view: ToggleButton = layout.findViewById(R.id.urgentButton)

        val params = with(resources) {
            val width = getDimensionPixelOffset(R.dimen.large_button_height)
            val height = getDimensionPixelOffset(R.dimen.large_button_height)
            wm.createDefaultParams(width, height)
        }
        view.setOnCheckedChangeListener(this)
        view.setOnTouchListener(WindowsManagerGestureListener(wm, view, params))
        wm.addView(layout, params)
        GlobalScope.launch(Dispatchers.Main + rootJob) {
            isUrgentState.collect { view.changeState(it, this@RecieverService) }
        }
        this.view = view
        isShown = true
    }

    private fun hideIfNeeded() {
        if (!isShown) {
            return
        }
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        try {
            view?.let { wm.removeViewImmediate(it) }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    private fun proceedError(device: BluetoothDevice, throwable: Throwable) {
        val notification = appNotificationManager.createNotification()
            .setContentTitle(getString(R.string.app_name))
            .setOngoing(false)
            .setSubText(getString(R.string.placeholder_error_while_connecting, device.address))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(0, getString(android.R.string.cancel), createPendingIntent())
            .build()
        appNotificationManager.showNotification(STATUS_NOTIFICATION_ID, notification)
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    private suspend fun sendData(heartRate: Int, isUrgent: Boolean) = withContext(Dispatchers.IO) {
        try {
            if (isConnected) {
                networkSocketManager.send(heartRate, isUrgent)
            } else {
                paramsDao.create(ParamsData(heartRate = heartRate, isUrgent = isUrgent))
            }
        } catch (ex: Exception) {
            paramsDao.create(ParamsData(heartRate = heartRate, isUrgent = isUrgent))
        }
    }

}
