package com.ahmed.localai

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvDeviceInfo = findViewById<TextView>(R.id.tvDeviceInfo)
        
        // قراءة الرامات
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalRamGB = memoryInfo.totalMem / (1024 * 1024 * 1024)
        val availRamGB = memoryInfo.availMem / (1024 * 1024 * 1024)
        
        // قراءة المساحة
        val stat = StatFs(Environment.getDataDirectory().path)
        val totalStorageGB = stat.blockCountLong * stat.blockSizeLong / (1024 * 1024 * 1024)
        val availStorageGB = stat.availableBlocksLong * stat.blockSizeLong / (1024 * 1024 * 1024)
        
        // قراءة المعالج
        val cpuModel = Build.HARDWARE
        val cpuCores = Runtime.getRuntime().availableProcessors()

        tvDeviceInfo.text = """
            📱 الجهاز: ${Build.MANUFACTURER} ${Build.MODEL}
            ⚙️ المعالج: $cpuModel ($cpuCores أنوية)
            🧠 الرامات: ${availRamGB}GB متاح من ${totalRamGB}GB
            💾 المساحة: ${availStorageGB}GB متاح من ${totalStorageGB}GB
        """.trimIndent()

        findViewById<Button>(R.id.btnChat).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        findViewById<Button>(R.id.btnModels).setOnClickListener {
            startActivity(Intent(this, ModelsActivity::class.java))
        }
    }
}
