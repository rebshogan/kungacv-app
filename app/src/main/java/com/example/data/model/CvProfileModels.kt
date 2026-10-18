package com.example.data.model

import java.util.UUID

/**
 * Core Kotlin data classes for the KungaCV structured CV Profile editor,
 * modeled on `vend-pixel/kungacv` (`redux/resume/helper.js` and `templatePresets.js`)
 * with 6 distinct, publication-grade CV layout architectures.
 */

data class ProfileLinks(
    val website: String = "",
    val linkedin: String = "",
    val github: String = "",
    val portfolio: String = ""
) {
    fun primaryDisplayLink(): String =
        listOf(website, linkedin, github, portfolio).firstOrNull { it.isNotBlank() }.orEmpty()

    fun allFormatted(): List<String> =
        listOf(website, linkedin, github, portfolio).map { it.trim() }.filter { it.isNotBlank() }.distinct()
}

data class PersonalInfo(
    val fullName: String = "",
    val jobTitle: String = "",
    val email: String = "",
    val address: String = "",
    val phone: String = "",
    val summary: String = "",
    val links: ProfileLinks = ProfileLinks()
)

data class CvDate(
    val month: String = "",
    val year: String = "",
    val onlyYear: Boolean = false,
    val presentYear: Boolean = false,
    val dontShow: Boolean = false
) {
    fun formatted(): String {
        if (dontShow) return ""
        if (presentYear) return "Present"
        if (onlyYear || month.isBlank()) return year.trim()
        return listOf(month.trim().take(3), year.trim()).filter { it.isNotBlank() }.joinToString(" ")
    }

    companion object {
        fun fromYearOrText(raw: String): CvDate {
            val trimmed = raw.trim()
            if (trimmed.equals("Present", ignoreCase = true)) {
                return CvDate(presentYear = true)
            }
            val parts = trimmed.split(" ").filter { it.isNotBlank() }
            return if (parts.size >= 2) {
                CvDate(month = parts.first(), year = parts.last())
            } else {
                CvDate(year = trimmed, onlyYear = true)
            }
        }
    }
}

data class WorkExperience(
    val id: String = UUID.randomUUID().toString(),
    val jobTitle: String = "",
    val employer: String = "",
    val city: String = "",
    val country: String = "",
    val startDate: CvDate = CvDate(),
    val endDate: CvDate = CvDate(),
    val link: String = "",
    val description: String = "",
    val highlights: List<String> = emptyList(),
    val visible: Boolean = true
) {
    fun periodDisplay(): String {
        val start = startDate.formatted()
        val end = endDate.formatted()
        return when {
            start.isNotBlank() && end.isNotBlank() -> "$start – $end"
            start.isNotBlank() -> start
            end.isNotBlank() -> end
            else -> ""
        }
    }

    fun locationDisplay(): String =
        listOf(city.trim(), country.trim()).filter { it.isNotBlank() }.joinToString(", ")

    fun effectiveBullets(): List<String> {
        val fromHighlights = highlights.map { it.trim() }.filter { it.isNotBlank() }
        if (fromHighlights.isNotEmpty()) return fromHighlights
        return description.lines().map { it.removePrefix("•").trim() }.filter { it.isNotBlank() }
    }

    fun toExperienceEntry(): ExperienceEntry = ExperienceEntry(
        id = id.ifBlank { UUID.randomUUID().toString() },
        role = jobTitle,
        company = employer,
        period = periodDisplay(),
        location = locationDisplay(),
        highlights = effectiveBullets()
    )

    companion object {
        fun fromExperienceEntry(entry: ExperienceEntry): WorkExperience {
            val periodParts = entry.period.split("–", "-").map { it.trim() }.filter { it.isNotBlank() }
            val start = periodParts.getOrNull(0)?.let { CvDate.fromYearOrText(it) } ?: CvDate()
            val end = periodParts.getOrNull(1)?.let { CvDate.fromYearOrText(it) } ?: CvDate()
            val locParts = entry.location.split(",").map { it.trim() }.filter { it.isNotBlank() }
            return WorkExperience(
                id = entry.id,
                jobTitle = entry.role,
                employer = entry.company,
                city = locParts.getOrNull(0).orEmpty(),
                country = locParts.getOrNull(1).orEmpty(),
                startDate = start,
                endDate = end,
                description = entry.highlights.joinToString("\n"),
                highlights = entry.highlights,
                visible = true
            )
        }
    }
}

