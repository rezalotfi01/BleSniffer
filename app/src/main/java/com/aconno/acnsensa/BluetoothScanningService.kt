package com.aconno.acnsensa

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import com.aconno.acnsensa.dagger.BluetoothScanningServiceComponent
import com.aconno.acnsensa.dagger.BluetoothScanningServiceModule
import com.aconno.acnsensa.dagger.DaggerBluetoothScanningServiceComponent
import com.aconno.acnsensa.domain.Bluetooth
import javax.inject.Inject

/**
 * @author aconno
 */
class BluetoothScanningService : Service() {

    @Inject
    lateinit var bluetooth: Bluetooth

    //@Inject
    lateinit var receiver: BroadcastReceiver

    @Inject
    lateinit var filter: IntentFilter

    //@Inject
    lateinit var notification: Notification

    val bluetoothScanningServiceComponent: BluetoothScanningServiceComponent by lazy {
        val acnSensaApplication: AcnSensaApplication? = application as? AcnSensaApplication
        DaggerBluetoothScanningServiceComponent.builder()
            .appComponent(acnSensaApplication?.appComponent)
            .bluetoothScanningServiceModule(BluetoothScanningServiceModule(this))
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        bluetoothScanningServiceComponent.inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.registerReceiver(receiver, filter)

        startForeground(1, notification)

        bluetooth.startScanning()
        return START_STICKY
    }

    fun stopScanning() {
        bluetooth.stopScanning()
        stopSelf()
    }

    companion object {

        fun start(context: Context) {
            createNotificationsChannel(context)
            val intent = Intent(context, BluetoothScanningService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}