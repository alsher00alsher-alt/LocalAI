package com.ahmed.localai

class LlamaBridge {
    private var handle: Long = 0L

    fun load(path: String, threads: Int = 4): Boolean {
        if (handle != 0L) free()
        handle = initContext(path, threads)
        return handle != 0L
    }

    fun generate(prompt: String, maxTokens: Int = 512): String {
        if (handle == 0L) return "النموذج مش محمل"
        return generateNative(handle, prompt, maxTokens)
    }

    fun free() {
        if (handle != 0L) { freeContext(handle); handle = 0L }
    }

    private external fun initContext(path: String, threads: Int): Long
    private external fun freeContext(handle: Long)
    private external fun generateNative(handle: Long, prompt: String, maxTokens: Int): String

    companion object {
        init { System.loadLibrary("ahmed_ai_jni") }
    }
}
