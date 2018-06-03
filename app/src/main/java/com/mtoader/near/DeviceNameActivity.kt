package com.mtoader.near

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText


class DeviceNameActivity : AppCompatActivity() {

    var deviceName: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_name)

        deviceName = this.findViewById(R.id.deviceNameInput)
    }

    fun chooseName(view: View) {
        val myIntent = Intent(baseContext, MainActivity::class.java)
        myIntent.putExtra("deviceName", deviceName!!.text.toString()) //Optional parameters

        this@DeviceNameActivity.startActivity(myIntent)
    }

}
