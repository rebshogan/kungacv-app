package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KungaTemplateCatalog
import com.example.ui.components.KungaCvLogo
import com.example.ui.theme.Brand600
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Ink400
import com.example.ui.theme.Ink500
import com.example.ui.theme.Ink700
import com.example.ui.theme.Ink950
import com.example.ui.theme.InstrumentSerifFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmInputSurface
import com.example.ui.theme.WarmPaperBg
import kotlinx.coroutines.launch

/**
 * Editorial Studio Launch Screen inspired by Read.cv & Craft.do.
 * Uses the signature Warm Paper (#FAF9F5) surface with subtle architectural grid rules,
 * the authentic KungaCV mark, and a hairline progress bar.
 */
@Composable
fun KungaLaunchScreen(
    statusText: String = "Preparing your workspace"
) {
    val transition = rememberInfiniteTransition(label = "launchPulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmPaperBg)
            .drawBehind {
                val stroke = 1.dp.toPx()
                val ruleColor = Color(0xFFE6E4DD).copy(alpha = 0.7f)
                // Architectural folio margin rules
                drawLine(
                    color = ruleColor,
                    start = Offset(28.dp.toPx(), 0f),
                    end = Offset(28.dp.toPx(), size.height),
                    strokeWidth = stroke
                )
                drawLine(
                    color = ruleColor,
                    start = Offset(size.width - 28.dp.toPx(), 0f),
                    end = Offset(size.width - 28.dp.toPx(), size.height),
                    strokeWidth = stroke
                )
            }
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("kunga_launch_screen")
    ) {
        // Top Folio Index
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 44.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "KUNGACV / EDITION 02",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = Ink500
            )
            Text(
                text = "A4 · 210 × 297 MM",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = Ink400
            )
        }

        // Center Brand Mark & Literary Tagline
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 44.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            KungaCvLogo(
                onDark = false,
                markSize = 40.dp,
                fontSize = 24.sp
            )

            Text(
                text = "Quiet tools for serious career work.",
                fontFamily = InstrumentSerifFontFamily,
                fontSize = 36.sp,
                lineHeight = 40.sp,
                color = Ink950
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.alpha(pulseAlpha)
            ) {
                LinearProgressIndicator(
                    color = Ink950,
                    trackColor = WarmBorder,
                    modifier = Modifier
                        .width(140.dp)
                        .height(2.dp)
                )
                Text(
                    text = statusText.uppercase(),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = Ink500,
                    letterSpacing = 1.sp
                )
            }
        }

        // Bottom Editorial Colophon
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 44.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "RESUME · LETTER · PIPELINE",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = Ink500
            )
            Text(
                text = "TYPESET ON ANDROID",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = Brand600
            )
        }
    }
}

/**
 * Calm, architectural paper skeleton shown while Firestore syncs documents.
 */
@Composable
fun KungaWorkspaceLoadingState(
    label: String = "Syncing workspace"
) {
    val transition = rememberInfiniteTransition(label = "skeletonShimmer")
    val shimmerAlpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmPaperBg)
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .widthIn(max = 640.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.uppercase(),
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                color = Ink500
            )
            Text(
                text = "A4 SHEET",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                color = Ink400
            )
        }

        HorizontalDivider(color = WarmBorder)

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, WarmBorder),
            modifier = Modifier
                .fillMaxWidth()
                .alpha(shimmerAlpha)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(22.dp)
                        .background(WarmInputSurface, RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp)
                        .background(WarmInputSurface, RoundedCornerShape(4.dp))
                )
                HorizontalDivider(color = WarmBorder)
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .background(WarmInputSurface, RoundedCornerShape(6.dp))
                    )
                }
            }
        }
    }
}

/**
 * Human-designer 3-step editorial onboarding inspired by Read.cv, Craft.do, and FlowCV.
 * Avoids generic AI marketing cards in favor of tactile paper folios, Instrument Serif
 * display typography, JetBrains Mono index numerals, and borderless architectural dividers.
 */
