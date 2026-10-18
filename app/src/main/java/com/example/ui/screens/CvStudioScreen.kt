package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Education
import com.example.data.model.KungaTemplateCatalog
import com.example.data.model.PersonalInfo
import com.example.data.model.ProfileLinks
import com.example.data.model.ProjectEntry
import com.example.data.model.ResumeDocument
import com.example.data.model.Skill
import com.example.data.model.SkillLevel
import com.example.data.model.TemplateFilterCategory
import com.example.data.model.WorkExperience
import com.example.ui.theme.Brand50
import com.example.ui.theme.Brand600
import com.example.ui.theme.Brand700
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.Ink400
import com.example.ui.theme.Ink500
import com.example.ui.theme.Ink800
import com.example.ui.theme.Ink950
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmInputSurface
import com.example.ui.theme.WarmPaperBg
import com.example.viewmodel.AiTaskState
import com.example.viewmodel.DataUiState
import com.example.viewmodel.KungaCvViewModel

import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.HorizontalDivider
import com.example.ui.theme.JetBrainsMonoFontFamily

enum class StudioSubMode(val label: String, val tag: String) {
    EDITOR("Content", "mode_cv_editor"),
    WORKSPACE("Templates", "mode_workspace_templates"),
    CUSTOMIZE("Typography & Grid", "mode_customize_cv")
}

enum class EditorSectionStep(val indexCode: String, val title: String, val subtitle: String, val tag: String) {
    PERSONAL("01", "Profile", "Contact, links & executive summary", "tab_profile"),
    EXPERIENCE("02", "Experience", "Roles, employers & quantified bullets", "tab_experience"),
    EDUCATION("03", "Education", "Degrees, universities & honors", "tab_education"),
    SKILLS("04", "Disciplines", "Competencies & 4-tier proficiency", "tab_skills"),
    PROJECTS("05", "Selected Work", "Key initiatives & portfolio links", "tab_projects")
}

