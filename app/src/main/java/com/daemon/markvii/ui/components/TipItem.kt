package com.daemon.markvii.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daemon.markvii.ui.theme.LocalAppColors

@Composable
fun TipItem(text: String) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            color = appColors.textSecondary,
            fontSize = 14.sp
        )
        
        Text(
            text = text,
            color = appColors.textSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}
