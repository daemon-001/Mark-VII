package com.daemon.markvii

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Language
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.daemon.markvii.ui.theme.LocalAppColors

@Composable
fun InfoSetting() {
    val appColors = LocalAppColors.current
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        // Card 1: App Info
        Surface(
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mini_logo),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(72.dp)
                    )
                    Column {
                            Text(
                                text = "MARK VI I",
                                fontSize = 24.sp,
                                fontFamily = FontFamily(Font(R.font.typographica)),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Next-generation multi-model AI assistant",
                                fontSize = 14.sp,
                                color = appColors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = appColors.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "v3.3.7 (30370)",
                                    fontSize = 12.sp,
                                    color = appColors.textSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "About Mark VII",
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.typographica)),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Mark VII provides the ultimate experience of using multiple industry-leading AI models all in one place. Seamlessly switch between models from Google, OpenAI, Anthropic, and more within a single unified interface. Enjoy advanced features like voice interaction, real-time vision capabilities, and effortless PDF exports to supercharge your productivity.",
                        fontSize = 14.sp,
                        color = appColors.textPrimary,
                        fontFamily = FontFamily.Default,
                        lineHeight = 20.sp
                    )
                }
            }

        // Card 2: Open Source
        Surface(
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = "Open Source",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Open Source",
                            fontFamily = FontFamily(Font(R.font.typographica)),
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "This is a proud open-source project! We believe in transparent development and community collaboration. You can view the full source code, report issues, or contribute to the project on our GitHub repository.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = appColors.textPrimary,
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    androidx.compose.material3.OutlinedButton(
                        onClick = { uriHandler.openUri("https://github.com/daemon-001/Mark-VII") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.Code,
                            contentDescription = "Code",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View GitHub Repository", fontWeight = FontWeight.Medium)
                    }
                }
            }

        // Card 3: Contact & Developer
        Surface(
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Developer Contact",
                        fontFamily = FontFamily(Font(R.font.typographica)),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { uriHandler.openUri("https://www.linkedin.com/in/daemon001") },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(appColors.surfaceVariant)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.linkedin),
                                contentDescription = "LinkedIn",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = { uriHandler.openUri("https://github.com/daemon-001") },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(appColors.surfaceVariant)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.github),
                                contentDescription = "GitHub",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = { uriHandler.openUri("https://devwizpro.com/") },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(appColors.surfaceVariant)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Rounded.Language,
                                contentDescription = "Website",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}




