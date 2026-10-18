package com.example.ai

import android.util.Log
import com.example.data.model.ChatMessageDocument
import com.example.data.model.GeminiModelTier
import com.example.data.model.ResumeDocument
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

data class AiPolishResult(
    val variations: List<String>,
    val coachingTip: String
)

class GeminiCareerService {

    private val polishSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "variations" to Schema.array(
                    items = Schema.string(description = "A polished, quantified achievement statement or executive summary"),
                    description = "Exactly 3 distinct high-impact rewrites"
                ),
                "coachingTip" to Schema.string(
                    description = "A brief 1-sentence explanation of why these rewrites are stronger for ATS and hiring managers"
                )
            )
        )
    }

    private val structuredPolishModel by lazy {
        Firebase.ai.generativeModel(
            modelName = GeminiModelTier.GENERAL.modelName,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = polishSchema
            },
            systemInstruction = content {
                text(
                    "You are KungaCV's Principal Executive Resume Editor. Rewrite raw job duties or career summaries into quantified, results-driven achievement statements using strong action verbs and executive clarity."
                )
            }
        )
    }

    suspend fun polishResumeText(
        rawText: String,
        sectionLabel: String,
        activeResume: ResumeDocument?
    ): AiPolishResult {
        val contextHeader = activeResume?.let {
            "Candidate Role: ${it.jobTitle.ifBlank { "Professional" }}\nCore Skills: ${it.skills.joinToString(", ")}"
        }.orEmpty()

        val prompt = buildString {
            appendLine("Polish the following $sectionLabel for an executive resume.")
            if (contextHeader.isNotBlank()) {
                appendLine(contextHeader)
            }
            appendLine("Original text:")
            appendLine(rawText)
            appendLine("Provide 3 high-impact variations (Concise Impact, Metrics-Driven, and Executive Leadership) and a brief coaching tip.")
        }

        val response = withTimeout(60_000L) {
            structuredPolishModel.generateContent(prompt)
        }
        val jsonText = response.text ?: throw IllegalStateException("Model returned empty response.")
        val json = JSONObject(jsonText)
        val arr = json.getJSONArray("variations")
        val list = (0 until arr.length()).map { arr.getString(it) }.filter { it.isNotBlank() }
        val tip = json.optString("coachingTip", "Quantified outcomes increase interview callbacks.")
        return AiPolishResult(
            variations = list.ifEmpty { listOf(rawText) },
            coachingTip = tip
        )
    }

    fun streamTailoredCoverLetter(
        resume: ResumeDocument,
        companyName: String,
        targetRole: String,
        jobDescription: String,
        tone: String
    ): Flow<String> = flow {
        val model = Firebase.ai.generativeModel(
            modelName = GeminiModelTier.GENERAL.modelName,
            systemInstruction = content {
                text(
                    """
                    You are KungaCV's Executive Cover Letter Strategist.
                    Write compelling, authentic, and tailored cover letters that connect the candidate's real achievements from their CV directly to the employer's needs.
                    Never use generic clichés or placeholder brackets unless information is genuinely missing.
                    Format with a clean professional salutation, 3-4 focused paragraphs, and a confident sign-off using the candidate's name.
                    """.trimIndent()
                )
            }
        )

        val prompt = buildString {
            appendLine("Write a tailored cover letter in a '$tone' tone.")
            appendLine("Target Company: $companyName")
            appendLine("Target Role: $targetRole")
            if (jobDescription.isNotBlank()) {
                appendLine("Job Description / Key Requirements:")
                appendLine(jobDescription)
            }
            appendLine("\nCandidate CV Profile:")
            appendLine(resume.toContextPromptSummary())
        }

        val accumulated = StringBuilder()
        withTimeout(60_000L) {
            model.generateContentStream(prompt).collect { chunk ->
                val text = chunk.text.orEmpty()
                if (text.isNotEmpty()) {
                    accumulated.append(text)
                    emit(accumulated.toString())
                }
            }
        }
    }

    fun streamCareerCoachReply(
        tier: GeminiModelTier,
        history: List<ChatMessageDocument>,
        userPrompt: String,
        activeResume: ResumeDocument?
    ): Flow<String> = flow {
        val resumeContext = activeResume?.toContextPromptSummary()
            ?: "No resume selected yet. Encourage the user to build or select their resume in CV Studio."

        val systemRolePrompt = """
            You are KungaCV's Senior Executive Career Coach & Resume Writer Assistant.
            Your role:
            1. Provide actionable, encouraging, and deeply practical resume rewrites, ATS keyword optimization, interview preparation, and career positioning advice.
            2. Ground your advice in the user's currently active CV shown below.
            3. Whenever you draft a polished Executive Summary or a ready-to-use Resume Bullet that the user might want to insert directly into their CV, also wrap the clean text inside [SUGGESTED_SUMMARY]...[/SUGGESTED_SUMMARY] at the very end of your response so the app can offer a one-tap "Apply to Active CV" button.

            CURRENT ACTIVE CV CONTEXT:
            $resumeContext
        """.trimIndent()

        val model = Firebase.ai.generativeModel(
            modelName = tier.modelName,
            systemInstruction = content {
                text(systemRolePrompt)
            }
        )

        val mappedHistory = history.takeLast(16).map { msg ->
            val role = if (msg.role == "model") "model" else "user"
            content(role) {
                text(msg.text)
            }
        }

        val chat = model.startChat(history = mappedHistory)
        val accumulated = StringBuilder()
        try {
            withTimeout(60_000L) {
                chat.sendMessageStream(userPrompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        emit(accumulated.toString())
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Career coach streaming failed on model ${tier.modelName}", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "GeminiCareerService"

        fun extractSuggestedSummary(fullResponse: String): Pair<String, String> {
            val startTag = "[SUGGESTED_SUMMARY]"
            val endTag = "[/SUGGESTED_SUMMARY]"
            val startIdx = fullResponse.indexOf(startTag)
            val endIdx = fullResponse.indexOf(endTag)
            return if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                val suggested = fullResponse.substring(startIdx + startTag.length, endIdx).trim()
                val cleaned = (fullResponse.substring(0, startIdx) + fullResponse.substring(endIdx + endTag.length)).trim()
                cleaned to suggested
            } else {
                fullResponse.trim() to ""
            }
        }
    }
}