data class Education(
    val id: String = UUID.randomUUID().toString(),
    val degree: String = "",
    val school: String = "",
    val city: String = "",
    val country: String = "",
    val startDate: CvDate = CvDate(),
    val endDate: CvDate = CvDate(),
    val link: String = "",
    val description: String = "",
    val visible: Boolean = true
) {
    fun periodDisplay(): String {
        val start = startDate.formatted()
        val end = endDate.formatted()
        return when {
            start.isNotBlank() && end.isNotBlank() -> "$start – $end"
            end.isNotBlank() -> end
            start.isNotBlank() -> start
            else -> ""
        }
    }

    fun locationDisplay(): String =
        listOf(city.trim(), country.trim()).filter { it.isNotBlank() }.joinToString(", ")

    fun toEducationEntry(): EducationEntry = EducationEntry(
        id = id.ifBlank { UUID.randomUUID().toString() },
        degree = degree,
        institution = listOf(school, locationDisplay()).filter { it.isNotBlank() }.joinToString(", "),
        year = periodDisplay(),
        details = description
    )

    companion object {
        fun fromEducationEntry(entry: EducationEntry): Education {
            val yearParts = entry.year.split("–", "-").map { it.trim() }.filter { it.isNotBlank() }
            val start = if (yearParts.size >= 2) CvDate.fromYearOrText(yearParts[0]) else CvDate()
            val end = if (yearParts.size >= 2) {
                CvDate.fromYearOrText(yearParts[1])
            } else {
                yearParts.firstOrNull()?.let { CvDate.fromYearOrText(it) } ?: CvDate()
            }
            val instParts = entry.institution.split(",").map { it.trim() }.filter { it.isNotBlank() }
            return Education(
                id = entry.id,
                degree = entry.degree,
                school = instParts.firstOrNull().orEmpty(),
                city = instParts.drop(1).joinToString(", "),
                startDate = start,
                endDate = end,
                description = entry.details,
                visible = true
            )
        }
    }
}

enum class SkillLevel(
    val key: String,
    val label: String,
    val dots: Int
) {
    BEGINNER("beginner", "Beginner", 1),
    SKILLFUL("skillful", "Skillful", 2),
    EXPERIENCED("experienced", "Experienced", 3),
    EXPERT("expert", "Expert", 4);

    companion object {
        fun fromKey(raw: String): SkillLevel =
            entries.firstOrNull { it.key.equals(raw.trim(), ignoreCase = true) } ?: EXPERIENCED
    }
}

data class Skill(
    val id: String = UUID.randomUUID().toString(),
    val skill: String = "",
    val skillLevel: SkillLevel = SkillLevel.EXPERIENCED,
    val description: String = "",
    val visible: Boolean = true
) {
    /**
     * Encodes into a compact string <= 80 chars for Firestore `skills` list while retaining level metadata.
     */
    fun toEncodedString(): String {
        val cleanName = skill.replace("::", " ").trim().take(55)
        return if (skillLevel == SkillLevel.EXPERIENCED && description.isBlank()) {
            cleanName
        } else {
            "$cleanName::${skillLevel.key}".take(80)
        }
    }

    companion object {
        fun fromEncodedString(encoded: String, index: Int = 0): Skill {
            val parts = encoded.split("::")
            val name = parts.firstOrNull()?.trim().orEmpty()
            val level = parts.getOrNull(1)?.let { SkillLevel.fromKey(it) } ?: SkillLevel.EXPERIENCED
            return Skill(
                id = "skill_${index}_${name.lowercase().replace(Regex("[^a-z0-9]"), "_")}",
                skill = name,
                skillLevel = level,
                visible = true
            )
        }
    }
}

