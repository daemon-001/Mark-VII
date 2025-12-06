package com.daemon.markvii.ui

import android.graphics.Bitmap
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.*
import com.daemon.markvii.R
import com.daemon.markvii.ApiProvider
import com.daemon.markvii.data.ChatData
import com.daemon.markvii.data.ModelInfo
import com.daemon.markvii.ui.components.ModelChatItem
import com.daemon.markvii.ui.components.PromptSuggestionBubbles
import com.daemon.markvii.ui.components.UserChatItem
import com.daemon.markvii.ui.theme.LocalAppColors
import com.daemon.markvii.ChatUiEvent
import com.daemon.markvii.ChatViewModel
import com.daemon.markvii.data.FirebaseConfigManager
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ChatScreen(
    paddingValues: PaddingValues,
    bitmap: Bitmap?,
    voiceInput: String,
    voicePitch: Float,
    isListening: Boolean,
    isTtsSpeaking: Boolean,
    isTtsReady: Boolean,
    onVoiceInputConsumed: () -> Unit,
    onRemoveImage: () -> Unit,
    onStopTts: () -> Unit,
    onSpeak: (String) -> Unit,
    onMicClick: () -> Unit,
    onImagePickerClick: () -> Unit
) {
    val chaViewModel = viewModel<ChatViewModel>()
    val chatState = chaViewModel.chatState.collectAsState().value
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
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        iterations = LottieConstants.IterateForever
                    )
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(100.dp)
                    )
                    Text(
                        text = "Fetching models...",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = { }
        )
    }
    
    // Observe error state from ViewModel
    val currentError = chatState.error

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
            onVoiceInputConsumed()
        }
    }

    // Auto-scroll to bottom when new message appears or when generating
    LaunchedEffect(chatState.chatList.size, chatState.isGeneratingResponse) {
        if (chatState.chatList.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }
    
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
                            },
                            isTtsSpeaking = isTtsSpeaking,
                            isTtsReady = isTtsReady,
                            onStopTts = onStopTts,
                            onSpeak = onSpeak
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
                .navigationBarsPadding()
                .imePadding()
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
                                    onRemoveImage()
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
                                            indication = androidx.compose.material3.ripple(
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
                                        onImagePickerClick()
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
                                    onMicClick()
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
                                            onRemoveImage()
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
