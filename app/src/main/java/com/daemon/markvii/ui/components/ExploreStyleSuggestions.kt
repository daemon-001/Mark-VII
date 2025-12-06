package com.daemon.markvii.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daemon.markvii.R
import com.daemon.markvii.ui.theme.LocalAppColors

import com.airbnb.lottie.compose.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreStyleSuggestions(onSuggestionClick: (String) -> Unit) {
    val appColors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Title
        Text(
            text = "MARK VII",
            fontSize = 32.sp,
            fontFamily = FontFamily(Font(R.font.typographica)),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        


        Spacer(modifier = Modifier.height(20.dp))

        // Explore Header
        Text(
            text = "Explore Features",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Categories FlowRow
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val categories = listOf(
                "Gemini 1.5 Pro", "Gemini 1.5 Flash", "OpenRouter Models",
                "Text-to-Speech", "Image Analysis", "PDF Export",
                "Smart History", "Markdown Support", "Voice Input"
            )

            categories.forEach { category ->
                ExploreChip(text = category, onClick = { onSuggestionClick(category) })
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun ExploreChip(text: String, onClick: () -> Unit) {
    val appColors = LocalAppColors.current
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                color = appColors.surfaceVariant.copy(alpha = 0.3f)
            )
            .border(
                width = 1.dp,
                color = appColors.divider.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
