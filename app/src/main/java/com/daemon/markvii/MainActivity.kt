

package com.daemon.markvii

/**
 * @author Nitesh
 */

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Refresh
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import dev.jeziellago.compose.markdowntext.MarkdownText




class MainActivity : ComponentActivity() {

    private val uriState = MutableStateFlow("")
    private val voiceInputState = MutableStateFlow("")
    
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val imagePicker =
        registerForActivityResult<PickVisualMediaRequest, Uri?>(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let {
                uriState.update { uri.toString() }
            }
        }

    private val voiceInputLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            spokenText?.let {
                if (it.isNotEmpty()) {
                    voiceInputState.update { _ -> it[0] }
                }
            }
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Thread.sleep(1000) // splash screen delay
        installSplashScreen()  // splash screen ui
        
        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                textToSpeech?.language = java.util.Locale.US
            }
        }
        
        setContent {

            MarkVIITheme {
                var opentimes by remember { mutableIntStateOf(0) }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
//                    color = MaterialTheme.colorScheme.background
                )

                {
//                    for switching between from home screen to infoTab
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home", builder = {
                        composable("home",){
                            opentimes++
                            Scaffold(
//                                top bar items
                                topBar = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primary)
                                        .height(50.dp)
                                        .padding(start = 20.dp, end = 5.dp)
                                ) {
                                    // Mark VII header title
                                    Text(
                                        text = "Mark VII",
                                        fontSize = 22.sp,
                                        fontFamily = FontFamily(Font(R.font.typographica)),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    )

//                                      info tab start icon at top bar
                                    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.info_card))
                                    LottieAnimation(
                                        composition = composition,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .align(Alignment.CenterEnd)
                                            .clickable {
                                                navController.navigate("info_screen")
                                            },
                                        iterations = LottieConstants.IterateForever  // Play in loop
                                    )
//                                        Icon(
//                                            modifier = Modifier
//                                                .size(35.dp)
//                                                .align(Alignment.CenterEnd)
//                                                .clickable {
//                                                    navController.navigate("info_screen")
////                                                    Toast.makeText(context, "Under development...", Toast.LENGTH_SHORT).show()
//                                                },
//                                            imageVector = Icons.Rounded.Info,
//                                            contentDescription = "Info Tab",
//                                            tint = MaterialTheme.colorScheme.onPrimary
//                                        )
                                }

