package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID

enum class CvTemplate(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val accentHex: Long
) {
    EXECUTIVE_NAVY(
        id = "executive_navy",
        displayName = "Classic Blue",
        subtitle = "KungaCV signature electric-blue rules & single-column clarity",
        accentHex = 0xFF0070F0
    ),
    EDITORIAL_SERIF(
        id = "editorial_serif",
        displayName = "Editorial Frame",
        subtitle = "Two-column / serif masthead with warm paper hierarchy",
        accentHex = 0xFF547980
    ),
    MODERN_MINIMAL(
        id = "modern_minimal",
        displayName = "Grid Skills",
        subtitle = "Compact architectural grid with high-density ATS readability",
        accentHex = 0xFF348AA7
    );

    companion object {
        fun fromId(id: String): CvTemplate =
            entries.firstOrNull { it.id == id } ?: EXECUTIVE_NAVY
    }
}

enum class GeminiModelTier(
    val modelName: String,
    val label: String,
    val badge: String,
    val description: String
) {
    FAST(
        modelName = "gemini-3.5-flash-lite",
        label = "Fast Polish",
        badge = "Flash-Lite",
        description = "Ultra-fast phrasing checks & grammar cleanup"
    ),
    GENERAL(
        modelName = "gemini-3.8-flash",
        label = "Career Coach",
        badge = "3.8 Flash",
        description = "Balanced resume writing, bullet quantification & coaching"
    ),
    COMPLEX(
        modelName = "gemini-4-argon",
        label = "Deep Strategy",
        badge = "4 Argon",
        description = "Complex executive positioning & comprehensive ATS analysis"
    )
}

data class ExperienceEntry(
    val id: String = UUID.randomUUID().toString(),
    val role: String = "",
    val company: String = "",
    val period: String = "",
    val location: String = "",
    val highlights: List<String> = emptyList()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id.ifBlank { UUID.randomUUID().toString() },
        "role" to role.take(120),
        "company" to company.take(120),
        "period" to period.take(80),
        "location" to location.take(120),
        "highlights" to highlights.map { it.take(500) }.take(15)
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): ExperienceEntry {
            val rawHighlights = map["highlights"] as? List<*> ?: emptyList<Any?>()
            return ExperienceEntry(
                id = (map["id"] as? String)?.ifBlank { UUID.randomUUID().toString() }
                    ?: UUID.randomUUID().toString(),
                role = (map["role"] as? String).orEmpty(),
                company = (map["company"] as? String).orEmpty(),
                period = (map["period"] as? String).orEmpty(),
                location = (map["location"] as? String).orEmpty(),
                highlights = rawHighlights.mapNotNull { it as? String }
            )
        }
    }
}

data class EducationEntry(
    val id: String = UUID.randomUUID().toString(),
    val degree: String = "",
    val institution: String = "",
    val year: String = "",
    val details: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id.ifBlank { UUID.randomUUID().toString() },
        "degree" to degree.take(120),
        "institution" to institution.take(120),
        "year" to year.take(60),
        "details" to details.take(400)
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): EducationEntry = EducationEntry(
            id = (map["id"] as? String)?.ifBlank { UUID.randomUUID().toString() }
                ?: UUID.randomUUID().toString(),
            degree = (map["degree"] as? String).orEmpty(),
            institution = (map["institution"] as? String).orEmpty(),
            year = (map["year"] as? String).orEmpty(),
            details = (map["details"] as? String).orEmpty()
        )
    }
}

data class ProjectEntry(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val role: String = "",
    val description: String = "",
    val link: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id.ifBlank { UUID.randomUUID().toString() },
        "name" to name.take(120),
        "role" to role.take(120),
        "description" to description.take(600),
        "link" to link.take(200)
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): ProjectEntry = ProjectEntry(
            id = (map["id"] as? String)?.ifBlank { UUID.randomUUID().toString() }
                ?: UUID.randomUUID().toString(),
            name = (map["name"] as? String).orEmpty(),
            role = (map["role"] as? String).orEmpty(),
            description = (map["description"] as? String).orEmpty(),
            link = (map["link"] as? String).orEmpty()
        )
    }
}

