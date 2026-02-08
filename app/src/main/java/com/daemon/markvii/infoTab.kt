package com.daemon.markvii

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    var isVisible by remember { mutableStateOf(false) }

    // Trigger animation on composition
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        // Header card with logo and title
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(300))
        ) {
        Surface(
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AboutAnimation(modifier = Modifier.size(64.dp))
                    Column {
                        Text(
                            text = "MARK VII",
                            fontSize = 22.sp,
                            fontFamily = FontFamily(Font(R.font.typographica)),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Smart multi-provider AI assistant",
                            fontSize = 13.sp,
                            color = appColors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "About Mark VII",
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.typographica)),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Mark VII is a powerful multi-provider AI assistant for Android that brings together the best AI models from Google, OpenAI, Anthropic, and more. Experience seamless conversations with advanced features including voice input, vision capabilities, PDF export, and intelligent context management. Switch between models on-the-fly to get the perfect response for any task.",
                    fontSize = 14.sp,
                    color = appColors.textPrimary,
                    fontFamily = FontFamily.Default,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Developer info
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "About Developer",
                        fontFamily = FontFamily(Font(R.font.typographica)),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = "Developed by Nitesh (a.k.a Daemon)",
                        fontFamily = FontFamily(Font(R.font.merienda_regular)),
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = appColors.textPrimary,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // App version and GitHub link
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "App Version: v3.2.0 (30200)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = appColors.textSecondary,
                    )

                    val annotatedString = buildAnnotatedString {
                        pushStringAnnotation(
                            tag = "URL",
                            annotation = "https://github.com/daemon-001/Mark-VII"
                        )
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("GitHub Repository")
                        }
                        pop()
                    }

                    ClickableText(
                        text = annotatedString,
                        modifier = Modifier.padding(top = 4.dp),
                        onClick = { offset ->
                            annotatedString.getStringAnnotations(
                                tag = "URL",
                                start = offset,
                                end = offset
                            ).firstOrNull()?.let { annotation ->
                                uriHandler.openUri(annotation.item)
                            }
                        }
                    )
                }
            }
        }
        } // End AnimatedVisibility for header card

        Spacer(modifier = Modifier.height(10.dp))

        // Contact card
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(300))
        ) {
        Surface(
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Contact Us",
                    fontFamily = FontFamily(Font(R.font.typographica)),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.padding(4.dp),
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
                        onClick = { uriHandler.openUri("https://www.instagram.com/mustbe_daemon") },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(appColors.surfaceVariant)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.instagram),
                            contentDescription = "Instagram",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
        } // End AnimatedVisibility for contact card

        Spacer(modifier = Modifier.height(8.dp))

    }
}

@Composable
fun AboutAnimation(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.aboutapp))
    LottieAnimation(
        composition = composition,
        modifier = modifier.size(100.dp),
        iterations = LottieConstants.IterateForever
    )
}


