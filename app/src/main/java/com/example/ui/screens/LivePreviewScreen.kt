package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CvCustomization
import com.example.data.model.CvLayoutArchitecture
import com.example.data.model.CvProfile
import com.example.data.model.KungaTemplateCatalog
import com.example.data.model.KungaTemplatePreset
import com.example.data.model.ResumeDocument
import com.example.data.model.Skill
import com.example.data.model.SkillDisplayStyle
import com.example.ui.theme.Brand50
import com.example.ui.theme.Brand600
import com.example.ui.theme.CoverLetterStageBg
import com.example.ui.theme.Ink400
import com.example.ui.theme.Ink500
import com.example.ui.theme.Ink700
import com.example.ui.theme.Ink950
import com.example.ui.theme.InstrumentSerifFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmInputSurface
import com.example.viewmodel.KungaCvViewModel

/**
 * Live A4 Paper Preview & PDF Export Screen featuring 6 distinct, human-crafted
 * publication CV architectures (Harvard Executive Double-Rule, Swiss 26/74 Left-Rail Index,
 * Tinted Left Dossier Sidebar, Literary 64/36 Vertical Rule Split, Architectural Masthead Banner,
 * and Framed Editorial Monograph).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LivePreviewScreen(
    activeResume: ResumeDocument?,
    viewModel: KungaCvViewModel
) {
    val context = LocalContext.current
    val customization by viewModel.activeCustomization.collectAsStateWithLifecycle()
    val exportWarnings by viewModel.exportWarnings.collectAsStateWithLifecycle()

    if (activeResume == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CoverLetterStageBg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Brand600,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "No dossier selected yet",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ink950
                )
                Text(
                    text = "Select a KungaCV template in the Workspace tab to inspect and export your A4 PDF.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink500,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val preset = KungaTemplateCatalog.fromPresetId(customization.presetId)
    val profile = activeResume.toCvProfile(preset).copy(customization = customization)

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoverLetterStageBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Editorial A4 Sheet Masthead + Export PDF CTA
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WarmBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "02 / A4 TYPESETTING · ${preset.categoryBadge}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = Brand600
                            )
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.displayMedium,
                                color = Ink950
                            )
                            Text(
                                text = "${preset.architecture.label} · ${customization.skillDisplayStyle.label}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink500
                            )
                        }

                        Button(
                            onClick = { viewModel.requestExportResumePdf(context) },
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Ink950,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("export_pdf_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF")
                        }
                    }

                    // Horizontal 12-Preset Switcher Strip with architectural tag
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KungaTemplateCatalog.presets.forEach { item ->
                            val isSelected = customization.presetId == item.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Ink950 else WarmInputSurface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Ink950 else WarmBorder
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.applyPresetToActiveOrCreate(item) }
                                    .testTag("preview_template_${item.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color(item.accentHex), CircleShape)
                                    )
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) Color.White else Ink950
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Live A4 Paper Sheet rendered with the preset's authentic page architecture
            KungaResumePaperCanvas(
                profile = profile,
                customization = customization,
                preset = preset
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun KungaResumePaperCanvas(
    profile: CvProfile,
    customization: CvCustomization,
    preset: KungaTemplatePreset = KungaTemplateCatalog.fromPresetId(customization.presetId)
) {
    val accentColor = Color(customization.accentHex)
    val headingFont = when (customization.fontFamily) {
        "Playfair Display" -> InstrumentSerifFontFamily
        "Plus Jakarta Sans" -> PlusJakartaSansFontFamily
        else -> PoppinsFontFamily
    }
    val visibleSections = customization.sectionOrder.filterNot {
        it in customization.hiddenSections
    }

    // Determine effective architecture: honor manual 2-column overrides if user toggled columns in Customize
    val effectiveArchitecture = when {
        customization.columns == 2 && customization.direction == "left" ->
            CvLayoutArchitecture.ARCHITECTURAL_LEFT_SIDEBAR
        customization.columns == 2 && customization.direction == "right" ->
            CvLayoutArchitecture.EDITORIAL_RIGHT_SIDEBAR
        preset.columns == 2 && customization.columns == 1 ->
            CvLayoutArchitecture.HARVARD_EXECUTIVE
        else -> preset.architecture
    }

    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(1.dp, WarmBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("live_cv_paper_canvas")
    ) {
        when (effectiveArchitecture) {
            CvLayoutArchitecture.HARVARD_EXECUTIVE -> HarvardExecutiveSheet(
                profile = profile,
                customization = customization,
                preset = preset,
                accentColor = accentColor,
                headingFont = headingFont,
                visibleSections = visibleSections
            )

            CvLayoutArchitecture.SWISS_INDEX_RAIL -> SwissIndexRailSheet(
                profile = profile,
                customization = customization,
                preset = preset,
                accentColor = accentColor,
                headingFont = headingFont,
                visibleSections = visibleSections
            )

            CvLayoutArchitecture.ARCHITECTURAL_LEFT_SIDEBAR -> ArchitecturalLeftSidebarSheet(
                profile = profile,
                customization = customization,
                preset = preset,
                accentColor = accentColor,
                headingFont = headingFont,
                visibleSections = visibleSections
            )

            CvLayoutArchitecture.EDITORIAL_RIGHT_SIDEBAR -> EditorialRightSidebarSheet(
                profile = profile,
                customization = customization,
                preset = preset,
                accentColor = accentColor,
                headingFont = headingFont,
                visibleSections = visibleSections
            )

            CvLayoutArchitecture.MODERN_BANNER_HEADER -> ModernBannerHeaderSheet(
                profile = profile,
                customization = customization,
                preset = preset,
                accentColor = accentColor,
                headingFont = headingFont,
                visibleSections = visibleSections
            )

            CvLayoutArchitecture.FRAMED_MONOGRAPH -> FramedMonographSheet(
                profile = profile,
                customization = customization,
                preset = preset,
                accentColor = accentColor,
                headingFont = headingFont,
                visibleSections = visibleSections
            )
        }
    }
}

/**
 * 1. HARVARD / WALL STREET EXECUTIVE LAYOUT (Presets 1 "Classic Blue" & 11 "Uppercase Studio")
 * Centered authoritative masthead, pipe-delimited tabular contact bar, classic executive double-rule,
 * bold company / right-aligned location + italic role / right-aligned tabular dates.
 */