@IgnoreExtraProperties
data class ResumeDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "Classic Blue Resume",
    val templateId: String = CvTemplate.EXECUTIVE_NAVY.id,
    val fullName: String = "",
    val jobTitle: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val website: String = "",
    val summary: String = "",
    val experiences: List<Map<String, Any>> = emptyList(),
    val educations: List<Map<String, Any>> = emptyList(),
    val skills: List<String> = emptyList(),
    val projects: List<Map<String, Any>> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun parsedExperiences(): List<ExperienceEntry> =
        experiences.map { ExperienceEntry.fromMap(it) }

    fun parsedEducations(): List<EducationEntry> =
        educations.map { EducationEntry.fromMap(it) }

    fun parsedProjects(): List<ProjectEntry> =
        projects.map { ProjectEntry.fromMap(it) }

    fun parsedWorkExperiences(): List<WorkExperience> =
        parsedExperiences().map { WorkExperience.fromExperienceEntry(it) }

    fun parsedTypedEducations(): List<Education> =
        parsedEducations().map { Education.fromEducationEntry(it) }

    fun parsedTypedSkills(): List<Skill> =
        skills.mapIndexed { idx, raw -> Skill.fromEncodedString(raw, idx) }

    fun displaySkillNames(): List<String> =
        parsedTypedSkills().map { it.skill }

    /**
     * Converts this Firestore `ResumeDocument` into the structured `CvProfile` data class hierarchy.
     */
    fun toCvProfile(presetOverride: KungaTemplatePreset? = null): CvProfile {
        val preset = presetOverride ?: KungaTemplateCatalog.fromCvTemplate(CvTemplate.fromId(templateId))
        return CvProfile(
            id = id,
            userId = userId,
            title = title,
            personalInfo = PersonalInfo(
                fullName = fullName,
                jobTitle = jobTitle,
                email = email,
                phone = phone,
                address = location,
                summary = summary,
                links = ProfileLinks(website = website)
            ),
            workExperience = parsedWorkExperiences(),
            education = parsedTypedEducations(),
            skills = parsedTypedSkills(),
            projects = parsedProjects(),
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
    }

    fun withTypedSections(
        experienceList: List<ExperienceEntry> = parsedExperiences(),
        educationList: List<EducationEntry> = parsedEducations(),
        projectList: List<ProjectEntry> = parsedProjects()
    ): ResumeDocument = copy(
        experiences = experienceList.take(25).map { it.toMap() },
        educations = educationList.take(15).map { it.toMap() },
        projects = projectList.take(20).map { it.toMap() }
    )

    fun withStructuredProfile(profile: CvProfile): ResumeDocument {
        val preset = KungaTemplateCatalog.fromPresetId(profile.customization.presetId)
        return copy(
            title = profile.title.ifBlank { "Untitled resume" }.take(120),
            templateId = preset.mappedTemplate.id,
            fullName = profile.personalInfo.fullName.ifBlank { "Your Name" }.take(120),
            jobTitle = profile.personalInfo.jobTitle.take(120),
            email = profile.personalInfo.email.take(160),
            phone = profile.personalInfo.phone.take(60),
            location = profile.personalInfo.address.take(120),
            website = profile.personalInfo.links.primaryDisplayLink().take(200),
            summary = profile.personalInfo.summary.take(3000),
            experiences = profile.workExperience.take(25).map { it.toExperienceEntry().toMap() },
            educations = profile.education.take(15).map { it.toEducationEntry().toMap() },
            skills = profile.skills.take(50).map { it.toEncodedString() },
            projects = profile.projects.take(20).map { it.toMap() }
        )
    }

    fun toContextPromptSummary(): String = buildString {
        appendLine("Resume Title: $title (${CvTemplate.fromId(templateId).displayName})")
        appendLine("Candidate: $fullName — $jobTitle")
        if (location.isNotBlank() || email.isNotBlank()) {
            appendLine("Contact: ${listOf(location, email, phone, website).filter { it.isNotBlank() }.joinToString(" | ")}")
        }
        if (summary.isNotBlank()) {
            appendLine("Executive Summary: $summary")
        }
        val exps = parsedExperiences()
        if (exps.isNotEmpty()) {
            appendLine("Work Experience:")
            exps.forEach { exp ->
                appendLine("- ${exp.role} at ${exp.company} (${exp.period}): ${exp.highlights.joinToString("; ")}")
            }
        }
        val edus = parsedEducations()
        if (edus.isNotEmpty()) {
            appendLine("Education:")
            edus.forEach { edu ->
                appendLine("- ${edu.degree}, ${edu.institution} (${edu.year})")
            }
        }
        val skillItems = parsedTypedSkills()
        if (skillItems.isNotEmpty()) {
            appendLine("Core Skills: ${skillItems.joinToString(", ") { "${it.skill} (${it.skillLevel.label})" }}")
        }
        val projs = parsedProjects()
        if (projs.isNotEmpty()) {
            appendLine("Key Projects:")
            projs.forEach { proj ->
                appendLine("- ${proj.name} (${proj.role}): ${proj.description}")
            }
        }
    }

    companion object {
        /**
         * Authentic KungaCV starter profile modeled directly on `utils/templatePresets.js`
         * from `vend-pixel/kungacv` (Thoko Banda / MobiWorks Africa / University of Malawi).
         */
        fun createExecutiveStarter(
            userId: String,
            candidateName: String,
            candidateEmail: String,
            preset: KungaTemplatePreset = KungaTemplateCatalog.presets.first()
        ): ResumeDocument {
            val safeName = candidateName.ifBlank { "Thoko Banda" }.take(120)
            val safeEmail = candidateEmail.ifBlank { "thoko.banda@kungacv.com" }.take(160)
            return ResumeDocument(
                userId = userId,
                title = "${safeName.split(" ").firstOrNull() ?: "Career"} — ${preset.name}",
                templateId = preset.mappedTemplate.id,
                fullName = safeName,
                jobTitle = "Product Designer & Digital Systems Lead",
                email = safeEmail,
                phone = "+265 888 120 440",
                location = "Lilongwe, Malawi",
                website = "thokobanda.design",
                summary = "Human-centred product designer and digital delivery lead with 6+ years crafting mobile-first, low-bandwidth digital products across Southern Africa. Improved user activation by 28% at MobiWorks Africa and translated field research into clear launch milestones.",
                skills = listOf(
                    Skill(skill = "Product strategy", skillLevel = SkillLevel.EXPERT).toEncodedString(),
                    Skill(skill = "Figma & Design Systems", skillLevel = SkillLevel.EXPERIENCED).toEncodedString(),
                    Skill(skill = "User Research", skillLevel = SkillLevel.EXPERIENCED).toEncodedString(),
                    Skill(skill = "Technical Writing", skillLevel = SkillLevel.SKILLFUL).toEncodedString(),
                    Skill(skill = "Stakeholder facilitation", skillLevel = SkillLevel.SKILLFUL).toEncodedString()
                )
            ).withTypedSections(
                experienceList = listOf(
                    ExperienceEntry(
                        id = "exp_1",
                        role = "Product Designer",
                        company = "MobiWorks Africa",
                        period = "Feb 2021 – Oct 2024",
                        location = "Lilongwe, Malawi",
                        highlights = listOf(
                            "Led cross-functional delivery of customer-facing digital products and improved onboarding activation by 28%.",
                            "Designed mobile-first, low-bandwidth workflows tailored for regional job seekers and small businesses.",
                            "Facilitated weekly design critiques and usability testing sessions across engineering and product teams."
                        )
                    ),
                    ExperienceEntry(
                        id = "exp_2",
                        role = "Associate Consultant",
                        company = "Bright Valley Studio",
                        period = "Feb 2018 – Oct 2021",
                        location = "Blantyre, Malawi",
                        highlights = listOf(
                            "Translated field research into clear service blueprints, interactive prototypes, and measurable launch milestones.",
                            "Co-created a client workshop toolkit adopted across 14 regional digital transformation projects."
                        )
                    ),
                    ExperienceEntry(
                        id = "exp_3",
                        role = "Project Intern",
                        company = "Cedar Labs",
                        period = "Feb 2017 – Oct 2018",
                        location = "Lilongwe, Malawi",
                        highlights = listOf(
                            "Supported a cross-functional product squad with documentation, QA testing, and client discovery workshops."
                        )
                    )
                ),
                educationList = listOf(
                    EducationEntry(
                        id = "edu_1",
                        degree = "BSc Information Systems",
                        institution = "University of Malawi, Zomba",
                        year = "2014 – 2018",
                        details = "Dean's list; concentration in human-centred information systems"
                    ),
                    EducationEntry(
                        id = "edu_2",
                        degree = "MS Office & Digital Communication",
                        institution = "Lilongwe Academy",
                        year = "2012 – 2013",
                        details = "Professional certificate with distinction"
                    )
                ),
                projectList = listOf(
                    ProjectEntry(
                        id = "proj_1",
                        name = "Market Access Toolkit",
                        role = "Lead Researcher",
                        description = "A practical mobile toolkit helping small businesses in Lilongwe and Blantyre reach new local customers.",
                        link = "github.com/vend-pixel/kungacv"
                    ),
                    ProjectEntry(
                        id = "proj_2",
                        name = "Open Data Weekend",
                        role = "Volunteer Organiser",
                        description = "A two-day community event pairing civic questions with clear, accessible data stories.",
                        link = "kungacv.com"
                    )
                )
            )
        }
    }
}

@IgnoreExtraProperties
data class CoverLetterDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val resumeId: String = "",
    val companyName: String = "",
    val targetRole: String = "",
    val jobDescription: String = "",
    val tone: String = "Executive",
    val content: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessageDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val role: String = "user",
    val text: String = "",
    val modelTier: String = GeminiModelTier.GENERAL.modelName,
    val suggestedSummary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