/**
 * KungaCV Studio Screen combining the mobile-adapted `/resume` Workspace Dashboard
 * and `/resume/create` Structured CV Profile Editor + Customizer from `vend-pixel/kungacv`.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CvStudioScreen(
    resumesState: DataUiState<List<ResumeDocument>>,
    selectedResumeId: String,
    aiTaskState: AiTaskState,
    viewModel: KungaCvViewModel,
    onNavigateToPreview: () -> Unit,
    onNavigateToCoach: () -> Unit
) {
    var subMode by rememberSaveable { mutableStateOf(StudioSubMode.EDITOR) }
    var activeStepIndex by rememberSaveable { mutableIntStateOf(0) }
    val activeStep = EditorSectionStep.entries[activeStepIndex]
    var filterCategory by rememberSaveable { mutableStateOf(TemplateFilterCategory.ALL) }

    val customization by viewModel.activeCustomization.collectAsStateWithLifecycle()

    var showCreateResumeDialog by remember { mutableStateOf(false) }
    var editingWorkExp by remember { mutableStateOf<WorkExperience?>(null) }
    var editingEducation by remember { mutableStateOf<Education?>(null) }
    var editingProject by remember { mutableStateOf<ProjectEntry?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmPaperBg),
        contentAlignment = Alignment.TopCenter
    ) {
        when (resumesState) {
            is DataUiState.Loading -> {
                KungaWorkspaceLoadingState(label = "Loading your KungaCV workspace…")
            }

            is DataUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = resumesState.message,
                        color = CrimsonError,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            is DataUiState.Success -> {
                val resumes = resumesState.data
                val activeResume = resumes.firstOrNull { it.id == selectedResumeId }
                    ?: resumes.firstOrNull()

                if (resumes.isEmpty() || activeResume == null) {
                    // Empty Workspace Dashboard matching app/resume/page.jsx
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 18.dp)
                            .widthIn(max = 680.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "YOUR CAREER STUDIO",
                                style = MaterialTheme.typography.labelSmall,
                                color = Brand700
                            )
                            Text(
                                text = "Start building your resume",
                                style = MaterialTheme.typography.displayMedium,
                                color = Ink950
                            )
                            Text(
                                text = "Choose a design you like. Add your story. Switch templates whenever you want.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ink500
                            )
                        }

                        // Next-step dark banner from globals.css (.workspace-next-step)
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Ink950),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "YOUR FIRST MOVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF7DBBFF)
                                )
                                Text(
                                    text = "Pick a look, then make it yours",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White
                                )
                                Text(
                                    text = "Start with KungaCV's starter profile or pick a template below. You can change the design later.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.78f)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { viewModel.createStarterResume() },
                                        shape = RoundedCornerShape(999.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Brand600,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.testTag("create_starter_cv_button")
                                    ) {
                                        Text("Start with Sample CV")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { showCreateResumeDialog = true },
                                        shape = RoundedCornerShape(999.dp),
                                        border = BorderStroke(1.dp, Ink800),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                        modifier = Modifier.testTag("create_blank_cv_button")
                                    ) {
                                        Text("Blank CV")
                                    }
                                }
                            }
                        }

                        KungaTemplateGallerySection(
                            activeCategory = filterCategory,
                            selectedPresetId = customization.presetId,
                            onSelectCategory = { filterCategory = it },
                            onUsePreset = { preset ->
                                viewModel.createStarterResume(preset)
                                subMode = StudioSubMode.EDITOR
                            },
                            onStartFromScratch = { showCreateResumeDialog = true }
                        )
                    }
                } else {
                    val preset = KungaTemplateCatalog.fromPresetId(customization.presetId)
                    val activeProfile = activeResume.toCvProfile(preset).copy(
                        customization = customization
                    )

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Minimalist Underline Sub-Mode Switcher (Read.cv / Linear style)
                        Surface(
                            color = WarmPaperBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    StudioSubMode.entries.forEach { mode ->
                                        val selected = subMode == mode
                                        Column(
                                            modifier = Modifier
                                                .clickable { subMode = mode }
                                                .padding(top = 4.dp)
                                                .testTag(mode.tag),
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            Text(
                                                text = mode.label,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                color = if (selected) Ink950 else Ink500,
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .width(if (selected) 48.dp else 0.dp)
                                                    .height(2.dp)
                                                    .background(if (selected) Ink950 else Color.Transparent)
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(color = WarmBorder)
                            }
                        }

                        when (subMode) {
                            StudioSubMode.WORKSPACE -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                        .widthIn(max = 680.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // Continue where you left off banner (.workspace-next-step)
                                    Card(
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = Ink950),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(18.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "CONTINUE WHERE YOU LEFT OFF",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFF7DBBFF)
                                                )
                                                Text(
                                                    text = "Keep shaping ${activeProfile.title}",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "${activeProfile.completionPercentage()}% complete · ${preset.name}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.75f)
                                                )
                                            }
                                            Button(
                                                onClick = { subMode = StudioSubMode.EDITOR },
                                                shape = RoundedCornerShape(999.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Brand600)
                                            ) {
                                                Text("Continue")
                                            }
                                        }
                                    }

                                    // Saved Resumes Section ("Your resumes" from app/resume/page.jsx)
                                    Card(
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, WarmBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(18.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "SAVED WORK",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Ink500
                                                    )
                                                    Text(
                                                        text = "Your resumes (${resumes.size})",
                                                        style = MaterialTheme.typography.titleLarge,
                                                        color = Ink950
                                                    )
                                                }
                                                OutlinedButton(
                                                    onClick = { showCreateResumeDialog = true },
                                                    shape = RoundedCornerShape(999.dp),
                                                    border = BorderStroke(1.dp, WarmBorder),
                                                    modifier = Modifier.testTag("new_resume_chip")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("New CV", color = Ink950)
                                                }
                                            }

                                            resumes.forEachIndexed { idx, doc ->
                                                val isSelected = doc.id == activeResume.id
                                                Surface(
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = if (isSelected) Brand50 else WarmInputSurface,
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (isSelected) Brand600 else WarmBorder
                                                    ),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            viewModel.selectResume(doc.id)
                                                            subMode = StudioSubMode.EDITOR
                                                        }
                                                        .testTag("resume_chip_${doc.id}")
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = doc.title,
                                                                style = MaterialTheme.typography.titleSmall,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = Ink950
                                                            )
                                                            Text(
                                                                text = "${if (idx == 0) "Recently opened" else "Saved"} · ${doc.fullName.ifBlank { "Untitled" }}",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = Ink500
                                                            )
                                                        }
                                                        Row {
                                                            IconButton(
                                                                onClick = { viewModel.duplicateResume(doc) },
                                                                modifier = Modifier.testTag("duplicate_resume_${doc.id}")
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.ContentCopy,
                                                                    contentDescription = "Duplicate resume",
                                                                    tint = Ink500,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            }
                                                            if (resumes.size > 1) {
                                                                IconButton(
                                                                    onClick = { viewModel.deleteResume(doc.id) },
                                                                    modifier = Modifier.testTag("delete_resume_${doc.id}")
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.DeleteOutline,
                                                                        contentDescription = "Delete resume",
                                                                        tint = CrimsonError,
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 12 Template Presets Gallery
                                    KungaTemplateGallerySection(
                                        activeCategory = filterCategory,
                                        selectedPresetId = customization.presetId,
                                        onSelectCategory = { filterCategory = it },
                                        onUsePreset = { chosen ->
                                            viewModel.applyPresetToActiveOrCreate(chosen)
                                            subMode = StudioSubMode.EDITOR
                                        },
                                        onStartFromScratch = { showCreateResumeDialog = true }
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }

                            StudioSubMode.CUSTOMIZE -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                        .widthIn(max = 680.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    CvCustomizationPanel(
                                        customization = customization,
                                        onSelectPreset = { viewModel.applyPresetToActiveOrCreate(it) },
                                        onUpdateCustomization = { viewModel.updateCustomization(it) },
                                        onMoveSection = { key, delta -> viewModel.moveSection(key, delta) },
                                        onToggleVisibility = { key -> viewModel.toggleSectionVisibility(key) }
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }

                            StudioSubMode.EDITOR -> {
                                ResumeEditorSplitLayout(
                                    activeProfile = activeProfile,
                                    customization = customization,
                                    preset = preset,
                                    activeResume = activeResume,
                                    activeStepIndex = activeStepIndex,
                                    onSelectStep = { activeStepIndex = it },
                                    aiTaskState = aiTaskState,
                                    viewModel = viewModel,
                                    onNavigateToCoach = onNavigateToCoach,
                                    onOpenCustomize = { subMode = StudioSubMode.CUSTOMIZE },
                                    onEditWorkExp = { editingWorkExp = it },
                                    onEditEducation = { editingEducation = it },
                                    onEditProject = { editingProject = it },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    // Editorial Variations Selection Dialog
                    if (aiTaskState is AiTaskState.PolishReady) {
                        AlertDialog(
                            onDismissRequest = { viewModel.dismissAiTaskState() },
                            title = {
                                Text("Editorial Revisions")
                            },
                            text = {
                                Column(
                                    modifier = Modifier.verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = aiTaskState.result.coachingTip,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Brand700
                                    )
                                    aiTaskState.result.variations.forEachIndexed { idx, variation ->
                                        Card(
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = WarmInputSurface),
                                            border = BorderStroke(1.dp, WarmBorder),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (aiTaskState.sectionLabel.contains("Summary", ignoreCase = true)) {
                                                        viewModel.saveResumeChanges(
                                                            activeResume.copy(summary = variation)
                                                        )
                                                    } else {
                                                        val firstExp = activeResume
                                                            .parsedExperiences()
                                                            .firstOrNull()
                                                        if (firstExp != null) {
                                                            viewModel.addOrUpdateExperience(
                                                                firstExp.copy(
                                                                    highlights = firstExp.highlights + variation
                                                                )
                                                            )
                                                        }
                                                    }
                                                    viewModel.dismissAiTaskState()
                                                }
                                                .testTag("ai_variation_card_$idx")
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = "Option ${idx + 1} (Tap to apply)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Brand600
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = variation,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Ink950
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { viewModel.dismissAiTaskState() }) {
                                    Text("Close")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateResumeDialog) {
        CreateResumeModal(
            onDismiss = { showCreateResumeDialog = false },
            onCreate = { title, template ->
                viewModel.createBlankResume(title, template)
                showCreateResumeDialog = false
                subMode = StudioSubMode.EDITOR
            }
        )
    }

    val expToEdit = editingWorkExp
    if (expToEdit != null) {
        WorkExperienceEditorDialog(
            initial = expToEdit,
            onDismiss = { editingWorkExp = null },
            onSave = { saved ->
                viewModel.addOrUpdateWorkExperience(saved)
                editingWorkExp = null
            }
        )
    }

    val eduToEdit = editingEducation
    if (eduToEdit != null) {
        StructuredEducationEditorDialog(
            initial = eduToEdit,
            onDismiss = { editingEducation = null },
            onSave = { saved ->
                viewModel.addOrUpdateTypedEducation(saved)
                editingEducation = null
            }
        )
    }

    val projToEdit = editingProject
    if (projToEdit != null) {
        ProjectEditorModal(
            initial = projToEdit,
            onDismiss = { editingProject = null },
            onSave = { saved ->
                viewModel.addOrUpdateProject(saved)
                editingProject = null
            }
        )
    }
}

@Composable
internal fun PersonalInfoSectionForm(
    resumeTitle: String,
    personalInfo: PersonalInfo,
    onSave: (PersonalInfo, String) -> Unit,
    onRequestAiPolish: (String) -> Unit,
    onNavigateToCoach: () -> Unit
) {
    var title by remember(resumeTitle) { mutableStateOf(resumeTitle) }
    var fullName by remember(personalInfo) { mutableStateOf(personalInfo.fullName) }
    var jobTitle by remember(personalInfo) { mutableStateOf(personalInfo.jobTitle) }
    var email by remember(personalInfo) { mutableStateOf(personalInfo.email) }
    var phone by remember(personalInfo) { mutableStateOf(personalInfo.phone) }
    var address by remember(personalInfo) { mutableStateOf(personalInfo.address) }
    var website by remember(personalInfo) { mutableStateOf(personalInfo.links.primaryDisplayLink()) }
    var summary by remember(personalInfo) { mutableStateOf(personalInfo.summary) }

    LaunchedEffect(personalInfo, resumeTitle) {
        title = resumeTitle
        fullName = personalInfo.fullName
        jobTitle = personalInfo.jobTitle
        email = personalInfo.email
        phone = personalInfo.phone
        address = personalInfo.address
        website = personalInfo.links.primaryDisplayLink()
        summary = personalInfo.summary
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, WarmBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "01 / PERSONAL INFORMATION",
                style = MaterialTheme.typography.labelSmall,
                color = Brand600
            )
            Text(
                text = "Contact details & profile summary",
                style = MaterialTheme.typography.titleLarge,
                color = Ink950
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Document Name") },
                singleLine = true,
                colors = kungaTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_resume_title")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_full_name")
                )
                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("Target Job Title") },
                    singleLine = true,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_job_title")
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_email")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone (+265 …)") },
                    singleLine = true,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_phone")
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("City & Country (e.g., Lilongwe, Malawi)") },
                    singleLine = true,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_location")
                )
                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("LinkedIn / Portfolio Link") },
                    singleLine = true,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_website")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Professional Summary",
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink950
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { onRequestAiPolish(summary) },
                        modifier = Modifier.testTag("polish_summary_ai_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Brand600
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sharpen phrasing", color = Brand700)
                    }
                    TextButton(
                        onClick = onNavigateToCoach,
                        modifier = Modifier.testTag("ask_coach_summary_button")
                    ) {
                        Text("Editorial notes", color = Ink500)
                    }
                }
            }

            OutlinedTextField(
                value = summary,
                onValueChange = { summary = it },
                label = { Text("2–4 sentences highlighting your core strengths & impact") },
                minLines = 4,
                colors = kungaTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_summary")
            )

            Button(
                onClick = {
                    onSave(
                        PersonalInfo(
                            fullName = fullName.trim(),
                            jobTitle = jobTitle.trim(),
                            email = email.trim(),
                            phone = phone.trim(),
                            address = address.trim(),
                            summary = summary.trim(),
                            links = ProfileLinks(website = website.trim())
                        ),
                        title.trim()
                    )
                },
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_profile_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Personal Info")
            }
        }
    }
}

@Composable
internal fun WorkExperienceSectionList(
    experiences: List<WorkExperience>,
    onAddClick: () -> Unit,
    onEditClick: (WorkExperience) -> Unit,
    onDeleteClick: (WorkExperience) -> Unit,
    onPolishClick: (WorkExperience) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "02 / WORK EXPERIENCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Brand600
                )
                Text(
                    text = "Professional roles (${experiences.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink950
                )
            }
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                modifier = Modifier.testTag("add_experience_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Role")
            }
        }

        if (experiences.isEmpty()) {
            EmptySectionPrompt(
                title = "No professional experience added yet",
                subtitle = "Add your current and past roles, dates, and achievement bullets."
            )
        } else {
            experiences.forEach { exp ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WarmBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exp.jobTitle.ifBlank { "Role Title" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Ink950
                                )
                                Text(
                                    text = listOf(exp.employer, exp.periodDisplay(), exp.locationDisplay())
                                        .filter { it.isNotBlank() }
                                        .joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Brand700,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row {
                                IconButton(
                                    onClick = { onPolishClick(exp) },
                                    modifier = Modifier.testTag("polish_exp_${exp.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = "Sharpen bullet points",
                                        tint = Brand600
                                    )
                                }
                                IconButton(
                                    onClick = { onEditClick(exp) },
                                    modifier = Modifier.testTag("edit_exp_${exp.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit experience",
                                        tint = Ink950
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteClick(exp) },
                                    modifier = Modifier.testTag("delete_exp_${exp.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete experience",
                                        tint = CrimsonError
                                    )
                                }
                            }
                        }

                        exp.effectiveBullets().forEach { bullet ->
                            Text(
                                text = "• $bullet",
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink500
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun EducationSectionList(
    educations: List<Education>,
    onAddClick: () -> Unit,
    onEditClick: (Education) -> Unit,
    onDeleteClick: (Education) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "03 / EDUCATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = Brand600
                )
                Text(
                    text = "Academic background (${educations.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink950
                )
            }
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                modifier = Modifier.testTag("add_education_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Degree")
            }
        }

        if (educations.isEmpty()) {
            EmptySectionPrompt(
                title = "No education entries yet",
                subtitle = "Add your university degrees, diplomas, and academic honors."
            )
        } else {
            educations.forEach { edu ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WarmBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = edu.degree,
                                style = MaterialTheme.typography.titleMedium,
                                color = Ink950
                            )
                            Text(
                                text = listOf(edu.school, edu.city, edu.periodDisplay())
                                    .filter { it.isNotBlank() }
                                    .joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                                color = Brand700,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (edu.description.isNotBlank()) {
                                Text(
                                    text = edu.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Ink500
                                )
                            }
                        }
                        Row {
                            IconButton(onClick = { onEditClick(edu) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit education",
                                    tint = Ink950
                                )
                            }
                            IconButton(onClick = { onDeleteClick(edu) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete education",
                                    tint = CrimsonError
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SkillsSectionEditor(
    skills: List<Skill>,
    onAddOrUpdateSkill: (Skill) -> Unit,
    onRemoveSkill: (String) -> Unit
) {
    var newSkillName by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf(SkillLevel.EXPERIENCED) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, WarmBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "04 / SKILLS & PROFICIENCY",
                style = MaterialTheme.typography.labelSmall,
                color = Brand600
            )
            Text(
                text = "Core skills & expertise levels",
                style = MaterialTheme.typography.titleLarge,
                color = Ink950
            )

            OutlinedTextField(
                value = newSkillName,
                onValueChange = { newSkillName = it },
                label = { Text("Skill name (e.g., Product Strategy, Kotlin, Figma)") },
                singleLine = true,
                colors = kungaTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_skill_input")
            )

            // 4-Tier SkillLevel Selector from redux/resume/helper.js
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SkillLevel.entries.forEach { level ->
                    val active = selectedLevel == level
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (active) Ink950 else WarmInputSurface,
                        border = BorderStroke(1.dp, if (active) Ink950 else WarmBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { selectedLevel = level }
                    ) {
                        Text(
                            text = level.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (active) Color.White else Ink500,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val trimmed = newSkillName.trim()
                    if (trimmed.isNotBlank()) {
                        onAddOrUpdateSkill(
                            Skill(
                                skill = trimmed,
                                skillLevel = selectedLevel
                            )
                        )
                        newSkillName = ""
                    }
                },
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_skill_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Skill (${selectedLevel.label})")
            }

            if (skills.isEmpty()) {
                Text(
                    text = "No skills added yet. Add 5–10 core competencies.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink500
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    skills.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = WarmInputSurface,
                            border = BorderStroke(1.dp, WarmBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.skill,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Ink950
                                    )
                                    Text(
                                        text = item.skillLevel.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Brand700
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    (1..4).forEach { dot ->
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    if (dot <= item.skillLevel.dots) Brand600
                                                    else WarmBorder,
                                                    CircleShape
                                                )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { onRemoveSkill(item.skill) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Remove ${item.skill}",
                                            tint = CrimsonError,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ProjectsSectionList(
    projects: List<ProjectEntry>,
    onAddClick: () -> Unit,
    onEditClick: (ProjectEntry) -> Unit,
    onDeleteClick: (ProjectEntry) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "05 / PROJECTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Brand600
                )
                Text(
                    text = "Key projects (${projects.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink950
                )
            }
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                modifier = Modifier.testTag("add_project_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Project")
            }
        }

        if (projects.isEmpty()) {
            EmptySectionPrompt(
                title = "No projects listed yet",
                subtitle = "Showcase open-source work, case studies, or community leadership."
            )
        } else {
            projects.forEach { proj ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WarmBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = proj.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Ink950
                            )
                            if (proj.role.isNotBlank() || proj.link.isNotBlank()) {
                                Text(
                                    text = listOf(proj.role, proj.link)
                                        .filter { it.isNotBlank() }
                                        .joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Brand700,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (proj.description.isNotBlank()) {
                                Text(
                                    text = proj.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Ink500
                                )
                            }
                        }
                        Row {
                            IconButton(onClick = { onEditClick(proj) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit project",
                                    tint = Ink950
                                )
                            }
                            IconButton(onClick = { onDeleteClick(proj) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete project",
                                    tint = CrimsonError
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySectionPrompt(
    title: String,
    subtitle: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, WarmBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WorkOutline,
                contentDescription = null,
                tint = Brand600,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Ink950
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Ink500
            )
        }
    }
}
