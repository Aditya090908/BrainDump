package com.example.braindump

import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import kotlinx.coroutines.flow.collect

class BrainAiManager {

    private val generativeModel: GenerativeModel =
        Generation.getClient()


    // =========================================================
    // CHECK AI STATUS
    // =========================================================

    suspend fun checkAiStatus(): AiStatus {

        return try {

            when (generativeModel.checkStatus()) {

                FeatureStatus.AVAILABLE ->
                    AiStatus.AVAILABLE

                FeatureStatus.DOWNLOADABLE ->
                    AiStatus.DOWNLOADABLE

                FeatureStatus.DOWNLOADING ->
                    AiStatus.DOWNLOADING

                else ->
                    AiStatus.UNAVAILABLE
            }

        } catch (e: Exception) {

            AiStatus.UNAVAILABLE
        }
    }


    // =========================================================
    // DOWNLOAD AI MODEL
    // =========================================================

    suspend fun downloadModel(
        onProgress: (String) -> Unit = {}
    ): Boolean {

        return try {

            generativeModel
                .download()
                .collect { status ->

                    when (status) {

                        is DownloadStatus.DownloadStarted -> {

                            onProgress(
                                "Starting AI download..."
                            )
                        }

                        is DownloadStatus.DownloadProgress -> {

                            onProgress(
                                "Downloading AI model..."
                            )
                        }

                        is DownloadStatus.DownloadCompleted -> {

                            onProgress(
                                "AI model ready!"
                            )
                        }

                        is DownloadStatus.DownloadFailed -> {

                            onProgress(
                                "AI download failed."
                            )
                        }

                        else -> Unit
                    }
                }

            true

        } catch (e: Exception) {

            false
        }
    }


    // =========================================================
    // CORE AI GENERATION
    // =========================================================

    private suspend fun generate(
        prompt: String
    ): String {

        return try {

            val response =
                generativeModel.generateContent(
                    prompt
                )

            response
                .candidates
                .firstOrNull()
                ?.text
                ?: "I couldn't generate a response."

        } catch (e: Exception) {

            "AI couldn't process this right now."
        }
    }


    // =========================================================
    // SUMMARIZE
    // =========================================================

    suspend fun summarize(
        thought: String
    ): String {

        return generate(

            """
            You are the private AI assistant inside a personal
            brain-dump application.

            Summarize the following thought clearly and briefly.

            Do not add information that isn't present.

            Thought:
            $thought
            """.trimIndent()
        )
    }


    // =========================================================
    // FIND TASKS
    // =========================================================

    suspend fun findTasks(
        thought: String
    ): String {

        return generate(

            """
            Analyze this brain dump and identify actionable tasks.

            Return only the tasks.

            If there are no clear tasks, say:

            No clear tasks found.

            Brain dump:
            $thought
            """.trimIndent()
        )
    }


    // =========================================================
    // FIND IDEAS
    // =========================================================

    suspend fun findIdeas(
        thought: String
    ): String {

        return generate(

            """
            Analyze this brain dump and identify ideas,
            possibilities, plans, or things the person wants
            to explore.

            Keep the answer concise.

            If there are no clear ideas, say:

            No clear ideas found.

            Brain dump:
            $thought
            """.trimIndent()
        )
    }


    // =========================================================
    // CATEGORIZE
    // =========================================================

    suspend fun categorize(
        thought: String
    ): String {

        return generate(

            """
            Categorize this brain dump into the most appropriate
            category.

            Choose ONE primary category:

            Study
            Work
            Fitness
            Personal
            Ideas
            Tasks
            Finance
            Travel
            Shopping
            Relationships
            Other

            Return only:

            Category: <category>

            Brain dump:
            $thought
            """.trimIndent()
        )
    }


    // =========================================================
    // ACTION PLAN
    // =========================================================

    suspend fun createActionPlan(
        thought: String
    ): String {

        return generate(

            """
            Turn this brain dump into a simple practical action
            plan.

            Give 3 to 5 steps maximum.

            Do not invent information.

            Brain dump:
            $thought
            """.trimIndent()
        )
    }


    // =========================================================
    // REWRITE
    // =========================================================

    suspend fun rewrite(
        thought: String
    ): String {

        return generate(

            """
            Rewrite this brain dump so that it is clearer and
            easier to understand.

            Preserve the original meaning.

            Do not make it overly formal.

            Brain dump:
            $thought
            """.trimIndent()
        )
    }


    // =========================================================
    // ASK AI ABOUT THOUGHTS
    // =========================================================

    suspend fun ask(
        question: String,
        thoughts: List<String>
    ): String {

        val combinedThoughts =
            thoughts.joinToString(
                separator = "\n\n---\n\n"
            )

        return generate(

            """
            You are a private AI assistant analyzing the user's
            personal brain dumps.

            Answer the user's question using ONLY the provided
            thoughts.

            If the information isn't present, say so clearly.

            User question:
            $question

            User's thoughts:
            $combinedThoughts
            """.trimIndent()
        )
    }


    // =========================================================
    // WEEKLY BRAIN SUMMARY
    // =========================================================

    suspend fun weeklySummary(
        thoughts: List<String>
    ): String {

        val combined =
            thoughts.joinToString(
                separator = "\n\n---\n\n"
            )

        return generate(

            """
            Analyze these personal brain dumps and create a
            short "Brain This Week" summary.

            Include:

            1. Main themes
            2. Important tasks
            3. Interesting ideas
            4. One practical suggestion

            Do not invent information.

            Thoughts:
            $combined
            """.trimIndent()
        )
    }


    // =========================================================
    // CLOSE AI
    // =========================================================

    fun close() {

        generativeModel.close()
    }
}


// =============================================================
// AI STATUS
// =============================================================

enum class AiStatus {

    AVAILABLE,

    DOWNLOADABLE,

    DOWNLOADING,

    UNAVAILABLE
}