@Composable
fun KungaOnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmPaperBg)
            .testTag("kunga_onboarding_screen")
    ) {
        // Editorial Folio Header Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KungaCvLogo(
                        onDark = false,
                        markSize = 26.dp,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "0${pagerState.currentPage + 1} / 03",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = Ink500
                    )
                }

                TextButton(
                    onClick = onFinishOnboarding,
                    modifier = Modifier.testTag("onboarding_skip_button")
                ) {
                    Text(
                        text = "Skip intro",
                        style = MaterialTheme.typography.labelMedium,
                        color = Ink500
                    )
                }
            }
            HorizontalDivider(color = WarmBorder)
        }

        // Horizontal Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> OnboardingTypesetSlide()
                1 -> OnboardingEditorialProcessSlide()
                else -> OnboardingDossierSyncSlide()
            }
        }

        // Minimalist Bottom Dock
        Surface(
            color = WarmPaperBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                HorizontalDivider(color = WarmBorder)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Architectural Step Indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { idx ->
                            val selected = pagerState.currentPage == idx
                            val barWidth by animateDpAsState(
                                targetValue = if (selected) 28.dp else 10.dp,
                                label = "folioBarWidth"
                            )
                            Box(
                                modifier = Modifier
                                    .width(barWidth)
                                    .height(3.dp)
                                    .background(if (selected) Ink950 else WarmBorder)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (pagerState.currentPage < 2) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                onFinishOnboarding()
                            }
                        },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Ink950,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("onboarding_primary_cta")
                    ) {
                        Text(
                            text = if (pagerState.currentPage < 2) "Next" else "Enter Studio",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Slide 01: Interactive A4 Sheet Typesetting Demo (inspired by Craft.do & KungaCV's TemplatesSection).
 */
@Composable
private fun OnboardingTypesetSlide() {
    val featuredPresets = remember { KungaTemplateCatalog.presets.take(4) }
    var selectedPresetIndex by remember { mutableIntStateOf(0) }
    val activePreset = featuredPresets[selectedPresetIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp)
            .widthIn(max = 520.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "01 — LIVE TYPESETTING",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                color = Brand600
            )
            Text(
                text = "Write your story once. Restyle it in a tap.",
                style = MaterialTheme.typography.displayLarge,
                color = Ink950
            )
            Text(
                text = "More flexible than rigid templates, calmer than a word processor. Tap a style below to see your A4 sheet adapt in real time.",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink500
            )
        }

        // Tactile Swatch Selector Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            featuredPresets.forEachIndexed { index, preset ->
                val selected = selectedPresetIndex == index
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selected) Ink950 else Color.White,
                    border = BorderStroke(1.dp, if (selected) Ink950 else WarmBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedPresetIndex = index }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(Color(preset.accentHex), CircleShape)
                            )
                            Text(
                                text = "0${index + 1}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                color = if (selected) Color.White.copy(alpha = 0.65f) else Ink400
                            )
                        }
                        Text(
                            text = preset.name.split(" ").first(),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Color.White else Ink950,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Overlapping Architectural Paper Folio (Asymmetrical depth)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp)
        ) {
            // Subtle offset backing sheet
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = WarmInputSurface,
                border = BorderStroke(1.dp, WarmBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(225.dp)
                    .offset(x = 6.dp, y = 6.dp)
            ) {}

            AnimatedContent(
                targetState = activePreset,
                label = "folioPaperPreview"
            ) { preset ->
                val accent = Color(preset.accentHex)
                val headerFont: FontFamily = if (preset.nameFont == "Playfair Display") {
                    PlayfairDisplayFontFamily
                } else {
                    PoppinsFontFamily
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, WarmBorder),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(accent)
                        )
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Thoko Banda",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontFamily = headerFont,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Ink950
                                    )
                                    Text(
                                        text = "Product Designer · Lilongwe, Malawi",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = accent
                                    )
                                }
                                Text(
                                    text = "${preset.name.uppercase()} · ${if (preset.columns == 2) "2 COL" else "1 COL"}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    color = Ink400
                                )
                            }

                            HorizontalDivider(thickness = 1.dp, color = accent.copy(alpha = 0.5f))

                            if (preset.columns == 2) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.weight(0.62f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "EXPERIENCE",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.sp,
                                            color = accent
                                        )
                                        Text(
                                            text = "MobiWorks Africa",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Ink950
                                        )
                                        Text(
                                            text = "Redesigned mobile onboarding, lifting activation by 28%.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Ink700
                                        )
                                    }
                                    Column(
                                        modifier = Modifier.weight(0.38f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "DISCIPLINES",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.sp,
                                            color = accent
                                        )
                                        Text("Product Strategy", style = MaterialTheme.typography.bodySmall, color = Ink950)
                                        Text("Design Systems", style = MaterialTheme.typography.bodySmall, color = Ink950)
                                        Text("Field Research", style = MaterialTheme.typography.bodySmall, color = Ink950)
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Product Designer — MobiWorks Africa",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Ink950
                                        )
                                        Text(
                                            text = "2021 — PRESENT",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            color = Ink500
                                        )
                                    }
                                    Text(
                                        text = "Led cross-functional delivery of low-bandwidth mobile products across Southern Africa, improving activation by 28%.",
                                        style = MaterialTheme.typography.bodySmall,
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

/**
 * Slide 02: Borderless Architectural Index ("From blank page to job-winning CV")
 * Modeled on `HowItWorks.jsx` with Read.cv / Linear hairline dividers instead of boxy cards.
 */
@Composable
private fun OnboardingEditorialProcessSlide() {
    val items = listOf(
        Triple(
            "01",
            "Structured career profile",
            "Enter your work history, education, projects, and skill proficiency in clean, guided fields — never wrestle with margins or text boxes."
        ),
        Triple(
            "02",
            "12 editorial design systems",
            "Switch between single-column and split-sidebar layouts, serif or geometric headings, and 4 skill visualization styles."
        ),
        Triple(
            "03",
            "Pre-flight check & A4 export",
            "Verify contact completeness and export a crisp, ATS-readable A4 PDF document ready to send."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp)
            .widthIn(max = 520.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "02 — THE WORKFLOW",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                color = Brand600
            )
            Text(
                text = "Three quiet steps from blank page to offer.",
                style = MaterialTheme.typography.displayLarge,
                color = Ink950
            )
            Text(
                text = "Designed for clarity and speed on mobile.",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink500
            )
        }

        Column {
            HorizontalDivider(color = Ink950, thickness = 1.5.dp)
            items.forEach { (num, title, body) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = num,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Brand600,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Ink950
                        )
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink500
                        )
                    }
                }
                HorizontalDivider(color = WarmBorder)
            }
        }
    }
}

