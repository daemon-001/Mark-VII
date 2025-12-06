package com.daemon.markvii.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daemon.markvii.ui.theme.LocalAppColors

@Composable
fun PromptSuggestionBubbles(onSuggestionClick: (String) -> Unit) {
    val appColors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Title
        Text(
            text = "Getting Started",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        
        // Instructions
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InstructionStep(
                number = "1",
                text = "Select your desired AI model"
            )
            
            InstructionStep(
                number = "2",
                text = "Type your message in the text box below"
            )
            
            InstructionStep(
                number = "3",
                text = "Tap the send button to get Model responses"
            )
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        
        // Response Actions section
        Text(
            text = "Response Actions",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionItem(
                icon = "📋",
                text = "Copy the response text to clipboard"
            )
            ActionItem(
                icon = "🔊",
                text = "Listen to the response with text-to-speech"
            )
            ActionItem(
                icon = "🔄",
                text = "Regenerate the response using a different model"
            )
            ActionItem(
                icon = "📤",
                text = "Share the response with other apps"
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Tips section
        Text(
            text = "💡 Tips",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = appColors.accent,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TipItem("All models are completely free to use")
            TipItem("Different models excel at different tasks")
            TipItem("You can switch models anytime during conversation")
        }
    }
}
