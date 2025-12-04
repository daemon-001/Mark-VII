

package com.daemon.markvii

/**
 * @author Nitesh
 */

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.with
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.daemon.markvii.data.Chat
import com.daemon.markvii.data.ChatData
import com.daemon.markvii.data.ModelInfo
import com.daemon.markvii.data.FirebaseConfigManager
import com.daemon.markvii.ui.theme.MarkVIITheme
import com.daemon.markvii.ui.theme.LocalAppColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.delay
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.rememberUpdatedState
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import com.daemon.markvii.utils.PdfGenerator
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share


import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance

class MainActivity : AppCompatActivity() {

    private val uriState = MutableStateFlow("")
    private val voiceInputState = MutableStateFlow("")
    private val isSigningInState = MutableStateFlow(false)
    
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    private val isTtsSpeakingState = MutableStateFlow(false)
    
    // Custom SpeechRecognizer
    private var speechRecognizer: SpeechRecognizer? = null
    private val isListeningState = MutableStateFlow(false)
    private val voicePitchState = MutableStateFlow(0f) // RMS dB value for pitch animation
    private val RECORD_AUDIO_PERMISSION_CODE = 101

    private val imagePicker =
        registerForActivityResult<PickVisualMediaRequest, Uri?>(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let {
                uriState.update { uri.toString() }
            }
        }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == com.daemon.markvii.data.AuthManager.RC_SIGN_IN) {
            lifecycleScope.launch {
                val result = com.daemon.markvii.data.AuthManager.handleSignInResult(data)
                result.onSuccess {
                    // Sign-in successful, state will update automatically
                    isSigningInState.value = false
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Sign in successful!", Toast.LENGTH_SHORT).show()
                    }
                }.onFailure { error ->
                    // Sign-in failed, reset loading state
                    isSigningInState.value = false
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Sign in failed: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Thread.sleep(1000) // splash screen delay
        installSplashScreen()  // splash screen ui
        
        // Initialize ChatHistoryManager
        com.daemon.markvii.data.ChatHistoryManager.init(applicationContext)
        
        // Initialize ThemePreferences
        com.daemon.markvii.data.ThemePreferences.init(applicationContext)
        
        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                // Language will be set dynamically based on detected content
                
                // Add listener to track speaking state
                textToSpeech?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isTtsSpeakingState.update { true }
                    }
                    
                    override fun onDone(utteranceId: String?) {
                        isTtsSpeakingState.update { false }
                    }
                    
                    override fun onError(utteranceId: String?) {
                        isTtsSpeakingState.update { false }
                    }
                })
            }
        }
        
        // Initialize SpeechRecognizer
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListeningState.update { true }
                    }
                    
                    override fun onBeginningOfSpeech() {
                        // User started speaking
                    }
                    
                    override fun onRmsChanged(rmsdB: Float) {
                        // Update pitch state for animation (normalize to 0-1 range)
                        // Typical range is -2 to 10 dB, normalize to 0-1
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        voicePitchState.update { normalized }
                    }
                    
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    
                    override fun onEndOfSpeech() {
                        isListeningState.update { false }
                        voicePitchState.update { 0f }
                    }
                    
                    override fun onError(error: Int) {
                        isListeningState.update { false }
                        voicePitchState.update { 0f }
                        
                        val errorMessage = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                            SpeechRecognizer.ERROR_SERVER -> "Server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                            else -> "Recognition error"
                        }
                        
                        // Only show error toast for non-timeout errors
                        if (error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT && 
                            error != SpeechRecognizer.ERROR_NO_MATCH) {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    
                    override fun onResults(results: Bundle?) {
                        isListeningState.update { false }
                        voicePitchState.update { 0f }
                        
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            voiceInputState.update { matches[0] }
                        }
                    }
                    
                    override fun onPartialResults(partialResults: Bundle?) {
                        // Optional: show partial results in real-time
                    }
                    
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
        
        setContent {
            // Observe theme changes
            val currentTheme by com.daemon.markvii.data.ThemePreferences.currentTheme.collectAsState()
            val darkTheme = when (currentTheme) {
                com.daemon.markvii.data.AppTheme.LIGHT -> false
                com.daemon.markvii.data.AppTheme.DARK -> true
                com.daemon.markvii.data.AppTheme.SYSTEM_DEFAULT -> isSystemInDarkTheme()
            }
            
            // Set window colors based on theme
            val backgroundColor = if (darkTheme) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            val statusBarColor = if (darkTheme) android.graphics.Color.parseColor("#1A1A2E") else android.graphics.Color.parseColor("#F5F5F5")
            
            SideEffect {
                window.decorView.setBackgroundColor(backgroundColor)
                window.statusBarColor = statusBarColor
                window.navigationBarColor = backgroundColor
            }

            MarkVIITheme(darkTheme = darkTheme) {
                var opentimes by remember { mutableIntStateOf(0) }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                )

                {
//                    for switching between from home screen to infoTab
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home", builder = {
                        composable("home",){
                            opentimes++
                            
                            // ViewModel needs to be at this scope to be accessible by both topBar and content
                            val chaViewModel = viewModel<ChatViewModel>()
                            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                            val coroutineScope = rememberCoroutineScope()
                            var showSettings by remember { mutableStateOf(false) } // Settings screen state
                            val isSigningIn by isSigningInState.collectAsState() // Sign-in loading state from Activity
                            val chatState by chaViewModel.chatState.collectAsState()
                            val appColors = LocalAppColors.current // Get theme colors
                            
                            Box(modifier = Modifier.fillMaxSize()) {
                            ModalNavigationDrawer(
                                drawerState = drawerState,
                                drawerContent = {
                                    ModalDrawerSheet {
                                        DrawerContent(
                                            chatViewModel = chaViewModel,
                                            onDismiss = {
                                                coroutineScope.launch {
                                                    drawerState.close()
                                                }
                                            },
                                            onSettingsClick = {
                                                coroutineScope.launch {
                                                    drawerState.close()
                                                }
                                                showSettings = true
                                            },
                                            onSigningInChanged = { signing ->
                                                isSigningInState.value = signing
                                            }
                                        )
                                    }
                                },
                                gesturesEnabled = true
                            ) {
                            Scaffold(
//                                top bar items
                                topBar = {
                                    var showClearDialog by remember { mutableStateOf(false) }
                                    // Optimize: Only collect user state, not entire chat state to prevent lag during streaming
                                    val currentUser by com.daemon.markvii.data.AuthManager.currentUser.collectAsState()
                                    
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(appColors.topBarBackground)
                                        .height(64.dp)
                                        .padding(horizontal = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Left side: Drawer Icon / Profile Picture + Title
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            // Drawer Icon / Profile Picture
                                            IconButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        if (drawerState.isOpen) {
                                                            drawerState.close()
                                                        } else {
                                                            drawerState.open()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                val user = currentUser
                                                if (user?.photoUrl != null) {
                                                    // User Profile Picture
                                                    androidx.compose.foundation.Image(
                                                        painter = rememberAsyncImagePainter(
                                                            model = ImageRequest.Builder(LocalContext.current)
                                                                .data( user.photoUrl)
                                                                .crossfade(true)
                                                                .build()
                                                        ),
                                                        contentDescription = "Profile",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .border(1.dp, appColors.accent, CircleShape)
                                                    )
                                                } else {
                                                    // Standard Hamburger Menu
                                                    Icon(
                                                        imageVector = Icons.Rounded.Menu,
                                                        contentDescription = "Menu",
                                                        tint = appColors.accent,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            // Title with custom font
                                            Text(
                                                text = "MARK",
                                                fontSize = 24.sp,
                                                fontFamily = FontFamily(Font(R.font.typographica)),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                letterSpacing = 1.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "VII",
                                                fontSize = 24.sp,
                                                fontFamily = FontFamily(Font(R.font.typographica)),
                                                color = appColors.accent,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                        
                                        // Right side: Action icons
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Info icon (Lottie animation)
                                            val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.info_card))
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clickable { navController.navigate("info_screen") },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                LottieAnimation(
                                                    composition = composition,
                                                    modifier = Modifier.size(48.dp),
                                                    iterations = LottieConstants.IterateForever
                                                )
                                            }
                                        }
                                    }
                                }
                            },

//                                    Show welcome guide once when app opens (no API call)
                                bottomBar = {
                                if(opentimes==1){
                                    chaViewModel.showWelcomeGuide()
                                }
                            }
                            ) {
                                ChatScreen(paddingValues = it)  // starting chat screen ui
                            }
                            } // End ModalNavigationDrawer
                            
                            // Settings screen overlay
                            if (showSettings) {
                                // Handle back button when settings is open
                                androidx.activity.compose.BackHandler {
                                    showSettings = false
                                }
                                
                                SettingsScreen(
                                    onBackClick = { showSettings = false },
                                    onSignOut = {
                                        chaViewModel.onEvent(ChatUiEvent.SignOut)
                                        showSettings = false
                                    },
                                    onThemeChanged = { /* Theme change is handled via StateFlow */ },
                                    isUserAuthenticated = chatState.currentUser != null
                                )
                            }
                            
                            // Loading overlay during sign-in - covers entire app
                            if (isSigningIn) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .clickable(enabled = false) { },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        androidx.compose.material3.CircularProgressIndicator(
                                            modifier = Modifier.size(56.dp),
                                            color = appColors.accent,
                                            strokeWidth = 5.dp
                                        )
                                        Text(
                                            text = "Signing in with Google...",
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            } // Box

                        }
                        composable("info_screen") {
                            InfoSetting() // starting infoTab ui (About section)
                        }

                    })


                }

            }
        }
    }
    
    override fun onDestroy() {
        // Shutdown TextToSpeech to free resources
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        isTtsSpeakingState.update { false }
        
        // Cleanup SpeechRecognizer
        speechRecognizer?.destroy()
        isListeningState.update { false }
        voicePitchState.update { 0f }
        
        super.onDestroy()
    }

    @Composable
    fun ErrorDialog(
        errorTitle: String,
        errorMessage: String,
        errorDetails: String,
        isRetryable: Boolean,
        onDismiss: () -> Unit,
        onRetry: (() -> Unit)? = null
    ) {
        val appColors = LocalAppColors.current
        var showDetails by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(20.dp),
            title = { 
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pin),
                            contentDescription = "Error",
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = errorTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B6B)
                        )
                    }
                    Text(
                        text = errorMessage,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 32.dp)
                    )
                }
            },
            text = {
                Column {
                    if (showDetails) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(appColors.surfaceTertiary)
                                .border(
                                    width = 1.dp,
                                    color = appColors.divider,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            val scrollState = rememberScrollState()
                            SelectionContainer {
                                Text(
                                    text = errorDetails,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 13.sp,
                                    color = appColors.textSecondary,
                                    lineHeight = 18.sp,
                                    modifier = Modifier
                                        .verticalScroll(scrollState)
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isRetryable && onRetry != null) {
                            onRetry()
                        }
                        onDismiss()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = if (isRetryable) appColors.accent else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = if (isRetryable) "Retry" else "OK",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDetails = !showDetails },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF8E8E93)
                    )
                ) {
                    Text(
                        text = if (showDetails) "Hide Details" else "Details",
                        fontSize = 15.sp
                    )
                }
            }
        )
    }

