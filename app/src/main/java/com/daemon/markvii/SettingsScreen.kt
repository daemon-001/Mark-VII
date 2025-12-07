package com.daemon.markvii


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daemon.markvii.data.AppTheme
import com.daemon.markvii.data.ThemePreferences
import com.daemon.markvii.ui.theme.LocalAppColors
import com.daemon.markvii.ui.UserApiConfigItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onSignOut: () -> Unit,
    onThemeChanged: (AppTheme) -> Unit = {},
    isUserAuthenticated: Boolean = false
) {
    var selectedTheme by remember { mutableStateOf(ThemePreferences.getTheme()) }
    var showThemeMenu by remember { mutableStateOf(false) }
    val appColors = LocalAppColors.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Appearance Section
            Text(
                text = "Appearance",
                fontSize = 14.sp,
                color = appColors.textSecondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            
            // Theme Selector
            Box {
                Button(
                    onClick = { showThemeMenu = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appColors.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Theme",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when (selectedTheme) {
                                    AppTheme.SYSTEM_DEFAULT -> "System Default"
                                    AppTheme.LIGHT -> "Light"
                                    AppTheme.DARK -> "Dark"
                                },
                                fontSize = 13.sp,
                                color = appColors.textSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = "Select theme",
                            tint = appColors.textSecondary
                        )
                    }
                }
                
                // Theme dropdown menu
                DropdownMenu(
                    expanded = showThemeMenu,
                    onDismissRequest = { showThemeMenu = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .width(200.dp)
                ) {
                    listOf(
                        AppTheme.SYSTEM_DEFAULT to "System Default",
                        AppTheme.LIGHT to "Light",
                        AppTheme.DARK to "Dark"
                    ).forEach { (theme, label) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = label,
                                    color = if (selectedTheme == theme) appColors.accent else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (selectedTheme == theme) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                selectedTheme = theme
                                ThemePreferences.setTheme(theme)
                                onThemeChanged(theme)
                                showThemeMenu = false
                            },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        )
                    }
                }
            }
            
            // Your APIs Section
            Text(
                text = "Your APIs",
                fontSize = 14.sp,
                color = appColors.textSecondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            
            val geminiKey by com.daemon.markvii.data.UserApiPreferences.geminiApiKey.collectAsState()
            val isGeminiEnabled by com.daemon.markvii.data.UserApiPreferences.isGeminiKeyEnabled.collectAsState()
            val openRouterKey by com.daemon.markvii.data.UserApiPreferences.openRouterApiKey.collectAsState()
            val isOpenRouterEnabled by com.daemon.markvii.data.UserApiPreferences.isOpenRouterKeyEnabled.collectAsState()
            val context = androidx.compose.ui.platform.LocalContext.current
            
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Gemini API Config
                UserApiConfigItem(
                    title = "Gemini API Key",
                    apiKey = geminiKey,
                    isEnabled = isGeminiEnabled,
                    getKeyUrl = "https://aistudio.google.com/app/apikey",
                    onKeyChanged = { com.daemon.markvii.data.UserApiPreferences.setGeminiApiKey(it) },
                    onEnabledChanged = { com.daemon.markvii.data.UserApiPreferences.setGeminiKeyEnabled(it) },
                    onVerify = { key -> com.daemon.markvii.data.GeminiClient.verifyKey(key) },
                    appColors = appColors
                )
                
                // OpenRouter API Config
                UserApiConfigItem(
                    title = "OpenRouter API Key",
                    apiKey = openRouterKey,
                    isEnabled = isOpenRouterEnabled,
                    getKeyUrl = "https://openrouter.ai/keys",
                    onKeyChanged = { com.daemon.markvii.data.UserApiPreferences.setOpenRouterApiKey(it) },
                    onEnabledChanged = { com.daemon.markvii.data.UserApiPreferences.setOpenRouterKeyEnabled(it) },
                    onVerify = { key -> com.daemon.markvii.data.OpenRouterClient.verifyKey(key) },
                    appColors = appColors
                )
            }
            if (isUserAuthenticated) {
                Text(
                    text = "Account",
                    fontSize = 14.sp,
                    color = appColors.textSecondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                
                // Logout Button
                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appColors.surfaceVariant,
                        contentColor = appColors.error
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ExitToApp,
                            contentDescription = "Logout",
                            tint = appColors.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Logout",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