@Composable
private fun HarvardExecutiveSheet(
    profile: CvProfile,
    customization: CvCustomization,
    preset: KungaTemplatePreset,
    accentColor: Color,
    headingFont: FontFamily,
    visibleSections: List<String>
) {
    val isUppercaseStudio = preset.id == 11
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 26.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Centered Executive Masthead
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = (profile.personalInfo.fullName.ifBlank { "Your Name" }).let {
                    if (isUppercaseStudio) it.uppercase() else it
                },
                fontFamily = if (isUppercaseStudio) PlusJakartaSansFontFamily else InstrumentSerifFontFamily,
                fontSize = if (isUppercaseStudio) 22.sp else 30.sp,
                fontWeight = if (isUppercaseStudio) FontWeight.ExtraBold else FontWeight.Normal,
                letterSpacing = if (isUppercaseStudio) 2.2.sp else (-0.3).sp,
                color = Ink950,
                textAlign = TextAlign.Center
            )
            if (profile.personalInfo.jobTitle.isNotBlank()) {
                Text(
                    text = profile.personalInfo.jobTitle.uppercase(),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.6.sp,
                    color = accentColor,
                    textAlign = TextAlign.Center
                )
            }
            val contactPieces = listOf(
                profile.personalInfo.address,
                profile.personalInfo.email,
                profile.personalInfo.phone,
                profile.personalInfo.links.primaryDisplayLink()
            ).filter { it.isNotBlank() }
            if (contactPieces.isNotEmpty()) {
                Text(
                    text = contactPieces.joinToString("   |   "),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                    color = Ink500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Classic Harvard / McKinsey Executive Double-Rule
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            HorizontalDivider(thickness = 2.dp, color = Ink950)
            HorizontalDivider(thickness = 0.75.dp, color = accentColor)
        }

        visibleSections.forEach { sectionKey ->
            when (sectionKey) {
                "personalInfo" -> {
                    if (profile.personalInfo.summary.isNotBlank()) {
                        ExecutiveSectionBlock("Executive Summary", accentColor) {
                            Text(
                                text = profile.personalInfo.summary,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    lineHeight = 17.sp
                                ),
                                color = Ink700
                            )
                        }
                    }
                }

                "workExperience" -> {
                    val visibleExp = profile.workExperience.filter { it.visible }
                    if (visibleExp.isNotEmpty()) {
                        ExecutiveSectionBlock("Professional Experience", accentColor) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                visibleExp.forEach { exp ->
                                    HarvardExperienceItem(exp = exp, accentColor = accentColor)
                                }
                            }
                        }
                    }
                }

                "education" -> {
                    val visibleEdu = profile.education.filter { it.visible }
                    if (visibleEdu.isNotEmpty()) {
                        ExecutiveSectionBlock("Education", accentColor) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                visibleEdu.forEach { edu ->
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = edu.school.ifBlank { "University" },
                                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.5.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = Ink950,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (edu.locationDisplay().isNotBlank()) {
                                                Text(
                                                    text = edu.locationDisplay(),
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                                    color = Ink500
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = edu.degree,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 11.5.sp,
                                                    fontStyle = FontStyle.Italic
                                                ),
                                                color = Ink700,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = edu.periodDisplay(),
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 10.sp,
                                                color = Ink500
                                            )
                                        }
                                        if (edu.description.isNotBlank()) {
                                            Text(
                                                text = "•  ${edu.description}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = Ink500,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "skills" -> {
                    val visibleSkills = profile.skills.filter { it.visible }
                    if (visibleSkills.isNotEmpty()) {
                        ExecutiveSectionBlock("Core Competencies & Technical Skills", accentColor) {
                            RenderSkillsSection(
                                skills = visibleSkills,
                                style = customization.skillDisplayStyle,
                                accentColor = accentColor
                            )
                        }
                    }
                }

                "projects" -> {
                    if (profile.projects.isNotEmpty()) {
                        ExecutiveSectionBlock("Selected Projects & Leadership", accentColor) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                profile.projects.forEach { proj ->
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = proj.name,
                                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = Ink950
                                            )
                                            if (proj.role.isNotBlank()) {
                                                Text(
                                                    text = proj.role.uppercase(),
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 9.5.sp,
                                                    color = accentColor
                                                )
                                            }
                                        }
                                        if (proj.description.isNotBlank()) {
                                            Text(
                                                text = proj.description,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = Ink700
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

        SheetFooterBar(presetName = preset.name, code = "EXEC-0${preset.id}")
    }
}

/**
 * 2. SWISS INTERNATIONAL / READ.CV 26/74 INDEX RAIL LAYOUT
 * (Presets 6 "Minimal Studio", 8 "Grid Skills", 10 "Compact Work Sans")
 * Iconic left index column (26%) with monospace numbered labels + right content column (74%).
 */
@Composable
private fun SwissIndexRailSheet(
    profile: CvProfile,
    customization: CvCustomization,
    preset: KungaTemplatePreset,
    accentColor: Color,
    headingFont: FontFamily,
    visibleSections: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Asymmetric 26/74 Swiss Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(0.27f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(4.dp)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "CURRICULUM\nVITAE",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.5.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink950
                )
                if (profile.personalInfo.address.isNotBlank()) {
                    Text(
                        text = profile.personalInfo.address,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = Ink500
                    )
                }
            }

            Column(
                modifier = Modifier.weight(0.73f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = profile.personalInfo.fullName.ifBlank { "Your Name" },
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    color = Ink950
                )
                if (profile.personalInfo.jobTitle.isNotBlank()) {
                    Text(
                        text = profile.personalInfo.jobTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
                val contactItems = listOf(
                    profile.personalInfo.email,
                    profile.personalInfo.phone,
                    profile.personalInfo.links.primaryDisplayLink()
                ).filter { it.isNotBlank() }
                if (contactItems.isNotEmpty()) {
                    Text(
                        text = contactItems.joinToString("  ·  "),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        color = Ink500
                    )
                }
            }
        }

        var sectionCounter = 1
        visibleSections.forEach { sectionKey ->
            when (sectionKey) {
                "personalInfo" -> {
                    if (profile.personalInfo.summary.isNotBlank()) {
                        val idx = "0${sectionCounter++}"
                        SwissRailRow(indexCode = idx, label = "ABOUT", accentColor = accentColor) {
                            Text(
                                text = profile.personalInfo.summary,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    lineHeight = 17.sp
                                ),
                                color = Ink700
                            )
                        }
                    }
                }

                "workExperience" -> {
                    val visibleExp = profile.workExperience.filter { it.visible }
                    if (visibleExp.isNotEmpty()) {
                        val idx = "0${sectionCounter++}"
                        SwissRailRow(indexCode = idx, label = "EXPERIENCE", accentColor = accentColor) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                visibleExp.forEach { exp ->
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${exp.jobTitle} · ${exp.employer}",
                                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = Ink950,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = exp.periodDisplay(),
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 9.5.sp,
                                                color = accentColor
                                            )
                                        }
                                        if (exp.locationDisplay().isNotBlank()) {
                                            Text(
                                                text = exp.locationDisplay(),
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 9.5.sp,
                                                color = Ink500
                                            )
                                        }
                                        exp.effectiveBullets().forEach { bullet ->
                                            Row(
                                                modifier = Modifier.padding(top = 2.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "—",
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 10.sp,
                                                    color = accentColor
                                                )
                                                Text(
                                                    text = bullet,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 11.sp,
                                                        lineHeight = 15.5.sp
                                                    ),
                                                    color = Ink700
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "education" -> {
                    val visibleEdu = profile.education.filter { it.visible }
                    if (visibleEdu.isNotEmpty()) {
                        val idx = "0${sectionCounter++}"
                        SwissRailRow(indexCode = idx, label = "EDUCATION", accentColor = accentColor) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                visibleEdu.forEach { edu ->
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = edu.degree,
                                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = Ink950,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = edu.periodDisplay(),
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 9.5.sp,
                                                color = Ink500
                                            )
                                        }
                                        Text(
                                            text = listOf(edu.school, edu.locationDisplay())
                                                .filter { it.isNotBlank() }
                                                .joinToString(", "),
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = Ink500
                                        )
                                        if (edu.description.isNotBlank()) {
                                            Text(
                                                text = edu.description,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                                color = Ink700
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "skills" -> {
                    val visibleSkills = profile.skills.filter { it.visible }
                    if (visibleSkills.isNotEmpty()) {
                        val idx = "0${sectionCounter++}"
                        SwissRailRow(indexCode = idx, label = "CAPABILITIES", accentColor = accentColor) {
                            RenderSkillsSection(
                                skills = visibleSkills,
                                style = customization.skillDisplayStyle,
                                accentColor = accentColor
                            )
                        }
                    }
                }

                "projects" -> {
                    if (profile.projects.isNotEmpty()) {
                        val idx = "0${sectionCounter++}"
                        SwissRailRow(indexCode = idx, label = "SELECTED\nWORK", accentColor = accentColor) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                profile.projects.forEach { proj ->
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = proj.name,
                                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = Ink950
                                            )
                                            Text(
                                                text = proj.role,
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 9.5.sp,
                                                color = accentColor
                                            )
                                        }
                                        if (proj.description.isNotBlank()) {
                                            Text(
                                                text = proj.description,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = Ink700
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

        SheetFooterBar(presetName = preset.name, code = "SWISS-0${preset.id}")
    }
}

@Composable
private fun SwissRailRow(
    indexCode: String,
    label: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalDivider(thickness = 1.dp, color = WarmBorder)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(0.27f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = indexCode,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    text = label,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.5.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink500
                )
            }
            Box(modifier = Modifier.weight(0.73f)) {
                content()
            }
        }
    }
}

/**
 * 3. ARCHITECTURAL TINTED LEFT-SIDEBAR DOSSIER (Presets 5 "Lavender Split" & 12 "Modern Neutral")
 * Full-bleed 34% left tinted sidebar column housing monogram, contact dossier, education, skills & languages,
 * paired with a 66% primary narrative column on crisp white paper.
 */
@Composable
private fun ArchitecturalLeftSidebarSheet(
    profile: CvProfile,
    customization: CvCustomization,
    preset: KungaTemplatePreset,
    accentColor: Color,
    headingFont: FontFamily,
    visibleSections: List<String>
) {
    val sidebarTint = accentColor.copy(alpha = 0.07f)
    val initials = profile.personalInfo.fullName
        .split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")
        .ifBlank { "CV" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Left 35% Tinted Architectural Dossier Column
        Column(
            modifier = Modifier
                .weight(0.36f)
                .fillMaxHeight()
                .background(sidebarTint)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Monogram Stamp
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initials,
                        fontFamily = InstrumentSerifFontFamily,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }

            // Contact Dossier
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SidebarSectionTitle("CONTACT DOSSIER", accentColor)
                if (profile.personalInfo.address.isNotBlank()) {
                    SidebarMetaItem("LOCATION", profile.personalInfo.address)
                }
                if (profile.personalInfo.email.isNotBlank()) {
                    SidebarMetaItem("EMAIL", profile.personalInfo.email)
                }
                if (profile.personalInfo.phone.isNotBlank()) {
                    SidebarMetaItem("TELEPHONE", profile.personalInfo.phone)
                }
                if (profile.personalInfo.links.primaryDisplayLink().isNotBlank()) {
                    SidebarMetaItem("PORTFOLIO", profile.personalInfo.links.primaryDisplayLink())
                }
            }

            // Skills in Left Sidebar
            val visibleSkills = profile.skills.filter { it.visible }
            if ("skills" in visibleSections && visibleSkills.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SidebarSectionTitle("COMPETENCIES", accentColor)
                    RenderSkillsSection(
                        skills = visibleSkills,
                        style = customization.skillDisplayStyle,
                        accentColor = accentColor
                    )
                }
            }

            // Education in Left Sidebar
            val visibleEdu = profile.education.filter { it.visible }
            if ("education" in visibleSections && visibleEdu.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SidebarSectionTitle("EDUCATION", accentColor)
                    visibleEdu.forEach { edu ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = edu.degree,
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 11.5.sp),
                                fontWeight = FontWeight.Bold,
                                color = Ink950
                            )
                            Text(
                                text = edu.school,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                color = Ink700
                            )
                            if (edu.periodDisplay().isNotBlank()) {
                                Text(
                                    text = edu.periodDisplay(),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    color = accentColor
                                )
                            }
                        }
                    }
                }
            }

            // Languages
            if (profile.languages.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SidebarSectionTitle("LANGUAGES", accentColor)
                    profile.languages.filter { it.visible }.forEach { lang ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = lang.language,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = Ink950
                            )
                            Text(
                                text = lang.proficiency,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                color = Ink500
                            )
                        }
                    }
                }
            }
        }

        // Crisp Vertical Hairline Rule
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(WarmBorder)
        )

        // Right 64% Main Narrative Column
        Column(
            modifier = Modifier
                .weight(0.64f)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Editorial Masthead
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = profile.personalInfo.fullName.ifBlank { "Your Name" },
                    fontFamily = headingFont,
                    fontSize = 28.sp,
                    lineHeight = 31.sp,
                    color = Ink950
                )
                if (profile.personalInfo.jobTitle.isNotBlank()) {
                    Text(
                        text = profile.personalInfo.jobTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(thickness = 1.5.dp, color = accentColor)
            }

            if ("personalInfo" in visibleSections && profile.personalInfo.summary.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    SidebarSectionTitle("EXECUTIVE PROFILE", accentColor)
                    Text(
                        text = profile.personalInfo.summary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.2.sp,
                            lineHeight = 16.5.sp
                        ),
                        color = Ink700
                    )
                }
            }

            val visibleExp = profile.workExperience.filter { it.visible }
            if ("workExperience" in visibleSections && visibleExp.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SidebarSectionTitle("WORK EXPERIENCE", accentColor)
                    visibleExp.forEach { exp ->
                        TimelineExperienceEntry(exp = exp, accentColor = accentColor)
                    }
                }
            }

            if ("projects" in visibleSections && profile.projects.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SidebarSectionTitle("KEY PROJECTS", accentColor)
                    profile.projects.forEach { proj ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = proj.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 11.8.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Ink950
                                )
                                Text(
                                    text = proj.role,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    color = accentColor
                                )
                            }
                            Text(
                                text = proj.description,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.8.sp),
                                color = Ink700
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            SheetFooterBar(presetName = preset.name, code = "SPLIT-0${preset.id}")
        }
    }
}

