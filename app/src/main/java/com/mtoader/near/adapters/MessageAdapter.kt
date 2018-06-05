package com.mtoader.near.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.mtoader.near.R
import com.mtoader.near.model.message.Message
import com.mtoader.near.model.message.MessageViewHolder

class MessageAdapter(private var context: Context) : BaseAdapter() {

    private var messages: MutableList<Message> = ArrayList()

    fun clear() {
        messages.clear()
        notifyDataSetChanged()
    }

    fun add(message: Message) {
        this.messages.add(message)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return messages.size
    }

    override fun getItem(i: Int): Any {
        return messages[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, convertView: View?, viewGroup: ViewGroup): View {
        val newView: View
        val holder = MessageViewHolder()
        val messageInflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val message = messages[i]

        if (message.isBelongsToCurrentUser) {
            newView = messageInflater.inflate(R.layout.my_message, null)
            holder.messageBody = newView.findViewById(R.id.message_body)
            newView.tag = holder
            holder.messageBody!!.text = message.text
        } else {
            newView = messageInflater.inflate(R.layout.their_message, null)
            holder.name = newView.findViewById(R.id.name)
            holder.messageBody = newView.findViewById(R.id.message_body)
            newView.tag = holder

            holder.name!!.text = message.data!!.name
            holder.messageBody!!.text = message.text
        }

        return newView
    }

}