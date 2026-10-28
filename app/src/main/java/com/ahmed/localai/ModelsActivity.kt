package com.ahmed.localai

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ModelsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_models)

        val deviceInfo = findViewById<TextView>(R.id.deviceInfo)
        
        // فحص الرامات المتاحة
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalRamGB = memoryInfo.totalMem / (1024 * 1024 * 1024)
        
        deviceInfo.text = "ذاكرة الجهاز: ${totalRamGB}GB | النماذج المناسبة لك تظهر باللون الأخضر"

        // قائمة النماذج الافتراضية
        val models = listOf(
            ModelItem("Qwen 2.5 0.5B", "نموذج خفيف جداً وسريع", 0.5, 0.5, "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf", "https://raw.githubusercontent.com/QwenLM/Qwen/main/assets/logo.png"),
            ModelItem("Llama 3.2 1B", "نموذج متوازن وسريع", 1.0, 1.0, "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf", "https://raw.githubusercontent.com/meta-llama/llama-models/main/assets/logo.png"),
            ModelItem("Gemma 2 2B", "نموذج جوجل المحسن", 2.0, 2.0, "https://huggingface.co/bartowski/gemma-2-2b-it-GGUF/resolve/main/gemma-2-2b-it-Q4_K_M.gguf", "https://www.gstatic.com/lamda/images/gemini_sparkle_v002_d4735304ff6292a690345.svg"),
            ModelItem("Phi-3 Mini 3.8B", "نموذج مايكروسوفت الذكي", 3.8, 3.0, "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q4.gguf", "https://upload.wikimedia.org/wikipedia/commons/9/96/Microsoft_logo_%282012%29.svg")
        )

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ModelsAdapter(models, totalRamGB)
    }
}

data class ModelItem(
    val name: String,
    val description: String,
    val requiredRam: Double,
    val sizeGb: Double,
    val downloadUrl: String,
    val iconUrl: String
)