/**
 * 4. EDITORIAL MONOGRAPH / FT & NEW YORKER 64/36 VERTICAL RULE SPLIT
 * (Presets 2 "Sage Sidebar" & 7 "Quiet Serif")
 * Literary Instrument Serif masthead with metadata bar, followed by a 64% primary column
 * and 36% right column separated by a full-height vertical rule.
 */
@Composable
private fun EditorialRightSidebarSheet(
    profile: CvProfile,
    customization: CvCustomization,
    preset: KungaTemplatePreset,
    accentColor: Color,
    headingFont: FontFamily,
    visibleSections: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Publication Masthead Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "CURRICULUM VITAE · EDITION 0${preset.id}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.5.sp,
                    letterSpacing = 1.4.sp,
                    color = accentColor
                )
                Text(
                    text = profile.personalInfo.fullName.ifBlank { "Your Name" },
                    fontFamily = InstrumentSerifFontFamily,
                    fontSize = 32.sp,
                    lineHeight = 34.sp,
                    color = Ink950
                )
                if (profile.personalInfo.jobTitle.isNotBlank()) {
                    Text(
                        text = profile.personalInfo.jobTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic
                        ),
                        color = accentColor
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf(
                    profile.personalInfo.address,
                    profile.personalInfo.email,
                    profile.personalInfo.phone,
                    profile.personalInfo.links.primaryDisplayLink()
                ).filter { it.isNotBlank() }.forEach { line ->
                    Text(
                        text = line,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        color = Ink500
                    )
                }
            }
        }

        // Publication Double Rule
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            HorizontalDivider(thickness = 1.5.dp, color = accentColor)
            HorizontalDivider(thickness = 0.75.dp, color = WarmBorder)
        }

        // 64 / 36 Two-Column Editorial Split with Continuous Vertical Hairline
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left 63% Primary Narrative
            Column(
                modifier = Modifier.weight(0.63f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if ("personalInfo" in visibleSections && profile.personalInfo.summary.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        EditorialSectionHeading("01 / Profile", accentColor)
                        Text(
                            text = profile.personalInfo.summary,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                lineHeight = 17.sp
                            ),
                            color = Ink700
                        )
                    }
                }

                val visibleExp = profile.workExperience.filter { it.visible }
                if ("workExperience" in visibleSections && visibleExp.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        EditorialSectionHeading("02 / Experience", accentColor)
                        visibleExp.forEach { exp ->
                            TimelineExperienceEntry(exp = exp, accentColor = accentColor)
                        }
                    }
                }

                if ("projects" in visibleSections && profile.projects.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EditorialSectionHeading("03 / Selected Projects", accentColor)
                        profile.projects.forEach { proj ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "${proj.name} — ${proj.role}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 11.8.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Ink950
                                )
                                Text(
                                    text = proj.description,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.8.sp),
                                    color = Ink700
                                )
                            }
                        }
                    }
                }
            }

            // Vertical Hairline Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(WarmBorder)
            )

            // Right 37% Secondary Column
            Column(
                modifier = Modifier.weight(0.37f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val visibleSkills = profile.skills.filter { it.visible }
                if ("skills" in visibleSections && visibleSkills.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EditorialSectionHeading("Competencies", accentColor)
                        RenderSkillsSection(
                            skills = visibleSkills,
                            style = customization.skillDisplayStyle,
                            accentColor = accentColor
                        )
                    }
                }

                val visibleEdu = profile.education.filter { it.visible }
                if ("education" in visibleSections && visibleEdu.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EditorialSectionHeading("Education", accentColor)
                        visibleEdu.forEach { edu ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = edu.degree,
                                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 11.5.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Ink950
                                )
                                Text(
                                    text = edu.school,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                    color = Ink700
                                )
                                Text(
                                    text = edu.periodDisplay(),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    color = accentColor
                                )
                                if (edu.description.isNotBlank()) {
                                    Text(
                                        text = edu.description,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = Ink500
                                    )
                                }
                            }
                        }
                    }
                }

                if (profile.languages.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        EditorialSectionHeading("Languages", accentColor)
                        profile.languages.filter { it.visible }.forEach { lang ->
                            Text(
                                text = "${lang.language} · ${lang.proficiency}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                color = Ink700
                            )
                        }
                    }
                }
            }
        }

        SheetFooterBar(presetName = preset.name, code = "EDIT-0${preset.id}")
    }
}

