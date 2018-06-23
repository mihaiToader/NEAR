package com.mtoader.near

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText


class DeviceNameActivity : AppCompatActivity() {

    private var deviceName: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_name)

        deviceName = findViewById(R.id.deviceNameInput)
    }

    fun chooseName(view: View) {
        val myIntent = Intent(baseContext, MainActivity::class.java)
        myIntent.putExtra("deviceName", deviceName!!.text.toString()) //Optional parameters

        this@DeviceNameActivity.startActivity(myIntent)
    }

    override fun onBackPressed() {
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

}