data class LanguageItem(
    val id: String = UUID.randomUUID().toString(),
    val language: String = "",
    val proficiency: String = "Native",
    val visible: Boolean = true
)

data class CertificateItem(
    val id: String = UUID.randomUUID().toString(),
    val certificate: String = "",
    val issuerAndYear: String = "",
    val visible: Boolean = true
)

data class ReferenceItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val jobTitle: String = "",
    val organization: String = "",
    val email: String = "",
    val phone: String = "",
    val visible: Boolean = true
)

enum class SkillDisplayStyle(val id: String, val label: String) {
    DOTS("dots", "Level Dots"),
    BAR("bar", "Progress Bars"),
    BUBBLE("bubble", "Pill Bubbles"),
    GRID("grid", "Compact Grid");

    companion object {
        fun fromId(id: String): SkillDisplayStyle =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DOTS
    }
}

enum class TemplateFilterCategory(val id: String, val label: String) {
    ALL("all", "All Editions"),
    SINGLE_COLUMN("single", "Single Column"),
    TWO_COLUMN("two_col", "Split Column"),
    SERIF("serif", "Editorial Serif"),
    COMPACT("compact", "Swiss / ATS Grid")
}

/**
 * Defines the structural page architecture of the A4 CV sheet so each preset has a
 * genuinely distinct, human-designed publication layout rather than just a color swap.
 */
enum class CvLayoutArchitecture(val label: String) {
    HARVARD_EXECUTIVE("Harvard Executive Double-Rule"),
    SWISS_INDEX_RAIL("Swiss 26/74 Left-Rail Index"),
    ARCHITECTURAL_LEFT_SIDEBAR("Tinted Left Dossier Sidebar"),
    EDITORIAL_RIGHT_SIDEBAR("Literary 64/36 Vertical Rule Split"),
    MODERN_BANNER_HEADER("Architectural Masthead Banner"),
    FRAMED_MONOGRAPH("Framed Editorial Monograph")
}

data class KungaTemplatePreset(
    val id: Int,
    val name: String,
    val subtitle: String,
    val accentHex: Long,
    val columns: Int,
    val direction: String,
    val nameFont: String,
    val skillStyle: SkillDisplayStyle,
    val mappedTemplate: CvTemplate,
    val categories: Set<TemplateFilterCategory>,
    val architecture: CvLayoutArchitecture = CvLayoutArchitecture.HARVARD_EXECUTIVE,
    val categoryBadge: String = "EXECUTIVE · ATS 99%"
)

/**
 * The 12 curated KungaCV templates, upgraded with 6 distinct architectural page layouts:
 * Harvard Executive Double-Rule, Swiss Index Rail (Read.cv style), Tinted Left Dossier Sidebar,
 * Literary 64/36 Vertical Rule Split, Architectural Masthead Banner, and Framed Monograph.
 */
