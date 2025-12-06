package com.daemon.markvii.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daemon.markvii.ui.theme.LocalAppColors
import dev.jeziellago.compose.markdowntext.MarkdownText

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
