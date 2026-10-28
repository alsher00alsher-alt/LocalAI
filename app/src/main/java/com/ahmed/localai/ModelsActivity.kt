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
                val request = Request.Builder()
                    .url("https://huggingface.co/api/models?filter=gguf&sort=downloads&limit=1000")
                    .build()
                val response = client.newCall(request).execute()
                val json = response.body?.string() ?: "[]"
                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val rawList: List<Map<String, Any>> = Gson().fromJson(json, type)

                allModels = rawList.map { map ->
                    val id = map["id"] as? String ?: "Unknown"
                    val downloads = (map["downloads"] as? Double)?.toInt() ?: 0
                    ModelItem(id, "تحميلات: $downloads", 0.0, 0.0, "chat", "https://huggingface.co/$id", "")
                }

                runOnUiThread {
                    adapter = ModelsAdapter(allModels, 24)
                    recyclerView.adapter = adapter
                    progressBar.visibility = View.GONE
                }
            } catch (e: IOException) {
                runOnUiThread { progressBar.visibility = View.GONE }
            }
        }.start()
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
