
package com.example.braindump.ai

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Conversation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalAIManager(private val context: Context) {

    companion object {
        private const val MODEL_FILE =
            "gemma3-1b-it-int4.litertlm"
    }

    private var engine: Engine? = null
    private var conversation: Conversation? = null

    // Initialize the local Gemma model
    suspend fun initialize(): Result<Unit> =
        withContext(Dispatchers.IO) {

            try {
                if (engine != null && conversation != null) {
                    return@withContext Result.success(Unit)
                }

                val modelDirectory =
                    context.getExternalFilesDir("models")
                        ?: error("Model directory unavailable")

                val modelFile = File(modelDirectory, MODEL_FILE)

                check(modelFile.exists()) {
                    "Gemma model not found: ${modelFile.absolutePath}"
                }

                val newEngine = Engine(
                    EngineConfig(
                        modelPath = modelFile.absolutePath,
                        backend = Backend.CPU(),
                        cacheDir = context.cacheDir.absolutePath
                    )
                )

                try {
                    newEngine.initialize()

                    val newConversation =
                        newEngine.createConversation()

                    engine = newEngine
                    conversation = newConversation

                    Result.success(Unit)

                } catch (error: Exception) {
                    newEngine.close()
                    throw error
                }

            } catch (error: Exception) {
                Result.failure(error)
            }
        }

    // Generate a response using Gemma
    suspend fun generateResponse(
        prompt: String
    ): Result<String> = withContext(Dispatchers.IO) {

        try {
            require(prompt.isNotBlank()) {
                "Prompt cannot be empty"
            }

            val activeConversation =
                checkNotNull(conversation) {
                    "AI model is not initialized"
                }

            val response =
                activeConversation.sendMessage(prompt)

            Result.success(response.toString())

        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    // Release native model resources
    fun close() {
        conversation?.close()
        conversation = null

        engine?.close()
        engine = null
    }
}
