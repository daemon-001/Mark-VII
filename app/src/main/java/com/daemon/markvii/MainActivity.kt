

package com.daemon.markvii

/**
 * @author Nitesh
 */

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.filled.AttachFile
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
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
import com.daemon.markvii.data.ModelConfiguration
import com.daemon.markvii.data.ModelInfo
import com.daemon.markvii.data.FirebaseConfigManager
import com.daemon.markvii.data.FirebaseModelInfo
import com.daemon.markvii.ui.theme.MarkVIITheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update




class MainActivity : ComponentActivity() {

    private val uriState = MutableStateFlow("")
    private val voiceInputState = MutableStateFlow("")

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
                    voiceInputState.update { text -> it[0] }
                }
            }
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Thread.sleep(1000) // splash screen delay
        installSplashScreen()  // splash screen ui
        setContent {

            MarkVIITheme {
                var opentimes by remember { mutableIntStateOf(0) }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Gray
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
                                var context = LocalContext.current
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
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1C1C1E))
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF3A3A3C),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            SelectionContainer {
                                Text(
                                    text = errorDetails,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 13.sp,
                                    color = Color(0xFFAAAAAA),
                                    lineHeight = 18.sp
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
        val scope = rememberCoroutineScope()
        
        // Observe Firebase config state
        val configState by FirebaseConfigManager.configState.collectAsState()
        var showFirebaseError by remember { mutableStateOf(false) }
        var firebaseErrorMessage by remember { mutableStateOf("") }
        
        // Initialize Firebase on first composition
        LaunchedEffect(Unit) {
            FirebaseConfigManager.initialize()
        }
        
        // Show loading popup while Firebase is loading
        if (configState is FirebaseConfigManager.ConfigState.Loading) {
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
                            text = "Loading models...",
                            fontSize = 16.sp,
                            color = Color(0xFFE5E5E5),
                            fontFamily = FontFamily(Font(R.font.typographica))
                        )
                    }
                },
                confirmButton = { }
            )
        }
        
        // Show Firebase error dialog
        if (showFirebaseError) {
            ErrorDialog(
                errorTitle = "Configuration Error",
                errorMessage = "Failed to load Firebase configuration",
                errorDetails = firebaseErrorMessage,
                isRetryable = true,
                onDismiss = { 
                    showFirebaseError = false
                },
                onRetry = {
                    scope.launch {
                        FirebaseConfigManager.initialize()
                    }
                }
            )
        }
        
        // Monitor config state for errors
        LaunchedEffect(configState) {
            when (configState) {
                is FirebaseConfigManager.ConfigState.Error -> {
                    val error = configState as FirebaseConfigManager.ConfigState.Error
                    firebaseErrorMessage = error.message
                    showFirebaseError = true
                }
                else -> {
                    showFirebaseError = false
                }
            }
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
        
        // Observe Firebase models and API key
        val firebaseModels by FirebaseConfigManager.models.collectAsState()
        val firebaseApiKey by FirebaseConfigManager.apiKey.collectAsState()
        
        val currentModels = if (firebaseModels.isNotEmpty()) {
            firebaseModels.map { ModelInfo(it.displayName, it.apiModel, it.isAvailable) }
        } else {
            emptyList()
        }

        // Update API key when Firebase data changes
        LaunchedEffect(firebaseApiKey) {
            if (firebaseApiKey.isNotEmpty()) {
                ChatData.updateApiKey(firebaseApiKey)
            }
        }
        
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


        BigHi() // chat screen background ui function
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            verticalArrangement = Arrangement.Bottom
        ) {

//            prompt entry textbox starts here
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                reverseLayout = true,
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
                        ModelChatItem(response = chat.prompt, modelUsed = chat.modelUsed)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                    .padding(start = 50.dp, top = 8.dp, bottom = 8.dp)
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
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp),
//                    textAlign = TextAlign.Right,
                    text = prompt,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )

            }
        }

    }

