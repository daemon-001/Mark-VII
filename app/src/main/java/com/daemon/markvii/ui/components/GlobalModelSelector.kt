package com.daemon.markvii.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daemon.markvii.ApiProvider
import com.daemon.markvii.data.GlobalModelInfo
import com.daemon.markvii.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalModelSelectorBottomSheet(
    globalModels: List<GlobalModelInfo>,
    currentApiProvider: ApiProvider,
    onProviderSelected: (ApiProvider) -> Unit,
    onModelSelected: (GlobalModelInfo) -> Unit,
    onDismissRequest: () -> Unit,
    hasImage: Boolean,
    selectedModelId: String,
    onReloadModels: (() -> Unit)? = null,
    isLoadingModels: Boolean = false
) {
    val appColors = LocalAppColors.current
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val bookmarkedModels by com.daemon.markvii.data.UserApiPreferences.bookmarkedModels.collectAsState()
    
    val filteredModels = remember(globalModels, currentApiProvider, searchQuery, bookmarkedModels) {
        val filtered = if (searchQuery.isBlank()) {
            globalModels.filter { it.provider == currentApiProvider }
        } else {
            globalModels.filter { it.displayName.contains(searchQuery, ignoreCase = true) }
        }
        filtered.sortedByDescending { bookmarkedModels.contains(it.apiModel) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            // Header with Search
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search models...", color = appColors.textSecondary) },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = "Search", tint = appColors.textSecondary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = appColors.textSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = appColors.surfaceVariant,
                        unfocusedContainerColor = appColors.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            // API Provider Tabs (only show when not searching)
            if (searchQuery.isBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProviderTab(
                        title = "Gemini",
                        isSelected = currentApiProvider == ApiProvider.GEMINI,
                        onClick = { onProviderSelected(ApiProvider.GEMINI) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ProviderTab(
                        title = "OpenRouter",
                        isSelected = currentApiProvider == ApiProvider.OPENROUTER,
                        onClick = { onProviderSelected(ApiProvider.OPENROUTER) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ProviderTab(
                        title = "Groq",
                        isSelected = currentApiProvider == ApiProvider.GROQ,
                        onClick = { onProviderSelected(ApiProvider.GROQ) },
                        modifier = Modifier.weight(1f)
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

            if (isLoadingModels) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = appColors.accent)
                }
            } else if (filteredModels.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "No models available for ${currentApiProvider.name}" else "No models found matching '$searchQuery'",
                        color = appColors.textSecondary,
                        fontSize = 14.sp
                    )
                    
                    if (searchQuery.isBlank() && onReloadModels != null) {
                        Button(
                            onClick = onReloadModels,
                            colors = ButtonDefaults.buttonColors(containerColor = appColors.accent.copy(alpha = 0.15f)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, tint = appColors.accent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reload Models", color = appColors.accent, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (searchQuery.isBlank() && onReloadModels != null) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                TextButton(onClick = onReloadModels) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = appColors.accent)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reload", color = appColors.accent, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    itemsIndexed(filteredModels) { index, model ->
                        val isSelected = model.apiModel == selectedModelId
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) appColors.accent.copy(alpha = 0.1f)
                                    else appColors.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .clickable {
                                    if (hasImage && model.provider != ApiProvider.GEMINI) {
                                        Toast.makeText(context, "Cannot use ${model.provider.name} with images. Switch to Gemini.", Toast.LENGTH_SHORT).show()
                                    } else if (!model.isAvailable) {
                                        Toast.makeText(context, "Model temporarily unavailable", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onModelSelected(model)
                                        onDismissRequest()
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = model.displayName,
                                        color = if (isSelected) appColors.accent else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    if (model.isPro) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        ChipBadge(text = "PRO", color = appColors.accent)
                                    }
                                    
                                }
                                
                                // Subtitle row: Provider + Paid text
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    if (searchQuery.isNotBlank()) {
                                        Text(
                                            text = model.provider.name,
                                            color = appColors.textSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    
                                    if (model.isPaid) {
                                        if (searchQuery.isNotBlank()) {
                                            Text(
                                                text = " • ",
                                                color = appColors.textSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Text(
                                            text = "Paid",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    
                                    if (searchQuery.isNotBlank() || model.isPaid) {
                                        Text(
                                            text = " • ",
                                            color = appColors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    
                                    Text(
                                        text = model.apiModel,
                                        color = appColors.textSecondary.copy(alpha = 0.7f),
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                            
                            // Bookmark Star Icon
                            IconButton(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                onClick = {
                                    val currentlyBookmarked = bookmarkedModels.contains(model.apiModel)
                                    com.daemon.markvii.data.UserApiPreferences.toggleModelBookmark(model.apiModel)
                                    val message = if (!currentlyBookmarked) "Bookmarked ${model.displayName}" else "Removed bookmark"
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                val isBookmarked = bookmarkedModels.contains(model.apiModel)
                                Icon(
                                    imageVector = if (isBookmarked) androidx.compose.material.icons.Icons.Rounded.Star else androidx.compose.material.icons.Icons.Rounded.StarBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isBookmarked) appColors.accent else appColors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) appColors.accent.copy(alpha = 0.2f)
                else appColors.surfaceTertiary
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) appColors.accent else appColors.textSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun ChipBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
