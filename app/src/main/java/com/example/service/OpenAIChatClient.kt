package com.example.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSource
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class OpenAIChatClient(
    private val client: OkHttpClient = OkHttpClient()
) {
    fun chatStream(
        baseUrl: String,
        apiKey: String,
        model: String,
        prompt: String,
        temperature: Float = 0.7f,
        maxTokens: Int? = null,
        systemPrompt: String? = null
    ): Flow<String> = flow {
        val messages = JSONArray()
        if (systemPrompt != null) {
            messages.put(JSONObject().put("role", "system").put("content", systemPrompt))
        }
        messages.put(JSONObject().put("role", "user").put("content", prompt))

        val requestBodyJson = JSONObject().apply {
            put("model", model)
            put("messages", messages)
            put("temperature", temperature)
            put("stream", true)
            if (maxTokens != null) {
                put("max_tokens", maxTokens)
            }
        }.toString()

        val request = Request.Builder()
            .url(if (baseUrl.endsWith("/")) "${baseUrl}chat/completions" else "$baseUrl/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val source: BufferedSource = response.body?.source() ?: throw IOException("Empty body")
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ")
                    if (data == "[DONE]") break
                    try {
                        val element = JSONObject(data)
                        val choices = element.optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                            val delta = choices.getJSONObject(0).optJSONObject("delta")
                            if (delta != null && delta.has("content")) {
                                emit(delta.getString("content"))
                            }
                        }
                    } catch (e: Exception) {
                        // ignore JSON parse errors on partial streams
                    }
                }
            }
        }
    }

    suspend fun chatBlock(
        baseUrl: String,
        apiKey: String,
        model: String,
        prompt: String,
        temperature: Float = 0.7f,
        maxTokens: Int? = null,
        systemPrompt: String? = null
    ): String {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val messages = JSONArray()
            if (systemPrompt != null) {
                messages.put(JSONObject().put("role", "system").put("content", systemPrompt))
            }
            messages.put(JSONObject().put("role", "user").put("content", prompt))

            val requestBodyJson = JSONObject().apply {
                put("model", model)
                put("messages", messages)
                put("temperature", temperature)
                put("stream", false)
                if (maxTokens != null) {
                    put("max_tokens", maxTokens)
                }
            }.toString()

            val request = Request.Builder()
                .url(if (baseUrl.endsWith("/")) "${baseUrl}chat/completions" else "$baseUrl/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                val responseBodyStr = response.body?.string() ?: throw IOException("Empty body")
                val root = JSONObject(responseBodyStr)
                val choices = root.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val message = choices.getJSONObject(0).optJSONObject("message")
                    return@withContext message?.optString("content") ?: ""
                }
                ""
            }
        }
    }
}
