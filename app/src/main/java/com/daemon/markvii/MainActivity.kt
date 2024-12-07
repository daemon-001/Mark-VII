package com.daemon.markvii

//import androidx.compose.foundation.layout.ColumnScopeInstance.align
//import androidx.compose.foundation.layout.FlowColumnScopeInstance.align
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.daemon.markvii.ui.theme.GeminiChatBotTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


class MainActivity : ComponentActivity() {

    private val uriState = MutableStateFlow("")
//    val animationView = LottieAnimationView(this)

    private val imagePicker =
        registerForActivityResult<PickVisualMediaRequest, Uri?>(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let {
                uriState.update { uri.toString() }
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(1000)
        installSplashScreen()
        setContent {

            GeminiChatBotTheme {
                var opentimes by remember { mutableIntStateOf(0) }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Gray
//                    color = MaterialTheme.colorScheme.background
                )

                {

                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home", builder = {
                        composable("home",){
                            opentimes++
                            Scaffold(
                                topBar = {
                                    var context = LocalContext.current
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.primary)
                                            .height(50.dp)
                                            .padding(horizontal = 15.dp)
                                    ) {
                                        geminiAnimation()
                                        Text(
                                            modifier = Modifier
                                                .align(Alignment.CenterStart),

                                            text = "AI ChatBot-M7",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

//                                        Home screen right corner image
                                        Image(
                                            modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                                .size(60.dp)
                                                .clickable {
                                                    navController.navigate("info_screen")
                                                    Toast.makeText(context, "Under development...", Toast.LENGTH_SHORT).show()
                                                },
                                            painter = painterResource(id = R.drawable.mini_logo),
                                            contentDescription = "Mini logo"
                                        )
                                    }

//                                    Greeting once when app open
                                    val chaViewModel = viewModel<ChatViewModel>()
                                    val bitmap = getBitmap()
                                    if(opentimes==1){
                                        chaViewModel.onStart(ChatUiEvent.SendPrompt("Greet me with a unique quote and ask me for any help", bitmap))
                                    }
                                }
                            ) {
                                ChatScreen(paddingValues = it)
                            }

                        }
                        composable("info_screen",){
                            infotab1()
                        }

                    })


                }

            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatScreen(paddingValues: PaddingValues) {
        BigHi()
        val chaViewModel = viewModel<ChatViewModel>()
        val chatState = chaViewModel.chatState.collectAsState().value
        val bitmap = getBitmap()


        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            verticalArrangement = Arrangement.Bottom
        ) {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                reverseLayout = true
            ) {
                itemsIndexed(chatState.chatList) { index, chat ->
                    if (chat.isFromUser) {
                        UserChatItem(
                            prompt = chat.prompt, bitmap = chat.bitmap
                        )
                    } else {
                        ModelChatItem(response = chat.prompt)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp, start = 0.dp, end = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {


                Column {

                    bitmap?.let {
                        Image(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(bottom = 2.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentDescription = "picked image",
                            contentScale = ContentScale.Crop,
                            bitmap = it.asImageBitmap()
                        )
                    }
                }

                TextField(
                    modifier = Modifier
                        .weight(1f),

                    value = chatState.prompt,

                    onValueChange = {
                        chaViewModel.onEvent(ChatUiEvent.UpdatePrompt(it))
                    },
                    placeholder = {
                        Text(text = "Ask anything!")
                    },
                    leadingIcon = {
                        val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.imgpckr))
                        LottieAnimation(
                            composition = composition,
                            modifier = Modifier
                                .size(50.dp)
                                .clickable {
                                    imagePicker.launch(
                                        PickVisualMediaRequest
                                            .Builder()
                                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            .build()
                                    )
                                },
                            iterations = LottieConstants.IterateForever,
                            // progress = { }
                        )
                    },
                    trailingIcon = {
                        val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.sendair))
                        LottieAnimation(
                            composition = composition,
                            modifier = Modifier
                                .size(60.dp)
                                .clickable {
//                            Toast.makeText(context, "Responding...", Toast.LENGTH_SHORT).show()
                                    chaViewModel.onEvent(ChatUiEvent.SendPrompt(chatState.prompt, bitmap))
                                    uriState.update { "" }
                                },
                            iterations = LottieConstants.IterateForever,
                            // progress = { }
                        )
                    },
                )
            }

        }

    }

    @Composable
    fun UserChatItem(prompt: String, bitmap: Bitmap?) {
        SelectionContainer() {
            Column(
                modifier = Modifier
                    .padding(start = 50.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End

            ) {

                bitmap?.let {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
//                            .height(260.dp)
                            .padding(bottom = 2.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentDescription = "image",
                        contentScale = ContentScale.FillWidth,
                        bitmap = it.asImageBitmap()
                    )
                }

                Text(
                    modifier = Modifier
//                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(10.dp),
//                    textAlign = TextAlign.Right,
                    text = prompt,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )

            }
        }

    }

    @Composable
    fun ModelChatItem(response: String) {
        SelectionContainer() {
            Column(
                modifier = Modifier.padding(end = 50.dp, bottom = 16.dp)


            ) {

                Text(
                    modifier = Modifier
//                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .padding(16.dp),
                    text = "\uD835\uDDE0\uD835\uDDEE\uD835\uDDFF\uD835\uDDF8 \uD835\uDDE9\uD835\uDDDC\uD835\uDDDC:\n\n" + response,
                    fontSize = 17.sp,
                    color = Color.White

                )


            }
        }
    }

    @Composable
    private fun getBitmap(): Bitmap? {
        val uri = uriState.collectAsState().value

        val imageState: AsyncImagePainter.State = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .size(Size.ORIGINAL)
                .build()
        ).state

        if (imageState is AsyncImagePainter.State.Success) {
            return imageState.result.drawable.toBitmap()
        }

        return null
    }

    @Composable
    fun BigHi(modifier: Modifier = Modifier) {
        Box(
            modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        )  {
            val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.gassist))
            LottieAnimation(
                composition = composition,
                modifier = Modifier.size(500.dp),
//                iterations = LottieConstants.IterateForever,
                // progress = { }
            )
        }
    }

    @Composable
    fun PinFile(modifier: Modifier = Modifier) {
        Box(
            modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        )  {
            val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.pinfile))
            LottieAnimation(
                composition = composition,
                modifier = Modifier.size(50.dp),
//                iterations = LottieConstants.IterateForever,
                // progress = { }
            )
        }
    }

    @Composable
    fun geminiAnimation(modifier: Modifier = Modifier) {
        Box(
            modifier
                .fillMaxSize()
                .padding(start = 70.dp),
            contentAlignment = Alignment.CenterStart
        )  {
            val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.gloading))
            LottieAnimation(
                composition = composition,
                modifier = Modifier.size(200.dp),
                iterations = LottieConstants.IterateForever,
//                 progress = { }
            )
//            var isAnimationVisible by remember { mutableStateOf(true) } // State to control visibility
//            LaunchedEffect(key1 = Unit) { // Start a delay when the composable is recomposed
//                delay(3000) // Delay for 3 seconds
//                isAnimationVisible = false // Hide the animation after delay
//            }
//
//            AnimatedVisibility(visible = isAnimationVisible) { // Show/hide based on state
//                LottieAnimation( // Your Lottie animation composable
//                    composition = composition // Your Lottie composition
//                )
        }
    }


}


