/**
 * 5. ARCHITECTURAL MASTHEAD BANNER (Presets 3 "Warm Dots" & 9 "Language Bubbles")
 * Tinted top masthead banner with left accent bar and 2x2 structured contact metadata table.
 */
@Composable
private fun ModernBannerHeaderSheet(
    profile: CvProfile,
    customization: CvCustomization,
    preset: KungaTemplatePreset,
    accentColor: Color,
    headingFont: FontFamily,
    visibleSections: List<String>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Full-bleed Tinted Banner Header with Left Architectural Accent Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(accentColor.copy(alpha = 0.08f))
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.personalInfo.fullName.ifBlank { "Your Name" },
                            fontFamily = headingFont,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Ink950
                        )
                        if (profile.personalInfo.jobTitle.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = accentColor,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = profile.personalInfo.jobTitle.uppercase(),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // 2x2 Contact Metadata Table
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (profile.personalInfo.email.isNotBlank()) {
                            SidebarMetaItem("EMAIL", profile.personalInfo.email)
                        }
                        if (profile.personalInfo.address.isNotBlank()) {
                            SidebarMetaItem("LOCATION", profile.personalInfo.address)
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (profile.personalInfo.phone.isNotBlank()) {
                            SidebarMetaItem("PHONE", profile.personalInfo.phone)
                        }
                        if (profile.personalInfo.links.primaryDisplayLink().isNotBlank()) {
                            SidebarMetaItem("WEB", profile.personalInfo.links.primaryDisplayLink())
                        }
                    }
                }
            }
        }

        HorizontalDivider(thickness = 1.5.dp, color = accentColor)

        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            visibleSections.forEach { sectionKey ->
                when (sectionKey) {
                    "personalInfo" -> {
                        if (profile.personalInfo.summary.isNotBlank()) {
                            BannerSectionBlock("Profile Summary", accentColor) {
                                Text(
                                    text = profile.personalInfo.summary,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.5.sp,
                                        lineHeight = 17.sp
                                    ),
                                    color = Ink700
                                )
                            }
                        }
                    }

                    "workExperience" -> {
                        val visibleExp = profile.workExperience.filter { it.visible }
                        if (visibleExp.isNotEmpty()) {
                            BannerSectionBlock("Experience", accentColor) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    visibleExp.forEach { exp ->
                                        TimelineExperienceEntry(exp = exp, accentColor = accentColor)
                                    }
                                }
                            }
                        }
                    }

                    "education" -> {
                        val visibleEdu = profile.education.filter { it.visible }
                        if (visibleEdu.isNotEmpty()) {
                            BannerSectionBlock("Education", accentColor) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    visibleEdu.forEach { edu ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = edu.degree,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color = Ink950
                                                )
                                                Text(
                                                    text = edu.school,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                    color = Ink500
                                                )
                                            }
                                            Text(
                                                text = edu.periodDisplay(),
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 9.5.sp,
                                                color = accentColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "skills" -> {
                        val visibleSkills = profile.skills.filter { it.visible }
                        if (visibleSkills.isNotEmpty()) {
                            BannerSectionBlock("Core Skills & Tools", accentColor) {
                                RenderSkillsSection(
                                    skills = visibleSkills,
                                    style = customization.skillDisplayStyle,
                                    accentColor = accentColor
                                )
                            }
                        }
                    }

                    "projects" -> {
                        if (profile.projects.isNotEmpty()) {
                            BannerSectionBlock("Selected Projects", accentColor) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    profile.projects.forEach { proj ->
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = "${proj.name} · ${proj.role}",
                                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = Ink950
                                            )
                                            Text(
                                                text = proj.description,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = Ink700
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SheetFooterBar(presetName = preset.name, code = "BNR-0${preset.id}")
        }
    }
}

/**
 * 6. FRAMED EDITORIAL MONOGRAPH (Preset 4 "Editorial Frame")
 * Perimeter-framed A4 sheet with boxed section header strips and classical literary serif typography.
 */
@Composable
private fun FramedMonographSheet(
    profile: CvProfile,
    customization: CvCustomization,
    preset: KungaTemplatePreset,
    accentColor: Color,
    headingFont: FontFamily,
    visibleSections: List<String>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .border(BorderStroke(1.5.dp, Ink950))
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Monograph Index Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "MONOGRAPH NO. 04",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = Ink500
                )
                Text(
                    text = "A4 ARCHIVAL SPECIFICATION",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = accentColor
                )
            }

            HorizontalDivider(thickness = 1.dp, color = WarmBorder)

            // Centered Literary Masthead
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = profile.personalInfo.fullName.ifBlank { "Your Name" },
                    fontFamily = InstrumentSerifFontFamily,
                    fontSize = 32.sp,
                    color = Ink950,
                    textAlign = TextAlign.Center
                )
                if (profile.personalInfo.jobTitle.isNotBlank()) {
                    Text(
                        text = profile.personalInfo.jobTitle,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic
                        ),
                        color = accentColor,
                        textAlign = TextAlign.Center
                    )
                }
                val contactLine = listOf(
                    profile.personalInfo.address,
                    profile.personalInfo.email,
                    profile.personalInfo.phone
                ).filter { it.isNotBlank() }.joinToString("  ·  ")
                if (contactLine.isNotEmpty()) {
                    Text(
                        text = contactLine,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        color = Ink500,
                        textAlign = TextAlign.Center
                    )
                }
            }

            visibleSections.forEach { sectionKey ->
                when (sectionKey) {
                    "personalInfo" -> {
                        if (profile.personalInfo.summary.isNotBlank()) {
                            FramedSectionBanner("I. Executive Dossier", accentColor) {
                                Text(
                                    text = profile.personalInfo.summary,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.5.sp,
                                        lineHeight = 17.sp
                                    ),
                                    color = Ink700
                                )
                            }
                        }
                    }

                    "workExperience" -> {
                        val visibleExp = profile.workExperience.filter { it.visible }
                        if (visibleExp.isNotEmpty()) {
                            FramedSectionBanner("II. Career Record", accentColor) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    visibleExp.forEach { exp ->
                                        HarvardExperienceItem(exp = exp, accentColor = accentColor)
                                    }
                                }
                            }
                        }
                    }

                    "education" -> {
                        val visibleEdu = profile.education.filter { it.visible }
                        if (visibleEdu.isNotEmpty()) {
                            FramedSectionBanner("III. Academic Credentials", accentColor) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    visibleEdu.forEach { edu ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = edu.degree,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color = Ink950
                                                )
                                                Text(
                                                    text = edu.school,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                    color = Ink500
                                                )
                                            }
                                            Text(
                                                text = edu.periodDisplay(),
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 9.5.sp,
                                                color = Ink500
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "skills" -> {
                        val visibleSkills = profile.skills.filter { it.visible }
                        if (visibleSkills.isNotEmpty()) {
                            FramedSectionBanner("IV. Core Competencies", accentColor) {
                                RenderSkillsSection(
                                    skills = visibleSkills,
                                    style = customization.skillDisplayStyle,
                                    accentColor = accentColor
                                )
                            }
                        }
                    }

                    "projects" -> {
                        if (profile.projects.isNotEmpty()) {
                            FramedSectionBanner("V. Selected Works", accentColor) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    profile.projects.forEach { proj ->
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = "${proj.name} — ${proj.role}",
                                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = Ink950
                                            )
                                            Text(
                                                text = proj.description,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = Ink700
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SheetFooterBar(presetName = preset.name, code = "MONO-04")
        }
    }
}

@Composable
private fun HarvardExperienceItem(
    exp: com.example.data.model.WorkExperience,
    accentColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        // Row 1: Company (Bold Left) + Location (Right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = exp.employer.ifBlank { exp.jobTitle },
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.5.sp),
                fontWeight = FontWeight.Bold,
                color = Ink950,
                modifier = Modifier.weight(1f)
            )
            if (exp.locationDisplay().isNotBlank()) {
                Text(
                    text = exp.locationDisplay(),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                    color = Ink500
                )
            }
        }
        // Row 2: Role (Italic Left) + Period (Tabular Mono Right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = exp.jobTitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.SemiBold
                ),
                color = accentColor,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = exp.periodDisplay(),
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 9.5.sp,
                color = Ink500
            )
        }
        // Impact Bullets
        exp.effectiveBullets().forEach { bullet ->
            Row(
                modifier = Modifier.padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "▪",
                    fontSize = 10.sp,
                    color = accentColor
                )
                Text(
                    text = bullet,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.5.sp
                    ),
                    color = Ink700
                )
            }
        }
    }
}

