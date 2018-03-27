package com.aconno.acnsensa.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.aconno.acnsensa.AcnSensaApplication
import com.aconno.acnsensa.R
import com.aconno.acnsensa.dagger.addaction.AddActionComponent
import com.aconno.acnsensa.dagger.addaction.AddActionModule
import com.aconno.acnsensa.dagger.addaction.DaggerAddActionComponent
import com.aconno.acnsensa.domain.ifttt.AddActionUseCase
import com.aconno.acnsensa.domain.ifttt.GeneralAction
import com.aconno.acnsensa.domain.ifttt.LimitCondition
import com.aconno.acnsensa.domain.ifttt.NotificationOutcome
import kotlinx.android.synthetic.main.activity_add_action.*
import javax.inject.Inject


class AddActionActivity : AppCompatActivity() {

    @Inject
    lateinit var actionViewModel: ActionViewModel

    private val addActionComponent: AddActionComponent by lazy {
        val acnSensaApplication: AcnSensaApplication? = application as? AcnSensaApplication
        DaggerAddActionComponent.builder()
            .appComponent(acnSensaApplication?.appComponent)
            .addActionModule(AddActionModule(this)).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_action)
        addActionComponent.inject(this)

        initSpinner(sensor_spinner, actionViewModel.getSensorTypes())
        initSpinner(condition_type_spinner, actionViewModel.getConditionTypes())

        add_action_button.setOnClickListener { this.addAction() }
    }

    override fun onResume() {
        super.onResume()
        actionViewModel.addActionResults.observe(this, Observer { onAddActionResult(it) })
    }

    private fun initSpinner(spinner: Spinner, contents: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, contents)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun addAction() {
        val name = action_name.text.toString()
        val sensorType = sensor_spinner.selectedItemPosition
        val conditionType = condition_type_spinner.selectedItem.toString()
        val value = condition_value.text.toString()
        val outcome = outcome_notification_text.toString()

        actionViewModel.addAction(name, sensorType, conditionType, value, "Got $value")
    }

    private fun onAddActionResult(success: Boolean?) {
        when (success) {
            true -> finish()
            else -> Toast.makeText(this, "Failed to create Action", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AddActionActivity::class.java)
            context.startActivity(intent)
        }
    }
}


class ActionViewModelFactory(
    private val addActionUseCase: AddActionUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val viewModel = ActionViewModel(addActionUseCase)
        val result = listOf(viewModel).filterIsInstance(modelClass)
        if (result.size == 1) {
            return result[0]
        } else {
            throw IllegalArgumentException()
        }
    }
}

class ActionViewModel(private val addActionUseCase: AddActionUseCase) : ViewModel() {

    val addActionResults: MutableLiveData<Boolean> = MutableLiveData()

    fun addAction(
        name: String,
        sensorType: Int,
        conditionType: String,
        value: String,
        outcomeMessage: String
    ) {
        try {
            val type = when (conditionType) {
                "Max" -> 1
                "Min" -> 0
                else -> throw IllegalArgumentException("Got invalid sensor type: $conditionType")
            }
            val condition = LimitCondition(sensorType, value.toFloat(), type)
            val outcome = NotificationOutcome(outcomeMessage)
            val action = GeneralAction(name, condition, outcome)
            addActionUseCase.execute(action)
                .subscribe({ onAddActionSuccess() }, { onAddActionFail() })
        } catch (e: Exception) {
            onAddActionFail()
        }
    }

    fun getSensorTypes(): List<String> {
        return listOf(
            TEMPERATURE,
            LIGHT,
            HUMIDITY,
            PRESSURE,
            MAGNETOMETER_X,
            MAGNETOMETER_Y,
            MAGNETOMETER_Z,
            ACCELEROMETER_X,
            ACCELEROMETER_Y,
            ACCELEROMETER_Z,
            GYROSCOPE_X,
            GYROSCOPE_Y,
            GYROSCOPE_Z,
            BATTERY_LEVEL
        )
    }

    fun onAddActionSuccess() {
        addActionResults.value = true
    }

    fun onAddActionFail() {
        addActionResults.value = false
    }

    fun getConditionTypes(): List<String> {
        return listOf("Max", "Min")
    }

    companion object {
        const val TEMPERATURE = "Temperature"
        const val LIGHT = "Light"
        const val HUMIDITY = "Humidity"
        const val PRESSURE = "Pressure"
        const val MAGNETOMETER_X = "Magnetometer X"
        const val MAGNETOMETER_Y = "Magnetometer Y"
        const val MAGNETOMETER_Z = "Magnetometer Z"
        const val ACCELEROMETER_X = "Accelerometer X"
        const val ACCELEROMETER_Y = "Accelerometer Y"
        const val ACCELEROMETER_Z = "Accelerometer Z"
        const val GYROSCOPE_X = "Gyroscope X"
        const val GYROSCOPE_Y = "Gyroscope Y"
        const val GYROSCOPE_Z = "Gyroscope Z"
        const val BATTERY_LEVEL = "Battery Level"
    }
}

