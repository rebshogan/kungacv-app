package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ai.GeminiCareerService
import com.example.auth.authStateFlow
import com.example.auth.configureAppCheck
import com.example.auth.signOutUser
import com.example.data.repository.KungaCvRepository
import com.example.ui.components.KungaCvLogo
import com.example.ui.screens.AuthGateScreen
import com.example.ui.screens.CareerCoachChatScreen
import com.example.ui.screens.CoverLetterStudioScreen
import com.example.ui.screens.CvStudioScreen
import com.example.ui.screens.KungaLaunchScreen
import com.example.ui.screens.KungaOnboardingScreen
import com.example.ui.screens.LivePreviewScreen
import com.example.ui.theme.Brand600
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Ink400
import com.example.ui.theme.Ink500
import com.example.ui.theme.Ink950
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.KungaCVTheme
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmPaperBg
import com.example.viewmodel.DataUiState
import com.example.viewmodel.KungaCvViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PREFS_NAME = "kunga_cv_onboarding_prefs"
private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed_v3"

private enum class KungaDestination(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    CV_STUDIO("Dossier", Icons.AutoMirrored.Filled.Article, Icons.AutoMirrored.Outlined.Article, "nav_cv_studio"),
    PREVIEW_PDF("A4 Sheet", Icons.Filled.Description, Icons.Outlined.Description, "nav_preview_pdf"),
    COVER_LETTERS("Letter", Icons.Filled.Mail, Icons.Outlined.MailOutline, "nav_cover_letters"),
    AI_COACH("Pipeline", Icons.Filled.Work, Icons.Outlined.WorkOutline, "nav_ai_coach")
}

private enum class RootEntryStage {
    SPLASH,
    ONBOARDING,
    AUTH_OR_WORKSPACE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase calls
        configureAppCheck(this, intent)

        // STEP 2: Enable edge-to-edge layout
        enableEdgeToEdge()

        setContent {
            KungaCVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = WarmPaperBg
                ) {
                    KungaCvRootNavigation()
                }
            }
        }
    }
}

