package com.mtoader.near.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.mtoader.near.R
import com.mtoader.near.model.Endpoint
import java.util.ArrayList

class DevicesAdapter(private val context: Context? = null, private val listOfDevices: ArrayList<Endpoint> = ArrayList()) : BaseAdapter() {
    private class ViewHolder(row: View?) {
        var txtName: TextView? = null

        init {
            this.txtName = row?.findViewById(R.id.txtName)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?
        val viewHolder: ViewHolder
        if (convertView == null) {
            val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.device_ticket, null)
            viewHolder = ViewHolder(view)
            view?.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        viewHolder.txtName?.text = listOfDevices[position].name

        return view as View
    }

    override fun getItem(p0: Int): Any {
        return listOfDevices[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return listOfDevices.size
    }
}