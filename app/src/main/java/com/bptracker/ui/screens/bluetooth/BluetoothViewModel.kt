package com.bptracker.ui.screens.bluetooth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bptracker.utils.BPMonitorDevice
import com.bptracker.utils.BPMonitorReading
import com.bptracker.utils.BluetoothBPMonitor
import com.bptracker.utils.BluetoothState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BluetoothUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothBPMonitor: BluetoothBPMonitor
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BluetoothUiState())
    val uiState: StateFlow<BluetoothUiState> = _uiState.asStateFlow()
    
    val bluetoothState: StateFlow<BluetoothState> = bluetoothBPMonitor.state
    val discoveredDevices: StateFlow<List<BPMonitorDevice>> = bluetoothBPMonitor.discoveredDevices
    val latestReading: StateFlow<BPMonitorReading?> = bluetoothBPMonitor.latestReading
    
    fun startScan() {
        bluetoothBPMonitor.startScan()
    }
    
    fun stopScan() {
        bluetoothBPMonitor.stopScan()
    }
    
    fun connectToDevice(device: BPMonitorDevice) {
        bluetoothBPMonitor.connectToDevice(device)
    }
    
    fun disconnect() {
        bluetoothBPMonitor.disconnect()
    }
    
    fun requestPermissions() {
        // Permission request is handled by the Activity
        // This method triggers a re-check
        if (bluetoothBPMonitor.hasBluetoothPermissions()) {
            startScan()
        }
    }
    
    fun isBluetoothSupported(): Boolean = bluetoothBPMonitor.isBluetoothSupported()
    fun isBluetoothEnabled(): Boolean = bluetoothBPMonitor.isBluetoothEnabled()
    fun hasPermissions(): Boolean = bluetoothBPMonitor.hasBluetoothPermissions()
    fun getRequiredPermissions(): Array<String> = bluetoothBPMonitor.getRequiredPermissions()
    
    override fun onCleared() {
        super.onCleared()
        bluetoothBPMonitor.stopScan()
        bluetoothBPMonitor.disconnect()
    }
}
