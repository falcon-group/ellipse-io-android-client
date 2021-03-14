package com.io.ellipse.service

import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.*
import android.widget.CompoundButton
import android.widget.ToggleButton
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.io.ellipse.R
import com.io.ellipse.data.bluetooth.connection.*
import com.io.ellipse.data.bluetooth.state.BluetoothStateManager
import com.io.ellipse.data.network.socket.*
import com.io.ellipse.data.notification.AppNotificationManager
import com.io.ellipse.data.persistence.database.entity.tracker.TrackerData
import com.io.ellipse.data.persistence.preferences.proto.auth.AuthPreferences
import com.io.ellipse.data.utils.AppOverlayManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


@AndroidEntryPoint
class BluetoothReceiverService : Service(), CompoundButton.OnCheckedChangeListener, CoroutineScope {

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

    @Inject
    lateinit var appOverlayManager: AppOverlayManager

    @Inject
    lateinit var authPreferences: AuthPreferences

    private val isUrgent: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val rootJob: Job = SupervisorJob()

    private var connectionJob: Job? = null

    private var trackerJob: Job? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + rootJob + CoroutineExceptionHandler { coroutineContext, throwable ->
            Timber.e(throwable)
        }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val notification = appNotificationManager.createNotification()
            .setContentTitle(getString(R.string.app_name))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        startForeground(STATUS_NOTIFICATION_ID, notification)
        drawView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val flag = intent?.getIntExtra(KEY_FLAG, FLAG_STOP_SERVICE) ?: FLAG_STOP_SERVICE
        when (flag) {
            FLAG_START_SERVICE -> {
                val device: BluetoothDevice = intent?.getParcelableExtra(KEY_DEVICE)
                    ?: return super.onStartCommand(intent, flags, startId).also { stopSelf() }
                device.also {
                    val notification = appNotificationManager.createNotification()
                        .setContentTitle(getString(R.string.app_name))
                        .setSubText("Attempt to connect ${it.address}")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .build()
                    appNotificationManager.showNotification(STATUS_NOTIFICATION_ID, notification)
                }.let {
                    connectionJob?.cancel()
                    connectionJob = Job(rootJob)
                    launch(coroutineContext + connectionJob!!) {
                        connectInternally(it)
                    }
                }
                startTrackingJob()
            }
            FLAG_IS_URGENT -> {
                isUrgent.value = intent?.getBooleanExtra(KEY_IS_URGENT, false) ?: false
            }
            FLAG_STOP_SERVICE -> {
                stopForeground(true)
                launch {
                    bluetoothConnectionManager.disconnect()
                    networkSocketManager.disconnect()
                }
                rootJob.cancel()
                stopSelf()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private suspend fun connectInternally(
        device: BluetoothDevice
    ) {
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

    private fun startTrackingJob() {
        trackerJob?.cancel()
        trackerJob = Job(rootJob)
        launch(coroutineContext + trackerJob!!) {
            val token = authPreferences.data.first().authorizationToken
            networkSocketManager.disconnect()
            networkSocketManager.connect(token)
            networkSocketManager.state.map {
                when (it) {
                    is OpenedState -> true
                    else -> false
                }
            }.distinctUntilChanged().flatMapLatest {
                when (it) {
                    true -> bluetoothConnectionManager.data
                    else -> emptyFlow()
                }
            }.flowOn(Dispatchers.IO).combine(isUrgent) { data, urgent ->
                TrackerData(data.heartRate, urgent)
            }.catch {
                Timber.e(it)
            }.sample(5000).collect {
                networkSocketManager.send(it.heartRate)
            }
        }
    }

    private fun proceedBlutoothDeviceState(connectionState: BluetoothState, isUrgent: Boolean) {
        val notification = when (connectionState) {
            is Connecting -> appNotificationManager.createNotification()
                .setContentTitle(getString(R.string.app_name))
                .setSubText(
                    getString(
                        R.string.placeholder_connecting,
                        connectionState.device.address
                    )
                )
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .build()
            is Connected -> appNotificationManager.createNotification()
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true)
                .setSubText(
                    getString(
                        R.string.placeholder_connected,
                        connectionState.device.address
                    )
                )
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
                .setOngoing(true)
                .setSubText(
                    getString(
                        R.string.placeholder_disconnecting,
                        connectionState.device.address
                    )
                )
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
            is Disconnected -> appNotificationManager.createNotification()
                .setContentTitle(getString(R.string.app_name))
                .setSubText(
                    getString(
                        R.string.placeholder_disconnected,
                        connectionState.device.address
                    )
                )
                .setOngoing(false)
                .setSubText("Disconnected to ${connectionState.device.address}")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
            is ErrorState -> appNotificationManager.createNotification()
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(false)
                .setSubText(
                    getString(
                        R.string.placeholder_error_while_connecting,
                        connectionState.device.address
                    )
                )
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
            else -> null
        }
        notification?.let { appNotificationManager.showNotification(STATUS_NOTIFICATION_ID, it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        launch {
            bluetoothConnectionManager.disconnect()
            networkSocketManager.disconnect()
        }
        rootJob.cancel()
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        vibrate()
        isUrgent.value = isChecked
    }

    private fun drawView() {
        if (!appOverlayManager.canDrawOverlays) {
            return
        }
        val inflater = LayoutInflater.from(this)
        val layout = inflater.inflate(R.layout.layout_overlay, null)
        val view: ToggleButton = layout.findViewById(R.id.urgentButton)
        Timber.e("$layout")
        Timber.e("$view")
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        view.setOnCheckedChangeListener(this)
        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        flags = flags or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        flags = flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

        val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                flags,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                flags,
                PixelFormat.TRANSLUCENT
            )
        }
        params.width = resources.getDimensionPixelOffset(R.dimen.large_button_height)
        params.height = resources.getDimensionPixelOffset(R.dimen.large_button_height)
        params.gravity = Gravity.NO_GRAVITY
        var (dx, dy) = (0f to 0f)
        val gestureDetector = object : GestureDetector.OnGestureListener {

            override fun onDown(e: MotionEvent?): Boolean = true

            override fun onShowPress(e: MotionEvent?) = Unit

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                view.toggle()
                return false
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean = false

            override fun onLongPress(e: MotionEvent?) = Unit

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean = false
        }

        val detector = GestureDetector(this, gestureDetector)
        val listener: (View, MotionEvent) -> Boolean = { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    wm.updateViewLayout(layout, params)
                    dx = params.x - event.rawX
                    dy = params.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = (event.rawX + dx).toInt()
                    params.y = (event.rawY + dy).toInt()
                    wm.updateViewLayout(layout, params)
                }
                MotionEvent.ACTION_UP -> {
                    params.x = (event.rawX + dx).toInt()
                    params.y = (event.rawY + dy).toInt()
                    wm.updateViewLayout(layout, params)
                }
            }
            true
        }
        view.setOnTouchListener { v, event ->
            detector.onTouchEvent(event) or listener(v, event)
        }
        wm.addView(layout, params)
        launch(Dispatchers.Main + rootJob) {
            isUrgent.collect { view.changeState(it) }
        }
    }

    private fun vibrate() {
        val vibrator: Vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    150,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(150)
        }
    }

    private fun ToggleButton.changeState(state: Boolean) {
        setOnCheckedChangeListener(null)
        isChecked = state
        setOnCheckedChangeListener(this@BluetoothReceiverService)
    }
}