//    chat home screen ui starts here
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    @Composable
    fun ChatScreen(paddingValues: PaddingValues) {
        val chaViewModel = viewModel<ChatViewModel>()
        val chatState = chaViewModel.chatState.collectAsState().value
        val bitmap = getSelectedBitmap()
        val voiceInput = voiceInputState.collectAsState().value
        val context = LocalContext.current
        val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current
        
        // Track haptic feedback based on chunk arrivals
        val lastHapticTrigger = remember { mutableStateOf(0L) }
        val hapticTrigger = chatState.hapticTrigger
        
        // Trigger haptic when new chunk arrives
        LaunchedEffect(hapticTrigger) {
            if (hapticTrigger > 0L && hapticTrigger != lastHapticTrigger.value) {
                hapticFeedback.performHapticFeedback(
                    androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove
                )
                lastHapticTrigger.value = hapticTrigger
            }
        }
        
        // State for loading free models from OpenRouter and Gemini
        var isLoadingModels by remember { mutableStateOf(ChatData.cachedFreeModels.isEmpty()) }
        var freeModels by remember { mutableStateOf<List<ModelInfo>>(ChatData.cachedFreeModels) }
        var modelsLoadError by remember { mutableStateOf<String?>(null) }
        val appColors = LocalAppColors.current // Get theme colors for outer scope
        
        // Observe Firebase API keys and Gemini models
        val firebaseApiKey by FirebaseConfigManager.apiKey.collectAsState()
        val geminiApiKey by FirebaseConfigManager.geminiApiKey.collectAsState()
        val firebaseGeminiModels by FirebaseConfigManager.geminiModels.collectAsState()
        
        // Convert Firebase Gemini models to ModelInfo
        val geminiModels = remember(firebaseGeminiModels) {
            firebaseGeminiModels.map { firebaseModel ->
                ModelInfo(
                    displayName = firebaseModel.displayName,
                    apiModel = firebaseModel.apiModel,
                    isAvailable = firebaseModel.isAvailable
                )
            }
        }
        
        // Initialize Firebase to get API keys and exception models
        LaunchedEffect(Unit) {
            FirebaseConfigManager.initialize()
        }
        
        // Update API keys when Firebase data changes
        LaunchedEffect(firebaseApiKey) {
            if (firebaseApiKey.isNotEmpty()) {
                ChatData.updateApiKey(firebaseApiKey)
            }
        }
        
        LaunchedEffect(geminiApiKey) {
            if (geminiApiKey.isNotEmpty()) {
                com.daemon.markvii.data.GeminiClient.updateApiKey(geminiApiKey)
            }
        }
        
        // Load free models from OpenRouter AFTER Firebase initializes
        // Observe exception models to trigger reload when they change
        val exceptionModels by FirebaseConfigManager.exceptionModels.collectAsState()
        
        LaunchedEffect(firebaseApiKey, exceptionModels) {
            if (firebaseApiKey.isNotEmpty()) {
                // Build a cache key that changes when either API key or exception models change
                val modelsCacheKey = "$firebaseApiKey|${exceptionModels.hashCode()}"

                // If we already have cached models with the same cache key, reuse them
                if (ChatData.cachedFreeModels.isNotEmpty() && ChatData.cachedFreeModelsKey == modelsCacheKey) {
                    // Models already cached, use them directly without showing loading
                    freeModels = ChatData.cachedFreeModels
                    isLoadingModels = false
                } else {
                    // Only show loading if we need to fetch new models
                    isLoadingModels = true
                    try {
                        // Use the convenience method which caches the result in ChatData
                        freeModels = ChatData.getOrFetchFreeModels(modelsCacheKey)
                        if (freeModels.isEmpty()) {
                            modelsLoadError = "No free models available"
                        }
                    } catch (e: Exception) {
                        modelsLoadError = "Failed to load models: ${e.message}"
                    } finally {
                        isLoadingModels = false
                    }
                }
            }
        }
        
        // Show loading popup while models are loading
        if (isLoadingModels) {
            AlertDialog(
                onDismissRequest = { },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                title = null,
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(60.dp),
                            color = appColors.accent,
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "Loading free models...",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily(Font(R.font.typographica))
                        )
                    }
                },
                confirmButton = { }
            )
        }
        
        // Observe error state from ViewModel
        val currentError = chatState.error

        // Error dialog removed - errors now displayed as red text in chat
        /*
        currentError?.let { errorInfo ->
            ErrorDialog(
                errorTitle = errorInfo.title,
                errorMessage = errorInfo.mainMessage,
                errorDetails = errorInfo.fullDetails,
                isRetryable = errorInfo.isRetryable,
                onDismiss = { 
                    chaViewModel.clearError()
                },
                onRetry = if (errorInfo.isRetryable) {
                    {
                        errorInfo.lastPrompt?.let { prompt ->
                            chaViewModel.onEvent(
                                ChatUiEvent.RetryPrompt(
                                    prompt,
                                    errorInfo.lastBitmap
                                )
                            )
                        }
                    }
                } else null
            )
        }
        */
        
        // Model selector state for prompt box
        val isPromptDropDownExpanded = remember { mutableStateOf(false) }
        val promptItemPosition = remember { mutableStateOf(0) }
        
        // LazyList state for auto-scrolling
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        
        // Get current API provider from state
        val currentApiProvider = chatState.currentApiProvider
        
        // Switch models based on API provider
        val currentModels = when (currentApiProvider) {
            ApiProvider.OPENROUTER -> freeModels
            ApiProvider.GEMINI -> geminiModels
        }
        
        // Reset model selection when API provider changes
        LaunchedEffect(currentApiProvider) {
            promptItemPosition.value = 0
            if (currentModels.isNotEmpty()) {
                ChatData.selected_model = currentModels[0].apiModel
            }
        }
        
        // Set initial model when models load (only once)
        var hasSetInitialModel by remember { mutableStateOf(false) }
        LaunchedEffect(currentModels) {
            if (!hasSetInitialModel && currentModels.isNotEmpty() && promptItemPosition.value < currentModels.size) {
                ChatData.selected_model = currentModels[promptItemPosition.value].apiModel
                hasSetInitialModel = true
            }
        }

        // Update prompt when voice input is received
        LaunchedEffect(voiceInput) {
            if (voiceInput.isNotEmpty()) {
                chaViewModel.onEvent(ChatUiEvent.UpdatePrompt(voiceInput))
                voiceInputState.update { "" }
            }
        }

        // Auto-scroll to bottom when new message appears or when generating
        LaunchedEffect(chatState.chatList.size, chatState.isGeneratingResponse) {
            if (chatState.chatList.isNotEmpty()) {
                listState.animateScrollToItem(0)
            }
        }
        
        
        // Auto-scroll during streaming when content changes - REMOVED to prevent flickering
        // The reverseLayout = true configuration handles upward expansion naturally
        // val currentChat = chatState.chatList.firstOrNull()
        // LaunchedEffect(currentChat?.prompt) {
        //     if (currentChat != null && currentChat.isStreaming && !currentChat.isFromUser) {
        //         listState.animateScrollToItem(0)
        //     }
        // }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = paddingValues.calculateTopPadding())) {
