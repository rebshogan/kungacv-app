package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.GeminiCareerService
import com.example.data.model.ResumeDocument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("KungaCV", appName)
    }

    @Test
    fun `extractSuggestedSummary parses suggested CV summary block cleanly`() {
        val raw = """
            Here is a stronger executive summary for your CV:
            [SUGGESTED_SUMMARY]
            Principal Product Lead with 8+ years scaling enterprise AI platforms.
            [/SUGGESTED_SUMMARY]
        """.trimIndent()
        val (visible, suggested) = GeminiCareerService.extractSuggestedSummary(raw)
        assertEquals("Here is a stronger executive summary for your CV:", visible)
        assertEquals("Principal Product Lead with 8+ years scaling enterprise AI platforms.", suggested)
    }

    @Test
    fun `createExecutiveStarter populates structured experiences educations and projects`() {
        val starter = ResumeDocument.createExecutiveStarter("uid_1", "Alex Vance", "alex@example.com")
        assertTrue(starter.parsedExperiences().isNotEmpty())
        assertTrue(starter.parsedEducations().isNotEmpty())
        assertTrue(starter.parsedProjects().isNotEmpty())
    }
}
