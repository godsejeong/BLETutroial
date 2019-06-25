package com.jjmin.safetaxi

import android.app.Activity
import android.bluetooth.BluetoothDevice
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior.setTag
import android.widget.TextView
import android.view.ViewGroup
import android.text.method.TextKeyListener.clear
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.BaseAdapter


class LeDeviceListAdapter(activity : Activity) : BaseAdapter() {
    private val mLeDevices: ArrayList<BluetoothDevice> = ArrayList()
    private val mInflator: LayoutInflater = activity.layoutInflater


    fun addDevice(device: BluetoothDevice) {
        Log.e("add","add")
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device)
        }
    }

    fun getDevice(position: Int): BluetoothDevice {
        return mLeDevices[position]
    }

    fun clear() {
        mLeDevices.clear()
    }

    override fun getCount(): Int {
        return mLeDevices.size
    }

    override fun getItem(i: Int): Any {
        return mLeDevices[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view
        val viewHolder: ViewHolder
        // General ListView optimization code.
        if (view == null) {
            view = mInflator.inflate(R.layout.listitem_device, null)
            viewHolder = ViewHolder()
            viewHolder.deviceAddress = view!!.findViewById(R.id.device_address)
            viewHolder.deviceName = view!!.findViewById(R.id.device_name)
            view!!.setTag(viewHolder)
        } else {
            viewHolder = view!!.getTag() as ViewHolder
        }
        val device = mLeDevices[i]
        val deviceName = device.name
        if (deviceName != null && deviceName.isNotEmpty())
            viewHolder.deviceName?.text = deviceName
        else
            viewHolder.deviceName?.text = "Unknown device"
        viewHolder.deviceAddress?.text = device.address
        return view
    }

    internal class ViewHolder {
        var deviceName: TextView? = null
        var deviceAddress: TextView? = null
    }
}
