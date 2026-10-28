package com.ahmed.localai

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.tvMessage)
        val container: LinearLayout = view.findViewById(R.id.msgContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg = messages[position]
        holder.container.removeAllViews()
        holder.messageText.visibility = View.GONE

        // لو فيه تفكير، نضيف مربع قابل للطي
        if (msg.thinking.isNotEmpty() && !msg.isUser) {
            val ctx = holder.itemView.context
            val thinkBox = TextView(ctx).apply {
                text = "🧠 التفكير (اضغط للطي/الفك)"
                setTextColor(Color.parseColor("#94a3b8"))
                textSize = 13f
                setPadding(20, 12, 20, 12)
                setBackgroundColor(Color.parseColor("#1e293b"))
                setTypeface(null, Typeface.BOLD)
            }
            
            val thinkContent = TextView(ctx).apply {
                text = msg.thinking
                setTextColor(Color.parseColor("#64748b"))
                textSize = 12f
                setPadding(20, 8, 20, 12)
                setBackgroundColor(Color.parseColor("#0f172a"))
                visibility = View.GONE
                typeface = Typeface.MONOSPACE
            }
            
            thinkBox.setOnClickListener {
                thinkContent.visibility = if (thinkContent.visibility == View.GONE) View.VISIBLE else View.GONE
                thinkBox.text = if (thinkContent.visibility == View.VISIBLE) "🧠 التفكير (اضغط للطي)" else "🧠 التفكير (اضغط للفك)"
            }
            
            holder.container.addView(thinkBox)
            holder.container.addView(thinkContent)
        }

        // نص الرسالة
        val txt = TextView(holder.itemView.context).apply {
            text = msg.text
            setPadding(24, 16, 24, 16)
            textSize = 15f
            if (msg.isUser) {
                setBackgroundColor(Color.parseColor("#3b82f6"))
                setTextColor(Color.WHITE)
            } else {
                setBackgroundColor(Color.parseColor("#1e293b"))
                setTextColor(Color.parseColor("#e2e8f0"))
            }
        }
        holder.container.addView(txt)

        holder.container.layoutDirection = if (msg.isUser) View.LAYOUT_DIRECTION_LTR else View.LAYOUT_DIRECTION_RTL
    }

    override fun getItemCount() = messages.size
}
