package net.rcdevgames.wawlaundry.domain.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import javax.inject.Inject
import java.util.UUID

interface ThermalPrinterService {
    suspend fun getPairedDevices(): List<BluetoothDevice>
    suspend fun connect(deviceAddress: String): Boolean
    suspend fun printReceipt(lines: List<String>, header: String?, footer: String?)
    fun disconnect()
}

class EscPosPrinterServiceImpl @Inject constructor() : ThermalPrinterService {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    // Standard SPP UUID for Bluetooth Serial Port Profile
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    @SuppressLint("MissingPermission")
    override suspend fun getPairedDevices(): List<BluetoothDevice> {
        return withContext(Dispatchers.IO) {
            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun connect(deviceAddress: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return@withContext false
                
                val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
                true
            } catch (e: Exception) {
                e.printStackTrace()
                disconnect()
                false
            }
        }
    }

    override suspend fun printReceipt(lines: List<String>, header: String?, footer: String?) {
        withContext(Dispatchers.IO) {
            if (outputStream == null) return@withContext

            try {
                // Initialize printer
                outputStream?.write(byteArrayOf(0x1B, 0x40))

                // Print Header (Centered, Bold)
                if (!header.isNullOrBlank()) {
                    outputStream?.write(byteArrayOf(0x1B, 0x61, 0x01)) // Center align
                    outputStream?.write(byteArrayOf(0x1B, 0x45, 0x01)) // Bold on
                    outputStream?.write("$header\n".toByteArray())
                    outputStream?.write(byteArrayOf(0x1B, 0x45, 0x00)) // Bold off
                    outputStream?.write(byteArrayOf(0x1B, 0x61, 0x00)) // Left align
                    outputStream?.write("--------------------------------\n".toByteArray())
                }

                // Print Lines
                lines.forEach { line ->
                    outputStream?.write("$line\n".toByteArray())
                }

                // Print Footer (Centered)
                if (!footer.isNullOrBlank()) {
                    outputStream?.write("--------------------------------\n".toByteArray())
                    outputStream?.write(byteArrayOf(0x1B, 0x61, 0x01)) // Center align
                    outputStream?.write("$footer\n".toByteArray())
                    outputStream?.write(byteArrayOf(0x1B, 0x61, 0x00)) // Left align
                }

                // Feed paper and cut
                outputStream?.write("\n\n\n\n".toByteArray())
                outputStream?.write(byteArrayOf(0x1D, 0x56, 0x41, 0x10)) // Partial cut
                
                outputStream?.flush()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun disconnect() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream = null
            bluetoothSocket = null
        }
    }
}
