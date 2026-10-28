#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "llama.h"

#define LOG_TAG "LlamaJNI"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

struct CtxWrap {
    llama_model* model;
    llama_context* ctx;
};

extern "C" JNIEXPORT jlong JNICALL
Java_com_ahmed_localai_LlamaBridge_initContext(JNIEnv* env, jobject, jstring jpath, jint n_threads) {
    const char* path = env->GetStringUTFChars(jpath, nullptr);
    llama_backend_init();
    llama_model_params mp = llama_model_default_params();
    mp.n_gpu_layers = 0;
    llama_model* model = llama_load_model_from_file(path, mp);
    env->ReleaseStringUTFChars(jpath, path);
    if (!model) { LOGE("model load failed"); return 0; }
    llama_context_params cp = llama_context_default_params();
    cp.n_ctx = 4096;
    cp.n_batch = 512;
    cp.n_threads = n_threads;
    cp.n_threads_batch = n_threads;
    llama_context* ctx = llama_new_context_with_model(model, cp);
    if (!ctx) { llama_free_model(model); return 0; }
    auto* w = new CtxWrap{model, ctx};
    return reinterpret_cast<jlong>(w);
}

extern "C" JNIEXPORT void JNICALL
Java_com_ahmed_localai_LlamaBridge_freeContext(JNIEnv*, jobject, jlong handle) {
    auto* w = reinterpret_cast<CtxWrap*>(handle);
    if (!w) return;
    llama_free(w->ctx);
    llama_free_model(w->model);
    delete w;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_ahmed_localai_LlamaBridge_generateNative(JNIEnv* env, jobject, jlong handle, jstring jprompt, jint max_tokens) {
    auto* w = reinterpret_cast<CtxWrap*>(handle);
    if (!w) return env->NewStringUTF("ERROR: null context");
    const char* prompt_c = env->GetStringUTFChars(jprompt, nullptr);
    std::string prompt(prompt_c);
    env->ReleaseStringUTFChars(jprompt, prompt_c);

    std::vector<llama_token> tokens(prompt.size() + 64);
    int n = llama_tokenize(w->model, prompt.c_str(), (int32_t)prompt.size(), tokens.data(), (int32_t)tokens.size(), true, true);
    if (n < 0) { tokens.resize(-n); n = llama_tokenize(w->model, prompt.c_str(), (int32_t)prompt.size(), tokens.data(), (int32_t)tokens.size(), true, true); }
    tokens.resize(n);
    if (n == 0) return env->NewStringUTF("");

    llama_kv_cache_clear(w->ctx);
    llama_batch batch = llama_batch_get_one(tokens.data(), (int32_t)tokens.size());
    if (llama_decode(w->ctx, batch) != 0) return env->NewStringUTF("ERROR: decode failed");

    auto sparams = llama_sampler_chain_default_params();
    llama_sampler* smpl = llama_sampler_chain_init(sparams);
    llama_sampler_chain_add(smpl, llama_sampler_init_top_k(40));
    llama_sampler_chain_add(smpl, llama_sampler_init_top_p(0.95f, 1));
    llama_sampler_chain_add(smpl, llama_sampler_init_temp(0.8f));
    llama_sampler_chain_add(smpl, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));

    std::string result;
    for (int i = 0; i < max_tokens; i++) {
        llama_token id = llama_sampler_sample(smpl, w->ctx, -1);
        if (llama_token_is_eog(w->model, id)) break;
        char buf[256];
        int len = llama_token_to_piece(w->model, id, buf, sizeof(buf), 0, true);
        if (len > 0) result.append(buf, len);
        llama_batch b2 = llama_batch_get_one(&id, 1);
        if (llama_decode(w->ctx, b2) != 0) break;
    }
    llama_sampler_free(smpl);
    return env->NewStringUTF(result.c_str());
}
