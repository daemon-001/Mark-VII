package com.daemon.markvii

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.Locale
import com.daemon.markvii.data.AuthManager
import com.daemon.markvii.data.ChatSession
import com.daemon.markvii.ui.theme.LocalAppColors
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

// Static formatter to avoid creating multiple instances during LazyColumn recomposition
private val dateFormatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

/**
 * Side drawer content for chat session management
 * @author Nitesh
 */
@Composable
fun DrawerContent(
    chatViewModel: ChatViewModel,
    onDismiss: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onSigningInChanged: (Boolean) -> Unit = {}
) {
    val currentUser by remember(chatViewModel) {
        chatViewModel.chatState.map { it.currentUser }.distinctUntilChanged()
    }.collectAsState(initial = chatViewModel.chatState.value.currentUser)
    
    val chatSessions by remember(chatViewModel) {
        chatViewModel.chatState.map { it.chatSessions }.distinctUntilChanged()
    }.collectAsState(initial = chatViewModel.chatState.value.chatSessions)
    
    val currentSessionId by remember(chatViewModel) {
        chatViewModel.chatState.map { it.currentSessionId }.distinctUntilChanged()
    }.collectAsState(initial = chatViewModel.chatState.value.currentSessionId)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appColors = LocalAppColors.current // Get theme colors
    
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(vertical = 16.dp)
    ) {
        // Header

        
        // Check if user is signed in
        val user = currentUser
        if (user != null) {
            // Stable callbacks
            val currentOnSettingsClick by rememberUpdatedState(onSettingsClick)
            val currentOnDismiss by rememberUpdatedState(onDismiss)
            
            val stableOnNewChat: () -> Unit = remember(chatViewModel) {
                {
                    chatViewModel.onEvent(ChatUiEvent.CreateNewSession)
                    currentOnDismiss()
                }
            }
            
            val stableOnSessionClick: (String) -> Unit = remember(chatViewModel) {
                { sessionId ->
                    chatViewModel.onEvent(ChatUiEvent.SwitchSession(sessionId))
                    currentOnDismiss()
                }
            }
            
            val stableOnSessionDelete: (String) -> Unit = remember(chatViewModel) {
                { sessionId ->
                    chatViewModel.onEvent(ChatUiEvent.DeleteSession(sessionId))
                }
            }
            
            val stableOnRename: (String, String) -> Unit = remember(chatViewModel) {
                { sessionId, newTitle ->
                    chatViewModel.onEvent(ChatUiEvent.RenameSession(sessionId, newTitle))
                }
            }
            
            val stableOnSignOut: () -> Unit = remember(chatViewModel) {
                {
                    chatViewModel.onEvent(ChatUiEvent.SignOut)
                    currentOnDismiss()
                }
            }

            // Authenticated state
            AuthenticatedDrawerContent(
                userDisplayName = user.displayName ?: "User",
                userEmail = user.email ?: "",
                userPhotoUrl = user.photoUrl,
                sessions = chatSessions, // Unstable list but sorted internally
                currentSessionId = currentSessionId,
                onNewChat = stableOnNewChat,
                onSessionClick = stableOnSessionClick,
                onSessionDelete = stableOnSessionDelete,
                onRename = stableOnRename,
                onSignOut = stableOnSignOut,
                onSettingsClick = { currentOnSettingsClick() }
            )
        } else {
            // Unauthenticated state
            UnauthenticatedDrawerContent(
                onNavigateToAuth = {
                    onDismiss()
                    onNavigateToAuth()
                },
                onNewChat = {
                    chatViewModel.onEvent(ChatUiEvent.CreateNewSession)
                    onDismiss()
                },
                onSettingsClick = onSettingsClick
            )
        }
    }
}

