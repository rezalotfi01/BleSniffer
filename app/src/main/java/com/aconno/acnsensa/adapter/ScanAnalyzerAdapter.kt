package com.aconno.acnsensa.adapter

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aconno.acnsensa.R
import com.aconno.acnsensa.domain.beacon.Beacon
import com.aconno.acnsensa.domain.deserializing.Deserializer
import kotlinx.android.synthetic.main.item_scan_record.view.*
import timber.log.Timber
import java.text.DateFormat
import java.util.*


fun ByteArray.toHex() = this.joinToString(separator = "") { "0x" + it.toInt().and(0xff).toString(16).padStart(2, '0') + " " }
fun ByteArray.inversedCopyOfRange(start: Int, end: Int) = this.reversedArray().copyOfRange((size - 1) - start, (size - 1) - end)
class ScanAnalyzerAdapter(
        private val scanLog: MutableList<MutablePair<Beacon, Int>>,
        private val scanRecordListener: ScanRecordListener,
        private var deserializers: MutableList<Deserializer>,
        private val longItemClickListener: LongItemClickListener<Beacon>
) : RecyclerView.Adapter<ScanAnalyzerAdapter.ViewHolder>() {

    var filter: String = ""
        set(value) {
            field = value
            if (value.isNotEmpty()) {
//                filteredList = scanLog.filter { beacon -> beacon.first.address.contains(filter, ignoreCase = true) }.toMutableList()
                notifyDataSetChanged()
            }
        }

//    private var filteredList: MutableList<MutablePair<Beacon, Int>> = mutableListOf()

    fun updateDeserializers(items: MutableList<Deserializer>) {
        deserializers = items
        notifyDataSetChanged()
    }

    fun setBeaconData(beaconData: List<Beacon>) {
        this.scanLog.clear()
        this.scanLog.addAll(beaconData.map { MutablePair(it, 1) })
//        if (filter.isNotEmpty()) filteredList = scanLog.filter { beacon -> beacon.first.address.contains(filter, ignoreCase = true) }.toMutableList()
        notifyDataSetChanged()
    }

    fun logScan(data: Beacon) {
//        if (scanLog.size > 0) {
//            Timber.e(data.advertisementData.toHex())
//            Timber.e(scanLog[0].first.advertisementData.toHex())
//        }
        scanLog.filter { (System.currentTimeMillis() - it.first.lastseen) < 2500 && it.first.address == data.address }.forEachIndexed { index, item ->
            //            Timber.e(scanLog.indexOf(item).toString())
//            if (item.first.address == data.address) {
            if (item.first.advertisementData.contentEquals(data.advertisementData)) {
//                    filteredList.indexOfFirst { it.first.lastseen == item.first.lastseen }.let {
//                        if (it == -1) return@let
//                        filteredList[it].second++
//                        filteredList[it].first.lastseen = data.lastseen
//                        notifyItemChanged(it)
//                    }
                Timber.e("Upgrading - " + item.first.name + data.name)
                item.second++
                item.first.lastseen = data.lastseen
                Timber.e("%s - %s", scanLog.indexOf(item), item.first.address)
                notifyItemChanged(scanLog.indexOf(item))
                return@logScan
            }
//            }
        }
        Timber.e("Added")
        scanLog.add(0, MutablePair(data, 1))



        if (filter.isEmpty() || data.address.contains(filter)) {
//            filteredList.add(0, MutablePair(data, 1))
        }
        notifyItemInserted(0)
        scanRecordListener.onRecordAdded()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_scan_record, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int = if (position > 0) 0 else 1

    override fun getItemCount(): Int {
        return scanLog.size
//        return (if (filter.isNotEmpty()) filteredList else scanLog).size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(scanLog[position])
//        holder.bind((if (filter.isNotEmpty()) filteredList else scanLog)[position])
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val deserializedFieldsAdapter = DeserializedFieldsAdapter()

        init {
            view.deserialized_field_list.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
            view.deserialized_field_list.adapter = deserializedFieldsAdapter
        }

        fun bind(data: MutablePair<Beacon, Int>) {
            view.time.text = getDateCurrentTimeZone(data.first.lastseen)
            view.repeating.text = "x${data.second}"

            if (deserializedFieldsAdapter.fields.isEmpty()) {
//                Timber.e(filteredList.size.toString())
                Timber.e(scanLog.size.toString())
                view.setOnLongClickListener { longItemClickListener.onLongItemClick(data.first) }
                view.address.text = data.first.address
                view.name.text = data.first.name
                view.data.text = data.first.advertisementData.toHex()

                with(data.first) {
                    deserializers.find {
                        when (it.filterType) {
                            Deserializer.Type.MAC -> address.contains(it.filter, ignoreCase = true)
                            Deserializer.Type.DATA -> advertisementData.toHex().contains(it.filter, ignoreCase = true)
                            else -> false
                        }
                    }?.let {
                        it.fieldDeserializers.map { d ->
                            val start = d.startIndexInclusive
                            val end = d.endIndexExclusive
                            val size = advertisementData.size
                            Triple(
                                    d.name,
                                    if (start > size || end > size) "Bad Indexes"
                                    else d.type.converter.deserialize(
                                            if (start <= end) advertisementData.copyOfRange(start, end)
                                            else advertisementData.inversedCopyOfRange(start, end)
                                    ).toString(),
                                    d.color
                            )
                        }.apply {
                        }.toMutableList().let {
                            deserializedFieldsAdapter.setFields(it)
                        }
                    }
                }
            }
        }
    }
}

fun getDateCurrentTimeZone(timestamp: Long): String {
    try {
        val calendar = Calendar.getInstance()
        val tz = TimeZone.getDefault()
        calendar.timeInMillis = timestamp
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.timeInMillis))
        val sdf = DateFormat.getDateTimeInstance()
        val currentTimeZone = calendar.time as Date
        return sdf.format(currentTimeZone)
    } catch (e: Exception) {
    }

    return ""
}

class MutablePair<A, B>(
        var first: A,
        var second: B
)