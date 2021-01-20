package com.devhouse.bleexample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Abdullah on 1/19/2021.
 */
class DevicesAdapter(private val devices: ArrayList<DeviceModel>,private val listener:HandleDevices) :
    RecyclerView.Adapter<DevicesAdapter.DevicesViewHolder>() {
    class DevicesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.deviceName)
        val deviceAddress: TextView = itemView.findViewById(R.id.deviceAddress)
    }


    interface HandleDevices{
        fun connectDevice(deviceModel: DeviceModel)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_device_item, parent, false)
        return DevicesViewHolder(view)
    }

    override fun onBindViewHolder(holder: DevicesViewHolder, position: Int) {
        holder.apply {
            deviceName.text = devices[position].device?.name ?: ""
            deviceAddress.text = devices[position].device?.address ?: ""

            itemView.setOnClickListener {
                listener.connectDevice(devices[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }
}