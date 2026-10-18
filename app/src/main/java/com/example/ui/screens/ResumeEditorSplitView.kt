package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CvCustomization
import com.example.data.model.CvProfile
import com.example.data.model.Education
import com.example.data.model.KungaTemplateCatalog
import com.example.data.model.KungaTemplatePreset
import com.example.data.model.ProjectEntry
import com.example.data.model.ResumeDocument
import com.example.data.model.WorkExperience
import com.example.ui.theme.Brand600
import com.example.ui.theme.Brand700
import com.example.ui.theme.CoverLetterStageBg
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Ink400
import com.example.ui.theme.Ink500
import com.example.ui.theme.Ink950
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmInputSurface
import com.example.ui.theme.WarmPaperBg
import com.example.viewmodel.AiTaskState
import com.example.viewmodel.KungaCvViewModel

enum class EditorViewMode(val label: String, val icon: ImageVector, val tag: String) {
    SPLIT("Split View", Icons.Default.ViewAgenda, "view_mode_split"),
    FORM("Form Only", Icons.Default.EditNote, "view_mode_form"),
    PREVIEW("Live A4", Icons.Default.Visibility, "view_mode_preview")
}

/**
 * Publication-Grade Split-View Resume Editor layout in Jetpack Compose.
 * Features an integrated dual-pane architecture with an input form section
 * and a live-updating preview pane for 12 authentic publication CV templates.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResumeEditorSplitLayout(
    activeProfile: CvProfile,
    customization: CvCustomization,
    preset: KungaTemplatePreset,
    activeResume: ResumeDocument,
    activeStepIndex: Int,
    onSelectStep: (Int) -> Unit,
    aiTaskState: AiTaskState,
    viewModel: KungaCvViewModel,
    onNavigateToCoach: () -> Unit,
    onOpenCustomize: () -> Unit,
    onEditWorkExp: (WorkExperience) -> Unit,
    onEditEducation: (Education) -> Unit,
    onEditProject: (ProjectEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exportWarnings by viewModel.exportWarnings.collectAsStateWithLifecycle()
    val saveStatusLabel by viewModel.saveStatusLabel.collectAsStateWithLifecycle()

    var editorViewMode by rememberSaveable { mutableStateOf(EditorViewMode.SPLIT) }
    var previewScale by rememberSaveable { mutableFloatStateOf(1.0f) }

    // Pre-flight check warning dialog before PDF download
    if (exportWarnings.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissExportWarnings() },
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "PRE-FLIGHT CHECK",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = Brand600
                    )
                    Text(
                        text = "Your CV is ready to export",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Ink950
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Before sending to a hiring manager, consider completing these fields:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink500
                    )
                    exportWarnings.forEach { warning ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = Brand600,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = warning,
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink950
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.requestExportResumePdf(context, force = true) },
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Ink950,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("download_anyway_button")
                ) {
                    Text("Download anyway")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.dismissExportWarnings() },
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, WarmBorder)
                ) {
                    Text("Keep editing", color = Ink950)
                }
            }
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 760.dp

        if (isWideScreen) {
            // SIDE-BY-SIDE SPLIT VIEW (Tablets / Landscape / Expanded Screens)
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Pane: Form Section for User Input
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(WarmPaperBg)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ResumeEditorFormHeader(
                        activeProfile = activeProfile,
                        preset = preset,
                        saveStatusLabel = saveStatusLabel
                    )

                    ResumeSectionStepTabs(
                        activeStepIndex = activeStepIndex,
                        onSelectStep = onSelectStep
                    )

                    ResumeAiStatusBanner(
                        aiTaskState = aiTaskState,
                        onDismiss = { viewModel.dismissAiTaskState() }
                    )

                    // Active Section Form
                    ResumeSectionFormContent(
                        activeStepIndex = activeStepIndex,
                        activeProfile = activeProfile,
                        activeResume = activeResume,
                        viewModel = viewModel,
                        onNavigateToCoach = onNavigateToCoach,
                        onEditWorkExp = onEditWorkExp,
                        onEditEducation = onEditEducation,
                        onEditProject = onEditProject
                    )

                    OutlinedButton(
                        onClick = onOpenCustomize,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, WarmBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customize_from_form_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Ink950
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Customize Typography & Grid", color = Ink950)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                VerticalDivider(color = WarmBorder, thickness = 1.dp)

                // Right Pane: Live Template Preview Pane
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(CoverLetterStageBg)
                ) {
                    ResumePreviewPaneHeader(
                        preset = preset,
                        customization = customization,
                        onSelectPreset = { viewModel.applyPresetToActiveOrCreate(it) },
                        onExportPdf = { viewModel.requestExportResumePdf(context) }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        KungaResumePaperCanvas(
                            profile = activeProfile,
                            customization = customization,
                            preset = preset
                        )
                    }
                }
            }
        } else {
            // COMPACT SCREEN (Phones / Narrow Foldables) with Dynamic Mode Switching
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(WarmPaperBg)
            ) {
                // Top Segmented Mode Selector
                Surface(
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 3-Mode Segmented Pill Bar
                            Row(
                                modifier = Modifier
                                    .background(WarmInputSurface, RoundedCornerShape(999.dp))
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                EditorViewMode.entries.forEach { mode ->
                                    val isSelected = editorViewMode == mode
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = if (isSelected) Ink950 else Color.Transparent,
                                        modifier = Modifier
                                            .clickable { editorViewMode = mode }
                                            .testTag(mode.tag)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = mode.icon,
                                                contentDescription = mode.label,
                                                tint = if (isSelected) Color.White else Ink500,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = mode.label,
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else Ink500
                                            )
                                        }
                                    }
                                }
                            }

                            // Quick Action: Export PDF
                            IconButton(
                                onClick = { viewModel.requestExportResumePdf(context) },
                                modifier = Modifier.testTag("mobile_quick_export_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "Export PDF",
                                    tint = Ink950,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = WarmBorder)
                    }
                }

                when (editorViewMode) {
                    EditorViewMode.SPLIT -> {
                        // VERTICAL SPLIT: Live Template Preview Pane on top, Form on bottom
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Top Live Preview Pane
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(290.dp)
                                    .background(CoverLetterStageBg)
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Compact Preset Switcher Bar
                                    CompactPresetSwitcherBar(
                                        selectedPresetId = customization.presetId,
                                        onSelectPreset = { viewModel.applyPresetToActiveOrCreate(it) }
                                    )

                                    // Scrollable miniature A4 preview
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.TopCenter
                                    ) {
                                        KungaResumePaperCanvas(
                                            profile = activeProfile,
                                            customization = customization,
                                            preset = preset
                                        )
                                    }
                                }
                            }

                            // Split Handle Divider
                            Surface(
                                color = WarmPaperBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(3.dp)
                                            .background(Ink400, RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                            HorizontalDivider(color = WarmBorder)

                            // Bottom Input Form Section
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ResumeSectionStepTabs(
                                    activeStepIndex = activeStepIndex,
                                    onSelectStep = onSelectStep
                                )

                                ResumeAiStatusBanner(
                                    aiTaskState = aiTaskState,
                                    onDismiss = { viewModel.dismissAiTaskState() }
                                )

                                ResumeSectionFormContent(
                                    activeStepIndex = activeStepIndex,
                                    activeProfile = activeProfile,
                                    activeResume = activeResume,
                                    viewModel = viewModel,
                                    onNavigateToCoach = onNavigateToCoach,
                                    onEditWorkExp = onEditWorkExp,
                                    onEditEducation = onEditEducation,
                                    onEditProject = onEditProject
                                )

                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }

                    EditorViewMode.FORM -> {
                        // FULL-HEIGHT FORM ONLY
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            ResumeEditorFormHeader(
                                activeProfile = activeProfile,
                                preset = preset,
                                saveStatusLabel = saveStatusLabel
                            )

                            ResumeSectionStepTabs(
                                activeStepIndex = activeStepIndex,
                                onSelectStep = onSelectStep
                            )

                            ResumeAiStatusBanner(
                                aiTaskState = aiTaskState,
                                onDismiss = { viewModel.dismissAiTaskState() }
                            )

                            ResumeSectionFormContent(
                                activeStepIndex = activeStepIndex,
                                activeProfile = activeProfile,
                                activeResume = activeResume,
                                viewModel = viewModel,
                                onNavigateToCoach = onNavigateToCoach,
                                onEditWorkExp = onEditWorkExp,
                                onEditEducation = onEditEducation,
                                onEditProject = onEditProject
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onOpenCustomize,
                                    shape = RoundedCornerShape(999.dp),
                                    border = BorderStroke(1.dp, WarmBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Ink950
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Customize Layout", color = Ink950)
                                }
                                Button(
                                    onClick = { editorViewMode = EditorViewMode.PREVIEW },
                                    shape = RoundedCornerShape(999.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Ink950),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("View Live A4")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))
                        }
                    }

                    EditorViewMode.PREVIEW -> {
                        // FULL-HEIGHT LIVE A4 PREVIEW PANE
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CoverLetterStageBg)
                        ) {
                            ResumePreviewPaneHeader(
                                preset = preset,
                                customization = customization,
                                onSelectPreset = { viewModel.applyPresetToActiveOrCreate(it) },
                                onExportPdf = { viewModel.requestExportResumePdf(context) }
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                KungaResumePaperCanvas(
                                    profile = activeProfile,
                                    customization = customization,
                                    preset = preset
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
private fun ResumeEditorFormHeader(
    activeProfile: CvProfile,
    preset: KungaTemplatePreset,
    saveStatusLabel: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, WarmBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${activeProfile.completionPercentage()}% COMPLETE · ${preset.name.uppercase()}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = Brand600
                    )
                    Text(
                        text = activeProfile.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink950
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(EmeraldSuccess, CircleShape)
                    )
                    Text(
                        text = saveStatusLabel.uppercase(),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = Ink500
                    )
                }
            }

            // Hairline Progress Rule
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(WarmBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(activeProfile.completionPercentage() / 100f)
                        .height(2.dp)
                        .background(Ink950)
                )
            }
        }
    }
}

@Composable
private fun ResumeSectionStepTabs(
    activeStepIndex: Int,
    onSelectStep: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EditorSectionStep.entries.forEachIndexed { idx, step ->
            val isCurrent = activeStepIndex == idx
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isCurrent) Ink950 else WarmInputSurface,
                border = BorderStroke(
                    1.dp,
                    if (isCurrent) Ink950 else WarmBorder
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelectStep(idx) }
                    .testTag(step.tag)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = step.indexCode,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = if (isCurrent) Color.White.copy(alpha = 0.65f) else Ink400
                    )
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isCurrent) Color.White else Ink950
                    )
                }
            }
        }
    }
}

@Composable
private fun ResumeAiStatusBanner(
    aiTaskState: AiTaskState,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(visible = aiTaskState !is AiTaskState.Idle) {
        when (aiTaskState) {
            is AiTaskState.Loading -> {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, WarmBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Ink950
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Drafting editorial revisions…",
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink950
                        )
                    }
                }
            }

            is AiTaskState.Error -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = aiTaskState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = CrimsonError,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onDismiss) {
                            Text("Dismiss")
                        }
                    }
                }
            }

            else -> {}
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResumeSectionFormContent(
    activeStepIndex: Int,
    activeProfile: CvProfile,
    activeResume: ResumeDocument,
    viewModel: KungaCvViewModel,
    onNavigateToCoach: () -> Unit,
    onEditWorkExp: (WorkExperience) -> Unit,
    onEditEducation: (Education) -> Unit,
    onEditProject: (ProjectEntry) -> Unit
) {
    val activeStep = EditorSectionStep.entries[activeStepIndex]
    when (activeStep) {
        EditorSectionStep.PERSONAL -> PersonalInfoSectionForm(
            resumeTitle = activeProfile.title,
            personalInfo = activeProfile.personalInfo,
            onSave = { info, nextTitle ->
                viewModel.updatePersonalInfo(info, nextTitle)
            },
            onRequestAiPolish = { draftSummary ->
                viewModel.requestAiPolish(draftSummary, "Executive Summary")
            },
            onNavigateToCoach = onNavigateToCoach
        )

        EditorSectionStep.EXPERIENCE -> WorkExperienceSectionList(
            experiences = activeProfile.workExperience,
            onAddClick = { onEditWorkExp(WorkExperience()) },
            onEditClick = { onEditWorkExp(it) },
            onDeleteClick = { viewModel.removeExperience(it.id) },
            onPolishClick = { exp ->
                val raw = exp.effectiveBullets().joinToString("\n")
                    .ifBlank { "${exp.jobTitle} at ${exp.employer}" }
                viewModel.requestAiPolish(raw, "Experience at ${exp.employer}")
            }
        )

        EditorSectionStep.EDUCATION -> EducationSectionList(
            educations = activeProfile.education,
            onAddClick = { onEditEducation(Education()) },
            onEditClick = { onEditEducation(it) },
            onDeleteClick = { viewModel.removeEducation(it.id) }
        )

        EditorSectionStep.SKILLS -> SkillsSectionEditor(
            skills = activeProfile.skills,
            onAddOrUpdateSkill = { viewModel.addOrUpdateSkill(it) },
            onRemoveSkill = { viewModel.removeTypedSkill(it) }
        )

        EditorSectionStep.PROJECTS -> ProjectsSectionList(
            projects = activeProfile.projects,
            onAddClick = { onEditProject(ProjectEntry()) },
            onEditClick = { onEditProject(it) },
            onDeleteClick = { viewModel.removeProject(it.id) }
        )
    }
}

@Composable
private fun ResumePreviewPaneHeader(
    preset: KungaTemplatePreset,
    customization: CvCustomization,
    onSelectPreset: (KungaTemplatePreset) -> Unit,
    onExportPdf: () -> Unit
) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LIVE A4 TYPESETTING · ${preset.categoryBadge}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = Brand600
                    )
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Ink950
                    )
                    Text(
                        text = "${preset.architecture.label} · ${customization.skillDisplayStyle.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink500
                    )
                }

                Button(
                    onClick = onExportPdf,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Ink950,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("preview_pane_export_pdf_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export PDF", fontSize = 12.sp)
                }
            }

            // 12-Template Preset Switcher Strip
            CompactPresetSwitcherBar(
                selectedPresetId = customization.presetId,
                onSelectPreset = onSelectPreset
            )
        }
        HorizontalDivider(color = WarmBorder)
    }
}

@Composable
private fun CompactPresetSwitcherBar(
    selectedPresetId: Int,
    onSelectPreset: (KungaTemplatePreset) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KungaTemplateCatalog.presets.forEach { item ->
            val isSelected = selectedPresetId == item.id
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) Ink950 else WarmInputSurface,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) Ink950 else WarmBorder
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelectPreset(item) }
                    .testTag("preset_pill_${item.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Color(item.accentHex), CircleShape)
                    )
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.White else Ink950
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}
