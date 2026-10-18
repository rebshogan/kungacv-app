package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.model.ChatMessageDocument
import com.example.data.model.GeminiModelTier
import com.example.data.model.JobStage
import com.example.data.model.ResumeDocument
import com.example.ui.theme.Brand50
import com.example.ui.theme.Brand600
import com.example.ui.theme.Brand700
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.Ink400
import com.example.ui.theme.Ink500
import com.example.ui.theme.Ink950
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmInputSurface
import com.example.ui.theme.WarmPaperBg
import com.example.viewmodel.AiTaskState
import com.example.viewmodel.DataUiState
import com.example.viewmodel.KungaCvViewModel

private enum class CoachOrTrackerTab(val label: String, val tag: String) {
    JOB_TRACKER("Application Ledger", "subtab_job_tracker"),
    AI_COACH("Editorial Desk", "subtab_ai_coach")
}

/**
 * Combines KungaCV's `/job-tracker` Application Ledger (`frontend/kungacv_client/app/job-tracker/page.jsx`)
 * with the Editorial Desk manuscript critique in a calm, human-designed layout.
 */
@Composable
fun CareerCoachChatScreen(
    activeResume: ResumeDocument?,
    chatMessagesState: DataUiState<List<ChatMessageDocument>>,
    selectedModelTier: GeminiModelTier,
    chatStreamingState: AiTaskState,
    viewModel: KungaCvViewModel
) {
    var activeTab by rememberSaveable { mutableStateOf(CoachOrTrackerTab.AI_COACH) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmPaperBg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Minimalist Underline Tab Switcher (Read.cv / Linear style)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                CoachOrTrackerTab.entries.forEach { tab ->
                    val selected = activeTab == tab
                    Column(
                        modifier = Modifier
                            .clickable { activeTab = tab }
                            .padding(top = 4.dp)
                            .testTag(tab.tag),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) Ink950 else Ink500,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .width(if (selected) 56.dp else 0.dp)
                                .height(2.dp)
                                .background(if (selected) Ink950 else Color.Transparent)
                        )
                    }
                }
            }
            HorizontalDivider(color = WarmBorder)
        }

        when (activeTab) {
            CoachOrTrackerTab.AI_COACH -> EditorialDeskCritiquePane(
                activeResume = activeResume,
                chatMessagesState = chatMessagesState,
                selectedModelTier = selectedModelTier,
                chatStreamingState = chatStreamingState,
                viewModel = viewModel
            )

            CoachOrTrackerTab.JOB_TRACKER -> KungaJobTrackerPane(
                activeResume = activeResume,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun EditorialDeskCritiquePane(
    activeResume: ResumeDocument?,
    chatMessagesState: DataUiState<List<ChatMessageDocument>>,
    selectedModelTier: GeminiModelTier,
    chatStreamingState: AiTaskState,
    viewModel: KungaCvViewModel
) {
    var messageInput by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    val messages = (chatMessagesState as? DataUiState.Success)?.data.orEmpty()
    val isBusy = chatStreamingState is AiTaskState.Loading || chatStreamingState is AiTaskState.Streaming

    LaunchedEffect(messages.size, chatStreamingState) {
        val totalItems = messages.size + if (chatStreamingState !is AiTaskState.Idle) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp)
    ) {
        // Editorial Desk Header Bar
        Surface(
            color = Color.White,
            border = BorderStroke(1.dp, WarmBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "EDITORIAL DESK · DOSSIER CRITIQUE",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = Brand600
                        )
                        Text(
                            text = if (activeResume != null) {
                                "Reviewing ${activeResume.title} · ${activeResume.fullName}"
                            } else {
                                "Positioning, bullet phrasing & interview notes"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink500
                        )
                    }
                    if (messages.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearCareerCoachChat() },
                            modifier = Modifier.testTag("clear_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear notes",
                                tint = Ink500
                            )
                        }
                    }
                }

                // Editorial Depth Selector (clean mono index chips instead of raw AI model badges)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val depthLabels = mapOf(
                        GeminiModelTier.FAST to ("01" to "Quick Note"),
                        GeminiModelTier.GENERAL to ("02" to "Editorial Review"),
                        GeminiModelTier.COMPLEX to ("03" to "Deep Audit")
                    )
                    GeminiModelTier.entries.forEach { tier ->
                        val isSelected = selectedModelTier == tier
                        val (idxCode, label) = depthLabels[tier] ?: ("01" to tier.label)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Ink950 else WarmInputSurface,
                            border = BorderStroke(1.dp, if (isSelected) Ink950 else WarmBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectModelTier(tier) }
                                .testTag("model_tier_chip_${tier.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = idxCode,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.65f) else Ink400
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White else Ink950
                                )
                            }
                        }
                    }
                }
            }
        }

        // Scrollable Editorial Notes Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("career_coach_message_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty() && chatStreamingState is AiTaskState.Idle) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, WarmBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "MANUSCRIPT REVIEW",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = Brand600
                            )
                            Text(
                                text = "Sharpen your career narrative.",
                                style = MaterialTheme.typography.displayMedium,
                                color = Ink950
                            )
                            Text(
                                text = "Select a review brief below or ask for specific bullet rewrites, ATS keyword alignment, or interview talking points.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink500
                            )

                            HorizontalDivider(color = Ink950, thickness = 1.dp)

                            listOf(
                                "01" to "Rewrite my executive summary to sound more results-driven",
                                "02" to "Audit my resume for missing ATS keywords and metrics",
                                "03" to "Turn my latest role into 3 quantified achievement bullets",
                                "04" to "Give me 3 interview stories based on my experience"
                            ).forEachIndexed { idx, (code, prompt) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.sendCareerCoachMessage(prompt) }
                                        .padding(vertical = 10.dp)
                                        .testTag("starter_prompt_$idx"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = code,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 11.sp,
                                            color = Brand600
                                        )
                                        Text(
                                            text = prompt,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = Ink950
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Ink400,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                HorizontalDivider(color = WarmBorder)
                            }
                        }
                    }
                }
            }

            items(messages, key = { it.id.ifBlank { it.text.hashCode().toString() } }) { message ->
                val isUser = message.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) Ink950 else Color.White
                        ),
                        border = if (isUser) null else BorderStroke(1.dp, WarmBorder),
                        modifier = Modifier.widthIn(max = 540.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isUser) "AUTHOR NOTE" else "EDITORIAL DESK",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = if (isUser) Color.White.copy(alpha = 0.65f) else Brand600
                            )
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUser) Color.White else Ink950
                            )
                            if (!isUser && message.suggestedSummary.isNotBlank()) {
                                Surface(
                                    color = WarmInputSurface,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, WarmBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "PROPOSED SUMMARY REVISION",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            color = Brand700
                                        )
                                        Text(
                                            text = message.suggestedSummary,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Ink950
                                        )
                                        Button(
                                            onClick = {
                                                viewModel.applySuggestedSummaryToActiveCv(
                                                    message.suggestedSummary
                                                )
                                            },
                                            shape = RoundedCornerShape(999.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Ink950),
                                            modifier = Modifier.testTag("apply_suggested_summary_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Apply to Active CV")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            when (chatStreamingState) {
                is AiTaskState.Loading -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, WarmBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Ink950
                                )
                                Text(
                                    text = "Reviewing your dossier…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Ink500
                                )
                            }
                        }
                    }
                }

                is AiTaskState.Streaming -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Ink950),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "DRAFTING NOTE",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = Brand600
                                )
                                Text(
                                    text = chatStreamingState.partialText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Ink950
                                )
                            }
                        }
                    }
                }

                is AiTaskState.Error -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = chatStreamingState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = CrimsonError,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                else -> {}
            }
        }

        // Minimalist Editorial Composer Bar
        Surface(
            color = Color.White,
            border = BorderStroke(1.dp, WarmBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text("Ask for bullet rewrites, positioning, or interview notes…") },
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = kungaTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input")
                )

                Button(
                    onClick = {
                        val toSend = messageInput.trim()
                        if (toSend.isNotBlank()) {
                            messageInput = ""
                            viewModel.sendCareerCoachMessage(toSend)
                        }
                    },
                    enabled = !isBusy && messageInput.isNotBlank(),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ink950),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("send_chat_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send note",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Authentic Job Tracker Pane modeled directly on `frontend/kungacv_client/app/job-tracker/page.jsx`
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KungaJobTrackerPane(
    activeResume: ResumeDocument?,
    viewModel: KungaCvViewModel
) {
    val jobs by viewModel.jobApplications.collectAsStateWithLifecycle()

    var company by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf("") }
    var selectedStage by rememberSaveable { mutableStateOf(JobStage.APPLIED) }
    var nextFollowUp by rememberSaveable { mutableStateOf("") }
    var coverLetterNote by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .widthIn(max = 680.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "04 / APPLICATION LEDGER",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                color = Brand600
            )
            Text(
                text = "Job tracker",
                style = MaterialTheme.typography.displayMedium,
                color = Ink950
            )
            Text(
                text = "Keep every application, linked resume version, and follow-up date in one calm ledger.",
                style = MaterialTheme.typography.bodySmall,
                color = Ink500
            )
        }

        // Architectural 4-stage summary strip
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, WarmBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                JobStage.entries.forEach { stage ->
                    val count = jobs.count { it.stage == stage }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = count.toString().padStart(2, '0'),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Ink950
                        )
                        Text(
                            text = stage.label.uppercase(),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = Ink500
                        )
                    }
                }
            }
        }

        // Add Application Card
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
                Text(
                    text = "NEW ENTRY",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = Brand600
                )
                Text(
                    text = "Log a job application",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink950
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("Company *") },
                        singleLine = true,
                        colors = kungaTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tracker_company_input")
                    )
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Role *") },
                        singleLine = true,
                        colors = kungaTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tracker_role_input")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nextFollowUp,
                        onValueChange = { nextFollowUp = it },
                        label = { Text("Next follow-up (e.g., 28 Oct)") },
                        singleLine = true,
                        colors = kungaTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = coverLetterNote,
                        onValueChange = { coverLetterNote = it },
                        label = { Text("Notes / Salary / Link") },
                        singleLine = true,
                        colors = kungaTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    JobStage.entries.forEach { stage ->
                        val active = selectedStage == stage
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (active) Ink950 else WarmInputSurface,
                            border = BorderStroke(1.dp, if (active) Ink950 else WarmBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedStage = stage }
                        ) {
                            Text(
                                text = stage.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (active) Color.White else Ink950,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (company.isNotBlank() && role.isNotBlank()) {
                            viewModel.addJobApplication(
                                company = company,
                                role = role,
                                stage = selectedStage,
                                nextFollowUp = nextFollowUp.ifBlank { "Next week" },
                                linkedResumeTitle = activeResume?.title ?: "Primary CV",
                                linkedCoverLetterTitle = coverLetterNote.ifBlank { "Synced letter" }
                            )
                            company = ""
                            role = ""
                            nextFollowUp = ""
                            coverLetterNote = ""
                        }
                    },
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ink950),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_job_application_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Application")
                }
            }
        }

        // Application List
        jobs.forEach { job ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = job.role,
                                style = MaterialTheme.typography.titleMedium,
                                color = Ink950
                            )
                            Text(
                                text = "${job.company} · CV: ${job.linkedResumeTitle}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Brand700,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Follow-up: ${job.nextFollowUp} · ${job.linkedCoverLetterTitle}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink500
                            )
                        }
                        IconButton(onClick = { viewModel.removeJobApplication(job.id) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Remove application",
                                tint = CrimsonError
                            )
                        }
                    }

                    // Stage switcher pills
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        JobStage.entries.forEach { stage ->
                            val isCurrent = job.stage == stage
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isCurrent) Brand50 else WarmInputSurface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isCurrent) Brand600 else WarmBorder
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.updateJobStage(job.id, stage) }
                            ) {
                                Text(
                                    text = stage.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isCurrent) Brand700 else Ink500,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
