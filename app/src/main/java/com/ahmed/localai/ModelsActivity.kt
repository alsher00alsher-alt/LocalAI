package com.ahmed.localai

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ModelsActivity : AppCompatActivity() {
    private lateinit var adapter: ModelsAdapter
    private lateinit var allModels: List<ModelItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_models)

        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalRamGB = memoryInfo.totalMem / (1024 * 1024 * 1024)

        // قائمة نماذج حقيقية مفتوحة المصدر (GGUF)
        allModels = listOf(
            // شات
            ModelItem("Qwen 2.5 0.5B", "شات خفيف جداً", 0.5, 0.4, "chat", "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf", "https://avatars.githubusercontent.com/u/141221163?s=200&v=4"),
            ModelItem("Llama 3.2 1B", "شات متوازن من Meta", 1.0, 0.8, "chat", "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf", "https://avatars.githubusercontent.com/u/153662371?s=200&v=4"),
            ModelItem("Gemma 2 2B", "شات ذكي من Google", 2.0, 1.6, "chat", "https://huggingface.co/bartowski/gemma-2-2b-it-GGUF/resolve/main/gemma-2-2b-it-Q4_K_M.gguf", "https://avatars.githubusercontent.com/u/153662371?s=200&v=4"),
            ModelItem("Phi-3 Mini 3.8B", "شات قوي من Microsoft", 3.8, 2.4, "chat", "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q4.gguf", "https://avatars.githubusercontent.com/u/6154722?s=200&v=4"),
            ModelItem("Qwen 3.5 4B", "شات متقدم", 4.0, 2.8, "chat", "https://huggingface.co/Qwen/Qwen3.5-4B-Instruct-GGUF/resolve/main/qwen3.5-4b-instruct-q4_k_m.gguf", "https://avatars.githubusercontent.com/u/141221163?s=200&v=4"),
            
            // رؤية
            ModelItem("SmolVLM 500M", "تحليل صور خفيف", 0.8, 0.5, "vision", "https://huggingface.co/ggml-org/SmolVLM-500M-Instruct-GGUF/resolve/main/SmolVLM-500M-Instruct-Q8_0.gguf", "https://avatars.githubusercontent.com/u/153662371?s=200&v=4"),
            ModelItem("SmolVLM 2.2B", "تحليل صور دقيق", 2.5, 1.8, "vision", "https://huggingface.co/ggml-org/SmolVLM-2.2B-Instruct-GGUF/resolve/main/SmolVLM-2.2B-Instruct-Q4_K_M.gguf", "https://avatars.githubusercontent.com/u/153662371?s=200&v=4"),
            ModelItem("Qwen 3.5 Vision 4B", "رؤية متقدمة", 4.0, 3.0, "vision", "https://huggingface.co/Qwen/Qwen3.5-VL-4B-Instruct-GGUF/resolve/main/qwen3.5-vl-4b-instruct-q4_k_m.gguf", "https://avatars.githubusercontent.com/u/141221163?s=200&v=4"),
            
            // صور
            ModelItem("Stable Diffusion 1.5", "توليد صور فني", 4.0, 2.0, "image", "https://huggingface.co/stable-diffusion-v1-5/stable-diffusion-v1-5/resolve/main/v1-5-pruned-emaonly.safetensors", "https://avatars.githubusercontent.com/u/153662371?s=200&v=4"),
            ModelItem("Anything V5", "توليد صور أنمي", 3.5, 2.0, "image", "https://huggingface.co/stablediffusionapi/anything-v5/resolve/main/anything-v5.safetensors", "https://avatars.githubusercontent.com/u/153662371?s=200&v=4")
        )

        adapter = ModelsAdapter(allModels, totalRamGB)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btnAll).setOnClickListener { filterModels("all") }
        findViewById<Button>(R.id.btnChat).setOnClickListener { filterModels("chat") }
        findViewById<Button>(R.id.btnVision).setOnClickListener { filterModels("vision") }
        findViewById<Button>(R.id.btnImage).setOnClickListener { filterModels("image") }
    }

    private fun filterModels(category: String) {
        val filtered = if (category == "all") allModels else allModels.filter { it.category == category }
        adapter.updateList(filtered)
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
