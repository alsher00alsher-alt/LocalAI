package com.ahmed.localai

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ModelsActivity : AppCompatActivity() {
    private lateinit var adapter: ModelsAdapter
    private lateinit var allModels: List<ModelItem>
    private lateinit var etSearch: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_models)

        etSearch = findViewById(R.id.etSearch)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val ramGB = memInfo.totalMem / (1024 * 1024 * 1024)

        allModels = listOf(
            // ============ نماذج ديب سيك (DeepSeek) ============
            ModelItem("DeepSeek R1 1.5B (Abliterated)", "شات غير مفلتر - خفيف", 2.0, 1.8, "chat", "https://huggingface.co/huihui-ai/DeepSeek-R1-Distill-Qwen-1.5B-abliterated-GGUF/resolve/main/DeepSeek-R1-Distill-Qwen-1.5B-abliterated.Q4_K_M.gguf"),
            ModelItem("DeepSeek R1 7B (Abliterated)", "شات غير مفلتر - تفكير عميق", 6.0, 4.4, "chat", "https://huggingface.co/huihui-ai/DeepSeek-R1-Distill-Qwen-7B-abliterated-GGUF/resolve/main/DeepSeek-R1-Distill-Qwen-7B-abliterated.Q4_K_M.gguf"),
            ModelItem("DeepSeek R1 8B Llama (Abliterated)", "شات غير مفلتر - نسخة Llama", 6.5, 4.9, "chat", "https://huggingface.co/huihui-ai/DeepSeek-R1-Distill-Llama-8B-abliterated-GGUF/resolve/main/DeepSeek-R1-Distill-Llama-8B-abliterated.Q4_K_M.gguf"),

            // ============ نماذج Qwen (غير مفلترة) ============
            ModelItem("Qwen 2.5 0.5B", "شات خفيف جداً - سريع", 1.0, 0.5, "chat", "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf"),
            ModelItem("Qwen 2.5 1.5B", "شات متوازن - سريع وذكي", 2.5, 1.1, "chat", "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf"),
            ModelItem("Qwen 2.5 3B", "شات قوي", 4.5, 2.0, "chat", "https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF/resolve/main/qwen2.5-3b-instruct-q4_k_m.gguf"),
            ModelItem("Qwen 2.5 7B", "شات متقدم - الأقوى", 9.0, 4.7, "chat", "https://huggingface.co/Qwen/Qwen2.5-7B-Instruct-GGUF/resolve/main/qwen2.5-7b-instruct-q4_k_m.gguf"),
            ModelItem("Qwen2.5-Coder 1.5B", "برمجة - خفيف", 2.5, 1.0, "chat", "https://huggingface.co/Qwen/Qwen2.5-Coder-1.5B-Instruct-GGUF/resolve/main/qwen2.5-coder-1.5b-instruct-q4_k_m.gguf"),
            ModelItem("Qwen2.5-Coder 7B", "برمجة - قوي", 9.0, 4.5, "chat", "https://huggingface.co/Qwen/Qwen2.5-Coder-7B-Instruct-GGUF/resolve/main/qwen2.5-coder-7b-instruct-q4_k_m.gguf"),
            ModelItem("Qwen3 4B (Abliterated)", "شات غير مفلتر", 5.5, 2.5, "chat", "https://huggingface.co/huihui-ai/Qwen3-4B-abliterated-GGUF/resolve/main/Qwen3-4B-abliterated.Q4_K_M.gguf"),

            // ============ نماذج Llama (غير مفلترة) ============
            ModelItem("Llama 3.2 1B", "شات سريع من Meta", 2.5, 0.8, "chat", "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf"),
            ModelItem("Llama 3.2 3B", "شات متوازن", 5.5, 2.0, "chat", "https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF/resolve/main/Llama-3.2-3B-Instruct-Q4_K_M.gguf"),
            ModelItem("Llama 3.1 8B (Abliterated)", "غير مفلتر - نسخة Llama 3.1", 9.5, 4.9, "chat", "https://huggingface.co/huihui-ai/Llama-3.1-8B-Instruct-abliterated-GGUF/resolve/main/Llama-3.1-8B-Instruct-abliterated-Q4_K_M.gguf"),

            // ============ نماذج Gemma من Google ============
            ModelItem("Gemma 2 2B", "شات ذكي من Google", 4.0, 1.7, "chat", "https://huggingface.co/bartowski/gemma-2-2b-it-GGUF/resolve/main/gemma-2-2b-it-Q4_K_M.gguf"),
            ModelItem("Gemma 2 9B (Abliterated)", "غير مفلتر - نسخة Google القوية", 11.0, 5.5, "chat", "https://huggingface.co/huihui-ai/gemma-2-9b-it-abliterated-GGUF/resolve/main/gemma-2-9b-it-abliterated-Q4_K_M.gguf"),

            // ============ نماذج Phi من Microsoft ============
            ModelItem("Phi-3 Mini 3.8B", "شات قوي من Microsoft", 6.0, 2.4, "chat", "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q4.gguf"),
            ModelItem("Phi-3.5 Mini 3.8B (Abliterated)", "غير مفلتر", 6.5, 2.4, "chat", "https://huggingface.co/huihui-ai/Phi-3.5-mini-instruct-abliterated-GGUF/resolve/main/Phi-3.5-mini-instruct-abliterated-Q4_K_M.gguf"),

            // ============ نماذج Mistral ============
            ModelItem("Mistral 7B (Abliterated)", "غير مفلتر - نسخة Mistral", 9.0, 4.4, "chat", "https://huggingface.co/huihui-ai/Mistral-7B-Instruct-v0.2-abliterated-GGUF/resolve/main/Mistral-7B-Instruct-v0.2-abliterated-Q4_K_M.gguf"),
            ModelItem("Mistral Nemo 12B", "شات متقدم", 13.0, 7.1, "chat", "https://huggingface.co/bartowski/Mistral-Nemo-Instruct-2407-GGUF/resolve/main/Mistral-Nemo-Instruct-2407-Q4_K_M.gguf"),

            // ============ نماذج Uncensored مخصصة ============
            ModelItem("Dolphin 2.9 Llama 8B", "غير مفلتر - مفتوح بالكامل", 9.5, 4.9, "chat", "https://huggingface.co/bartowski/dolphin-2.9-llama3-8b-GGUF/resolve/main/dolphin-2.9-llama3-8b-Q4_K_M.gguf"),
            ModelItem("Dolphin 2.9 Mistral 7B", "غير مفلتر - نسخة Mistral", 9.0, 4.4, "chat", "https://huggingface.co/bartowski/dolphin-2.9-mistral-7b-GGUF/resolve/main/dolphin-2.9-mistral-7b-Q4_K_M.gguf"),
            ModelItem("Nous Hermes 2 Mixtral", "غير مفلتر - قوي جداً", 15.0, 8.5, "chat", "https://huggingface.co/bartowski/Nous-Hermes-2-Mixtral-8x7B-DPO-GGUF/resolve/main/Nous-Hermes-2-Mixtral-8x7B-DPO-Q4_K_M.gguf"),

            // ============ نماذج رؤية (تحليل صور) ============
            ModelItem("SmolVLM 500M", "تحليل صور خفيف جداً", 1.5, 0.5, "vision", "https://huggingface.co/ggml-org/SmolVLM-500M-Instruct-GGUF/resolve/main/SmolVLM-500M-Instruct-Q8_0.gguf"),
            ModelItem("SmolVLM 2.2B", "تحليل صور متقدم", 4.5, 1.8, "vision", "https://huggingface.co/ggml-org/SmolVLM-2.2B-Instruct-GGUF/resolve/main/SmolVLM-2.2B-Instruct-Q4_K_M.gguf"),
            ModelItem("Qwen 2.5 VL 3B", "رؤية ذكية - Qwen", 5.5, 2.3, "vision", "https://huggingface.co/ggml-org/Qwen2.5-VL-3B-Instruct-GGUF/resolve/main/Qwen2.5-VL-3B-Instruct-Q4_K_M.gguf"),

            // ============ نماذج توليد صور ============
            ModelItem("Stable Diffusion 1.5", "توليد صور فني", 5.0, 2.0, "image", "https://huggingface.co/stable-diffusion-v1-5/stable-diffusion-v1-5/resolve/main/v1-5-pruned-emaonly.safetensors"),
            ModelItem("SDXL Turbo", "توليد صور سريع", 8.0, 6.9, "image", "https://huggingface.co/stabilityai/sdxl-turbo/resolve/main/sd_xl_turbo_1.0_fp16.safetensors"),
            ModelItem("Anything V5 (Anime)", "توليد صور أنمي", 5.0, 2.0, "image", "https://huggingface.co/stablediffusionapi/anything-v5/resolve/main/anything-v5.safetensors")
        )

        adapter = ModelsAdapter(allModels, ramGB)
        recyclerView.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s.toString().lowercase()
                adapter.updateList(allModels.filter { it.name.lowercase().contains(q) })
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}

data class ModelItem(
    val name: String,
    val description: String,
    val requiredRam: Double,
    val sizeGb: Double,
    val category: String,
    val downloadUrl: String
)
