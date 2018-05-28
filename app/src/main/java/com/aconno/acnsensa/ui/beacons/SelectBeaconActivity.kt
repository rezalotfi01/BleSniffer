package com.aconno.acnsensa.ui.beacons

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.aconno.acnsensa.R
import kotlinx.android.synthetic.main.activity_toolbar.*

class SelectBeaconActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toolbar)
        toolbar.title = getString(R.string.select_beacon)

        var fragment = supportFragmentManager.findFragmentById(R.id.content_container)
        if (fragment == null) {
            fragment = BeaconListFragment.newInstance()
            supportFragmentManager.beginTransaction().add(R.id.content_container, fragment).commit()
        }
    }

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, SelectBeaconActivity::class.java)
            context.startActivity(intent)
        }
    }
}