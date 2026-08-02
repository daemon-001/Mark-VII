package com.daemon.markvii.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daemon.markvii.data.OpenRouterClient
import com.daemon.markvii.data.OpenRouterKeyInfo
import com.daemon.markvii.data.UserApiPreferences
import com.daemon.markvii.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsagesScreen(onBackClick: () -> Unit) {
    val appColors = LocalAppColors.current
    val context = LocalContext.current

    // API Keys state
    val geminiKey by UserApiPreferences.geminiApiKey.collectAsState()
    val isGeminiEnabled by UserApiPreferences.isGeminiKeyEnabled.collectAsState()
    val openRouterKey by UserApiPreferences.openRouterApiKey.collectAsState()
    val isOpenRouterEnabled by UserApiPreferences.isOpenRouterKeyEnabled.collectAsState()
    val groqKey by UserApiPreferences.groqApiKey.collectAsState()
    val isGroqEnabled by UserApiPreferences.isGroqKeyEnabled.collectAsState()

    // OpenRouter state
    var openRouterUsage by remember { mutableStateOf<OpenRouterKeyInfo?>(null) }
    var isLoadingOpenRouter by remember { mutableStateOf(false) }

    LaunchedEffect(openRouterKey, isOpenRouterEnabled) {
        if (isOpenRouterEnabled && openRouterKey.isNotBlank()) {
            isLoadingOpenRouter = true
            openRouterUsage = OpenRouterClient.getKeyUsage(openRouterKey)
            isLoadingOpenRouter = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "API Usages",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // OpenRouter Card
            if (isOpenRouterEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = appColors.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "OpenRouter",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (isLoadingOpenRouter) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.CenterHorizontally),
                                color = appColors.accent
                            )
                        } else if (openRouterUsage != null) {
                            val usageInfo = openRouterUsage!!
                            UsageRow("Key Label", usageInfo.label ?: "Unknown")
                            UsageRow("Credits Used", "$${String.format("%.4f", usageInfo.usage)}")
                            UsageRow("Credits Limit", if (usageInfo.limit != null) "$${String.format("%.4f", usageInfo.limit)}" else "Unlimited")
                            UsageRow("Tier", if (usageInfo.isFreeTier) "Free" else "Paid")
                        } else {
                            Text(
                                text = "Could not load usage data.",
                                color = appColors.error,
                                fontSize = 14.sp
                            )
                        }
                        
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://openrouter.ai/activity"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View on OpenRouter", color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Rounded.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Gemini Card
            if (isGeminiEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = appColors.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Gemini",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "Info",
                                tint = appColors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Usage metrics are not available programmatically via the chat API key.",
                                color = appColors.textSecondary,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/usage"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View on Google AI Studio", color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Rounded.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Groq Card
            if (isGroqEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = appColors.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Groq",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "Info",
                                tint = appColors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Usage metrics are not available programmatically via the chat API key.",
                                color = appColors.textSecondary,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://console.groq.com/dashboard/usage"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View on Groq Console", color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Rounded.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (!isGeminiEnabled && !isOpenRouterEnabled && !isGroqEnabled) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No APIs enabled.",
                        color = appColors.textSecondary,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun UsageRow(label: String, value: String) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = appColors.textSecondary
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
