package com.daemon.markvii.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daemon.markvii.ApiProvider
import com.daemon.markvii.R

import com.daemon.markvii.data.ChatData
import com.daemon.markvii.data.ModelInfo
import com.daemon.markvii.ui.theme.LocalAppColors
import com.daemon.markvii.utils.PdfGenerator
import kotlinx.coroutines.delay

// model chat text bubble
@Composable
fun ModelChatItem(
    response: String,
    userPrompt: String = "",
    modelUsed: String = "",
    onRetry: (String) -> Unit = {},
    isStreaming: Boolean = false,
    freeModels: List<ModelInfo> = emptyList(),
    geminiModels: List<ModelInfo> = emptyList(),
    groqModels: List<ModelInfo> = emptyList(),
    currentApiProvider: ApiProvider = ApiProvider.GEMINI,
    hasImage: Boolean = false,
    isError: Boolean = false,
    onApiSwitch: (ApiProvider) -> Unit = {},
    isTtsSpeaking: Boolean,
    onStopTts: () -> Unit,
    onSpeak: (String) -> Unit,
    isTtsReady: Boolean,
    retryOfPrompt: String? = null,       // non-null = this is a retry bubble; value = source prompt text
    onScrollToPrompt: () -> Unit = {}    // called when user taps the source prompt tag
) {
    val appColors = LocalAppColors.current
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    var showModelSelector by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var selectedApiProvider by remember { mutableStateOf(currentApiProvider) }
    
    // Unified Smooth Streaming Engine
    var displayedText by remember { mutableStateOf("") }
    val currentResponse by rememberUpdatedState(response)
    
    LaunchedEffect(isStreaming) {
        if (isStreaming) {
            displayedText = ""
            var lastTargetLength = 0      // last seen network buffer length
            var lastHapticMs = 0L         // debounce gate
            while (true) {
                val target = currentResponse
                val current = displayedText

                // Detect new chunk from server: network buffer grew since last tick
                val targetGrew = target.length > lastTargetLength
                if (targetGrew) {
                    lastTargetLength = target.length
                    val now = System.currentTimeMillis()
                    if (now - lastHapticMs >= 50L) {
                        // New server data arrived — fire haptic synced to this chunk
                        hapticFeedback.performHapticFeedback(
                            androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove
                        )
                        lastHapticMs = now
                    }
                }

                if (current.length < target.length) {
                    val diff = target.length - current.length
                    val (charsToProcess, delayMs) = when {
                        diff > 50 -> 5 to 5L
                        diff > 20 -> 2 to 10L
                        diff > 5  -> 1 to 15L
                        else      -> 1 to 30L
                    }
                    val nextIndex = (current.length + charsToProcess).coerceAtMost(target.length)
                    displayedText = target.substring(0, nextIndex)
                    delay(delayMs)
                } else {
                    delay(50)
                }
            }
        } else if (response.isNotEmpty()) {
            // Streaming stopped — ensure final text is shown
            displayedText = response
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
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.typographica)),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (modelUsed.isNotEmpty()) {
                    Text(
                        text = modelUsed.replace(":free", ""),
                        fontSize = 13.sp,
                        color = appColors.textSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                // Retry source-prompt tag chip — tapping scrolls to the original prompt
                if (retryOfPrompt != null) {
                    val shortPrompt = if (retryOfPrompt.length > 40)
                        retryOfPrompt.take(40) + "…" else retryOfPrompt
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 6.dp, bottom = 2.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(appColors.surfaceTertiary)
                            .clickable(
                                onClick = onScrollToPrompt,
                                indication = null,
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Retried from",
                            tint = appColors.textSecondary,
                            modifier = Modifier
                                .size(15.dp)
                                .padding(end = 0.dp)
                        )
                        Text(
                            text = "  $shortPrompt",
                            fontSize = 15.sp,
                            color = appColors.textSecondary,
                            maxLines = 1
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                // Render text — StreamingMarkdown during streaming (pure Compose, no flicker)
                // MarkdownWithCodeCopy after completion (full Markwon renderer)
                if (displayedText.isNotEmpty()) {
                    if (isError) {
                        Text(
                            text = displayedText,
                            fontSize = 16.sp,
                            color = appColors.error,
                            fontFamily = FontFamily.Monospace
                        )
                    } else if (isStreaming) {
                        // Pure-Compose renderer: no AndroidView = no flicker every 30ms
                        StreamingMarkdown(text = displayedText)
                    } else {
                        // Full renderer with tables, links, etc. after streaming is done
                        MarkdownWithCodeCopy(response = displayedText, context = context)
                    }
                }
                
                // Blinking cursor shown below the streaming text
                if (isStreaming) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        val infTrans = rememberInfiniteTransition(label = "cursor")
                        val cursorAlpha by infTrans.animateFloat(
                            initialValue = 0.3f, targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ), label = "ca"
                        )
                        Text(
                            text = "▋",
                            color = appColors.accent.copy(alpha = cursorAlpha),
                            fontSize = 14.sp
                        )
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
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy",
                    tint = appColors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Speak button
            
            IconButton(
                onClick = {
                    if (isTtsReady) {
                        if (isTtsSpeaking) {
                            onStopTts()
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
                            
                            onSpeak(cleanText)
                            Toast.makeText(context, "Speaking...", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Text-to-speech not ready", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.size(32.dp)
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
                            .size(16.dp)
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
                        modifier = Modifier.size(16.dp)
                    )
                }
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
                    tint = appColors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Export PDF button
            IconButton(
                onClick = { showExportDialog = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PictureAsPdf,
                    contentDescription = "Export PDF",
                    tint = appColors.textSecondary,
                    modifier = Modifier.size(16.dp)
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
                    tint = appColors.textSecondary,
                    modifier = Modifier.size(16.dp)
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

                            Spacer(modifier = Modifier.width(8.dp))

                            // Groq button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selectedApiProvider == ApiProvider.GROQ)
                                            appColors.accent.copy(alpha = 0.2f)
                                        else
                                            appColors.surfaceVariant
                                    )
                                    .clickable {
                                        if (hasImage) {
                                            Toast.makeText(
                                                context,
                                                "⚠️ Groq doesn't support images. Please use Gemini for image queries.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            selectedApiProvider = ApiProvider.GROQ
                                            onApiSwitch(ApiProvider.GROQ)
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Groq",
                                    color = if (selectedApiProvider == ApiProvider.GROQ)
                                        appColors.accent
                                    else
                                        appColors.textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedApiProvider == ApiProvider.GROQ)
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
                        val currentModels = when (selectedApiProvider) {
                            ApiProvider.GEMINI -> geminiModels
                            ApiProvider.OPENROUTER -> freeModels
                            ApiProvider.GROQ -> groqModels
                        }
                        
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
