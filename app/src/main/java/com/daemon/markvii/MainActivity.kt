
package com.daemon.markvii

/**
 * @author Nitesh
 */

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
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
import com.daemon.markvii.data.ChatData
import com.daemon.markvii.data.ModelConfiguration
import com.daemon.markvii.data.ModelInfo
import com.daemon.markvii.data.FirebaseConfigManager
import com.daemon.markvii.data.FirebaseModelInfo
import com.daemon.markvii.ui.theme.MarkVIITheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update




class MainActivity : ComponentActivity() {

    private val uriState = MutableStateFlow("")

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
//        Thread.sleep(1000) // splash screen delay
        installSplashScreen()  // splash screen ui
        setContent {

            MarkVIITheme {
                // Initialize Firebase configuration at app startup
                LaunchedEffect(Unit) {
                    FirebaseConfigManager.initialize()
                }
                
                var opentimes by remember { mutableIntStateOf(0) }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Gray
//                    color = MaterialTheme.colorScheme.background
                )

                {
//                    for switching between from home screen to infoTab
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home", builder = {
                        composable("home",){
                            opentimes++
                            Scaffold(
//                                top bar items
                                topBar = {
                                var context = LocalContext.current
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primary)
                                        .height(50.dp)
                                        .padding(start = 15.dp, end = 5.dp)
                                ) {
                                    DropDownDemo() // nlp model selector dropdown menu ui

//                                        Spacer(modifier = Modifier.width(8.dp)) // makes blank space

//                                      info tab start icon at top bar
                                    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.info_card))
                                    LottieAnimation(
                                        composition = composition,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .align(Alignment.CenterEnd)
                                            .clickable {
                                                navController.navigate("info_screen")
                                            },
                                        iterations = LottieConstants.IterateForever  // Play in loop
                                    )
//                                        Icon(
//                                            modifier = Modifier
//                                                .size(35.dp)
//                                                .align(Alignment.CenterEnd)
//                                                .clickable {
//                                                    navController.navigate("info_screen")
////                                                    Toast.makeText(context, "Under development...", Toast.LENGTH_SHORT).show()
//                                                },
//                                            imageVector = Icons.Rounded.Info,
//                                            contentDescription = "Info Tab",
//                                            tint = MaterialTheme.colorScheme.onPrimary
//                                        )
                                }

//                                    Show welcome guide once when app opens (no API call)
                                val chaViewModel = viewModel<ChatViewModel>()
                                if(opentimes==1){
                                    chaViewModel.showWelcomeGuide()
                                }
                            }
                            ) {
                                ChatScreen(paddingValues = it)  // starting chat screen ui
                            }

                        }
                        composable("info_screen",){
                            InfoSetting() // starting infoTab ui (About section)
                        }

                    })


                }

            }
        }
    }

