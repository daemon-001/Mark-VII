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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExitToApp
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
import com.daemon.markvii.data.AuthManager
import com.daemon.markvii.data.ChatSession
import com.daemon.markvii.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Side drawer content for chat session management
 * @author Nitesh
 */
@Composable
fun DrawerContent(
    chatViewModel: ChatViewModel,
    onDismiss: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onSigningInChanged: (Boolean) -> Unit = {}
) {
    val chatState by chatViewModel.chatState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appColors = LocalAppColors.current // Get theme colors
    
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 16.dp)
    ) {
        // Header

        

        
        // Check if user is signed in
        val currentUser = chatState.currentUser
        
        if (currentUser != null) {
            // Authenticated state
            AuthenticatedDrawerContent(
                user = currentUser,
                sessions = chatState.chatSessions,
                currentSessionId = chatState.currentSessionId,
                onNewChat = {
                    chatViewModel.onEvent(ChatUiEvent.CreateNewSession)
                    onDismiss()
                },
                onSessionClick = { sessionId ->
                    chatViewModel.onEvent(ChatUiEvent.SwitchSession(sessionId))
                    onDismiss()
                },
                onSessionDelete = { sessionId ->
                    chatViewModel.onEvent(ChatUiEvent.DeleteSession(sessionId))
                },
                onRename = { sessionId, newTitle ->
                    chatViewModel.onEvent(ChatUiEvent.RenameSession(sessionId, newTitle))
                },
                onSignOut = {
                    chatViewModel.onEvent(ChatUiEvent.SignOut)
                    onDismiss()
                },
                onSettingsClick = onSettingsClick
            )
        } else {
            // Unauthenticated state
            UnauthenticatedDrawerContent(
                onSignIn = {
                    onSigningInChanged(true)
                    coroutineScope.launch {
                        try {
                            AuthManager.signInWithGoogle(context as ComponentActivity)
                            // Result will be handled in MainActivity.onActivityResult
                        } catch (e: Exception) {
                            onSigningInChanged(false)
                        }
                        onDismiss()
                    }
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
    onSignIn: () -> Unit,
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Not signed in",
                    fontSize = 12.sp,
                    color = appColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Settings button
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = appColors.textSecondary
                )
            }
        }
        
        Divider(color = appColors.divider, thickness = 1.dp)
        
        Spacer(modifier = Modifier.height(12.dp))

        // New chat button
        Button(
            onClick = onNewChat,
            colors = ButtonDefaults.buttonColors(
                containerColor = appColors.accent,
                contentColor = if (appColors.accent == MaterialTheme.colorScheme.primary) Color.White else Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "New chat",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "New Chat",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Chat sessions list header
        Text(
            text = "Recent Chats",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = appColors.textSecondary,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Continue with Google button (in chat list area)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sign in to save your chat history",
                fontSize = 14.sp,
                color = appColors.textSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onSignIn,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, appColors.divider),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .height(44.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    // Google logo
                    Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Google",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun AuthenticatedDrawerContent(
    user: com.google.firebase.auth.FirebaseUser,
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
        // User profile section - memoized to prevent recomposition
        key(user.uid) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User photo
                val painter = rememberAsyncImagePainter(
                    model = user.photoUrl
                )
                Image(
                    painter = painter,
                    contentDescription = "Profile photo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(width = 2.dp, color = appColors.accent, shape = CircleShape)
                )
            
                Spacer(modifier = Modifier.width(12.dp))
            
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.displayName ?: "User",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = user.email ?: "",
                        fontSize = 12.sp,
                        color = appColors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            
                // Settings button
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = appColors.textSecondary
                    )
                }
            }
        }
        
        Divider(color = appColors.divider, thickness = 1.dp)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // New chat button
        Button(
            onClick = onNewChat,
            colors = ButtonDefaults.buttonColors(
                containerColor = appColors.accent,
                contentColor = if (appColors.accent == MaterialTheme.colorScheme.primary) Color.White else Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "New chat",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "New Chat",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Chat sessions list
        Text(
            text = "Recent Chats",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF8E8E93),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
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
                    session = session,
                    isSelected = session.id == currentSessionId,
                    onClick = { onSessionClick(session.id) },
                    onDelete = { onSessionDelete(session.id) },
                    onRename = { newTitle -> onRename(session.id, newTitle) }
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatSessionItem(
    session: ChatSession,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val appColors = LocalAppColors.current // Get theme colors in this scope
    
    // Memoize formatted date to prevent recalculation
    val formattedDate = remember(session.updatedAt) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(session.updatedAt.toDate())
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) appColors.surfaceVariant else Color.Transparent)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Use pre-calculated formatted date
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = formattedDate,
                    fontSize = 10.sp,
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
        var newTitle by remember { mutableStateOf(session.title) }
        
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
                            onRename(newTitle)
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
                        onDelete()
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

