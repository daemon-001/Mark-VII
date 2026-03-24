package com.daemon.markvii.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daemon.markvii.ui.theme.LocalAppColors

@Composable
fun UserChatItem(prompt: String, bitmap: Bitmap?, isBlinking: Boolean = false) {
    val appColors = LocalAppColors.current
    val shape = RoundedCornerShape(20.dp)

    // When blinking: pulse an accent border on the bubble so the user sees which prompt was referenced
    val blinkAlpha by animateFloatAsState(
        targetValue = if (isBlinking) 1f else 0f,
        animationSpec = if (isBlinking)
            infiniteRepeatable(
                animation = tween(250, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        else
            tween(200),
        label = "blink"
    )

    SelectionContainer {
        Column(
            modifier = Modifier
                .padding(start = 50.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            bitmap?.let {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp)
                        .clip(shape),
                    contentDescription = "image",
                    contentScale = ContentScale.FillWidth,
                    bitmap = it.asImageBitmap()
                )
            }

            Text(
                modifier = Modifier
                    .clip(shape)
                    .background(appColors.surfaceVariant)
                    .then(
                        if (isBlinking)
                            Modifier.border(
                                width = 2.dp,
                                color = appColors.accent.copy(alpha = blinkAlpha),
                                shape = shape
                            )
                        else Modifier
                    )
                    .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp),
                text = prompt,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
