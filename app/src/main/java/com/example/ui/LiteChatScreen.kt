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
    val sessionKeyInput by viewModel.sessionKeyInput.collectAsStateWithLifecycle()

    val isSendingCode by viewModel.isSendingCode.collectAsStateWithLifecycle()
    val isVerifyingCode by viewModel.isVerifyingCode.collectAsStateWithLifecycle()
    val isFetchingChats by viewModel.isFetchingChats.collectAsStateWithLifecycle()
    val requiresPassword by viewModel.requiresPassword.collectAsStateWithLifecycle()

    val proxyType by viewModel.proxyType.collectAsStateWithLifecycle()
    val proxyHost by viewModel.proxyHost.collectAsStateWithLifecycle()
    val proxyPort by viewModel.proxyPort.collectAsStateWithLifecycle()
    val proxySecret by viewModel.proxySecret.collectAsStateWithLifecycle()
    val proxyPingMs by viewModel.proxyPingMs.collectAsStateWithLifecycle()
    val isTestingProxy by viewModel.isTestingProxy.collectAsStateWithLifecycle()

    var showPasswordVisible by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(0) }
    var loginMode by remember { mutableStateOf(0) }

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
            // Integrated Header (matching CSS px-6 py-5 bg-[#0F1115])
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
                        .clickable { activeTab = 3 } // Quick node settings jump
                        .testTag("settings_btn"),
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

            // Error Banner (floating style)
            if (error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x26EF4444)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
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
                            // Render Login Card
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
                                                            text = "Choose authorization method: Phone OTP or direct Telegram Session Key import.",
                                                            color = Slate400,
                                                            fontSize = 12.sp,
                                                            modifier = Modifier.padding(top = 4.dp)
                                                        )
                                                    }

                                                    Divider(color = Slate800.copy(alpha = 0.5f))

                                                    // Authentication Mode Switcher
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Slate950, RoundedCornerShape(12.dp))
                                                            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                                                            .padding(4.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(36.dp)
                                                                .background(
                                                                    if (loginMode == 0) Indigo600 else Color.Transparent,
                                                                    RoundedCornerShape(8.dp)
                                                                )
                                                                .clickable { loginMode = 0 },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("Phone + Code", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(36.dp)
                                                                .background(
                                                                    if (loginMode == 1) Indigo600 else Color.Transparent,
                                                                    RoundedCornerShape(8.dp)
                                                                )
                                                                .clickable { loginMode = 1 },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("Session Key String", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }

                                                    if (loginMode == 0) {

                                                    OutlinedTextField(
                                                        value = apiId,
                                                        onValueChange = { viewModel.onApiIdChanged(it) },
                                                        label = { Text("API ID") },
                                                        placeholder = { Text("e.g., 123456") },
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
                                                        modifier = Modifier.fillMaxWidth().testTag("api_id_input"),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )

                                                    OutlinedTextField(
                                                        value = apiHash,
                                                        onValueChange = { viewModel.onApiHashChanged(it) },
                                                        label = { Text("API Hash") },
                                                        placeholder = { Text("e.g., abcd1234efgh5678") },
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = Indigo500,
                                                            unfocusedBorderColor = Slate700,
                                                            focusedLabelColor = Indigo400,
                                                            unfocusedLabelColor = Slate400,
                                                            focusedTextColor = Color.White,
                                                            unfocusedTextColor = Color.White
                                                        ),
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth().testTag("api_hash_input"),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )

                                                    OutlinedTextField(
                                                        value = phone,
                                                        onValueChange = { viewModel.onPhoneChanged(it) },
                                                        label = { Text("Phone Number") },
                                                        placeholder = { Text("e.g., +1234567890") },
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
                                                        modifier = Modifier.fillMaxWidth().testTag("phone_input"),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )

                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    Button(
                                                        onClick = { viewModel.sendAuthenticationCode() },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                                                        enabled = !isSendingCode,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(48.dp)
                                                            .testTag("btn_send_code"),
                                                        shape = RoundedCornerShape(14.dp)
                                                    ) {
                                                        if (isSendingCode) {
                                                            CircularProgressIndicator(
                                                                color = Color.White,
                                                                modifier = Modifier.size(18.dp),
                                                                strokeWidth = 2.dp
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("Requesting...")
                                                        } else {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Outlined.Send,
                                                                contentDescription = "Send",
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("Send Authentication Code", fontWeight = FontWeight.Bold)
                                                        }
                                                    }


                                                    } else {
                                                        // Session Key String Direct Import
                                                        OutlinedTextField(
                                                            value = sessionKeyInput,
                                                            onValueChange = { viewModel.onSessionKeyInputChanged(it) },
                                                            label = { Text("Session Key String") },
                                                            placeholder = { Text("Paste Pyrogram / Telethon Session String (e.g., 1BPhvYsw...)") },
                                                            colors = OutlinedTextFieldDefaults.colors(
                                                                focusedBorderColor = Indigo500,
                                                                unfocusedBorderColor = Slate700,
                                                                focusedLabelColor = Indigo400,
                                                                unfocusedLabelColor = Slate400,
                                                                focusedTextColor = Color.White,
                                                                unfocusedTextColor = Color.White
                                                            ),
                                                            minLines = 3,
                                                            maxLines = 5,
                                                            modifier = Modifier.fillMaxWidth().testTag("session_key_input"),
                                                            shape = RoundedCornerShape(12.dp)
                                                        )

                                                        OutlinedTextField(
                                                            value = phone,
                                                            onValueChange = { viewModel.onPhoneChanged(it) },
                                                            label = { Text("Account Label / Phone (Optional)") },
                                                            placeholder = { Text("e.g., +1 (555) 0199 or Work Account") },
                                                            colors = OutlinedTextFieldDefaults.colors(
                                                                focusedBorderColor = Indigo500,
                                                                unfocusedBorderColor = Slate700,
                                                                focusedLabelColor = Indigo400,
                                                                unfocusedLabelColor = Slate400,
                                                                focusedTextColor = Color.White,
                                                                unfocusedTextColor = Color.White
                                                            ),
                                                            singleLine = true,
                                                            modifier = Modifier.fillMaxWidth().testTag("session_alias_input"),
                                                            shape = RoundedCornerShape(12.dp)
                                                        )

                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        Button(
                                                            onClick = { viewModel.importSessionKey() },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                                                            enabled = !isVerifyingCode,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(48.dp)
                                                                .testTag("btn_import_session_key"),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            if (isVerifyingCode) {
                                                                CircularProgressIndicator(
                                                                    color = Color.White,
                                                                    modifier = Modifier.size(18.dp),
                                                                    strokeWidth = 2.dp
                                                                )
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Text("Authenticating Key...")
                                                            } else {
                                                                Icon(
                                                                    imageVector = Icons.Default.VpnKey,
                                                                    contentDescription = "Key",
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Text("Import & Activate Session Key", fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }
                                                LoginStep.ENTER_CODE -> {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.clickable { viewModel.resetStep() }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                            contentDescription = "Back",
                                                            tint = Indigo400,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Back to credentials", fontSize = 12.sp, color = Indigo400, fontWeight = FontWeight.Bold)
                                                    }

                                                    Column {
                                                        Text(
                                                            text = "Verify Identity",
                                                            color = Color.White,
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = "A temporary security OTP has been sent via Telegram.",
                                                            color = Slate400,
                                                            fontSize = 12.sp,
                                                            modifier = Modifier.padding(top = 4.dp)
                                                        )
                                                    }

                                                    Divider(color = Slate800.copy(alpha = 0.5f))

                                                    OutlinedTextField(
                                                        value = code,
                                                        onValueChange = { viewModel.onCodeChanged(it) },
                                                        label = { Text("Telegram Code") },
                                                        placeholder = { Text("Enter 5-digit code") },
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
                                                        modifier = Modifier.fillMaxWidth().testTag("code_input"),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )

                                                    AnimatedVisibility(
                                                        visible = requiresPassword,
                                                        enter = expandVertically() + fadeIn(),
                                                        exit = shrinkVertically() + fadeOut()
                                                    ) {
                                                        OutlinedTextField(
                                                            value = password,
                                                            onValueChange = { viewModel.onPasswordChanged(it) },
                                                            label = { Text("2-Step Password") },
                                                            placeholder = { Text("Enter cloud password") },
                                                            colors = OutlinedTextFieldDefaults.colors(
                                                                focusedBorderColor = Indigo500,
                                                                unfocusedBorderColor = Slate700,
                                                                focusedLabelColor = Indigo400,
                                                                unfocusedLabelColor = Slate400,
                                                                focusedTextColor = Color.White,
                                                                unfocusedTextColor = Color.White
                                                            ),
                                                            visualTransformation = if (showPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                                            trailingIcon = {
                                                                val icon = if (showPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                                                IconButton(onClick = { showPasswordVisible = !showPasswordVisible }) {
                                                                    Icon(imageVector = icon, contentDescription = "Toggle")
                                                                }
                                                            },
                                                            singleLine = true,
                                                            modifier = Modifier.fillMaxWidth().testTag("password_input"),
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                    }

                                                    Button(
                                                        onClick = { viewModel.verifyAuthenticationCode() },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                                                        enabled = !isVerifyingCode,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(48.dp)
                                                            .testTag("btn_verify_code"),
                                                        shape = RoundedCornerShape(14.dp)
                                                    ) {
                                                        if (isVerifyingCode) {
                                                            CircularProgressIndicator(
                                                                color = Color.White,
                                                                modifier = Modifier.size(18.dp),
                                                                strokeWidth = 2.dp
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("Connecting...")
                                                        } else {
                                                            Icon(
                                                                imageVector = Icons.Default.VerifiedUser,
                                                                contentDescription = "Verify",
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("Verify & Connect Session", fontWeight = FontWeight.Bold)
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

                            // Grid style Row
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .weight(2f)
                                            .height(80.dp)
                                            .clickable {
                                                clipboardManager.setText(AnnotatedString(session.sessionString))
                                                Toast.makeText(context, "Session string copied!", Toast.LENGTH_SHORT).show()
                                            },
                                        colors = CardDefaults.cardColors(containerColor = Slate900),
                                        shape = RoundedCornerShape(20.dp),
                                        border = BorderStroke(1.dp, Slate800)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Active Session String",
                                                color = Slate400,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = session.sessionString,
                                                    color = Indigo400,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy key",
                                                    tint = Indigo400,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }

                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(80.dp),
                                        colors = CardDefaults.cardColors(containerColor = Indigo600),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "API ID",
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = session.apiId,
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // Active Connection / Disconnect Banner
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Slate900),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, Slate800)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(modifier = Modifier.size(40.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .background(Slate800, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = "User",
                                                        tint = Slate400,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                // Emerald active dot
                                                Box(
                                                    modifier = Modifier
                                                        .size(11.dp)
                                                        .background(Color(0xFF10B981), CircleShape)
                                                        .border(2.dp, Slate900, CircleShape)
                                                        .align(Alignment.BottomEnd)
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = session.phone,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Authenticated via Telethon",
                                                    color = Slate400,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = { viewModel.logout() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Slate800,
                                                contentColor = Rose400
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(32.dp).testTag("logout_button")
                                        ) {
                                            Text("Disconnect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Direct Messages Block
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Slate900),
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, Slate800)
                                ) {
                                    Column {
                                        // Header inside chats
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "Direct Messages",
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                // 4 UNREAD badge style
                                                val totalUnread = cachedChats.sumOf { it.unreadCount }
                                                if (totalUnread > 0) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Indigo600.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                            .border(0.5.dp, Indigo400.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "$totalUnread UNREAD",
                                                            color = Indigo400,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "UP TO DATE",
                                                            color = Color(0xFF10B981),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }

                                            Button(
                                                onClick = { viewModel.fetchChats() },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Slate800,
                                                    contentColor = Indigo400
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                enabled = !isFetchingChats,
                                                modifier = Modifier.height(32.dp).testTag("refresh_chats_button")
                                            ) {
                                                if (isFetchingChats) {
                                                    CircularProgressIndicator(
                                                        color = Indigo400,
                                                        modifier = Modifier.size(12.dp),
                                                        strokeWidth = 2.dp
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Syncing...", fontSize = 11.sp)
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Refresh,
                                                        contentDescription = "Sync",
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Refresh", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Divider(color = Slate800.copy(alpha = 0.5f))

                                        if (cachedChats.isEmpty()) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 44.dp, horizontal = 16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Forum,
                                                    contentDescription = "No chats",
                                                    tint = Slate700,
                                                    modifier = Modifier.size(40.dp)
                                                )
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "No chats cached yet",
                                                    color = Slate400,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Sync with the active connection to load direct messages.",
                                                    color = Slate500,
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                        } else {
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                cachedChats.forEach { chat ->
                                                    ChatRowItem(chat = chat)
                                                    Divider(color = Slate800.copy(alpha = 0.4f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> { // Sessions Management Tab
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate900),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, Slate800)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Saved Local Sessions & Accounts",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = "Manage and switch between multiple connected Telegram profiles or session keys.",
                                                color = Slate400,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                viewModel.logout()
                                                activeTab = 0
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("btn_add_account")
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Account", modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("+ Add Account", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Divider(color = Slate800.copy(alpha = 0.5f))

                                    if (savedSessions.isEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 48.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Key,
                                                contentDescription = "Keys",
                                                tint = Slate700,
                                                modifier = Modifier.size(40.dp)
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "No saved sessions.",
                                                color = Slate400,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Sign in to a session from the Chats tab to display and manage active profiles.",
                                                color = Slate500,
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    } else {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.padding(top = 16.dp)
                                        ) {
                                            savedSessions.forEach { s ->
                                                val isCurrent = activeSession?.phone == s.phone
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            if (isCurrent) Indigo600.copy(alpha = 0.15f) else Color.Transparent,
                                                            RoundedCornerShape(16.dp)
                                                        )
                                                        .border(
                                                            1.dp,
                                                            if (isCurrent) Indigo400.copy(alpha = 0.4f) else Slate800,
                                                            RoundedCornerShape(16.dp)
                                                        )
                                                        .clickable { viewModel.selectSession(s.phone) }
                                                        .padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .background(
                                                                    if (isCurrent) Indigo600 else Slate800,
                                                                    CircleShape
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = if (isCurrent) "✓" else "🔑",
                                                                color = Color.White,
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                        Column {
                                                            Text(
                                                                text = s.phone,
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp
                                                            )
                                                            Text(
                                                                text = "API ID: ${s.apiId} • Stored Securely",
                                                                color = Slate400,
                                                                fontSize = 10.sp
                                                            )
                                                        }
                                                    }

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        if (isCurrent) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = "ACTIVE",
                                                                    color = Color(0xFF10B981),
                                                                    fontSize = 8.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }

                                                        IconButton(
                                                            onClick = { viewModel.deleteSession(s) },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.DeleteOutline,
                                                                contentDescription = "Delete",
                                                                tint = Rose400.copy(alpha = 0.8f),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // Profile / Telemetry Tab
                        item {
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
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = "Profile",
                                            tint = Indigo400,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Security Configuration",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }

                                    Divider(color = Slate800.copy(alpha = 0.5f))

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Slate950, RoundedCornerShape(16.dp))
                                            .border(1.dp, Slate800, RoundedCornerShape(16.dp))
                                            .padding(14.dp)
                                    ) {
                                        Text(
                                            text = "NON-CUSTODIAL HARDENING",
                                            color = Indigo400,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "All keys, credentials, and message headers stay strictly locked inside local SQLite on this device.",
                                            color = Slate400,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column {
                                                Text("Local Profiles", color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Text("${savedSessions.size}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Column {
                                                Text("Cached Contacts", color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Text("${cachedChats.size}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Column {
                                                Text("Client Mode", color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Text("STANDALONE", color = Color(0xFF10B981), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = "Shield",
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "End-to-End Cryptography",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "Keys never touch external APIs or central relays.",
                                                color = Slate400,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Indigo600.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudQueue,
                                                contentDescription = "Cloud",
                                                tint = Indigo400,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "Telethon Nodes Integration",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "Direct connection to your self-hosted FastAPI server.",
                                                color = Slate400,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> { // Nodes Connection & Proxy Tab
                        item {
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
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Hub,
                                                contentDescription = "Nodes",
                                                tint = Indigo400,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(
                                                text = "Network & Proxy Nodes",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = if (proxyPingMs != null) Color(0xFF10B981).copy(alpha = 0.2f) else Slate800,
                                            border = BorderStroke(1.dp, if (proxyPingMs != null) Color(0xFF10B981) else Slate700)
                                        ) {
                                            Text(
                                                text = if (proxyPingMs != null) "$proxyPingMs ms" else "Offline",
                                                color = if (proxyPingMs != null) Color(0xFF10B981) else Slate400,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Divider(color = Slate800.copy(alpha = 0.5f))

                                    // Connection Mode Selector
                                    Column {
                                        Text(
                                            text = "NETWORK CONNECTIVITY MODE",
                                            color = Slate400,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            letterSpacing = 1.sp,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Slate950, RoundedCornerShape(14.dp))
                                                .padding(4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            listOf(
                                                ProxyType.DIRECT to "Direct (Wi-Fi / Data)",
                                                ProxyType.MTPROTO to "MTProto Proxy",
                                                ProxyType.SOCKS5 to "SOCKS5 Proxy"
                                            ).forEach { (type, label) ->
                                                val isSelected = proxyType == type
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (isSelected) Indigo600 else Color.Transparent)
                                                        .clickable { viewModel.onProxyTypeChanged(type) }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = label,
                                                        color = if (isSelected) Color.White else Slate400,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 11.sp,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (proxyType == ProxyType.DIRECT) {
                                        // Direct Connection Banner
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color(0x1510B981)),
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Wifi,
                                                    contentDescription = "Direct Network",
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = "Direct Network Active",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = "App connects directly using Wi-Fi, Mobile Data (4G/5G), or Ethernet. No proxy needed.",
                                                        color = Slate400,
                                                        fontSize = 11.sp,
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // Optional Proxy Presets
                                        Column {
                                            Text(
                                                text = "OPTIONAL PROXY PRESETS",
                                                color = Slate400,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                letterSpacing = 1.sp,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                viewModel.proxyPresets.filter { it.type == proxyType }.forEach { preset ->
                                                    val isCurrent = proxyHost == preset.host
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(if (isCurrent) Indigo600.copy(alpha = 0.15f) else Slate950)
                                                            .border(1.dp, if (isCurrent) Indigo500 else Slate800, RoundedCornerShape(12.dp))
                                                            .clickable { viewModel.applyProxyPreset(preset) }
                                                            .padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = preset.name,
                                                                color = Color.White,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                text = "${preset.host}:${preset.port} • ${preset.location}",
                                                                color = Slate400,
                                                                fontSize = 10.sp,
                                                                modifier = Modifier.padding(top = 2.dp)
                                                            )
                                                        }
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = Slate800,
                                                            border = BorderStroke(1.dp, Slate700)
                                                        ) {
                                                            Text(
                                                                text = preset.type.name,
                                                                fontSize = 9.sp,
                                                                color = Indigo400,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Detailed Configuration Inputs
                                    if (proxyType != ProxyType.DIRECT) {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                OutlinedTextField(
                                                    value = proxyHost,
                                                    onValueChange = { viewModel.onProxyHostChanged(it) },
                                                    label = { Text("Proxy Server / Host") },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = Indigo500,
                                                        unfocusedBorderColor = Slate700,
                                                        focusedLabelColor = Indigo400,
                                                        unfocusedLabelColor = Slate400,
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White
                                                    ),
                                                    singleLine = true,
                                                    modifier = Modifier.weight(2f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                OutlinedTextField(
                                                    value = proxyPort,
                                                    onValueChange = { viewModel.onProxyPortChanged(it) },
                                                    label = { Text("Port") },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = Indigo500,
                                                        unfocusedBorderColor = Slate700,
                                                        focusedLabelColor = Indigo400,
                                                        unfocusedLabelColor = Slate400,
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White
                                                    ),
                                                    singleLine = true,
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                            }

                                            if (proxyType == ProxyType.MTPROTO) {
                                                OutlinedTextField(
                                                    value = proxySecret,
                                                    onValueChange = { viewModel.onProxySecretChanged(it) },
                                                    label = { Text("MTProto Secret Key") },
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
                                            }

                                            Button(
                                                onClick = { viewModel.testProxyPing() },
                                                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                                enabled = !isTestingProxy,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(42.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                if (isTestingProxy) {
                                                    CircularProgressIndicator(
                                                        color = Indigo400,
                                                        modifier = Modifier.size(16.dp),
                                                        strokeWidth = 2.dp
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Pinging Proxy Socket...", fontSize = 12.sp)
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Speed,
                                                        contentDescription = "Ping",
                                                        tint = Indigo400,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Test Connection Latency", fontSize = 12.sp, color = Color.White)
                                                }
                                            }
                                        }
                                    }

                                    // Terminal Logs Console
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "PROXY & NETWORK LOGS",
                                            color = Slate500,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "[SYSTEM] Direct Client Active\n[PROXY] ${proxyType.name} -> ${if (proxyHost.isBlank()) "Direct Network" else "$proxyHost:$proxyPort"}\n[STATUS] Handshake ready - Direct connection",
                                            color = Color(0xFF10B981),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 14.sp
                                        )
                                    }
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
fun ChatRowItem(chat: SavedChat) {
    // Elegant high density colored avatar matching initials
    val avatarBg = remember(chat.id) {
        val colors = listOf(
            Color(0xFF3B82F6), // Blue
            Color(0xFF10B981), // Emerald
            Color(0xFF8B5CF6), // Violet
            Color(0xFFF59E0B), // Amber
            Color(0xFFEC4899), // Pink
            Color(0xFF06B6D4)  // Cyan
        )
        colors[(chat.id.hashCode() % colors.size).let { if (it < 0) -it else it }]
    }

    val initials = remember(chat.name) {
        chat.name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Could display chat messages in a detailed sub-view if needed */ }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // High-density rounded 12.dp initials box instead of plain circles
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(avatarBg.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .border(0.5.dp, avatarBg.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = avatarBg,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name and Message Column
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = chat.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (chat.username != "No username") {
                    Text(
                        text = "@${chat.username}",
                        color = Indigo400,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = chat.lastMessage.ifBlank { "No messages" },
                color = if (chat.unreadCount > 0) Color.White else Slate400,
                fontWeight = if (chat.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right side indicators (Phone / Unread badge)
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = chat.phone,
                color = Slate400,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            if (chat.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(Indigo600, CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chat.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

