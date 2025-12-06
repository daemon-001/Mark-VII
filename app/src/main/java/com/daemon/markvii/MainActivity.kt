

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
import androidx.activity.enableEdgeToEdge
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
import com.daemon.markvii.ui.ChatScreen


import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import com.airbnb.lottie.compose.animateLottieCompositionAsState

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
        enableEdgeToEdge()

        // Initialize ChatHistoryManager
        com.daemon.markvii.data.ChatHistoryManager.init(applicationContext)
        
        // Initialize ThemePreferences
        com.daemon.markvii.data.ThemePreferences.init(applicationContext)

        // Initialize OnboardingPreferences
        com.daemon.markvii.data.OnboardingPreferences.init(applicationContext)
        
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
            val statusBarColor = android.graphics.Color.TRANSPARENT
            val navigationBarColor = android.graphics.Color.TRANSPARENT
            
            SideEffect {
                window.statusBarColor = statusBarColor
                window.navigationBarColor = navigationBarColor
            }

            MarkVIITheme(darkTheme = darkTheme) {
                var opentimes by remember { mutableIntStateOf(0) }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                )

                {
                    var isOnboarding by remember { mutableStateOf(com.daemon.markvii.data.OnboardingPreferences.isFirstRun()) }

                    if (isOnboarding) {
                        com.daemon.markvii.ui.OnboardingScreen(
                            onFinish = {
                                com.daemon.markvii.data.OnboardingPreferences.setFirstRunCompleted()
                                isOnboarding = false
                            }
                        )
                    } else {
                        // for switching between from home screen to infoTab
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
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    appColors.topBarBackground, // Solid at top
                                                    appColors.topBarBackground.copy(alpha = 0.95f), // Slightly transparent
                                                    appColors.topBarBackground.copy(alpha = 0f) // Transparent at bottom
                                                )
                                            )
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .statusBarsPadding()
                                            .height(100.dp)
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp)
                                    ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(top = 8.dp), // Add some top padding
                                        verticalAlignment = Alignment.Top,
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
                                }
                            },

//                                    Show welcome guide once when app opens (no API call)
                                bottomBar = {
                                if(opentimes==1){
                                    chaViewModel.showWelcomeGuide()
                                }
                            }
                            ) {
                                ChatScreen(
                                    paddingValues = it,
                                    bitmap = getSelectedBitmap(),
                                    voiceInput = voiceInputState.collectAsState().value,
                                    voicePitch = voicePitchState.collectAsState().value,
                                    isListening = isListeningState.collectAsState().value,
                                    isTtsSpeaking = isTtsSpeakingState.collectAsState().value,
                                    isTtsReady = isTtsInitialized && textToSpeech != null,
                                    onVoiceInputConsumed = { voiceInputState.update { "" } },
                                    onRemoveImage = { uriState.update { "" } },
                                    onStopTts = { 
                                        textToSpeech?.stop()
                                        isTtsSpeakingState.update { false }
                                    },
                                    onSpeak = { text -> speakText(text) },
                                    onMicClick = { 
                                        if (isListeningState.value) {
                                            speechRecognizer?.stopListening()
                                            isListeningState.update { false }
                                            voicePitchState.update { 0f }
                                        } else {
                                             if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                                 ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_CODE)
                                             } else {
                                                 val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                                                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                                                 }
                                                 speechRecognizer?.startListening(intent)
                                             }
                                        }
                                    },
                                    onImagePickerClick = {
                                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    }
                                )
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
        val voiceInput = voiceInputState.collectAsState().value
        val voicePitch = voicePitchState.collectAsState().value
        val isListening = isListeningState.collectAsState().value
        val isTtsSpeaking = isTtsSpeakingState.collectAsState().value
        
        com.daemon.markvii.ui.ChatScreen(
            paddingValues = paddingValues,
            bitmap = getSelectedBitmap(),
            voiceInput = voiceInput,
            voicePitch = voicePitch,
            isListening = isListening,
            isTtsSpeaking = isTtsSpeaking,
            isTtsReady = isTtsInitialized,
            onVoiceInputConsumed = { voiceInputState.update { "" } },
            onRemoveImage = { uriState.update { "" } },
            onStopTts = { 
                textToSpeech?.stop()
                isTtsSpeakingState.update { false }
            },
            onSpeak = { speakText(it) },
            onMicClick = { 
                if (isListening) {
                   speechRecognizer?.stopListening()
                   isListeningState.update { false }
                } else {
                   val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                   }
                   try {
                       speechRecognizer?.startListening(intent)
                       isListeningState.update { true }
                   } catch (e: Exception) {
                       Toast.makeText(this@MainActivity, "Error starting mic", Toast.LENGTH_SHORT).show()
                   }
                }
            },
            onImagePickerClick = {
                 imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        ) }

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
    


}