@Composable
fun KungaCvRootNavigation(
    auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var hasCompletedOnboarding by rememberSaveable {
        mutableStateOf(prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false))
    }
    var isBooting by rememberSaveable { mutableStateOf(true) }
    var showTourOverlay by rememberSaveable { mutableStateOf(false) }
    var isGuestSession by rememberSaveable { mutableStateOf(false) }

    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    LaunchedEffect(Unit) {
        delay(650L)
        isBooting = false
    }

    val stage = when {
        isBooting -> RootEntryStage.SPLASH
        showTourOverlay || !hasCompletedOnboarding -> RootEntryStage.ONBOARDING
        else -> RootEntryStage.AUTH_OR_WORKSPACE
    }

    BackHandler(enabled = showTourOverlay) {
        showTourOverlay = false
    }

    Crossfade(
        targetState = stage,
        animationSpec = tween(durationMillis = 260),
        label = "rootEntryCrossfade"
    ) { activeStage ->
        when (activeStage) {
            RootEntryStage.SPLASH -> {
                KungaLaunchScreen(statusText = "Preparing your workspace")
            }

            RootEntryStage.ONBOARDING -> {
                KungaOnboardingScreen(
                    onFinishOnboarding = {
                        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
                        hasCompletedOnboarding = true
                        showTourOverlay = false
                    }
                )
            }

            RootEntryStage.AUTH_OR_WORKSPACE -> {
                val user = currentUser
                if (user == null && !isGuestSession) {
                    AuthGateScreen(
                        onAuthSuccess = { /* AuthStateListener updates currentUser */ },
                        onContinueAsGuest = { isGuestSession = true },
                        onReplayTour = { showTourOverlay = true }
                    )
                } else {
                    AuthenticatedWorkspace(
                        currentUserId = user?.uid ?: "guest_demo_user",
                        userDisplayName = user?.displayName ?: "Thoko Ndlovu",
                        userEmail = user?.email ?: "thoko.career@kungacv.design",
                        isGuest = user == null,
                        onOpenProductTour = { showTourOverlay = true },
                        onSignOut = { isGuestSession = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedWorkspace(
    currentUserId: String,
    userDisplayName: String,
    userEmail: String,
    isGuest: Boolean = false,
    onOpenProductTour: () -> Unit,
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: KungaCvViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                KungaCvViewModel(
                    repository = KungaCvRepository(db),
                    geminiService = GeminiCareerService(),
                    currentUserId = currentUserId,
                    userDisplayName = userDisplayName,
                    userEmail = userEmail
                )
            }
        }
    )

    val resumesState by viewModel.resumesState.collectAsStateWithLifecycle()
    val coverLettersState by viewModel.coverLettersState.collectAsStateWithLifecycle()
    val chatMessagesState by viewModel.chatMessagesState.collectAsStateWithLifecycle()
    val selectedResumeId by viewModel.selectedResumeId.collectAsStateWithLifecycle()
    val selectedModelTier by viewModel.selectedModelTier.collectAsStateWithLifecycle()
    val aiTaskState by viewModel.aiTaskState.collectAsStateWithLifecycle()
    val chatStreamingState by viewModel.chatStreamingState.collectAsStateWithLifecycle()
    val coverLetterDraft by viewModel.coverLetterDraft.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.bannerMessage.collectAsStateWithLifecycle()
    val saveStatusLabel by viewModel.saveStatusLabel.collectAsStateWithLifecycle()

    var currentTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val currentDestination = KungaDestination.entries[currentTabIndex]

    BackHandler(enabled = currentTabIndex != 0) {
        currentTabIndex = 0
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(bannerMessage) {
        val msg = bannerMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBannerMessage()
        }
    }

    val activeResume = remember(resumesState, selectedResumeId) {
        val list = (resumesState as? DataUiState.Success)?.data.orEmpty()
        list.firstOrNull { it.id == selectedResumeId } ?: list.firstOrNull()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 700.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = WarmPaperBg,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                // Minimalist Editorial Folio Header (Read.cv / Craft.do style on Warm Paper)
                Surface(
                    color = WarmPaperBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
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
                                if (activeResume != null) {
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
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isGuest) {
                                    Surface(
                                        shape = CircleShape,
                                        color = WarmPaperBg,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                                        modifier = Modifier
                                            .clickable {
                                                val activity = context as? androidx.activity.ComponentActivity
                                                if (activity != null) {
                                                    com.example.auth.onGoogleSignInClicked(
                                                        context = activity,
                                                        credentialManager = credentialManager,
                                                        onAuthSuccess = {},
                                                        onAuthError = { err: String ->
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(err)
                                                            }
                                                        },
                                                        scope = scope
                                                    )
                                                }
                                            }
                                            .testTag("connect_google_button")
                                    ) {
                                        Text(
                                            text = "GUEST · LINK GOOGLE",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Brand600,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = onOpenProductTour,
                                    modifier = Modifier.testTag("open_onboarding_tour_button")
                                ) {
                                    Text(
                                        text = "Guide",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = Ink500
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (isGuest) {
                                            onSignOut()
                                        } else {
                                            signOutUser(
                                                credentialManager = credentialManager,
                                                onSignOutComplete = onSignOut,
                                                scope = scope
                                            )
                                        }
                                    },
                                    modifier = Modifier.testTag("sign_out_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = "Sign out",
                                        tint = Ink950,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = WarmBorder)
                    }
                }
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    // Sleek Architectural Bottom Dock with 2.dp Top Active Rule (Linear / Read.cv style)
                    Surface(
                        color = Color.White,
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
                                    .height(60.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                KungaDestination.entries.forEachIndexed { idx, dest ->
                                    val selected = currentTabIndex == idx
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable { currentTabIndex = idx }
                                            .testTag(dest.tag),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Top 2.dp Ink Indicator Rule
                                        Box(
                                            modifier = Modifier
                                                .width(32.dp)
                                                .height(2.dp)
                                                .background(if (selected) Ink950 else Color.Transparent)
                                        )
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = dest.label,
                                                tint = if (selected) Ink950 else Ink400,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = dest.label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 11.sp,
                                                    letterSpacing = 0.sp
                                                ),
                                                color = if (selected) Ink950 else Ink500
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    NavigationRail(
                        containerColor = Color.White,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.size(12.dp))
                        KungaDestination.entries.forEachIndexed { idx, dest ->
                            val selected = currentTabIndex == idx
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTabIndex = idx },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag(dest.tag)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    when (currentDestination) {
                        KungaDestination.CV_STUDIO -> CvStudioScreen(
                            resumesState = resumesState,
                            selectedResumeId = selectedResumeId,
                            aiTaskState = aiTaskState,
                            viewModel = viewModel,
                            onNavigateToPreview = {
                                currentTabIndex = KungaDestination.PREVIEW_PDF.ordinal
                            },
                            onNavigateToCoach = {
                                currentTabIndex = KungaDestination.AI_COACH.ordinal
                            }
                        )

                        KungaDestination.PREVIEW_PDF -> LivePreviewScreen(
                            activeResume = activeResume,
                            viewModel = viewModel
                        )

                        KungaDestination.COVER_LETTERS -> CoverLetterStudioScreen(
                            activeResume = activeResume,
                            coverLettersState = coverLettersState,
                            coverLetterDraft = coverLetterDraft,
                            aiTaskState = aiTaskState,
                            viewModel = viewModel
                        )

                        KungaDestination.AI_COACH -> CareerCoachChatScreen(
                            activeResume = activeResume,
                            chatMessagesState = chatMessagesState,
                            selectedModelTier = selectedModelTier,
                            chatStreamingState = chatStreamingState,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
