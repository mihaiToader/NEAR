package com.mtoader.near

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_device_name.*
import android.content.SharedPreferences




class DeviceNameActivity : AppCompatActivity() {

    private var debugFields: Boolean = false
    private val MY_PREFS_NAME = "MyPrefsFile"

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_name)

        sessionName.visibility = View.GONE
        loggingAppAddress.visibility = View.GONE

        sharedPreferences = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)

        deviceNameInput.setText(sharedPreferences.getString("deviceName", ""))
        sessionName.setText(sharedPreferences.getString("sessionName", ""))
        loggingAppAddress.setText(sharedPreferences.getString("loggingAddressApp", ""))

        nearStartIcon.setOnClickListener({
            if (debugFields) {
                sessionName.visibility = View.GONE
                loggingAppAddress.visibility = View.GONE
            } else {
                sessionName.visibility = View.VISIBLE
                loggingAppAddress.visibility = View.VISIBLE
            }
            debugFields = !debugFields
        })
    }

    fun chooseName(view: View) {
        val myIntent = Intent(baseContext, MainActivity::class.java)
        myIntent.putExtra("deviceName", deviceNameInput.text.toString())
        myIntent.putExtra("sessionName", sessionName.text.toString())
        myIntent.putExtra("loggingAddressApp", loggingAppAddress.text.toString())

        val editor = sharedPreferences.edit()
        editor.putString("deviceName", deviceNameInput.text.toString())
        editor.putString("sessionName", sessionName.text.toString())
        editor.putString("loggingAddressApp", loggingAppAddress.text.toString())
        editor.apply()

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
