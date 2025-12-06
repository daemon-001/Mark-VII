package com.daemon.markvii.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.daemon.markvii.ui.theme.LocalAppColors

// Syntax highlighting for code blocks
@Composable
fun HighlightedCodeText(code: String) {
    val appColors = LocalAppColors.current
    val annotatedString = buildAnnotatedString {
        val text = code.trim()
        append(text)
        
        // Define color scheme based on theme
        val isDark = appColors.textPrimary.luminance() > 0.5f
        val keywordColor = if (isDark) Color(0xFF569CD6) else Color(0xFF0000FF)
        val stringColor = if (isDark) Color(0xFFCE9178) else Color(0xFFA31515)
        val commentColor = if (isDark) Color(0xFF6A9955) else Color(0xFF008000)
        val numberColor = if (isDark) Color(0xFFB5CEA8) else Color(0xFF098658)
        val functionColor = if (isDark) Color(0xFFDCDCAA) else Color(0xFF795E26)
        
        // Keywords
        val keywords = listOf(
            "fun", "val", "var", "class", "object", "interface", "enum", "when", "if", "else", "for", "while", "do",
            "return", "break", "continue", "try", "catch", "finally", "throw", "import", "package",
            "public", "private", "protected", "internal", "abstract", "final", "open", "override",
            "const", "data", "sealed", "inline", "suspend", "lateinit", "companion",
            "function", "let", "async", "await", "export", "default", "new", "this", "super",
            "int", "string", "bool", "void", "null", "true", "false", "undefined", "def", "from", "as"
        )
        
        // Apply highlighting patterns
        val patterns = listOf(
            Regex("\"([^\"\\\\]|\\\\.)*\"") to stringColor,
            Regex("'([^'\\\\]|\\\\.)*'") to stringColor,
            Regex("//.*") to commentColor,
            Regex("/\\*[\\s\\S]*?\\*/") to commentColor,
            Regex("#.*") to commentColor,
            Regex("\\b\\d+(\\.\\d+)?\\b") to numberColor,
            Regex("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(") to functionColor,
            Regex("\\b(${keywords.joinToString("|")})\\b") to keywordColor
        )
        
        patterns.forEach { (pattern, color) ->
            pattern.findAll(text).forEach { match ->
                addStyle(
                    style = SpanStyle(color = color),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
        }
    }
    
    Text(
        text = annotatedString,
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        ),
        softWrap = false,
        maxLines = Int.MAX_VALUE
    )
}
