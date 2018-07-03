package com.mtoader.near

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.Toast
import com.example.near_library.ConnectionsActivity
import com.example.near_library.model.Command
import com.example.near_library.model.Endpoint
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.Strategy
import com.mtoader.near.adapters.DevicesAdapter
import com.mtoader.near.adapters.MessageAdapter
import com.mtoader.near.model.NearPayloadType
import com.mtoader.near.model.dto.*
import com.mtoader.near.model.message.MemberData
import com.mtoader.near.model.message.Message
import com.mtoader.near.service.NearLogsApi
import com.mtoader.near.service.NearLogsApiUtils
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : ConnectionsActivity() {
    private lateinit var mDiscoveredDevicesAdapter: DevicesAdapter
    private lateinit var mConnectedDevicesAdapter: DevicesAdapter
    private lateinit var mNetworkDevicesAdapter: DevicesAdapter
    private lateinit var mMessageAdapter: MessageAdapter

    private var chattingUser: MemberData? = null
    private var currentData: MemberData? = null
    private var mIsChatActive: Boolean = false
    private var mIsChatPending: Boolean = false

    private lateinit var mAdvertiseMenuItem: MenuItem
    private lateinit var mDiscoverMenuItem: MenuItem
    private lateinit var mAPIService: NearLogsApi

    private lateinit var mNearLogsAddress: String
    private lateinit var mSession: String

    private lateinit var authorizationToken: AuthorizationToken
    private val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ROOT)

    private var logs: ArrayList<LogDto> = ArrayList()
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

        initialiseDiscoveredDevicesList()

        initialiseConnectedDevicesList()

        initialiseNetworkDevicesList()

        initialiseLogsList()

        initialiseDeviceName()

        chatView.visibility = View.GONE


        initialiseMessages()

        currentData = MemberData(mName, getRandomColor(), null)

        mAPIService = NearLogsApiUtils.getApiService(mNearLogsAddress)

        initialiseDrawer()
        makeLoginToNearLogs()
