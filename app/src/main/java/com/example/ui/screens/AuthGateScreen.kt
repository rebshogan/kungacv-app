package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.auth.attemptAutoSignIn
import com.example.auth.onGoogleSignInClicked
import com.example.ui.components.KungaCvLogo
import com.example.ui.theme.Brand600
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.CrimsonErrorBg
import com.example.ui.theme.CrimsonErrorBorder
import com.example.ui.theme.Ink400
import com.example.ui.theme.Ink500
import com.example.ui.theme.Ink950
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmPaperBg

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Editorial Sign-In Screen inspired by Read.cv, Linear, and `vend-pixel/kungacv/app/login/page.jsx`.
 * Uses warm paper (#FAF9F5), architectural hairline dividers, Instrument Serif display type,
 * and a high-contrast Ink-950 primary action button.
 */
@Composable
fun AuthGateScreen(
    onAuthSuccess: () -> Unit = {},
    onContinueAsGuest: () -> Unit = {},
    onReplayTour: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val scope = rememberCoroutineScope()

    var isSigningIn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val hostActivity = activity ?: return@LaunchedEffect
        attemptAutoSignIn(
            context = hostActivity,
            credentialManager = credentialManager,
            onAuthSuccess = { onAuthSuccess() },
            onUnauthenticated = {},
            scope = scope
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmPaperBg)
    ) {
        // Minimalist Folio Header Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                KungaCvLogo(
                    onDark = false,
                    markSize = 26.dp,
                    fontSize = 17.sp
                )
                TextButton(
                    onClick = onReplayTour,
                    modifier = Modifier.testTag("replay_tour_from_auth_button")
                ) {
                    Text(
                        text = "Studio guide",
                        style = MaterialTheme.typography.labelMedium,
                        color = Ink500
                    )
                }
            }
            HorizontalDivider(color = WarmBorder)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp, vertical = 28.dp)
                .widthIn(max = 460.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "MEMBER ACCESS",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = Brand600
                    )
                    Text(
                        text = "Your career dossier, kept in sync.",
                        style = MaterialTheme.typography.displayLarge,
                        color = Ink950
                    )
                    Text(
                        text = "Sign in with Google to sync across devices, or explore directly in Guest Demo mode.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ink500
                    )
                }

                // Architectural Specification Table (instead of generic AI pill badges)
                Column {
                    HorizontalDivider(color = Ink950, thickness = 1.dp)
                    listOf(
                        "01 / TEMPLATES" to "12 customizable A4 layouts",
                        "02 / DOCUMENTS" to "Resume + synced cover letter",
                        "03 / EXPORT" to "Instant ATS-ready PDF sheet"
                    ).forEach { (key, spec) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = key,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = Ink500
                            )
                            Text(
                                text = spec,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ink950
                            )
                        }
                        HorizontalDivider(color = WarmBorder)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (errorMessage != null) {
                    Surface(
                        color = CrimsonErrorBg,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CrimsonErrorBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_error_banner")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Authentication notice",
                                    tint = CrimsonError,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = errorMessage.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CrimsonError
                                    )
                                    TextButton(
                                        onClick = onContinueAsGuest,
                                        modifier = Modifier.testTag("error_continue_guest_button")
                                    ) {
                                        Text(
                                            text = "Open Demo Workspace →",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = CrimsonError
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        val hostActivity = activity
                        if (hostActivity == null) {
                            errorMessage = "Unable to launch Google Sign-In from this context."
                            return@Button
                        }
                        errorMessage = null
                        isSigningIn = true
                        onGoogleSignInClicked(
                            context = hostActivity,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isSigningIn = false
                                onAuthSuccess()
                            },
                            onAuthError = { err: String ->
                                isSigningIn = false
                                errorMessage = err
                            },
                            scope = scope,
                            onAuthCancelled = {
                                isSigningIn = false
                            }
                        )
                    },
                    enabled = !isSigningIn,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Ink950,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("google_sign_in_button")
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Opening workspace…",
                            style = MaterialTheme.typography.labelLarge
                        )
                    } else {
                        Text(
                            text = "Continue with Google",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onContinueAsGuest,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, WarmBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Ink950
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("continue_as_guest_button")
                ) {
                    Text(
                        text = "Explore in Demo Mode (Guest)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Ink950
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "FREE CAREER STUDIO",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = Ink400
                    )
                    Text(
                        text = "NO CREDIT CARD NEEDED",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = Ink400
                    )
                }
            }
        }
    }
}