//    chat home screen ui starts here
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatScreen(paddingValues: PaddingValues) {
        val chaViewModel = viewModel<ChatViewModel>()
        val chatState = chaViewModel.chatState.collectAsState().value
        val bitmap = getBitmap()


        BigHi() // chat screen background ui function
        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            verticalArrangement = Arrangement.Bottom
        ) {

//            prompt entry textbox starts here
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                reverseLayout = true,
            ) {
                itemsIndexed(chatState.chatList) { index, chat ->
                    if (chat.isFromUser) {
                        UserChatItem(
                            prompt = chat.prompt, bitmap = chat.bitmap
                        )
                    } else {
                        ModelChatItem(response = chat.prompt, modelUsed = chat.modelUsed)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {


                Column {
//                    picked image display
                    bitmap?.let {
                        Image(
                            modifier = Modifier
                                .padding(bottom = 10.dp, start = 10.dp)
                                .size(58.dp)
                                .clip(CircleShape),
                            contentDescription = "picked image",
                            contentScale = ContentScale.Crop,
                            bitmap = it.asImageBitmap()
                        )
                    }
                }
// ###################################### Text Field ##################################
                TextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
                        .clip(shape = CircleShape),
                    value = chatState.prompt,
                    singleLine = true,
                    onValueChange = {
                        chaViewModel.onEvent(ChatUiEvent.UpdatePrompt(it))
                    },
                    placeholder = {
                        Text(text = "Ask anything!")
                    },

//                    textbox image picker icon
                    leadingIcon = {
                        val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.imgpckr))
                        LottieAnimation(
                            composition = composition,
                            modifier = Modifier
                                .size(50.dp)
                                .clickable {
                                    imagePicker.launch(PickVisualMediaRequest
                                            .Builder()
                                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            .build()
                                    )
                                },
                            iterations = LottieConstants.IterateForever,
                            // progress = { }
                        )
                    },

//                    textbox send icon
                    trailingIcon = {
                        Alignment.Bottom
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

//    user chat text bubble
    @Composable
    fun UserChatItem(prompt: String, bitmap: Bitmap?) {
        SelectionContainer() {
            Column(
                modifier = Modifier
                    .padding(start = 50.dp, top = 8.dp, bottom = 8.dp)
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
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp),
//                    textAlign = TextAlign.Right,
                    text = prompt,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )

            }
        }

    }

//    model chat text bubble
    @Composable
    fun ModelChatItem(response: String, modelUsed: String = "") {
        // Extract brand name from model (e.g., "deepseek/deepseek-chat-v3.1" -> "Deepseek")
        val brandName = if (modelUsed.isNotEmpty()) {
            val brand = modelUsed.substringBefore("/")
            brand.split("-", "_").firstOrNull()?.replaceFirstChar { it.uppercase() } 
                ?: brand.replaceFirstChar { it.uppercase() }
        } else {
            ""
        }
        
        val headerText = if (brandName.isNotEmpty()) {
            "Mark VII  x  $brandName"
        } else {
            "Mark VII"
        }
        
        SelectionContainer() {
            Column(
                modifier = Modifier.padding(end = 50.dp, top = 8.dp, bottom = 8.dp)


            ) {
//                model response text display
                Box(
                    modifier = Modifier
//                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                        .padding(16.dp),
                ){
                    Text(
                        text = headerText,
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.typographica)),
                        color = Color.White
                    )
                    Text(
                        text = "\n\n" + response,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

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

//    Dropdown menu of nlp models
    @Composable
    fun DropDownDemo() {
        val isDropDownExpanded = remember { mutableStateOf(false) }
        val itemPosition = remember { mutableStateOf(0) }
        val scope = rememberCoroutineScope()
        
        // Observe Firebase models and API key
        val firebaseModels by FirebaseConfigManager.models.collectAsState()
        val firebaseApiKey by FirebaseConfigManager.apiKey.collectAsState()
        val configState by FirebaseConfigManager.configState.collectAsState()
        
        // Use ONLY Firebase models - no local fallback
        val currentModels = if (firebaseModels.isNotEmpty()) {
            firebaseModels.map { ModelInfo(it.displayName, it.apiModel, it.isAvailable) }
        } else {
            // If Firebase not configured, show empty list
            emptyList()
        }
        
        // Update API key when Firebase data changes
        LaunchedEffect(firebaseApiKey) {
            if (firebaseApiKey.isNotEmpty()) {
                ChatData.updateApiKey(firebaseApiKey)
            }
        }
        
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {

        Box {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    isDropDownExpanded.value = true
                }
            ) {

                // Show loading indicator, model name, or error message
                when (configState) {
                    is FirebaseConfigManager.ConfigState.Loading -> {
                        Text(
                            text = "Loading...",
                            fontSize = 20.sp,
                            fontFamily = FontFamily(Font(R.font.typographica)),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    is FirebaseConfigManager.ConfigState.Error -> {
                        Text(
                            text = "Firebase Required",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.typographica)),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {
                        if (currentModels.isNotEmpty() && itemPosition.value < currentModels.size) {
                            Text(
                                text = currentModels[itemPosition.value].displayName,
                                fontSize = 20.sp,
                                fontFamily = FontFamily(Font(R.font.typographica)),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = "Setup Firebase",
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.typographica)),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                Image(
                    painter = painterResource(id = R.drawable.drop_down_ic),
                    contentDescription = "DropDown Icon"
                )

                // Set initial model
                if (currentModels.isNotEmpty() && itemPosition.value < currentModels.size) {
                    ChatData.selected_model = currentModels[itemPosition.value].apiModel
                }
            }

            DropdownMenu(
                expanded = isDropDownExpanded.value,
                onDismissRequest = {
                    isDropDownExpanded.value = false
                }) {
                val context = LocalContext.current

                if (currentModels.isEmpty()) {
                    // Show message when Firebase not configured
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "⚠️ Firebase not configured\nPlease set up Firebase with models and API key",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            isDropDownExpanded.value = false
                            Toast.makeText(context, "Please configure Firebase first", Toast.LENGTH_LONG).show()
                        }
                    )
                } else {
                    currentModels.forEachIndexed { index, model ->
                        DropdownMenuItem(
                            text = {
                                Text(text = model.displayName)
                            },
                            onClick = {
                                isDropDownExpanded.value = false
                                itemPosition.value = index
                                
                                // Update the selected model
                                ChatData.selected_model = model.apiModel

                                // Handle unavailable models
                                if (!model.isAvailable) {
                                    Toast.makeText(context, "Model temporarily unavailable", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }



    }



// chat screen backgrounds
    @Composable
    fun BigHi(modifier: Modifier = Modifier) {
        Box(
            modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        )  {

//            chat screen background wallpaper
            Image(
                alignment = Alignment.Center,
                painter = painterResource(id = R.drawable.background),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

//            Startup big hi  animation
            val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.gassist))
            LottieAnimation(
                composition = composition,
                modifier = Modifier
                    .size(500.dp)
                    .align(Alignment.Center)
                    .shadow(300.dp),
//                iterations = LottieConstants.IterateForever // Play in loop
//                progress = {}
            )


        }
    }





}




