//                                    Show welcome guide once when app opens (no API call)
                                val chaViewModel = viewModel<ChatViewModel>()
                                if(opentimes==1){
                                    chaViewModel.showWelcomeGuide()
                                }
                            }
                            ) {
                                ChatScreen(paddingValues = it)  // starting chat screen ui
                            }

                        }
                        composable("info_screen",){
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
        var showDetails by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF2C2C2E),
            titleContentColor = Color(0xFFE5E5E5),
            textContentColor = Color(0xFFE5E5E5),
            shape = RoundedCornerShape(20.dp),
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pin), // Replace with error icon if available
                        contentDescription = "Error",
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = errorTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE5E5E5)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = errorMessage,
                        fontSize = 15.sp,
                        color = Color(0xFFE5E5E5)
                    )
                    
                    if (showDetails) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1C1C1E))
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF3A3A3C),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            val scrollState = rememberScrollState()
                            SelectionContainer {
                                Text(
                                    text = errorDetails,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 13.sp,
                                    color = Color(0xFFAAAAAA),
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
                        contentColor = if (isRetryable) Color(0xFF00D9FF) else Color(0xFFE5E5E5)
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
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatScreen(paddingValues: PaddingValues) {
        val chaViewModel = viewModel<ChatViewModel>()
        val chatState = chaViewModel.chatState.collectAsState().value
        val bitmap = getBitmap()
        val voiceInput = voiceInputState.collectAsState().value
        val context = LocalContext.current
        
        // State for loading free models from OpenRouter
        var isLoadingModels by remember { mutableStateOf(false) }
        var freeModels by remember { mutableStateOf<List<ModelInfo>>(emptyList()) }
        var modelsLoadError by remember { mutableStateOf<String?>(null) }
        
        // Observe Firebase API key
        val firebaseApiKey by FirebaseConfigManager.apiKey.collectAsState()
        
        // Initialize Firebase to get API key
        LaunchedEffect(Unit) {
            FirebaseConfigManager.initialize()
        }
        
        // Update API key when Firebase data changes
        LaunchedEffect(firebaseApiKey) {
            if (firebaseApiKey.isNotEmpty()) {
                ChatData.updateApiKey(firebaseApiKey)
            }
        }
        
        // Load free models from OpenRouter on first composition
        LaunchedEffect(Unit) {
            isLoadingModels = true
            try {
                freeModels = ChatData.fetchFreeModels()
                if (freeModels.isEmpty()) {
                    modelsLoadError = "No free models available"
                }
            } catch (e: Exception) {
                modelsLoadError = "Failed to load models: ${e.message}"
            } finally {
                isLoadingModels = false
            }
        }
        
        // Show loading popup while models are loading
        if (isLoadingModels) {
            AlertDialog(
                onDismissRequest = { },
                containerColor = Color(0xFF2C2C2E),
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
                            color = Color(0xFF00D9FF),
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "Loading free models...",
                            fontSize = 16.sp,
                            color = Color(0xFFE5E5E5),
                            fontFamily = FontFamily(Font(R.font.typographica))
                        )
                    }
                },
                confirmButton = { }
            )
        }
        
        // Observe error state from ViewModel
        val currentError = chatState.error

        // Show error dialog when error state is present
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
        
        // Model selector state for prompt box
        val isPromptDropDownExpanded = remember { mutableStateOf(false) }
        val promptItemPosition = remember { mutableStateOf(0) }
        
        val currentModels = freeModels
        
        // Set initial model when models load
        LaunchedEffect(currentModels) {
            if (currentModels.isNotEmpty() && promptItemPosition.value < currentModels.size) {
                ChatData.selected_model = currentModels[promptItemPosition.value].apiModel
            }
        }

        // Update prompt when voice input is received
        LaunchedEffect(voiceInput) {
            if (voiceInput.isNotEmpty()) {
                chaViewModel.onEvent(ChatUiEvent.UpdatePrompt(voiceInput))
                voiceInputState.update { "" }
            }
        }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(top = paddingValues.calculateTopPadding())) {
//            Chat messages list - extends to bottom of screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    reverseLayout = true,
                    contentPadding = PaddingValues(bottom = 200.dp) // Space for prompt box
                ) {
                    // Show typing indicator when generating response
                    if (chatState.isGeneratingResponse) {
                        item {
                            TypingIndicator()
                        }
                    }
                    
                    itemsIndexed(chatState.chatList) { index, chat ->
                        if (chat.isFromUser) {
                            UserChatItem(
                                prompt = chat.prompt, bitmap = chat.bitmap
                            )
                        } else {
                            // Get the previous user message for retry
                            val previousUserChat = if (index < chatState.chatList.size - 1) {
                                chatState.chatList.getOrNull(index + 1)
                            } else null
                            
                            ModelChatItem(
                                response = chat.prompt,
                                modelUsed = chat.modelUsed,
                                onRetry = { _ ->
                                    chaViewModel.onEvent(
                                        ChatUiEvent.RetryPrompt(
                                            previousUserChat?.prompt ?: "",
                                            previousUserChat?.bitmap
                                        )
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
                                    Color.Black
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
                            .background(Color(0xFF2C2C2E))
                            .border(
                                width = 1.dp,
                                color = Color(0xFF3A3A3C),
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
                                tint = Color(0xFFAAAAAA),
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
                                color = Color(0xFFE5E5E5),
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Remove button
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Remove image",
                                tint = Color(0xFFAAAAAA),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        uriState.update { "" }
                                    }
                            )
                        }
                    }
                }

// ================ Dark Minimalist Input Box (Gemini Style) ================
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = Color.Black.copy(alpha = 0.4f),
                                spotColor = Color.Black.copy(alpha = 0.4f)
                            )
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xFF2C2C2E))
                            .border(
                                width = 1.dp,
                                color = Color(0xFF3A3A3C),
                                shape = RoundedCornerShape(28.dp)
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
                                        color = Color(0xFF8E8E93)
                                    )
                                },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 15.sp,
                                    color = Color(0xFFE5E5E5),
                                    lineHeight = 20.sp
                                ),
                    colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFFE5E5E5),
                                    unfocusedTextColor = Color(0xFFE5E5E5),
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    cursorColor = Color(0xFFE5E5E5),
                                    focusedPlaceholderColor = Color(0xFF8E8E93),
                                    unfocusedPlaceholderColor = Color(0xFF8E8E93)
                                )
                            )
                            
                            // Bottom row: Icons + Model Selector + Mic + Send button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 12.dp, bottom = 8.dp, top = 0.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                
                                // Model selector (clickable text with dropdown icon)
                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = ripple(
                                                    color = Color.White.copy(alpha = 0.3f)
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
                                            color = Color(0xFFE5E5E5),
                                            style = MaterialTheme.typography.bodyMedium,
                                            lineHeight = 13.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Icon(
                                            painter = painterResource(id = R.drawable.drop_down_ic),
                                            contentDescription = "Select model",
                                            tint = Color(0xFFE5E5E5),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    
                                    // Dropdown menu - Aesthetic design
                                    Box(
                                        modifier = Modifier
                                            .width(280.dp)
                                            .shadow(
                                                elevation = 12.dp,
                                                shape = RoundedCornerShape(20.dp),
                                                ambientColor = Color.Black.copy(alpha = 0.5f),
                                                spotColor = Color.Black.copy(alpha = 0.5f)
                                            )
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color(0xFF2C2C2E))
                                            .border(
                                                width = 1.dp,
                                                color = Color(0xFF3A3A3C),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                    ) {
                                        DropdownMenu(
                                            expanded = isPromptDropDownExpanded.value,
                                            onDismissRequest = { isPromptDropDownExpanded.value = false },
                                            modifier = Modifier
                                                .width(280.dp)
                                                .heightIn(max = 400.dp)
                                                .background(Color(0xFF2C2C2E)),
                                            shape = RoundedCornerShape(20.dp),
                                            containerColor = Color(0xFF2C2C2E)
                                        ) {
                                        if (currentModels.isEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "No models available",
                                                    color = Color(0xFF8E8E93),
                                                    fontSize = 14.sp,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
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
                                                                // Model indicator dot
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(6.dp)
                                                                        .clip(CircleShape)
                                                                        .background(
                                                                            if (model.isAvailable) 
                                                                                Color(0xFF00D9FF) 
                                                                            else 
                                                                                Color(0xFF8E8E93)
                                                                        )
                                                                )
                                                                
                                                                Spacer(modifier = Modifier.width(12.dp))
                                                                
                                                                Text(
                                                                    text = model.displayName,
                                                                    color = if (promptItemPosition.value == index) 
                                                                        Color(0xFF00D9FF) 
                                                                    else 
                                                                        Color(0xFFE5E5E5),
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
                                                                    Color(0xFF00D9FF).copy(alpha = 0.1f)
                                                                else
                                                                    Color(0xFF2C2C2E)
                                                            ),
                                                        contentPadding = PaddingValues(
                                                            horizontal = 12.dp,
                                                            vertical = 12.dp
                                                        ),
                                                        colors = MenuDefaults.itemColors(
                                                            textColor = Color(0xFFE5E5E5),
                                                            leadingIconColor = Color(0xFFE5E5E5),
                                                            trailingIconColor = Color(0xFFE5E5E5),
                                                            disabledTextColor = Color(0xFF8E8E93),
                                                            disabledLeadingIconColor = Color(0xFF8E8E93),
                                                            disabledTrailingIconColor = Color(0xFF8E8E93)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                        }
                                    }
                                }
                                
                                // Microphone icon (moved to right)
                                IconButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                putExtra(
                                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                                )
                                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                                            }
                                            voiceInputLauncher.launch(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "Voice input not available",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                        Icon(
                                        imageVector = Icons.Rounded.Mic,
                                        contentDescription = "Voice input",
                                        tint = Color(0xFFE5E5E5),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                
                                // Send button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (chatState.prompt.isNotEmpty() || bitmap != null) {
                                                Color(0xFFE5E5E5)
                                            } else {
                                                Color(0xFF3A3A3C)
                                            }
                                        )
                                        .clickable(
                                            enabled = chatState.prompt.isNotEmpty() || bitmap != null
                                        ) {
                                            chaViewModel.onEvent(
                                                ChatUiEvent.SendPrompt(
                                                    chatState.prompt,
                                                    bitmap
                                                )
                                            )
                                            uriState.update { "" }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                        Icon(
                                        imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = "Send",
                                        tint = if (chatState.prompt.isNotEmpty() || bitmap != null) {
                                            Color(0xFF1C1C1E)
                                        } else {
                                            Color(0xFF8E8E93)
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

    }

//    Typing indicator animation (WhatsApp style)
    @Composable
    fun TypingIndicator() {
        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "typing")
        
        Row(
            modifier = Modifier
                .padding(start = 8.dp, end = 50.dp, top = 8.dp, bottom = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF2C2C2E))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Three animated dots
            for (i in 0..2) {
                val offset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -8f,
                    animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                        animation = androidx.compose.animation.core.tween(
                            durationMillis = 600,
                            delayMillis = i * 200,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        ),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                    ),
                    label = "dot_$i"
                )
                
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .offset(y = offset.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8E8E93))
                )
            }
        }
    }

