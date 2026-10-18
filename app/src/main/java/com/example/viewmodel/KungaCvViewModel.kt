package com.example.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiPolishResult
import com.example.ai.GeminiCareerService
import com.example.data.model.ChatMessageDocument
import com.example.data.model.CoverLetterDocument
import com.example.data.model.CvCustomization
import com.example.data.model.CvProfile
import com.example.data.model.CvTemplate
import com.example.data.model.Education
import com.example.data.model.EducationEntry
import com.example.data.model.ExperienceEntry
import com.example.data.model.GeminiModelTier
import com.example.data.model.JobApplicationEntry
import com.example.data.model.JobStage
import com.example.data.model.KungaTemplateCatalog
import com.example.data.model.KungaTemplatePreset
import com.example.data.model.PersonalInfo
import com.example.data.model.ProjectEntry
import com.example.data.model.ResumeDocument
import com.example.data.model.Skill
import com.example.data.model.WorkExperience
import com.example.data.repository.KungaCvRepository
import com.example.export.CvPdfExportEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface DataUiState<out T> {
    data object Loading : DataUiState<Nothing>
    data class Success<T>(val data: T) : DataUiState<T>
    data class Error(val message: String) : DataUiState<Nothing>
}

sealed interface AiTaskState {
    data object Idle : AiTaskState
    data object Loading : AiTaskState
    data class Streaming(val partialText: String) : AiTaskState
    data class PolishReady(val sectionLabel: String, val result: AiPolishResult) : AiTaskState
    data class Success(val message: String) : AiTaskState
    data class Error(val message: String) : AiTaskState
}

