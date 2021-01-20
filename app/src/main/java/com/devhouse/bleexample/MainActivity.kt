package com.devhouse.bleexample

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), DevicesAdapter.HandleDevices {

    companion object {
        const val REQUEST_ENABLE_BT = 101
    }

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
    private var mScanning = false
    private val handler = Handler()
    var bluetoothGatt: BluetoothGatt? = null
    private lateinit var devicesListener: DevicesAdapter.HandleDevices

    private var leDeviceListAdapter: DevicesAdapter? = null
    private val devices: ArrayList<DeviceModel> = ArrayList()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, "Missing LE", Toast.LENGTH_SHORT).show()
            finish()
        }

        devicesListener = this

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        enableBluetooth()


    }

    private fun enableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            scanLeDevice()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            scanLeDevice()
        } else {
            Snackbar.make(findViewById(android.R.id.content), "You need to enable bluetooth", Snackbar.LENGTH_INDEFINITE).setAction("Enable") {
                enableBluetooth()
            }.show()
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val deviceModel = DeviceModel(result?.device)
            Log.d("TAG", "onScanResult: $deviceModel")
            devices.add(deviceModel)
        }
    }


    private fun scanLeDevice() {
        if (!mScanning) {
            handler.postDelayed({
                mScanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
                val set: Set<DeviceModel> = HashSet(devices)
                devices.clear()
                devices.addAll(set)
                leDeviceListAdapter = DevicesAdapter(devices, this)
                rvDevices.apply {
                    layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
                    adapter = leDeviceListAdapter
                }
            }, SCAN_PERIOD)
            mScanning = true
            devices.clear()
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            mScanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }

    override fun connectDevice(deviceModel: DeviceModel) {

        bluetoothGatt = deviceModel.device?.connectGatt(this, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.i("TAG", "Connected to GATT server.")
                        Log.i("TAG", "Attempting to start service discovery: " +
                                bluetoothGatt?.discoverServices())
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {

                        Log.i("TAG", "Disconnected from GATT server.")
                    }
                }
            }
        })


        handler.postDelayed({
            bluetoothGatt?.disconnect()
        }, SCAN_PERIOD)


    }


}
