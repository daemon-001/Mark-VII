package com.daemon.markvii.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.daemon.markvii.ui.theme.AppColors
import kotlinx.coroutines.launch

@Composable
fun UserApiConfigItem(
    title: String,
    apiKey: String,
    isEnabled: Boolean,
    getKeyUrl: String,
    onKeyChanged: (String) -> Unit,
    onEnabledChanged: (Boolean) -> Unit,
    onVerify: suspend (String) -> Boolean,
    appColors: AppColors
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    
    // Reset error when key changes
    LaunchedEffect(apiKey) {
        if (isError) isError = false
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        OutlinedTextField(
            value = apiKey,
            onValueChange = onKeyChanged,
            label = { Text(title, color = if (isError) appColors.error else appColors.textSecondary, fontSize = 13.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrect = false,
                keyboardType = KeyboardType.Text
            ),
            isError = isError,
            supportingText = if (isError) {
                { Text("Invalid API Key", color = appColors.error, fontSize = 12.sp) }
            } else null,
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).padding(4.dp),
                        strokeWidth = 2.dp,
                        color = appColors.accent
                    )
                } else if (apiKey.isNotEmpty()) {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                // Validate before enabling
                                isLoading = true
                                isError = false
                                coroutineScope.launch {
                                    val isValid = onVerify(apiKey.trim())
                                    isLoading = false
                                    if (isValid) {
                                        onEnabledChanged(true)
                                    } else {
                                        isError = true
                                        onEnabledChanged(false)
                                    }
                                }
                            } else {
                                onEnabledChanged(false)
                            }
                        },
                        modifier = Modifier.scale(0.8f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = appColors.accent,
                            checkedTrackColor = appColors.accent.copy(alpha = 0.5f),
                            uncheckedThumbColor = appColors.textSecondary,
                            uncheckedTrackColor = appColors.surfaceTertiary
                        )
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = appColors.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = appColors.surfaceVariant.copy(alpha = 0.5f),
                focusedIndicatorColor = appColors.accent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = appColors.accent,
                errorContainerColor = appColors.surfaceVariant.copy(alpha = 0.5f),
                errorIndicatorColor = appColors.error
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        Text(
            text = "Get API key here",
            fontSize = 12.sp,
            color = appColors.accent,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getKeyUrl))
                    context.startActivity(intent)
                }
                .padding(top = 4.dp, end = 4.dp)
        )
    }
}