class KungaCvViewModel(
    private val repository: KungaCvRepository,
    private val geminiService: GeminiCareerService,
    private val currentUserId: String,
    private val userDisplayName: String,
    private val userEmail: String
) : ViewModel() {

    private val isGuestUser = currentUserId.startsWith("guest") || currentUserId.startsWith("demo")

    private val initialGuestStarter = ResumeDocument.createExecutiveStarter(
        userId = currentUserId,
        candidateName = userDisplayName.ifBlank { "Thoko Ndlovu" },
        candidateEmail = userEmail.ifBlank { "thoko.career@kungacv.design" }
    )

    private val _localResumes = MutableStateFlow<List<ResumeDocument>>(
        if (isGuestUser) listOf(initialGuestStarter) else emptyList()
    )

    private val _localCoverLetters = MutableStateFlow<List<CoverLetterDocument>>(
        if (isGuestUser) listOf(
            CoverLetterDocument(
                id = "guest_cover_1",
                userId = currentUserId,
                resumeId = initialGuestStarter.id,
                companyName = "MobiWorks Design",
                targetRole = "Lead Product Strategist",
                jobDescription = "High-impact design leadership in mobile applications.",
                tone = "Executive",
                content = "Dear Hiring Committee,\n\nI am writing to express my enthusiastic interest in the Lead Product Strategist position. With over a decade of experience designing scalable mobile architectures and publication-grade design systems across emerging markets, I have consistently aligned product vision with measurable operational impact.\n\nThank you for considering my application.\n\nSincerely,\nThoko Ndlovu"
            )
        ) else emptyList()
    )

    private val _localChatMessages = MutableStateFlow<List<ChatMessageDocument>>(
        if (isGuestUser) listOf(
            ChatMessageDocument(
                id = "guest_msg_1",
                userId = currentUserId,
                role = "model",
                text = "Welcome to KungaCV Career Studio! I am your AI Career Coach. I can help polish your executive summary, quantify your experience bullets, or critique your CV structure. What would you like to refine today?",
                modelTier = "gemini-3.8-flash"
            )
        ) else emptyList()
    )

    val resumesState: StateFlow<DataUiState<List<ResumeDocument>>> =
        if (isGuestUser) {
            _localResumes
                .map<List<ResumeDocument>, DataUiState<List<ResumeDocument>>> { list ->
                    if (list.isNotEmpty() && _selectedResumeId.value.isBlank()) {
                        _selectedResumeId.value = list.first().id
                    }
                    DataUiState.Success(list)
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = DataUiState.Success(_localResumes.value)
                )
        } else {
            repository.observeResumes(currentUserId)
                .map<List<ResumeDocument>, DataUiState<List<ResumeDocument>>> { list ->
                    if (list.isNotEmpty() && _selectedResumeId.value.isBlank()) {
                        _selectedResumeId.value = list.first().id
                    }
                    DataUiState.Success(list)
                }
                .catch { error ->
                    Log.w(TAG, "Error observing resumes", error)
                    emit(DataUiState.Error(error.localizedMessage ?: "Failed to load resumes"))
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = DataUiState.Loading
                )
        }

    val coverLettersState: StateFlow<DataUiState<List<CoverLetterDocument>>> =
        if (isGuestUser) {
            _localCoverLetters
                .map<List<CoverLetterDocument>, DataUiState<List<CoverLetterDocument>>> {
                    DataUiState.Success(it)
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = DataUiState.Success(_localCoverLetters.value)
                )
        } else {
            repository.observeCoverLetters(currentUserId)
                .map<List<CoverLetterDocument>, DataUiState<List<CoverLetterDocument>>> {
                    DataUiState.Success(it)
                }
                .catch { error ->
                    Log.w(TAG, "Error observing cover letters", error)
                    emit(DataUiState.Error(error.localizedMessage ?: "Failed to load cover letters"))
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = DataUiState.Loading
                )
        }

    val chatMessagesState: StateFlow<DataUiState<List<ChatMessageDocument>>> =
        if (isGuestUser) {
            _localChatMessages
                .map<List<ChatMessageDocument>, DataUiState<List<ChatMessageDocument>>> {
                    DataUiState.Success(it)
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = DataUiState.Success(_localChatMessages.value)
                )
        } else {
            repository.observeChatMessages(currentUserId)
                .map<List<ChatMessageDocument>, DataUiState<List<ChatMessageDocument>>> {
                    DataUiState.Success(it)
                }
                .catch { error ->
                    Log.w(TAG, "Error observing chat messages", error)
                    emit(DataUiState.Error(error.localizedMessage ?: "Failed to load chat history"))
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = DataUiState.Loading
                )
        }

    private val _selectedResumeId = MutableStateFlow("")
    val selectedResumeId: StateFlow<String> = _selectedResumeId.asStateFlow()

    private val _selectedModelTier = MutableStateFlow(GeminiModelTier.GENERAL)
    val selectedModelTier: StateFlow<GeminiModelTier> = _selectedModelTier.asStateFlow()

    private val _aiTaskState = MutableStateFlow<AiTaskState>(AiTaskState.Idle)
    val aiTaskState: StateFlow<AiTaskState> = _aiTaskState.asStateFlow()

    private val _chatStreamingState = MutableStateFlow<AiTaskState>(AiTaskState.Idle)
    val chatStreamingState: StateFlow<AiTaskState> = _chatStreamingState.asStateFlow()

    private val _coverLetterDraft = MutableStateFlow("")
    val coverLetterDraft: StateFlow<String> = _coverLetterDraft.asStateFlow()

    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    private val _saveStatusLabel = MutableStateFlow("Saved")
    val saveStatusLabel: StateFlow<String> = _saveStatusLabel.asStateFlow()

    private val _activeCustomization = MutableStateFlow(CvCustomization())
    val activeCustomization: StateFlow<CvCustomization> = _activeCustomization.asStateFlow()

    private val _exportWarnings = MutableStateFlow<List<String>>(emptyList())
    val exportWarnings: StateFlow<List<String>> = _exportWarnings.asStateFlow()

    private val _jobApplications = MutableStateFlow(
        listOf(
            JobApplicationEntry(
                id = "job_1",
                company = "MobiWorks Africa",
                role = "Senior Product Designer",
                stage = JobStage.INTERVIEWING,
                nextFollowUp = "24 Oct",
                linkedResumeTitle = "Thoko — Classic Blue",
                linkedCoverLetterTitle = "MobiWorks Cover Letter"
            ),
            JobApplicationEntry(
                id = "job_2",
                company = "Bright Valley Studio",
                role = "Lead UX Strategist",
                stage = JobStage.APPLIED,
                nextFollowUp = "28 Oct",
                linkedResumeTitle = "Thoko — Sage Sidebar",
                linkedCoverLetterTitle = ""
            )
        )
    )
    val jobApplications: StateFlow<List<JobApplicationEntry>> = _jobApplications.asStateFlow()

    fun clearBannerMessage() {
        _bannerMessage.value = null
    }

    fun dismissAiTaskState() {
        _aiTaskState.value = AiTaskState.Idle
    }

    fun dismissExportWarnings() {
        _exportWarnings.value = emptyList()
    }

    fun selectResume(resumeId: String) {
        _selectedResumeId.value = resumeId
    }

    fun selectModelTier(tier: GeminiModelTier) {
        _selectedModelTier.value = tier
    }

    fun updateCoverLetterDraft(text: String) {
        _coverLetterDraft.value = text
    }

    fun currentActiveResume(): ResumeDocument? {
        val state = resumesState.value
        if (state !is DataUiState.Success) return null
        val id = _selectedResumeId.value
        return state.data.firstOrNull { it.id == id } ?: state.data.firstOrNull()
    }

    fun currentActiveCvProfile(): CvProfile? {
        val active = currentActiveResume() ?: return null
        val preset = KungaTemplateCatalog.fromPresetId(_activeCustomization.value.presetId)
        return active.toCvProfile(preset).copy(
            customization = _activeCustomization.value
        )
    }

    fun createStarterResume(preset: KungaTemplatePreset = KungaTemplateCatalog.presets.first()) {
        viewModelScope.launch {
            _saveStatusLabel.value = "Saving…"
            val starter = ResumeDocument.createExecutiveStarter(
                userId = currentUserId,
                candidateName = userDisplayName,
                candidateEmail = userEmail,
                preset = preset
            )
            _activeCustomization.value = CvCustomization(
                presetId = preset.id,
                presetName = preset.name,
                accentHex = preset.accentHex,
                columns = preset.columns,
                direction = preset.direction,
                skillDisplayStyle = preset.skillStyle,
                fontFamily = preset.nameFont
            )
            if (isGuestUser) {
                val list = _localResumes.value.toMutableList()
                list.add(0, starter)
                _localResumes.value = list
                _selectedResumeId.value = starter.id
                _saveStatusLabel.value = "Saved (Local)"
                _bannerMessage.value = "Created '${starter.title}'"
                return@launch
            }
            val result = repository.createResume(starter)
            result.onSuccess { newId ->
                _selectedResumeId.value = newId
                _saveStatusLabel.value = "Saved"
                _bannerMessage.value = "Created '${starter.title}'"
            }.onFailure { err ->
                _saveStatusLabel.value = "Not saved — retry"
                _bannerMessage.value = err.localizedMessage ?: "Failed to create starter CV"
            }
        }
    }

    fun createBlankResume(title: String, template: CvTemplate) {
        viewModelScope.launch {
            _saveStatusLabel.value = "Saving…"
            val preset = KungaTemplateCatalog.fromCvTemplate(template)
            val blank = ResumeDocument(
                userId = currentUserId,
                title = title.ifBlank { "Untitled resume" },
                templateId = template.id,
                fullName = userDisplayName.ifBlank { "Your Name" },
                jobTitle = "",
                email = userEmail,
                phone = "",
                location = "",
                website = "",
                summary = ""
            )
            _activeCustomization.value = CvCustomization(
                presetId = preset.id,
                presetName = preset.name,
                accentHex = preset.accentHex,
                columns = preset.columns,
                direction = preset.direction,
                skillDisplayStyle = preset.skillStyle,
                fontFamily = preset.nameFont
            )
            if (isGuestUser) {
                val guestBlank = blank.copy(id = "resume_${System.currentTimeMillis()}")
                val list = _localResumes.value.toMutableList()
                list.add(0, guestBlank)
                _localResumes.value = list
                _selectedResumeId.value = guestBlank.id
                _saveStatusLabel.value = "Saved (Local)"
                _bannerMessage.value = "Created '${guestBlank.title}'"
                return@launch
            }
            repository.createResume(blank).onSuccess { newId ->
                _selectedResumeId.value = newId
                _saveStatusLabel.value = "Saved"
                _bannerMessage.value = "Created '${blank.title}'"
            }.onFailure { err ->
                _saveStatusLabel.value = "Not saved — retry"
                _bannerMessage.value = err.localizedMessage ?: "Failed to create resume"
            }
        }
    }

    fun duplicateResume(source: ResumeDocument) {
        viewModelScope.launch {
            _saveStatusLabel.value = "Saving…"
            val copyDoc = source.copy(
                id = "",
                title = "${source.title} (Copy)".take(120)
            )
            if (isGuestUser) {
                val guestCopy = copyDoc.copy(id = "resume_${System.currentTimeMillis()}")
                val list = _localResumes.value.toMutableList()
                list.add(0, guestCopy)
                _localResumes.value = list
                _selectedResumeId.value = guestCopy.id
                _saveStatusLabel.value = "Saved (Local)"
                _bannerMessage.value = "Duplicated '${source.title}'"
                return@launch
            }
            repository.createResume(copyDoc).onSuccess { newId ->
                _selectedResumeId.value = newId
                _saveStatusLabel.value = "Saved"
                _bannerMessage.value = "Duplicated '${source.title}'"
            }.onFailure { err ->
                _saveStatusLabel.value = "Not saved — retry"
                _bannerMessage.value = err.localizedMessage ?: "Failed to duplicate resume"
            }
        }
    }

    fun applyPresetToActiveOrCreate(preset: KungaTemplatePreset) {
        val nextCust = _activeCustomization.value.copy(
            presetId = preset.id,
            presetName = preset.name,
            accentHex = preset.accentHex,
            columns = preset.columns,
            direction = preset.direction,
            skillDisplayStyle = preset.skillStyle,
            fontFamily = preset.nameFont
        )
        _activeCustomization.value = nextCust
        val active = currentActiveResume()
        if (active == null) {
            createStarterResume(preset)
        } else {
            saveResumeChanges(
                active.copy(templateId = preset.mappedTemplate.id),
                showToast = false
            )
            _bannerMessage.value = "Applied '${preset.name}' template style"
        }
    }

    fun updateCustomization(updated: CvCustomization) {
        _activeCustomization.value = updated
        val active = currentActiveResume() ?: return
        val preset = KungaTemplateCatalog.fromPresetId(updated.presetId)
        if (active.templateId != preset.mappedTemplate.id) {
            saveResumeChanges(active.copy(templateId = preset.mappedTemplate.id), showToast = false)
        }
    }

    fun moveSection(sectionKey: String, delta: Int) {
        val currentOrder = _activeCustomization.value.sectionOrder.toMutableList()
        val idx = currentOrder.indexOf(sectionKey)
        if (idx == -1) return
        val target = (idx + delta).coerceIn(0, currentOrder.lastIndex)
        if (target == idx) return
        val item = currentOrder.removeAt(idx)
        currentOrder.add(target, item)
        _activeCustomization.value = _activeCustomization.value.copy(sectionOrder = currentOrder)
    }

    fun toggleSectionVisibility(sectionKey: String) {
        val hidden = _activeCustomization.value.hiddenSections.toMutableSet()
        if (!hidden.add(sectionKey)) {
            hidden.remove(sectionKey)
        }
        _activeCustomization.value = _activeCustomization.value.copy(hiddenSections = hidden)
    }

    fun saveResumeChanges(updated: ResumeDocument, showToast: Boolean = true) {
        viewModelScope.launch {
            _saveStatusLabel.value = "Saving…"
            if (isGuestUser) {
                val list = _localResumes.value.toMutableList()
                val existingIdx = list.indexOfFirst { it.id == updated.id }
                val docToSave = if (updated.id.isBlank()) updated.copy(id = "resume_${System.currentTimeMillis()}") else updated
                if (existingIdx >= 0) {
                    list[existingIdx] = docToSave
                } else {
                    list.add(0, docToSave)
                }
                _localResumes.value = list
                _selectedResumeId.value = docToSave.id
                _saveStatusLabel.value = "Saved"
                if (showToast) _bannerMessage.value = "Changes saved"
                return@launch
            }
            if (updated.id.isBlank()) {
                repository.createResume(updated).onSuccess { newId ->
                    _selectedResumeId.value = newId
                    _saveStatusLabel.value = "Saved"
                    if (showToast) _bannerMessage.value = "Saved to KungaCV cloud"
                }.onFailure { err ->
                    _saveStatusLabel.value = "Not saved — retry"
                    _bannerMessage.value = err.localizedMessage ?: "Failed to save resume"
                }
            } else {
                repository.updateResume(updated).onSuccess {
                    _saveStatusLabel.value = "Saved"
                    if (showToast) _bannerMessage.value = "Changes saved"
                }.onFailure { err ->
                    _saveStatusLabel.value = "Not saved — retry"
                    _bannerMessage.value = err.localizedMessage ?: "Failed to update resume"
                }
            }
        }
    }

    fun updatePersonalInfo(personalInfo: PersonalInfo, resumeTitle: String? = null) {
        val active = currentActiveResume() ?: return
        val updated = active.copy(
            title = (resumeTitle ?: active.title).ifBlank { "Untitled resume" }.take(120),
            fullName = personalInfo.fullName.ifBlank { "Your Name" }.take(120),
            jobTitle = personalInfo.jobTitle.take(120),
            email = personalInfo.email.take(160),
            phone = personalInfo.phone.take(60),
            location = personalInfo.address.take(120),
            website = personalInfo.links.primaryDisplayLink().take(200),
            summary = personalInfo.summary.take(3000)
        )
        saveResumeChanges(updated)
    }

    fun addOrUpdateWorkExperience(workExp: WorkExperience) {
        addOrUpdateExperience(workExp.toExperienceEntry())
    }

    fun addOrUpdateTypedEducation(education: Education) {
        addOrUpdateEducation(education.toEducationEntry())
    }

    fun addOrUpdateSkill(skill: Skill) {
        val active = currentActiveResume() ?: return
        val existingSkills = active.parsedTypedSkills().toMutableList()
        val idx = existingSkills.indexOfFirst {
            it.id == skill.id || it.skill.equals(skill.skill, ignoreCase = true)
        }
        if (idx >= 0) {
            existingSkills[idx] = skill
        } else {
            existingSkills.add(skill)
        }
        val encoded = existingSkills.map { it.toEncodedString() }.take(50)
        saveResumeChanges(active.copy(skills = encoded), showToast = false)
    }

    fun removeTypedSkill(skillName: String) {
        val active = currentActiveResume() ?: return
        val remaining = active.parsedTypedSkills()
            .filterNot { it.skill.equals(skillName, ignoreCase = true) }
            .map { it.toEncodedString() }
        saveResumeChanges(active.copy(skills = remaining), showToast = false)
    }

    fun updateTemplateForActiveResume(template: CvTemplate) {
        val active = currentActiveResume() ?: return
        val preset = KungaTemplateCatalog.fromCvTemplate(template)
        _activeCustomization.value = _activeCustomization.value.copy(
            presetId = preset.id,
            presetName = preset.name,
            accentHex = preset.accentHex,
            columns = preset.columns,
            direction = preset.direction,
            skillDisplayStyle = preset.skillStyle,
            fontFamily = preset.nameFont
        )
        saveResumeChanges(active.copy(templateId = template.id), showToast = false)
    }

    fun deleteResume(resumeId: String) {
        viewModelScope.launch {
            if (isGuestUser) {
                val list = _localResumes.value.filterNot { it.id == resumeId }
                _localResumes.value = list
                if (_selectedResumeId.value == resumeId) {
                    _selectedResumeId.value = list.firstOrNull()?.id.orEmpty()
                }
                _bannerMessage.value = "Resume deleted"
                return@launch
            }
            repository.deleteResume(resumeId).onSuccess {
                if (_selectedResumeId.value == resumeId) {
                    _selectedResumeId.value = ""
                }
                _bannerMessage.value = "Resume deleted"
            }.onFailure { err ->
                _bannerMessage.value = err.localizedMessage ?: "Failed to delete resume"
            }
        }
    }

    fun addOrUpdateExperience(entry: ExperienceEntry) {
        val active = currentActiveResume() ?: return
        val currentList = active.parsedExperiences().toMutableList()
        val existingIdx = currentList.indexOfFirst { it.id == entry.id }
        if (existingIdx >= 0) {
            currentList[existingIdx] = entry
        } else {
            currentList.add(0, entry)
        }
        saveResumeChanges(active.withTypedSections(experienceList = currentList))
    }

    fun removeExperience(entryId: String) {
        val active = currentActiveResume() ?: return
        val filtered = active.parsedExperiences().filterNot { it.id == entryId }
        saveResumeChanges(active.withTypedSections(experienceList = filtered))
    }

    fun addOrUpdateEducation(entry: EducationEntry) {
        val active = currentActiveResume() ?: return
        val currentList = active.parsedEducations().toMutableList()
        val existingIdx = currentList.indexOfFirst { it.id == entry.id }
        if (existingIdx >= 0) {
            currentList[existingIdx] = entry
        } else {
            currentList.add(0, entry)
        }
        saveResumeChanges(active.withTypedSections(educationList = currentList))
    }

    fun removeEducation(entryId: String) {
        val active = currentActiveResume() ?: return
        val filtered = active.parsedEducations().filterNot { it.id == entryId }
        saveResumeChanges(active.withTypedSections(educationList = filtered))
    }

    fun addOrUpdateProject(entry: ProjectEntry) {
        val active = currentActiveResume() ?: return
        val currentList = active.parsedProjects().toMutableList()
        val existingIdx = currentList.indexOfFirst { it.id == entry.id }
        if (existingIdx >= 0) {
            currentList[existingIdx] = entry
        } else {
            currentList.add(0, entry)
        }
        saveResumeChanges(active.withTypedSections(projectList = currentList))
    }

    fun removeProject(entryId: String) {
        val active = currentActiveResume() ?: return
        val filtered = active.parsedProjects().filterNot { it.id == entryId }
        saveResumeChanges(active.withTypedSections(projectList = filtered))
    }

    // --- JOB TRACKER (/job-tracker from vend-pixel/kungacv) ---

    fun addJobApplication(
        company: String,
        role: String,
        stage: JobStage,
        nextFollowUp: String,
        linkedResumeTitle: String,
        linkedCoverLetterTitle: String
    ) {
        if (company.isBlank() || role.isBlank()) {
            _bannerMessage.value = "Enter both company and role to track an application."
            return
        }
        val entry = JobApplicationEntry(
            id = UUID.randomUUID().toString(),
            company = company.trim(),
            role = role.trim(),
            stage = stage,
            nextFollowUp = nextFollowUp.trim(),
            linkedResumeTitle = linkedResumeTitle.trim(),
            linkedCoverLetterTitle = linkedCoverLetterTitle.trim()
        )
        _jobApplications.value = listOf(entry) + _jobApplications.value
        _bannerMessage.value = "Added '${entry.role}' at ${entry.company} to tracker"
    }

    fun updateJobStage(id: String, newStage: JobStage) {
        _jobApplications.value = _jobApplications.value.map {
            if (it.id == id) it.copy(stage = newStage) else it
        }
    }

    fun removeJobApplication(id: String) {
        _jobApplications.value = _jobApplications.value.filterNot { it.id == id }
    }

    // --- AI BULLET & SUMMARY POLISH ---

    fun requestAiPolish(rawText: String, sectionLabel: String) {
        if (rawText.isBlank()) {
            _bannerMessage.value = "Enter some draft text first before polishing with AI."
            return
        }
        viewModelScope.launch {
            _aiTaskState.value = AiTaskState.Loading
            try {
                val result = geminiService.polishResumeText(
                    rawText = rawText,
                    sectionLabel = sectionLabel,
                    activeResume = currentActiveResume()
                )
                _aiTaskState.value = AiTaskState.PolishReady(sectionLabel, result)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI polish timed out", e)
                _aiTaskState.value = AiTaskState.Error("AI polish timed out after 60 seconds. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI polish failed", e)
                _aiTaskState.value = AiTaskState.Error(e.localizedMessage ?: "AI polish failed.")
            }
        }
    }

    // --- TAILORED COVER LETTER GENERATION ---

    fun generateAndSaveCoverLetter(
        companyName: String,
        targetRole: String,
        jobDescription: String,
        tone: String
    ) {
        val active = currentActiveResume()
        if (active == null) {
            _bannerMessage.value = "Create or select a resume in CV Studio first."
            return
        }
        if (companyName.isBlank() || targetRole.isBlank()) {
            _bannerMessage.value = "Please enter both the target company and role title."
            return
        }
        viewModelScope.launch {
            _aiTaskState.value = AiTaskState.Loading
            var finalContent = ""
            try {
                geminiService.streamTailoredCoverLetter(
                    resume = active,
                    companyName = companyName,
                    targetRole = targetRole,
                    jobDescription = jobDescription,
                    tone = tone
                ).collect { partial ->
                    finalContent = partial
                    _coverLetterDraft.value = partial
                    _aiTaskState.value = AiTaskState.Streaming(partial)
                }

                if (finalContent.isNotBlank()) {
                    val doc = CoverLetterDocument(
                        userId = currentUserId,
                        resumeId = active.id,
                        companyName = companyName.trim(),
                        targetRole = targetRole.trim(),
                        jobDescription = jobDescription.trim(),
                        tone = tone,
                        content = finalContent.trim()
                    )
                    repository.saveCoverLetter(doc)
                    _aiTaskState.value = AiTaskState.Success("Cover letter generated & saved")
                } else {
                    _aiTaskState.value = AiTaskState.Error("Model returned an empty cover letter.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Cover letter generation timed out", e)
                _aiTaskState.value = AiTaskState.Error("Cover letter generation timed out after 60s.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Cover letter generation failed", e)
                _aiTaskState.value = AiTaskState.Error(e.localizedMessage ?: "Cover letter generation failed.")
            }
        }
    }

    fun saveExistingCoverLetter(letter: CoverLetterDocument) {
        viewModelScope.launch {
            repository.saveCoverLetter(letter).onSuccess {
                _bannerMessage.value = "Cover letter saved"
            }.onFailure { err ->
                _bannerMessage.value = err.localizedMessage ?: "Failed to save cover letter"
            }
        }
    }

    fun deleteCoverLetter(letterId: String) {
        viewModelScope.launch {
            repository.deleteCoverLetter(letterId).onSuccess {
                _bannerMessage.value = "Cover letter deleted"
            }
        }
    }

    // --- MULTI-TURN GEMINI CAREER COACH CHATBOT ---

    fun sendCareerCoachMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank()) return

        val tier = _selectedModelTier.value
        val existingHistory = (chatMessagesState.value as? DataUiState.Success)?.data.orEmpty()
        val activeResume = currentActiveResume()

        viewModelScope.launch {
            _chatStreamingState.value = AiTaskState.Loading

            repository.appendChatMessage(
                ChatMessageDocument(
                    userId = currentUserId,
                    role = "user",
                    text = trimmed,
                    modelTier = tier.modelName,
                    suggestedSummary = ""
                )
            )

            var fullModelText = ""
            try {
                geminiService.streamCareerCoachReply(
                    tier = tier,
                    history = existingHistory,
                    userPrompt = trimmed,
                    activeResume = activeResume
                ).collect { accumulated ->
                    fullModelText = accumulated
                    val (cleanVisible, _) = GeminiCareerService.extractSuggestedSummary(accumulated)
                    _chatStreamingState.value = AiTaskState.Streaming(cleanVisible)
                }

                if (fullModelText.isNotBlank()) {
                    val (cleanText, suggestedSnippet) = GeminiCareerService.extractSuggestedSummary(fullModelText)
                    repository.appendChatMessage(
                        ChatMessageDocument(
                            userId = currentUserId,
                            role = "model",
                            text = cleanText.ifBlank { fullModelText },
                            modelTier = tier.modelName,
                            suggestedSummary = suggestedSnippet
                        )
                    )
                    _chatStreamingState.value = AiTaskState.Idle
                } else {
                    _chatStreamingState.value = AiTaskState.Error("Model returned an empty response.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Career coach chat timed out", e)
                _chatStreamingState.value = AiTaskState.Error(
                    "Response timed out after 60 seconds. Try switching to Fast Polish or retry."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Career coach chat failed", e)
                _chatStreamingState.value = AiTaskState.Error(
                    e.localizedMessage ?: "Failed to generate Career Coach reply."
                )
            }
        }
    }

    fun applySuggestedSummaryToActiveCv(snippet: String) {
        val active = currentActiveResume()
        if (active == null) {
            _bannerMessage.value = "Create or select a resume first to apply this summary."
            return
        }
        saveResumeChanges(active.copy(summary = snippet.take(3000)), showToast = false)
        _bannerMessage.value = "Applied AI summary to '${active.title}'"
    }

    fun clearCareerCoachChat() {
        viewModelScope.launch {
            repository.clearChatHistory(currentUserId).onSuccess {
                _chatStreamingState.value = AiTaskState.Idle
                _bannerMessage.value = "Coaching conversation cleared"
            }
        }
    }

    // --- NATIVE PDF EXPORT WITH EXPORT CHECK WARNINGS ---

    fun requestExportResumePdf(context: Context, force: Boolean = false) {
        val profile = currentActiveCvProfile()
        if (profile == null) {
            _bannerMessage.value = "Select a resume first to export PDF."
            return
        }
        val warnings = profile.exportWarnings()
        if (warnings.isNotEmpty() && !force) {
            _exportWarnings.value = warnings
            return
        }
        _exportWarnings.value = emptyList()
        exportActiveResumePdf(context)
    }

    fun exportActiveResumePdf(context: Context) {
        val active = currentActiveResume()
        if (active == null) {
            _bannerMessage.value = "Select a resume first to export PDF."
            return
        }
        viewModelScope.launch {
            _bannerMessage.value = "Rendering print-ready A4 PDF..."
            CvPdfExportEngine.exportAndShareResumePdf(
                context = context,
                resume = active,
                accentHex = _activeCustomization.value.accentHex,
                customization = _activeCustomization.value
            )
                .onSuccess {
                    _bannerMessage.value = "PDF ready to share or save"
                }
                .onFailure { err ->
                    Log.e(TAG, "PDF export failed", err)
                    _bannerMessage.value = "PDF export failed: ${err.localizedMessage}"
                }
        }
    }

    fun exportCoverLetterPdf(context: Context, letter: CoverLetterDocument) {
        val active = currentActiveResume()
        val candidateName = active?.fullName?.ifBlank { userDisplayName } ?: userDisplayName
        val candidateContact = listOfNotNull(active?.email?.ifBlank { userEmail }, active?.phone, active?.location)
            .filter { it.isNotBlank() }
            .joinToString("  •  ")
        viewModelScope.launch {
            CvPdfExportEngine.exportAndShareCoverLetterPdf(
                context = context,
                letter = letter,
                candidateName = candidateName,
                candidateContact = candidateContact
            ).onFailure { err ->
                _bannerMessage.value = "PDF export failed: ${err.localizedMessage}"
            }
        }
    }

    private companion object {
        const val TAG = "KungaCvViewModel"
    }
}