/**
 * Slide 03: Unified Career Dossier (Resume + Synced Letter + Application Ledger).
 */
@Composable
private fun OnboardingDossierSyncSlide() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp)
            .widthIn(max = 520.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "03 — UNIFIED DOSSIER",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                color = Brand600
            )
            Text(
                text = "Every application kept in one calm ledger.",
                style = MaterialTheme.typography.displayLarge,
                color = Ink950
            )
            Text(
                text = "Your cover letter automatically inherits your resume's masthead, contact details, and typography while your pipeline tracks every follow-up.",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink500
            )
        }

        // Tactile Ledger Preview Sheet
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(1.dp, WarmBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "APPLICATION LEDGER",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = Ink500
                    )
                    Text(
                        text = "SYNCED WITH CV",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = EmeraldSuccess
                    )
                }

                HorizontalDivider(color = WarmBorder)

                listOf(
                    Triple("MobiWorks Africa", "Senior Product Designer", "INTERVIEWING"),
                    Triple("PayChangu", "Lead Systems Designer", "APPLIED"),
                    Triple("Cedar Labs", "Design Consultant", "OFFER")
                ).forEachIndexed { idx, (company, role, stage) ->
                    if (idx > 0) HorizontalDivider(color = WarmBorder.copy(alpha = 0.6f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = role,
                                style = MaterialTheme.typography.titleSmall,
                                color = Ink950
                            )
                            Text(
                                text = company,
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink500
                            )
                        }
                        Text(
                            text = stage,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = if (stage == "OFFER") EmeraldSuccess else Brand600
                        )
                    }
                }
            }
        }

        Text(
            text = "\"A resume builder that treats your career like editorial publishing — not a form filler.\"",
            fontFamily = InstrumentSerifFontFamily,
            fontStyle = FontStyle.Italic,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            color = Ink700
        )
    }
}
