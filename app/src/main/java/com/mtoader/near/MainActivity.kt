package com.mtoader.near

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.Strategy
import com.mtoader.near.adapters.DevicesAdapter
import com.mtoader.near.model.Endpoint
import com.mtoader.near.nearbyConnections.ConnectionsActivity
import java.util.*
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.content.Intent
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener




class MainActivity : ConnectionsActivity() {
    private var devices: ArrayList<Endpoint> = ArrayList()
    private var adapter: DevicesAdapter? = null
    private var listView: ListView? = null
    private var deviceNameTextView: TextView? = null

    /** A running log of debug messages. Only visible when DEBUG=true.  */
    private var logsTextView: TextView? = null
    private var logsView: View? = null

    private var hideLogsBtn: Button? = null

    private var advertiseDiscoverBtn: Button? = null

    /**
     * The connection strategy we'll use for Nearby Connections. In this case, we've decided on
     * P2P_STAR, which is a combination of Bluetooth Classic and WiFi Hotspots.
     */
    private val STRATEGY = Strategy.P2P_CLUSTER

    /**
     * Advertise for 30 seconds before going back to discovering. If a client connects, we'll continue
     * to advertise indefinitely so others can still connect.
     */
    private val ADVERTISING_DURATION: Long = 30000

    /**
     * This service id lets us find other nearby devices that are interested in the same thing. Our
     * sample does exactly one thing, so we hardcode the ID.
     */
    private val SERVICE_ID = "NEAR_SERVICE_ID"

    /**
     * A Handler that allows us to post back on to the UI thread. We use this to resume discovery
     * after an uneventful bout of advertising.
     */
    private val mUiHandler = Handler(Looper.getMainLooper())

    /** Starts discovery. Used in a postDelayed manor with [.mUiHandler].  */
    private val mDiscoverRunnable = Runnable { setState(State.DISCOVERING) }


    /**
     * The state of the app. As the app changes states, the UI will update and advertising/discovery
     * will start/stop.
     */
    private var mState = State.UNKNOWN

    /** A random UID used as this device's endpoint name.  */
    private lateinit var mName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById<View>(R.id.foundDevicesListView) as ListView

        adapter = DevicesAdapter(this, devices)

        listView?.adapter = adapter