@Composable
private fun TimelineExperienceEntry(
    exp: com.example.data.model.WorkExperience,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Left Timeline Node & Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(accentColor, CircleShape)
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(WarmBorder)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exp.jobTitle,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                    fontWeight = FontWeight.Bold,
                    color = Ink950,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = exp.periodDisplay(),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = accentColor
                )
            }
            Text(
                text = listOf(exp.employer, exp.locationDisplay())
                    .filter { it.isNotBlank() }
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                fontWeight = FontWeight.Medium,
                color = Ink500
            )
            exp.effectiveBullets().forEach { bullet ->
                Text(
                    text = "•  $bullet",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.8.sp,
                        lineHeight = 15.sp
                    ),
                    color = Ink700,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ExecutiveSectionBlock(
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title.uppercase(),
            fontFamily = PlusJakartaSansFontFamily,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            color = Ink950
        )
        HorizontalDivider(thickness = 1.25.dp, color = accentColor)
        content()
    }
}

@Composable
private fun EditorialSectionHeading(
    title: String,
    accentColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title.uppercase(),
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = accentColor
        )
        HorizontalDivider(thickness = 1.dp, color = WarmBorder)
    }
}

@Composable
private fun SidebarSectionTitle(
    title: String,
    accentColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.3.sp,
            color = accentColor
        )
        HorizontalDivider(thickness = 1.dp, color = accentColor.copy(alpha = 0.28f))
    }
}

