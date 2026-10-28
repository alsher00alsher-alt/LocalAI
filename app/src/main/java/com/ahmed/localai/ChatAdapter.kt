package com.ahmed.localai

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.tvMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg = messages[position]
        holder.messageText.text = msg.text
        if (msg.isUser) {
            holder.messageText.setBackgroundColor(Color.parseColor("#3b82f6")) // أزرق
            holder.itemView.layoutDirection = View.LAYOUT_DIRECTION_LTR // المستخدم على اليسار
        } else {
            holder.messageText.setBackgroundColor(Color.parseColor("#1e293b")) // رمادي
            holder.itemView.layoutDirection = View.LAYOUT_DIRECTION_RTL // البوت على اليمين
        }
    }

    override fun getItemCount() = messages.size
}