//            Chat messages list - extends to bottom of screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Show centered prompt suggestions when chat is empty
                if (chatState.showPromptSuggestions && chatState.chatList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PromptSuggestionBubbles(
                            onSuggestionClick = { suggestion ->
                                chaViewModel.onEvent(ChatUiEvent.UpdatePrompt(suggestion))
                            }
                        )
                    }
                }
                
                // Pre-compute previous user chat map for performance
                val previousUserChatMap = remember(chatState.chatList) {
                    chatState.chatList.mapIndexedNotNull { index, chat ->
                        if (!chat.isFromUser) {
                            index to chatState.chatList
                                .drop(index + 1)
                                .firstOrNull { it.isFromUser }
                        } else null
                    }.toMap()
                }
                
                // Memoize model lists to prevent unnecessary recompositions
                val stableFreeModels = remember(freeModels) { freeModels }
                val stableGeminiModels = remember(geminiModels) { geminiModels }
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    state = listState,
                    reverseLayout = true,
                    contentPadding = PaddingValues(bottom = 140.dp) // Space for prompt box
                ) {
                    itemsIndexed(
                        items = chatState.chatList,
                        key = { _, chat -> chat.id },
                        contentType = { _, chat -> if (chat.isFromUser) "user" else "model" }
                    ) { index, chat ->
                        if (chat.isFromUser) {
                            UserChatItem(
                                prompt = chat.prompt, bitmap = chat.bitmap
                            )
                        } else {
                            // Use pre-computed previous user chat from map
                            val previousUserChat = previousUserChatMap[index]
                            
                            ModelChatItem(
                                response = chat.prompt,
                                userPrompt = previousUserChat?.prompt ?: "",
                                modelUsed = chat.modelUsed,
                                isStreaming = chat.isStreaming,
                                isError = chat.isError,
                                freeModels = stableFreeModels,
                                geminiModels = stableGeminiModels,
                                currentApiProvider = currentApiProvider,
                                hasImage = previousUserChat?.bitmap != null,
                                onRetry = { _ ->
                                    chaViewModel.onEvent(
                                        ChatUiEvent.RetryPrompt(
                                            previousUserChat?.prompt ?: "",
                                            previousUserChat?.bitmap
                                        )
                                    )
                                },
                                onApiSwitch = { provider ->
                                    chaViewModel.onEvent(
                                        ChatUiEvent.SwitchApiProvider(provider)
                                    )
                                }
                            )
                        }
                    }
                }
                
                // Fade effect at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )
            }

//            Prompt box - overlays at bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
//                ================ Picked Image Display with Pin Icon ================
                bitmap?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(appColors.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = appColors.divider,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Pin icon
                            Icon(
                                painter = painterResource(id = R.drawable.ic_pin),
                                contentDescription = "Pinned image",
                                tint = appColors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            
                            // Thumbnail image
                        Image(
                            modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                            contentDescription = "picked image",
                            contentScale = ContentScale.Crop,
                            bitmap = it.asImageBitmap()
                        )
                            
                            // Image text
                            Text(
                                text = "Image attached",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Remove button
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Remove image",
                                tint = appColors.textSecondary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        uriState.update { "" }
                                    }
                            )
                        }
                    }
                }

