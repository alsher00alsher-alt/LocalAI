package com.ahmed.localai

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ModelsAdapter(private val models: List<ModelItem>, private val deviceRam: Long) : RecyclerView.Adapter<ModelsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.modelIcon)
        val name: TextView = view.findViewById(R.id.modelName)
        val specs: TextView = view.findViewById(R.id.modelSpecs)
        val btn: Button = view.findViewById(R.id.btnDownload)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_model, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = models[position]
        holder.name.text = model.name
        holder.specs.text = "${model.requiredRam}GB RAM - ${model.sizeGb}GB Size"

        // تحميل أيقونة النموذج
        Glide.with(holder.itemView.context).load(model.iconUrl).into(holder.icon)

        // فحص إمكانيات الجهاز
        if (model.requiredRam > deviceRam) {
            holder.btn.isEnabled = false
            holder.btn.text = "غير متوافق"
            holder.btn.setBackgroundColor(0xFFef4444.toInt())
            holder.specs.setTextColor(0xFFef4444.toInt())
        } else {
            holder.btn.isEnabled = true
            holder.btn.text = "تنزيل"
            holder.btn.setBackgroundColor(0xFF3b82f6.toInt())
            holder.specs.setTextColor(0xFF10b981.toInt())
        }

        // زرار التنزيل
        holder.btn.setOnClickListener {
            startDownload(holder.itemView.context, model)
            Toast.makeText(holder.itemView.context, "بدأ تنزيل ${model.name} في الخلفية...", Toast.LENGTH_LONG).show()
            holder.btn.text = "جاري التنزيل..."
            holder.btn.isEnabled = false
        }
    }

    override fun getItemCount() = models.size

    private fun startDownload(context: Context, model: ModelItem) {
        val fileName = "${model.name.replace(" ", "_")}.gguf"
        val request = DownloadManager.Request(Uri.parse(model.downloadUrl))
            .setTitle("تنزيل ${model.name}")
            .setDescription("جاري تحميل النموذج...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }
}
