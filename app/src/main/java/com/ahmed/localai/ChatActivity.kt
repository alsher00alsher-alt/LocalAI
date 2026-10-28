package com.ahmed.localai

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ChatActivity : AppCompatActivity() {
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private var bridge: LlamaBridge? = null
    private var currentPath: String = ""
    private var chatHistory = StringBuilder()

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

        val modelsDir = File("/sdcard/Download/AHMED_AI")
        val files = modelsDir.listFiles { f -> f.name.endsWith(".gguf", true) } ?: emptyArray()
        val names = if (files.isEmpty()) arrayOf("مفيش موديلات - حمل من الشاشة السابقة") else files.map { it.name }.toTypedArray()
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener
            if (files.isEmpty()) { Toast.makeText(this, "حمل موديل الأول", Toast.LENGTH_SHORT).show(); return@setOnClickListener }

            val idx = spinner.selectedItemPosition
            if (idx < 0 || idx >= files.size) return@setOnClickListener
            val file = files[idx]

            messages.add(ChatMessage(text, true))
            chatAdapter.notifyItemInserted(messages.size - 1)
            recyclerView.scrollToPosition(messages.size - 1)
            etMessage.text.clear()

            val thinkingMsg = ChatMessage("... يفكر", false)
            messages.add(thinkingMsg)
            val thinkIdx = messages.size - 1
            chatAdapter.notifyItemInserted(thinkIdx)
            recyclerView.scrollToPosition(thinkIdx)

            lifecycleScope.launch {
                val reply = withContext(Dispatchers.IO) {
                    if (bridge == null || currentPath != file.absolutePath) {
                        bridge?.free()
                        bridge = LlamaBridge()
                        val ok = bridge!!.load(file.absolutePath, 4)
                        if (!ok) return@withContext "فشل تحميل الموديل"
                        currentPath = file.absolutePath
                        chatHistory = StringBuilder()
                    }
                    chatHistory.append("<|im_start|>user\n").append(text).append("<|im_end|>\n<|im_start|>assistant\n")
                    val out = bridge!!.generate(chatHistory.toString(), 512)
                    chatHistory.append(out).append("<|im_end|>\n")
                    out.trim()
                }
                messages[thinkIdx] = ChatMessage(reply, false)
                chatAdapter.notifyItemChanged(thinkIdx)
                recyclerView.scrollToPosition(thinkIdx)
            }
        }
    }

    override fun onDestroy() { super.onDestroy(); bridge?.free() }
}

data class ChatMessage(val text: String, val isUser: Boolean)
