package com.ahmed.localai

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import kotlin.math.roundToInt

class ModelsActivity : AppCompatActivity() {
    private lateinit var adapter: ModelsAdapter
    private lateinit var allModels: List<ModelItem>
    private lateinit var etSearch: EditText
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_models)

        etSearch = findViewById(R.id.etSearch)
        progressBar = findViewById(R.id.progressBar)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadModelsFromHuggingFace(recyclerView)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                val filtered = allModels.filter { it.name.lowercase().contains(query) }
                adapter.updateList(filtered)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadModelsFromHuggingFace(recyclerView: RecyclerView) {
        progressBar.visibility = View.VISIBLE
        Thread {
            try {
                val client = OkHttpClient()
                // بنجيب موديلات GGUF مرتّبة حسب التحميلات
                val request = Request.Builder()
                    .url("https://huggingface.co/api/models?filter=gguf&sort=downloads&limit=100")
                    .build()
                val response = client.newCall(request).execute()
                val json = response.body?.string() ?: "[]"
                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val rawList: List<Map<String, Any>> = Gson().fromJson(json, type)

                val modelList = mutableListOf<ModelItem>()
                for (map in rawList) {
                    val id = map["id"] as? String ?: continue
                    val downloads = (map["downloads"] as? Double)?.toInt() ?: 0
                    
                    // بنستخرج اسم الملف اللي جواه
                    val siblings = map["siblings"] as? List<Map<String, String>>
                    val ggufFile = siblings?.find { it["rfilename"]?.endsWith(".gguf") == true }
                    
                    if (ggufFile != null) {
                        val filename = ggufFile["rfilename"]!!
                        // تخمين الحجم من اسم الملف (لو فيه رقم زي 1.5B يبقى تقريباً 1.5GB)
                        val sizeGb = extractSizeFromName(filename)
                        val requiredRam = (sizeGb * 1.2).roundToInt().toDouble() // الرام المطلوبة أعلى من الحجم شوية
                        
                        val downloadUrl = "https://huggingface.co/$id/resolve/main/$filename"
                        
                        modelList.add(
                            ModelItem(
                                name = id,
                                description = "الملف: $filename\nتحميلات: $downloads",
                                requiredRam = requiredRam,
                                sizeGb = sizeGb,
                                category = "chat",
                                downloadUrl = downloadUrl,
                                iconUrl = "https://huggingface.co/front/assets/huggingface_logo-noborder.svg"
                            )
                        )
                    }
                }

                allModels = modelList
                runOnUiThread {
                    adapter = ModelsAdapter(allModels, 24) // بنفترض 24 جيجا رام كحد أقصى للتجربة
                    recyclerView.adapter = adapter
                    progressBar.visibility = View.GONE
                    etSearch.isEnabled = true
                }
            } catch (e: IOException) {
                runOnUiThread { progressBar.visibility = View.GONE }
            }
        }.start()
    }

    private fun extractSizeFromName(filename: String): Double {
        // بنبحث عن أرقام زي 0.5 أو 1.5 أو 7 أو 14
        val regex = "(\\d+\\.?\\d*)[Bb]".toRegex()
        val match = regex.find(filename)
        return if (match != null) {
            match.groupValues[1].toDouble()
        } else {
            2.0 // قيمة افتراضية لو مفيش رقم
        }
    }
}

data class ModelItem(
    val name: String,
    val description: String,
    val requiredRam: Double,
    val sizeGb: Double,
    val category: String,
    val downloadUrl: String,
    val iconUrl: String
)
