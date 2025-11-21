package com.daemon.markvii
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun InfoSetting() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
//            .padding(16.dp)
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,

    ) {
//        Spacer(modifier = Modifier.height(70.dp))
//        AboutAnimation()
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
//               .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .align(alignment = Alignment.CenterHorizontally)
                .padding(20.dp)
        ){
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                text = "About Mark VII",
                fontSize = 24.sp,
                fontFamily = FontFamily(Font(R.font.typographica)),
                color = Color.White,

                )

            Text(
                modifier = Modifier
                    .padding(10.dp),
//                    textAlign = TextAlign.Right,
                text = "Mark VII is an AI-powered android chatbot application that leverage multiple Natural Language Processing (NLP) models from different companies to provide intelligent and context-aware responses. It integrates top NLP technologies to offer a seamless and advanced conversational experience to users.",
                fontSize = 14.sp,
                color = Color.White,
                fontFamily = FontFamily.Default,
                lineHeight = 17.sp,
//                style = MaterialTheme.typography.titleLarge,

            )

            Text(
                modifier = Modifier
                    .padding(start = 10.dp, top = 20.dp),
//                    textAlign = TextAlign.Right,
                text = "About Developer",
                fontFamily = FontFamily(Font(R.font.typographica)),
                fontSize = 16.sp,
                color = Color.White,
            )
            Text(
                modifier = Modifier
                    .padding(start = 10.dp, top = 5.dp),
//                    textAlign = TextAlign.Right,
                text = "Developed by Nitesh, a B. Tech CSE student of RCET Bhilai CG.",
                fontFamily = FontFamily(Font(R.font.merienda_regular)),
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = Color.White,
            )

            Text(
                modifier = Modifier
                    .padding(start = 10.dp, top = 20.dp),
//                    textAlign = TextAlign.Right,
                text = "App Version: v3.0",
                fontFamily = FontFamily(Font(R.font.typographica)),
                fontSize = 16.sp,
                color = Color.White,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

//        2nd Card
        Column(
            modifier = Modifier
//                        .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .align(alignment = Alignment.CenterHorizontally)
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                text = "CONTACT  US",
                fontFamily = FontFamily(Font(R.font.typographica)),
                fontSize = 20.sp,
                lineHeight = 12.sp,
                color = Color.White
                )

            Row( modifier = Modifier
                .padding(10.dp)
                .align(alignment = Alignment.CenterHorizontally)

            ){
                val uriHandler = LocalUriHandler.current

                Image(  // Linkedin icon with link
                    alignment = Alignment.Center,
                    painter = painterResource(id = R.drawable.linkedin),
                    contentDescription = "Circular Image",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(start = 10.dp, end = 10.dp)
                        .clickable {
                            uriHandler.openUri("https://www.linkedin.com/in/daemon001")
                        }
                )
                Image(   // Github icon with link
                    alignment = Alignment.Center,
                    painter = painterResource(id = R.drawable.github),
                    contentDescription = "Circular Image",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(start = 10.dp, end = 10.dp)
                        .clickable {
                            uriHandler.openUri("https://github.com/daemon-001")
                        }
                )
                Image(   // Instagram icon with link
                    alignment = Alignment.Center,
                    painter = painterResource(id = R.drawable.instagram),
                    contentDescription = "Circular Image",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(start = 10.dp, end = 10.dp)
                        .clickable {
                            uriHandler.openUri("https://www.instagram.com/mustbe_daemon")
                        }

                )

            }
        }
    }
}

@Composable
fun AboutAnimation(modifier: Modifier = Modifier) {
        val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.aboutapp))
        LottieAnimation(
            composition = composition,
            modifier = Modifier.size(100.dp),
            iterations = LottieConstants.IterateForever,
            // progress = { }
        )
}