object KungaTemplateCatalog {
    val presets: List<KungaTemplatePreset> = listOf(
        KungaTemplatePreset(
            id = 1,
            name = "Classic Blue",
            subtitle = "Harvard & McKinsey executive standard · Centered masthead with double rule & tabular dates",
            accentHex = 0xFF386FA4,
            columns = 1,
            direction = "top",
            nameFont = "Poppins",
            skillStyle = SkillDisplayStyle.DOTS,
            mappedTemplate = CvTemplate.EXECUTIVE_NAVY,
            categories = setOf(TemplateFilterCategory.ALL, TemplateFilterCategory.SINGLE_COLUMN),
            architecture = CvLayoutArchitecture.HARVARD_EXECUTIVE,
            categoryBadge = "EXECUTIVE · ATS 100%"
        ),
        KungaTemplatePreset(
            id = 2,
            name = "Sage Sidebar",
            subtitle = "Literary 64/36 split with full-height vertical rule, timeline markers & serif headers",
            accentHex = 0xFF547980,
            columns = 2,
            direction = "right",
            nameFont = "Playfair Display",
            skillStyle = SkillDisplayStyle.DOTS,
            mappedTemplate = CvTemplate.EDITORIAL_SERIF,
            categories = setOf(TemplateFilterCategory.ALL, TemplateFilterCategory.TWO_COLUMN, TemplateFilterCategory.SERIF),
            architecture = CvLayoutArchitecture.EDITORIAL_RIGHT_SIDEBAR,
            categoryBadge = "EDITORIAL · 64/36 SPLIT"
        ),
        KungaTemplatePreset(
            id = 3,
            name = "Warm Dots",
            subtitle = "Tinted architectural masthead block with structured 2×2 metadata grid & rose-terracotta rules",
            accentHex = 0xFFC06C84,
            columns = 1,
            direction = "top",
            nameFont = "Poppins",
            skillStyle = SkillDisplayStyle.DOTS,
            mappedTemplate = CvTemplate.EXECUTIVE_NAVY,
            categories = setOf(TemplateFilterCategory.ALL, TemplateFilterCategory.SINGLE_COLUMN),
            architecture = CvLayoutArchitecture.MODERN_BANNER_HEADER,
            categoryBadge = "CREATIVE LEAD · BANNER"
        ),
        KungaTemplatePreset(
            id = 4,
            name = "Editorial Frame",
            subtitle = "Perimeter-framed A4 monograph with numbered section banners & literary serif masthead",
            accentHex = 0xFF6798C0,
            columns = 1,
            direction = "top",
            nameFont = "Playfair Display",
            skillStyle = SkillDisplayStyle.BAR,
            mappedTemplate = CvTemplate.EDITORIAL_SERIF,
            categories = setOf(TemplateFilterCategory.ALL, TemplateFilterCategory.SINGLE_COLUMN, TemplateFilterCategory.SERIF),
            architecture = CvLayoutArchitecture.FRAMED_MONOGRAPH,
            categoryBadge = "MONOGRAPH · FRAMED"
        ),
        KungaTemplatePreset(
            id = 5,
            name = "Lavender Split",
            subtitle = "Full-bleed tinted left dossier (34/66) with competency bars & executive narrative column",
            accentHex = 0xFF672D50,
            columns = 2,
            direction = "left",
            nameFont = "Playfair Display",
            skillStyle = SkillDisplayStyle.BAR,
            mappedTemplate = CvTemplate.EDITORIAL_SERIF,
            categories = setOf(TemplateFilterCategory.ALL, TemplateFilterCategory.TWO_COLUMN, TemplateFilterCategory.SERIF),
            architecture = CvLayoutArchitecture.ARCHITECTURAL_LEFT_SIDEBAR,
            categoryBadge = "DOSSIER · LEFT RAIL"
        ),
        KungaTemplatePreset(
            id = 6,
            name = "Minimal Studio",
            subtitle = "Read.cv & Swiss International 26/74 index grid with monospace section numbers",
            accentHex = 0xFF495963,
            columns = 1,
            direction = "top",
            nameFont = "Plus Jakarta Sans",
            skillStyle = SkillDisplayStyle.GRID,
            mappedTemplate = CvTemplate.MODERN_MINIMAL,
            categories = setOf(TemplateFilterCategory.ALL, TemplateFilterCategory.SINGLE_COLUMN, TemplateFilterCategory.COMPACT),
            architecture = CvLayoutArchitecture.SWISS_INDEX_RAIL,
            categoryBadge = "SWISS GRID · READ.CV"
        ),
        KungaTemplatePreset(
            id = 7,
            name = "Quiet Serif",
            subtitle = "Financial Times & New Yorker 66/34 editorial column with italicized role & vertical hairline",
            accentHex = 0xFF547980,
            columns = 2,
            direction = "right",
            nameFont = "Playfair Display",
            skillStyle = SkillDisplayStyle.DOTS,
            mappedTemplate = CvTemplate.EDITORIAL_SERIF,
            categories = setOf(TemplateFilterCategory.ALL, TemplateFilterCategory.TWO_COLUMN, TemplateFilterCategory.SERIF),
            architecture = CvLayoutArchitecture.EDITORIAL_RIGHT_SIDEBAR,
            categoryBadge = "PUBLICATION · SERIF"
        ),
        KungaTemplatePreset(
            id = 8,
            name = "Grid Skills",
            subtitle = "Engineering & Product spec sheet · Swiss left-rail index with structured competency matrix",
            accentHex = 0xFF348AA7,
            columns = 1,
            direction = "top",
            nameFont = "Plus Jakarta Sans",
            skillStyle = SkillDisplayStyle.GRID,
            mappedTemplate = CvTemplate.MODERN_MINIMAL,
            categories = setOf(TemplateFilterCategory.ALL, TemplateFilterCategory.SINGLE_COLUMN, TemplateFilterCategory.COMPACT),
            architecture = CvLayoutArchitecture.SWISS_INDEX_RAIL,
            categoryBadge = "TECHNICAL · ATS 100%"
        ),
        KungaTemplatePreset(
            id = 9,
            name = "Language Bubbles",
            subtitle = "Modern product design banner with structured metadata table & pill-tag matrix",
            accentHex = 0xFFC7417B,
            columns = 1,
            direction = "top",
            nameFont = "Poppins",
            skillStyle = SkillDisplayStyle.BUBBLE,
            mappedTemplate = CvTemplate.EXECUTIVE_NAVY,
            categories = setOf(TemplateFilterCategory.ALL, TemplateFilterCategory.SINGLE_COLUMN),
            architecture = CvLayoutArchitecture.MODERN_BANNER_HEADER,
            categoryBadge = "PRODUCT · PILL MATRIX"
        ),
        KungaTemplatePreset(
            id = 10,
            name = "Compact Work Sans",
            subtitle = "High-density FAANG / Staff Engineer single-page spec with Swiss left-rail dates",
            accentHex = 0xFF495963,
            columns = 1,
            direction = "top",
            nameFont = "Plus Jakarta Sans",
            skillStyle = SkillDisplayStyle.GRID,
            mappedTemplate = CvTemplate.MODERN_MINIMAL,
            categories = setOf(TemplateFilterCategory.ALL, TemplateFilterCategory.SINGLE_COLUMN, TemplateFilterCategory.COMPACT),
            architecture = CvLayoutArchitecture.SWISS_INDEX_RAIL,
            categoryBadge = "DENSE · 1-PAGE SPEC"
        ),
        KungaTemplatePreset(
            id = 11,
            name = "Uppercase Studio",
            subtitle = "Wall Street & Strategy Consulting single-column authority with bold uppercase rules",
            accentHex = 0xFF0070F0,
            columns = 1,
            direction = "top",
            nameFont = "Poppins",
            skillStyle = SkillDisplayStyle.BAR,
            mappedTemplate = CvTemplate.EXECUTIVE_NAVY,
            categories = setOf(TemplateFilterCategory.ALL, TemplateFilterCategory.SINGLE_COLUMN),
            architecture = CvLayoutArchitecture.HARVARD_EXECUTIVE,
            categoryBadge = "FINANCE & STRATEGY"
        ),
        KungaTemplatePreset(
            id = 12,
            name = "Modern Neutral",
            subtitle = "Architectural 34/66 tinted left column with cerulean competency pills & clean timeline",
            accentHex = 0xFF6798C0,
            columns = 2,
            direction = "left",
            nameFont = "Playfair Display",
            skillStyle = SkillDisplayStyle.BUBBLE,
            mappedTemplate = CvTemplate.EDITORIAL_SERIF,
            categories = setOf(TemplateFilterCategory.ALL, TemplateFilterCategory.TWO_COLUMN, TemplateFilterCategory.SERIF),
            architecture = CvLayoutArchitecture.ARCHITECTURAL_LEFT_SIDEBAR,
            categoryBadge = "ARCHITECTURAL · 34/66"
        )
    )