//    user chat text bubble
    @Composable
    fun UserChatItem(prompt: String, bitmap: Bitmap?) {
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
                        .background(Color(0xFF3E3E3E))
                        .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp),
//                    textAlign = TextAlign.Right,
                    text = prompt,
                    fontSize = 17.sp,
                    color = Color.White,
                )

            }
        }

    }

    // Custom Markdown renderer with copy buttons for code blocks
    @Composable
    fun MarkdownWithCodeCopy(response: String, context: android.content.Context) {
        val codeBlockRegex = Regex("```([\\s\\S]*?)```")
        val parts = mutableListOf<Pair<String, Boolean>>() // Pair<content, isCodeBlock>
        
        var lastIndex = 0
        codeBlockRegex.findAll(response).forEach { match ->
            // Add text before code block
            if (match.range.first > lastIndex) {
                parts.add(Pair(response.substring(lastIndex, match.range.first), false))
            }
            // Add code block
            parts.add(Pair(match.groupValues[1], true))
            lastIndex = match.range.last + 1
        }
        // Add remaining text
        if (lastIndex < response.length) {
            parts.add(Pair(response.substring(lastIndex), false))
        }
        
        if (parts.isEmpty()) {
            // No code blocks, render normal markdown
            MarkdownText(
                markdown = response,
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                )
            )
        } else {
            // Render parts with code blocks having copy buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                parts.forEach { (content, isCodeBlock) ->
                    if (isCodeBlock) {
                        // Code block with copy button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E1E1E))
                                    .padding(12.dp)
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                SelectionContainer {
                                    Text(
                                        text = content.trim(),
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFFE0E0E0),
                                            fontSize = 14.sp
                                        ),
                                        softWrap = false,
                                        maxLines = Int.MAX_VALUE
                                    )
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
                                    tint = Color(0xFF8E8E93),
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
                                    color = Color.White,
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
        modelUsed: String = "",
        onRetry: (String) -> Unit = {}
    ) {
        val context = LocalContext.current
        var showModelSelector by remember { mutableStateOf(false) }
        
        // Get current free models
        var freeModels by remember { mutableStateOf<List<ModelInfo>>(emptyList()) }
        LaunchedEffect(Unit) {
            freeModels = ChatData.fetchFreeModels()
        }
        
        // Extract brand name from model (e.g., "deepseek/deepseek-chat-v3.1" -> "Deepseek")
        val brandName = if (modelUsed.isNotEmpty()) {
            val brand = modelUsed.substringBefore("/")
            brand.split("-", "_").firstOrNull()?.replaceFirstChar { it.uppercase() } 
                ?: brand.replaceFirstChar { it.uppercase() }
        } else {
            ""
        }
        
        val headerText = if (brandName.isNotEmpty()) {
            "Mark VII  x  $brandName"
        } else {
            "Mark VII"
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
                        color = Color.White
                    )
                    if (modelUsed.isNotEmpty()) {
                        Text(
                            text = modelUsed.replace(":free", ""),
                            fontSize = 12.sp,
                            color = Color(0xFF8E8E93),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Render markdown with code block enhancements
                    MarkdownWithCodeCopy(response = response, context = context)
                }
            }
            
            // Action buttons row
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
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // Speak button
                IconButton(
                    onClick = {
                        if (isTtsInitialized && textToSpeech != null) {
                            if (textToSpeech!!.isSpeaking) {
                                textToSpeech?.stop()
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
                                
                                textToSpeech?.speak(
                                    cleanText,
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    null
                                )
                                Toast.makeText(context, "Speaking...", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Text-to-speech not ready", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Speak",
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // Retry button with model selector
                IconButton(
                    onClick = {
                        showModelSelector = true
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Retry with different model",
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(18.dp)
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
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Share",
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // Model selector dialog for retry
            if (showModelSelector) {
                AlertDialog(
                    onDismissRequest = { showModelSelector = false },
                    containerColor = Color(0xFF2C2C2E),
                    shape = RoundedCornerShape(20.dp),
                    title = {
                        Text(
                            text = "Retry with Model",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5E5E5)
                        )
                    },
                    text = {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                        ) {
                            if (freeModels.isEmpty()) {
                                item {
                                    Text(
                                        text = "Loading models...",
                                        color = Color(0xFF8E8E93),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            } else {
                                itemsIndexed(freeModels) { _, model ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (model.apiModel == modelUsed)
                                                    Color(0xFF00D9FF).copy(alpha = 0.1f)
                                                else
                                                    Color.Transparent
                                            )
                                            .clickable {
                                                ChatData.selected_model = model.apiModel
                                                showModelSelector = false
                                                onRetry(model.apiModel)
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF00D9FF))
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = model.displayName,
                                                color = if (model.apiModel == modelUsed)
                                                    Color(0xFF00D9FF)
                                                else
                                                    Color(0xFFE5E5E5),
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
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showModelSelector = false },
                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF8E8E93)
                            )
                        ) {
                            Text("Cancel", fontSize = 15.sp)
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun getBitmap(): Bitmap? {
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