// ================ Enhanced Visible Input Box ================
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (appColors.textPrimary.luminance() > 0.5f) {
                                    // Dark theme - darker background for contrast
                                    appColors.surfaceTertiary
                                } else {
                                    // Light theme - use surface with slight tint
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                            .border(
                                width = 1.5.dp,
                                color = appColors.divider.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Column {
                            // Text input field at top (multiline, expands upward)
                TextField(
                    modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(0.dp)
                                    .heightIn(min = 40.dp, max = 150.dp),
                    value = chatState.prompt,
                                singleLine = false,
                                maxLines = 6,
                    onValueChange = {
                        chaViewModel.onEvent(ChatUiEvent.UpdatePrompt(it))
                    },
                    placeholder = {
                                    Text(
                                        text = "Ask Mark VII...",
                                        fontSize = 14.sp,
                                        color = appColors.textSecondary
                                    )
                                },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                ),
                    colors = TextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.onSurface,
                                    focusedPlaceholderColor = appColors.textSecondary,
                                    unfocusedPlaceholderColor = appColors.textSecondary
                                )
                            )
                            
                            // Bottom row: Icons + Model Selector + Mic + Send button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                
                                // Model selector or Pitch Waveform
                                val isListening by isListeningState.collectAsState()
                                val voicePitch by voicePitchState.collectAsState()
                                
                                // Animated transition between model selector and pitch graph
                                AnimatedVisibility(
                                    visible = !isListening,
                                    enter = fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)) + 
                                            scaleIn(initialScale = 0.9f, animationSpec = tween(200, easing = FastOutSlowInEasing)),
                                    exit = fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing)) + 
                                           scaleOut(targetScale = 0.9f, animationSpec = tween(150, easing = FastOutSlowInEasing)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = ripple(
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                )
                                            ) { 
                                                isPromptDropDownExpanded.value = true 
                                            }
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (currentModels.isNotEmpty() && promptItemPosition.value < currentModels.size) {
                                                currentModels[promptItemPosition.value].displayName
                                            } else {
                                                "Model Selector"
                                            },
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyMedium,
                                            lineHeight = 13.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Icon(
                                            painter = painterResource(id = R.drawable.drop_down_ic),
                                            contentDescription = "Select model",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    
                                    // Dropdown menu - Aesthetic design with animation
                                    val dropdownScale by androidx.compose.animation.core.animateFloatAsState(
                                        targetValue = if (isPromptDropDownExpanded.value) 1f else 0.95f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        ),
                                        label = "dropdown_scale"
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .width(280.dp)
                                            .graphicsLayer {
                                                scaleX = dropdownScale
                                                scaleY = dropdownScale
                                                alpha = if (isPromptDropDownExpanded.value) 1f else 0f
                                            }
                                            .shadow(
                                                elevation = 12.dp,
                                                shape = RoundedCornerShape(20.dp),
                                                ambientColor = Color.Black.copy(alpha = 0.5f),
                                                spotColor = Color.Black.copy(alpha = 0.5f)
                                            )
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(
                                                width = 1.dp,
                                                color = appColors.divider,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                    ) {
                                        DropdownMenu(
                                            expanded = isPromptDropDownExpanded.value,
                                            onDismissRequest = { isPromptDropDownExpanded.value = false },
                                            modifier = Modifier
                                                .width(280.dp)
                                                .heightIn(max = 450.dp)
                                                .background(MaterialTheme.colorScheme.surface),
                                            shape = RoundedCornerShape(20.dp),
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ) {
                                            // API Provider Switch at the top
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Gemini button (now on the left)
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(
                                                                if (currentApiProvider == ApiProvider.GEMINI)
                                                                    appColors.accent.copy(alpha = 0.2f)
                                                                else
                                                                    appColors.surfaceTertiary
                                                            )
                                                            .clickable {
                                                                chaViewModel.onEvent(
                                                                    ChatUiEvent.SwitchApiProvider(ApiProvider.GEMINI)
                                                                )
                                                            }
                                                            .padding(vertical = 8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "Gemini",
                                                            color = if (currentApiProvider == ApiProvider.GEMINI)
                                                                appColors.accent
                                                            else
                                                                appColors.textSecondary,
                                                            fontSize = 13.sp,
                                                            fontWeight = if (currentApiProvider == ApiProvider.GEMINI)
                                                                FontWeight.SemiBold
                                                            else
                                                                FontWeight.Normal
                                                        )
                                                    }
                                                    
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    
                                                    // OpenRouter button (now on the right)
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(
                                                                if (currentApiProvider == ApiProvider.OPENROUTER)
                                                                    appColors.accent.copy(alpha = 0.2f)
                                                                else
                                                                    appColors.surfaceTertiary
                                                            )
                                                            .clickable {
                                                                chaViewModel.onEvent(
                                                                    ChatUiEvent.SwitchApiProvider(ApiProvider.OPENROUTER)
                                                                )
                                                            }
                                                            .padding(vertical = 8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "OpenRouter",
                                                            color = if (currentApiProvider == ApiProvider.OPENROUTER)
                                                                appColors.accent
                                                            else
                                                                appColors.textSecondary,
                                                            fontSize = 13.sp,
                                                            fontWeight = if (currentApiProvider == ApiProvider.OPENROUTER)
                                                                FontWeight.SemiBold
                                                            else
                                                                FontWeight.Normal
                                                        )
                                                    }
                                                }
                                                
                                                // Divider
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 8.dp)
                                                        .height(1.dp)
                                                        .background(appColors.divider)
                                                )
                                            }
                                            
                                        if (currentModels.isEmpty()) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 16.dp, horizontal = 16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Text(
                                                    text = "No models available",
                                                    color = appColors.textSecondary,
                                                    fontSize = 14.sp,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                
                                                // Show reload button only for OpenRouter
                                                if (currentApiProvider == ApiProvider.OPENROUTER) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(appColors.accent.copy(alpha = 0.15f))
                                                            .clickable {
                                                                // Clear cache and reload models
                                                                ChatData.cachedFreeModels = emptyList()
                                                                ChatData.cachedFreeModelsKey = ""
                                                                isLoadingModels = true
                                                                coroutineScope.launch {
                                                                    try {
                                                                        val modelsCacheKey = "$firebaseApiKey|${exceptionModels.hashCode()}"
                                                                        freeModels = ChatData.getOrFetchFreeModels(modelsCacheKey)
                                                                        if (freeModels.isEmpty()) {
                                                                            modelsLoadError = "No free models available"
                                                                        } else {
                                                                            modelsLoadError = null
                                                                        }
                                                                    } catch (e: Exception) {
                                                                        modelsLoadError = "Failed to load models: ${e.message}"
                                                                    } finally {
                                                                        isLoadingModels = false
                                                                    }
                                                                }
                                                            }
                                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                                    ) {
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.Refresh,
                                                                contentDescription = "Reload models",
                                                                tint = appColors.accent,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                            Text(
                                                                text = "Reload Models",
                                                                color = appColors.accent,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            currentModels.forEachIndexed { index, model ->
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    DropdownMenuItem(
                                                        text = {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    text = model.displayName,
                                                                    color = if (promptItemPosition.value == index) 
                                                                        appColors.accent 
                                                                    else 
                                                                        MaterialTheme.colorScheme.onSurface,
                                                                    fontSize = 15.sp,
                                                                    style = MaterialTheme.typography.bodyMedium,
                                                                    fontWeight = if (promptItemPosition.value == index) 
                                                                        FontWeight.SemiBold 
                                                                    else 
                                                                        FontWeight.Normal
                                                                )
                                                            }
                                                        },
                                                        onClick = {
                                                            isPromptDropDownExpanded.value = false
                                                            promptItemPosition.value = index
                                                            ChatData.selected_model = model.apiModel
                                                            
                                                            if (!model.isAvailable) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Model temporarily unavailable",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(
                                                                if (promptItemPosition.value == index)
                                                                    appColors.accent.copy(alpha = 0.1f)
                                                                else
                                                                    appColors.surfaceVariant
                                                            ),
                                                        contentPadding = PaddingValues(
                                                            horizontal = 12.dp,
                                                            vertical = 12.dp
                                                        ),
                                                        colors = MenuDefaults.itemColors(
                                                            textColor = MaterialTheme.colorScheme.onSurface,
                                                            leadingIconColor = MaterialTheme.colorScheme.onSurface,
                                                            trailingIconColor = MaterialTheme.colorScheme.onSurface,
                                                            disabledTextColor = appColors.textSecondary,
                                                            disabledLeadingIconColor = appColors.textSecondary,
                                                            disabledTrailingIconColor = appColors.textSecondary
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                        }
                                    }
                                }
                                }
                                
                                // Pitch Waveform - visible when listening
                                AnimatedVisibility(
                                    visible = isListening,
                                    enter = fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)) + 
                                            scaleIn(initialScale = 0.9f, animationSpec = tween(200, easing = FastOutSlowInEasing)),
                                    exit = fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing)) + 
                                           scaleOut(targetScale = 0.9f, animationSpec = tween(150, easing = FastOutSlowInEasing)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Moving waveform timeline (like audio recorders)
                                    var waveformData by remember { mutableStateOf(List(60) { 0.2f }) }
                                    
                                    // Update waveform data at constant speed
                                    LaunchedEffect(Unit) {
                                        while (isListening) {
                                            // Add new pitch value at the end and remove first
                                            waveformData = waveformData.drop(1) + voicePitch.coerceIn(0.15f, 1f)
                                            delay(30) // Constant 30ms interval for smooth, consistent movement
                                        }
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.Transparent)
                                            .padding(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        // Center line
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .align(Alignment.Center)
                                                .background(appColors.divider.copy(alpha = 0.3f))
                                        )
                                        
                                        // Waveform bars moving from right to left
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.spacedBy(1.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            waveformData.forEachIndexed { index, amplitude ->
                                                // Fade out older bars (left side)
                                                val alpha = (index.toFloat() / waveformData.size).coerceIn(0.3f, 1f)
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight(amplitude)
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .background(appColors.textPrimary.copy(alpha = alpha))
                                                )
                                            }
                                        }
                                        
                                        // Current position indicator (red line on the right)
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .fillMaxHeight()
                                                .align(Alignment.CenterEnd)
                                                .background(appColors.error.copy(alpha = 0.8f))
                                        )
                                    }
                                }
                                
                                // Image picker icon - only visible when Gemini is selected and NOT listening
                                if (currentApiProvider == ApiProvider.GEMINI && !isListening) {
                                    IconButton(
                                        onClick = {
                                            imagePicker.launch(
                                                PickVisualMediaRequest(
                                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                                )
                                            )
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = "Add image",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                
                                // Microphone icon with circular background when listening
                                IconButton(
                                    onClick = {
                                        if (isListening) {
                                            // Stop listening
                                            speechRecognizer?.stopListening()
                                            isListeningState.update { false }
                                            voicePitchState.update { 0f }
                                        } else {
                                            // Check permission
                                            if (ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.RECORD_AUDIO
                                                ) != PackageManager.PERMISSION_GRANTED
                                            ) {
                                                ActivityCompat.requestPermissions(
                                                    this@MainActivity,
                                                    arrayOf(Manifest.permission.RECORD_AUDIO),
                                                    RECORD_AUDIO_PERMISSION_CODE
                                                )
                                            } else {
                                                // Start listening
                                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                    putExtra(
                                                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                                    )
                                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                                                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                                                }
                                                speechRecognizer?.startListening(intent)
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Circular background when listening
                                        if (isListening) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(appColors.accent.copy(alpha = 0.2f))
                                            )
                                        }
                                        
                                        Icon(
                                            imageVector = Icons.Rounded.Mic,
                                            contentDescription = if (isListening) "Stop listening" else "Voice input",
                                            tint = if (isListening) appColors.accent else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                
                                // Send/Stop button with scale animation
                                val interactionSource = remember { MutableInteractionSource() }
                                val isButtonEnabled = chatState.isGeneratingResponse || chatState.prompt.isNotEmpty() || bitmap != null
                                val buttonScale by androidx.compose.animation.core.animateFloatAsState(
                                    targetValue = if (isButtonEnabled) 1f else 0.85f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "button_scale"
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .graphicsLayer {
                                            scaleX = buttonScale
                                            scaleY = buttonScale
                                        }
                                        .clip(CircleShape)
                                        .background(
                                            if (chatState.isGeneratingResponse) {
                                                appColors.error // Red when streaming
                                            } else if (chatState.prompt.isNotEmpty() || bitmap != null) {
                                                appColors.accent
                                            } else {
                                                appColors.surfaceTertiary
                                            }
                                        )
                                        .clickable(
                                            enabled = isButtonEnabled,
                                            interactionSource = interactionSource,
                                            indication = null // Remove ripple effect for instant response
                                        ) {
                                            if (chatState.isGeneratingResponse) {
                                                // Stop streaming
                                                chaViewModel.onEvent(ChatUiEvent.StopStreaming)
                                            } else {
                                                // Send message
                                                chaViewModel.onEvent(
                                                    ChatUiEvent.SendPrompt(
                                                        chatState.prompt,
                                                        bitmap
                                                    )
                                                )
                                                uriState.update { "" }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnimatedContent(
                                        targetState = chatState.isGeneratingResponse,
                                        transitionSpec = {
                                            fadeIn(animationSpec = tween(200)) with
                                            fadeOut(animationSpec = tween(200))
                                        },
                                        label = "icon_animation"
                                    ) { isGenerating ->
                                    Icon(
                                        imageVector = if (isGenerating) {
                                            Icons.Rounded.Stop
                                        } else {
                                            Icons.Rounded.ArrowUpward
                                        },
                                        contentDescription = if (isGenerating) "Stop" else "Send",
                                        tint = if (isGenerating) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else if (chatState.prompt.isNotEmpty() || bitmap != null) {
                                            Color(0xFF1C1C1E)
                                        } else {
                                            appColors.textSecondary
                                        },
                                        modifier = Modifier.size(18.dp)
                                    )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } // ModalNavigationDrawer

    }

//    user chat text bubble
    @Composable
    fun UserChatItem(prompt: String, bitmap: Bitmap?) {
        val appColors = LocalAppColors.current
        
        SelectionContainer() {
            Column(
                modifier = Modifier
                    .padding(start = 50.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End

            ) {
//                user picked image display
                bitmap?.let {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
//                            .height(260.dp)
                            .padding(bottom = 2.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        contentDescription = "image",
                        contentScale = ContentScale.FillWidth,
                        bitmap = it.asImageBitmap()
                    )
                }

//                user prompt display
                Text(
                    modifier = Modifier
//                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(appColors.surfaceVariant)
                        .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp),
//                    textAlign = TextAlign.Right,
                    text = prompt,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )

            }
        }

    }

    // Syntax highlighting for code blocks
    @Composable
    fun HighlightedCodeText(code: String) {
        val appColors = LocalAppColors.current
        val annotatedString = buildAnnotatedString {
            val text = code.trim()
            append(text)
            
            // Define color scheme based on theme
            val isDark = appColors.textPrimary.luminance() > 0.5f
            val keywordColor = if (isDark) Color(0xFF569CD6) else Color(0xFF0000FF)
            val stringColor = if (isDark) Color(0xFFCE9178) else Color(0xFFA31515)
            val commentColor = if (isDark) Color(0xFF6A9955) else Color(0xFF008000)
            val numberColor = if (isDark) Color(0xFFB5CEA8) else Color(0xFF098658)
            val functionColor = if (isDark) Color(0xFFDCDCAA) else Color(0xFF795E26)
            
            // Keywords
            val keywords = listOf(
                "fun", "val", "var", "class", "object", "interface", "enum", "when", "if", "else", "for", "while", "do",
                "return", "break", "continue", "try", "catch", "finally", "throw", "import", "package",
                "public", "private", "protected", "internal", "abstract", "final", "open", "override",
                "const", "data", "sealed", "inline", "suspend", "lateinit", "companion",
                "function", "let", "async", "await", "export", "default", "new", "this", "super",
                "int", "string", "bool", "void", "null", "true", "false", "undefined", "def", "from", "as"
            )
            
            // Apply highlighting patterns
            val patterns = listOf(
                Regex("\"([^\"\\\\]|\\\\.)*\"") to stringColor,
                Regex("'([^'\\\\]|\\\\.)*'") to stringColor,
                Regex("//.*") to commentColor,
                Regex("/\\*[\\s\\S]*?\\*/") to commentColor,
                Regex("#.*") to commentColor,
                Regex("\\b\\d+(\\.\\d+)?\\b") to numberColor,
                Regex("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(") to functionColor,
                Regex("\\b(${keywords.joinToString("|")})\\b") to keywordColor
            )
            
            patterns.forEach { (pattern, color) ->
                pattern.findAll(text).forEach { match ->
                    addStyle(
                        style = SpanStyle(color = color),
                        start = match.range.first,
                        end = match.range.last + 1
                    )
                }
            }
        }
        
        Text(
            text = annotatedString,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            ),
            softWrap = false,
            maxLines = Int.MAX_VALUE
        )
    }

    // Custom Markdown renderer with copy buttons for code blocks
    @Composable
    fun MarkdownWithCodeCopy(response: String, context: android.content.Context) {
        val appColors = LocalAppColors.current
        
        // Memoize parsed parts to avoid recalculation on every recomposition
        val parts = remember(response) {
            val codeBlockRegex = Regex("```(\\w+)?\\n?([\\s\\S]*?)```")
            val parsedParts = mutableListOf<Triple<String, Boolean, String>>()
            
            var lastIndex = 0
            codeBlockRegex.findAll(response).forEach { match ->
                // Add text before code block
                if (match.range.first > lastIndex) {
                    parsedParts.add(Triple(response.substring(lastIndex, match.range.first), false, ""))
                }
                // Add code block with language
                val language = match.groupValues[1].ifEmpty { "code" }
                val code = match.groupValues[2]
                parsedParts.add(Triple(code, true, language))
                lastIndex = match.range.last + 1
            }
            // Add remaining text
            if (lastIndex < response.length) {
                parsedParts.add(Triple(response.substring(lastIndex), false, ""))
            }
            parsedParts
        }
        
        if (parts.isEmpty()) {
            // No code blocks, render normal markdown
            MarkdownText(
                markdown = response,
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                )
            )
        } else {
            // Render parts with code blocks having copy buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                parts.forEach { (content, isCodeBlock, language) ->
                    if (isCodeBlock) {
                        // Code block with language label and copy button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(appColors.surfaceTertiary)
                                    .padding(12.dp)
                            ) {
                                // Language label
                                Text(
                                    text = language,
                                    style = TextStyle(
                                        color = appColors.textSecondary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                // Code with horizontal scroll
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    SelectionContainer {
                                        HighlightedCodeText(code = content.trim())
                                    }
                                }
                            }
                            // Copy button overlay
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Code", content.trim())
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "Copy code",
                                    tint = appColors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        // Regular markdown text
                        if (content.isNotBlank()) {
                            MarkdownText(
                                markdown = content,
                                modifier = Modifier.fillMaxWidth(),
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp,
                                    lineHeight = 22.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }

//    model chat text bubble
    @Composable
    fun ModelChatItem(
        response: String,
        userPrompt: String = "",
        modelUsed: String = "",
        onRetry: (String) -> Unit = {},
        isStreaming: Boolean = false,
        freeModels: List<ModelInfo> = emptyList(),
        geminiModels: List<ModelInfo> = emptyList(),
        currentApiProvider: ApiProvider = ApiProvider.GEMINI,
        hasImage: Boolean = false,
        isError: Boolean = false,
        onApiSwitch: (ApiProvider) -> Unit = {}
    ) {
        val appColors = LocalAppColors.current
        val context = LocalContext.current
        val hapticFeedback = LocalHapticFeedback.current
        var showModelSelector by remember { mutableStateOf(false) }
        var showExportDialog by remember { mutableStateOf(false) }
        var selectedApiProvider by remember { mutableStateOf(currentApiProvider) }
        
        // Unified Smooth Streaming Engine
        // Decouples network chunks from visual display for consistent premium feel
        
        // Display buffer - what the user sees
        var displayedText by remember { mutableStateOf("") }
        
        // Reset display when a new streaming session starts
        LaunchedEffect(isStreaming) {
            if (isStreaming) {
                displayedText = ""
            } else if (response.isNotEmpty()) {
                // Ensure final text is shown when streaming stops
                displayedText = response
            }
        }
        
        // Stable reference to target text (network buffer)
        val currentResponse by rememberUpdatedState(response)
        
        // Animation Loop
        LaunchedEffect(isStreaming) {
            if (isStreaming) {
                while (true) {
                    val target = currentResponse
                    val current = displayedText
                    
                    if (current.length < target.length) {
                        val diff = target.length - current.length
                        
                        // Adaptive Speed Algorithm
                        // Adjusts typing speed based on how far behind we are
                        val (charsToProcess, delayMs) = when {
                            diff > 50 -> 5 to 5L    // Catch up fast (Gemini chunks)
                            diff > 20 -> 2 to 10L   // Medium catch up
                            diff > 5 -> 1 to 15L    // Slight lag, speed up a bit
                            else -> 1 to 30L        // Natural typing speed
                        }
                        
                        // Update Display
                        val nextIndex = (current.length + charsToProcess).coerceAtMost(target.length)
                        val newText = target.substring(0, nextIndex)
                        displayedText = newText
                        
                        // Unified Haptics
                        // Trigger every 3 characters of DISPLAYED text
                        // This ensures sync regardless of network chunk size
                        if (newText.length % 3 == 0) {
                             hapticFeedback.performHapticFeedback(
                                 androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove
                             )
                        }
                        
                        delay(delayMs)
                    } else {
                        // Buffer empty, wait for more chunks
                        delay(50)
                        
                        // Break if streaming finished and we caught up
                        if (!isStreaming && current.length == target.length) break
                    }
                }
            }
        }
        
        // Extract brand name from model (memoized)
        val brandName = remember(modelUsed) {
            if (modelUsed.isNotEmpty()) {
                val brand = modelUsed.substringBefore("/")
                brand.split("-", "_").firstOrNull()?.replaceFirstChar { it.uppercase() } 
                    ?: brand.replaceFirstChar { it.uppercase() }
            } else {
                ""
            }
        }
        
        val headerText = remember(brandName) {
            if (brandName.isNotEmpty()) {
                "Mark VII  x  $brandName"
            } else {
                "Mark VII"
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp, top = 8.dp, bottom = 8.dp)
        ) {
//            model response text display with Markdown support - no bubble
            SelectionContainer() {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = headerText,
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.typographica)),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (modelUsed.isNotEmpty()) {
                        Text(
                            text = modelUsed.replace(":free", ""),
                            fontSize = 12.sp,
                            color = appColors.textSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    
                    // Show blinking cursor when streaming (moved below header)
                    if (isStreaming) {
                        // Blinking cursor animation - only created when streaming
                        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "cursor")
                        val cursorAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                animation = androidx.compose.animation.core.tween(
                                    durationMillis = 800,
                                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                                ),
                                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                            ),
                            label = "cursor_blink"
                        )
                        
                        val cursorScale by infiniteTransition.animateFloat(
                            initialValue = 0.95f,
                            targetValue = 1.05f,
                            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                animation = androidx.compose.animation.core.tween(
                                    durationMillis = 800,
                                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                                ),
                                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                            ),
                            label = "cursor_scale"
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .alpha(cursorAlpha)
                                    .graphicsLayer(
                                        scaleX = cursorScale,
                                        scaleY = cursorScale
                                    )
                            ) {
                                Text(
                                    text = "▋",
                                    color = appColors.accent,
                                    fontSize = 16.sp
                                )
                            }
                            Text(
                                text = "Generating response...",
                                fontSize = 12.sp,
                                color = appColors.textSecondary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Render text - use plain text during OpenRouter animation, markdown when complete
                    if (displayedText.isNotEmpty()) {
                        if (isError) {
                            Text(
                                text = displayedText,
                                fontSize = 16.sp,
                                color = appColors.error,
                                fontFamily = FontFamily.Monospace
                            )
                        } else if (isStreaming) {
                            // Plain text during streaming for instant rendering (Unified Engine)
                            Text(
                                text = displayedText,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Markdown rendering for Gemini or completed responses
                            MarkdownWithCodeCopy(response = displayedText, context = context)
                        }
                    }
                }
            }
            
            // Action buttons row - only show when response is complete
            if (!isStreaming && response.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(start = 8.dp, top = 4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Copy button
                IconButton(
                    onClick = {
                        // Remove markdown formatting before copying
                        val cleanText = response
                            .replace("```[a-zA-Z]*\\n".toRegex(), "")
                            .replace("```", "")
                            .replace("**", "")
                            .replace("*", "")
                            .replace("##", "")
                            .replace("#", "")
                            .replace("`", "")
                            .replace("---", "")
                            .replace("- ", "• ")
                            .trim()
                        
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("response", cleanText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy",
                        tint = appColors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Speak button
                val isTtsSpeaking by isTtsSpeakingState.collectAsState()
                
                IconButton(
                    onClick = {
                        if (isTtsInitialized && textToSpeech != null) {
                            if (textToSpeech!!.isSpeaking) {
                                textToSpeech?.stop()
                                isTtsSpeakingState.update { false }  // Immediately stop animation
                                Toast.makeText(context, "Speech stopped", Toast.LENGTH_SHORT).show()
                            } else {
                            // Remove markdown formatting for better speech
                                val cleanText = response
                                    .replace("```[a-zA-Z]*\\n".toRegex(), "")
                                    .replace("```", "")
                                    .replace("**", "")
                                    .replace("*", "")
                                    .replace("#", "")
                                    .replace("`", "")
                                    .trim()
                                
                                speakText(cleanText)
                                Toast.makeText(context, "Speaking...", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Text-to-speech not ready", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    // Animated pause icon when speaking, speaker icon when not
                    if (isTtsSpeaking) {
                        // Pulsing animation for pause icon
                        val infiniteTransition = rememberInfiniteTransition(label = "pause_pulse")
                        val pulseAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.6f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 800,
                                    easing = FastOutSlowInEasing
                                ),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pause_alpha"
                        )
                        
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 800,
                                    easing = FastOutSlowInEasing
                                ),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pause_scale"
                        )
                        
                        Icon(
                            imageVector = Icons.Rounded.Pause,
                            contentDescription = "Stop speaking",
                            tint = appColors.accent,
                            modifier = Modifier
                                .size(20.dp)
                                .alpha(pulseAlpha)
                                .graphicsLayer(
                                    scaleX = pulseScale,
                                    scaleY = pulseScale
                                )
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Speak",
                            tint = appColors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Retry button with model selector
                IconButton(
                    onClick = {
                        showModelSelector = true
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Retry with different model",
                        tint = appColors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Export PDF button
                IconButton(
                    onClick = { showExportDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PictureAsPdf,
                        contentDescription = "Export PDF",
                        tint = appColors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Share button
                IconButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, response)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share response"))
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Share",
                        tint = appColors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            }
            
            // Model selector dialog for retry
            if (showModelSelector) {
                AlertDialog(
                    onDismissRequest = { showModelSelector = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    title = {
                        Text(
                            text = "Retry with Model",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // API Provider Switch
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Gemini button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (selectedApiProvider == ApiProvider.GEMINI)
                                                appColors.accent.copy(alpha = 0.2f)
                                            else
                                                appColors.surfaceVariant
                                        )
                                        .clickable {
                                            selectedApiProvider = ApiProvider.GEMINI
                                            onApiSwitch(ApiProvider.GEMINI)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Gemini",
                                        color = if (selectedApiProvider == ApiProvider.GEMINI)
                                            appColors.accent
                                        else
                                            appColors.textSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedApiProvider == ApiProvider.GEMINI)
                                            FontWeight.SemiBold
                                        else
                                            FontWeight.Normal
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                // OpenRouter button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (selectedApiProvider == ApiProvider.OPENROUTER)
                                                appColors.accent.copy(alpha = 0.2f)
                                            else
                                                appColors.surfaceVariant
                                        )
                                        .clickable {
                                            if (hasImage) {
                                                Toast.makeText(
                                                    context,
                                                    "⚠️ OpenRouter doesn't support images. Please use Gemini for image queries.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                selectedApiProvider = ApiProvider.OPENROUTER
                                                onApiSwitch(ApiProvider.OPENROUTER)
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "OpenRouter",
                                        color = if (selectedApiProvider == ApiProvider.OPENROUTER)
                                            appColors.accent
                                        else
                                            appColors.textSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedApiProvider == ApiProvider.OPENROUTER)
                                            FontWeight.SemiBold
                                        else
                                            FontWeight.Normal
                                    )
                                }
                            }
                            
                            // Show warning if image exists and OpenRouter selected
                            if (hasImage && selectedApiProvider == ApiProvider.OPENROUTER) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(appColors.error.copy(alpha = 0.1f))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "⚠️",
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = "Images are not supported with OpenRouter",
                                            color = appColors.error,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                            
                            // Divider
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .height(1.dp)
                                    .background(appColors.divider)
                            )
                            
                            // Model List
                            val currentModels = if (selectedApiProvider == ApiProvider.GEMINI) geminiModels else freeModels
                            
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp)
                            ) {
                                if (currentModels.isEmpty()) {
                                    item {
                                        Text(
                                            text = "Loading models...",
                                            color = appColors.textSecondary,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                } else {
                                    itemsIndexed(currentModels) { _, model ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (model.apiModel == modelUsed)
                                                        appColors.accent.copy(alpha = 0.1f)
                                                    else
                                                        Color.Transparent
                                                )
                                                .clickable {
                                                    if (hasImage && selectedApiProvider == ApiProvider.OPENROUTER) {
                                                        Toast.makeText(
                                                            context,
                                                            "⚠️ Cannot use OpenRouter with images. Switch to Gemini.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        ChatData.selected_model = model.apiModel
                                                        showModelSelector = false
                                                        onRetry(model.apiModel)
                                                    }
                                                }
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = model.displayName,
                                                    color = if (model.apiModel == modelUsed)
                                                        appColors.accent
                                                    else
                                                        MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 15.sp,
                                                    fontWeight = if (model.apiModel == modelUsed)
                                                        FontWeight.SemiBold
                                                    else
                                                        FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showModelSelector = false },
                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                contentColor = appColors.textSecondary
                            )
                        ) {
                            Text("Cancel", fontSize = 15.sp)
                        }
                    }
                )
            }
            
            // Export Dialog
            if (showExportDialog) {
                AlertDialog(
                    onDismissRequest = { showExportDialog = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                    title = { Text("Export Response", color = MaterialTheme.colorScheme.onSurface) },
                    text = { Text("Choose how you want to export this response as PDF.", color = appColors.textPrimary) },
                    confirmButton = {
                        TextButton(onClick = {
                            showExportDialog = false
                            PdfGenerator.exportToPdf(context, response, brandName, modelUsed, userPrompt)
                        }) {
                            Text("Save to Device", color = appColors.accent)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showExportDialog = false
                            PdfGenerator.sharePdf(context, response, brandName, modelUsed, userPrompt)
                        }) {
                            Text("Share PDF", color = appColors.accent)
                        }
                    }
                )
            }
        }
    }

    private fun speakText(text: String) {
        if (textToSpeech == null) return
        
        // Detect language using MLKit
        val languageIdentifier = LanguageIdentification.getClient(
            LanguageIdentificationOptions.Builder()
                .setConfidenceThreshold(0.34f)
                .build()
        )
        
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode != "und") {
                    // Map MLKit language codes to Locale
                    val locale = when (languageCode) {
                        "zh" -> java.util.Locale.SIMPLIFIED_CHINESE
                        "zh-Hant" -> java.util.Locale.TRADITIONAL_CHINESE
                        "ja" -> java.util.Locale.JAPANESE
                        "ko" -> java.util.Locale.KOREAN
                        "es" -> java.util.Locale("es")
                        "fr" -> java.util.Locale.FRENCH
                        "de" -> java.util.Locale.GERMAN
                        "it" -> java.util.Locale.ITALIAN
                        "pt" -> java.util.Locale("pt")
                        "ru" -> java.util.Locale("ru")
                        "ar" -> java.util.Locale("ar")
                        "hi" -> java.util.Locale("hi")
                        "en" -> java.util.Locale.US
                        else -> java.util.Locale.US
                    }
                    
                    // Set language if available
                    val result = textToSpeech?.setLanguage(locale)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        // Fallback to English if language not supported
                        textToSpeech?.setLanguage(java.util.Locale.US)
                    }
                }
                
                // Speak the text after setting language
                speakTextWithLanguage(text)
            }
            .addOnFailureListener {
                // If detection fails, use English as fallback
                textToSpeech?.setLanguage(java.util.Locale.US)
                speakTextWithLanguage(text)
            }
    }
    
    private fun speakTextWithLanguage(text: String) {
        if (textToSpeech == null) return
        
        val maxLength = TextToSpeech.getMaxSpeechInputLength() - 100 // Buffer
        val utteranceId = "tts_${System.currentTimeMillis()}"
        
        if (text.length <= maxLength) {
            val params = Bundle()
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            return
        }
        
        // Flush existing
        textToSpeech?.speak("", TextToSpeech.QUEUE_FLUSH, null, null)
        
        var remainingText = text
        var chunkIndex = 0
        while (remainingText.isNotEmpty()) {
            val chunk = if (remainingText.length > maxLength) {
                // Find a good break point
                val splitIndex = remainingText.lastIndexOf('.', maxLength)
                    .takeIf { it > 0 }
                    ?: remainingText.lastIndexOf(' ', maxLength)
                    .takeIf { it > 0 }
                    ?: maxLength
                
                remainingText.substring(0, splitIndex)
            } else {
                remainingText
            }
            
            val params = Bundle()
            val chunkUtteranceId = "${utteranceId}_chunk_${chunkIndex}"
            textToSpeech?.speak(chunk, TextToSpeech.QUEUE_ADD, params, chunkUtteranceId)
            chunkIndex++
            
            remainingText = if (chunk.length < remainingText.length) {
                remainingText.substring(chunk.length).trim()
            } else {
                ""
            }
        }
    }

    @Composable
    fun getSelectedBitmap(): Bitmap? {
        val uri = uriState.collectAsState().value

        val imageState: AsyncImagePainter.State = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .size(Size.ORIGINAL)
                .build()
        ).state

        if (imageState is AsyncImagePainter.State.Success) {
            return imageState.result.drawable.toBitmap()
        }

        return null
    }
    
    @Composable
    fun PromptSuggestionBubbles(onSuggestionClick: (String) -> Unit) {
        val appColors = LocalAppColors.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Title
            Text(
                text = "Getting Started",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            
            // Instructions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InstructionStep(
                    number = "1",
                    text = "Select your desired AI model"
                )
                
                InstructionStep(
                    number = "2",
                    text = "Type your message in the text box below"
                )
                
                InstructionStep(
                    number = "3",
                    text = "Tap the send button to get Model responses"
                )
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Response Actions section
            Text(
                text = "Response Actions",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionItem(
                    icon = "📋",
                    text = "Copy the response text to clipboard"
                )
                ActionItem(
                    icon = "🔊",
                    text = "Listen to the response with text-to-speech"
                )
                ActionItem(
                    icon = "🔄",
                    text = "Regenerate the response using a different model"
                )
                ActionItem(
                    icon = "📤",
                    text = "Share the response with other apps"
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tips section
            Text(
                text = "💡 Tips",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors.accent,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TipItem("All models are completely free to use")
                TipItem("Different models excel at different tasks")
                TipItem("You can switch models anytime during conversation")
            }
        }
    }
    
    @Composable
    fun InstructionStep(
        number: String,
        text: String
    ) {
        val appColors = LocalAppColors.current
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Step number circle
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(appColors.accent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
    
    @Composable
    fun ActionItem(
        icon: String,
        text: String
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = icon,
                fontSize = 18.sp
            )
            
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
    
    @Composable
    fun TipItem(text: String) {
        val appColors = LocalAppColors.current
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "•",
                color = appColors.textSecondary,
                fontSize = 14.sp
            )
            
            Text(
                text = text,
                color = appColors.textSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }


}
