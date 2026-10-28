package com.ahmed.localai

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

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
        holder.name.text = model.name
        holder.specs.text = "${model.description}\nRAM: ${model.requiredRam}GB | Size: ${model.sizeGb}GB"

        val iconRes = when (model.category) {
            "chat" -> android.R.drawable.ic_menu_edit
            "vision" -> android.R.drawable.ic_menu_camera
            "image" -> android.R.drawable.ic_menu_gallery
            else -> android.R.drawable.ic_menu_info_details
        }
        holder.icon.setImageResource(iconRes)
        holder.icon.setColorFilter(0xFF60a5fa.toInt())

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
            val dm = holder.itemView.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val fileName = model.downloadUrl.substringAfterLast("/")
            
            // التأكد إن الملف مش متحمل قبل كده
            val file = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "AHMED_AI/$fileName")
            if (file.exists()) {
                Toast.makeText(holder.itemView.context, "الملف موجود بالفعل!", Toast.LENGTH_SHORT).show()
                holder.btn.text = "تم التنزيل ✓"
                holder.btn.setBackgroundColor(0xFF10b981.toInt())
                return@setOnClickListener
            }

            val request = DownloadManager.Request(Uri.parse(model.downloadUrl))
                .setTitle("AHMED AI: ${model.name}")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "AHMED_AI/$fileName")
                .setAllowedOverMetered(true)

            val downloadId = dm.enqueue(request)
            Toast.makeText(holder.itemView.context, "بدأ التنزيل...", Toast.LENGTH_LONG).show()
            holder.btn.isEnabled = false

            // متابعة النسبة المئوية
            val handler = Handler(Looper.getMainLooper())
            val runnable = object : Runnable {
                override fun run() {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = dm.query(query)
                    if (cursor.moveToFirst()) {
                        val bytesDownloaded = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val bytesTotal = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            holder.btn.text = "تم التنزيل ✓"
                            holder.btn.setBackgroundColor(0xFF10b981.toInt())
                        } else if (status == DownloadManager.STATUS_FAILED) {
                            holder.btn.text = "فشل التنزيل"
                            holder.btn.setBackgroundColor(0xFFef4444.toInt())
                            holder.btn.isEnabled = true
                        } else {
                            if (bytesTotal > 0) {
                                val progress = (bytesDownloaded * 100 / bytesTotal)
                                holder.btn.text = "$progress%"
                            } else {
                                holder.btn.text = "جاري التنزيل..."
                            }
                            handler.postDelayed(this, 1000) // تحديث كل ثانية
                        }
                    }
                    cursor.close()
                }
            }
            handler.post(runnable)
        }
    }

    override fun getItemCount() = models.size

    fun updateList(newModels: List<ModelItem>) {
        models = newModels
        notifyDataSetChanged()
    }
}
