package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SavedChat
import com.example.data.SavedSession

// Beautiful design colors matching FastAPI index.html
val Slate950 = Color(0xFF0F1115) // High Density Background
val Slate900 = Color(0xFF11131A) // High Density Card background
val Slate800 = Color(0xFF1E293B) // High Density Border
val Slate700 = Color(0xFF334155) // Outer borders
val Slate500 = Color(0xFF64748B) // Subtitles
val Slate400 = Color(0xFF94A3B8) // Muted texts
val Indigo600 = Color(0xFF4F46E5) // Primary Indigo
val Indigo500 = Color(0xFF6366F1) // Highlight Glow
val Indigo400 = Color(0xFF818CF8) // Focused Branding
val Rose400 = Color(0xFFFB7185)   // Warm Red Warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiteChatScreen(
    viewModel: LiteChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val cachedChats by viewModel.cachedChats.collectAsStateWithLifecycle()
    val savedSessions by viewModel.savedSessions.collectAsStateWithLifecycle()
    val loginStep by viewModel.loginStep.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val apiId by viewModel.apiId.collectAsStateWithLifecycle()
    val apiHash by viewModel.apiHash.collectAsStateWithLifecycle()
    val phone by viewModel.phone.collectAsStateWithLifecycle()
    val code by viewModel.code.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val sessionKey by viewModel.sessionKey.collectAsStateWithLifecycle()
    val customBaseUrl by viewModel.customBaseUrl.collectAsStateWithLifecycle()

    val isSendingCode by viewModel.isSendingCode.collectAsStateWithLifecycle()
    val isVerifyingCode by viewModel.isVerifyingCode.collectAsStateWithLifecycle()
    val isFetchingChats by viewModel.isFetchingChats.collectAsStateWithLifecycle()
    val requiresPassword by viewModel.requiresPassword.collectAsStateWithLifecycle()

    var showPasswordVisible by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Slate950,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .border(width = (0.5).dp, color = Slate800)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val items = listOf(
                        Triple("Chats", Icons.Default.Forum, 0),
                        Triple("Sessions", Icons.Default.Key, 1),
                        Triple("Profile", Icons.Default.AccountCircle, 2),
                        Triple("Nodes", Icons.Default.Hub, 3)
                    )
                    items.forEach { (label, icon, tabIdx) ->
                        val isActive = activeTab == tabIdx
                        val tintColor = if (isActive) Indigo400 else Slate400.copy(alpha = 0.5f)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeTab = tabIdx }
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = tintColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = label,
                                color = tintColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.2).sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate950)
        ) {
            // Integrated Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate950)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "LiteChat",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "LIVE TELEGRAM CLIENT",
                        color = Indigo400,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Indigo600.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                        .border(1.dp, Indigo400.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .clickable { activeTab = 3 },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SettingsInputComponent,
                        contentDescription = "Nodes",
                        tint = Indigo400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Error Banner
            if (error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x26EF4444)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Rose400,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = error ?: "",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Slate400,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (activeTab) {
                    0 -> { // Chats / Direct Messages Tab
                        if (activeSession == null) {
                            item {
                                AnimatedContent(
                                    targetState = loginStep,
                                    label = "LoginStepsTransition"
                                ) { step ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Slate900),
                                        shape = RoundedCornerShape(24.dp),
                                        border = BorderStroke(1.dp, Slate800)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            verticalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            when (step) {
                                                LoginStep.ENTER_CREDENTIALS -> {
                                                    Column {
                                                        Text(
                                                            text = "Connect Your Account",
                                                            color = Color.White,
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = "Enter your Telegram API credentials and phone number.",
                                                            color = Slate400,
                                                            fontSize = 12.sp,
                                                            modifier = Modifier.padding(top = 4.dp)
                                                        )
                                                    }

                                                    Divider(color = Slate800.copy(alpha = 0.5f))

                                                    OutlinedTextField(
                                                        value = apiId,
                                                        onValueChange = { viewModel.onApiIdChanged(it) },
                                                        label = { Text("API ID") },
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = Indigo500,
                                                            unfocusedBorderColor = Slate700,
                                                            focusedLabelColor = Indigo400,
                                                            unfocusedLabelColor = Slate400,
                                                            focusedTextColor = Color.White,
                                                            unfocusedTextColor = Color.White
                                                        ),
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )

                                                    OutlinedTextField(
                                                        value = apiHash,
                                                        onValueChange = { viewModel.onApiHashChanged(it) },
                                                        label = { Text("API Hash") },
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = Indigo500,
                                                            unfocusedBorderColor = Slate700,
                                                            focusedLabelColor = Indigo400,
                                                            unfocusedLabelColor = Slate400,
                                                            focusedTextColor = Color.White,
                                                            unfocusedTextColor = Color.White
                                                        ),
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )

                                                    OutlinedTextField(
                                                        value = phone,
                                                        onValueChange = { viewModel.onPhoneChanged(it) },
                                                        label = { Text("Phone Number") },
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = Indigo500,
                                                            unfocusedBorderColor = Slate700,
                                                            focusedLabelColor = Indigo400,
                                                            unfocusedLabelColor = Slate400,
                                                            focusedTextColor = Color.White,
                                                            unfocusedTextColor = Color.White
                                                        ),
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )

                                                    Button(
                                                        onClick = { viewModel.sendAuthenticationCode() },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                                                        enabled = !isSendingCode,
                                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                                        shape = RoundedCornerShape(14.dp)
                                                    ) {
                                                        if (isSendingCode) {
                                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("Requesting...")
                                                        } else {
                                                            Icon(imageVector = Icons.AutoMirrored.Outlined.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("Send OTP Code", fontWeight = FontWeight.Bold)
                                                        }
                                                    }

                                                    TextButton(
                                                        onClick = { viewModel.setLoginStep(LoginStep.SESSION_KEY) },
                                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                                    ) {
                                                        Text("Or Login with Session Key", color = Indigo400, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                LoginStep.ENTER_CODE -> {
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.resetStep() }) {
                                                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Indigo400, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Back", fontSize = 12.sp, color = Indigo400, fontWeight = FontWeight.Bold)
                                                    }
                                                    Column {
                                                        Text(text = "Verify OTP", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                                        Text(text = "Enter the code sent to your Telegram app.", color = Slate400, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                                                    }
                                                    OutlinedTextField(
                                                        value = code,
                                                        onValueChange = { viewModel.onCodeChanged(it) },
                                                        label = { Text("OTP Code") },
                                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo500, unfocusedBorderColor = Slate700, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    if (requiresPassword) {
                                                        OutlinedTextField(
                                                            value = password,
                                                            onValueChange = { viewModel.onPasswordChanged(it) },
                                                            label = { Text("Cloud Password (2FA)") },
                                                            visualTransformation = if (showPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                                            trailingIcon = {
                                                                IconButton(onClick = { showPasswordVisible = !showPasswordVisible }) {
                                                                    Icon(imageVector = if (showPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = "Toggle", tint = Slate400)
                                                                }
                                                            },
                                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo500, unfocusedBorderColor = Slate700, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                            singleLine = true,
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                    }
                                                    Button(
                                                        onClick = { viewModel.verifyAuthenticationCode() },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                                                        enabled = !isVerifyingCode,
                                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                                        shape = RoundedCornerShape(14.dp)
                                                    ) {
                                                        if (isVerifyingCode) {
                                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                                        } else {
                                                            Text("Verify & Login", fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                                LoginStep.SESSION_KEY -> {
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.resetStep() }) {
                                                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Indigo400, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Back", fontSize = 12.sp, color = Indigo400, fontWeight = FontWeight.Bold)
                                                    }
                                                    Column {
                                                        Text(text = "Session Key Login", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                                        Text(text = "Paste your Telethon session string below.", color = Slate400, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                                                    }
                                                    OutlinedTextField(
                                                        value = apiId,
                                                        onValueChange = { viewModel.onApiIdChanged(it) },
                                                        label = { Text("API ID") },
                                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo500, unfocusedBorderColor = Slate700, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    OutlinedTextField(
                                                        value = apiHash,
                                                        onValueChange = { viewModel.onApiHashChanged(it) },
                                                        label = { Text("API Hash") },
                                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo500, unfocusedBorderColor = Slate700, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    OutlinedTextField(
                                                        value = phone,
                                                        onValueChange = { viewModel.onPhoneChanged(it) },
                                                        label = { Text("Phone Number") },
                                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo500, unfocusedBorderColor = Slate700, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    OutlinedTextField(
                                                        value = sessionKey,
                                                        onValueChange = { viewModel.onSessionKeyChanged(it) },
                                                        label = { Text("Session Key") },
                                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo500, unfocusedBorderColor = Slate700, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    Button(
                                                        onClick = { viewModel.loginWithSessionKey() },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                                                        enabled = !isVerifyingCode,
                                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                                        shape = RoundedCornerShape(14.dp)
                                                    ) {
                                                        if (isVerifyingCode) {
                                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                                        } else {
                                                            Text("Import Session", fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Authorized Dashboard View
                            val session = activeSession!!
                            item {
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Card(modifier = Modifier.weight(2f).height(80.dp).clickable {
                                        clipboardManager.setText(AnnotatedString(session.sessionString))
                                        Toast.makeText(context, "Session string copied!", Toast.LENGTH_SHORT).show()
                                    }, colors = CardDefaults.cardColors(containerColor = Slate900), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Slate800)) {
                                        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = "Active Session String", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = session.sessionString, color = Indigo400, fontFamily = FontFamily.Monospace, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Indigo400, modifier = Modifier.size(13.dp))
                                            }
                                        }
                                    }
                                    Card(modifier = Modifier.weight(1f).height(80.dp), colors = CardDefaults.cardColors(containerColor = Indigo600), shape = RoundedCornerShape(20.dp)) {
                                        Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                            Text(text = "API ID", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text(text = session.apiId, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            item {
                                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = Slate900), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Slate800)) {
                                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Box(modifier = Modifier.size(40.dp)) {
                                                Box(modifier = Modifier.size(40.dp).background(Slate800, CircleShape), contentAlignment = Alignment.Center) {
                                                    Icon(imageVector = Icons.Default.Person, contentDescription = "User", tint = Slate400, modifier = Modifier.size(18.dp))
                                                }
                                                Box(modifier = Modifier.size(11.dp).background(Color(0xFF10B981), CircleShape).border(2.dp, Slate900, CircleShape).align(Alignment.BottomEnd))
                                            }
                                            Column {
                                                Text(text = session.phone, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                Text(text = "Authenticated via Telethon", color = Slate400, fontSize = 10.sp)
                                            }
                                        }
                                        Button(onClick = { viewModel.logout() }, colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Rose400), shape = RoundedCornerShape(10.dp), modifier = Modifier.height(32.dp)) {
                                            Text("Disconnect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            item {
                                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = Slate900), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Slate800)) {
                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "Direct Messages", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { viewModel.fetchChats() }) {
                                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = Indigo400, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        if (isFetchingChats) {
                                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Indigo400, trackColor = Slate800)
                                        }
                                        if (cachedChats.isEmpty() && !isFetchingChats) {
                                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                                Text("No messages found.", color = Slate400, fontSize = 12.sp)
                                            }
                                        } else {
                                            cachedChats.forEach { chat ->
                                                ChatRowItem(chat)
                                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Slate800)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> { // Sessions Tab
                        items(savedSessions) { session ->
                            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { viewModel.selectSession(session.phone) }, colors = CardDefaults.cardColors(containerColor = if (session.isActive) Indigo600.copy(alpha = 0.1f) else Slate900), border = BorderStroke(1.dp, if (session.isActive) Indigo400 else Slate800), shape = RoundedCornerShape(16.dp)) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = if (session.isActive) Indigo400 else Slate400)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = session.phone, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text(text = "API ID: ${session.apiId}", color = Slate400, fontSize = 11.sp)
                                    }
                                    IconButton(onClick = { viewModel.deleteSession(session) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Rose400.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }
                    2 -> { // Profile Tab
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.size(100.dp).background(Indigo600.copy(alpha = 0.2f), CircleShape).border(2.dp, Indigo400, CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Indigo400, modifier = Modifier.size(48.dp))
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Security Profile", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text("Your sessions are stored locally and encrypted.", color = Slate400, fontSize = 12.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    3 -> { // Nodes Tab
                        item {
                            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = Slate900), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Slate800)) {
                                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("Backend Node Settings", color = Color.White, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = customBaseUrl,
                                        onValueChange = { viewModel.onBaseUrlChanged(it) },
                                        label = { Text("Base URL") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo500, unfocusedBorderColor = Slate700, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Text("Configure your own Telegram-to-HTTP bridge server here.", color = Slate400, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatRowItem(chat: com.example.data.SavedChat) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(48.dp).background(Indigo600.copy(alpha = 0.1f), CircleShape).border(1.dp, Indigo400.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
            Text(text = chat.name.take(1).uppercase(), color = Indigo400, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = chat.name, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (chat.unreadCount > 0) {
                    Box(modifier = Modifier.background(Indigo600, CircleShape).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(text = chat.unreadCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(text = chat.lastMessage, color = Slate400, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
