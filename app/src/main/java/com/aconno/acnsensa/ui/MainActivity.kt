package com.aconno.acnsensa.ui

import android.arch.lifecycle.Observer
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.aconno.acnsensa.AcnSensaApplication
import com.aconno.acnsensa.BluetoothScanningService
import com.aconno.acnsensa.R
import com.aconno.acnsensa.dagger.mainactivity.DaggerMainActivityComponent
import com.aconno.acnsensa.dagger.mainactivity.MainActivityComponent
import com.aconno.acnsensa.dagger.mainactivity.MainActivityModule
import com.aconno.acnsensa.device.permissons.PermissionActionFactory
import com.aconno.acnsensa.domain.model.ScanEvent
import com.aconno.acnsensa.viewmodel.BluetoothScanningViewModel
import com.aconno.acnsensa.viewmodel.PermissionViewModel
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity(), PermissionViewModel.PermissionCallbacks {

    @Inject
    lateinit var bluetoothScanningViewModel: BluetoothScanningViewModel

    private var mainMenu: Menu? = null

    private lateinit var permissionViewModel: PermissionViewModel

    val mainActivityComponent: MainActivityComponent by lazy {
        val acnSensaApplication: AcnSensaApplication? = application as? AcnSensaApplication
        DaggerMainActivityComponent.builder()
            .appComponent(acnSensaApplication?.appComponent)
            .mainActivityModule(MainActivityModule(this))
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissionAction = PermissionActionFactory.getPermissionAction(this)
        permissionViewModel =
                PermissionViewModel(permissionAction, this)

        mainActivityComponent.inject(this)
        custom_toolbar.title = getString(R.string.app_name)
        setSupportActionBar(custom_toolbar)

        invalidateOptionsMenu()

        if (savedInstanceState == null) {
            addFragment()
        }
    }

    override fun onResume() {
        super.onResume()

        bluetoothScanningViewModel.getResult().observe(this, Observer { handleScanEvent(it) })

        if (!isBtEnabled()) {
            Timber.e("BT enabled")
            Snackbar.make(activity_container, R.string.bt_disabled, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.enable) { enableBt() }
                .show()
        }
    }

    private fun handleScanEvent(scanEvent: ScanEvent?) {
        Timber.d("Handle scan event ${scanEvent?.message}")
        val eventType: Int? = scanEvent?.type
        when (eventType) {
            ScanEvent.SCAN_FAILED_ALREADY_STARTED -> onScanFailedAlreadyStarted()
            ScanEvent.SCAN_FAILED -> onScanFailed()
            ScanEvent.SCAN_START -> onScanStart()
            ScanEvent.SCAN_STOP -> onScanStop()
        }
    }

    private fun onScanFailedAlreadyStarted() {
        //Do nothing.
    }

    private fun onScanFailed() {
        onScanStop()
    }

    private fun onScanStart() {
        mainMenu?.let {
            val menuItem: MenuItem? = it.findItem(R.id.action_toggle_scan)
            menuItem?.let {
                it.isChecked = true
                it.setTitle(getString(R.string.stop_scan))
            }
        }
    }

    private fun onScanStop() {
        mainMenu?.let {
            val menuItem: MenuItem? = it.findItem(R.id.action_toggle_scan)
            menuItem?.let {
                it.isChecked = false
                it.setTitle(getString(R.string.start_scan))
            }
        }
    }

    private fun addFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(activity_container.id, SensorListFragment())
        transaction.commit()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        mainMenu = menu
        mainMenu?.clear()
        menuInflater.inflate(R.menu.main_menu, menu)

        mainMenu?.findItem(R.id.action_toggle_scan)?.let {
            setScanMenuLabel(it)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id: Int? = item?.itemId
        when (id) {
            R.id.action_toggle_scan -> toggleScan(item)
            R.id.action_start_actions_activity -> startActionListActivity()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun startActionListActivity() {
        ActionListActivity.start(this)
    }

    private fun toggleScan(item: MenuItem?) {
        item?.let {
            if (item.isChecked) {
                bluetoothScanningViewModel.stopScanning()
            } else {
                permissionViewModel.requestAccessFineLocation()
            }
        }
    }

    private fun setScanMenuLabel(menuItem: MenuItem) {
        if (BluetoothScanningService.isRunning()) {
            menuItem.title = getString(R.string.stop_scan)
            menuItem.isChecked = true
        } else {
            menuItem.title = getString(R.string.start_scan)
            menuItem.isChecked = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionViewModel.checkGrantedPermission(grantResults, requestCode)
    }

    private fun isBtEnabled(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter?.isEnabled ?: false
    }

    private fun enableBt() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter.enable()
    }

    override fun permissionAccepted(actionCode: Int) {
        bluetoothScanningViewModel.startScanning()
        //TODO: Permission accepted
    }

    override fun permissionDenied(actionCode: Int) {
        //TODO: Permission denied
    }

    override fun showRationale(actionCode: Int) {
        //TODO: Show rationale
    }
}

