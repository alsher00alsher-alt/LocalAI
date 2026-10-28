package com.ahmed.localai

import android.content.Context
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
import com.khushpanchal.ketch.Ketch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        // أيقونة النموذج
        Glide.with(holder.itemView.context).load(model.iconUrl).into(holder.icon)

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
            startDownload(holder.itemView.context, model, holder)
        }
    }

    override fun getItemCount() = models.size

    fun updateList(newModels: List<ModelItem>) {
        models = newModels
        notifyDataSetChanged()
    }

    private fun startDownload(context: Context, model: ModelItem, holder: ViewHolder) {
        val ketch = Ketch.builder().build(context)
        val fileName = "${model.name.replace(" ", "_")}.${if (model.category == "image") "safetensors" else "gguf"}"
        val path = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/AHMED_AI/"
        
        val id = ketch.download(model.downloadUrl, fileName, path)
        
        Toast.makeText(context, "بدأ تنزيل ${model.name}...", Toast.LENGTH_LONG).show()
        holder.btn.text = "جاري التنزيل..."
        holder.btn.isEnabled = false

        // متابعة التحميل لايف وتحديث النسبة
        CoroutineScope(Dispatchers.Main).launch {
            ketch.observeDownloadById(id).collect { downloadModel ->
                if (downloadModel != null) {
                    val progress = downloadModel.progress
                    holder.btn.text = "$progress%"
                    if (progress >= 100) {
                        holder.btn.text = "مكتمل ✓"
                        holder.btn.setBackgroundColor(0xFF10b981.toInt())
                        Toast.makeText(context, "${model.name} تم تحميله بنجاح!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
