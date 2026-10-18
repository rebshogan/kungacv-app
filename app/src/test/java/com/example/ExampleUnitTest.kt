package com.example

import com.example.data.model.CvCustomization
import com.example.data.model.CvDate
import com.example.data.model.CvProfile
import com.example.data.model.Education
import com.example.data.model.GeminiModelTier
import com.example.data.model.KungaTemplateCatalog
import com.example.data.model.PersonalInfo
import com.example.data.model.ProfileLinks
import com.example.data.model.ResumeDocument
import com.example.data.model.Skill
import com.example.data.model.SkillDisplayStyle
import com.example.data.model.SkillLevel
import com.example.data.model.WorkExperience
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun cvProfile_personalExperienceEducationSkills_roundTripsAndComputesCompletion() {
        val preset = KungaTemplateCatalog.presets.first()
        val profile = CvProfile(
            title = "Senior Product Designer CV",
            personalInfo = PersonalInfo(
                fullName = "Thoko Banda",
                jobTitle = "Product Designer",
                email = "thoko.banda@example.com",
                phone = "+265 991 234 567",
                address = "Lilongwe, Malawi",
                links = ProfileLinks(linkedin = "linkedin.com/in/thokobanda"),
                summary = "Product designer focused on clear user flows, calm interfaces, and measurable product improvements across web and mobile."
            ),
            workExperience = listOf(
                WorkExperience(
                    jobTitle = "Product Designer",
                    employer = "MobiWorks Africa",
                    city = "Lilongwe",
                    country = "Malawi",
                    startDate = CvDate(month = "Mar", year = "2023"),
                    endDate = CvDate(presentYear = true),
                    highlights = listOf(
                        "Led the redesign of the customer onboarding flow, increasing activation by 28%."
                    )
                )
            ),
            education = listOf(
                Education(
                    degree = "BSc in Business Information Systems",
                    school = "University of Malawi",
                    city = "Zomba, Malawi",
                    startDate = CvDate(year = "2017", onlyYear = true),
                    endDate = CvDate(year = "2021", onlyYear = true),
                    description = "Graduated with distinction."
                )
            ),
            skills = listOf(
                Skill(skill = "Product Design", skillLevel = SkillLevel.EXPERT),
                Skill(skill = "Figma", skillLevel = SkillLevel.EXPERIENCED),
                Skill(skill = "Design Systems", skillLevel = SkillLevel.EXPERT)
            ),
            customization = CvCustomization(
                presetId = preset.id,
                presetName = preset.name,
                accentHex = preset.accentHex,
                columns = preset.columns,
                direction = preset.direction,
                skillDisplayStyle = preset.skillStyle,
                fontFamily = preset.nameFont
            )
        )

        assertEquals(100, profile.completionPercentage())
        assertTrue(profile.exportWarnings().isEmpty())

        val firestoreDoc = ResumeDocument(userId = "user-123").withStructuredProfile(profile)
        assertEquals("Thoko Banda", firestoreDoc.fullName)
        assertEquals("Product Designer", firestoreDoc.jobTitle)
        assertEquals(1, firestoreDoc.parsedExperiences().size)
        assertEquals(1, firestoreDoc.parsedEducations().size)
        assertEquals(3, firestoreDoc.parsedTypedSkills().size)
        assertEquals(SkillLevel.EXPERT, firestoreDoc.parsedTypedSkills().first().skillLevel)
    }

    @Test
    fun kungaTemplateCatalog_containsAllTwelveRepoPresets() {
        assertEquals(12, KungaTemplateCatalog.presets.size)
        val classicBlue = KungaTemplateCatalog.fromPresetId(1)
        assertEquals("Classic Blue", classicBlue.name)
        assertEquals(0xFF386FA4, classicBlue.accentHex)

        val lavenderSplit = KungaTemplateCatalog.fromPresetId(5)
        assertEquals("Lavender Split", lavenderSplit.name)
        assertEquals(2, lavenderSplit.columns)
        assertEquals(SkillDisplayStyle.BAR, lavenderSplit.skillStyle)

        val distinctArchitectures = KungaTemplateCatalog.presets.map { it.architecture }.toSet()
        assertEquals(6, distinctArchitectures.size)
    }

    @Test
    fun starterResume_matchesAuthenticKungaCvStarterProfile() {
        val starter = ResumeDocument.createExecutiveStarter(
            userId = "user-123",
            candidateName = "",
            candidateEmail = ""
        )
        assertEquals("Thoko Banda", starter.fullName)
        assertTrue(starter.parsedExperiences().any { it.company == "MobiWorks Africa" })
        assertTrue(starter.parsedEducations().any { it.institution.contains("University of Malawi") })
        assertFalse(starter.skills.isEmpty())
    }

    @Test
    fun geminiModelTiers_useCurrentRecommendedModelIds() {
        assertEquals("gemini-3.5-flash-lite", GeminiModelTier.FAST.modelName)
        assertEquals("gemini-3.8-flash", GeminiModelTier.GENERAL.modelName)
        assertEquals("gemini-4-argon", GeminiModelTier.COMPLEX.modelName)
    }
}