@Composable
private fun SidebarMetaItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 8.5.sp,
            color = Ink400
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
            color = Ink950
        )
    }
}

@Composable
private fun BannerSectionBlock(
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height(10.dp)
                    .background(accentColor, RoundedCornerShape(2.dp))
            )
            Text(
                text = title.uppercase(),
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
                color = Ink950
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = WarmBorder
            )
        }
        content()
    }
}

@Composable
private fun FramedSectionBanner(
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            color = accentColor.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title.uppercase(),
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                color = Ink950,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
        content()
    }
}

@Composable
private fun SheetFooterBar(presetName: String, code: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        HorizontalDivider(color = WarmBorder)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "KUNGACV STUDIO · A4 ARCHIVAL",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 8.5.sp,
                color = Ink400
            )
            Text(
                text = "$presetName ($code)",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 8.5.sp,
                color = Ink400
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RenderSkillsSection(
    skills: List<Skill>,
    style: SkillDisplayStyle,
    accentColor: Color
) {
    when (style) {
        SkillDisplayStyle.DOTS -> {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                skills.forEach { skill ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = skill.skill,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = Ink950,
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..4).forEach { dot ->
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(
                                            if (dot <= skill.skillLevel.dots) accentColor
                                            else WarmBorder,
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        SkillDisplayStyle.BAR -> {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                skills.forEach { skill ->
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = skill.skill,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = Ink950
                            )
                            Text(
                                text = skill.skillLevel.label.uppercase(),
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 8.5.sp,
                                color = Ink500
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(WarmBorder, RoundedCornerShape(2.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(skill.skillLevel.dots / 4f)
                                    .height(4.dp)
                                    .background(accentColor, RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }
        }

        SkillDisplayStyle.BUBBLE -> {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                skills.forEach { skill ->
                    Surface(
                        color = accentColor.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "${skill.skill} · ${skill.skillLevel.label}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            color = Ink950,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        SkillDisplayStyle.GRID -> {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                skills.forEach { skill ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(accentColor)
                        )
                        Text(
                            text = "${skill.skill} (${skill.skillLevel.label})",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.8.sp),
                            color = Ink950
                        )
                    }
                }
            }
        }
    }
}
