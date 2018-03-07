package com.aconno.acnsensa.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.aconno.acnsensa.domain.Bluetooth
import com.aconno.acnsensa.domain.interactor.type.MaybeUseCaseWithParameter
import com.aconno.acnsensa.domain.interactor.type.SingleUseCaseWithParameter
import com.aconno.acnsensa.domain.model.ScanResult
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import io.reactivex.Flowable

/**
 * @aconno
 */
class LiveGraphViewModel(
    private val bluetooth: Bluetooth,
    private val filterAdvertisementsUseCase: MaybeUseCaseWithParameter<ScanResult, ScanResult>,
    private val sensorValuesUseCase: SingleUseCaseWithParameter<Map<String, Number>, ScanResult>
) : ViewModel() {
    val updates: MutableLiveData<Unit> = MutableLiveData()

    private val temperatureSeries = BleDataSeries("Temperature")
    private val lightSeries = BleDataSeries("Light")
    private val humiditySeries = BleDataSeries("Humidity")
    private val pressureSeries = BleDataSeries("Pressure")
    private val xMagnetometerSeries = BleDataSeries("Magnetometer X")
    private val yMagnetometerSeries = BleDataSeries("Magnetometer Y")
    private val zMagnetometerSeries = BleDataSeries("Magnetometer Z")
    private val xAccelerometerSeries = BleDataSeries("Accelerometer X")
    private val yAccelerometerSeries = BleDataSeries("Accelerometer Y")
    private val zAccelerometerSeries = BleDataSeries("Accelerometer Z")
    private val xGyroscopeSeries = BleDataSeries("Gyroscope X")
    private val yGyroscopeSeries = BleDataSeries("Gyroscope Y")
    private val zGyroscopeSeries = BleDataSeries("Gyroscope Z")

    val temperatureGraph = BleGraph("Temperature", "Temperature Graph", listOf(temperatureSeries))
    val lightGraph = BleGraph("Light", "Light Graph", listOf(lightSeries))
    val humidityGraph = BleGraph("Humidity", "Humidity Graph", listOf(humiditySeries))
    val pressureGraph = BleGraph("Test", "Description", listOf(pressureSeries))
    val magnetometerGraph = BleGraph("Test", "Description", listOf(xMagnetometerSeries, yMagnetometerSeries, zMagnetometerSeries))
    val accelerometerGraph = BleGraph("Test", "Description", listOf(xAccelerometerSeries, yAccelerometerSeries, zAccelerometerSeries))
    val gyroscopeGraph = BleGraph("Test", "Description", listOf(xGyroscopeSeries, yGyroscopeSeries, zGyroscopeSeries))


    fun getGraph(type: Int): BleGraph {
        return when(type) {
            1 -> temperatureGraph
            2 -> lightGraph
            3 -> humidityGraph
            4 -> pressureGraph
            5 -> magnetometerGraph
            6 -> accelerometerGraph
            7 -> gyroscopeGraph
            else -> throw IllegalArgumentException()
        }
    }

    init {
        subscribe()

    }

    private fun subscribe() {
        val observable: Flowable<ScanResult> = bluetooth.getScanResults()
        observable.subscribe { scanResult ->
            filterAdvertisementsUseCase
                .execute(scanResult)
                .subscribe { filteredScanResult ->
                    sensorValuesUseCase
                        .execute(filteredScanResult)
                        .subscribe { sensorValues -> processSensorValues(sensorValues) }
                }
        }
    }

    private fun processSensorValues(sensorValues: Map<String, Number>?) {
        sensorValues?.let {
            for((a, b) in it) {
                when (a) {
                    "Temperature" -> temperatureSeries.updateDataSet(listOf(Pair(System.currentTimeMillis(),b)))
                }
            }
        }

        updates.value = Unit
    }

//    private fun subscribe() {
//        val type = 0
//        val observable: Flowable<List<Pair<Long, Number>>> = getObservableSensorReadings(type)
//        observable.subscribe {
//            dataSeries.updateDataSet(it)
//            updates.value = Unit
//        }
//
//        val type2 = 1
//        val observable2: Flowable<List<Pair<Long, Number>>> = getObservableSensorReadings(type2)
//        observable2.subscribe {
//            dataSeries2.updateDataSet(it)
//            updates.value = Unit
//        }
//    }

    private fun getObservableSensorReadings(type: Int): Flowable<List<Pair<Long, Number>>> {
        return when (type) {
            0 -> Flowable.fromArray(mockReadings())
            1 -> Flowable.fromArray(mockReadings2())
            else -> throw IllegalArgumentException()
        }

    }

    private fun mockReadings(): List<Pair<Long, Number>> {
        return arrayOf(Pair(0L, 0), Pair(1L, 1), Pair(2L, 2), Pair(3L, 3)).toList()
    }

    private fun mockReadings2(): List<Pair<Long, Number>> {
        return arrayOf(Pair(0L, 0), Pair(-1L, -1), Pair(-2L, -2), Pair(-3L, -3)).toList()
    }
}


class BleDataSeries(val title: String) {
    private val entries: MutableList<Entry> = mutableListOf(Entry(0f, 0f))

    val lineDataSet = LineDataSet(entries, title)

    init {
        //lineDataSet.color = ContextCompat.getColor()
    }

    fun updateDataSet(newEntries: List<Pair<Long, Number>>) {
        //TODO: uncomment
        //entries.clear()
        newEntries.forEach { (timestamp, value) ->
            val entry = Entry(timestamp.toFloat(), value.toFloat())
            lineDataSet.addEntry(entry)
        }
        lineDataSet.notifyDataSetChanged()
    }
}

class BleGraph(val title: String, val description: String, val series: List<BleDataSeries>) {


    val lineData = LineData(series.map { it.lineDataSet })


    init {
        //lineDataSet.color = ContextCompat.getColor()
    }

    fun getDescription(): Description {
        val description = Description()
        description.text = this.description
        return description
    }
}