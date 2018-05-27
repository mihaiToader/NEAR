package com.mtoader.near

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ListView
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.mtoader.near.adapters.DevicesAdapter
import java.util.*
import kotlin.text.Charsets.UTF_8

class MainActivity : AppCompatActivity() {
    private val TAG = "Near"


    private var devices: ArrayList<String> = ArrayList()
    private var adapter: DevicesAdapter? = null
    private var listView: ListView? = null

    private val STRATEGY = Strategy.P2P_STAR
    private val codeName = CodeName.generate()

    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION)

    // Our handle to Nearby Connections
    private var connectionsClient: ConnectionsClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById(R.id.foundDevicesListView)

        devices.addAll(arrayListOf("Mihai", "Claudia"))
        adapter = DevicesAdapter(this, devices)

        listView?.adapter = adapter

        connectionsClient = Nearby.getConnectionsClient(this)
    }

    override fun onStop() {
        connectionsClient?.stopAllEndpoints()

        super.onStop()
    }


    // Callbacks for receiving payloads
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.i(TAG, "onPayloadReceived: ${String(payload.asBytes()!!, UTF_8)}")
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            Log.i(TAG, "onPayloadReceived: $endpointId $update")
        }
    }

    // Callbacks for finding other devices
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.i(TAG, "onEndpointFound: endpoint found, connecting")
//            connectionsClient!!.requestConnection(codeName, endpointId, connectionLifecycleCallback)
            devices.add(endpointId)
            adapter!!.notifyDataSetChanged()
        }

        override fun onEndpointLost(endpointId: String) {}
    }

    // Callbacks for connections to other devices
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.i(TAG, "onConnectionInitiated: accepting connection")
            connectionsClient!!.acceptConnection(endpointId, payloadCallback)
            Log.i(TAG, "onConnectionInitiated: accepting connection ${connectionInfo.endpointName}")

        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                Log.i(TAG, "onConnectionResult: connection successful")

//                connectionsClient!!.stopDiscovery()
//                connectionsClient!!.stopAdvertising()

            } else {
                Log.i(TAG, "onConnectionResult: connection failed")
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.i(TAG, "onDisconnected: disconnected from the opponent")
        }
    }


    /** Broadcasts our presence using Nearby Connections so other players can find us.  */
    private fun startAdvertising() {
        // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
        connectionsClient!!.startAdvertising(
                codeName, packageName, connectionLifecycleCallback, AdvertisingOptions(STRATEGY))
    }

    /** Starts looking for other players using Nearby Connections.  */
    private fun startDiscovery() {
        // Note: Discovery may fail. To keep this demo simple, we don't handle failures.
        connectionsClient!!.startDiscovery(
                packageName, endpointDiscoveryCallback, DiscoveryOptions(STRATEGY))
    }

    fun searchDevices(view: View) {
        startAdvertising()
        startDiscovery()
    }

}
