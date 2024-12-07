package com.daemon.markvii
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun infotab1(modifier: Modifier = Modifier.fillMaxSize()){
    Text(
        text = "\n\nUnder Development",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )

}