@Composable
fun UnauthenticatedDrawerContent(
    onNavigateToAuth: () -> Unit,
    onNewChat: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val appColors = LocalAppColors.current
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Guest profile section (matching authenticated layout)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Guest",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Not signed in",
                    fontSize = 13.sp,
                    color = appColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Login button
            FilledTonalButton(
                onClick = onNavigateToAuth,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = appColors.accent.copy(alpha = 0.15f),
                    contentColor = appColors.accent
                ),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Sign In", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Settings button
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(appColors.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // New chat button
        Button(
            onClick = onNewChat,
            colors = ButtonDefaults.buttonColors(
                containerColor = appColors.accent,
                contentColor = appColors.onAccent
            ),
            shape = CircleShape, // Fully rounded pill shape
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "New chat",
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "New Chat",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Chat sessions list header
        Text(
            text = "RECENT CHATS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = appColors.textSecondary,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Empty state for guest
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sign in to save and view your chat history across devices.",
                fontSize = 14.sp,
                color = appColors.textSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun AuthenticatedDrawerContent(
    userDisplayName: String,
    userEmail: String,
    userPhotoUrl: android.net.Uri?,
    sessions: List<ChatSession>,
    currentSessionId: String?,
    onNewChat: () -> Unit,
    onSessionClick: (String) -> Unit,
    onSessionDelete: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onSignOut: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val appColors = LocalAppColors.current // Get theme colors in this scope
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // User profile section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (userPhotoUrl == null) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(appColors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile photo",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                val painter = rememberAsyncImagePainter(
                    model = userPhotoUrl
                )
                Image(
                    painter = painter,
                    contentDescription = "Profile photo",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                )
            }
        
            Spacer(modifier = Modifier.width(16.dp))
        
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userDisplayName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = userEmail,
                    fontSize = 13.sp,
                    color = appColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        
            // Settings button
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(appColors.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // New chat button
        Button(
            onClick = onNewChat,
            colors = ButtonDefaults.buttonColors(
                containerColor = appColors.accent,
                contentColor = if (appColors.accent == MaterialTheme.colorScheme.primary) Color.White else Color.Black
            ),
            shape = CircleShape, // Fully rounded pill shape
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "New chat",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "New Chat",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Chat sessions list
        Text(
            text = "RECENT CHATS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = appColors.textSecondary,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Memoize sorted sessions to prevent re-sorting on every recomposition
        val sortedSessions = remember(sessions) {
            sessions.sortedByDescending { it.updatedAt }
        }
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            items(
                items = sortedSessions,
                key = { it.id },
                contentType = { "chat_session" }
            ) { session ->
                ChatSessionItem(
                    sessionId = session.id,
                    sessionTitle = session.title,
                    sessionUpdatedAt = session.updatedAt.toDate().time,
                    isSelected = session.id == currentSessionId,
                    onClick = onSessionClick,
                    onDelete = onSessionDelete,
                    onRename = onRename
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatSessionItem(
    sessionId: String,
    sessionTitle: String,
    sessionUpdatedAt: Long, // primitive for stability
    isSelected: Boolean,
    onClick: (String) -> Unit,
    onDelete: (String) -> Unit,
    onRename: (String, String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val appColors = LocalAppColors.current // Get theme colors in this scope
    
    // Format date string
    val formattedDate = remember(sessionUpdatedAt) {
        dateFormatter.format(java.util.Date(sessionUpdatedAt))
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) appColors.accent.copy(alpha = 0.1f) else Color.Transparent)
            .combinedClickable(
                onClick = { onClick(sessionId) },
                onLongClick = { showMenu = true }
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chat Icon inside a circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) appColors.accent.copy(alpha = 0.2f) else appColors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = if (isSelected) appColors.accent else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sessionTitle,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) appColors.accent else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Use pre-calculated formatted date
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = appColors.textSecondary,
                    maxLines = 1
                )
            }
        }
        
        // Context Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text("Rename", color = MaterialTheme.colorScheme.onSurface) },
                onClick = {
                    showMenu = false
                    showRenameDialog = true
                },
                leadingIcon = {
                    Icon(
                        androidx.compose.material.icons.Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
            DropdownMenuItem(
                text = { Text("Delete", color = appColors.error) },
                onClick = {
                    showMenu = false
                    showDeleteDialog = true
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = appColors.error
                    )
                }
            )
        }
    }
    
    // Rename Dialog
    if (showRenameDialog) {
        var newTitle by remember { mutableStateOf(sessionTitle) }
        
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Chat") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = appColors.accent,
                        focusedBorderColor = appColors.accent,
                        unfocusedBorderColor = appColors.divider
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            onRename(sessionId, newTitle)
                            showRenameDialog = false
                        }
                    }
                ) {
                    Text("Save", color = appColors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = appColors.textPrimary
        )
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Chat?") },
            text = { Text("This will permanently delete this chat session.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(sessionId)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = appColors.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = appColors.textPrimary
        )
    }
}

