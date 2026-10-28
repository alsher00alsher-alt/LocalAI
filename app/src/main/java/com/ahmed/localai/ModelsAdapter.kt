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

class ModelsAdapter(private var models: List<ModelItem>, private val deviceRam: Long) : RecyclerView.Adapter<ModelsAdapter.ViewHolder>() {

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
        
        // تنظيف اسم الموديل عشان يبان حلو
        val displayName = model.name.substringAfter("/")
        holder.name.text = displayName
        holder.specs.text = "الحجم: ${model.sizeGb}GB | الرام: ${model.requiredRam}GB\n${model.description}"
        
        // تحميل الأيقونة
        Glide.with(holder.itemView.context)
            .load(model.iconUrl)
            .circleCrop()
            .into(holder.icon)

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

        holder.btn.setOnClickListener {
            startDownload(holder.itemView.context, model)
            Toast.makeText(holder.itemView.context, "بدأ التنزيل... تابع من الإشعارات", Toast.LENGTH_LONG).show()
            holder.btn.text = "جاري التنزيل..."
            holder.btn.isEnabled = false
        }
    }

    override fun getItemCount() = models.size

    fun updateList(newModels: List<ModelItem>) {
        models = newModels
        notifyDataSetChanged()
    }

    private fun startDownload(context: Context, model: ModelItem) {
        val fileName = model.downloadUrl.substringAfterLast("/")
        val folder = "AHMED_AI"
        
        val request = DownloadManager.Request(Uri.parse(model.downloadUrl))
            .setTitle("AHMED AI: $fileName")
            .setDescription("جاري التحميل...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$folder/$fileName")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }
}
