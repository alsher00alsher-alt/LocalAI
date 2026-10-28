package com.ahmed.localai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private lateinit var spinner: Spinner
    private var modelFiles: Array<File> = emptyArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        checkStoragePermission()

        val recyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<Button>(R.id.btnSend)
        val btnRefresh = findViewById<Button>(R.id.btnRefresh)
        spinner = findViewById(R.id.spinnerModels)

        chatAdapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        loadModelsList()

        btnRefresh.setOnClickListener {
            loadModelsList()
            Toast.makeText(this, "تم تحديث قائمة الموديلات", Toast.LENGTH_SHORT).show()
        }

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener
            if (modelFiles.isEmpty()) { Toast.makeText(this, "حمل موديل الأول من المكتبة", Toast.LENGTH_SHORT).show(); return@setOnClickListener }

            val idx = spinner.selectedItemPosition
            if (idx < 0 || idx >= modelFiles.size) return@setOnClickListener
            val file = modelFiles[idx]

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
                        val maxThreads = Runtime.getRuntime().availableProcessors()
                        val ok = bridge!!.load(file.absolutePath, maxThreads)
                        if (!ok) return@withContext "فشل تحميل الموديل"
                        currentPath = file.absolutePath
                        chatHistory = StringBuilder()
                    }
                    if (chatHistory.length > 2000) {
                        chatHistory = StringBuilder()
                    }
                    chatHistory.append("<|im_start|>user\n").append(text).append("<|im_end|>\n<|im_start|>assistant\n")
                    val out = bridge!!.generate(chatHistory.toString(), 256)
                    chatHistory.append(out).append("<|im_end|>\n")
                    out.trim()
                }
                messages[thinkIdx] = ChatMessage(reply, false)
                chatAdapter.notifyItemChanged(thinkIdx)
                recyclerView.scrollToPosition(thinkIdx)
            }
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadModelsList()
    }

    private fun loadModelsList() {
        val modelsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "AHMED_AI")
        if (!modelsDir.exists()) modelsDir.mkdirs()
        
        modelFiles = modelsDir.listFiles { f -> f.name.endsWith(".gguf", true) } ?: emptyArray()
        val names = if (modelFiles.isEmpty()) arrayOf("مفيش موديلات - حمل من المكتبة") else modelFiles.map { it.name }.toTypedArray()
        
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
    }

    override fun onDestroy() { super.onDestroy(); bridge?.free() }
}

data class ChatMessage(val text: String, val isUser: Boolean)
