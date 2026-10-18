package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CvCustomization
import com.example.data.model.CvDate
import com.example.data.model.CvLayoutArchitecture
import com.example.data.model.CvTemplate
import com.example.data.model.Education
import com.example.data.model.KungaTemplateCatalog
import com.example.data.model.KungaTemplatePreset
import com.example.data.model.ProjectEntry
import com.example.data.model.SkillDisplayStyle
import com.example.data.model.TemplateFilterCategory
import com.example.data.model.WorkExperience
import com.example.ui.theme.Brand50
import com.example.ui.theme.Brand600
import com.example.ui.theme.Brand700
import com.example.ui.theme.Ink400
import com.example.ui.theme.Ink500
import com.example.ui.theme.Ink950
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmInputSurface

@Composable
fun kungaTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = WarmInputSurface,
    unfocusedContainerColor = WarmInputSurface,
    focusedBorderColor = Brand600,
    unfocusedBorderColor = WarmBorder,
    focusedLabelColor = Brand700,
    unfocusedLabelColor = Ink500,
    focusedTextColor = Ink950,
    unfocusedTextColor = Ink950
)

/**
 * Authentic SectionOrganizer card from `frontend/kungacv_client/components/ResumeComponents/SectionOrganizer.jsx`
 */
@Composable
fun SectionOrganizerCard(
    customization: CvCustomization,
    onMoveSection: (String, Int) -> Unit,
    onToggleVisibility: (String) -> Unit
) {
    val sectionLabels = mapOf(
        "personalInfo" to "Personal Info & Summary",
        "workExperience" to "Professional Experience",
        "education" to "Education",
        "skills" to "Skills & Proficiency",
        "projects" to "Projects"
    )

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
            Text(
                text = "SECTION ORGANIZER",
                style = MaterialTheme.typography.labelSmall,
                color = Brand600
            )
            Text(
                text = "Order & visibility on your A4 CV",
                style = MaterialTheme.typography.titleMedium,
                color = Ink950
            )
            customization.sectionOrder.forEachIndexed { idx, key ->
                val isHidden = key in customization.hiddenSections
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = WarmInputSurface,
                    border = BorderStroke(1.dp, WarmBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${idx + 1}. ${sectionLabels[key] ?: key}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isHidden) Ink400 else Ink950,
                            modifier = Modifier.weight(1f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onMoveSection(key, -1) },
                                enabled = idx > 0
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Move section up",
                                    tint = if (idx > 0) Ink950 else Ink400,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { onMoveSection(key, 1) },
                                enabled = idx < customization.sectionOrder.lastIndex
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Move section down",
                                    tint = if (idx < customization.sectionOrder.lastIndex) Ink950 else Ink400,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(onClick = { onToggleVisibility(key) }) {
                                Icon(
                                    imageVector = if (isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle section visibility",
                                    tint = if (isHidden) Ink400 else Brand600,
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

/**
 * Customization Studio card matching `frontend/kungacv_client/components/customization/Customization.jsx`
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CvCustomizationPanel(
    customization: CvCustomization,
    onSelectPreset: (KungaTemplatePreset) -> Unit,
    onUpdateCustomization: (CvCustomization) -> Unit,
    onMoveSection: (String, Int) -> Unit,
    onToggleVisibility: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 1. Template Presets & Accent Colors
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
                    text = "01 / TEMPLATE & ACCENT COLOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = Brand600
                )
                Text(
                    text = "Pick a KungaCV preset or accent color",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink950
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KungaTemplateCatalog.presets.forEach { preset ->
                        val isSelected = customization.presetId == preset.id
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Ink950 else WarmInputSurface,
                            border = BorderStroke(1.dp, if (isSelected) Ink950 else WarmBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectPreset(preset) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(preset.accentHex), CircleShape)
                                )
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else Ink950
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Column Layout & Skill Visualization Style
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
                    text = "02 / COLUMNS, SKILLS & TYPOGRAPHY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Brand600
                )
                Text(
                    text = "Column structure",
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink950
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        Triple(1, "top", "Single Column"),
                        Triple(2, "right", "Two Column (Right Sidebar)"),
                        Triple(2, "left", "Two Column (Left Sidebar)")
                    ).forEach { (cols, dir, label) ->
                        val active = customization.columns == cols &&
                            (cols == 1 || customization.direction == dir)
                        OutlinedButton(
                            onClick = {
                                onUpdateCustomization(
                                    customization.copy(columns = cols, direction = dir)
                                )
                            },
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, if (active) Brand600 else WarmBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (active) Brand50 else Color.White,
                                contentColor = if (active) Brand700 else Ink950
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                Text(
                    text = "Skills display style",
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink950
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkillDisplayStyle.entries.forEach { style ->
                        val active = customization.skillDisplayStyle == style
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (active) Brand600 else WarmInputSurface,
                            border = BorderStroke(1.dp, if (active) Brand600 else WarmBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable {
                                    onUpdateCustomization(
                                        customization.copy(skillDisplayStyle = style)
                                    )
                                }
                        ) {
                            Text(
                                text = style.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) Color.White else Ink950,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Document heading font",
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink950
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Poppins", "Playfair Display", "Plus Jakarta Sans").forEach { fontName ->
                        val active = customization.fontFamily == fontName
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (active) Ink950 else WarmInputSurface,
                            border = BorderStroke(1.dp, if (active) Ink950 else WarmBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable {
                                    onUpdateCustomization(customization.copy(fontFamily = fontName))
                                }
                        ) {
                            Text(
                                text = fontName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (active) Color.White else Ink950,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Section Organizer
        SectionOrganizerCard(
            customization = customization,
            onMoveSection = onMoveSection,
            onToggleVisibility = onToggleVisibility
        )
    }
}

/**
 * Template Gallery Section from `frontend/kungacv_client/app/resume/page.jsx`
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KungaTemplateGallerySection(
    activeCategory: TemplateFilterCategory,
    selectedPresetId: Int,
    onSelectCategory: (TemplateFilterCategory) -> Unit,
    onUsePreset: (KungaTemplatePreset) -> Unit,
    onStartFromScratch: () -> Unit
) {
    val filteredPresets = remember(activeCategory) {
        KungaTemplateCatalog.presets.filter { activeCategory in it.categories }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Filter chips matching template-filter in globals.css
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TemplateFilterCategory.entries.forEach { cat ->
                val active = activeCategory == cat
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (active) Ink950 else Color.White,
                    border = BorderStroke(1.dp, if (active) Ink950 else WarmBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onSelectCategory(cat) }
                        .testTag("filter_chip_${cat.id}")
                ) {
                    Text(
                        text = cat.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (active) Color.White else Ink950,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Start from scratch card + Template Preset cards
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.5.dp, WarmBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStartFromScratch() }
                .testTag("start_from_scratch_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = WarmInputSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, WarmBorder),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Ink950
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Start from scratch",
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink950
                        )
                        Text(
                            text = "Blank, flexible and yours",
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink500
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Ink950
                )
            }
        }

        filteredPresets.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { preset ->
                    val isSelected = selectedPresetId == preset.id
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) Ink950 else WarmBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onUsePreset(preset) }
                            .testTag("template_preset_card_${preset.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // High-precision miniature A4 sheet rendering the preset's true architecture
                            MiniatureCvPagePreview(preset = preset)

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = preset.categoryBadge,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(preset.accentHex)
                                    )
                                    if (isSelected) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Ink950
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected template",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Text(
                                                    text = "ACTIVE",
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 7.5.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Ink950
                                )
                                Text(
                                    text = preset.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.5.sp,
                                        lineHeight = 14.sp
                                    ),
                                    color = Ink500,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Miniature A4 CV architectural preview that renders the real visual structure of each
 * of the 6 KungaCV layout architectures inside the template gallery cards.
 */
@Composable
private fun MiniatureCvPagePreview(preset: KungaTemplatePreset) {
    val accent = Color(preset.accentHex)
    val inkBar = Ink950.copy(alpha = 0.82f)
    val bodyLine = Ink500.copy(alpha = 0.28f)
    val subtleLine = Ink400.copy(alpha = 0.18f)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = WarmInputSurface,
        border = BorderStroke(1.dp, WarmBorder),
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Miniature A4 Paper Sheet
            Surface(
                shape = RoundedCornerShape(3.dp),
                color = Color.White,
                shadowElevation = 3.dp,
                border = BorderStroke(0.75.dp, WarmBorder),
                modifier = Modifier.fillMaxSize()
            ) {
                when (preset.architecture) {
                    CvLayoutArchitecture.HARVARD_EXECUTIVE -> {
                        // Centered Masthead + Double Rule + Single Column Tabular Dates
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.52f)
                                        .height(5.dp)
                                        .background(inkBar, RoundedCornerShape(1.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.34f)
                                        .height(2.5.dp)
                                        .background(accent, RoundedCornerShape(1.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.68f)
                                        .height(2.dp)
                                        .background(subtleLine, RoundedCornerShape(1.dp))
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                HorizontalDivider(thickness = 1.5.dp, color = Ink950)
                                HorizontalDivider(thickness = 0.5.dp, color = accent)
                            }
                            repeat(2) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.5.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.38f)
                                            .height(2.5.dp)
                                            .background(accent, RoundedCornerShape(1.dp))
                                    )
                                    HorizontalDivider(thickness = 0.5.dp, color = accent.copy(alpha = 0.5f))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.45f)
                                                .height(3.dp)
                                                .background(inkBar, RoundedCornerShape(1.dp))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.28f)
                                                .height(2.5.dp)
                                                .background(bodyLine, RoundedCornerShape(1.dp))
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.92f)
                                            .height(2.dp)
                                            .background(bodyLine, RoundedCornerShape(1.dp))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.78f)
                                            .height(2.dp)
                                            .background(subtleLine, RoundedCornerShape(1.dp))
                                    )
                                }
                            }
                        }
                    }

                    CvLayoutArchitecture.SWISS_INDEX_RAIL -> {
                        // Read.cv / Swiss 26/74 Left-Rail Index Grid
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 9.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(0.28f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(14.dp)
                                            .height(3.dp)
                                            .background(accent)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.75f)
                                            .height(2.dp)
                                            .background(bodyLine)
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(0.72f),
                                    verticalArrangement = Arrangement.spacedBy(2.5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.72f)
                                            .height(5.dp)
                                            .background(inkBar, RoundedCornerShape(1.dp))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.48f)
                                            .height(2.5.dp)
                                            .background(accent, RoundedCornerShape(1.dp))
                                    )
                                }
                            }
                            repeat(3) {
                                HorizontalDivider(thickness = 0.6.dp, color = WarmBorder)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.weight(0.28f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(10.dp)
                                                .height(2.5.dp)
                                                .background(accent, RoundedCornerShape(1.dp))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.8f)
                                                .height(2.dp)
                                                .background(subtleLine)
                                        )
                                    }
                                    Column(
                                        modifier = Modifier.weight(0.72f),
                                        verticalArrangement = Arrangement.spacedBy(2.5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.65f)
                                                .height(3.dp)
                                            .background(inkBar, RoundedCornerShape(1.dp))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.95f)
                                                .height(2.dp)
                                                .background(bodyLine, RoundedCornerShape(1.dp))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.8f)
                                                .height(2.dp)
                                                .background(subtleLine, RoundedCornerShape(1.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    CvLayoutArchitecture.ARCHITECTURAL_LEFT_SIDEBAR -> {
                        // Full-Bleed Tinted Left 35% Dossier Sidebar + 65% Right Main Column
                        Row(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .weight(0.36f)
                                    .fillMaxHeight()
                                    .background(accent.copy(alpha = 0.11f))
                                    .padding(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(accent, RoundedCornerShape(3.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .height(2.dp)
                                        .background(accent)
                                )
                                repeat(3) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .height(2.dp)
                                            .background(bodyLine, RoundedCornerShape(1.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.65f)
                                        .height(2.dp)
                                        .background(accent)
                                )
                                repeat(3) { idx ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .background(WarmBorder, RoundedCornerShape(1.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(if (idx == 0) 0.9f else if (idx == 1) 0.75f else 0.82f)
                                                .height(3.dp)
                                                .background(accent, RoundedCornerShape(1.dp))
                                        )
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .width(0.75.dp)
                                    .fillMaxHeight()
                                    .background(WarmBorder)
                            )
                            Column(
                                modifier = Modifier
                                    .weight(0.64f)
                                    .fillMaxHeight()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.75f)
                                        .height(5.dp)
                                        .background(inkBar, RoundedCornerShape(1.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.45f)
                                        .height(2.5.dp)
                                        .background(accent, RoundedCornerShape(1.dp))
                                )
                                HorizontalDivider(thickness = 1.dp, color = accent)
                                repeat(2) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.6f)
                                                .height(3.dp)
                                                .background(inkBar, RoundedCornerShape(1.dp))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.95f)
                                                .height(2.dp)
                                                .background(bodyLine, RoundedCornerShape(1.dp))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.82f)
                                                .height(2.dp)
                                                .background(subtleLine, RoundedCornerShape(1.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    CvLayoutArchitecture.EDITORIAL_RIGHT_SIDEBAR -> {
                        // Literary Masthead + 64/36 Vertical Hairline Split
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 9.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .width(48.dp)
                                            .height(5.5.dp)
                                            .background(inkBar, RoundedCornerShape(1.dp))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(32.dp)
                                            .height(2.5.dp)
                                            .background(accent, RoundedCornerShape(1.dp))
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(1.5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(24.dp)
                                            .height(2.dp)
                                            .background(bodyLine)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(28.dp)
                                            .height(2.dp)
                                            .background(subtleLine)
                                    )
                                }
                            }
                            HorizontalDivider(thickness = 1.dp, color = accent)
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(0.62f),
                                    verticalArrangement = Arrangement.spacedBy(3.5.dp)
                                ) {
                                    repeat(2) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.5f)
                                                .height(2.5.dp)
                                                .background(accent, RoundedCornerShape(1.dp))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.95f)
                                                .height(2.5.dp)
                                                .background(inkBar, RoundedCornerShape(1.dp))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.9f)
                                                .height(2.dp)
                                                .background(bodyLine, RoundedCornerShape(1.dp))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.75f)
                                                .height(2.dp)
                                                .background(subtleLine, RoundedCornerShape(1.dp))
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .width(0.75.dp)
                                        .fillMaxHeight()
                                        .background(WarmBorder)
                                )
                                Column(
                                    modifier = Modifier.weight(0.38f),
                                    verticalArrangement = Arrangement.spacedBy(3.5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.7f)
                                            .height(2.5.dp)
                                            .background(accent, RoundedCornerShape(1.dp))
                                    )
                                    repeat(4) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(0.5f)
                                                    .height(2.dp)
                                                    .background(inkBar)
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
                                                repeat(3) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(3.dp)
                                                            .background(accent, CircleShape)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    CvLayoutArchitecture.MODERN_BANNER_HEADER -> {
                        // Top Tinted Architectural Banner + Left Vertical Accent Strip
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .background(accent.copy(alpha = 0.12f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .fillMaxHeight()
                                        .background(accent)
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.56f)
                                            .height(5.dp)
                                            .background(inkBar, RoundedCornerShape(1.dp))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(34.dp)
                                            .height(4.dp)
                                            .background(accent, RoundedCornerShape(2.dp))
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .width(28.dp)
                                                .height(2.dp)
                                                .background(bodyLine)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(28.dp)
                                                .height(2.dp)
                                                .background(bodyLine)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(thickness = 1.dp, color = accent)
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(2) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(accent, RoundedCornerShape(1.dp))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.4f)
                                                .height(2.5.dp)
                                                .background(inkBar, RoundedCornerShape(1.dp))
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.92f)
                                            .height(2.dp)
                                            .background(bodyLine, RoundedCornerShape(1.dp))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.76f)
                                            .height(2.dp)
                                            .background(subtleLine, RoundedCornerShape(1.dp))
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    repeat(3) {
                                        Box(
                                            modifier = Modifier
                                                .width(22.dp)
                                                .height(6.dp)
                                                .background(accent.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                                .border(0.5.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    CvLayoutArchitecture.FRAMED_MONOGRAPH -> {
                        // Perimeter-Framed Monograph with Tinted Section Banners
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(5.dp)
                                .border(1.dp, Ink950)
                                .padding(6.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.55f)
                                            .height(5.dp)
                                            .background(inkBar, RoundedCornerShape(1.dp))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.35f)
                                            .height(2.dp)
                                            .background(accent, RoundedCornerShape(1.dp))
                                    )
                                }
                                repeat(2) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .background(accent.copy(alpha = 0.12f))
                                            .border(0.5.dp, accent.copy(alpha = 0.45f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .height(2.5.dp)
                                            .background(inkBar, RoundedCornerShape(1.dp))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.82f)
                                            .height(2.dp)
                                            .background(bodyLine, RoundedCornerShape(1.dp))
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

/**
 * Structured WorkExperience Dialog supporting full fields from `redux/resume/helper.js`:
 * `jobTitle`, `employer`, `city`, `country`, `startDate` (`month`, `year`), `endDate` (`month`, `year`, `presentYear`), and `highlights`.
 */
@Composable
fun WorkExperienceEditorDialog(
    initial: WorkExperience,
    onDismiss: () -> Unit,
    onSave: (WorkExperience) -> Unit
) {
    var jobTitle by remember { mutableStateOf(initial.jobTitle) }
    var employer by remember { mutableStateOf(initial.employer) }
    var city by remember { mutableStateOf(initial.city) }
    var country by remember { mutableStateOf(initial.country) }
    var startMonth by remember { mutableStateOf(initial.startDate.month) }
    var startYear by remember { mutableStateOf(initial.startDate.year) }
    var endMonth by remember { mutableStateOf(initial.endDate.month) }
    var endYear by remember { mutableStateOf(initial.endDate.year) }
    var isPresent by remember { mutableStateOf(initial.endDate.presentYear) }
    var bulletsText by remember { mutableStateOf(initial.effectiveBullets().joinToString("\n")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial.jobTitle.isBlank()) "Add Work Experience" else "Edit Work Experience",
                style = MaterialTheme.typography.titleLarge,
                color = Ink950
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("Job Title *") },
                    singleLine = true,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exp_role_input")
                )
                OutlinedTextField(
                    value = employer,
                    onValueChange = { employer = it },
                    label = { Text("Employer / Organization *") },
                    singleLine = true,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exp_company_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City (e.g., Lilongwe)") },
                        singleLine = true,
                        colors = kungaTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("exp_location_input")
                    )
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Country") },
                        singleLine = true,
                        colors = kungaTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startMonth,
                        onValueChange = { startMonth = it },
                        label = { Text("Start Month") },
                        singleLine = true,
                        colors = kungaTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = startYear,
                        onValueChange = { startYear = it },
                        label = { Text("Start Year") },
                        singleLine = true,
                        colors = kungaTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("exp_period_input")
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Checkbox(
                        checked = isPresent,
                        onCheckedChange = { isPresent = it }
                    )
                    Text(
                        text = "I currently work here (Present)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink950
                    )
                }
                if (!isPresent) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = endMonth,
                            onValueChange = { endMonth = it },
                            label = { Text("End Month") },
                            singleLine = true,
                            colors = kungaTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endYear,
                            onValueChange = { endYear = it },
                            label = { Text("End Year") },
                            singleLine = true,
                            colors = kungaTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                OutlinedTextField(
                    value = bulletsText,
                    onValueChange = { bulletsText = it },
                    label = { Text("Achievement Highlights (one per line)") },
                    minLines = 4,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exp_highlights_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (jobTitle.isNotBlank() && employer.isNotBlank()) {
                        val parsedBullets = bulletsText.lines()
                            .map { it.removePrefix("•").trim() }
                            .filter { it.isNotBlank() }
                        onSave(
                            initial.copy(
                                jobTitle = jobTitle.trim(),
                                employer = employer.trim(),
                                city = city.trim(),
                                country = country.trim(),
                                startDate = CvDate(month = startMonth.trim(), year = startYear.trim()),
                                endDate = CvDate(
                                    month = endMonth.trim(),
                                    year = endYear.trim(),
                                    presentYear = isPresent
                                ),
                                description = parsedBullets.joinToString("\n"),
                                highlights = parsedBullets
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                modifier = Modifier.testTag("save_experience_dialog_button")
            ) {
                Text("Save Experience")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Ink500)
            }
        }
    )
}

@Composable
fun StructuredEducationEditorDialog(
    initial: Education,
    onDismiss: () -> Unit,
    onSave: (Education) -> Unit
) {
    var degree by remember { mutableStateOf(initial.degree) }
    var school by remember { mutableStateOf(initial.school) }
    var city by remember { mutableStateOf(initial.city) }
    var startYear by remember { mutableStateOf(initial.startDate.year) }
    var endYear by remember { mutableStateOf(initial.endDate.year) }
    var description by remember { mutableStateOf(initial.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial.degree.isBlank()) "Add Education" else "Edit Education",
                style = MaterialTheme.typography.titleLarge,
                color = Ink950
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = degree,
                    onValueChange = { degree = it },
                    label = { Text("Degree / Qualification *") },
                    singleLine = true,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edu_degree_input")
                )
                OutlinedTextField(
                    value = school,
                    onValueChange = { school = it },
                    label = { Text("School / University *") },
                    singleLine = true,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edu_institution_input")
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City / Country (e.g., Zomba, Malawi)") },
                    singleLine = true,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startYear,
                        onValueChange = { startYear = it },
                        label = { Text("Start Year") },
                        singleLine = true,
                        colors = kungaTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endYear,
                        onValueChange = { endYear = it },
                        label = { Text("Graduation Year") },
                        singleLine = true,
                        colors = kungaTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edu_year_input")
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Honors, Coursework, or Details") },
                    minLines = 2,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edu_details_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (degree.isNotBlank() && school.isNotBlank()) {
                        onSave(
                            initial.copy(
                                degree = degree.trim(),
                                school = school.trim(),
                                city = city.trim(),
                                startDate = CvDate(year = startYear.trim(), onlyYear = true),
                                endDate = CvDate(year = endYear.trim(), onlyYear = true),
                                description = description.trim()
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                modifier = Modifier.testTag("save_education_dialog_button")
            ) {
                Text("Save Education")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Ink500)
            }
        }
    )
}

@Composable
fun CreateResumeModal(
    onDismiss: () -> Unit,
    onCreate: (String, CvTemplate) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf(CvTemplate.EXECUTIVE_NAVY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create New Resume",
                style = MaterialTheme.typography.titleLarge,
                color = Ink950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Resume Title (e.g., Product Design CV)") },
                    singleLine = true,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_resume_title_input")
                )
                Text(
                    text = "Select Starting Style",
                    style = MaterialTheme.typography.labelLarge,
                    color = Ink950
                )
                CvTemplate.entries.forEach { tmpl ->
                    val active = selectedTemplate == tmpl
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (active) Brand50 else WarmInputSurface,
                        border = BorderStroke(1.dp, if (active) Brand600 else WarmBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTemplate = tmpl }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = tmpl.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Ink950
                            )
                            Text(
                                text = tmpl.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink500
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title, selectedTemplate) },
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                modifier = Modifier.testTag("confirm_create_resume_button")
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Ink500)
            }
        }
    )
}

@Composable
fun ProjectEditorModal(
    initial: ProjectEntry,
    onDismiss: () -> Unit,
    onSave: (ProjectEntry) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var role by remember { mutableStateOf(initial.role) }
    var link by remember { mutableStateOf(initial.link) }
    var description by remember { mutableStateOf(initial.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.name.isBlank()) "Add Project" else "Edit Project") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Title *") },
                    singleLine = true,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("proj_name_input")
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Your Role / Subtitle") },
                    singleLine = true,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("proj_role_input")
                )
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Link / Repository URL") },
                    singleLine = true,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("proj_link_input")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Impact & Description") },
                    minLines = 3,
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("proj_description_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            initial.copy(
                                name = name.trim(),
                                role = role.trim(),
                                link = link.trim(),
                                description = description.trim()
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                modifier = Modifier.testTag("save_project_dialog_button")
            ) {
                Text("Save Project")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Ink500)
            }
        }
    )
}