//        setState(State.SEARCHING)

        Thread.setDefaultUncaughtExceptionHandler { paramThread, paramThrowable ->
            logE(paramThrowable.toString(), paramThrowable)
            System.exit(2)
        }

        sendNearLog("Application started", LogType.INFO, "onCreate")
    }

    override fun onBackPressed() {
        sendNearLog("Back button pressed", LogType.INFO, "onBackPressed")
        val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        } else {
            AlertDialog.Builder(this)
        }
        builder.setTitle("Exit?")
                .setMessage("Are you sure you want to exit? :(")
                .setPositiveButton(android.R.string.yes, { _, _ ->
                    super.onBackPressed()
                })
                .setNegativeButton(android.R.string.no, { _, _ ->

                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }

    private fun initialiseDiscoveredDevicesList() {
        mDiscoveredDevicesAdapter = DevicesAdapter(this)
        foundDevicesListView.visibility = View.GONE
        foundDevicesListView.adapter = mDiscoveredDevicesAdapter
        foundDevicesListView.onItemClickListener = OnItemClickListener { parent, _, position, _ ->
            val entry: Endpoint = parent.getItemAtPosition(position) as Endpoint
            if (this.connectedEndpoints.contains(entry)) {
                Toast.makeText(
                        this, "Already connected!", Toast.LENGTH_SHORT)
                        .show()
            } else {
                connectToEndpoint(entry)
            }
        }
    }

    private fun initialiseConnectedDevicesList() {
        mConnectedDevicesAdapter = DevicesAdapter(this)
        visibleDevicesText.text = getString(R.string.connected_devices)
        connectedDevicesListView.visibility = View.VISIBLE
        connectedDevicesListView.adapter = mConnectedDevicesAdapter
        connectedDevicesListView.onItemClickListener = OnItemClickListener { parent, _, position, _ ->
            val entry: Endpoint = parent.getItemAtPosition(position) as Endpoint
            onChatStarted(entry)
        }
    }

    private fun initialiseNetworkDevicesList() {
        mNetworkDevicesAdapter = DevicesAdapter(this)
        networkDevicesListView.adapter = mNetworkDevicesAdapter
        networkDevicesListView.visibility = View.GONE
        networkDevicesListView.onItemClickListener = OnItemClickListener { parent, _, position, _ ->
            val entry: Endpoint = parent.getItemAtPosition(position) as Endpoint
            onChatStarted(entry)
        }
    }

    private fun initialiseLogsList() {
        logsTextView!!.movementMethod = ScrollingMovementMethod()

        hideLogsButton.text = getString(R.string.hideLogs)
    }

    private fun initialiseDeviceName() {
        mName = intent.getStringExtra("deviceName")
        deviceNameTextView.text = getString(R.string.device_name, mName)

        mSession = intent.getStringExtra("sessionName")
        mNearLogsAddress = intent.getStringExtra("loggingAddressApp")
    }

    private fun initialiseMessages() {
        waitingToAccept.visibility = View.GONE
        loadingBar.visibility = View.GONE

        mMessageAdapter = MessageAdapter(this)
        messagesListView.adapter = mMessageAdapter
    }

    private fun initialiseDrawer() {
        mAdvertiseMenuItem = navigationView.menu.findItem(R.id.advertise_menu)
        mDiscoverMenuItem = navigationView.menu.findItem(R.id.discover_menu)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            // close drawer when item is tapped
            drawerLayout.closeDrawers()
            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here

            when {
                menuItem.itemId == R.id.advertise_menu -> {
                    startOrStopAdvertising()
                }
                menuItem.itemId == R.id.discover_menu -> {
                    startOrStopDiscovering()
                }
                menuItem.itemId == R.id.show_connected -> {
                    foundDevicesListView.visibility = View.GONE
                    connectedDevicesListView.visibility = View.VISIBLE
                    networkDevicesListView.visibility = View.GONE
                    visibleDevicesText.text = getString(R.string.connected_devices)
                    menuItem.isChecked = true
                }
                menuItem.itemId == R.id.show_discovered -> {
                    foundDevicesListView.visibility = View.VISIBLE
                    connectedDevicesListView.visibility = View.GONE
                    networkDevicesListView.visibility = View.GONE
                    visibleDevicesText.text = getString(R.string.discovered_devices)
                    menuItem.isChecked = true
                }
                menuItem.itemId == R.id.show_network -> {
                    foundDevicesListView.visibility = View.GONE
                    networkDevicesListView.visibility = View.VISIBLE
                    connectedDevicesListView.visibility = View.GONE
                    visibleDevicesText.text = getString(R.string.network_devices)
                    menuItem.isChecked = true
                }
            }
            true
        }
    }

    private fun appendToLogs(msg: CharSequence) {
        logsTextView.append("\n")
        logsTextView.append(DateFormat.format("hh:mm", System.currentTimeMillis()).toString() + ": ")
        logsTextView.append(msg)
    }

    override fun logV(msg: String) {
        super.logV(msg)
        appendToLogs(toColor(msg, R.color.log_verbose))
        sendNearLog(msg, LogType.INFO, "logV")
    }

    override fun logD(msg: String) {
        super.logD(msg)
        appendToLogs(toColor(msg, R.color.log_debug))
        sendNearLog(msg, LogType.DEBUG, "logD")
    }

    override fun logW(msg: String) {
        super.logW(msg)
        appendToLogs(toColor(msg, R.color.log_warning))
        sendNearLog(msg, LogType.WARNING, "logW")
    }

    override fun logE(msg: String, e: Throwable) {
        super.logE(msg, e)
        appendToLogs(toColor(msg, R.color.log_error))
        sendNearLog(e.printStackTrace().toString(), LogType.ERROR, "logE")
    }

    private fun toColor(msg: String, color: Int): CharSequence {
        val spannable = SpannableString(msg)
        spannable.setSpan(ForegroundColorSpan(color), 0, msg.length, 0)
        return spannable
    }

    fun hideLogs(view: View) {
        if (logsView!!.visibility == View.GONE) {
            logsView!!.visibility = View.VISIBLE
            hideLogsButton!!.text = getString(R.string.hideLogs)
        } else {
            logsView!!.visibility = View.GONE
            hideLogsButton!!.text = getString(R.string.showLogs)
        }
    }

    private fun startOrStopAdvertising() {
        if (isAdvertising) {
            stopAdvertising()
        } else {
            startAdvertising()
        }
    }

    private fun startOrStopDiscovering() {
        if (isDiscovering) {
            stopDiscovering()
        } else {
            startDiscovering()
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
        mDiscoveredDevicesAdapter.addDevice(endpoint)
        logD("Endpoint discoverd $endpoint.name")
        connectToEndpoint(endpoint)
    }

    override fun onConnectionInitiated(endpoint: Endpoint, connectionInfo: ConnectionInfo) {
        // A connection to another device has been initiated! We'll accept the connection immediately.
        acceptConnection(endpoint)
    }

    private fun onChatStarted(endpoint: Endpoint) {
        Toast.makeText(
                this, getString(R.string.chat_with, endpoint.name), Toast.LENGTH_SHORT)
                .show()

        devicesView.visibility = View.GONE
        chatView.visibility = View.VISIBLE
        chatInput.visibility = View.GONE

        loadingBar.visibility = View.VISIBLE
        waitingToAccept.visibility = View.VISIBLE
        visibleDevicesText.visibility = View.GONE

        chattingUser = MemberData(endpoint.name, getRandomColor(), endpoint.id)

        sendPayload(NearPayloadType.REQUEST_CHAT.toString(), "", endpoint.id, currentData!!.id)
    }

    override fun onEndpointConnected(endpoint: Endpoint?) {
        mConnectedDevicesAdapter.addDevice(endpoint!!)
        Toast.makeText(
                this, getString(R.string.toast_connected, endpoint.name), Toast.LENGTH_SHORT)
                .show()
    }

    override fun onEndpointDisconnected(endpoint: Endpoint) {
        super.onEndpointDisconnected(endpoint)
        Toast.makeText(
                this, getString(R.string.toast_disconnected, endpoint.name), Toast.LENGTH_SHORT)
                .show()

        mConnectedDevicesAdapter.removeDevice(endpoint)
        mDiscoveredDevicesAdapter.removeDevice(endpoint)

        if (mIsChatActive && endpoint.id == chattingUser!!.id!!) {
            deniChat()
        }
    }

    override fun onEndpointDiscoverLost(endpoint: Endpoint?) {
        if (endpoint != null) {
            mDiscoveredDevicesAdapter.removeDevice(endpoint)
        }

    }

    override fun onDiscoveryStarted() {
        Toast.makeText(
                this, "Discovery started!", Toast.LENGTH_SHORT)
                .show()
        logD("Discovery started")
    }

    override fun onConnectionFailed(endpoint: Endpoint) {
        // Let's try someone else.
        if (getState() == State.SEARCHING) {
            startDiscovering()
        }
    }

    override fun onDiscoveryFailed() {
        changeDiscoverBtnText()
        Toast.makeText(
                this, "Discovery failed!", Toast.LENGTH_SHORT)
                .show()
        logW("Discovery failed")
    }

    override fun onAdvertisingFailed() {
        changeAdvertiseBtnText()
        Toast.makeText(
                this, "Advertising failed!", Toast.LENGTH_SHORT)
                .show()
        logW("Advertising failed")
    }

    override fun onAdvertisingStarted() {
        Toast.makeText(
                this, "Advertising started!", Toast.LENGTH_SHORT)
                .show()
    }

    override fun onSourceSet(endpoint: Endpoint?) {
        if (endpoint != null) {
            currentData!!.id = endpoint.id
            setSourceToNearLogs(endpoint)
        }
    }

    override fun onPause() {
        super.onPause()
        stopAdvertising()
        stopDiscovering()
        stopAllEndpoints()
        clearGraphToNearLogs()
        setPayloadHistoryToNearLogs()
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
            mDiscoverMenuItem.title = getString(R.string.stop_discovering_lower)
        } else {
            mDiscoverMenuItem.title = getString(R.string.discover_lower)
        }
    }

    private fun changeAdvertiseBtnText() {
        if (isAdvertising) {
            mAdvertiseMenuItem.title = getString(R.string.stop_advertising_lower)
        } else {
            mAdvertiseMenuItem.title = getString(R.string.advertise_lower)
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
            else -> {
            }
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

    override fun onReceiveCommand(type: String?, data: String?, endpoint: Endpoint?) {
        addPayloadToNearLogs(PayloadType.INCOMING, endpoint!!.name, type!!, data!!)
    }

    override fun onSendPayload(type: String?, data: String?, endpointId: String?) {
        val endpoint = mNetworkDevicesAdapter.getEndpoint(endpointId!!)
        if (endpoint != null) {
            addPayloadToNearLogs(PayloadType.OUTGOING, endpoint.name, type!!, data!!)
        } else {
            addPayloadToNearLogs(PayloadType.OUTGOING, endpointId, type!!, data!!)
        }
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
        val message = messageInputText!!.text.toString()
        if (!message.isEmpty()) {
            messageInputText!!.text.clear()

            mMessageAdapter.add(Message(message, currentData, true))
            val count = messagesListView.count
            messagesListView.setSelection(count - 1)
            sendPayload(NearPayloadType.CHAT_MESSAGE.toString(), message, chattingUser!!.id!!, currentData!!.id)
        }
    }

    fun closeChat(view: View) {
        deniChat()

        sendPayload(NearPayloadType.DISCONNECT_CHAT.toString(), "", chattingUser!!.id!!, currentData!!.id)
    }

    override fun onReceive(endpoint: Endpoint, command: Command?) {
        if (command != null) {
            if (command.destinationId != null && command.destinationId != "") {
                val dest = mNetworkDevicesAdapter.getEndpoint(command.destinationId)
                if (dest != null) {
                    decisionMaking(NearPayloadType.valueOf(command.type), command.data, dest)
                } else {
                    decisionMaking(NearPayloadType.valueOf(command.type), command.data, endpoint)
                }
            } else {
                decisionMaking(NearPayloadType.valueOf(command.type), command.data, endpoint)
            }
        }
    }

    private fun getRandomColor(): String {
        val r = Random()
        val sb = StringBuffer("#")
        while (sb.length < 7) {
            sb.append(Integer.toHexString(r.nextInt()))
        }
        return sb.toString().substring(0, 7)
    }

    private fun createAlertDialog(fromEndpoint: Endpoint) {
        val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        } else {
            AlertDialog.Builder(this)
        }
        builder.setTitle("Connection requested")
                .setMessage("Accept chatting request from ${fromEndpoint.name}?")
                .setPositiveButton(android.R.string.yes, { _, _ ->
                    sendPayload(NearPayloadType.ACCEPT_CHAT.toString(), "", fromEndpoint.id, currentData!!.id)
                    acceptChat(fromEndpoint)
                })
                .setNegativeButton(android.R.string.no, { _, _ ->
                    sendPayload(NearPayloadType.DENI_CHAT.toString(), "", fromEndpoint.id, currentData!!.id)
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show()
    }

    private fun decisionMaking(type: NearPayloadType, data: String, fromEndpoint: Endpoint) {
        when (type) {
            NearPayloadType.REQUEST_CHAT -> {
                createAlertDialog(fromEndpoint)
            }

            NearPayloadType.ACCEPT_CHAT -> {
                acceptChat(fromEndpoint)
            }

            NearPayloadType.DENI_CHAT -> {
                deniChat()
            }

            NearPayloadType.DISCONNECT_CHAT -> {
                deniChat()
            }

            NearPayloadType.CHAT_MESSAGE -> {
                addChatMessage(data)
            }

            NearPayloadType.UPDATE_GRAPH -> {
            }
        }
    }

    private fun acceptChat(fromEndpoint: Endpoint) {
        mIsChatActive = true

        devicesView.visibility = View.GONE
        chatView.visibility = View.VISIBLE
        chatInput.visibility = View.VISIBLE

        loadingBar.visibility = View.GONE
        waitingToAccept.visibility = View.GONE

        visibleDevicesText.visibility = View.GONE
        chattingUser = MemberData(fromEndpoint.name, getRandomColor(), fromEndpoint.id)
    }

    private fun deniChat() {
        mIsChatActive = false

        mMessageAdapter.clear()
        devicesView.visibility = View.VISIBLE
        chatView.visibility = View.GONE

        visibleDevicesText.visibility = View.VISIBLE

        loadingBar.visibility = View.GONE
        waitingToAccept.visibility = View.GONE
        Toast.makeText(
                this, "Chat denied!", Toast.LENGTH_SHORT)
                .show()
    }

    private fun addChatMessage(message: String) {
        mMessageAdapter.add(Message(message, chattingUser, false))
        val count = messagesListView.count
        messagesListView.setSelection(count - 1)
    }


    private fun makeLoginToNearLogs() {
        mAPIService.makeLogin(NearLogsUser("admin", "admin")).enqueue(object : Callback<AuthorizationToken> {
            override fun onResponse(call: Call<AuthorizationToken>, response: Response<AuthorizationToken>) {
                if (response.isSuccessful) {
                    logD("Logged at near logs!")
                    authorizationToken = response.body()!!
                    sendAllLogs()
                }
            }

            override fun onFailure(call: Call<AuthorizationToken>, t: Throwable) {
                logD("Login failed!")
            }
        })
    }

    override fun onNetworkEndpointAdded(source: Endpoint, destination: Endpoint) {
        mNetworkDevicesAdapter.addDevice(destination)
        addEdgeToNearLogs(source, destination)
    }

    override fun onNetworkEndpointRemoved(source: Endpoint, destination: Endpoint) {
        mNetworkDevicesAdapter.removeDevice(destination)
        removeNodeToNearLogs(destination)
    }

    private fun sendAllLogs() {
        for (log in logs) {
            addLogToNearLogs(log)
        }
    }

    private fun sendNearLog(message: String, type: LogType, origin: String) {
        val log = LogDto(df.format(Date()), type, origin, message,
                SessionDto(mSession, "None"),
                DeviceDto(mName))
        if (this::authorizationToken.isInitialized) {
            addLogToNearLogs(log)
        } else {
            logs.add(log)
        }
    }

    private fun addLogToNearLogs(log: LogDto) {
        if (this::authorizationToken.isInitialized) {
            mAPIService.addLog(authorizationToken.token, log).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    appendToLogs("Log send failed!")
                }
            })
        }
    }

    private fun setSourceToNearLogs(endpoint: Endpoint) {
        if (this::authorizationToken.isInitialized) {
            mAPIService.setSource(authorizationToken.token, NodeDto(endpoint.name, endpoint.id, name, mSession)).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    appendToLogs("Set source failed!")
                }
            })
        }
    }

    private fun addEdgeToNearLogs(source: Endpoint, destination: Endpoint) {
        if (this::authorizationToken.isInitialized) {
            mAPIService.addEdge(authorizationToken.token, EdgeWithNodesDto(
                    NodeDto(source.name, source.id, name, mSession),
                    NodeDto(destination.name, destination.id, name, mSession),
                    name, mSession
            )).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    appendToLogs("Add edge failed!")
                }
            })
        }
    }

    private fun removeNodeToNearLogs(endpoint: Endpoint) {
        if (this::authorizationToken.isInitialized) {
            mAPIService.removeNode(authorizationToken.token, NodeDto(endpoint.name, endpoint.id, name, mSession)).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    appendToLogs("Remove node failed!")
                }
            })
        }
    }

    private fun clearGraphToNearLogs() {
        if (this::authorizationToken.isInitialized) {
            mAPIService.clearGraph(authorizationToken.token, name, mSession).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    appendToLogs("Clear graph failed!")
                }
            })
        }
    }

    private fun addPayloadToNearLogs(type: PayloadType, destination: String, commandType: String, data: String) {
        if (this::authorizationToken.isInitialized) {
            mAPIService.addPayload(authorizationToken.token, PayloadDto(type, destination, commandType, data, name, mSession)).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    appendToLogs("Send payload failed!")
                }
            })
        }
    }

    private fun setPayloadHistoryToNearLogs() {
        if (this::authorizationToken.isInitialized) {
            mAPIService.setHistory(authorizationToken.token, name, mSession).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    appendToLogs("Set payload history failed!")
                }
            })
        }
    }
}
