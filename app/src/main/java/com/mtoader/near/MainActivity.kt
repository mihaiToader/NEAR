package com.mtoader.near

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.annotation.RequiresApi
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.example.near_library.ConnectionsActivity
import com.example.near_library.Endpoint
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Strategy
import com.mtoader.near.adapters.DevicesAdapter
import com.mtoader.near.adapters.MessageAdapter
import com.mtoader.near.model.message.Message
import java.util.*
import com.mtoader.near.model.message.MemberData
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : ConnectionsActivity() {
    private var devices: ArrayList<Endpoint> = ArrayList()
    private var adapter: DevicesAdapter? = null
    private lateinit var listView: ListView
    private lateinit var deviceNameTextView: TextView
    private var messageText: EditText? = null
    private var messagesView: ListView? = null
    private var messageAdapter: MessageAdapter? = null

    private lateinit var devicesView: View
    private lateinit var chatView: View

    private var data: MemberData? = null
    private var currentData: MemberData? = null

    private lateinit var hideLogsBtn: Button
    private lateinit var advertiseBtn: Button
    private lateinit var discoverBtn: Button

    /** A running log of debug messages. Only visible when DEBUG=true.  */
    private var logsTextView: TextView? = null
    private var logsView: View? = null


    /**
     * The connection strategy we'll use for Nearby Connections. In this case, we've decided on
     * P2P_STAR, which is a combination of Bluetooth Classic and WiFi Hotspots.
     */
    private val STRATEGY = Strategy.P2P_STAR

    /**
     * This service id lets us find other nearby devices that are interested in the same thing. Our
     * sample does exactly one thing, so we hardcode the ID.
     */
    private val SERVICE_ID = "NEAR_SERVICE_ID"

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

        hideLogsBtn = findViewById<Button>(R.id.hideLogsButton)
        hideLogsBtn!!.text = getString(R.string.hideLogs)

        advertiseBtn = findViewById(R.id.btnAdvertising)
        discoverBtn = findViewById(R.id.btnDiscovering)

        mName = intent.getStringExtra("deviceName")
        deviceNameTextView = findViewById(R.id.deviceNameTextView)

        deviceNameTextView!!.text = getString(R.string.device_name, mName)

        //messages
        messageText = findViewById<View>(R.id.editText) as EditText

        messageAdapter = MessageAdapter(this)
        messagesView = findViewById<View>(R.id.messages_view) as ListView
        messagesView!!.adapter = messageAdapter

        devicesView = findViewById(R.id.devicesView)
        chatView = findViewById(R.id.chatView)

        chatView!!.visibility = View.GONE

        data = MemberData(mName, getRandomColor())
        currentData = MemberData(mName, getRandomColor())

//        setState(State.SEARCHING)

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

    fun onAdvertise(view: View) {
        if (isAdvertising) {
            stopAdvertising()
        } else {
            startAdvertising()
        }
    }

    fun onDiscover(view: View) {
        if (isDiscovering) {
            stopDiscovering()
        } else {
            startDiscovering()
        }
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        setState(State.UNKNOWN)

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

        devicesView!!.visibility = View.GONE
        chatView!!.visibility = View.VISIBLE

        data = MemberData(endpoint.name, getRandomColor())
    }

    override fun onEndpointDisconnected(endpoint: Endpoint) {
        Toast.makeText(
                this, getString(R.string.toast_disconnected, endpoint.name), Toast.LENGTH_SHORT)
                .show()

        // If we lost all our endpoints, then we should reset the state of our app and go back
        // to our initial state (discovering).
//        if (connectedEndpoints.isEmpty()) {
//            setState(State.DISCOVERING)
//        }
//
        devices.remove(endpoint)
        adapter!!.notifyDataSetChanged()

        messageAdapter!!.clear()
        devicesView!!.visibility = View.VISIBLE
        chatView!!.visibility = View.GONE
        setState(State.SEARCHING)
    }

    override fun onEndpointDiscoverLost(endpoint: Endpoint?) {
        super.onEndpointDiscoverLost(endpoint)
        devices.remove(endpoint)
        adapter!!.notifyDataSetChanged()
    }

    override fun onConnectionFailed(endpoint: Endpoint) {
        // Let's try someone else.
        if (getState() == State.SEARCHING) {
            startDiscovering()
        }
    }

    override fun startAdvertising() {
        super.startAdvertising()
        changeAdvertiseBtnText()
        logD("Start advertising!")
    }

    override fun stopAdvertising() {
        super.stopAdvertising()
        changeAdvertiseBtnText()
        logD("Stop advertising")
    }

    override fun startDiscovering() {
        super.startDiscovering()
        changeDiscoverBtnText()
        logD("Start discovering")
    }

    override fun stopDiscovering() {
        super.stopDiscovering()
        changeDiscoverBtnText()
        logD("Stop discovering")
    }

    private fun changeDiscoverBtnText() {
        if (isDiscovering) {
            btnDiscovering.text = getString(R.string.stop_discovering)
        } else {
            btnDiscovering.text = getString(R.string.discover)
        }
    }

    private fun changeAdvertiseBtnText() {
        if (isAdvertising) {
            btnAdvertising.text = getString(R.string.stop_advertising)
        } else {
            btnAdvertising.text = getString(R.string.advertise)
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
            State.SEARCHING -> {
                disconnectFromAllEndpoints()
                startDiscovering()
                startAdvertising()
            }
            State.CONNECTED -> {
                stopDiscovering()
                stopAdvertising()
            }
            State.UNKNOWN -> stopAllEndpoints()
        }
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

    /** States that the UI goes through.  */
    enum class State {
        UNKNOWN,
        ADVERTISE,
        DISCOVER,
        CONNECTED,
        SEARCHING
    }

    fun sendMessage(view: View) {
        val message = messageText!!.text.toString()
        if (!message.isEmpty()) {
            messageText!!.text.clear()

            messageAdapter!!.add(Message(message, data, true))
            val count = messagesView!!.count
            messagesView!!.setSelection(count - 1)
            this.send(Payload.fromBytes(message.toByteArray()))
        }
    }

    fun closeChat(view: View) {
        disconnect(data!!.name)

        messageAdapter!!.clear()
        devicesView!!.visibility = View.VISIBLE
        chatView!!.visibility = View.GONE
    }

    override fun onReceive(endpoint: Endpoint, payload: Payload) {
        if (data!!.name != endpoint.name) {
            data = MemberData(endpoint.name, getRandomColor())
        }
        messageAdapter!!.add(Message(String(payload.asBytes()!!), data, false ))
        val count = messagesView!!.count
        messagesView!!.setSelection(count - 1)
    }


    private fun getRandomColor(): String {
        val r = Random()
        val sb = StringBuffer("#")
        while (sb.length < 7) {
            sb.append(Integer.toHexString(r.nextInt()))
        }
        return sb.toString().substring(0, 7)
    }
}