//    model chat text bubble
    @Composable
    fun ModelChatItem(response: String, modelUsed: String = "") {
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
        
        SelectionContainer() {
            Column(
                modifier = Modifier.padding(end = 50.dp, top = 8.dp, bottom = 8.dp)


            ) {
//                model response text display
                Box(
                    modifier = Modifier
//                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                        .padding(16.dp),
                ){
                    Text(
                        text = headerText,
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.typographica)),
                        color = Color.White
                    )
                    Text(
                        text = "\n\n" + response,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

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

//    Dropdown menu of nlp models
    @Composable
    fun DropDownDemo() {
        val isDropDownExpanded = remember { mutableStateOf(false) }
        val itemPosition = remember { mutableStateOf(0) }
        val scope = rememberCoroutineScope()
        
        // Observe Firebase models and API key
        val firebaseModels by FirebaseConfigManager.models.collectAsState()
        val firebaseApiKey by FirebaseConfigManager.apiKey.collectAsState()
        val configState by FirebaseConfigManager.configState.collectAsState()
        
        // Use ONLY Firebase models - no local fallback
        val currentModels = if (firebaseModels.isNotEmpty()) {
            firebaseModels.map { ModelInfo(it.displayName, it.apiModel, it.isAvailable) }
        } else {
            // If Firebase not configured, show empty list
            emptyList()
        }
        
        // Update API key when Firebase data changes
        LaunchedEffect(firebaseApiKey) {
            if (firebaseApiKey.isNotEmpty()) {
                ChatData.updateApiKey(firebaseApiKey)
            }
        }
        
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {

        Box {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    isDropDownExpanded.value = true
                }
            ) {

                // Show loading indicator, model name, or error message
                when (configState) {
                    is FirebaseConfigManager.ConfigState.Loading -> {
                        Text(
                            text = "Loading...",
                            fontSize = 20.sp,
                            fontFamily = FontFamily(Font(R.font.typographica)),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    is FirebaseConfigManager.ConfigState.Error -> {
                        Text(
                            text = "Firebase Required",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.typographica)),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {
                        if (currentModels.isNotEmpty() && itemPosition.value < currentModels.size) {
                            Text(
                                text = currentModels[itemPosition.value].displayName,
                                fontSize = 18.sp,
                                fontFamily = FontFamily(Font(R.font.typographica)),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = "Setup Firebase",
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.typographica)),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                Image(
                    painter = painterResource(id = R.drawable.drop_down_ic),
                    contentDescription = "DropDown Icon"
                )

                // Set initial model
                if (currentModels.isNotEmpty() && itemPosition.value < currentModels.size) {
                    ChatData.selected_model = currentModels[itemPosition.value].apiModel
                }
            }

            DropdownMenu(
                expanded = isDropDownExpanded.value,
                onDismissRequest = {
                    isDropDownExpanded.value = false
                }) {
                val context = LocalContext.current

                if (currentModels.isEmpty()) {
                    // Show message when Firebase not configured
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "⚠️ Firebase not configured\nPlease set up Firebase with models and API key",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            isDropDownExpanded.value = false
                            Toast.makeText(context, "Please configure Firebase first", Toast.LENGTH_LONG).show()
                        }
                    )
                } else {
                    currentModels.forEachIndexed { index, model ->
                        DropdownMenuItem(
                            text = {
                                Text(text = model.displayName)
                            },
                            onClick = {
                                isDropDownExpanded.value = false
                                itemPosition.value = index
                                
                                // Update the selected model
                                ChatData.selected_model = model.apiModel

                                // Handle unavailable models
                                if (!model.isAvailable) {
                                    Toast.makeText(context, "Model temporarily unavailable", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }



    }



// chat screen backgrounds
    @Composable
    fun BigHi(modifier: Modifier = Modifier) {
        Box(
            modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        )  {

//            chat screen background wallpaper
            Image(
                alignment = Alignment.Center,
                painter = painterResource(id = R.drawable.background),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

//            Startup big hi  animation
            val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.gassist))
            LottieAnimation(
                composition = composition,
                modifier = Modifier
                    .size(500.dp)
                    .align(Alignment.Center)
                    .shadow(300.dp),
//                iterations = LottieConstants.IterateForever // Play in loop
//                progress = {}
            )


        }
    }





}