        listView?.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val entry: Endpoint = parent.getItemAtPosition(position) as Endpoint
            connectToEndpoint(entry)
        }

        logsTextView = findViewById<View>(R.id.logsTextView) as TextView
        logsTextView!!.movementMethod = ScrollingMovementMethod()

        logsView = findViewById(R.id.logsView)

        hideLogsBtn = findViewById(R.id.hideLogsButton)
        hideLogsBtn!!.text = getString(R.string.hideLogs)

        advertiseDiscoverBtn = findViewById(R.id.btnAdvOrDisc)

        mName = intent.getStringExtra("deviceName")
        deviceNameTextView = findViewById(R.id.deviceNameTextView)

        deviceNameTextView!!.text = getString(R.string.device_name, mName)

    }

    private fun appendToLogs(msg: CharSequence) {
        logsTextView!!.append("\n")
        logsTextView!!.append(DateFormat.format("hh:mm", System.currentTimeMillis()).toString() + ": ")
        logsTextView!!.append(msg)
    }

    override fun logV(msg: String) {
        super.logV(msg)
        appendToLogs(toColor(msg, R.color.log_verbose))
    }

    override fun logD(msg: String) {
        super.logD(msg)
        appendToLogs(toColor(msg, R.color.log_debug))
    }

    override fun logW(msg: String) {
        super.logW(msg)
        appendToLogs(toColor(msg, R.color.log_warning))
    }

    override fun logE(msg: String, e: Throwable) {
        super.logE(msg, e)
        appendToLogs(toColor(msg, R.color.log_error))
    }

    private fun toColor(msg: String, color: Int): CharSequence {
        val spannable = SpannableString(msg)
        spannable.setSpan(ForegroundColorSpan(color), 0, msg.length, 0)
        return spannable
    }

    fun advertiseOrDiscover(view: View) {
        if (advertiseDiscoverBtn!!.text == getString(R.string.advertise)) {
            advertiseDiscoverBtn!!.text = getString(R.string.discover)
            setState(State.ADVERTISING)
        } else {
            advertiseDiscoverBtn!!.text = getString(R.string.advertise)
            setState(State.DISCOVERING)
        }
    }

    fun discover(view: View) {
        setState(State.DISCOVERING)
    }

    fun hideLogs(view: View) {
        if (logsView!!.visibility == View.GONE) {
            logsView!!.visibility = View.VISIBLE
            hideLogsBtn!!.text = getString(R.string.hideLogs)
        } else {
            logsView!!.visibility = View.GONE
            hideLogsBtn!!.text = getString(R.string.showLogs)
        }
    }

    private fun generateRandomName(): String {
        var name = ""
        val random = Random()
        for (i in 0..4) {
            name += random.nextInt(10)
        }
        return name
    }

    override fun onStart() {
        super.onStart()
        setState(State.DISCOVERING)
    }

    override fun onStop() {
        setState(State.UNKNOWN)

        mUiHandler.removeCallbacksAndMessages(null)
        super.onStop()
    }

    override fun onEndpointDiscovered(endpoint: Endpoint) {
        // We found an advertiser!
        devices.add(endpoint)
        adapter!!.notifyDataSetChanged()
        logD("Endpoint discoverd $endpoint.name")
        if (!isConnecting) {
//            connectToEndpoint(endpoint)
        }
    }

    override fun onConnectionInitiated(endpoint: Endpoint, connectionInfo: ConnectionInfo) {
        // A connection to another device has been initiated! We'll accept the connection immediately.
        acceptConnection(endpoint)
    }

    override fun onEndpointConnected(endpoint: Endpoint) {
        Toast.makeText(
                this, getString(R.string.toast_connected, endpoint.name), Toast.LENGTH_SHORT)
                .show()
        setState(State.CONNECTED)
    }

    override fun onEndpointDisconnected(endpoint: Endpoint) {
        Toast.makeText(
                this, getString(R.string.toast_disconnected, endpoint.name), Toast.LENGTH_SHORT)
                .show()

        // If we lost all our endpoints, then we should reset the state of our app and go back
        // to our initial state (discovering).
        if (connectedEndpoints.isEmpty()) {
            setState(State.DISCOVERING)
        }

        adapter!!.notifyDataSetChanged()
    }

    override fun onEndpointDiscoverLost(endpoint: Endpoint?) {
        super.onEndpointDiscoverLost(endpoint)
        devices.remove(endpoint)
    }

    override fun onConnectionFailed(endpoint: Endpoint) {
        // Let's try someone else.
        if (getState() == State.DISCOVERING && !discoveredEndpoints.isEmpty()) {
            connectToEndpoint(pickRandomElem(discoveredEndpoints))
        }
    }

    /**
     * The state has changed. I wonder what we'll be doing now.
     *
     * @param state The new state.
     */
    private fun setState(state: State) {
        if (mState == state) {
            logW("State set to $state but already in that state")
            return
        }

        logD("State set to $state")
        val oldState = mState
        mState = state
        onStateChanged(oldState, state)
    }

    /** @return The current state.
     */
    private fun getState(): State {
        return mState
    }

    /**
     * State has changed.
     *
     * @param oldState The previous state we were in. Clean up anything related to this state.
     * @param newState The new state we're now in. Prepare the UI for this state.
     */
    private fun onStateChanged(oldState: State, newState: State) {
        // Update Nearby Connections to the new state.
        when (newState) {
            State.DISCOVERING -> {
                if (isAdvertising) {
                    stopAdvertising()
                }
                disconnectFromAllEndpoints()
                startDiscovering()
            }
            State.ADVERTISING -> {
                if (isDiscovering) {
                    stopDiscovering()
                }
                disconnectFromAllEndpoints()
                startAdvertising()
            }
            State.CONNECTED -> if (isDiscovering) {
                stopDiscovering()
            } else if (isAdvertising) {
                // Continue to advertise, so others can still connect,
                // but clear the discover runnable.
                removeCallbacks(mDiscoverRunnable)
            }
            State.UNKNOWN -> stopAllEndpoints()
            else -> {
            }
        }// no-op
    }

    override fun getName(): String {
        return mName
    }

    override fun getServiceId(): String {
        return SERVICE_ID
    }

    override fun getStrategy(): Strategy {
        return STRATEGY
    }

    /** {@see Handler#removeCallbacks(Runnable)}  */
    private fun removeCallbacks(r: Runnable) {
        mUiHandler.removeCallbacks(r)
    }

    private inline fun <reified T> pickRandomElem(collection: Collection<T>): T {
        return collection.toTypedArray()[Random().nextInt(collection.size)]
    }

    /** States that the UI goes through.  */
    enum class State {
        UNKNOWN,
        DISCOVERING,
        ADVERTISING,
        CONNECTED
    }
}
