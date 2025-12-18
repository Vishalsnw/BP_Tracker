package com.bptracker.utils

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.bptracker.data.model.BloodPressureReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

data class BPMonitorDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val device: BluetoothDevice
)

data class BPMonitorReading(
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val deviceName: String = ""
)

sealed class BluetoothState {
    object Idle : BluetoothState()
    object Scanning : BluetoothState()
    object Connecting : BluetoothState()
    data class Connected(val deviceName: String) : BluetoothState()
    data class ReadingReceived(val reading: BPMonitorReading) : BluetoothState()
    data class Error(val message: String) : BluetoothState()
    object BluetoothDisabled : BluetoothState()
    object PermissionRequired : BluetoothState()
}

@Singleton
class BluetoothBPMonitor @Inject constructor(
    private val context: Context
) {
    companion object {
        val BLOOD_PRESSURE_SERVICE_UUID: UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb")
        val BLOOD_PRESSURE_MEASUREMENT_UUID: UUID = UUID.fromString("00002a35-0000-1000-8000-00805f9b34fb")
        val INTERMEDIATE_CUFF_PRESSURE_UUID: UUID = UUID.fromString("00002a36-0000-1000-8000-00805f9b34fb")
        val BLOOD_PRESSURE_FEATURE_UUID: UUID = UUID.fromString("00002a49-0000-1000-8000-00805f9b34fb")
        val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        
        private const val SCAN_TIMEOUT_MS = 30000L
    }
    
    private val bluetoothManager: BluetoothManager? = 
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null
    
    private val _state = MutableStateFlow<BluetoothState>(BluetoothState.Idle)
    val state: StateFlow<BluetoothState> = _state.asStateFlow()
    
    private val _discoveredDevices = MutableStateFlow<List<BPMonitorDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BPMonitorDevice>> = _discoveredDevices.asStateFlow()
    
    private val _latestReading = MutableStateFlow<BPMonitorReading?>(null)
    val latestReading: StateFlow<BPMonitorReading?> = _latestReading.asStateFlow()
    
    private val handler = Handler(Looper.getMainLooper())
    private var isScanning = false
    
    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true
    
    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null
    
    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!hasBluetoothPermissions()) {
            _state.value = BluetoothState.PermissionRequired
            return
        }
        
        if (!isBluetoothEnabled()) {
            _state.value = BluetoothState.BluetoothDisabled
            return
        }
        
        if (isScanning) return
        
        _discoveredDevices.value = emptyList()
        _state.value = BluetoothState.Scanning
        
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(android.os.ParcelUuid(BLOOD_PRESSURE_SERVICE_UUID))
            .build()
        
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        
        try {
            bluetoothLeScanner?.startScan(listOf(scanFilter), scanSettings, scanCallback)
            isScanning = true
            
            handler.postDelayed({
                stopScan()
            }, SCAN_TIMEOUT_MS)
        } catch (e: Exception) {
            _state.value = BluetoothState.Error("Failed to start scan: ${e.message}")
        }
    }
    
    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (!isScanning) return
        
        try {
            bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            // Ignore
        }
        
        isScanning = false
        if (_state.value is BluetoothState.Scanning) {
            _state.value = BluetoothState.Idle
        }
    }
    
    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BPMonitorDevice) {
        if (!hasBluetoothPermissions()) {
            _state.value = BluetoothState.PermissionRequired
            return
        }
        
        stopScan()
        _state.value = BluetoothState.Connecting
        
        bluetoothGatt = device.device.connectGatt(context, false, gattCallback)
    }
    
    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _state.value = BluetoothState.Idle
    }
    
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val deviceName = device.name ?: "Unknown BP Monitor"
            
            val bpDevice = BPMonitorDevice(
                name = deviceName,
                address = device.address,
                rssi = result.rssi,
                device = device
            )
            
            val currentDevices = _discoveredDevices.value.toMutableList()
            if (currentDevices.none { it.address == bpDevice.address }) {
                currentDevices.add(bpDevice)
                _discoveredDevices.value = currentDevices
            }
        }
        
        override fun onScanFailed(errorCode: Int) {
            _state.value = BluetoothState.Error("Scan failed with error code: $errorCode")
            isScanning = false
        }
    }
    
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    val deviceName = gatt.device.name ?: "BP Monitor"
                    _state.value = BluetoothState.Connected(deviceName)
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _state.value = BluetoothState.Idle
                    gatt.close()
                }
            }
        }
        
        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val bpService = gatt.getService(BLOOD_PRESSURE_SERVICE_UUID)
                if (bpService != null) {
                    val measurementChar = bpService.getCharacteristic(BLOOD_PRESSURE_MEASUREMENT_UUID)
                    if (measurementChar != null) {
                        gatt.setCharacteristicNotification(measurementChar, true)
                        
                        val descriptor = measurementChar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
                        if (descriptor != null) {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                        }
                    }
                } else {
                    _state.value = BluetoothState.Error("Blood Pressure service not found")
                }
            }
        }
        
        @SuppressLint("MissingPermission")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == BLOOD_PRESSURE_MEASUREMENT_UUID) {
                val reading = parseBloodPressureMeasurement(characteristic.value, gatt.device.name ?: "BP Monitor")
                if (reading != null) {
                    _latestReading.value = reading
                    _state.value = BluetoothState.ReadingReceived(reading)
                }
            }
        }
    }
    
    private fun parseBloodPressureMeasurement(data: ByteArray, deviceName: String): BPMonitorReading? {
        if (data.isEmpty()) return null
        
        try {
            val flags = data[0].toInt() and 0xFF
            val isKpa = (flags and 0x01) != 0
            
            var offset = 1
            
            val systolicRaw = ((data[offset + 1].toInt() and 0xFF) shl 8) or (data[offset].toInt() and 0xFF)
            offset += 2
            
            val diastolicRaw = ((data[offset + 1].toInt() and 0xFF) shl 8) or (data[offset].toInt() and 0xFF)
            offset += 2
            
            offset += 2
            
            var pulse = 0
            if ((flags and 0x04) != 0) {
                pulse = ((data[offset + 1].toInt() and 0xFF) shl 8) or (data[offset].toInt() and 0xFF)
            }
            
            val systolic = if (isKpa) (systolicRaw * 7.50062).toInt() else systolicRaw
            val diastolic = if (isKpa) (diastolicRaw * 7.50062).toInt() else diastolicRaw
            
            return BPMonitorReading(
                systolic = systolic,
                diastolic = diastolic,
                pulse = pulse,
                deviceName = deviceName
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
}
