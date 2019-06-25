package com.jjmin.safetaxi

import android.Manifest
import android.app.ListActivity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.Manifest.permission
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.WRITE_CALENDAR
import android.bluetooth.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.InputDevice.getDevice
import android.content.*
import android.nfc.Tag
import android.os.IBinder
import android.view.View
import android.widget.ListAdapter
import android.widget.ListView
import java.lang.NullPointerException
import java.util.ArrayList
import java.util.HashMap

private const val SCAN_PERIOD: Long = 10000

class MainActivity : AppCompatActivity() {
    private var mLeDeviceListAdapter: LeDeviceListAdapter? = null
    val REQUEST_ENABLE_BT = 100
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private val leDeviceListAdapter: LeDeviceListAdapter? = null
    private var mScanning: Boolean = false
    private var mBluetoothLeService: BluetoothLeService? = null

    private var mDeviceName: String? = null
    private var mDeviceAddress: String? = null
    private var mConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissionCheck1 = ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
        val permissionCheck2 = ContextCompat.checkSelfPermission(this, permission.SEND_SMS)
        val permissionCheck3 = ContextCompat.checkSelfPermission(this, permission.READ_EXTERNAL_STORAGE)
        if (permissionCheck1 == PackageManager.PERMISSION_DENIED ||
            permissionCheck2 == PackageManager.PERMISSION_DENIED ||
            permissionCheck3 == PackageManager.PERMISSION_DENIED ) {
            // 권한 없음
            permissionCheck()
        } else {
            // 권한 있음
        }

        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, "이 기종은 BLE를 지원하지 않습니다.", Toast.LENGTH_SHORT)
            finish()
        }


        val bluetoothManager : BluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter


        if (mBluetoothAdapter == null || !mBluetoothAdapter?.isEnabled!!) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        mLeDeviceListAdapter = LeDeviceListAdapter(this)
        mainListview.adapter = mLeDeviceListAdapter

        bleBtn.setOnClickListener {
            scanLeDevice(true)
        }
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)

    fun permissionCheck(){
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    permission.ACCESS_FINE_LOCATION,
                    permission.SEND_SMS,
                    permission.READ_EXTERNAL_STORAGE),
                100)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 100){
            if (grantResults.isNotEmpty() && grantResults[0] === PackageManager.PERMISSION_GRANTED &&
                 grantResults[1] === android.content.pm.PackageManager.PERMISSION_GRANTED
                && grantResults[2] === PackageManager.PERMISSION_GRANTED) {
                // 권한 허가
                Toast.makeText(this, "권한을 허용하였습니다.", Toast.LENGTH_SHORT)
            } else {
                // 권한 거부
                Toast.makeText(this, "앱을 사용하시려면 권한을 승락해 주세요.", Toast.LENGTH_SHORT)
            }
            return
        }
    }

    private fun scanLeDevice(enable: Boolean) {
        when (enable) {
            true -> {
                // Stops scanning after a pre-defined scan period.
                Handler().postDelayed({
                    mScanning = false
                    mBluetoothAdapter?.stopLeScan(leScanCallback)
                }, SCAN_PERIOD)
                mScanning = true
                mBluetoothAdapter?.startLeScan(leScanCallback)
            }
            else -> {
                mScanning = false
                mBluetoothAdapter?.stopLeScan(leScanCallback)
            }
        }
    }
    val leScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->

        runOnUiThread {
            try {
                var device = device
                if(device.name.contains("SUNRIN")){
                    interlockDevice(device)
                }
                Log.e("devicename",device.address)
            }catch (e : NullPointerException){
                e.printStackTrace()
                Log.e("devicename","null")
            }catch (e : IllegalStateException){
                e.printStackTrace()
            }
        }
    }

    fun interlockDevice(device : BluetoothDevice){
        Toast.makeText(this, "기기가 연동되었습니다.", Toast.LENGTH_SHORT)

        val intent = Intent(this, DeviceControlActivity::class.java)
        Log.e(device.name,device.address)
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.name)
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.address)
        if (mScanning) {
            mBluetoothAdapter?.stopLeScan(leScanCallback)
            mScanning = false
        }
//        startService()
        startActivity(intent)
    }

//    fun startService(){
//        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
//        bindService(gattServiceIntent, mServiceConnection,Context.BIND_AUTO_CREATE)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
//        if (mBluetoothLeService != null) {
//            val result = mBluetoothLeService?.connect(mDeviceAddress)
//            Log.d("resume", "Connect request result=$result")
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        unregisterReceiver(mGattUpdateReceiver)
//    }
//
//    private fun makeGattUpdateIntentFilter(): IntentFilter {
//        val intentFilter = IntentFilter()
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
//        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
//        return intentFilter
//    }
}
