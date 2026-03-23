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
import androidx.compose.ui.text.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daemon.markvii.ui.theme.LocalAppColors
import dev.jeziellago.compose.markdowntext.MarkdownText

/**
 * Parses a plain-text span into an AnnotatedString with inline markdown:
 *   **bold**, *italic*, `code`, ~~strikethrough~~
 * Pure Compose — no AndroidView, no flicker.
 */
private fun parseInlineMarkdown(text: String, codeBackground: androidx.compose.ui.graphics.Color): AnnotatedString =
    buildAnnotatedString {
        val src = text
        var i = 0
        while (i < src.length) {
            when {
                // Bold + italic ***text***
                src.startsWith("***", i) -> {
                    val end = src.indexOf("***", i + 3)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                            append(src.substring(i + 3, end))
                        }
                        i = end + 3
                    } else { append(src[i]); i++ }
                }
                // Bold **text**
                src.startsWith("**", i) -> {
                    val end = src.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(src.substring(i + 2, end))
                        }
                        i = end + 2
                    } else { append(src[i]); i++ }
                }
                // Italic *text*
                src.startsWith("*", i) && (i + 1 < src.length && src[i + 1] != ' ') -> {
                    val end = src.indexOf("*", i + 1)
                    if (end != -1 && end > i + 1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(src.substring(i + 1, end))
                        }
                        i = end + 1
                    } else { append(src[i]); i++ }
                }
                // Inline code `code`
                src.startsWith("`", i) -> {
                    val end = src.indexOf("`", i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = codeBackground, fontSize = 14.sp)) {
                            append(src.substring(i + 1, end))
                        }
                        i = end + 1
                    } else { append(src[i]); i++ }
                }
                // Strikethrough ~~text~~
                src.startsWith("~~", i) -> {
                    val end = src.indexOf("~~", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)) {
                            append(src.substring(i + 2, end))
                        }
                        i = end + 2
                    } else { append(src[i]); i++ }
                }
                else -> { append(src[i]); i++ }
            }
        }
    }

/**
 * Pure-Compose streaming markdown renderer.
 * Used DURING streaming to avoid AndroidView/Markwon flicker.
 * Supports: # headers, ``` code fences, **bold**, *italic*, `inline code`, ~~strike~~
 * After streaming completes, ModelChatItem switches to MarkdownWithCodeCopy for full rendering.
 */
@Composable
fun StreamingMarkdown(text: String) {
    val appColors = LocalAppColors.current
    val blocks = remember(text) { parseMarkdownBlocks(text) }
    val codeBackground = appColors.surfaceTertiary

    Column(modifier = Modifier.fillMaxWidth()) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    val (size, topPad, weight) = when (block.level) {
                        1 -> Triple(20.sp, 12.dp, FontWeight.Bold)
                        2 -> Triple(18.sp, 10.dp, FontWeight.SemiBold)
                        3 -> Triple(17.sp, 8.dp, FontWeight.SemiBold)
                        else -> Triple(16.sp, 6.dp, FontWeight.Medium)
                    }
                    Text(
                        text = block.text,
                        fontSize = size,
                        fontWeight = weight,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = (size.value + 6).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = topPad, bottom = 4.dp)
                    )
                }
                is MarkdownBlock.CodeFence -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(appColors.surfaceTertiary)
                            .padding(12.dp)
                    ) {
                        if (block.language.isNotBlank() && block.language != "code") {
                            Text(
                                text = block.language,
                                style = TextStyle(
                                    color = appColors.textSecondary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())) {
                            Text(
                                text = block.code.trim(),
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
                is MarkdownBlock.Body -> {
                    if (block.text.isNotBlank()) {
                        // Render bullet list lines individually, rest as annotated string
                        val lines = block.text.lines()
                        Column(modifier = Modifier.fillMaxWidth()) {
                            lines.forEach { line ->
                                when {
                                    line.trimStart().startsWith("- ") || line.trimStart().startsWith("• ") -> {
                                        val content = line.trimStart().removePrefix("- ").removePrefix("• ")
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 1.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "•  ",
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 22.sp
                                            )
                                            Text(
                                                text = parseInlineMarkdown(content, codeBackground),
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 22.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                    line.isBlank() -> Spacer(modifier = Modifier.height(4.dp))
                                    else -> {
                                        Text(
                                            text = parseInlineMarkdown(line, codeBackground),
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 22.sp,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// Sealed type for parsed markdown blocks
private sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class CodeFence(val code: String, val language: String) : MarkdownBlock()
    data class Body(val text: String) : MarkdownBlock()
}

// Parse raw markdown into blocks: headers, code fences, and regular body text
private fun parseMarkdownBlocks(md: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = md.lines()
    var i = 0
    val bodyBuffer = StringBuilder()

    fun flushBody() {
        val t = bodyBuffer.toString().trimEnd()
        if (t.isNotBlank()) blocks.add(MarkdownBlock.Body(t))
        bodyBuffer.clear()
    }

    while (i < lines.size) {
        val line = lines[i]
        when {
            // Code fence
            line.trimStart().startsWith("```") -> {
                flushBody()
                val lang = line.trimStart().removePrefix("```").trim().ifEmpty { "code" }
                val codeLines = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                    codeLines.appendLine(lines[i])
                    i++
                }
                blocks.add(MarkdownBlock.CodeFence(codeLines.toString().trimEnd(), lang))
                i++ // skip closing ```
            }
            // ATX headers (# ## ### #### only)
            line.startsWith("#### ") -> { flushBody(); blocks.add(MarkdownBlock.Header(4, line.removePrefix("#### ").trim())); i++ }
            line.startsWith("### ") -> { flushBody(); blocks.add(MarkdownBlock.Header(3, line.removePrefix("### ").trim())); i++ }
            line.startsWith("## ") -> { flushBody(); blocks.add(MarkdownBlock.Header(2, line.removePrefix("## ").trim())); i++ }
            line.startsWith("# ") -> { flushBody(); blocks.add(MarkdownBlock.Header(1, line.removePrefix("# ").trim())); i++ }
            else -> { bodyBuffer.appendLine(line); i++ }
        }
    }
    flushBody()
    return blocks
}

// Custom Markdown renderer with copy buttons for code blocks and controlled header sizes
@Composable
fun MarkdownWithCodeCopy(response: String, context: android.content.Context) {
    val appColors = LocalAppColors.current
    val blocks = remember(response) { parseMarkdownBlocks(response) }

    Column(modifier = Modifier.fillMaxWidth()) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    // Controlled header sizes — much smaller than default MarkdownText rendering
                    val (size, topPad, weight) = when (block.level) {
                        1 -> Triple(20.sp, 12.dp, FontWeight.Bold)
                        2 -> Triple(18.sp, 10.dp, FontWeight.SemiBold)
                        3 -> Triple(17.sp, 8.dp, FontWeight.SemiBold)
                        else -> Triple(16.sp, 6.dp, FontWeight.Medium)
                    }
                    Text(
                        text = block.text,
                        fontSize = size,
                        fontWeight = weight,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = (size.value + 6).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = topPad, bottom = 4.dp)
                    )
                }

                is MarkdownBlock.CodeFence -> {
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
                            Text(
                                text = block.language,
                                style = TextStyle(
                                    color = appColors.textSecondary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                SelectionContainer {
                                    HighlightedCodeText(code = block.code.trim())
                                }
                            }
                        }
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Code", block.code.trim())
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
                }

                is MarkdownBlock.Body -> {
                    if (block.text.isNotBlank()) {
                        MarkdownText(
                            markdown = block.text,
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

