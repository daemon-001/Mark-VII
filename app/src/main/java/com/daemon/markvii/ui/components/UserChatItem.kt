package com.daemon.markvii.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daemon.markvii.ui.theme.LocalAppColors

// user chat text bubble
@Composable
fun UserChatItem(prompt: String, bitmap: Bitmap?) {
    val appColors = LocalAppColors.current
    
    SelectionContainer() {
        Column(
            modifier = Modifier
                .padding(start = 50.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.End

        ) {
//                user picked image display
            bitmap?.let {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
//                            .height(260.dp)
                        .padding(bottom = 2.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentDescription = "image",
                    contentScale = ContentScale.FillWidth,
                    bitmap = it.asImageBitmap()
                )
            }

//                user prompt display
            Text(
                modifier = Modifier
//                        .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(appColors.surfaceVariant)
                    .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp),
//                    textAlign = TextAlign.Right,
                text = prompt,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )

        }
    }

}
