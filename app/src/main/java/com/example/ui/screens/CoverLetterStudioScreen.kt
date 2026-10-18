package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CoverLetterDocument
import com.example.data.model.ResumeDocument
import com.example.ui.theme.Brand50
import com.example.ui.theme.Brand600
import com.example.ui.theme.Brand700
import com.example.ui.theme.BrandTintBorder
import com.example.ui.theme.CoverLetterStageBg
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Ink400
import com.example.ui.theme.Ink500
import com.example.ui.theme.Ink700
import com.example.ui.theme.Ink950
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmInputSurface
import com.example.viewmodel.AiTaskState
import com.example.viewmodel.DataUiState
import com.example.viewmodel.KungaCvViewModel

private enum class CoverLetterStylePreset(
    val id: String,
    val label: String,
    val accentHex: Long
) {
    CLASSIC("classic", "Classic Blue", 0xFF0070F0),
    EDITORIAL("editorial", "Editorial Serif", 0xFF547980),
    WARM("warm", "Warm Terracotta", 0xFFC06C84)
}

/**
 * KungaCV Correspondence / Cover Letter Studio modeled on `frontend/kungacv_client/app/cover-letter/page.jsx`
 * with human-crafted editorial typography (Instrument Serif + JetBrains Mono + Poppins) and zero AI slop.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoverLetterStudioScreen(
    activeResume: ResumeDocument?,
    coverLettersState: DataUiState<List<CoverLetterDocument>>,
    coverLetterDraft: String,
    aiTaskState: AiTaskState,
    viewModel: KungaCvViewModel
) {
    val context = LocalContext.current

    var documentTitle by rememberSaveable { mutableStateOf("Application Cover Letter") }
    var recipientName by rememberSaveable { mutableStateOf("Hiring Manager") }
    var companyName by rememberSaveable { mutableStateOf("") }
    var targetRole by rememberSaveable { mutableStateOf(activeResume?.jobTitle.orEmpty()) }
    var subjectLine by rememberSaveable {
        mutableStateOf("Application for ${activeResume?.jobTitle?.ifBlank { "Product Role" } ?: "Product Role"}")
    }
    var jobDescription by rememberSaveable { mutableStateOf("") }
    var greeting by rememberSaveable { mutableStateOf("Dear Hiring Manager,") }
    var closing by rememberSaveable { mutableStateOf("Sincerely,") }
    var selectedTone by rememberSaveable { mutableStateOf("Executive") }
    var stylePreset by rememberSaveable { mutableStateOf(CoverLetterStylePreset.CLASSIC) }
    var activeLetterId by rememberSaveable { mutableStateOf("") }

    val defaultBodyFromResume = remember(activeResume, companyName, targetRole) {
        val role = targetRole.ifBlank { activeResume?.jobTitle ?: "this role" }
        val company = companyName.ifBlank { "your organization" }
        val firstHighlight = activeResume?.parsedExperiences()?.firstOrNull()?.highlights?.firstOrNull()
            ?: "delivering measurable user and business outcomes across cross-functional teams."
        buildString {
            appendLine("I am writing to apply for the $role position at $company.")
            appendLine()
            if (!activeResume?.summary.isNullOrBlank()) {
                appendLine(activeResume?.summary)
                appendLine()
            }
            appendLine("In my recent work, I focused on $firstHighlight I would welcome the opportunity to bring this energy and discipline to $company and contribute from day one.")
            appendLine()
            append("Thank you for considering my application. I would be glad to discuss how my experience can support your goals.")
        }
    }

    var editableBody by remember(coverLetterDraft) {
        mutableStateOf(coverLetterDraft.ifBlank { defaultBodyFromResume })
    }

    LaunchedEffect(coverLetterDraft) {
        if (coverLetterDraft.isNotBlank()) {
            editableBody = coverLetterDraft
        }
    }

    val isGenerating = aiTaskState is AiTaskState.Loading || aiTaskState is AiTaskState.Streaming

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CoverLetterStageBg),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .widthIn(max = 680.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Editorial Masthead
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "03 / CORRESPONDENCE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = Brand600
                    )
                    Text(
                        text = "Cover letter",
                        style = MaterialTheme.typography.displayMedium,
                        color = Ink950
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(EmeraldSuccess, CircleShape)
                        )
                        Text(
                            text = if (activeResume != null) {
                                "Header synced with ${activeResume.title}"
                            } else {
                                "Synced with your active dossier"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink500
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.exportCoverLetterPdf(
                            context = context,
                            letter = CoverLetterDocument(
                                id = activeLetterId,
                                resumeId = activeResume?.id.orEmpty(),
                                companyName = companyName.ifBlank { "Company" },
                                targetRole = targetRole.ifBlank { "Role" },
                                jobDescription = jobDescription,
                                tone = selectedTone,
                                content = buildString {
                                    appendLine(greeting)
                                    appendLine()
                                    appendLine(editableBody)
                                    appendLine()
                                    appendLine(closing)
                                    append(activeResume?.fullName ?: "Candidate")
                                }
                            )
                        )
                    },
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Ink950,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("export_cover_letter_pdf_button")
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

            // Section 01 / Recipient & Role
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "01 / RECIPIENT & ROLE",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = Ink500
                            )
                            Text(
                                text = "Address the letter",
                                style = MaterialTheme.typography.titleLarge,
                                color = Ink950
                            )
                        }
                        IconButton(
                            onClick = {
                                if (activeResume != null) {
                                    targetRole = activeResume.jobTitle.ifBlank { targetRole }
                                    subjectLine = "Application for ${targetRole.ifBlank { "Open Role" }}"
                                    editableBody = defaultBodyFromResume
                                    viewModel.updateCoverLetterDraft(defaultBodyFromResume)
                                }
                            },
                            modifier = Modifier.testTag("sync_from_resume_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync from resume",
                                tint = Ink950
                            )
                        }
                    }

                    OutlinedTextField(
                        value = documentTitle,
                        onValueChange = { documentTitle = it },
                        label = { Text("Document name") },
                        singleLine = true,
                        colors = kungaTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = recipientName,
                            onValueChange = {
                                recipientName = it
                                greeting = "Dear ${it.ifBlank { "Hiring Manager" }},"
                            },
                            label = { Text("Recipient name") },
                            singleLine = true,
                            colors = kungaTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("Company *") },
                            singleLine = true,
                            colors = kungaTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cl_company_input")
                        )
                    }

                    OutlinedTextField(
                        value = targetRole,
                        onValueChange = {
                            targetRole = it
                            subjectLine = "Application for ${it.ifBlank { "Role" }}"
                        },
                        label = { Text("Role you are applying for *") },
                        singleLine = true,
                        colors = kungaTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cl_role_input")
                    )

                    OutlinedTextField(
                        value = subjectLine,
                        onValueChange = { subjectLine = it },
                        label = { Text("Subject line") },
                        singleLine = true,
                        colors = kungaTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = jobDescription,
                        onValueChange = { jobDescription = it },
                        label = { Text("Role requirements or job description (optional context)") },
                        minLines = 3,
                        colors = kungaTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cl_jd_input")
                    )

                    // Editorial Voice Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "VOICE",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = Ink500
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Executive", "Impactful", "Conversational").forEach { tone ->
                                val selected = selectedTone == tone
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) Ink950 else WarmInputSurface,
                                    border = BorderStroke(1.dp, if (selected) Ink950 else WarmBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedTone = tone }
                                        .testTag("cl_tone_${tone.lowercase()}")
                                ) {
                                    Text(
                                        text = tone,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) Color.White else Ink950,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            activeLetterId = ""
                            viewModel.generateAndSaveCoverLetter(
                                companyName = companyName,
                                targetRole = targetRole,
                                jobDescription = jobDescription,
                                tone = selectedTone
                            )
                        },
                        enabled = !isGenerating,
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Ink950,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_cover_letter_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Drafting tailored letter…")
                        } else {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Draft tailored letter")
                        }
                    }
                }
            }

            // Section 02 / Letter body
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "02 / YOUR MESSAGE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = Ink500
                    )

                    OutlinedTextField(
                        value = greeting,
                        onValueChange = { greeting = it },
                        label = { Text("Salutation") },
                        singleLine = true,
                        colors = kungaTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editableBody,
                        onValueChange = {
                            editableBody = it
                            viewModel.updateCoverLetterDraft(it)
                        },
                        label = { Text("Letter body") },
                        minLines = 8,
                        colors = kungaTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cl_content_editor")
                    )

                    OutlinedTextField(
                        value = closing,
                        onValueChange = { closing = it },
                        label = { Text("Sign-off") },
                        singleLine = true,
                        colors = kungaTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                editableBody = defaultBodyFromResume
                                viewModel.updateCoverLetterDraft(defaultBodyFromResume)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Ink950,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset from resume", color = Ink950)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.saveExistingCoverLetter(
                                    CoverLetterDocument(
                                        id = activeLetterId,
                                        resumeId = activeResume?.id.orEmpty(),
                                        companyName = companyName.ifBlank { "Target Company" },
                                        targetRole = targetRole.ifBlank { "Target Role" },
                                        jobDescription = jobDescription,
                                        tone = selectedTone,
                                        content = editableBody
                                    )
                                )
                            },
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, WarmBorder),
                            modifier = Modifier.testTag("save_cover_letter_button")
                        ) {
                            Text("Save copy", color = Ink950)
                        }
                    }
                }
            }

            // Section 03 / Match your resume
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Brand50),
                border = BorderStroke(1.dp, BrandTintBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "03 / MATCH YOUR RESUME",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = Ink500
                    )
                    Text(
                        text = "Your contact header stays synced with the dossier you are editing. Pick a matching rule and typeface.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink700
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CoverLetterStylePreset.entries.forEach { item ->
                            val isSelected = stylePreset == item
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) Ink950 else WarmBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { stylePreset = item }
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(22.dp)
                                            .background(
                                                Color(item.accentHex).copy(alpha = 0.22f),
                                                RoundedCornerShape(4.dp)
                                            )
                                    )
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Ink950
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Saved copies section
            val savedLetters = (coverLettersState as? DataUiState.Success)?.data.orEmpty()
            if (savedLetters.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(14.dp),
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
                                    text = "ARCHIVE",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = Ink500
                                )
                                Text(
                                    text = "Saved letters (${savedLetters.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Ink950
                                )
                            }
                        }

                        savedLetters.forEach { letter ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = WarmInputSurface,
                                border = BorderStroke(1.dp, WarmBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        activeLetterId = letter.id
                                        companyName = letter.companyName
                                        targetRole = letter.targetRole
                                        jobDescription = letter.jobDescription
                                        selectedTone = letter.tone
                                        editableBody = letter.content
                                        viewModel.updateCoverLetterDraft(letter.content)
                                    }
                                    .testTag("saved_cl_card_${letter.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${letter.targetRole} · ${letter.companyName}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Ink950
                                        )
                                        Text(
                                            text = "${letter.tone} voice",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Ink500
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "OPEN",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            color = Brand700
                                        )
                                        IconButton(onClick = { viewModel.deleteCoverLetter(letter.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete cover letter",
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

            // Live A4 Paper Sheet Preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "A4 CORRESPONDENCE SHEET",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = Ink500
                )
                Text(
                    text = "210 × 297 MM",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = Ink400
                )
            }

            Card(
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                val letterAccent = Color(stylePreset.accentHex)
                val headerFont = if (stylePreset == CoverLetterStylePreset.EDITORIAL) {
                    PlayfairDisplayFontFamily
                } else {
                    PoppinsFontFamily
                }

                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header synced with activeResume
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeResume?.fullName?.ifBlank { "Thoko Banda" } ?: "Thoko Banda",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = headerFont,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Ink950
                            )
                            Text(
                                text = activeResume?.jobTitle?.ifBlank { targetRole } ?: targetRole,
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink500
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = activeResume?.email.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink500
                            )
                            Text(
                                text = activeResume?.phone.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink500
                            )
                            Text(
                                text = activeResume?.location.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink500
                            )
                        }
                    }

                    HorizontalDivider(
                        thickness = if (stylePreset == CoverLetterStylePreset.EDITORIAL) 1.dp else 2.5.dp,
                        color = letterAccent
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = recipientName,
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink700
                            )
                            Text(
                                text = companyName.ifBlank { "Target Organization" },
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink700
                            )
                        }
                        Text(
                            text = "October 2026",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = Ink500
                        )
                    }

                    Text(
                        text = subjectLine,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = letterAccent
                    )

                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink950
                    )

                    Text(
                        text = editableBody,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = Ink700
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = closing,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink950
                        )
                        Text(
                            text = activeResume?.fullName?.ifBlank { "Thoko Banda" } ?: "Thoko Banda",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Ink950
                        )
                    }

                    HorizontalDivider(color = WarmBorder)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Ink400,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Prepared with KungaCV",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = Ink400
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
