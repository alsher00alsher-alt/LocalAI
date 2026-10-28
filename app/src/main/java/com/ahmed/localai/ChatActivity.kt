package com.ahmed.localai

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ljcamargo.llamacpp.LlamaEngine
import java.io.File

class ChatActivity : AppCompatActivity() {
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private var engine: LlamaEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val recyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<Button>(R.id.btnSend)
        val spinner = findViewById<Spinner>(R.id.spinnerModels)

        chatAdapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        // قراءة الموديلات المحملة من مجلد Download/AHMED_AI
        val modelsDir = File("/sdcard/Download/AHMED_AI")
        val files = modelsDir.listFiles { f -> f.name.endsWith(".gguf") } ?: emptyArray()
        val modelNames = files.map { it.name }.toTypedArray()
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, modelNames)

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                messages.add(ChatMessage(text, true))
                chatAdapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)
                etMessage.text.clear()

                val selectedModelName = spinner.selectedItem as? String ?: ""
                val selectedFile = files.find { it.name == selectedModelName }
                
                if (selectedFile != null) {
                    // تحميل الموديل وتشغيله (ده كود حقيقي بس بياخد وقت)
                    Thread {
                        if (engine == null) {
                            engine = LlamaEngine(modelPath = selectedFile.absolutePath, nThreads = 4)
                        }
                        // هنا بنبعت الرسالة للمحرك (الرد وهمي مؤقتاً لحد ما نربط الـ callback)
                        val reply = "تم تحميل الموديل ${selectedModelName}، جاري التفكير..."
                        runOnUiThread {
                            messages.add(ChatMessage(reply, false))
                            chatAdapter.notifyItemInserted(messages.size - 1)
                            recyclerView.scrollToPosition(messages.size - 1)
                        }
                    }.start()
                } else {
                    messages.add(ChatMessage("مفيش موديل متحمل! روح لشاشة النماذج وحمل واحد.", false))
                    chatAdapter.notifyItemInserted(messages.size - 1)
                }
            }
        }
    }
}

data class ChatMessage(val text: String, val isUser: Boolean)