    fun fromPresetId(id: Int): KungaTemplatePreset =
        presets.firstOrNull { it.id == id } ?: presets.first()

    fun fromCvTemplate(template: CvTemplate): KungaTemplatePreset = when (template) {
        CvTemplate.EXECUTIVE_NAVY -> presets[0]
        CvTemplate.EDITORIAL_SERIF -> presets[1]
        CvTemplate.MODERN_MINIMAL -> presets[7]
    }
}

data class CvCustomization(
    val presetId: Int = 1,
    val presetName: String = "Classic Blue",
    val accentHex: Long = 0xFF386FA4,
    val columns: Int = 1,
    val direction: String = "top",
    val skillDisplayStyle: SkillDisplayStyle = SkillDisplayStyle.DOTS,
    val fontFamily: String = "Poppins",
    val sectionOrder: List<String> = DEFAULT_SECTION_ORDER,
    val hiddenSections: Set<String> = emptySet()
) {
    companion object {
        val DEFAULT_SECTION_ORDER = listOf(
            "personalInfo",
            "workExperience",
            "education",
            "skills",
            "projects"
        )
    }
}

/**
 * Complete typed CV Profile model used by the structured editor & live preview.
 */
data class CvProfile(
    val id: String = "",
    val userId: String = "",
    val title: String = "Untitled resume",
    val personalInfo: PersonalInfo = PersonalInfo(),
    val workExperience: List<WorkExperience> = emptyList(),
    val education: List<Education> = emptyList(),
    val skills: List<Skill> = emptyList(),
    val projects: List<ProjectEntry> = emptyList(),
    val languages: List<LanguageItem> = listOf(
        LanguageItem(language = "English", proficiency = "Native"),
        LanguageItem(language = "Chichewa", proficiency = "Native")
    ),
    val certificates: List<CertificateItem> = emptyList(),
    val references: List<ReferenceItem> = emptyList(),
    val customization: CvCustomization = CvCustomization()
) {
    fun completionPercentage(): Int {
        var completed = 0
        val total = 5
        if (personalInfo.fullName.isNotBlank() && personalInfo.email.isNotBlank()) completed++
        if (personalInfo.summary.isNotBlank()) completed++
        if (workExperience.isNotEmpty()) completed++
        if (education.isNotEmpty()) completed++
        if (skills.isNotEmpty()) completed++
        return ((completed * 100f) / total).toInt().coerceIn(10, 100)
    }

    /**
     * Exact pre-export quality checks from `frontend/kungacv_client/app/resume/create/page.jsx`
     */
    fun exportWarnings(): List<String> = buildList {
        if (personalInfo.fullName.isBlank()) {
            add("Add your name so employers know whose resume they are reading.")
        }
        if (personalInfo.email.isBlank()) {
            add("Add an email address so employers can contact you.")
        }
        if (workExperience.isEmpty() && projects.isEmpty()) {
            add("Add experience or a project to show what you can contribute.")
        }
    }
}

/**
 * Job Application Tracker model from `frontend/kungacv_client/app/job-tracker/page.jsx`
 */
enum class JobStage(val label: String, val badgeHex: Long) {
    SAVED("Saved", 0xFF6B6B76),
    APPLIED("Applied", 0xFF0070F0),
    INTERVIEWING("Interviewing", 0xFFC68B2C),
    OFFER("Offer", 0xFF15733B);

    companion object {
        fun fromLabel(label: String): JobStage =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: SAVED
    }
}

data class JobApplicationEntry(
    val id: String = UUID.randomUUID().toString(),
    val company: String = "",
    val role: String = "",
    val stage: JobStage = JobStage.APPLIED,
    val nextFollowUp: String = "",
    val linkedResumeTitle: String = "",
    val linkedCoverLetterTitle: String = ""
)
